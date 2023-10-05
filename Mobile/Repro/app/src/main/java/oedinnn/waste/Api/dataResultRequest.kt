package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class dataResultRequest {
    @SerializedName("offset")
    @Expose
    var offset: String?=null

    @SerializedName("limit")
    @Expose
    var limit: String?=null

    @SerializedName("id_user")
    @Expose
    var id_user: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null

    @SerializedName("id_ambil")
    @Expose
    var id_ambil : String?=null

    @SerializedName("partner_id")
    @Expose
    var partner_id : String?=null

}