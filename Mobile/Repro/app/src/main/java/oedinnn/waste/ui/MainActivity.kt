package oedinnn.waste.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import oedinnn.waste.ui.Pengambil.ResultAmbil
import oedinnn.waste.databinding.ActivityMainBinding
import oedinnn.waste.ui.Pengambil.BarcodeScanningActivity


class MainActivity : AppCompatActivity() {

    private val cameraPermissionRequestCode = 1
    private var selectedScanningSDK = BarcodeScanningActivity.ScannerSDK.MLKIT
    private lateinit var binding: ActivityMainBinding
    private lateinit var account : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.tvTitle.setOnClickListener {
            Intent(this, AccountActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.textView5.setOnClickListener {
            Intent(this, ResultAmbil::class.java).also {
                startActivity(it)
            }
        }

        binding.cardMlKit.setOnClickListener {

        }
    }

}