package com.app.seedlockapp.ui.screens.viewseed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSeedScreen(
    navController: NavController,
    seedId: String,
    viewModel: ViewSeedViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            Timber.d("ViewSeedScreen disposed. Clearing sensitive data.")
            state.seedPhrase?.clear()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sessionManager.refreshInteraction()
    }

    LaunchedEffect(seedId) {
        Timber.d("Initializing ViewSeedScreen for seedId: $seedId")
        viewModel.load(seedId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lihat Seed",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.sessionManager.refreshInteraction()
                            navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.sessionManager.refreshInteraction()
                    navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "TUTUP",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures { viewModel.sessionManager.refreshInteraction() }
                }
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Tampilkan error jika ada
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Tampilkan loading indicator
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Konten utama hanya jika tidak loading dan tidak ada error
                if (state.error == null) {
                    Text(
                        text = "Nama Seed: ${state.alias}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    state.seedPhrase?.getValue()?.let { phrase ->
                        val words = phrase.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

                        if (words.isNotEmpty()) {
                            val columns = 3
                            val rows = (words.size + columns - 1) / columns

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(rows) { r ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (c in 0 until columns) {
                                            val idx = r * columns + c
                                            if (idx < words.size) {
                                                Box(
                                                    Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .background(Color.Transparent),
                                                ) {
                                                    Text(
                                                        "${idx + 1}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(4.dp)
                                                    )

                                                    Text(
                                                        words[idx],
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .padding(horizontal = 4.dp)
                                                    )
                                                }
                                            } else {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Seed phrase is empty")
                        }
                    } ?: Text("Seed phrase not available")
                }
            }
        }
    }
}