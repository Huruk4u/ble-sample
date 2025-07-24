package net.flow9.thisiskotiln.ble_sample.presentation.main

import MainViewModel
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val receivedCard by viewModel.receivedCard.collectAsState()

    // BLE 통신 여부를 관리하는 상태 필드
    val isScanning by viewModel.isScanning.collectAsState()
    val isAdvertising by viewModel.isAdvertising.collectAsState()

    // bluetooth 활성화 여부 체크
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val bluetoothEnabled = remember { mutableStateOf(checkBluetoothEnabled(context)) }

    // 스캔한 디바이스 결과를 조회하는 상태 필드
    val scanResults by viewModel.scanResult.collectAsState()

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothEnabled.value = checkBluetoothEnabled(context)
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "블루투스 활성화에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Bluetooth 활성화 함수
    fun ensureBluetoothEnabled() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "내 카드", style = MaterialTheme.typography.titleLarge)
        Text(text = "${viewModel.myUserCard.value?.userId} / ${viewModel.myUserCard.value?.userName} / ${viewModel.myUserCard.value?.position}")

        // BLE 통신이 활성화 되어있지 않은 상태에서만 버튼을 활성화 한다.
        if (!isScanning && !isAdvertising) {
            // BLE 중앙 기기, Scan 활성화 버튼
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                onClick = {
                    if (bluetoothEnabled.value && bluetoothAdapter.isEnabled) {
                        viewModel.startAdvertising()
                    } else {
                        ensureBluetoothEnabled()
                    }
                    }) { Text("내 카드 주기") }

            // BLE 주변 기기, Advertising버튼
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                onClick = {
                    if (bluetoothEnabled.value && bluetoothAdapter.isEnabled) {
                        viewModel.startScanning()
                    } else {
                        ensureBluetoothEnabled()
                    }
                }) { Text("상대 카드를 받아오기") }
        } else {

            if (isScanning) {
                // 스캔을 시작할 경우에만 동작한다.
                Text("주변 BLE 기기 목록")
                LazyColumn {
                    items(scanResults) { device ->
                        Text("${device?.name ?: "이름없음"} | ${device?.address}}")
                    }
                }
            }

            // 스캔 중이거나 광고 중인 상태라면, 비활성화를 해주는 버튼
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                onClick = {
                    if (isScanning) {
                        viewModel.stopScanning()
                    } else {
                        viewModel.stopAdvertising()
                    }
                },
            ) { Text("연결 종료") }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "상대 카드 정보")
            Text(text = "아이디 : ${receivedCard?.userId}")
            Text(text = "이름 : ${receivedCard?.userName}")
            Text(text = "포지션 : ${receivedCard?.position}")
        }
    }

}

fun checkBluetoothEnabled(context: Context): Boolean {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    return bluetoothAdapter?.isEnabled == true
}
