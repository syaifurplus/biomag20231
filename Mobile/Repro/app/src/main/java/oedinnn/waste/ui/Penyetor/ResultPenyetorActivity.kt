package oedinnn.waste.ui.Penyetor

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import oedinnn.waste.Api.*
import oedinnn.waste.R
import oedinnn.waste.Response.payloadResultSetor
import oedinnn.waste.Response.setorResultResponse
import oedinnn.waste.adapter.penyetorAdapter
import oedinnn.waste.databinding.ActivityResultPenyetorBinding
import oedinnn.waste.ui.Pengambil.ResultAmbil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class ResultPenyetorActivity : AppCompatActivity(), penyetorAdapter.OnItemClickCallback {
    private lateinit var binding: ActivityResultPenyetorBinding
    private lateinit var account: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultPenyetorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        account = getSharedPreferences("login_session", MODE_PRIVATE)
        supportActionBar?.hide()

        binding.textUsername.text = account.getString("nama",null).toString()

        binding.imageAcc.setOnClickListener {
            Intent(this, AccountPenyetorActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.addsetor.setOnClickListener {
            Intent(this, InputSetorActivity::class.java).also {
                startActivity(it)
            }
        }
        resultSetorApi()
    }

    override fun onClickButton(payloadResultSetor: payloadResultSetor) {
        // Handle the click event here
        // You can access the clicked item's data using the 'payloadResultSetor' parameter
        // Implement your desired behavior
        // Example: Show a Toast with the item's name
        Toast.makeText(this, payloadResultSetor.name, Toast.LENGTH_SHORT).show()
    }


    private fun resultSetorApi() {
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Panggil fungsi untuk merefresh data
            resultSetorApi()
        }
        // Create and show a loading indicator (ProgressDialog or ProgressBar)
        val progressDialog = ProgressDialog(this@ResultPenyetorActivity)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it
        progressDialog.show()

        val request = setorResultRequest()
        request.id_user = account.getInt("id_user", -1).toString()
        request.pasword = account.getString("password", null)
        request.partner_id = account.getInt("user_uid", -1).toString()
        request.id_setor = "%"
        request.limit = "0"
        request.offset = "0"

        val api = ApiConfig.getApiService()

        api.setorResult(request).enqueue(object : Callback<setorResultResponse> {
            override fun onResponse(
                call: Call<setorResultResponse>,
                response: Response<setorResultResponse>
            ) {
                // Dismiss the loading indicator when the response is received
                progressDialog.dismiss()
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val adapter = data?.let { penyetorAdapter(it) }
                    binding.rvSetor.layoutManager = LinearLayoutManager(this@ResultPenyetorActivity)
                    binding.rvSetor.adapter = adapter

                    getQr(adapter)
                    Toast.makeText(this@ResultPenyetorActivity, "berhasil retrofit", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<setorResultResponse>, t: Throwable) {
                // Dismiss the loading indicator when there is a failure
                progressDialog.show()
                swipeRefreshLayout.isRefreshing = false

                Toast.makeText(this@ResultPenyetorActivity, "gagal mengolah data", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getQr(adapter: penyetorAdapter?) {
        adapter?.setOnItemClickCallback(object : penyetorAdapter.OnItemClickCallback {
            override fun onClickButton(payloadResultSetor: payloadResultSetor) {
                val idSetor = payloadResultSetor.id.toInt()

                val api = ApiConfig.getApiService()
                val call = api.getQRCode(idSetor)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val executor = Executors.newSingleThreadExecutor()

                            executor.execute {
                                try {
                                    val url = java.net.URL("http://154.41.251.66:8082/qrcode_setor/$idSetor") // Ubah URL sesuai dengan kebutuhan Anda
                                    val connection = url.openConnection()
                                    connection.doInput = true
                                    connection.connect()
                                    val input = connection.getInputStream()
                                    val img = BitmapFactory.decodeStream(input)
                                    runOnUiThread {
                                        val view = View.inflate(this@ResultPenyetorActivity, R.layout.dialog_qrcode, null)
                                        val builder = AlertDialog.Builder(this@ResultPenyetorActivity)
                                        builder.setView(view)
                                        val dialog = builder.create()
                                        dialog.show()
                                        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                                        val imgqt = dialog.findViewById<ImageView>(R.id.imageQr1)
                                        val imgCl = dialog.findViewById<ImageView>(R.id.imageClose)
                                        val setTxt = dialog.findViewById<TextView>(R.id.textNt)

                                        imgCl.setOnClickListener {
                                            dialog.dismiss()
                                        }
                                        setTxt.text = payloadResultSetor.name
                                        imgqt.setImageBitmap(img)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@ResultPenyetorActivity, "Gagal ambil QR", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        runOnUiThread {
                            Toast.makeText(this@ResultPenyetorActivity, "Gagal retrofit", Toast.LENGTH_LONG).show()
                        }
                    }
                })


            }
        })
    }

}
