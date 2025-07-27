package net.flow9.thisiskotiln.ble_sample.data.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import net.flow9.thisiskotlin.ble_sample.data.ble.model.BleConstants

/**
 * BLE광고를 발견하여 기기를 찾아내고, GATT Client로 서버에 연결을 시도한다.
 */
class BleScanner (
    private val bluetoothAdapter: BluetoothAdapter,
    private val onDeviceFound: ((BluetoothDevice) -> Unit)?
) {
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    // 스캐너의 중복 호출을 방지하기 위함.
    private var scanning = false

    // BLE 스캔을 시작한다.
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {

        Log.d("BleScanner", "스캔 호출")

        if (scanning) {
            Log.d("BleScanner", "이미 스캔 중입니다.")
            return
        }
        // bluetooth활성화 여부 검사
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BleScanner", "Bluetooth가 활성화 되어 있지 않습니다.")
            return
        }

        // bluetoothLeScanner를 호출한다.
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Log.e("BleScanner", "BLE Scanner 지원하지 않음")
            return
        }

        // BLE Advertise를 필터링한다.
        // SERVICE UUID가 일치하는 advertise만 필터링 할거임.
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        Log.d("BleScanner", "요구하는 UUID ${BleConstants.SERVICE_UUID}")

        // BLE 스캔 거리는 짧게 설정
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .build()

        Log.d("BleScanner", "$bluetoothLeScanner")
        bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
        scanning = true
        Log.d("BleScanner", "BLE 스캔 시작")
    }

    // 스캔 종료
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        // 이쪽에서 아예 ble세션을 클리어 해야할 듯.
        scanning = false
        bluetoothLeScanner?.stopScan(scanCallback)
        bluetoothLeScanner = null
        Log.d("BleScanner", "BLE 스캔 중지")

    }

    // 스캔 시작 시 발생하는 콜백 함수
    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val reason = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "이미 스캔을 하고 있는 상태"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "앱 등록 실패"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "지원하지 않는 기능"
                else -> "알 수 없는 오류"
            }
            Log.e("BleScanner", "BLE 스캔 실패: $reason")
        }

        // 스캔 성공 시 호출되는 함수
        @RequiresPermission(allOf = [
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ])
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("BleScanner", "스캔 결과 $result")

            result?.device?.let { device ->
                Log.d("BleScanner", "기기 발견 ${device.name?:"이름 없음"}, ${device.address}")
                Log.d("BleScanner", "$onDeviceFound")
                onDeviceFound?.invoke(device)
                stopScan()
            }
        }
    }
}