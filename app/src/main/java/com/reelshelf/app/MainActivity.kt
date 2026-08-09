package com.reelshelf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reelshelf.app.catchup.CatchUpScreen
import com.reelshelf.app.catchup.CatchUpViewModel
import com.reelshelf.app.category.CategoriesScreen
import com.reelshelf.app.category.CategoriesViewModel
import com.reelshelf.app.clipdetail.ClipDetailScreen
import com.reelshelf.app.clipdetail.ClipDetailViewModel
import com.reelshelf.app.data.InboxFilter
import com.reelshelf.app.data.SourceApp
import com.reelshelf.app.inbox.InboxScreen
import com.reelshelf.app.inbox.InboxViewModel
import com.reelshelf.app.sender.SenderDetailScreen
import com.reelshelf.app.sender.SenderDetailViewModel
import com.reelshelf.app.sender.SendersScreen
import com.reelshelf.app.sender.SendersViewModel
import com.reelshelf.app.share.QuickSaveScreen
import com.reelshelf.app.share.QuickSaveViewModel
import com.reelshelf.app.ui.LocalAppLanguage
import com.reelshelf.app.ui.LocalUiStrings
import com.reelshelf.app.ui.PrivacyScreen
import com.reelshelf.app.ui.Routes
import com.reelshelf.app.ui.UiStrings
import com.reelshelf.app.ui.theme.ReelShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = application.reelShelfContainer
        setContent {
            val lang by container.localePreferences.language.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalAppLanguage provides lang,
                LocalUiStrings provides UiStrings.forLanguage(lang),
            ) {
                ReelShelfTheme {
                    ReelShelfNav(container = container)
                }
            }
        }
    }
}

@Composable
fun ReelShelfNav(container: com.reelshelf.app.di.AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.INBOX) {
        composable(Routes.INBOX) { entry ->
            val vm: InboxViewModel = viewModel(factory = InboxViewModel.factory(container))
            val pendingFilter by entry.savedStateHandle.getStateFlow("inboxFilter", "")
                .collectAsStateWithLifecycle()
            LaunchedEffect(pendingFilter) {
                if (pendingFilter.isNotBlank()) {
                    runCatching { InboxFilter.valueOf(pendingFilter) }.getOrNull()?.let(vm::setFilter)
                    entry.savedStateHandle["inboxFilter"] = ""
                }
            }
            InboxScreen(
                viewModel = vm,
                localePreferences = container.localePreferences,
                onOpenClip = { navController.navigate(Routes.clip(it)) },
                onPaste = { navController.navigate(Routes.PASTE) },
                onSenders = { navController.navigate(Routes.SENDERS) },
                onCategories = { navController.navigate(Routes.CATEGORIES) },
                onCatchUp = { filter -> navController.navigate(Routes.catchUp(filter.name)) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
            )
        }
        composable(Routes.PASTE) {
            val vm: QuickSaveViewModel =
                viewModel(
                    factory =
                        QuickSaveViewModel.factory(
                            container = container,
                            initialText = "",
                            inferredSource = SourceApp.OTHER,
                            fingerprint = null,
                        ),
                )
            QuickSaveScreen(
                viewModel = vm,
                title = "Paste link",
                allowEditText = true,
                onFinished = { navController.popBackStack() },
            )
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SENDERS) {
            val vm: SendersViewModel = viewModel(factory = SendersViewModel.factory(container))
            SendersScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenSender = { navController.navigate(Routes.sender(it)) },
                onDone = {
                    navController.getBackStackEntry(Routes.INBOX).savedStateHandle["inboxFilter"] =
                        InboxFilter.COMPLETED.name
                    navController.popBackStack(Routes.INBOX, inclusive = false)
                },
            )
        }
        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.factory(container))
            CategoriesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Routes.CATCH_UP,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
        ) { entry ->
            val modeName = entry.arguments?.getString("mode") ?: InboxFilter.UNWATCHED.name
            val mode =
                runCatching { InboxFilter.valueOf(modeName) }.getOrDefault(InboxFilter.UNWATCHED)
            val vm: CatchUpViewModel =
                viewModel(factory = CatchUpViewModel.factory(container, mode))
            CatchUpScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Routes.SENDER,
            arguments = listOf(navArgument("senderId") { type = NavType.StringType }),
        ) { entry ->
            val senderId = entry.arguments?.getString("senderId") ?: return@composable
            val vm: SenderDetailViewModel =
                viewModel(factory = SenderDetailViewModel.factory(container, senderId))
            SenderDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenClip = { navController.navigate(Routes.clip(it)) },
            )
        }
        composable(
            Routes.CLIP,
            arguments = listOf(navArgument("clipId") { type = NavType.StringType }),
        ) { entry ->
            val clipId = entry.arguments?.getString("clipId") ?: return@composable
            val vm: ClipDetailViewModel =
                viewModel(factory = ClipDetailViewModel.factory(container, clipId))
            ClipDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
