package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class userConfirmRequest {
    @SerializedName("id_user")
    @Expose
    var id_user: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null

    @SerializedName("id_ambil")
    @Expose
    var id_ambil: String?=null

    @SerializedName("state")
    @Expose
    var state: String?=null
}