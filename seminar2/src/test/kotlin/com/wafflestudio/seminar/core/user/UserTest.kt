package com.wafflestudio.seminar.core.user

import com.wafflestudio.seminar.core.user.api.UserException
import com.wafflestudio.seminar.core.user.api.request.LoginRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.database.UserRepository
import com.wafflestudio.seminar.core.user.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class UserTest @Autowired constructor(
    private val userService: UserService,
    private val userTestHelper: UserTestHelper,
    private val userRepository: UserRepository,
) {
    @Test
    @Order(1)
    fun `회원 가입`() {
        // given
        val request = SignUpRequest("w@affle.com", "smkim", "1234", SignUpRequest.Role.INSTRUCTOR)
        val beforeSize = userRepository.findAll().size
        
        // when
        userService.createUser(request)

        // then
        assertThat(userRepository.findAll()).hasSize(beforeSize + 1)
        assertThat(userRepository.findByEmail("w@affle.com")).isNotNull
    }

    @Test
    @Order(2)
    fun `회원가입 실패`() {
        // given
        val request = SignUpRequest("w@affle.com", "smkim", "1234", SignUpRequest.Role.INSTRUCTOR)

        // when
        val exception = assertThrows<UserException> {
            userService.createUser(request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.CONFLICT)
    }
    
    @Test
    @Order(3)
    fun `로그인`() {
        // given
        userTestHelper.createUser("waffle@studio.com", password = "1234")
        val request = LoginRequest("waffle@studio.com", "1234")
        val size = userRepository.findAll().size

        // when
        val result = userService.loginUser(request)

        // then
        assertThat(result.accessToken).isNotEmpty
        assertThat(userRepository.findAll()).hasSize(size)
        assertThat(userRepository.findByEmail("waffle@studio.com")).isNotNull
    }

    @Test
    @Order(4)
    fun `로그인 실패 1`() {
        // given
        val request = LoginRequest("waffl@studio.com", "1234")

        // when
        val exception = assertThrows<UserException> {
            userService.loginUser(request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @Order(5)
    fun `로그인 실패 2`() {
        // given
        val request = LoginRequest("waffle@studio.com", "123")

        // when
        val exception = assertThrows<UserException> {
            userService.loginUser(request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}