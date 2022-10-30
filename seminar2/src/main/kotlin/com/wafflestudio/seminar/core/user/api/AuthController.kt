package com.wafflestudio.seminar.core.user.api

import com.wafflestudio.seminar.common.Authenticated
import com.wafflestudio.seminar.core.user.api.request.LogInRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.service.AuthService
import com.wafflestudio.seminar.core.user.service.AuthToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/api/v1/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest) = authService.signUp(request)

    @PostMapping("/api/v1/signin")
    fun logIn(@Valid @RequestBody request: LogInRequest) = authService.logIn(request)

    @Authenticated
    @GetMapping("/api/v1/me")
    fun getMe(@RequestHeader request: AuthToken) = authService.getMe(request)
}