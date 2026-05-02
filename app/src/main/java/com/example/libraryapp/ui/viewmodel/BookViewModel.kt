package com.example.libraryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.model.Book
import com.example.libraryapp.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
    private val repository = BookRepository()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Arama sonuçları — boşsa tüm liste gösterilir
    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository
                .getAllBooks()
                .onSuccess { _books.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // ── Silme ─────────────────────────────────────────────────────────────────────────
    fun deleteBook(id: String) {
        viewModelScope.launch {
            repository
                .deleteBook(id)
                .onSuccess {
                    // Sunucuya gitmeden listeyi anında güncelle (optimistic update)
                    _books.value = _books.value.filter { it.id != id }
                }
                .onFailure { _error.value = it.message }
        }
    }

    // ── Güncelleme ────────────────────────────────────────────────────────────────────
    fun updateBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            repository
                .updateBook(book)
                .onSuccess {
                    // Listedeki ilgili kitabı güncelle
                    _books.value = _books.value.map {
                        if (it.id == book.id) book else it
                    }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // ── Arama ─────────────────────────────────────────────────────────────────────────
    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value   = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            repository
                .searchBooks(query)
                .onSuccess { _searchResults.value = it }
                .onFailure { _error.value = it.message }
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _isSearching.value   = false
    }

    fun clearError() {
        _error.value = null
    }
}