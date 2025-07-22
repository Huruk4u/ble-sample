package net.flow9.thisiskotiln.ble_sample.presentation.main

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.domain.repository.BleRepository

/**
 * MainActivity에 보여줄거 UserCard밖에 없으니까. 이것만 정의할거다.
 * UI에 필요한 데이터 정의 및 데이터를 받아오기 위한 비즈니스 로직 정의
 */
class MainViewModel (
    private val bleRepository: BleRepository
) : ViewModel() {
    
    private val _receivedUserCard = MutableStateFlow<UserCard?>(null)
    val receivedUserCard: StateFlow<UserCard?> = _receivedUserCard

    /**
     * Ble통신을 시작한다.
     * 1. BLE Advertise 시작
     * 2. GATT Server 시작
     * 3. BLE Advertise Scan 시작
     */
    fun startBleExchange(userCard: UserCard) {
        bleRepository.startAdvertising()
        bleRepository.startGattServer()
        bleRepository.startScan()
    }

    /**
     * Ble통신을 종료한다.
     * 1. BLE Advertise 종료
     * 2. GATT Server 종료
     * 3. BLE Advertise Scan 종료
     */
    fun stopBleExchange() {
        bleRepository.stopAdvertising()
        bleRepository.stopGattServer()
        bleRepository.stopScan()
    }

    /**
     * BLE 통신으로 발견한 디바이스 연결
     */
    fun onDeviceFound(device: BluetoothDevice) {
        bleRepository.connectToDevice(device)
    }

    /**
     * BLE통신으로 UserCard를 받아온다.
     */
    fun onUserCardReceived(card: UserCard) {
        _receivedUserCard.value = card;
    }
}