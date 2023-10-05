package oedinnn.waste.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import oedinnn.waste.Response.payloadResultSetor
import oedinnn.waste.databinding.ItemSetorBinding


class penyetorAdapter (private val list: List<payloadResultSetor>):RecyclerView.Adapter<penyetorAdapter.resultViewHolder>(){
    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onClickButton(payloadResultSetor: payloadResultSetor)
    }
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class resultViewHolder(val binding: ItemSetorBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("ResourceAsColor")
        fun bind(payloadResultSetor: payloadResultSetor){
            binding.apply {
                val partnerPengambil = payloadResultSetor.partner_pengambil
                val partnerPenyetor = payloadResultSetor.partner_penyetor
                val namaPengambil = partnerPengambil?.get(1)
                val namaPenyetor = partnerPenyetor?.get(1)
                val beratDesimal: Double = payloadResultSetor.sampah_berat
                val beratInt: Int = beratDesimal.toInt()
                binding.tvDate.text = payloadResultSetor.tanggal
                binding.tvTitle.text = payloadResultSetor.name
                binding.textAmount.text = payloadResultSetor.sampah_harga
                binding.tvMetod.text = payloadResultSetor.mtd_pengambilan
                binding.tvBerat.text = beratInt.toString()
                binding.tvMakanan.text = payloadResultSetor.sampah_nama
                binding.tvJenis.text = payloadResultSetor.jenis_sampah
                binding.tvSatuan.text = payloadResultSetor.sampah_satuan
                binding.tvSetor.text = namaPenyetor.toString()
                binding.tvPengambil.text = namaPengambil.toString()

                binding.imageCode.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = list[position]
                        onItemClickCallback.onClickButton(item)
                    }
                }
                if (payloadResultSetor.state == "false"){
                    binding.imageCode.visibility = View.GONE
                    binding.tvState.text = "Cancel"
                    binding.cardViewState.setCardBackgroundColor(Color.RED)
                }
                else if (payloadResultSetor.state == "sukses"){
                    binding.imageCode.visibility = View.GONE
                    binding.tvState.text = "Sukses"
                    binding.cardViewState.setCardBackgroundColor(Color.GREEN)
                }
                else if (payloadResultSetor.state == "ready"){
                    binding.imageCode.visibility = View.VISIBLE
                    binding.tvState.text = "Ready"
                    binding.cardViewState.setCardBackgroundColor(Color.BLUE)
                }
                else if (payloadResultSetor.state == "proses"){
                    binding.imageCode.visibility = View.VISIBLE
                    binding.tvState.text = "Proses"
                    binding.cardViewState.setCardBackgroundColor(Color.GRAY)
                }
                else if (payloadResultSetor.state == "draft"){
                    binding.imageCode.visibility = View.VISIBLE
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
        val view = ItemSetorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return resultViewHolder(view)
    }

    fun getItem(position: Int): payloadResultSetor {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: resultViewHolder, position: Int) {
        holder.bind(list?.get(position)!!)
    }
}

