package net.flow9.thisiskotiln.ble_sample.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

//
class PermissionChecker {

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

    // 모든 권한이 허용됐는지 확인
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