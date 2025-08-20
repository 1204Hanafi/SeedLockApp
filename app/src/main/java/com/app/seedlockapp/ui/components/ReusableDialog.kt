package com.app.seedlockapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * Sebuah komponen dialog AlertDialog yang dapat digunakan kembali di seluruh aplikasi
 * untuk menampilkan pesan konfirmasi atau informasi.
 *
 * @param showDialog State [Boolean] yang mengontrol visibilitas dialog. `true` untuk menampilkan.
 * @param title Teks yang akan ditampilkan sebagai judul dialog.
 * @param message Teks yang akan ditampilkan sebagai isi/pesan dari dialog.
 * @param confirmButtonText Teks untuk tombol konfirmasi (default: "OK").
 * @param dismissButtonText Teks untuk tombol batal (default: "Batal"). Bisa null jika tidak ingin menampilkan tombol batal.
 * @param onConfirm Lambda yang akan dieksekusi ketika tombol konfirmasi ditekan.
 * @param onDismiss Lambda yang akan dieksekusi ketika dialog ditutup (baik dengan menekan tombol batal atau di luar area dialog).
 */
@Composable
fun ReusableDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "OK",
    dismissButtonText: String? = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                dismissButtonText?.let {
                    TextButton(onClick = onDismiss) {
                        Text(it)
                    }
                }
            }
        )
    }
}