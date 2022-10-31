package com.wafflestudio.seminar.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class SeminarExceptionHandler {
    @ExceptionHandler(value = [SeminarException::class])
    fun handle(e: SeminarException): ResponseEntity<Any> {
        return ResponseEntity(e.message, e.status)
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handle(e: MethodArgumentNotValidException): ResponseEntity<Any> {
        val errors = HashMap<String, String>()
        e.fieldErrors.forEach { errors[it.field] = it.defaultMessage!! }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(value = [MissingRequestHeaderException::class])
    fun handle(e: MissingRequestHeaderException): ResponseEntity<Any> {
        return ResponseEntity("사용자를 식별할 수 없습니다", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
    fun handle(e: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        return ResponseEntity(e.name + " requires " + e.requiredType, HttpStatus.BAD_REQUEST)
    }
    
    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handle(e: HttpMessageNotReadableException): ResponseEntity<Any> {
        return ResponseEntity("잘못된 역할입니다", HttpStatus.BAD_REQUEST)
    }
}