package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.IllegalArgumentException

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun saveBookTest() {
        val request = BookRequest("이상한 나라의 엘리스")

        bookService.saveBook(request)

        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("이상한 나라의 엘리스")
    }

    @Test
    fun loanBookTest() {
        val savedBook = bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("김민혁", null))
        val request = BookLoanRequest("김민혁", "이상한 나라의 엘리스")

        bookService.loanBook(request)

        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo(savedBook.name)
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].isReturn).isFalse
    }

    @Test
    fun loanBookFailTest() {
        val savedBook = bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("김민혁", null))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                savedUser,
                savedBook.name,
                false
            )
        )
        val request = BookLoanRequest(savedUser.name, savedBook.name)

        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    fun returnBookTest() {
        val savedBook = bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("김민혁", null))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                savedUser,
                savedBook.name,
                false
            )
        )
        val request = BookReturnRequest(savedUser.name, savedBook.name)

        bookService.returnBook(request)

        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue

    }
}