package com.wafflestudio.seminar.core.user

import com.wafflestudio.seminar.core.user.api.request.LoginRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.database.UserEntity
import com.wafflestudio.seminar.core.user.database.UserRepository
import com.wafflestudio.seminar.core.user.service.AuthToken
import com.wafflestudio.seminar.core.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
internal class UserTestHelper @Autowired constructor(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val userService: UserService,
) {
    fun createUser(
        email: String,
        username: String = "",
        password: String = "",
    ): UserEntity {
        return userRepository.save(UserEntity(email, username, passwordEncoder.encode(password)))
    }

    fun createInstructor(
        email: String,
        username: String = "",
        password: String = "",
        company: String = "",
        year: Int? = null,
    ): AuthToken {
        val request = SignUpRequest(
            email, username, password,
            role = SignUpRequest.Role.INSTRUCTOR,
            company = company,
            year = year
        )
        return userService.createUser(request)
    }

    fun createParticipant(
        email: String,
        username: String = "",
        password: String = "",
        university: String = "",
        isRegistered: Boolean = true,
    ): AuthToken {
        val request = SignUpRequest(
            email, username, password,
            role = SignUpRequest.Role.PARTICIPANT,
            university = university,
            isRegistered = isRegistered,
        )
        return userService.createUser(request)
    }

    fun login(
        email: String,
        password: String = ""
    ): AuthToken {
        val loginRequest = LoginRequest(email, password)
        return userService.loginUser(loginRequest)
    }

    fun size(): Long {
        return userRepository.count()
    }
}