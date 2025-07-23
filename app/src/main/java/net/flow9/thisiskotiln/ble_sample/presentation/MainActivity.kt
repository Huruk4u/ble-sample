package net.flow9.thisiskotiln.ble_sample.presentation

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

        val missingPermissions = PermissionChecker().getMissingPermissions(this)
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions)
        }

        setContent {
            Ble_sampleTheme {
                }
                MainScreen(
                    viewModel = viewModel,
                    myUserCard = myCard
                )
            }
        }
    }
}