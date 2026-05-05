package com.example.libraryapp.data.repository

import com.example.libraryapp.data.model.Book
import com.example.libraryapp.data.model.BorrowRecord
import com.example.libraryapp.data.supabase.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.*

class BookRepository {


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

    suspend fun updateBook(book: Book): Result<Unit> = runCatching {
        supabase.postgrest["books"].update(book) {
            filter { eq("id", book.id) }
        }
    }

    suspend fun deleteBook(id: String): Result<Unit> = runCatching {
        supabase.postgrest["books"].delete {
            filter { eq("id", id) }
        }
    }

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


    /**
     * 1. available_copies > 0 kontrolü
     * 2. borrow_records tablosuna kayıt ekle
     * 3. available_copies'i 1 azalt (Supabase RPC ile atomic)
     *
     * @param studentId  Giriş yapan kullanıcının auth id'si
     * @param book       Ödünç alınacak kitap
     * @param days       Kaç günlüğüne (1–5)
     */
    suspend fun borrowBook(
        studentId: String,
        book: Book,
        days: Int
    ): Result<BorrowRecord> = runCatching {
        if (book.avaiableCopies <= 0)
            error("Bu kitabın stoğu tükenmiş.")

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val cal = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, days.coerceIn(1, 5))
        }

        val record = BorrowRecord(
            studentId  = studentId,
            bookId     = book.id,
            borrowedAt = fmt.format(today),
            dueDate    = fmt.format(cal.time),
            isReturned = false
        )

        val inserted = supabase.postgrest["borrow_records"]
            .insert(record) { select() }
            .decodeSingle<BorrowRecord>()

        supabase.postgrest.rpc(
            "decrement_available_copies",
            buildJsonObject { put("p_book_id", book.id) }
        )

        inserted
    }

    suspend fun getBorrowsByStudent(studentId: String): Result<List<BorrowRecord>> = runCatching {
        supabase.postgrest["borrow_records"]
            .select {
                filter { eq("student_id", studentId) }
                order("borrowed_at", Order.DESCENDING)
            }
            .decodeList<BorrowRecord>()
    }


    suspend fun returnBook(record: BorrowRecord): Result<Unit> = runCatching {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        supabase.postgrest["borrow_records"]
            .update(
                buildJsonObject {
                    put("is_returned", true)
                    put("returned_at", today)
                }
            ) {
                filter { eq("id", record.id) }
            }

        supabase.postgrest.rpc(
            "increment_available_copies",
            buildJsonObject { put("p_book_id", record.bookId) }
        )
    }
}