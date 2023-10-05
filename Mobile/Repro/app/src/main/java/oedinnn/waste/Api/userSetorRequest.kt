package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class userSetorRequest {
    @SerializedName("id_user")
    @Expose
    var id_user: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null

    @SerializedName("partner_pengambil")
    @Expose
    var partner_pengambil: String?=null

    @SerializedName("mtd_pengambilan")
    @Expose
    var mtd_pengambilan: String?=null

    @SerializedName("partner_sampah")
    @Expose
    var partner_sampah: String?=null

    @SerializedName("jenis_sampah")
    @Expose
    var jenis_sampah: String?=null

    @SerializedName("sampah_harga")
    @Expose
    var sampah_harga: String?=null

    @SerializedName("sampah_nama")
    @Expose
    var sampah_nama: String?=null

    @SerializedName("sampah_berat")
    @Expose
    var sampah_berat: String?=null

    @SerializedName("sampah_satuan")
    @Expose
    var sampah_satuan: String?=null

    @SerializedName("tanggal")
    @Expose
    var tanggal: String?=null

    @SerializedName("partner_id")
    @Expose
    var partner_id: String?=null
}