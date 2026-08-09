package com.reelshelf.app.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reelshelf.app.data.CategoryEntity
import com.reelshelf.app.data.ClipRepository
import com.reelshelf.app.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val clipRepository: ClipRepository,
) : ViewModel() {
    val categories: StateFlow<List<CategoryEntity>> =
        clipRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String) {
        viewModelScope.launch {
            runCatching { clipRepository.createCategory(name) }
        }
    }

    fun rename(categoryId: String, name: String) {
        viewModelScope.launch {
            runCatching { clipRepository.renameCategory(categoryId, name) }
        }
    }

    fun delete(categoryId: String) {
        viewModelScope.launch {
            clipRepository.deleteCategory(categoryId)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CategoriesViewModel(container.clipRepository) as T
            }
    }
}
