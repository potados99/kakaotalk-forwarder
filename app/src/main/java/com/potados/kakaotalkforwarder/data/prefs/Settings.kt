package com.potados.kakaotalkforwarder.data.prefs

data class Settings(
    val apiUrl: String,
    val bearerToken: String,
    val filterNickname: String,
) {
    val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && bearerToken.isNotBlank()

    companion object {
        const val DEFAULT_FILTER_NICKNAME = "진한식당m"

        val Empty = Settings(
            apiUrl = "",
            bearerToken = "",
            filterNickname = DEFAULT_FILTER_NICKNAME,
        )
    }
}
