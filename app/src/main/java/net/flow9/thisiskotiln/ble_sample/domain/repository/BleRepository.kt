package net.flow9.thisiskotiln.ble_sample.domain.repository

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard

/**
 * BLE 통신으로 데이터 서빙을 하기 위한 인터페이스
 * 샘플 코드이므로, 불러오는 데이터는 GATT 서버 기기의 UserCard다.
 */
interface BleRepository {
    // BLE Advertise 시작
    fun startAdvertising()
    
    // BLE Advertise 종료
    fun stopAdvertising()
    
    // GATT Server 시작
    fun startGattServer()
    
    // GATT Server 종료
    fun stopGattServer()
    
    // BLE Advertise Scan 시작
    fun startScan()
    
    // BLE Advertise Scan 종료
    fun stopScan()
    
    // 디바이스 연결
    fun connectToDevice(device: BluetoothDevice)

    // userCard 데이터 바인딩
    fun setUserCard(userCard: StateFlow<UserCard?>)
}