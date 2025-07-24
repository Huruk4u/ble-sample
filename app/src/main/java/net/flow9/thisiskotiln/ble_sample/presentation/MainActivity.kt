package net.flow9.thisiskotiln.ble_sample.presentation

import MainViewModel
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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

        // 허용되지 않은 권한 조회 후 권한 요청
        val missingPermissions = PermissionChecker().getMissingPermissions(this)
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions)
        }

        // Repository, viewModel간 의존 관계 설정
        val bleRepository = BleRepositoryImpl(this@MainActivity, bluetoothAdapter, null, null)
        // viewModel, repository의 상호 의존 관계 때문에 동시에 생성해야함.
        val viewModel = MainViewModel(bleRepository)

        // DI처리, 초기에 null값으로 넘겼던 콜백함수를 세팅함.
        bleRepository.setOnUserCardReceivedListener { receivedCard ->
            viewModel.onUserCardReceived(receivedCard)
        }

        bleRepository.setOnDeviceFoundListener { device ->
            viewModel.onDeviceFound(device)
        }

        viewModel.setMyUserCard(UserCard(1, "seongminYoo", "Backend Engineer"))

        setContent {
            Ble_sampleTheme {
                MainScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
