package com.app.seedlockapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Reusable AlertDialog component.
 * 
 * @param showDialog Boolean state to control dialog visibility.
 * @param title Title of the dialog.
 * @param message Message content of the dialog.
 * @param onConfirm Callback for confirm button click.
 * @param onDismiss Callback for dismiss button click or outside touch.
 */
@Composable
fun ReusableDialog(
    showDialog: Boolean,
    title: String,
    message: String,
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        )
    }
}

