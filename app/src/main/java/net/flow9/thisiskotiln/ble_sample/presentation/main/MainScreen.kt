package net.flow9.thisiskotiln.ble_sample.presentation.main

import MainViewModel
import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    myUserCard: UserCard
) {
    val receivedCard by viewModel.receivedCard.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "내 카드", style = MaterialTheme.typography.titleLarge)
        Text(text = "${myUserCard.userName} / ${myUserCard.position}")

        // BLE 중앙 기기, Scanner버튼
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            onClick = {
                if (!viewModel.isBleActivate.value) {
                    viewModel.startScanning()
                } else {
                    viewModel.stopScanning()
                }
            }) { Text("내 카드 주기") }

        Spacer(modifier = Modifier.height(16.dp))

        // BLE 주변 기기, Advertising버튼
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            onClick = {
                if (!viewModel.isBleActivate.value) {
                    viewModel.startAdvertising()
                } else {
                    viewModel.stopAdvertising()
                }
            }) { Text("상대 카드를 받아오기") }

        Spacer(modifier = Modifier.height(16.dp));

        Card() {
            Text(text = "상대 카드 정보")
            Text(text = "아이디 : ${receivedCard?.userId}")
            Text(text = "이름 : ${receivedCard?.userName}")
            Text(text = "포지션 : ${receivedCard?.position}")
        }
    }
}
