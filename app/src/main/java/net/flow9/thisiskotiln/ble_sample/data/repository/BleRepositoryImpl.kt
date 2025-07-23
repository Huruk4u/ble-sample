package net.flow9.thisiskotiln.ble_sample.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
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
) : BleRepository {

    private var gattServerManager: GattServerManager? = null
    private var gattClientManager: GattClientManager? = null
    private var bleAdvertiser: BleAdvertiser? = null
    private var bleScanner: BleScanner? = null
    private var myUserCard: UserCard? = null

    private var onUserCardReceived: ((UserCard) -> Unit)? = null

    // 콜백함수를 설정
    fun setOnUserCardReceivedListener(listener: (UserCard) -> Unit) {
        onUserCardReceived = listener
    }

    private fun handleUserCardReceived(card: UserCard) {
        onUserCardReceived?.invoke(card)
    }

    // BLE Adveriser 객체 생성 및 startAdvertising
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun startAdvertising() {
        bleAdvertiser = BleAdvertiser(bluetoothAdapter)
        bleAdvertiser?.startAdvertising()
    }

    // BLE Adveriser 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    override fun stopAdvertising() {
        bleAdvertiser?.stopAdvertising()
        bleAdvertiser = null
    }

    // GATT 서버 열고, context와 userCard를 넘긴다.
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startGattServer() {
        myUserCard?.let { card ->
            gattServerManager = GattServerManager(context,card)
            gattServerManager?.startGattServer()
        }
    }

    // GATT 서버 매니저 인스턴스 초기화
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun stopGattServer() {
        gattServerManager?.stopGattServer()
        gattServerManager = null
    }

    // GATT 클라이언트 인스턴스 선언 및 할당, bleScanner 선언 및 connectToDevice로 넘김
    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ])
    override fun startScan() {
        gattClientManager = GattClientManager(context, onUserCardReceived)
        // Scan도중 콜백함수에서 connectToDevice로 넘긴다.
        bleScanner = BleScanner(bluetoothAdapter) { device ->
            connectToDevice(device)
        }
        bleScanner?.startScan()
    }

    // ble Scanner 인스턴스 초기화
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun stopScan() {
        bleScanner?.stopScan()
        bleScanner = null
        gattClientManager?.disconnect()
    }

    // 디바이스 발견 시 GATT Client로 연결
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BluetoothDevice) {
        gattClientManager?.connectToDevice(device)
    }

    // usercard 세팅
    override fun setUserCard(userCard: UserCard) {
        this.myUserCard = userCard
    }


}