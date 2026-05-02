package com.example.libraryapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.libraryapp.data.model.Book

/**
 * BookCard — Tek bir kitabı gösteren Material3 kart composable'ı.
 *
 * ── HomeScreen / BookListScreen'de kullanımı ──────────────────────────────────────────
 *
 *   LazyColumn {
 *       items(books) { book ->
 *           BookCard(
 *               book          = book,
 *               onEditClick   = { bookViewModel.startEdit(book) },
 *               onDeleteClick = { bookViewModel.delete(book.id) }
 *           )
 *       }
 *   }
 *
 * ─────────────────────────────────────────────────────────────────────────────────────
 *
 * @param book          Gösterilecek kitap verisi
 * @param onEditClick   Düzenle ikonuna tıklandığında çağrılır
 * @param onDeleteClick Sil ikonuna tıklandığında çağrılır
 * @param modifier      Dışarıdan ek Modifier geçilebilir
 */
@Composable
fun BookCard(
    book: Book,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = book.avaiableCopies > 0

    // Müsaitlik rengini animasyonlu geçişle göster
    val availabilityColor by animateColorAsState(
        targetValue   = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
        animationSpec = tween(400),
        label         = "availabilityColor"
    )

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Sol: Kitap ikonu + renkli arka plan ───────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Book,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(30.dp)
                )
            }

            // ── Orta: Kitap bilgileri ─────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Kitap adı
                Text(
                    text       = book.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Yazar
                Text(
                    text     = book.author,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Kategori
                if (book.category.isNotBlank()) {
                    Text(
                        text     = book.category,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Müsaitlik rozeti
                Surface(
                    shape    = RoundedCornerShape(50),
                    color    = availabilityColor.copy(alpha = 0.15f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text       = if (isAvailable)
                            "Müsait (${book.avaiableCopies}/${book.totalCopies})"
                        else
                            "Ödünç Verildi",
                        color      = availabilityColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // ── Sağ: Aksiyon butonları ────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick  = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick  = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}