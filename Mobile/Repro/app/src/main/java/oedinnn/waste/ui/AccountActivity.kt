package oedinnn.waste.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import oedinnn.waste.Api.ApiConfig
import oedinnn.waste.Api.dataResultRequest
import oedinnn.waste.Response.payloadResult
import oedinnn.waste.Response.resultResponse
import oedinnn.waste.adapter.resultAdapter
import oedinnn.waste.databinding.ActivityAccountBinding
import oedinnn.waste.ui.login.LoginPageActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var account : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        account = getSharedPreferences("login_session", MODE_PRIVATE)

        binding.idUser.text = account.getString("username",null)


        binding.username.text = account.getString("nama",null)

        //setJumlah Data
        allData()

        binding.btnLogout.setOnClickListener {
            account.edit().clear().commit()

            val intent = Intent(this, LoginPageActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            this?.finish()
        }

    }

    private fun allData() {
        val progressDialog = ProgressDialog(this@AccountActivity)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it
        progressDialog.show()
        val request = dataResultRequest()

        request.id_user = account.getInt("id_user",-1).toString()
        request.pasword = account.getString("password",null)
        request.partner_id = account.getInt("user_uid",-1).toString()
        request.id_ambil = "%"
        request.limit = "0"
        request.offset = "0"

        val Api = ApiConfig.getApiService()

        Api.dataResult(request).enqueue(object : Callback<resultResponse> {
            override fun onResponse(
                call: Call<resultResponse>,
                response: Response<resultResponse>
            ) {
                if (response.isSuccessful){
                    progressDialog.dismiss()
                    val data = response.body()?.data
                    val adapter = data?.let { resultAdapter(it) }
                    if (adapter != null){
                        val itemCount = adapter.itemCount
                        var readyCount = 0
                        var stateCount = 0
                        var cancelCount = 0
                        var prosesCount = 0
                        var berat = 0

                        for (i in 0 until itemCount) {
                            val item = adapter.getItem(i) as? payloadResult

                            if (item != null && item.state == "sukses") {
                                stateCount++
                                berat += item.sampah_berat.toInt()
                            }

                            if (item != null && item.state == "ready") {
                                readyCount++
                            }
                            if (item != null && item.state == "proses") {
                                prosesCount++
                            }
                            if (item != null && item.state == "cancel") {
                                cancelCount++
                            }

                        }

                        binding.jmlsukses.setText("$stateCount")
                        binding.jmlready.setText("$readyCount")
                        binding.jmlproses.setText("$prosesCount")
                        binding.jmlCancel.setText("$cancelCount")
                        binding.jmlBerat.setText("$berat")
                    }
                }
            }

            override fun onFailure(call: Call<resultResponse>, t: Throwable) {
                progressDialog.show()
                Toast.makeText(this@AccountActivity,"gagal retrofit", Toast.LENGTH_LONG).show()
            }

        })


    }
}
