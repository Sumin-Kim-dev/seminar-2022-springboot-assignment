package com.wafflestudio.seminar.survey.database

import com.wafflestudio.seminar.survey.domain.OperatingSystem
import com.wafflestudio.seminar.survey.domain.SurveyResponse

data class SurveyResponse(
    val id: Long,
    val operatingSystem: OperatingSystem,
    val springExp: Int,
    val rdbExp: Int,
    val programmingExp: Int,
    val major: String,
    val grade: String,
) {
    constructor(surveyResponse: SurveyResponse) : this(
        id = surveyResponse.id,
        operatingSystem = surveyResponse.operatingSystem,
        springExp = surveyResponse.springExp,
        rdbExp = surveyResponse.rdbExp,
        programmingExp = surveyResponse.programmingExp,
        major = surveyResponse.major,
        grade = surveyResponse.grade
    )
}