package net.flow9.thisiskotlin.ble_sample.data.ble.model

import java.util.UUID

/**
 * BLE 통신에서 통신 대상을 식별하는 키.
 * SERVICE_UUID : GATT서버에서 SERVICE를 식별하는 UUID
 * CHARACTERISTIC_UUID : 서비스 내의 CHARACTERISTIC UUID가 일치하는 특성 식별용 UUID
 */
object BleConstants {
    val SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
    val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
}
