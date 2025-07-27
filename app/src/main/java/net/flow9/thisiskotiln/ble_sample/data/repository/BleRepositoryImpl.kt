package net.flow9.thisiskotiln.ble_sample.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.flow9.thisiskotiln.ble_sample.data.ble.advertiser.BleAdvertiser
import net.flow9.thisiskotiln.ble_sample.data.ble.gatt.GattClientManager
import net.flow9.thisiskotiln.ble_sample.data.ble.gatt.GattServerManager
import net.flow9.thisiskotiln.ble_sample.data.ble.scanner.BleScanner
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.domain.repository.BleRepository
import javax.inject.Inject

/**
 * BleRepository 구현부
 */
class BleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
) : BleRepository {

    // BleRepository에서 다룰 gattServerManager와 gattClientManager.
    // 여기서 인스턴스를 2~3개 이상 생성해서 오류가 나는건가?
    private var gattServerManager: GattServerManager? = null
    private var gattClientManager: GattClientManager? = null

    // 사용자의 카드 정보
    private var myUserCard: UserCard? = null

    // BleRepo에서 직접 다룰 advertiser객체와 scanner객체
    private lateinit var bleAdvertiser: BleAdvertiser
    private lateinit var bleScanner: BleScanner

    // Scanner : gattClient에서 유저의 카드 정보를 받아내는 데 성공하면 동작하는 콜백함수
    private var onUserCardReceived: ((UserCard) -> Unit)? = null

    // Scanner에서 상대방의 기기를 발견할 시 동작하는 콜백함수
    private var onDeviceFound: ((BluetoothDevice) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // BLE Advertiser의 초기 선언을 위해 발생하는 함수
    private fun initBleAdvertiserIfNeeded() {
        if (!::bleAdvertiser.isInitialized) {
            bleAdvertiser = BleAdvertiser(bluetoothAdapter)
        }
    }

    // BLE Scanner의 초기 선언을 위해 발생하는 함수
    private fun initBleScannerIfNeeded() {
        if (!::bleScanner.isInitialized) {
            if (onDeviceFound != null) {
                bleScanner = BleScanner(bluetoothAdapter, onDeviceFound!!)
            } else {
                throw IllegalStateException("onDeviceFound가 설정되지 않았음.")
            }
        }
    }

    override fun setOnUserCardReceivedListener(listener: (UserCard) -> Unit) {
        onUserCardReceived = listener
        if (gattClientManager == null) gattClientManager = GattClientManager(context, onUserCardReceived)
    }

    override fun setOnDeviceFoundListener(listener: (BluetoothDevice) -> Unit) {
        onDeviceFound = listener
        if (!::bleScanner.isInitialized) {
            bleScanner = BleScanner(bluetoothAdapter, listener)
        }
    }

    // GATT 서버 열고, context와 userCard를 넘긴다.
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startGattServer() {
        if (gattServerManager == null) {
            myUserCard?.let { card ->
                Log.d("BleRepository", "GATT 서버 시작 $card")
                gattServerManager = GattServerManager(context, card)
                gattServerManager?.startGattServer()
            } ?: Log.e("BleRepository", "myUserCard가 null이라서 서버를 시작할 수 없다.")
        } else {
            Log.w("BleRepo", "이미 열려있는 GATT 서버 존재함")
        }
    }

    // GATT 서버 매니저 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun stopGattServer() {
        gattServerManager?.stopGattServer()
        gattServerManager = null
    }

    // BLE Adveriser 객체 생성 및 startAdvertising
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun startAdvertising() {
        initBleAdvertiserIfNeeded()
        bleAdvertiser.startAdvertising()
    }

    // BLE Adveriser 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun stopAdvertising() {
        bleAdvertiser.stopAdvertising()
    }

    /**
     * BLE Scan을 시작합니다.
     * 1. Gatt Server가 열려있으면 Server와 Client가 동시에 열리는 동작을 방지하기 위해 Server를 종료합니다.
     * 2. BleScanner가 선언되지 않았다면 선언합니다.
     * 3. 스캔을 시작합니다.
     */
    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ])
    override fun startScan() {
        if (gattServerManager != null) stopGattServer()

        initBleScannerIfNeeded()
        bleScanner.startScan()
    }

    /**
     * BLE Scan을 종료합니다.
     * 1. gattClientManager의 단일 흐름 처리를 위해 disconnect를 발동시킵니다.
     * 2. 스캔을 종료합니다.
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun stopScan() {
        scope.launch {
            if (gattClientManager != null) {
                gattClientManager?.disconnect()
                delay(100)
                bleScanner.stopScan()
            }
        }
    }

    /**
     * 디바이스를 연결합니다.
     * 1. gattClientManager의 단일 흐름 처리를 위해 disconnect를 발동시킨다.
     * 2. 이후, gattClientManager의 connectToDevice를 발동시킨다.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connectToDevice(device: BluetoothDevice) {
        // 로그 꼬일 수 있어서 disconnect하고 들어감
        gattClientManager?.disconnect()
        delay(200)
        gattClientManager?.connectToDevice(device)
    }

    /**
     * 넘겨줄 사용자의 카드 정보를 세팅합니다.
     * ViewModel의 stateFlow 카드를 스냅샷으로 전환하여 전달합니다.
     */
    override fun setUserCard(userCard: StateFlow<UserCard?>) {
        this.myUserCard = userCard.value
    }
}