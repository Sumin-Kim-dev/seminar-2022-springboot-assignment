package com.wafflestudio.seminar.core.seminar

import com.wafflestudio.seminar.common.SeminarException
import com.wafflestudio.seminar.core.seminar.api.request.ApplySeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.CreateSeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.ModifySeminarRequest
import com.wafflestudio.seminar.core.seminar.database.SeminarRepository
import com.wafflestudio.seminar.core.seminar.database.UserSeminarRepository
import com.wafflestudio.seminar.core.seminar.service.SeminarService
import com.wafflestudio.seminar.core.user.UserTestHelper
import com.wafflestudio.seminar.global.HibernateQueryCounter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import javax.transaction.Transactional

@SpringBootTest
internal class SeminarTest @Autowired constructor(
    private val seminarService: SeminarService,
    private val hibernateQueryCounter: HibernateQueryCounter,
    private val seminarRepository: SeminarRepository,
    private val userSeminarRepository: UserSeminarRepository,
    private val userTestHelper: UserTestHelper,
) {
    
    @Test
    @Transactional
    fun `세미나 생성`() {
        // given
        val token = userTestHelper.createInstructor("ins@tructor.com").accessToken
        val request = CreateSeminarRequest(
            name = "세미나1", capacity = 100, count = 1, time = "09:00"
        )
        val beforeSize = seminarRepository.findAll().size
        
        // when
        seminarService.createSeminar(token, request)
        
        // then
        assertThat(seminarRepository.findAll()).hasSize(beforeSize + 1)
    }

    @Test
    @Transactional
    fun `세미나 생성 실패 1`() {
        // given
        userTestHelper.createInstructor("ins@tructor.com")
        val token = userTestHelper.login("ins@tructor.com").accessToken
        val request = CreateSeminarRequest(
            name = "세미나1", capacity = 100, count = 1, time = "09:00"
        )
        seminarService.createSeminar(token, request)

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.createSeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("이미 참여하고 있는 세미나가 존재합니다.")
    }

    @Test
    @Transactional
    fun `세미나 생성 실패 2`() {
        // given
        val token = userTestHelper.createInstructor("ins@tructor.com").accessToken
        val request = CreateSeminarRequest(
            name = "", capacity = 100, count = 1, time = "09:00"
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.createSeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("필요한 정보를 모두 입력해주세요")
    }

    @Test
    @Transactional
    fun `세미나 생성 실패 3`() {
        // given
        val token = userTestHelper.createInstructor("ins@tructor.com").accessToken
        val request = CreateSeminarRequest(
            name = "세미나", capacity = 100, count = 1, time = "09-00"
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.createSeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("올바른 시간 값을 입력해주세요")
    }

    @Test
    @Transactional
    fun `세미나 생성 실패 4`() {
        // given
        val token = userTestHelper.createParticipant("ins@tructor.com").accessToken
        val request = CreateSeminarRequest(
            name = "세미나", capacity = 100, count = 1, time = "09-00"
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.createSeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("세미나 진행자만 세미나를 생성할 수 있습니다.")
    }

    @Test
    @Transactional
    fun `세미나 수정`() {
        // given
        createSeminars(1)
        val token = userTestHelper.login("ins@tructor#1.com").accessToken
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val request = ModifySeminarRequest(
            id = id, name = "수정", capacity = null, count = null, time = null, online = null
        )

        // when
        val seminar = seminarService.modifySeminar(token, request)

        // then
        assertThat(seminar.name).isEqualTo("수정")
        assertThat(seminar.capacity).isEqualTo(2)
    }

    @Test
    @Transactional
    fun `세미나 수정 실패 1`() {
        // given
        createSeminars(2)
        val token = userTestHelper.login("ins@tructor#1.com").accessToken
        val id = seminarRepository.findSeminarByName("세미나#2")[0].id
        val request = ModifySeminarRequest(
            id = id, name = "수정", capacity = null, count = null, time = null, online = null
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.modifySeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    @Transactional
    fun `세미나 수정 실패 2`() {
        // given
        createSeminars(2)
        val token = userTestHelper.login("ins@tructor#1.com").accessToken
        val request = ModifySeminarRequest(
            id = -1, name = "수정", capacity = null, count = null, time = null, online = null
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.modifySeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @Transactional
    fun `세미나 수정 실패 3`() {
        // given
        createSeminars(1)
        val token = userTestHelper.login("ins@tructor#1.com").accessToken
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val request = ModifySeminarRequest(
            id = id, name = "수정", capacity = -1, count = null, time = null, online = null
        )

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.modifySeminar(token, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
    }
    
    @ParameterizedTest
    @Transactional
    @CsvSource(value = ["2,0,3", "4,0,2", "4,1,2", "4,2,2", "7,2,3"], delimiter = ',')
    fun `전체 세미나 조회`(seminarSize: Int, page: Int, pageSize: Int) {
        // given
        createSeminars(seminarSize)

        // when
        val pageable = PageRequest.of(page, pageSize)
        val (result, queryCount) = hibernateQueryCounter.count {
            seminarService.getAllSeminar(null, null, pageable)
        }

        // then
        val maxSize = seminarSize / pageSize
        val size = when (page) {
            maxSize -> seminarSize % pageSize
            in 0 until maxSize -> pageSize
            else -> 0
        }
        val const = if (size in 1 until pageSize) {1} else {2}
        assertThat(result).hasSize(size)
        assertThat(queryCount).isEqualTo(2 * size + const)
    }

    @ParameterizedTest
    @Transactional
    @CsvSource(value = ["30,12,2,null", "30,12,2,earliest", "30,0,seminar,null"], delimiter = ',')
    fun `전체 세미나 조회 2`(size: Int, expectedSize: Int, seminarName: String, order: String) {
        // given
        createSeminars(size)

        // when
        val pageable = PageRequest.of(0, 5)
        val (result, queryCount) = hibernateQueryCounter.count {
            seminarService.getAllSeminar(seminarName, order, pageable)
        }

        // then
        assertThat(result).hasSize(expectedSize)
        assertThat(queryCount).isEqualTo(2 * expectedSize + 1)
    }
    
    @Test
    @Transactional
    fun `세미나 조회`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        
        // when
        val seminar = seminarService.readSeminar(id)

        // then
        assertThat(seminar.instructors.size).isEqualTo(1)
        assertThat(seminar.instructors[0].email).isEqualTo("ins@tructor#1.com")
    }

    @Test
    @Transactional
    fun `세미나 조회 실패`() {
        // given
        val id = -1L

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.readSeminar(id)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @Transactional
    fun `세미나 등록`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("participant")

        // when
        val seminar = seminarService.applySeminar(token, id, request)

        // then
        assertThat(seminar.participants?.size).isEqualTo(1)
        assertThat(seminar.participants!![0].email).isEqualTo("par@ticipant.com")
    }

    @Test
    @Transactional
    fun `세미나 등록 2`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createInstructor("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("instructor")

        // when
        val seminar = seminarService.applySeminar(token, id, request)

        // then
        assertThat(seminar.instructors.size).isEqualTo(2)
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 1`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("participant")
        seminarService.applySeminar(token, id, request)

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("이미 세미나에 참여하고 있습니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 2`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("adf")

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("올바른 role을 입력해주세요.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 3`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val tokens = (1..3).map {
            userTestHelper.createParticipant("par#${it}@ticipant.com").accessToken
        }
        val request = ApplySeminarRequest("participant")
        (0 until 2).map {
            seminarService.applySeminar(tokens[it], id, request)
        }

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(tokens[2], id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("이미 수강 정원이 가득찼습니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 4`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("instructor")

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("세미나 진행 자격이 없습니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 5`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createInstructor("ins@tructor#2.com").accessToken
        val request = ApplySeminarRequest("participant")

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("세미나 참여 자격이 없습니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 6`() {
        // given
        createSeminars(2)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.login("ins@tructor#2.com").accessToken
        val request = ApplySeminarRequest("instructor")

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("이미 진행하는 세미나가 존재합니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 7`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com", isRegistered = false).accessToken
        val request = ApplySeminarRequest("participant")

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("활성회원이 아닙니다.")
    }

    @Test
    @Transactional
    fun `세미나 등록 실패 8`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("participant")
        seminarService.applySeminar(token, id, request)
        seminarService.deleteParticipantFromSeminar(token, id)

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.applySeminar(token, id, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("중도포기한 세미나는 다시 참여할 수 없습니다.")
    }
    
    @Test
    @Transactional
    fun `세미나 드랍`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("participant")
        seminarService.applySeminar(token, id, request)
        val beforeSize = userSeminarRepository.findActiveParticipantCountById(id)!!
        
        // when
        seminarService.deleteParticipantFromSeminar(token, id)
        
        // then
        val afterSize = userSeminarRepository.findActiveParticipantCountById(id)!!
        assertThat(afterSize).isEqualTo(beforeSize - 1)
    }

    @Test
    @Transactional
    fun `세미나 드랍 실패 1`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.createParticipant("par@ticipant.com").accessToken
        val request = ApplySeminarRequest("participant")
        seminarService.applySeminar(token, id, request)
        seminarService.deleteParticipantFromSeminar(token, id)

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.deleteParticipantFromSeminar(token, id)
        }
        
        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("이미 드랍한 세미나입니다.")
    }

    @Test
    @Transactional
    fun `세미나 드랍 실패 2`() {
        // given
        createSeminars(1)
        val id = seminarRepository.findSeminarByName("세미나#1")[0].id
        val token = userTestHelper.login("ins@tructor#1.com").accessToken

        // when
        val exception = assertThrows<SeminarException> {
            seminarService.deleteParticipantFromSeminar(token, id)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(exception.message).isEqualTo("세미나 진행자는 세미나를 드랍할 수 없습니다.")
    }
    
    private fun createSeminars(size: Int) {
        val instructorTokens = (1..size).map {
            userTestHelper.createInstructor("ins@tructor#$it.com").accessToken
        }
        val requests = (1..size).map {
            CreateSeminarRequest(
                name = "세미나#$it", capacity = 2, count = 1, time = "09:00"
            )
        }
        (0 until size).map {
            seminarService.createSeminar(instructorTokens[it], requests[it])
        }
    }
}