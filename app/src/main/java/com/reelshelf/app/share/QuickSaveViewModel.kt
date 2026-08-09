package com.reelshelf.app.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.data.ClipIngestor
import com.reelshelf.app.data.IngestOutcome
import com.reelshelf.app.data.IngestRequest
import com.reelshelf.app.data.SenderEntity
import com.reelshelf.app.data.SenderRepository
import com.reelshelf.app.data.SourceApp
import com.reelshelf.app.di.AppContainer
import com.reelshelf.app.urls.UrlExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickSaveUiState(
    val text: String = "",
    val urlCount: Int = 0,
    val recentSenders: List<SenderEntity> = emptyList(),
    val selectedSenderId: String? = null,
    val sourceApp: SourceApp = SourceApp.OTHER,
    val newSenderName: String = "",
    val saving: Boolean = false,
    val message: String? = null,
    val done: Boolean = false,
)

class QuickSaveViewModel(
    private val senderRepository: SenderRepository,
    private val clipIngestor: ClipIngestor,
    initialText: String,
    inferredSource: SourceApp,
    private val fingerprint: String?,
) : ViewModel() {
    private val text = MutableStateFlow(initialText)
    private val selectedSenderId = MutableStateFlow<String?>(null)
    private val sourceApp = MutableStateFlow(inferredSource)
    private val newSenderName = MutableStateFlow("")
    private val saving = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val done = MutableStateFlow(false)

    private data class FormSlice(
        val text: String,
        val senders: List<SenderEntity>,
        val selectedSenderId: String?,
        val sourceApp: SourceApp,
        val newSenderName: String,
    )

    private data class StatusSlice(
        val saving: Boolean,
        val message: String?,
        val done: Boolean,
    )

    val uiState: StateFlow<QuickSaveUiState> =
        combine(
            combine(text, senderRepository.observeRecent(), selectedSenderId, sourceApp, newSenderName) {
                    t, senders, selected, source, newName ->
                FormSlice(t, senders, selected, source, newName)
            },
            combine(saving, message, done) { isSaving, msg, isDone ->
                StatusSlice(isSaving, msg, isDone)
            },
        ) { form, status ->
            QuickSaveUiState(
                text = form.text,
                urlCount = UrlExtractor.extract(form.text).size,
                recentSenders = form.senders,
                selectedSenderId = form.selectedSenderId ?: form.senders.firstOrNull()?.id,
                sourceApp = form.sourceApp,
                newSenderName = form.newSenderName,
                saving = status.saving,
                message = status.message,
                done = status.done,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            QuickSaveUiState(text = initialText, sourceApp = inferredSource),
        )

    fun setText(value: String) {
        text.value = value
    }

    fun selectSender(id: String) {
        selectedSenderId.value = id
    }

    fun setSourceApp(value: SourceApp) {
        sourceApp.value = value
    }

    fun setNewSenderName(value: String) {
        newSenderName.value = value
    }

    fun createSenderAndSelect() {
        viewModelScope.launch {
            val created = senderRepository.create(newSenderName.value)
            selectedSenderId.value = created.id
            newSenderName.value = ""
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = uiState.value
            val senderId = state.selectedSenderId
            if (senderId == null) {
                message.value = "Select or create a sender"
                return@launch
            }
            saving.value = true
            when (
                val outcome =
                    clipIngestor.ingest(
                        IngestRequest(
                            text = state.text,
                            senderId = senderId,
                            sourceApp = state.sourceApp,
                            fingerprint = fingerprint,
                        ),
                    )
            ) {
                IngestOutcome.NoUrls -> message.value = "No http(s) URLs found"
                IngestOutcome.DuplicateIntent -> {
                    message.value = "Already handled this share"
                    done.value = true
                }
                is IngestOutcome.Saved -> {
                    val existing = outcome.results.count { it.wasExistingClip }
                    val created = outcome.results.size - existing
                    val senderName =
                        state.recentSenders.find { it.id == senderId }?.displayName
                            ?: state.newSenderName.trim().ifBlank { "sender" }
                    message.value =
                        when {
                            existing > 0 && created == 0 ->
                                com.reelshelf.app.ui.Copy.alreadySavedAddedSender(senderName)
                            existing > 0 ->
                                com.reelshelf.app.ui.Copy.savedNewAndExisting(created, existing)
                            else -> com.reelshelf.app.ui.Copy.savedClips(outcome.results.size)
                        }
                    done.value = true
                }
            }
            saving.value = false
        }
    }

    companion object {
        fun factory(
            container: AppContainer,
            initialText: String,
            inferredSource: SourceApp,
            fingerprint: String?,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    QuickSaveViewModel(
                        senderRepository = container.senderRepository,
                        clipIngestor = container.clipIngestor,
                        initialText = initialText,
                        inferredSource = inferredSource,
                        fingerprint = fingerprint,
                    ) as T
            }
    }
}
