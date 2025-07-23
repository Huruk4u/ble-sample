import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.domain.repository.BleRepository

class MainViewModel(
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _receivedCard = MutableStateFlow<UserCard?>(null)
    val receivedCard: StateFlow<UserCard?> = _receivedCard

    // Scanning 활성화의 상태를 나타내는 필드
    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> = _isScanning


    // Advertising 활성화 상태를 나타내는 필드
    private val _isAdvertising = MutableStateFlow<Boolean>(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising


    // 데이터를 받아오는 쪽, 굳이 GATT 서버를 열지 않아도 데이터를 받을 수 있다.
    // BLE통신 중앙 역할. 광고한 기기를 탐색한다.
    fun startScanning() {
        bleRepository.startScan()
        _isScanning.value = true
    }

    // Scanning 종료
    fun stopScanning() {
        bleRepository.stopScan()
        _isScanning.value = false
    }

    // 데이터를 주는 쪽, GATT 서버를 열어서 광고를 한다.
    // Gatt Client. Scanner가 찾아오도록 만든다.
    fun startAdvertising() {
        bleRepository.startGattServer()
        bleRepository.startAdvertising()
        _isAdvertising.value = true
    }

    // Advertising 종료
    fun stopAdvertising() {
        bleRepository.stopAdvertising()
        bleRepository.stopGattServer()
        _isAdvertising.value = false
    }

    fun onDeviceFound(device: BluetoothDevice) {
        bleRepository.connectToDevice(device)
    }

    // 광고 기기로부터 받아온 카드를 viewModel의 카드에 저장한다.
    fun onUserCardReceived(card: UserCard) {
        _receivedCard.value = card
    }
}