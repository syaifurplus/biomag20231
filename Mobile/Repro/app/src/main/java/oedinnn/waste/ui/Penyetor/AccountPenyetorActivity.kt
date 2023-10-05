package oedinnn.waste.ui.Penyetor

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import oedinnn.waste.Api.ApiConfig
import oedinnn.waste.Api.setorResultRequest
import oedinnn.waste.Response.payloadResultSetor
import oedinnn.waste.Response.setorResultResponse
import oedinnn.waste.adapter.penyetorAdapter
import oedinnn.waste.databinding.ActivityAccountPenyetorBinding
import oedinnn.waste.ui.login.LoginPageActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountPenyetorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountPenyetorBinding
    private lateinit var account : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountPenyetorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        account = getSharedPreferences("login_session", MODE_PRIVATE)

        binding.username.text = account.getString("nama",null)
        binding.idUser.text = account.getString("username",null)

        binding.btnLogout.setOnClickListener {
            account.edit().clear().commit()

            val intent = Intent(this, LoginPageActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            this?.finish()
        }

        allData()
    }

    private fun allData() {
        val progressDialog = ProgressDialog(this@AccountPenyetorActivity)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it
        progressDialog.show()
        val request = setorResultRequest()

        request.id_user = account.getInt("id_user",-1).toString()
        request.pasword = account.getString("password",null)
        request.partner_id = account.getInt("user_uid",-1).toString()
        request.id_setor = "%"
        request.limit = "0"
        request.offset = "0"

        val Api = ApiConfig.getApiService()

        Api.setorResult(request).enqueue(object : Callback<setorResultResponse> {

            override fun onResponse(
                call: Call<setorResultResponse>,
                response: Response<setorResultResponse>
            ) {
                if (response.isSuccessful){
                    progressDialog.dismiss()
                    val data = response.body()?.data

                    val adapter = data?.let { penyetorAdapter(it) }
                    if (adapter != null){
                        val itemCount = adapter.itemCount
                        var readyCount = 0
                        var stateCount = 0
                        var cancelCount = 0
                        var prosesCount = 0
                        var berat = 0 // Inisialisasi berat ke 0 di sini

                        for (i in 0 until itemCount) {
                            val item = adapter.getItem(i) as? payloadResultSetor

                            if (item != null && item.state == "sukses") {
                                stateCount++
                                berat += item.sampah_berat.toInt() // Tambahkan berat item yang memiliki status "sukses" ke berat total
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

                        binding.jmlsukses.text = "$stateCount"
                        binding.jmlready.text = "$readyCount"
                        binding.jmlproses.text = "$prosesCount"
                        binding.jmlCancel.text = "$cancelCount"
                        binding.jmlBerat.text = "$berat"
                    }
                }
            }

            override fun onFailure(call: Call<setorResultResponse>, t: Throwable) {
                Toast.makeText(this@AccountPenyetorActivity,"gagal retrofit", Toast.LENGTH_LONG).show()
            }

        })
    }
}


