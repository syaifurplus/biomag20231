package oedinnn.waste.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class userRequest {
    @SerializedName("username")
    @Expose
    var username: String?=null

    @SerializedName("pasword")
    @Expose
    var pasword: String?=null
}