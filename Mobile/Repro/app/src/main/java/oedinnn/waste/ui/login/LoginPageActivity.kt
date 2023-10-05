package oedinnn.waste.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import oedinnn.waste.Api.ApiConfig
import oedinnn.waste.Api.userRequest
import oedinnn.waste.ui.Pengambil.ResultAmbil
import oedinnn.waste.ui.Penyetor.ResultPenyetorActivity
import oedinnn.waste.Response.LoginResponse
import oedinnn.waste.databinding.ActivityLoginPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var account : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        account = getSharedPreferences("login_session", MODE_PRIVATE)
        val username = account.getString("username", null)
        val password = account.getString("password", null)
        val otoritasAmbil = account.getBoolean("otoritas_ambil", false)
        val otoritasSetor = account.getBoolean("otoritas_setor", false)

        if (username != null && password != null) {
            if (otoritasAmbil) {
                startActivity(Intent(this, ResultAmbil::class.java))
            } else if (otoritasSetor) {
                startActivity(Intent(this, ResultPenyetorActivity::class.java))
            }
            finish()
        }


        binding.crdLogin.setOnClickListener {
            getData()
        }
    }

    private fun getData() {
        val progressDialog = ProgressDialog(this@LoginPageActivity)
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it
        progressDialog.show()
        val request = userRequest()
        request.username = binding.Edemail.text.toString()
        request.pasword = binding.EdPasswrod.text.toString()
        val api = ApiConfig.getApiService()

        api.userLogin(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful){
                    getSharedPreferences("login_session", MODE_PRIVATE)
                        .edit()
                        .putString("username", response.body()?.username)
                        .putString("password",response.body()?.password)
                        .putString("nama",response.body()?.nama)
                        .putBoolean("otoritas_ambil", response.body()?.otoritas_ambil!!)
                        .putBoolean("otoritas_setor", response.body()?.otoritas_setor!!)
                        .putInt("user_uid",response.body()?.user_uid!!)
                        .putInt("id_user", response.body()?.id_user!!)
                        .apply()
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.otoritas_ambil!!) {
                            progressDialog.dismiss()
                            // SharePreference
                            Toast.makeText(this@LoginPageActivity, "Berhasil Login", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginPageActivity, ResultAmbil::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        } else if (responseBody.otoritas_setor!!) {
                            progressDialog.dismiss()
                            // SharePreference
                            Toast.makeText(this@LoginPageActivity, "Berhasil Login", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginPageActivity, ResultPenyetorActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }

                    else{
                        progressDialog.show()
                        Toast.makeText(this@LoginPageActivity,"Gagal Login", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressDialog.show()
                Toast.makeText(this@LoginPageActivity,"gagal Retrofit", Toast.LENGTH_SHORT).show()
            }

        })
    }
}