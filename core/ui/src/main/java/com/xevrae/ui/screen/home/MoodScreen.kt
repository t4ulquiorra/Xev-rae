package com.xevrae.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.xevrae.ui.component.EndOfPage
import com.xevrae.ui.component.MoodAndGenresContentItem
import com.xevrae.ui.component.NormalAppBar
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.MoodViewModel
import xevrae.composeapp.generated.resources.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import xevrae.composeapp.generated.resources.*

@Composable
fun MoodScreen(
    navController: NavController,
    viewModel: MoodViewModel = hiltViewModel(),
    params: String?,
) {
    val moodData by viewModel.moodsMomentObject.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = params) {
        if (params != null) {
            viewModel.getMood(params)
        }
    }

    Column {
        NormalAppBar(
            title = {
                Text(
                    text = moodData?.header ?: "",
                    style = typo().labelMedium,
                )
            },
            leftIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painterResource(Res.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Back",
                    )
                }
            },
        )
        AnimatedVisibility(visible = !loading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(moodData?.items ?: emptyList()) { item ->
                    MoodAndGenresContentItem(
                        data = item,
                        navController = navController,
                    )
                }
                item {
                    EndOfPage()
                }
            }
        }
        AnimatedVisibility(visible = loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}