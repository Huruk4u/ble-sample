package net.flow9.thisiskotiln.ble_sample.data.ble.advertiser

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import net.flow9.thisiskotiln.ble_sample.data.ble.gatt.GattServerManager
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotlin.ble_sample.data.ble.model.BleConstants

/**
 * GATT 서버를 열고 나서, 플루딩을 수행하기 위한 Advertiser 클래스
 */
class BleAdvertiser (
    private val bluetoothAdapter: BluetoothAdapter,
) {
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    /**
     * Advertising을 시작한다.
     * 1. Bluetooth, BluetoothLeAdvertiser 활성화 확인
     * 2. Advertising 세팅, 데이터, 콜백함수 준비 후, BLE 광고 시작
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun startAdvertising() {
        // bluetoothLeAdvertiser를 호출한다.
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        // Advertise 옵션을 세팅한다.
        // 근거리 통신, 전송 세기 높게, connect option true로 설정
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()

        // Advertise데이터 설정
        // 디바이스 명, 서비스 UUID
        // 동봉?할 수 있는 데이터의 크기가 31바이트로 제한되어있으므로, Device Name을 포함하지 않는다.
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        // BLE advertising 시작, 설정한 옵션과 data, callback을 넘겨 advertising을 시작한다.
        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertisingCallback)
        Log.d("BleAdvertiser", "BLE 광고 시작")
    }

    // BLE advertising 중지
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertisingCallback)
        bluetoothLeAdvertiser = null
        Log.d("BleAdvertiser", "BLE 광고 중지")
    }

    // Advertise가 시작될 때 발생하는 콜백 함수.
    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val reason = when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> "이미 광고를 하고 있는 상태"
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "데이터가 너무 큼"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "광고가 너무 많음"
                else -> "알 수 없는 오류"
            }
            Log.e("BleAdvertiser", "BLE 광고 시작 실패 : $reason")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("BleAdvertiser", "BLE 광고 시작 성공")
        }
    }
}