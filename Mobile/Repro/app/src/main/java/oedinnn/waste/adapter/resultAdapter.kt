package oedinnn.waste.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import oedinnn.waste.Response.payloadResult
import oedinnn.waste.databinding.ItemResultBinding

class resultAdapter (private val list: List<payloadResult>):RecyclerView.Adapter<resultAdapter.resultViewHolder>(){
    inner class resultViewHolder(val binding: ItemResultBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("ResourceAsColor")
        fun bind(payloadResult: payloadResult){
            binding.apply {

                if (payloadResult.partner_sampah is List<*>) {
                    val setor = payloadResult.partner_sampah
                    val NamaSetor = setor?.get(1)
                    binding.tvSetor.text = NamaSetor.toString()
                }
                val beratDesimal: Double = payloadResult.sampah_berat
                val beratInt: Int = beratDesimal.toInt()

                val partnerPengambil = payloadResult.partner_pengambil
                val namaPengambil = partnerPengambil?.get(1)
                binding.tvDate.text = payloadResult.tanggal
                binding.tvTitle.text = payloadResult.name
                binding.textAmount.text = payloadResult.sampah_harga
                binding.tvMetod.text = payloadResult.mtd_pengambilan
                binding.tvBerat.text = beratInt.toString()
                binding.tvMakanan.text = payloadResult.sampah_nama
                binding.tvJenis.text = payloadResult.jenis_sampah
                binding.tvSatuan.text = payloadResult.sampah_satuan
                binding.tvPengambil.text = namaPengambil.toString()
                if (payloadResult.state == "false"){
                    binding.tvState.text = "Cancel"
                    binding.cardViewState.setCardBackgroundColor(Color.RED)
                }
                else if (payloadResult.state == "sukses"){
                    binding.tvState.text = "Sukses"
                    binding.cardViewState.setCardBackgroundColor(Color.GREEN)
                }
                else if (payloadResult.state == "ready"){
                    binding.tvState.text = "Ready"
                    binding.cardViewState.setCardBackgroundColor(Color.BLUE)
                }
                else if (payloadResult.state == "proses"){
                    binding.tvState.text = "Proses"
                    binding.cardViewState.setCardBackgroundColor(Color.GRAY)
                }
                else if (payloadResult.state == "draft"){
                    binding.tvState.text = "Draft"
                    binding.cardViewState.setCardBackgroundColor(Color.MAGENTA)
                }
                binding.layoutcard.setOnClickListener {
                    if (binding.data.visibility == View.GONE){
                        binding.data.visibility = View.VISIBLE
                    }else{
                        binding.data.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): resultViewHolder {
        val view = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return resultViewHolder(view)
    }

    fun getItem(position: Int): payloadResult {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: resultViewHolder, position: Int) {
        holder.bind(list?.get(position)!!)
    }
}
