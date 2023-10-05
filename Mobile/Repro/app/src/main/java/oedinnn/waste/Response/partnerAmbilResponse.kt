package oedinnn.waste.Response

data class partnerAmbilResponse(
    val result: Boolean,
    val data: List<partnerAmbil>
)
data class partnerAmbil(
    val id: Int,
    val partner_pengambil: List<Any>,
    val mobile: Any
)