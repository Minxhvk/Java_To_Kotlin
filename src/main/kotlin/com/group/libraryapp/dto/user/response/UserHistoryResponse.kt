package com.group.libraryapp.dto.user.response

data class UserLoanHistoryResponse(
    val name: String, // User Name
    val books: List<BookHistoryResponse>
)

data class BookHistoryResponse(
    val name: String, // Book Name
    val isReturn: Boolean
)