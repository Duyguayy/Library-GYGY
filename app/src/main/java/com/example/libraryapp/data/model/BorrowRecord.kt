package com.example.libraryapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BorrowRecord(
    val id: String = "",
    @SerialName("student_id") val studentId: String = "",
    @SerialName("book_id") val bookId: String = "",
    @SerialName("borrowed_at") val borrowedAt: String = "",
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("returned_at") val returnedAt: String? = null,
    @SerialName("is_returned") val isReturned: Boolean = false
) {

    val isOverdue: Boolean
        get() {
            if (isReturned) return false
            return try {
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val due = fmt.parse(dueDate) ?: return false
                due.before(java.util.Date())
            } catch (e: Exception) { false }
        }
}