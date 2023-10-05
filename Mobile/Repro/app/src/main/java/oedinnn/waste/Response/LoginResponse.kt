package oedinnn.waste.Response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("username")
    @Expose
    var username: String?=null,

    @SerializedName("password")
    @Expose
    var password: String?=null,

    @SerializedName("id_user")
    @Expose
    var id_user: Int?= -1,

    @SerializedName("user_uid")
    @Expose
    var user_uid: Int?= -1,

    @SerializedName("nama")
    @Expose
    var nama : String?=null,

    @SerializedName("otoritas_ambil")
    @Expose
    var otoritas_ambil : Boolean?=false,

    @SerializedName("otoritas_setor")
    @Expose
    var otoritas_setor : Boolean?=false
)