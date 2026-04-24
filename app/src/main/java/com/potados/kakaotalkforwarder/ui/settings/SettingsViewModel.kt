package com.potados.kakaotalkforwarder.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.potados.kakaotalkforwarder.app
import com.potados.kakaotalkforwarder.data.ForwardingRepository
import com.potados.kakaotalkforwarder.data.prefs.Settings
import com.potados.kakaotalkforwarder.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val forwardingRepository: ForwardingRepository,
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings.Empty)

    suspend fun save(apiUrl: String, bearerToken: String, filterNickname: String) {
        settingsRepository.update(apiUrl.trim(), bearerToken.trim(), filterNickname.trim())
    }

    suspend fun clearHistory() = forwardingRepository.clearAll()

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val app = context.app
                SettingsViewModel(app.settingsRepository, app.forwardingRepository)
            }
        }
    }
}
