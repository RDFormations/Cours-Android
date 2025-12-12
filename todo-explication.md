# 📝 Explication de l'Application Todo

---

## 1. Architecture Globale

```
┌─────────────────────────────────────────────────────────────────┐
│                         TodoScreen                              │
│                    (UI - Composables)                           │
│                           │                                     │
│              collectAsState()  │  appelle fonctions             │
│                           ▼                                     │
├─────────────────────────────────────────────────────────────────┤
│                       TodoViewModel                             │
│                    (Logique métier)                             │
│                           │                                     │
│                    MutableStateFlow                             │
│                           ▼                                     │
├─────────────────────────────────────────────────────────────────┤
│                       TodoUiState                               │
│                    (État immutable)                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Le Modèle de Données

### `Todo.kt`

```kotlin
data class Todo(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)
```

**Explication :**

- `data class` : Génère automatiquement `equals()`, `hashCode()`, `toString()`, `copy()`
- `val` : Propriétés immutables (on ne modifie jamais, on crée une copie)
- `isDone = false` : Valeur par défaut, une nouvelle tâche n'est pas terminée

---

## 3. L'État de l'UI

### `TodoUiState`

```kotlin
data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val searchQuery: String = "",
    val inputText: String = ""
) {
    val filteredTodos: List<Todo>
        get() = if (searchQuery.isBlank()) todos
        else todos.filter { it.title.contains(searchQuery, ignoreCase = true) }
}
```

**Explication :**

| Propriété       | Rôle                                                     |
| --------------- | -------------------------------------------------------- |
| `todos`         | Liste complète des tâches                                |
| `searchQuery`   | Texte de la barre de recherche                           |
| `inputText`     | Texte du champ d'ajout                                   |
| `filteredTodos` | Propriété calculée : filtre les todos selon la recherche |

**Pourquoi une propriété calculée ?**

```kotlin
val filteredTodos: List<Todo>
    get() = if (searchQuery.isBlank()) todos
    else todos.filter { it.title.contains(searchQuery, ignoreCase = true) }
```

- Pas besoin de stocker deux listes
- Se recalcule automatiquement quand `todos` ou `searchQuery` change
- `ignoreCase = true` : recherche insensible à la casse

---

## 4. Le ViewModel

### Structure

```kotlin
class TodoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private var nextId = 0

    // ... fonctions
}
```

**Explication :**

| Élément    | Rôle                                                               |
| ---------- | ------------------------------------------------------------------ |
| `_uiState` | StateFlow **mutable** (privé) - seul le ViewModel peut le modifier |
| `uiState`  | StateFlow **lecture seule** (public) - exposé à l'UI               |
| `nextId`   | Compteur pour générer des IDs uniques                              |

### Fonctions du ViewModel

#### 1. Modifier la recherche

```kotlin
fun onSearchQueryChange(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
}
```

- `update {}` : Mise à jour thread-safe du StateFlow
- `copy()` : Crée une nouvelle instance avec `searchQuery` modifié
- L'UI se recompose automatiquement

#### 2. Modifier le texte d'entrée

```kotlin
fun onInputChange(text: String) {
    _uiState.update { it.copy(inputText = text) }
}
```

#### 3. Ajouter une tâche

```kotlin
fun addTodo() {
    val currentInput = _uiState.value.inputText
    if (currentInput.isBlank()) return  // Validation

    _uiState.update { state ->
        state.copy(
            todos = state.todos + Todo(id = nextId++, title = currentInput),
            inputText = ""  // Vide le champ
        )
    }
}
```

**Décomposition :**

1. Récupère le texte actuel
2. Validation : ne rien faire si vide
3. Met à jour l'état :
   - Ajoute un nouveau `Todo` à la liste (`+` crée une nouvelle liste)
   - Vide le champ d'entrée
   - `nextId++` : utilise puis incrémente

#### 4. Basculer l'état d'une tâche

```kotlin
fun toggleTodo(id: Int) {
    _uiState.update { state ->
        state.copy(
            todos = state.todos.map { todo ->
                if (todo.id == id) todo.copy(isDone = !todo.isDone) else todo
            }
        )
    }
}
```

**Décomposition :**

- `map {}` : Transforme chaque élément de la liste
- Si l'ID correspond : inverse `isDone` avec `copy()`
- Sinon : retourne le todo inchangé

#### 5. Supprimer une tâche

```kotlin
fun deleteTodo(id: Int) {
    _uiState.update { state ->
        state.copy(todos = state.todos.filter { it.id != id })
    }
}
```

- `filter {}` : Garde uniquement les éléments où la condition est vraie
- Garde tous les todos dont l'ID est différent de celui à supprimer

---

## 5. L'Interface Utilisateur

### Structure de `TodoScreen`

```kotlin
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ...
}
```

**Connexion ViewModel → UI :**

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

- `collectAsState()` : Convertit le `StateFlow` en `State` Compose
- `by` : Délégation, permet d'utiliser `uiState` directement (pas `uiState.value`)
- Quand le StateFlow émet une nouvelle valeur → recomposition automatique

### Barre de Recherche

```kotlin
OutlinedTextField(
    value = uiState.searchQuery,
    onValueChange = viewModel::onSearchQueryChange,
    modifier = Modifier.fillMaxWidth(),
    placeholder = { Text("Rechercher...") },
    leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Rechercher")
    },
    singleLine = true
)
```

**Flux de données :**

```
Utilisateur tape "test"
       ↓
onValueChange déclenché
       ↓
viewModel.onSearchQueryChange("test")
       ↓
_uiState.update { it.copy(searchQuery = "test") }
       ↓
StateFlow émet nouvelle valeur
       ↓
collectAsState() détecte le changement
       ↓
UI se recompose avec filteredTodos mis à jour
```

### Champ d'Ajout

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    OutlinedTextField(
        value = uiState.inputText,
        onValueChange = viewModel::onInputChange,
        modifier = Modifier.weight(1f),
        placeholder = { Text("Nouvelle tâche...") },
        singleLine = true
    )
    Spacer(modifier = Modifier.width(8.dp))
    FilledIconButton(onClick = viewModel::addTodo) {
        Icon(Icons.Default.Add, contentDescription = "Ajouter")
    }
}
```

- `Row` : Disposition horizontale
- `Modifier.weight(1f)` : Le TextField prend tout l'espace restant
- `viewModel::addTodo` : Référence de fonction (équivalent à `{ viewModel.addTodo() }`)

### Liste des Tâches

```kotlin
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(uiState.filteredTodos, key = { it.id }) { todo ->
        TodoItem(
            todo = todo,
            onToggle = { viewModel.toggleTodo(todo.id) },
            onDelete = { viewModel.deleteTodo(todo.id) }
        )
    }
}
```

**Explication :**

| Élément                      | Rôle                                                           |
| ---------------------------- | -------------------------------------------------------------- |
| `LazyColumn`                 | Liste scrollable optimisée (ne rend que les éléments visibles) |
| `Arrangement.spacedBy(8.dp)` | Espacement de 8dp entre chaque élément                         |
| `key = { it.id }`            | Identifiant unique pour optimiser les recompositions           |
| `filteredTodos`              | Utilise la liste filtrée, pas la liste complète                |

### Composable TodoItem

```kotlin
@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isDone,
                onCheckedChange = { onToggle() }
            )

            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                color = if (todo.isDone) Color.Gray else Color.Unspecified
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

**Pattern State Hoisting :**

- `TodoItem` est **stateless** : il ne gère pas d'état
- L'état (`todo`) et les callbacks (`onToggle`, `onDelete`) viennent du parent
- Avantages : réutilisable, testable, prévisible

**Style conditionnel :**

```kotlin
textDecoration = if (todo.isDone) TextDecoration.LineThrough else null
color = if (todo.isDone) Color.Gray else Color.Unspecified
```

- Si terminé : texte barré et gris
- Sinon : style par défaut

---

## 6. Flux de Données Complet

```
┌─────────────────────────────────────────────────────────────────┐
│                        UTILISATEUR                              │
│                             │                                   │
│          tape/clique/coche  │                                   │
│                             ▼                                   │
├─────────────────────────────────────────────────────────────────┤
│                         TodoScreen                              │
│                             │                                   │
│         onValueChange / onClick                                 │
│                             ▼                                   │
├─────────────────────────────────────────────────────────────────┤
│                       TodoViewModel                             │
│                             │                                   │
│              _uiState.update { ... }                            │
│                             ▼                                   │
├─────────────────────────────────────────────────────────────────┤
│                    MutableStateFlow                             │
│                             │                                   │
│                      émet nouvelle valeur                       │
│                             ▼                                   │
├─────────────────────────────────────────────────────────────────┤
│                    collectAsState()                             │
│                             │                                   │
│                      recomposition                              │
│                             ▼                                   │
├─────────────────────────────────────────────────────────────────┤
│                    UI mise à jour                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Résumé des Concepts Clés

| Concept            | Utilisation                              |
| ------------------ | ---------------------------------------- |
| `data class`       | Modèle immutable avec `copy()`           |
| `StateFlow`        | Flux d'état réactif                      |
| `update {}`        | Modification thread-safe                 |
| `collectAsState()` | Connexion StateFlow → Compose            |
| `LazyColumn`       | Liste performante                        |
| `key`              | Optimisation des recompositions          |
| State Hoisting     | Composables stateless réutilisables      |
| `map`, `filter`    | Transformations fonctionnelles de listes |
