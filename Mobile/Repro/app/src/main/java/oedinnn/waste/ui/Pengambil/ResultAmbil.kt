package oedinnn.waste.ui.Pengambil

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import oedinnn.waste.Api.ApiConfig
import oedinnn.waste.Api.dataResultRequest
import oedinnn.waste.R
import oedinnn.waste.Response.resultResponse
import oedinnn.waste.adapter.resultAdapter
import oedinnn.waste.databinding.ActivityResultAmbilBinding
import oedinnn.waste.ui.AccountActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultAmbil : AppCompatActivity() {
    private val cameraPermissionRequestCode = 1
    private var selectedScanningSDK = BarcodeScanningActivity.ScannerSDK.MLKIT
    private lateinit var binding: ActivityResultAmbilBinding
    private lateinit var account : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultAmbilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.scanner.setOnClickListener {
            selectedScanningSDK = BarcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }
        binding.imageAcc.setOnClickListener {
            Intent(this, AccountActivity::class.java).also {
                startActivity(it)
            }
        }
        account = getSharedPreferences("login_session",MODE_PRIVATE)
        binding.textUsername.text = account.getString("nama",null)

        resultApi()
    }

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraWithScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraWithScanner()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, cameraPermissionRequestCode)
            }
        }
    }

    private fun openCameraWithScanner() {
        BarcodeScanningActivity.start(this, selectedScanningSDK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraPermissionRequestCode) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCameraWithScanner()
            }
        }
    }

    private fun resultApi() {
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayoutAmbil)
        swipeRefreshLayout.setOnRefreshListener {
            // Panggil fungsi untuk merefresh data
            resultApi()
        }

        val progressDialog = ProgressDialog(this@ResultAmbil)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it
        progressDialog.show()

        val request = dataResultRequest()
        request.id_user = account.getInt("id_user", -1).toString()
        request.pasword = account.getString("password", null)
        request.partner_id = account.getInt("user_uid", -1).toString()
        request.id_ambil = "%"
        request.limit = "0"
        request.offset = "0"

        val api = ApiConfig.getApiService()

        api.dataResult(request).enqueue(object : Callback<resultResponse> {
            override fun onResponse(
                call: Call<resultResponse>,
                response: Response<resultResponse>
            ) {
                // Dismiss the loading indicator when the response is received
                progressDialog.dismiss()
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val adapter = data?.let { resultAdapter(it) }
                    binding.rvResult.layoutManager = LinearLayoutManager(this@ResultAmbil)
                    binding.rvResult.adapter = adapter

                    Toast.makeText(this@ResultAmbil, "berhasil retrofit", Toast.LENGTH_LONG).show()
                }
                else {
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    Toast.makeText(this@ResultAmbil, "Kode Status: $errorCode, Pesan: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<resultResponse>, t: Throwable) {
                progressDialog.show()
                swipeRefreshLayout.isRefreshing = false
                t.printStackTrace() // Ini akan mencetak detail kesalahan ke logcat
                Toast.makeText(this@ResultAmbil, "Gagal retrofit: ${t.message}", Toast.LENGTH_LONG).show()
                Log.d("Mantap", t.message.toString())
            }

        })
    }

}