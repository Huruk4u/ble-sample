package net.flow9.thisiskotiln.ble_sample.domain.model

// 주변의 검색 중인 기기를 출력하기 위함
data class BleDeviceInfo (
    val name: String,
    val address: String,
)