package net.flow9.thisiskotiln.ble_sample.presentation.main

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    myUserCard: UserCard
) {
    var isExchanging by remember { mutableStateOf(false) }

    // 상대 카드 받았는지 관찰
    val receivedCard by viewModel.receivedUserCard.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "내 카드", style = MaterialTheme.typography.titleLarge)
        Text(text = "${myUserCard.userName} / ${myUserCard.position}")

        Button(
            onClick = {
                if (isExchanging) {
                    viewModel.stopBleExchange()
                } else {
                    viewModel.onUserCardReceived(myUserCard)
                    viewModel.startBleExchange(myUserCard)
                }
                isExchanging = !isExchanging
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isExchanging) "BLE 종료" else "BLE 명함 교환 시작")
        }

        Divider()

        Text(text = "받은 카드", style = MaterialTheme.typography.titleLarge)
        if (receivedCard != null) {
            Text("이름: ${receivedCard?.userName}")
            Text("직책: ${receivedCard?.position}")
        } else {
            Text("아직 카드 없음")
        }
    }
}
