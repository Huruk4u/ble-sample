package net.flow9.thisiskotiln.ble_sample.data.ble.gatt

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.google.gson.Gson
import net.flow9.thisiskotiln.ble_sample.domain.model.UserCard
import net.flow9.thisiskotlin.ble_sample.data.ble.model.BleConstants

/**
 * 상대 기기의 Gatt Server로부터 SERVICE UUID, CHARACTERISTIC UUID가 일치하는 데이터를 받아온다.
 */
class GattClientManager (
    private val context: Context,
    private val onUserCardReceived: ((UserCard) -> Unit)?
) {

    // json데이터 파싱을 위한 gson, GattClient 인스턴스를 생성하기 위한 bluetoothGatt 인스턴스 호출
    private val gson = Gson()
    private var bluetoothGatt: BluetoothGatt? = null

    // bluetoothGatt에 connect 할당
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    // BLE 통신이 이루어짐에 따라 Callback함수를 호출한다.
    private val gattCallback = object : BluetoothGattCallback() {

        // connection state의 변화에 따라 호출되는 함수
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                // 연결이 성공하면 service 탐색
                Log.d("GattClient", "연결 성공")
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // 연결이 끊이면 bluetoothGatt 초기화
                Log.d("GattClient", "연결 끊김")
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered( gatt: BluetoothGatt?, status: Int ) {
            super.onServicesDiscovered(gatt, status)

            Log.d("GattClient", "요구 서비스 uuid${BleConstants.SERVICE_UUID}")
            Log.d("GattClient", "요구 특성 uuid${BleConstants.CHARACTERISTIC_UUID}")

            // BleConstants에 정의된 SERVICE UUID, CHARACTERISTIC UUID가 일치하는지 확인
            val service = gatt?.getService(BleConstants.SERVICE_UUID)
            val characteristic = service?.getCharacteristic(BleConstants.CHARACTERISTIC_UUID)
            Log.d("GattClient", "device name : ${gatt?.device?.name}")
            Log.d("GattClient", "service uuid : ${service?.uuid}")
            Log.d("GattClient", "characteristic uuid : ${characteristic?.uuid}")

            Log.d("GattClient", "서비스 탐색 성공")
            // 만약 일치하는 characteristic을 발견했다면, GATT 서버로부터 데이터를 요청한다.
            if (characteristic != null) {
                gatt.readCharacteristic(characteristic)
            } else {
                // 일치하는 characteristic을 발견하지 못했다면 로그 출력
                Log.e("GattClient", "CHARACTERISTIC UUID 일치하지 않음")
            }
        }

        // GATT 서버로부터 Characteristic을 읽을 때 호출되는 함수
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d("GattClient", "$status, ${BluetoothGatt.GATT_SUCCESS}")
            // 서버의 응답이 GATT_SUCCESS이고, characteristic의 UUID가 일치한다면, 데이터를 json으로 변환
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic?.uuid == BleConstants.CHARACTERISTIC_UUID) {
                val json = characteristic.value?.toString(Charsets.UTF_8)
                Log.d("GattClient", "받은 UserCard: $json")

                try {
                    val userCard = gson.fromJson(json, UserCard::class.java)
                    onUserCardReceived?.invoke(userCard)
                    Log.d("GattClient", "UserCard 파싱 성공 $userCard")
                } catch (e: Exception) {
                    Log.e("GattClient", "UserCard 파싱 실패", e)
                }

                disconnect()
            }
        }
    }

    // Gatt Client 닫기
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}