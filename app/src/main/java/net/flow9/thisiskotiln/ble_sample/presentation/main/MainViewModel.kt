import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.flow9.thisiskotiln.ble_sample.data.repository.BleRepositoryImpl
import net.flow9.thisiskotiln.ble_sample.domain.model.BleDeviceInfo
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.domain.repository.BleRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val bleRepository: BleRepositoryImpl
) : ViewModel() {

    init {
        bleRepository.setOnUserCardReceivedListener { card ->
            onUserCardReceived(card)
        }

        bleRepository.setOnDeviceFoundListener { device ->
            onDeviceFound(device)
        }
    }

    private val _myUserCard = MutableStateFlow<UserCard?>(null)
    val myUserCard: StateFlow<UserCard?> = _myUserCard

    private val _receivedCard = MutableStateFlow<UserCard?>(null)
    val receivedCard: StateFlow<UserCard?> = _receivedCard

    // Scanning 활성화의 상태를 나타내는 필드
    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    // Advertising 활성화 상태를 나타내는 필드
    private val _isAdvertising = MutableStateFlow<Boolean>(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising

    // 통신 중인 scan결과 목록
    private val _scanResult = MutableStateFlow<List<BleDeviceInfo?>>(emptyList())
    val scanResult: StateFlow<List<BleDeviceInfo?>> = _scanResult

    fun setMyUserCard(card: UserCard) {
        _myUserCard.value = card;
    }

    // 데이터를 받아오는 쪽, 굳이 GATT 서버를 열지 않아도 데이터를 받을 수 있다.
    // BLE통신 중앙 역할. 광고한 기기를 탐색한다.
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun startScanning() {
        bleRepository.startScan()
        _isScanning.value = true
    }

    // Scanning 종료
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun stopScanning() {
        bleRepository.stopScan()
        _isScanning.value = false
    }

    // 데이터를 주는 쪽, GATT 서버를 열어서 광고를 한다.
    // Gatt Client. Scanner가 찾아오도록 만든다.
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE])
    fun startAdvertising() {
        // 허용된 권한 목록 체크하기
        bleRepository.setUserCard(myUserCard)

        bleRepository.startGattServer()

        Handler(Looper.getMainLooper()).postDelayed({
            bleRepository.startAdvertising()
        }, 300)

        _isAdvertising.value = true
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE])
    // Advertising 종료
    fun stopAdvertising() {
        bleRepository.stopAdvertising()
        _isAdvertising.value = false
        bleRepository.stopGattServer()
    }

    @SuppressLint("MissingPermission")
    fun onDeviceFound(device: BluetoothDevice) {

        Log.d("MainViewModel", "디바이스를 발견하여 GattClient생성")
        viewModelScope.launch {
            bleRepository.connectToDevice(device)
        }
        val deviceInfo = BleDeviceInfo(device.name, device.address)
        _scanResult.value = _scanResult.value.filter { it?.address != deviceInfo.address } + deviceInfo
    }

    // 광고 기기로부터 받아온 카드를 viewModel의 카드에 저장한다.
    fun onUserCardReceived(card: UserCard) {
        _receivedCard.value = card
    }
}