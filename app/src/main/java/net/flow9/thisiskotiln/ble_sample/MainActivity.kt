package net.flow9.thisiskotiln.ble_sample

import MainViewModel
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import net.flow9.thisiskotiln.ble_sample.data.repository.BleRepositoryImpl
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.presentation.main.MainScreen
import net.flow9.thisiskotiln.ble_sample.ui.theme.Ble_sampleTheme
import net.flow9.thisiskotiln.ble_sample.util.PermissionChecker

class MainActivity : ComponentActivity() {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // BLE 통신 관련 권한이 허용되었을 때만 동작을 허용한다.
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 허용되지 않은 권한 조회 후 권한 요청, 초기 상태면 여기에 4개의 권한이 모두 들어가 있어야되는데.
        val missingPermissions = PermissionChecker.getMissingPermissions(this)

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions)
        }

        val bleRepository = BleRepositoryImpl(this, bluetoothAdapter, null, null)

        // viewModel 생성
        val viewModel = MainViewModel(this@MainActivity, bleRepository)

        viewModel.setMyUserCard(UserCard(1, "seongminYoo", "Backend Engineer"))

        bleRepository.setOnUserCardReceivedListener { viewModel.onUserCardReceived(it) }
        bleRepository.setOnDeviceFoundListener { viewModel.onDeviceFound(it) }

        setContent {
            Ble_sampleTheme {
                MainScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}