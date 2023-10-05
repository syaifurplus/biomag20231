package oedinnn.waste.Response



data class resultResponse(
    var data: List<payloadResult>
)
data class payloadResult(
    val id :String,
    val name :String,
    val sampah_harga:String,
    val mtd_pengambilan:String,
    val state : String,
    val tanggal:String,
    val partner_penyetor: List<Any>,
    val partner_pengambil: List<Any>,
    val jenis_sampah: String,
    val sampah_nama: String,
    val sampah_berat: Double,
    val sampah_satuan: String,
    val partner_sampah : Any,
    val no_penyetoran : Any
)

