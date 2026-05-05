package com.example.libraryapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.libraryapp.data.model.Book
import java.text.SimpleDateFormat
import java.util.*

/**
 * Kitap ödünç alma dialogu.
 * Öğrenci 1–5 gün arasında seçim yapar, iade tarihi anlık hesaplanır.
 *
 * Kullanım (HomeScreen içinde):
 *   BorrowDialog(
 *       book      = selectedBook,
 *       onConfirm = { days -> bookViewModel.borrowBook(studentId, book, days) },
 *       onDismiss = { selectedBook = null }
 *   )
 */
@Composable
fun BorrowDialog(
    book: Book,
    onConfirm: (days: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(3) }

    val dueDate = remember(selectedDays) {
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, selectedDays)
        fmt.format(cal.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ödünç Al", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = book.title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Kaç günlüğüne alıyorsunuz?", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))

                // 1–5 gün seçici
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { day ->
                        FilterChip(
                            selected = selectedDays == day,
                            onClick  = { selectedDays = day },
                            label    = { Text("$day gün", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // İade tarihi özeti
                Card(
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier            = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "İade Tarihi",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text       = dueDate,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Maksimum kiralama süresi 5 gündür.",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Vazgeç") }

                    Button(
                        onClick  = { onConfirm(selectedDays) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Onayla") }
                }
            }
        }
    }
}