package net.flow9.thisiskotiln.ble_sample.presentation

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import net.flow9.thisiskotiln.ble_sample.data.repository.BleRepositoryImpl
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotiln.ble_sample.presentation.main.MainScreen
import net.flow9.thisiskotiln.ble_sample.presentation.main.MainViewModel
import net.flow9.thisiskotiln.ble_sample.ui.theme.Ble_sampleTheme

class MainActivity : ComponentActivity() {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    // accompanist로 권한을 선언적으로 처리했다.
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (bluetoothAdapter == null) {
            Log.e("MainActivity", "이 기기는 블루투스를 지원하지 않습니다.")
            finish()
            return
        }

        setContent {
            Ble_sampleTheme {

                // 권한 목록 선언
                val permissions = rememberMultiplePermissionsState(
                    listOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                )

                // 권한이 모두 허용됐을 때는 정상적으로 액티비티 동작
                if (permissions.allPermissionsGranted) {

                    val myCard = remember {
                        UserCard(
                            userId = 1,
                            userName = "seongminYoo",
                            position = "Backend Engineer"
                        )
                    }

                    // viewModel과 repository가 상호 의존적인데.. 이걸 어떻게 처리해주지?
                    val viewModelRef = remember { mutableStateOf<MainViewModel?>(null) }

                    val repository = remember {
                        BleRepositoryImpl(
                            context = this@MainActivity,
                            bluetoothAdapter = bluetoothAdapter,
                            onUserCardReceived = { card ->
                                viewModelRef.value?.onUserCardReceived(card)
                            }
                        ).apply {
                            setUserCard(myCard)
                        }
                    }

                    val viewModel = remember {
                        MainViewModel(repository)
                    }.also {
                        viewModelRef.value = it
                    }


                    MainScreen(
                        viewModel = viewModel,
                        myUserCard = myCard
                    )
                } else {
                    LaunchedEffect(Unit) {
                        permissions.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }
}