package net.flow9.thisiskotiln.ble_sample.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

//
object PermissionChecker {

    public fun checkBlePermissions(context: Context) {
        val scan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        val connect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        val advertise = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        Log.d("BlePermissionCheck", "BLUETOOTH_SCAN: $scan, BLUETOOTH_CONNECT: $connect, BLUETOOTH_ADVERTISE: $advertise, FINE_LOCATION: $fine, COARSE_LOCATION: $coarse")
    }


    // BLE통신을 위한 권한 목록 조회
    fun getRequireBlePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else { // android version 12 이하부터는 이 권한을 허용해야 한다.
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    // 모든 권한이 허용됐는지 확인 당장은 BLE권한 밖에 확인할 거 없어서 사용처 X
    fun hasAllPermissions(context: Context): Boolean {
        val permissions = getRequireBlePermissions()
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 허용하지 않은 권한 목록 조회
    fun getMissingPermissions(context: Context): Array<String> {
        return getRequireBlePermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}