package com.batmancpu.player.ui.metadata

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.batmancpu.player.R
import com.batmancpu.player.ui.theme.PlayerPalette
import com.batmancpu.player.ui.theme.PlayerSpacing

@Composable
internal fun MetadataCreditsScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Surface(Modifier.fillMaxSize(), color = PlayerPalette.background, contentColor = PlayerPalette.textPrimary) {
        Column(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayerSpacing.screenHorizontal, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Text(stringResource(R.string.metadata_back))
            }
            Spacer(Modifier.height(4.dp))
            Text("Player", style = MaterialTheme.typography.displayMedium, color = PlayerPalette.textPrimary)
            Text(stringResource(R.string.metadata_credits_title), style = MaterialTheme.typography.titleLarge)
            Image(
                painter = painterResource(R.drawable.tmdb_logo),
                contentDescription = stringResource(R.string.metadata_credits_logo_description),
                modifier = Modifier.sizeIn(maxWidth = 150.dp, maxHeight = 108.dp),
            )
            Text(
                stringResource(R.string.metadata_credits_notice),
                color = PlayerPalette.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = { uriHandler.openUri("https://www.themoviedb.org") }) {
                Text(stringResource(R.string.metadata_credits_link))
            }
        }
    }
}
