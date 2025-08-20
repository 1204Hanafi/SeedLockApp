package com.app.seedlockapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Komponen Card yang menampilkan informasi ringkas tentang satu seed yang tersimpan.
 * Menyediakan aksi untuk melihat detail dan menghapus seed.
 *
 * @param number Nomor urut seed yang akan ditampilkan.
 * @param name Alias atau nama dari seed.
 * @param onView Lambda yang dipanggil saat tombol "lihat" ditekan.
 * @param onDelete Lambda yang dipanggil saat tombol "hapus" ditekan.
 */
@Composable
fun SeedCard(
    number: Int,
    name: String,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Teks nomor dengan padding agar selalu dua digit (misal: 01, 02, 10)
                Text(
                    text = number.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            // Aksi tombol
            Row {
                IconButton(onClick = onView) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Lihat Seed $name",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Seed $name",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}