package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository
) {
    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    fun saveUserTest() {
        val request = UserCreateRequest("김민혁", null)

        userService.saveUser(request)

        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("김민혁")
        assertThat(results[0].age).isNull()
    }

    @Test
    fun getUsersTest() {
        userRepository.saveAll(
            listOf(
                User("A", 20),
                User("B", 30),
            )
        )

        val results = userService.getUsers()

        assertThat(results).hasSize(2)
        assertThat(results).extracting("name").containsExactlyInAnyOrder("A", "B") // ["A", "B"]
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, 30) // ["A", "B"]
    }

    @Test
    fun updateUserNameTest() {

        val savedUser = userRepository.save(User("A", null))
        val request = UserUpdateRequest(savedUser.id!!, "B")

        userService.updateUserName(request)

        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    @Test
    fun deleteUserTest() {
        userRepository.save(User("A", null))

        userService.deleteUser("A")

        userRepository.findAll().isEmpty()

    }

    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다.")
    fun getUserLoanHistoriesTest1() {

        userRepository.save(User("A", null))

        val results = userService.getUserLoanHistories()

        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 많은 유저의 응답이 정상 동작한다.")
    fun getUserLoanHistoriesTest2() {

        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED),
        ))

        val results = userService.getUserLoanHistories()

        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
            .containsExactlyInAnyOrder("책1", "책2", "책3")
        assertThat(results[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)
    }

    // 테스트 코드가 너무 거대해 졌음. 2개로 쪼개는게 더 유지보수 용이.
    // 앞부분 실패 시 뒷부분 검증 불가능.
    @Test
    @DisplayName("Test1 + Test2")
    fun getUserLoanHistoriesTest3() {

        val savedUser = userRepository.saveAll(listOf(
            User("A", null),
            User("B", null),
        ))


        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser[0], "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser[0], "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser[0], "책3", UserLoanStatus.RETURNED),
        ))

        val results = userService.getUserLoanHistories()

        assertThat(results).hasSize(2)
        val userAResult = results.first { it.name == "A"}

        assertThat(userAResult.name).isEqualTo("A")
        assertThat(userAResult.books).hasSize(3)
        assertThat(userAResult.books).extracting("name")
            .containsExactlyInAnyOrder("책1", "책2", "책3")
        assertThat(userAResult.books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)

        val userBResult = results.first { it.name == "B" }

        assertThat(userBResult.name).isEqualTo("B")
        assertThat(userBResult.books).isEmpty()
    }
}