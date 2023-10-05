package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class partnerRequest {
    @SerializedName("id_user")
    @Expose
    var id_user: String?=null

    @SerializedName("user_uid")
    @Expose
    var user_uid: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null
}