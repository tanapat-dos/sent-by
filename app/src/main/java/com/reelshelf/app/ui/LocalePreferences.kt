package com.reelshelf.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val LocalUiStrings = compositionLocalOf { UiStrings.forLanguage(AppLanguage.EN) }
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }

class LocalePreferences(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("sentby_locale", Context.MODE_PRIVATE)
    private val _language = MutableStateFlow(read())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        Copy.language = _language.value
    }

    fun setLanguage(value: AppLanguage) {
        prefs.edit().putString(KEY, value.name).apply()
        _language.value = value
        Copy.language = value
    }

    private fun read(): AppLanguage {
        val raw = prefs.getString(KEY, null)
        return when (raw) {
            AppLanguage.TH.name -> AppLanguage.TH
            AppLanguage.EN.name -> AppLanguage.EN
            else -> {
                val sys = appContext.resources.configuration.locales[0]?.language
                if (sys == "th") AppLanguage.TH else AppLanguage.EN
            }
        }
    }

    companion object {
        private const val KEY = "lang"
    }
}

@Composable
fun rememberUiStrings(localePreferences: LocalePreferences): UiStrings {
    val lang by localePreferences.language.collectAsState()
    return UiStrings.forLanguage(lang)
}
