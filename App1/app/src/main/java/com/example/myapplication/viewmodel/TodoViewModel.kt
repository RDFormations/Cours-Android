package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val searchQuery: String = "",
    val inputText: String = ""
) {
    val filteredTodos: List<Todo>
        get() = if (searchQuery.isBlank()) todos
        else todos.filter { it.title.contains(searchQuery, ignoreCase = true) }
}

class TodoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private var nextId = 0

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun addTodo() {
        val currentInput = _uiState.value.inputText
        if (currentInput.isBlank()) return

        _uiState.update { state ->
            state.copy(
                todos = state.todos + Todo(id = nextId++, title = currentInput),
                inputText = ""
            )
        }
    }

    fun toggleTodo(id: Int) {
        _uiState.update { state ->
            state.copy(
                todos = state.todos.map { todo ->
                    if (todo.id == id) todo.copy(isDone = !todo.isDone) else todo
                }
            )
        }
    }

    fun deleteTodo(id: Int) {
        _uiState.update { state ->
            state.copy(todos = state.todos.filter { it.id != id })
        }
    }
}
