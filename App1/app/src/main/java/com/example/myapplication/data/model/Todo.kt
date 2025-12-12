package com.example.myapplication.data.model

data class Todo(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)
