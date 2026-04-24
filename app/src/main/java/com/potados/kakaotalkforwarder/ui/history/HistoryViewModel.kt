package com.potados.kakaotalkforwarder.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.potados.kakaotalkforwarder.app
import com.potados.kakaotalkforwarder.data.ForwardingRepository
import com.potados.kakaotalkforwarder.data.db.ForwardLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val forwardingRepository: ForwardingRepository,
) : ViewModel() {

    val logs: StateFlow<List<ForwardLog>> = forwardingRepository.logs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun retry(id: Long) {
        viewModelScope.launch { forwardingRepository.retry(id) }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer { HistoryViewModel(context.app.forwardingRepository) }
        }
    }
}
