package co.daily.dailyvideoapp.android

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<MaterialButton>(R.id.btnStartCall).setOnClickListener {
            permissionsBuilder(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ).build().send { result ->
                if (result.allGranted()) {
                    startActivity(Intent(this, CallActivity::class.java))
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "We need those permissions in order to make a call",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}