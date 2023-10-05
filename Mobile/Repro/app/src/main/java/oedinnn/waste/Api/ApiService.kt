package oedinnn.waste.Api

import oedinnn.waste.Response.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("login_api")
    fun userLogin(
        @Body userRequest: userRequest
    ): Call<LoginResponse>

    @POST("ambil_input_api")
    fun userScan(
        @Body userScanRequest: userScanRequest
    ): Call<ScanResponse>

    @POST("ambil_set_state_api")
    fun userScanConfirm(
        @Body userConfirmRequest: userConfirmRequest
    ): Call<Boolean>

    @POST("setor_set_state_api")
    fun userStateConfirm(
        @Body userStateSetorRequest: userStateSetorRequest
    ): Call<Boolean>


    @POST("ambil_api")
    fun dataResult(
        @Body dataResultRequest:dataResultRequest
    ): Call<resultResponse>

    @POST("setor_api")
    fun setorResult(
        @Body setorResultRequest: setorResultRequest
    ): Call<setorResultResponse>

    @POST("partner_sampah_api")
    fun partnerItem(
        @Body partnerRequest: partnerRequest
    ): Call<partnerResponse>

    @POST("partner_pengambil_api")
    fun partnerAmbil(
        @Body partnerAmbilRequest: partnerAmbilRequest
    ): Call<partnerAmbilResponse>

    @POST("setor_input_api")
    fun userSetor(
        @Body userSetorRequest: userSetorRequest
    ): Call<setorResponse>

    @GET("qrcode_setor/{id}")
    fun getQRCode(@Path("id") id: Int): Call<ResponseBody>
}