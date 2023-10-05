package oedinnn.waste.ui.Pengambil

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import oedinnn.waste.Api.ApiConfig
import oedinnn.waste.Api.userConfirmRequest
import oedinnn.waste.Api.userScanRequest
import oedinnn.waste.R
import oedinnn.waste.Response.ScanResponse
import oedinnn.waste.databinding.FragmentScannerResultDialogListDialogBinding
import oedinnn.waste.ui.MainActivity
import oedinnn.waste.ui.Penyetor.ResultPenyetorActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val RadioButton.checkedRadioButtonId: Any
    get() {
        TODO("Not yet implemented")
    }
const val ARG_SCANNING_RESULT = "scanning_result"

class ScannerResultDialog(private val listener: DialogDismissListener) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentScannerResultDialogListDialogBinding
    private lateinit var account : SharedPreferences
    private lateinit var ambil : SharedPreferences

    private lateinit var confirmDialog: AlertDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerResultDialogListDialogBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        account = activity?.getSharedPreferences("login_session", AppCompatActivity.MODE_PRIVATE)!!
        val scannedResult = arguments?.getString(ARG_SCANNING_RESULT)
        binding.edtResult.setText(scannedResult!!).toString()

        setQrCodeApi()

        binding.btnCopy.setOnClickListener {
            val view = View.inflate(requireContext(), R.layout.dialog_confirm_ambil, null)
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            confirmDialog = builder.create()
            confirmDialog.show()
            confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val txtAbl = confirmDialog.findViewById<TextView>(R.id.txtIdAmbil)
            val btnAbl = confirmDialog.findViewById<CardView>(R.id.buttonConfirm)


            txtAbl.text = ambil.getInt("id_ambil",-1).toString()
            btnAbl.setOnClickListener {
                confirmAmbil() // Pass the dialog reference to the function
            }
        }
    }

    private fun confirmAmbil() {
        val rdoAbl = confirmDialog.findViewById<RadioGroup>(R.id.sortByRadioGroup)
        val password = account.getString("password",null)
        val request = userConfirmRequest()

        var selectedOption = ""
        val buttonId = rdoAbl?.checkedRadioButtonId
        if (buttonId != null) {
            val selectedButton =  confirmDialog.findViewById<RadioButton>(buttonId)
            selectedOption = selectedButton?.text.toString()
        }

        request.id_user = account.getInt("id_user",-1).toString()
        request.pasword = password
        request.id_ambil = ambil.getInt("id_ambil",-1).toString()
        request.state = selectedOption

        val Api = ApiConfig.getApiService()
        Api.userScanConfirm(request).enqueue(object: Callback<Boolean>{
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful){
                    Toast.makeText(activity,"Scan di konfirmasi",Toast.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), ResultAmbil::class.java)
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

    private fun setQrCodeApi() {
        val password = account.getString("password",null)

        val request = userScanRequest()
        request.id_user = account.getInt("id_user",-1).toString()
        request.partner_pengambil = account.getInt("user_uid",-1).toString()
        request.pasword = password
        request.id_setor = binding.edtResult.text.toString()
        val Api = ApiConfig.getApiService()
        Api.userScan(request).enqueue(object : Callback<ScanResponse>{
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                ambil = activity?.getSharedPreferences("ambil_session", AppCompatActivity.MODE_PRIVATE)!!
                if (account.getString("id_ambil", null)!=null){
                    if (account.getString("password",null)!= null)
                        startActivity(Intent(activity, ResultAmbil::class.java))
                }
                if (response.isSuccessful){
                    activity?.getSharedPreferences("ambil_session", AppCompatActivity.MODE_PRIVATE)
                        ?.edit()
                        ?.putInt("id_ambil", response.body()?.id_ambil!!)
                        ?.apply()
                }

            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                Toast.makeText(activity,"Gagal retrofit", Toast.LENGTH_SHORT).show()
            }

        })



    }


    companion object {

        fun newInstance(scanningResult: String, listener: DialogDismissListener): ScannerResultDialog =
            ScannerResultDialog(listener).apply {
                arguments = Bundle().apply {
                    putString(ARG_SCANNING_RESULT, scanningResult)
                }
            }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onDismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.onDismiss()
    }

    interface DialogDismissListener {
        fun onDismiss()
    }
}