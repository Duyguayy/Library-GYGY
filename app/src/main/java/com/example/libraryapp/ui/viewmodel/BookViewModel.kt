package com.example.libraryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.model.Book
import com.example.libraryapp.data.model.BorrowRecord
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

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init { loadBooks() }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllBooks()
                .onSuccess { _books.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch {
            repository.deleteBook(id)
                .onSuccess { _books.value = _books.value.filter { it.id != id } }
                .onFailure { _error.value = it.message }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateBook(book)
                .onSuccess {
                    _books.value = _books.value.map { if (it.id == book.id) book else it }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value   = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            repository.searchBooks(query)
                .onSuccess { _searchResults.value = it }
                .onFailure { _error.value = it.message }
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _isSearching.value   = false
    }

    fun clearError() { _error.value = null }
    private val _borrowLoading = MutableStateFlow(false)
    val borrowLoading: StateFlow<Boolean> = _borrowLoading

    private val _borrowSuccess = MutableStateFlow<BorrowRecord?>(null)
    val borrowSuccess: StateFlow<BorrowRecord?> = _borrowSuccess
    private val _borrows = MutableStateFlow<List<BorrowRecord>>(emptyList())
    val borrows: StateFlow<List<BorrowRecord>> = _borrows

    private val _borrowsLoading = MutableStateFlow(false)
    val borrowsLoading: StateFlow<Boolean> = _borrowsLoading

    fun borrowBook(studentId: String, book: Book, days: Int) {
        viewModelScope.launch {
            _borrowLoading.value = true
            repository.borrowBook(studentId, book, days)
                .onSuccess { record ->
                    _borrowSuccess.value = record
                    // Listedeki available_copies'i güncelle (sunucuya gitmeden)
                    _books.value = _books.value.map {
                        if (it.id == book.id) it.copy(avaiableCopies = it.avaiableCopies - 1)
                        else it
                    }
                }
                .onFailure { _error.value = it.message }
            _borrowLoading.value = false
        }
    }

    fun clearBorrowSuccess() { _borrowSuccess.value = null }

    fun loadBorrows(studentId: String) {
        viewModelScope.launch {
            _borrowsLoading.value = true
            repository.getBorrowsByStudent(studentId)
                .onSuccess { _borrows.value = it }
                .onFailure { _error.value = it.message }
            _borrowsLoading.value = false
        }
    }

    fun returnBook(record: BorrowRecord, studentId: String) {
        viewModelScope.launch {
            repository.returnBook(record)
                .onSuccess {
                    // Listeyi yenile
                    loadBorrows(studentId)
                    // Ana kitap listesinde available_copies'i artır
                    _books.value = _books.value.map {
                        if (it.id == record.bookId) it.copy(avaiableCopies = it.avaiableCopies + 1)
                        else it
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }
}