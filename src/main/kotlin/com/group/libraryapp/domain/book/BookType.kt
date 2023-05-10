package com.group.libraryapp.domain.book

enum class BookType {
    COMPUTER,
    ECONOMY,
    SOCIETY,
    LANGUAGE,
    SCIENCE,
}

// 분기를 태우지 않고, 유용하게 활용 가능.
enum class BookTypeV2(val score: Int) {
    COMPUTER(10),
    ECONOMY(8),
    SOCIETY(7),
}

fun getBookScore(type: BookTypeV2): Int {
    return type.score
}