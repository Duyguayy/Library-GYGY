package com.example.libraryapp.data.repository

import com.example.libraryapp.data.model.Book
import com.example.libraryapp.data.supabase.supabase
import io.github.jan.supabase.postgrest.postgrest

class BookRepository {

    // ── Mevcut fonksiyonlar (dokunulmadı) ─────────────────────────────────────────────

    suspend fun getAllBooks(): Result<List<Book>> = runCatching {
        supabase.postgrest["books"]
            .select()
            .decodeList<Book>()
    }

    suspend fun getBookById(id: String): Result<Book> = runCatching {
        supabase.postgrest["books"]
            .select { filter { eq("id", id) } }
            .decodeSingle<Book>()
    }

    suspend fun addBook(book: Book): Result<Unit> = runCatching {
        supabase.postgrest["books"].insert(book)
    }

    // ── ÖDEV 2: Güncelleme ────────────────────────────────────────────────────────────
    /**
     * Var olan bir kitabın tüm alanlarını günceller.
     * Supabase'de `id` eşleşen satırı [book] nesnesiyle değiştirir.
     *
     * Kullanım (ViewModel'den):
     *   bookRepository.updateBook(updatedBook)
     *       .onSuccess { /* UI güncelle */ }
     *       .onFailure { /* hata göster */ }
     */
    suspend fun updateBook(book: Book): Result<Unit> = runCatching {
        supabase.postgrest["books"].update(book) {
            filter { eq("id", book.id) }
        }
    }

    // ── ÖDEV 2: Silme ─────────────────────────────────────────────────────────────────
    /**
     * Verilen [id]'ye sahip kitabı kalıcı olarak siler.
     *
     * Kullanım (ViewModel'den):
     *   bookRepository.deleteBook(book.id)
     *       .onSuccess { /* listeden kaldır */ }
     *       .onFailure { /* hata göster */ }
     */
    suspend fun deleteBook(id: String): Result<Unit> = runCatching {
        supabase.postgrest["books"].delete {
            filter { eq("id", id) }
        }
    }

    // ── ÖDEV 2: Arama ─────────────────────────────────────────────────────────────────
    /**
     * Kitap adı (title) VEYA yazar (author) alanında büyük/küçük harf
     * duyarsız kısmi eşleşme (ILIKE) ile arama yapar.
     *
     * Örnek:
     *   searchBooks("tolkien") → Tolkien'e ait tüm kitapları döner.
     *   searchBooks("yüzük")   → Başlığında "yüzük" geçen kitapları döner.
     *
     * Kullanım (ViewModel'den):
     *   bookRepository.searchBooks(query)
     *       .onSuccess { books -> _searchResults.value = books }
     *       .onFailure { /* hata göster */ }
     */
    suspend fun searchBooks(query: String): Result<List<Book>> = runCatching {
        val pattern = "%${query.trim()}%"
        supabase.postgrest["books"]
            .select {
                filter {
                    or {
                        ilike("title", pattern)
                        ilike("author", pattern)
                    }
                }
            }
            .decodeList<Book>()
    }
}