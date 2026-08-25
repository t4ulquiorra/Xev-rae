package com.xevrae.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.xevrae.domain.utils.LocalResource
import com.xevrae.ui.component.ActionButton
import com.xevrae.ui.component.CenterLoadingBox
import com.xevrae.ui.navigation.destination.home.AnalyticsDestination
import com.xevrae.ui.navigation.destination.home.SettingsDestination
import com.xevrae.ui.navigation.destination.login.LoginDestination
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.SettingsViewModel
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.add_an_account
import xevrae.composeapp.generated.resources.baseline_close_24
import xevrae.composeapp.generated.resources.baseline_people_alt_24
import xevrae.composeapp.generated.resources.baseline_playlist_add_24
import xevrae.composeapp.generated.resources.guest
import xevrae.composeapp.generated.resources.no_account
import xevrae.composeapp.generated.resources.signed_in
import xevrae.composeapp.generated.resources.youtube_account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeftPanelContent(
    accountUrl: String,
    accountName: String,
    navController: NavController,
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    var showYouTubeAccountDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(true) {
        settingsViewModel.getAllGoogleAccount()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(accountUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A)),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = accountName,
                    style = typo().titleMedium,
                    color = Color.White,
                )
                Text(
                    text = stringResource(Res.string.signed_in),
                    style = typo().bodySmall,
                    color = Color.Gray,
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2A2A2A))
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    settingsViewModel.getAllGoogleAccount()
                    showYouTubeAccountDialog = true
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(stringResource(Res.string.add_an_account), style = typo().labelMedium, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDismiss()
                    navController.navigate(AnalyticsDestination)
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.ShowChart, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text("Your Stats", style = typo().labelMedium, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDismiss()
                    navController.navigate(SettingsDestination)
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Settings, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text("Settings", style = typo().labelMedium, color = Color.White)
        }
    }

    if (showYouTubeAccountDialog) {
        BasicAlertDialog(
            onDismissRequest = { },
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = Color(0xFF242424),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 1.dp,
            ) {
                val googleAccounts by settingsViewModel.googleAccounts.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            IconButton(
                                onClick = { showYouTubeAccountDialog = false },
                                colors = IconButtonDefaults.iconButtonColors().copy(contentColor = Color.White),
                                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight(),
                            ) {
                                Icon(Icons.Outlined.Close, null, tint = Color.White)
                            }
                            Text(
                                stringResource(Res.string.youtube_account),
                                style = typo().titleMedium,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .wrapContentHeight(Alignment.CenterVertically)
                                    .wrapContentWidth(),
                            )
                        }
                    }
                    if (googleAccounts is LocalResource.Success) {
                        val data = googleAccounts.data
                        if (data.isNullOrEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.no_account),
                                    style = typo().bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                )
                            }
                        } else {
                            items(data) { account ->
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .clickable { settingsViewModel.setUsedAccount(account) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Spacer(Modifier.width(24.dp))
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalPlatformContext.current)
                                            .data(account.thumbnailUrl)
                                            .crossfade(550)
                                            .build(),
                                        placeholder = painterResource(Res.drawable.baseline_people_alt_24),
                                        error = painterResource(Res.drawable.baseline_people_alt_24),
                                        contentDescription = account.name,
                                        modifier = Modifier.size(48.dp).clip(CircleShape),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(account.name, style = typo().labelMedium, color = Color.White)
                                        Text(account.email, style = typo().bodySmall)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AnimatedVisibility(account.isUsed) {
                                        Text(
                                            stringResource(Res.string.signed_in),
                                            style = typo().bodySmall,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(0.dp, 64.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(24.dp))
                                }
                            }
                        }
                    } else {
                        item {
                            CenterLoadingBox(Modifier.fillMaxWidth().height(80.dp))
                        }
                    }
                    item {
                        Column {
                            ActionButton(
                                icon = painterResource(Res.drawable.baseline_people_alt_24),
                                text = Res.string.guest,
                            ) {
                                settingsViewModel.setUsedAccount(null)
                                showYouTubeAccountDialog = false
                            }
                            ActionButton(
                                icon = painterResource(Res.drawable.baseline_playlist_add_24),
                                text = Res.string.add_an_account,
                            ) {
                                showYouTubeAccountDialog = false
                                navController.navigate(LoginDestination)
                            }
                        }
                    }
                }
            }
        }
    }
}
