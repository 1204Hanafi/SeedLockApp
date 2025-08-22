package com.app.seedlockapp.ui.screens.addseed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.seedlockapp.ui.components.ReusableDialog
import com.app.seedlockapp.ui.navigation.Screen

/**
 * Layar untuk menambahkan seed phrase baru.
 * Pengguna dapat memilih antara 12 atau 24 kata, mengisinya,
 * memberikan alias, dan menyimpannya dengan aman.
 *
 * @param navController Controller untuk navigasi.
 * @param viewModel ViewModel yang mengelola state input dan logika penyimpanan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeedScreen(
    navController: NavController,
    viewModel: AddSeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAliasDialog by remember { mutableStateOf(false) }
    var aliasText by remember { mutableStateOf("") }
    var wordCount by remember { mutableIntStateOf(12) }
    val words = remember { mutableStateListOf<String>().apply {
        repeat(24) { add("") }
    } }
    var showErrorDialog by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { List(24) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Memperbarui sesi setiap ada interaksi.
    LaunchedEffect(Unit) {
        viewModel.sessionManager.refreshInteraction()
    }

    // Bereaksi terhadap perubahan state dari ViewModel.
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddSeedUiState.Success -> {
             showAliasDialog = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.AddSeed.route) { inclusive = true }
                }
                viewModel.resetUiState()
            }
            is AddSeedUiState.Error -> {
                apiError = state.message
                showAliasDialog = false
                viewModel.resetUiState()
            }
            else -> {
                // Tidak ada aksi untuk Idle atau Loading di sini
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Seed",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.sessionManager.refreshInteraction()
                            navController.navigateUp()
                        }
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
                    val cleaned = words.take(wordCount).map { it.trim()}.filter { it.isNotEmpty() }
                    if (cleaned.size < wordCount) showErrorDialog = true
                    else {
                        showAliasDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(28.dp)
            ){
                Text(
                    "SIMPAN",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .pointerInput(Unit){
                    detectTapGestures { viewModel.sessionManager.refreshInteraction() }
                }
                .padding(16.dp),
        ) {
            Text(
                "INPUT SEED SESUAI URUTAN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(50))
            ) {
                Row(Modifier.fillMaxSize()) {
                    SegmentedButton(
                        text = "12",
                        selected = wordCount == 12,
                        onClick = { viewModel.sessionManager.refreshInteraction(); wordCount = 12 },
                        shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedButton(
                        text = "24",
                        selected = wordCount == 24,
                        onClick = { viewModel.sessionManager.refreshInteraction(); wordCount = 24 },
                        shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            val rows = (wordCount + 2) / 3
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until 3) {
                            val idx = r * 3 + c
                            if (idx < wordCount) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(Color.Transparent),
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 4.dp, top = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = words[idx],
                                        onValueChange = {
                                            viewModel.sessionManager.refreshInteraction(); words[idx] =
                                            it
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                            .focusRequester(focusRequester[idx]),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            textAlign = TextAlign.Center
                                        ),
                                        placeholder = {Text("")},
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = if (idx == wordCount - 1) ImeAction.Done else ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                if (idx < wordCount - 1) {
                                                    focusRequester[idx + 1].requestFocus()
                                                }
                                            },
                                            onDone = { keyboardController?.hide() }
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent
                                        ),
                                    )
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog untuk validasi input yang tidak lengkap.
    ReusableDialog(
        showDialog = showErrorDialog,
        title = "Data Tidak Lengkap",
        message = "Silahkan isi semua kata sebelum menyimpan.",
        onConfirm = { showErrorDialog = false },
        onDismiss = { showErrorDialog = false }
    )

    // Dialog untuk menampilkan error dari ViewModel
    if (apiError != null) {
        ReusableDialog(
            showDialog = true,
            title = "Gagal Menyimpan",
            message = apiError!!,
            onConfirm = { apiError = null },
            onDismiss = { apiError = null }
        )
    }

    // Dialog untuk memasukkan alias dan memicu penyimpanan.
    if (showAliasDialog) {
        var aliasError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (uiState !is AddSeedUiState.Loading) showAliasDialog = false },
            title = { Text("Masukkan Alias") },
            text = {
                // Kode text field alias tidak berubah
                Column {
                    OutlinedTextField(
                        value = aliasText,
                        onValueChange = {
                            viewModel.sessionManager.refreshInteraction()
                            aliasText = it
                            aliasError = it.isBlank()
                        },
                        label = { Text("Alias") },
                        isError = aliasError,
                        singleLine = true
                    )
                    if (aliasError) {
                        Text("Alias tidak boleh kosong", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                // 3. Tombol konfirmasi sekarang bereaksi terhadap UI State
                TextButton(
                    onClick = {
                        if (aliasText.isNotBlank()) {
                            viewModel.sessionManager.refreshInteraction()
                            val cleaned = words.take(wordCount).map { it.trim() }.filter { it.isNotEmpty() }
                            val phrase = cleaned.joinToString(" ")

                            viewModel.onPhraseChanged(phrase)
                            viewModel.onAliasChanged(aliasText.trim())
                            // Panggil saveSeed tanpa context
                            viewModel.saveSeed()
                        } else {
                            aliasError = true
                        }
                    },
                    // Tombol dinonaktifkan saat loading
                    enabled = uiState !is AddSeedUiState.Loading
                ) {
                    // Tampilkan loading indicator atau teks biasa
                    if (uiState is AddSeedUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Simpan") // Teks diubah dari "Selanjutnya" menjadi "Simpan"
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAliasDialog = false },
                    // Tombol dinonaktifkan saat loading
                    enabled = uiState !is AddSeedUiState.Loading
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Komponen privat untuk tombol tersegmentasi (12/24 kata).
 */
@Composable
private fun SegmentedButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
        ),
        elevation = null
    ) {
        Text(text)
    }
}
