package it.subito.esito.retrofit.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestOkResponse(
    @SerialName("message") val message: String
)
