package com.example.libraryapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.libraryapp.data.model.Book
import com.example.libraryapp.ui.components.BookCard
import com.example.libraryapp.ui.components.BorrowDialog
import com.example.libraryapp.ui.viewmodel.AuthViewModel
import com.example.libraryapp.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    bookViewModel: BookViewModel,
    onNavigateToMyBorrows: () -> Unit
) {
    val profile       by authViewModel.profile.collectAsState()
    val books         by bookViewModel.books.collectAsState()
    val isLoading     by bookViewModel.isLoading.collectAsState()
    val borrowLoading by bookViewModel.borrowLoading.collectAsState()
    val borrowSuccess by bookViewModel.borrowSuccess.collectAsState()
    val error         by bookViewModel.error.collectAsState()

    // Profile.userId kullan
    val studentId = profile?.userId ?: ""

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(borrowSuccess) {
        val record = borrowSuccess
        if (record != null) {
            snackbarHostState.showSnackbar("✅ Kitap ödünç alındı! İade: ${record.dueDate}")
            bookViewModel.clearBorrowSuccess()
        }
    }

    LaunchedEffect(error) {
        val msg = error
        if (msg != null) {
            snackbarHostState.showSnackbar("❌ $msg")
            bookViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Kütüphane") },
                actions = {
                    TextButton(onClick = onNavigateToMyBorrows) {
                        Text("Kiralamalarım")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.primary
                )

                books.isEmpty() -> Text("Kitaplar yüklenemedi.")

                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(books, key = { book -> book.id }) { book ->
                        BookCard(
                            book          = book,
                            onEditClick   = { /* TODO */ },
                            onDeleteClick = { bookViewModel.deleteBook(book.id) },
                            onBorrowClick = { clicked -> selectedBook = clicked }
                        )
                    }
                }
            }
        }

        if (borrowLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
    }

    selectedBook?.let { book ->
        BorrowDialog(
            book      = book,
            onConfirm = { days ->
                bookViewModel.borrowBook(studentId, book, days)
                selectedBook = null
            },
            onDismiss = { selectedBook = null }
        )
    }
}