package com.reelshelf.app.sender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.data.ClipEntity
import com.reelshelf.app.data.SenderEntity
import com.reelshelf.app.data.SenderRepository
import com.reelshelf.app.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SendersViewModel(
    private val senderRepository: SenderRepository,
) : ViewModel() {
    val senders: StateFlow<List<SenderEntity>> =
        senderRepository.observeRecent()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rename(senderId: String, name: String) {
        viewModelScope.launch { senderRepository.rename(senderId, name) }
    }

    fun merge(fromId: String, intoId: String) {
        viewModelScope.launch { senderRepository.merge(fromId, intoId) }
    }

    fun setFavorite(senderId: String, favorite: Boolean) {
        viewModelScope.launch { senderRepository.setFavorite(senderId, favorite) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SendersViewModel(container.senderRepository) as T
            }
    }
}

class SenderDetailViewModel(
    senderId: String,
    senderRepository: SenderRepository,
) : ViewModel() {
    val sender: StateFlow<SenderEntity?> =
        senderRepository.observeSender(senderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val clips: StateFlow<List<ClipEntity>> =
        senderRepository.observeClips(senderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary =
        senderRepository.observeHistorySummary(senderId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                com.reelshelf.app.data.SenderHistorySummary(0, null),
            )

    companion object {
        fun factory(container: AppContainer, senderId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SenderDetailViewModel(senderId, container.senderRepository) as T
            }
    }
}
