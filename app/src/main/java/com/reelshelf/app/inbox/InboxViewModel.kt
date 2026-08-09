package com.reelshelf.app.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.data.CategoryEntity
import com.reelshelf.app.data.ClipInboxRow
import com.reelshelf.app.data.ClipRepository
import com.reelshelf.app.data.InboxFilter
import com.reelshelf.app.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InboxUiState(
    val filter: InboxFilter = InboxFilter.ALL,
    val query: String = "",
    val categoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val clips: List<ClipInboxRow> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(
    private val clipRepository: ClipRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(InboxFilter.ALL)
    private val query = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<InboxUiState> =
        combine(filter, query, categoryId, clipRepository.observeCategories()) { f, q, catId, cats ->
            InboxQuery(f, q, catId, cats)
        }.flatMapLatest { params ->
            clipRepository.observeInbox(params.filter, params.query, params.categoryId).map { clips ->
                InboxUiState(
                    filter = params.filter,
                    query = params.query,
                    categoryId = params.categoryId,
                    categories = params.categories,
                    clips = clips,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxUiState())

    fun setFilter(value: InboxFilter) {
        filter.value = value
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setCategoryId(value: String?) {
        categoryId.value = value
    }

    private data class InboxQuery(
        val filter: InboxFilter,
        val query: String,
        val categoryId: String?,
        val categories: List<CategoryEntity>,
    )

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    InboxViewModel(container.clipRepository) as T
            }
    }
}
