package net.flow9.thisiskotiln.ble_sample.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow
import net.flow9.thisiskotiln.ble_sample.data.ble.advertiser.BleAdvertiser
import net.flow9.thisiskotiln.ble_sample.data.ble.gatt.GattClientManager
import net.flow9.thisiskotiln.ble_sample.data.ble.gatt.GattServerManager
import net.flow9.thisiskotiln.ble_sample.data.ble.scanner.BleScanner
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.domain.repository.BleRepository

/**
 * BleRepository 구현부
 */
class BleRepositoryImpl (
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private var onUserCardReceived: ((UserCard) -> Unit)?,
    private var onDeviceFound: ((BluetoothDevice) -> Unit)?,
) : BleRepository {

    private var gattServerManager: GattServerManager? = null
    private var gattClientManager: GattClientManager? = null
    private var myUserCard: UserCard? = null

    private var bleAdvertiser: BleAdvertiser
    private var bleScanner: BleScanner

    init {
        bleScanner = BleScanner(bluetoothAdapter, null)
        bleAdvertiser = BleAdvertiser(bluetoothAdapter)
    }

    fun setOnUserCardReceivedListener(listener: (UserCard) -> Unit) {
        Log.d("BleRepository", "onUserCardReceivedListener 설정 $listener")
        onUserCardReceived = listener
        gattClientManager = GattClientManager(context, onUserCardReceived)
    }

    fun setOnDeviceFoundListener(listener: (BluetoothDevice) -> Unit) {
        Log.d("BleRepository", "onDeviceFoundListener 설정 $listener")
        onDeviceFound = listener
        bleScanner = BleScanner(bluetoothAdapter, onDeviceFound)
    }

    // GATT 서버 열고, context와 userCard를 넘긴다.
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startGattServer() {
        Log.d("BleRepository", "GATT 서버 시작 $myUserCard")
        gattServerManager = GattServerManager(context, myUserCard!!)
        gattServerManager?.startGattServer()
    }

    // GATT 서버 매니저 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun stopGattServer() {
        gattServerManager?.stopGattServer()
    }

    // BLE Adveriser 객체 생성 및 startAdvertising
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun startAdvertising() {
        bleAdvertiser.startAdvertising()
    }

    // BLE Adveriser 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun stopAdvertising() {
        bleAdvertiser.stopAdvertising()
    }

    // GATT 클라이언트 인스턴스 선언 및 할당, bleScanner 선언 및 connectToDevice로 넘김
    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ])
    override fun startScan() {
        // Scan도중 콜백함수에서 connectToDevice로 넘긴다.
        bleScanner.startScan()
    }

    // ble Scanner 인스턴스 초기화
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun stopScan() {
        gattClientManager?.disconnect()
        bleScanner.stopScan()
    }

    // 디바이스 발견 시 GATT Client로 연결
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BluetoothDevice) {
        // 로그 꼬일 수 있어서 disconnect하고 들어감
        gattClientManager?.disconnect()
        gattClientManager?.connectToDevice(device)
    }

    // 내가 넘겨줄 유저 카드 정보를 세팅
    override fun setUserCard(userCard: StateFlow<UserCard?>) {
        this.myUserCard = userCard.value
    }
}