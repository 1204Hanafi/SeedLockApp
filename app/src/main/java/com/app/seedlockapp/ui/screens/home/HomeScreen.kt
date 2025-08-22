package com.app.seedlockapp.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.seedlockapp.R
import com.app.seedlockapp.ui.components.ReusableDialog
import com.app.seedlockapp.ui.components.SeedCard
import com.app.seedlockapp.ui.navigation.Screen

/**
 * Layar utama aplikasi yang menampilkan daftar seed yang telah disimpan.
 * Pengguna dapat melihat, menghapus, atau menavigasi ke layar tambah seed dari sini.
 *
 * @param navController Controller untuk menangani navigasi ke layar lain.
 * @param viewModel ViewModel yang menyediakan state UI dan menangani logika bisnis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialogFor by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Efek samping untuk mereset interaksi sesi setiap kali ada interaksi.
    LaunchedEffect(Unit) {
        viewModel.sessionManager.refreshInteraction()
    }

    // Menampilkan pesan error dari ViewModel jika ada.
    uiState.error?.let { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        // Memberi tahu ViewModel bahwa error telah ditampilkan agar tidak muncul lagi.
        viewModel.errorShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Seeds",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.appbar),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.sessionManager.refreshInteraction()
                    navController.navigate(Screen.AddSeed.route) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Seed",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit){
                    detectTapGestures { viewModel.sessionManager.refreshInteraction() }
                }
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                "LIST SEEDS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            if (uiState.seeds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tidak ada Seed yang Tersimpan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(uiState.seeds) { index, seed ->
                        SeedCard(
                            number = index + 1,
                            name = seed.name,
                            onView = {
                                viewModel.sessionManager.refreshInteraction()
                                navController.navigate("${Screen.ViewSeed.route}/${seed.id}")
                            },
                            onDelete = {
                                viewModel.sessionManager.refreshInteraction()
                                showDeleteDialogFor = seed.id
                            }
                        )
                    }
                }
            }

            // Dialog konfirmasi penghapusan.
            ReusableDialog(
                showDialog = showDeleteDialogFor != null,
                title = "Hapus Seed?",
                message = "Yakin ingin menghapus seed ini?",
                onConfirm = {
                    viewModel.sessionManager.refreshInteraction()
                    showDeleteDialogFor?.let { viewModel.deleteSeed(it) }
                    showDeleteDialogFor = null
                },
                onDismiss = {
                    showDeleteDialogFor = null
                }
            )
        }
    }
}
