package it.subito.esito.retrofit.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestErrorResponse(
    @SerialName("error") val message: String
)
