package com.example.libraryapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.libraryapp.data.model.BorrowRecord
import com.example.libraryapp.ui.viewmodel.BookViewModel

/**
 * Giriş yapmış öğrencinin aktif ve geçmiş kiralamalarını listeler.
 *
 * NavGraph'tan çağırım:
 *   MyBorrowsScreen(
 *       studentId    = currentUserId,
 *       bookViewModel = bookViewModel,
 *       onBack       = { navController.popBackStack() }
 *   )
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBorrowsScreen(
    studentId: String,
    bookViewModel: BookViewModel,
    onBack: () -> Unit
) {
    val borrows        by bookViewModel.borrows.collectAsState()
    val borrowsLoading by bookViewModel.borrowsLoading.collectAsState()
    val error          by bookViewModel.error.collectAsState()

    // Sayfa açılınca yükle
    LaunchedEffect(studentId) { bookViewModel.loadBorrows(studentId) }

    // İade onay dialogu için seçili kayıt
    var recordToReturn by remember { mutableStateOf<BorrowRecord?>(null) }

    val active = borrows.filter { !it.isReturned }
    val past   = borrows.filter { it.isReturned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kiralamalarım") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->

        // Hata snackbar
        error?.let { msg ->
            LaunchedEffect(msg) {
                bookViewModel.clearError()
            }
        }

        if (borrowsLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (borrows.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📚", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Henüz kiralama yapmadınız.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Aktif kiralamalar ────────────────────────────────────────────
            if (active.isNotEmpty()) {
                item {
                    SectionHeader(title = "Aktif Kiralamalar", count = active.size)
                }
                items(active, key = { it.id }) { record ->
                    BorrowRecordCard(
                        record         = record,
                        onReturnClick  = { recordToReturn = record }
                    )
                }
            }

            // ── Geçmiş kiralamalar ───────────────────────────────────────────
            if (past.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader(title = "Geçmiş Kiralamalar", count = past.size)
                }
                items(past, key = { it.id }) { record ->
                    BorrowRecordCard(record = record, onReturnClick = null)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // ── İade onay dialogu ────────────────────────────────────────────────────
    recordToReturn?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToReturn = null },
            title   = { Text("Kitabı İade Et") },
            text    = { Text("\"${record.bookId}\" kitabını iade etmek istiyor musunuz?") },
            confirmButton = {
                Button(onClick = {
                    bookViewModel.returnBook(record, studentId)
                    recordToReturn = null
                }) { Text("İade Et") }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordToReturn = null }) { Text("Vazgeç") }
            }
        )
    }
}

// ── Bölüm başlığı ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(vertical = 6.dp)
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Badge { Text("$count") }
    }
}

// ── Kiralama kartı ─────────────────────────────────────────────────────────────
@Composable
private fun BorrowRecordCard(
    record: BorrowRecord,
    onReturnClick: (() -> Unit)?
) {
    val isActive = !record.isReturned

    val borderColor = when {
        !isActive       -> Color(0xFF4CAF50)
        record.isOverdue -> Color(0xFFB00020)
        else             -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Durum rozeti + Kitap id (bookTitle yoksa bookId gösterilir,
            // BorrowRecord'a bookTitle eklemek istersen modeli güncelle)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = record.bookId,   // ← bookTitle eklenirse buraya yaz
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }

                val (statusText, statusColor) = when {
                    !isActive        -> "İADE EDİLDİ" to Color(0xFF4CAF50)
                    record.isOverdue -> "GECİKMİŞ"    to Color(0xFFB00020)
                    else             -> "AKTİF"        to MaterialTheme.colorScheme.primary
                }
                Text(
                    text       = statusText,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = statusColor,
                    modifier   = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Tarih satırı
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateLabel(label = "Alındı",    date = record.borrowedAt)
                DateLabel(label = "Son Tarih", date = record.dueDate)
                if (!isActive && record.returnedAt != null)
                    DateLabel(label = "İade",  date = record.returnedAt)
            }

            // İade butonu — sadece aktif kiralamalar
            if (isActive && onReturnClick != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick          = onReturnClick,
                    modifier         = Modifier.fillMaxWidth(),
                    shape            = RoundedCornerShape(8.dp),
                    contentPadding   = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("İade Et", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DateLabel(label: String, date: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(date,  fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}