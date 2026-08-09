package com.reelshelf.app.catchup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.clipdetail.ClipDetailUiState
import com.reelshelf.app.clipdetail.ShareWithSender
import com.reelshelf.app.data.ClipInboxRow
import com.reelshelf.app.data.ClipRepository
import com.reelshelf.app.data.InboxFilter
import com.reelshelf.app.data.ReplyStatus
import com.reelshelf.app.data.SenderRepository
import com.reelshelf.app.data.WatchStatus
import com.reelshelf.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CatchUpUiState(
    val queue: List<ClipInboxRow> = emptyList(),
    val index: Int = 0,
    val detail: ClipDetailUiState = ClipDetailUiState(),
    val finished: Boolean = false,
) {
    val current: ClipInboxRow? get() = queue.getOrNull(index)

    val progressLabel: String
        get() =
            when {
                finished && queue.isEmpty() -> "Nothing to catch up"
                finished -> "Caught up"
                else -> "${index + 1} of ${queue.size}"
            }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CatchUpViewModel(
    private val clipRepository: ClipRepository,
    private val senderRepository: SenderRepository,
    private val mode: InboxFilter,
) : ViewModel() {
    private val index = MutableStateFlow(0)

    private val queueFlow =
        clipRepository.observeInbox(mode, query = "")

    val uiState: StateFlow<CatchUpUiState> =
        combine(queueFlow, index) { queue, idx ->
            val finished = queue.isEmpty() || idx >= queue.size
            val safeIndex = if (finished) idx else idx.coerceIn(0, queue.lastIndex)
            Triple(queue, safeIndex, finished)
        }.flatMapLatest { (queue, idx, finished) ->
            val clipId = queue.getOrNull(idx)?.id
            if (finished || clipId == null) {
                flowOf(
                    CatchUpUiState(
                        queue = queue,
                        index = idx,
                        finished = true,
                    ),
                )
            } else {
                combine(
                    combine(
                        clipRepository.observeClip(clipId),
                        clipRepository.observeShares(clipId),
                        senderRepository.observeRecent(),
                    ) { clip, shares, senders -> Triple(clip, shares, senders) },
                    clipRepository.observeCategories(),
                    clipRepository.observeCategoriesForClip(clipId),
                ) { core, allCategories, assigned ->
                    val (clip, shares, senders) = core
                    val byId = senders.associateBy { it.id }
                    CatchUpUiState(
                        queue = queue,
                        index = idx,
                        detail =
                            ClipDetailUiState(
                                clip = clip,
                                shares = shares.map { ShareWithSender(it, byId[it.senderId]) },
                                allCategories = allCategories,
                                assignedCategoryIds = assigned.map { it.id }.toSet(),
                            ),
                        finished = false,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatchUpUiState())

    fun setWatched(watched: Boolean) {
        val clipId = uiState.value.current?.id ?: return
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

    fun next() {
        index.value = index.value + 1
    }

    companion object {
        fun factory(container: AppContainer, mode: InboxFilter): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatchUpViewModel(container.clipRepository, container.senderRepository, mode) as T
            }
    }
}
