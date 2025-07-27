package net.flow9.thisiskotiln.ble_sample.data.ble.gatt

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.gson.Gson
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotlin.ble_sample.data.ble.model.BleConstants

/**
 * 유저의 기기가 데이터를 주는 쪽일 경우.
 * GattServerManager의 로직대로 행동한다.
 */
class GattServerManager (
    private val context: Context,
    private val userCard: UserCard
) {
    private var gattServer: BluetoothGattServer? = null
    private val gson = Gson();

    /**
     * GattServer를 시작한다.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startGattServer() {
        // manager : BluetoothManager, Gatt Server 인스턴스 호출
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = manager.openGattServer(context, gattServerCallback) // Server를 열고 내부 동작은 gattServerCallback에서 정의
        Log.d("GattServer", "GattServer 시작")

        // GattServer에 서비스 추가
        val service = BluetoothGattService(
            BleConstants.SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        // 데이터 Characteristic 정의
        // UUID : Characteristic UUID, 클라이언트가 읽기만 가능하도록 설정
        val characteristic = BluetoothGattCharacteristic(
            BleConstants.CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        Log.d("GattServer", "GattServer 서비스 추가 ${service.uuid}")
        Log.d("GattServer", "Gatt Characteristic 추가 ${characteristic.uuid}")
        service.addCharacteristic(characteristic) // 서비스에 Characteristic 추가
        gattServer?.addService(service)?.also {
            Log.d("GattServer", "서비스 등록 요청 결과: $it")
        }
    }

    /**
     * BLE 통신이 연결, 연결해제 될 때 동작하는 콜백함수.
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        // 연결 상태가 변경될 때마다 로그 찍어줄려고 넣었음.
        override fun onConnectionStateChange(
            device: BluetoothDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)
            Log.d("GattServer", "연결 상태 변경: $device, $status -> $newState")
        }

        // Gatt Client(상대 기기)로부터 Charateristic을 읽으려는 요청이 발생했을 때, 동작하는 함수
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)


            // Characteristic UUID가 일치하는지 확인. 일치하면 데이터를 넘겨준다.
            if (characteristic.uuid == BleConstants.CHARACTERISTIC_UUID) {
                
                // 나의 UserCard를 JSON으로 변환 후, byteArray로 변환해서 value에 담음.
                val json = gson.toJson(userCard)
                val value = json.toByteArray(Charsets.UTF_8)
                
                // Response 보내기
                val response = gattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)
                Log.d("GattServer", "userCard 전송함: $json")
                Log.d("GattServer", "sendResponse 반환값 $response")

                // GATT 서버 간 타이밍 문제가 발생하는 것 같아서 닫는 속도 늦춤. 넉넉하게 5초 준다.
                Handler(Looper.getMainLooper()).postDelayed({
                    stopGattServer()
                }, 5000)
            }
        }
    }

    // Gatt 서버 닫기
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopGattServer() {
        gattServer?.close()
        gattServer = null
        Log.d("GattServerManager", "서버 정상 종료")
    }
}