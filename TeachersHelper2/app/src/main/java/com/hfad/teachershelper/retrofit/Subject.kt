package com.hfad.teachershelper.retrofit


data class CreateSubjectsRequest(
    val subjects: List<Subject>
)

data class Subject(
    val id: Int,
    val name: String,
    val topics: List<Topic>
)

data class Topic(
    val id: Int,
    val title: String,
    val content: String,
    val quiz: Quiz
)

// Quiz.kt
data class Quiz(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
) {
    companion object {
        val EMPTY = Quiz("", emptyList(), -1)
    }
}

//data class Quiz(
//    val question: String,
//    val options: List<String>,
//    val correctAnswerIndex: Int
//) = этот правильный

//data class Subject (
//    val id: Int,
//    val name: String,
//    val topics: List<Topic>
//    // сюда лобавить параметры если нужно
//)

//data class Topic(
//    val id: Int,
//    val title: String,
//    val content: String,
//    val quiz: Quiz
//)

//data class Quiz(
//    val question: String,
//    val options: List<String>,
//    val correctAnswerIndex: Int
//)
