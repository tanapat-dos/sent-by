package com.reelshelf.app.clipdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.data.CategoryEntity
import com.reelshelf.app.data.ClipEntity
import com.reelshelf.app.data.ClipRepository
import com.reelshelf.app.data.ReplyStatus
import com.reelshelf.app.data.SenderEntity
import com.reelshelf.app.data.SenderRepository
import com.reelshelf.app.data.ShareRecordEntity
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShareWithSender(
    val share: ShareRecordEntity,
    val sender: SenderEntity?,
)

data class ClipDetailUiState(
    val clip: ClipEntity? = null,
    val shares: List<ShareWithSender> = emptyList(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val assignedCategoryIds: Set<String> = emptySet(),
)

class ClipDetailViewModel(
    private val clipId: String,
    private val clipRepository: ClipRepository,
    private val senderRepository: SenderRepository,
) : ViewModel() {
    val uiState: StateFlow<ClipDetailUiState> =
        combine(
            combine(
                clipRepository.observeClip(clipId),
                clipRepository.observeShares(clipId),
                senderRepository.observeRecent(),
            ) { clip, shares, senders ->
                Triple(clip, shares, senders)
            },
            clipRepository.observeCategories(),
            clipRepository.observeCategoriesForClip(clipId),
        ) { core, allCategories, assigned ->
            val (clip, shares, senders) = core
            val byId = senders.associateBy { it.id }
            ClipDetailUiState(
                clip = clip,
                shares = shares.map { ShareWithSender(it, byId[it.senderId]) },
                allCategories = allCategories,
                assignedCategoryIds = assigned.map { it.id }.toSet(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClipDetailUiState())

    fun setWatched(watched: Boolean) {
        viewModelScope.launch {
            clipRepository.setWatchStatus(
                clipId,
                if (watched) WatchStatus.WATCHED else WatchStatus.UNWATCHED,
            )
        }
    }

    fun setReplyStatus(shareId: String, status: ReplyStatus, replyText: String? = null) {
        viewModelScope.launch {
            clipRepository.setReplyStatus(shareId, status, replyText)
        }
    }

    fun toggleCategory(categoryId: String, assigned: Boolean) {
        viewModelScope.launch {
            clipRepository.setClipCategory(clipId, categoryId, assigned)
        }
    }

    companion object {
        fun factory(container: AppContainer, clipId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ClipDetailViewModel(clipId, container.clipRepository, container.senderRepository) as T
            }
    }
}
