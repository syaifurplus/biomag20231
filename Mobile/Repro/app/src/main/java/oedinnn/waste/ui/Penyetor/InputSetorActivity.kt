package oedinnn.waste.ui.Penyetor

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputLayout
import oedinnn.waste.Api.*
import oedinnn.waste.R
import oedinnn.waste.Response.*
import oedinnn.waste.databinding.ActivityInputSetorBinding
import oedinnn.waste.ui.Pengambil.ResultAmbil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class InputSetorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputSetorBinding
    private lateinit var account: SharedPreferences
    private lateinit var dropdownMenu: Spinner

    private var selectedPartnerSampahId: String? = null
    private var selectedPartnerPengambilId: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputSetorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        account = getSharedPreferences("login_session", MODE_PRIVATE)
        supportActionBar?.hide()

        val dropdownMenu: AutoCompleteTextView = binding.dropdownMenu
        val dropdownLayout: TextInputLayout = binding.dropdownLayout
        val dropdownMenusatuan: AutoCompleteTextView = binding.dropdownMenuSatuan
        val dropdownLayoutSatuan: TextInputLayout = binding.dropdownLayoutsatuan
        val dropdownMenuMetode: AutoCompleteTextView = binding.dropdownMenuMetode
        val dropdownLayoutMetode: TextInputLayout = binding.dropdownLayoutMetode

        val options = arrayOf("organik", "anorganik")
        val optionsSatuan = arrayOf("Kg", "Gram")
        val optionMetode = arrayOf("diambil", "diantar")

        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        binding.companyLayout.editText?.setText(account.getString("nama", null))


        binding.date.text = formattedDate

        binding.imageBack.setOnClickListener {
            val intent = Intent(this@InputSetorActivity, ResultPenyetorActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        //option metode
        val adapterMetode = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, optionMetode)
        dropdownMenuMetode.setAdapter(adapterMetode)

        dropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            dropdownLayoutMetode.hint = selectedItem
        }

        //option jenis Sampah
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        dropdownMenu.setAdapter(adapter)

        dropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            dropdownLayout.hint = selectedItem
        }

        //option Satuan Sampah
        val adapterSatuan = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, optionsSatuan)
        dropdownMenusatuan.setAdapter(adapterSatuan)

        dropdownMenusatuan.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            dropdownLayoutSatuan.hint = selectedItem
        }
        binding.btnSetor.setOnClickListener {
            setorApi()
        }
        dataPartnerApi()
        dataPartnerAmbil()
    }

    private fun setorApi() {
        val request = userSetorRequest()
        val user_uid = account.getInt("user_uid", -1).toString()
        selectedPartnerSampahId = user_uid

        request.id_user = account.getInt("id_user", -1).toString()
        request.pasword = account.getString("password",null)
        request.sampah_nama = binding.edNama.text.toString()
        request.jenis_sampah = binding.dropdownMenu.text.toString()
        request.sampah_satuan = binding.dropdownMenuSatuan.text.toString()
        request.mtd_pengambilan = binding.dropdownMenuMetode.text.toString()
        request.sampah_harga = binding.edHarga.text.toString()
        request.sampah_berat = binding.edberat.text.toString()
        request.tanggal = binding.date.text.toString()
        request.partner_pengambil = selectedPartnerPengambilId.toString()
        request.partner_sampah = selectedPartnerSampahId.toString()
        request.partner_id = user_uid

        Log.d("Coba request", request.partner_pengambil.toString())
        Log.d("Coba request", request.partner_sampah.toString())

        val api = ApiConfig.getApiService()

        api.userSetor(request).enqueue(object : Callback<setorResponse>{
            override fun onResponse(call: Call<setorResponse>, response: Response<setorResponse>) {
                if (response.isSuccessful) {
                    val setorResponse = response.body()

                    val idSetor = setorResponse?.id_setor
                    val view = View.inflate(this@InputSetorActivity, R.layout.dialog_state_setor, null)
                    val builder = AlertDialog.Builder(this@InputSetorActivity)
                    builder.setView(view)
                    val dialog = builder.create()
                    dialog.show()
                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    val btnKonfirm = dialog.findViewById<CardView>(R.id.buttonConfirm2)
                    btnKonfirm.setOnClickListener {
                        setStateSetor(dialog,idSetor)
                    }

                    Toast.makeText(this@InputSetorActivity,"Berhasil Setor", Toast.LENGTH_SHORT).show()
                }
            }

            private fun setStateSetor(dialog: AlertDialog?, idSetor: Int?) {
                val rdoAbl = dialog?.findViewById<RadioGroup>(R.id.RadioGroupState)
                val password = account.getString("password",null)
                val request = userStateSetorRequest()

                var selectedOption = ""


                val buttonId = rdoAbl?.checkedRadioButtonId
                if (buttonId != null) {
                    val selectedButton =  dialog.findViewById<RadioButton>(buttonId)
                    selectedOption = selectedButton?.text.toString()
                }

                request.id_user = account.getInt("id_user",-1).toString()
                request.pasword = password
                request.id_setor = idSetor.toString()
                request.state = selectedOption

                val Api = ApiConfig.getApiService()
                Api.userStateConfirm(request).enqueue(object: Callback<Boolean>{
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful){
                            Toast.makeText(this@InputSetorActivity,"State dikonfirmasi",Toast.LENGTH_LONG).show()
                            val intent = Intent(this@InputSetorActivity, ResultPenyetorActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onFailure(call: Call<setorResponse>, t: Throwable) {
                Toast.makeText(this@InputSetorActivity,"Gagal Retrofit", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dataPartnerAmbil() {
        val request = partnerAmbilRequest()
        val requestSetor = userSetorRequest()
        request.id_user = account.getInt("id_user", -1).toString()
        request.user_uid = account.getInt("user_uid", -1).toString()
        request.pasword = account.getString("password", null)

        val api = ApiConfig.getApiService()

        api.partnerAmbil(request).enqueue(object : Callback<partnerAmbilResponse> {
            override fun onResponse(
                call: Call<partnerAmbilResponse>,
                response: Response<partnerAmbilResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val partnerDataList = responseBody?.data

                    if (!partnerDataList.isNullOrEmpty()) {
                        val partnerNames = partnerDataList.map {
                            it.partner_pengambil[1] as String
                        }

                        // Membuat adapter untuk dropdown menu
                        val adapter = ArrayAdapter(
                            this@InputSetorActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            partnerNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

                        // Mengatur adapter ke spinner
                        binding.dropdownMenuAmbil.setAdapter(adapter)

                        // Menambahkan listener untuk mendapatkan partner_pengambil saat dipilih
                        binding.dropdownMenuAmbil.setOnItemClickListener { parent, view, position, id ->
                            val partnerItem = partnerDataList[position]

                            if (partnerItem.partner_pengambil is List<*> && partnerItem.partner_pengambil.size >= 2) {
                                // Ambil elemen pertama dari list ("PerusahanCoba, pengambil") untuk ditampilkan di dropdown
                                val selectedPartnerId = partnerItem.partner_pengambil[0]

                                if (selectedPartnerId is Double) {
                                    val selectedPartnerIdInt = selectedPartnerId.toInt()

                                    // Update selectedPartnerPengambilId hanya dengan ID (elemen pertama)
                                    selectedPartnerPengambilId = selectedPartnerIdInt.toString()

                                    Toast.makeText(
                                        this@InputSetorActivity,
                                        "Selected Partner: ${partnerItem.partner_pengambil[1]} (ID: $selectedPartnerIdInt)",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    requestSetor.partner_pengambil = selectedPartnerPengambilId as String
                                    // Selanjutnya, Anda dapat menggunakan selectedPartnerPengambilId dalam permintaan API Anda
                                    // misalnya, request.partner_pengambil = selectedPartnerPengambilId
                                }
                            }
                        }



                    }
                }
            }

            override fun onFailure(call: Call<partnerAmbilResponse>, t: Throwable) {
                Toast.makeText(
                    this@InputSetorActivity,
                    "Gagal Retrofit",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun dataPartnerApi() {
        val request = partnerRequest()
        request.id_user = account.getInt("id_user", -1).toString()
        request.pasword = account.getString("password", null)

        val api = ApiConfig.getApiService()

        api.partnerItem(request).enqueue(object : Callback<partnerResponse> {
            override fun onResponse(
                call: Call<partnerResponse>,
                response: Response<partnerResponse>
            ) {
                if (response.isSuccessful) {

                }
            }

            override fun onFailure(call: Call<partnerResponse>, t: Throwable) {
                // Tangani jika gagal mengambil data dari API
            }
        })
    }
}
