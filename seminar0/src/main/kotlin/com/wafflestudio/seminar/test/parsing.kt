package com.wafflestudio.seminar.test

// 첫 입력을 파싱해서 학생 리스트로 만들어주는 함수
fun toStudentList(command: String): MutableList<Student> {
    return command.trim().split('[', ']', '"', ',').filter {
        it.isNotEmpty()
    }.map { Student(name = it, valid = true) }.toMutableList()
}

// 이후 입력되는 명령어를 파싱해서 반환
fun toCommand(command: String): List<String> {
    return command.trim().split(" ")
}