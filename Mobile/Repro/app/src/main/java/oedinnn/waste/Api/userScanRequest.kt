package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class userScanRequest {
    @SerializedName("id_user")
    @Expose
    var id_user: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null

    @SerializedName("id_setor")
    @Expose
    var id_setor: String?=null

    @SerializedName("partner_pengambil")
    @Expose
    var partner_pengambil: String?=null
}