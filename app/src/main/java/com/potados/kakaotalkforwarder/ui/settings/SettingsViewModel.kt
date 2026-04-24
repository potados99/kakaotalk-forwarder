package com.potados.kakaotalkforwarder.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.potados.kakaotalkforwarder.app
import com.potados.kakaotalkforwarder.data.prefs.Settings
import com.potados.kakaotalkforwarder.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings.Empty)

    suspend fun save(apiUrl: String, bearerToken: String, filterNickname: String) {
        settingsRepository.update(apiUrl.trim(), bearerToken.trim(), filterNickname.trim())
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer { SettingsViewModel(context.app.settingsRepository) }
        }
    }
}
