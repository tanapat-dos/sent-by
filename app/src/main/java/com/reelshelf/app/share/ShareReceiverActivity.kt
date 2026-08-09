package com.reelshelf.app.share

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reelshelf.app.data.ClipIngestor
import com.reelshelf.app.data.SourceApp
import com.reelshelf.app.reelShelfContainer
import com.reelshelf.app.ui.theme.ReelShelfTheme

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = application.reelShelfContainer
        val text = intent?.getCharSequenceExtra(android.content.Intent.EXTRA_TEXT)?.toString().orEmpty()
        val diagnostics =
            ShareDiagnosticsFactory.from(
                action = intent?.action,
                type = intent?.type,
                text = text,
                referringPackage = runCatching {
                    referrer?.let { uri ->
                        when (uri.scheme) {
                            "android-app" -> uri.host
                            else -> uri.authority ?: uri.host
                        }
                    }
                }.getOrNull(),
                referrerUri = runCatching { referrer?.toString() }.getOrNull(),
                callingPackage = callingPackage,
                extrasKeys = intent?.extras?.keySet()?.toList().orEmpty(),
            )
        if (com.reelshelf.app.BuildConfig.DEBUG) {
            android.util.Log.i("ReelShelfShare", diagnostics.toLogLine())
        } else {
            android.util.Log.i(
                "ReelShelfShare",
                "share received type=${diagnostics.type} textPresent=${diagnostics.textPresent} " +
                    "urlCountHint=${diagnostics.urlCountHint} callingPackage=${diagnostics.callingPackage}",
            )
        }
        val source = inferSourceApp(diagnostics.callingPackage ?: diagnostics.referringPackage)
        val fingerprint =
            ClipIngestor.fingerprintFor(
                text = text,
                senderId = diagnostics.callingPackage ?: "unknown-package",
                sourceApp = source,
            )
        setContent {
            ReelShelfTheme {
                val vm: QuickSaveViewModel =
                    viewModel(
                        factory =
                            QuickSaveViewModel.factory(
                                container = container,
                                initialText = text,
                                inferredSource = source,
                                fingerprint = fingerprint,
                            ),
                    )
                QuickSaveScreen(
                    viewModel = vm,
                    title = "Save shared clip",
                    allowEditText = text.isBlank(),
                    onFinished = { finish() },
                )
            }
        }
    }

    private fun inferSourceApp(pkg: String?): SourceApp =
        when (pkg) {
            "jp.naver.line.android" -> SourceApp.LINE
            "com.facebook.orca" -> SourceApp.MESSENGER
            else -> SourceApp.OTHER
        }
}
