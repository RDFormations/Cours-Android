# Jour 1 - Introduction au Développement Android avec Jetpack Compose

## 1. Introduction à Kotlin

### 1.1 Pourquoi Kotlin ?

Kotlin est le langage officiel recommandé par Google pour le développement Android depuis 2019. Il offre plusieurs avantages majeurs :

- **Concis** : Moins de code boilerplate qu'en Java
- **Sûr** : Gestion native des valeurs nulles (null safety)
- **Interopérable** : Compatible à 100% avec Java
- **Fonctionnel** : Support des paradigmes de programmation fonctionnelle

### 1.2 Les Bases de Kotlin

#### Variables et Types

```kotlin
// Variables immutables (recommandé)
val nom: String = "Android"
val age = 10  // Inférence de type

// Variables mutables
var compteur: Int = 0
compteur = 1

// Types nullables
val texte: String? = null
```

#### Fonctions

```kotlin
// Fonction classique
fun addition(a: Int, b: Int): Int {
    return a + b
}

// Expression function (single-expression)
fun multiplication(a: Int, b: Int) = a * b

// Fonction avec paramètres par défaut
fun saluer(nom: String, prefix: String = "Bonjour") = "$prefix, $nom!"

// Fonction d'ordre supérieur
fun operer(a: Int, b: Int, operation: (Int, Int) -> Int) = operation(a, b)
```

#### Collections et Programmation Fonctionnelle

```kotlin
val nombres = listOf(1, 2, 3, 4, 5)

// Map, Filter, Reduce
val doubles = nombres.map { it * 2 }           // [2, 4, 6, 8, 10]
val pairs = nombres.filter { it % 2 == 0 }     // [2, 4]
val somme = nombres.reduce { acc, n -> acc + n } // 15

// Chaînage d'opérations
val resultat = nombres
    .filter { it > 2 }
    .map { it * 10 }
    .sum()  // 120
```

#### Classes et Data Classes

```kotlin
// Data class (génère equals, hashCode, toString, copy)
data class Utilisateur(
    val id: Int,
    val nom: String,
    val email: String
)

// Utilisation
val user = Utilisateur(1, "Alice", "alice@mail.com")
val copie = user.copy(nom = "Bob")

// Sealed class (hiérarchie fermée)
sealed class Resultat<out T> {
    data class Succes<T>(val data: T) : Resultat<T>()
    data class Erreur(val message: String) : Resultat<Nothing>()
    object Chargement : Resultat<Nothing>()
}
```

#### Extensions

```kotlin
// Ajouter des fonctions à des classes existantes
fun String.capitalizeWords() = split(" ").joinToString(" ") {
    it.replaceFirstChar { c -> c.uppercase() }
}

val texte = "hello world".capitalizeWords() // "Hello World"
```

---

## 2. Introduction à Android

### 2.1 Architecture d'une Application Android

```
┌─────────────────────────────────────────┐
│              Application                │
├─────────────────────────────────────────┤
│   UI Layer (Jetpack Compose)            │
│   ├── Screens / Composables             │
│   └── ViewModels                        │
├─────────────────────────────────────────┤
│   Domain Layer (Optionnel)              │
│   └── Use Cases                         │
├─────────────────────────────────────────┤
│   Data Layer                            │
│   ├── Repositories                      │
│   ├── Data Sources (API, DB)            │
│   └── Models                            │
└─────────────────────────────────────────┘
```

### 2.2 Cycle de Vie Android

Une activité Android passe par différents états :

```
onCreate() → onStart() → onResume() → [RUNNING]
                                          ↓
                                     onPause()
                                          ↓
                                     onStop()
                                          ↓
                                    onDestroy()
```

Avec Jetpack Compose, la gestion du cycle de vie est simplifiée grâce aux **LifecycleOwner** et aux **effets**.

---

## 3. Jetpack Compose - Les Bases

### 3.1 Qu'est-ce que Jetpack Compose ?

Jetpack Compose est le toolkit moderne de Google pour construire des interfaces utilisateur natives Android de manière **déclarative**.

**Avantages :**

- Code UI 100% Kotlin (plus de XML)
- UI déclarative et réactive
- Prévisualisation en temps réel
- Moins de code, plus de lisibilité
- Gestion d'état simplifiée

### 3.2 Premier Composable

```kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Bonjour, $name!",
        modifier = modifier
    )
}

// Prévisualisation
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting(name = "Android")
}
```

### 3.3 Les Composables de Base

#### Text

```kotlin
@Composable
fun TextExemple() {
    Text(
        text = "Hello Compose",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Blue,
        textAlign = TextAlign.Center
    )
}
```

#### Button

```kotlin
@Composable
fun ButtonExemple(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue
        )
    ) {
        Text("Cliquez-moi")
    }
}
```

#### Image

```kotlin
@Composable
fun ImageExemple() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo de l'application",
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Crop
    )
}
```

#### TextField

```kotlin
@Composable
fun TextFieldExemple() {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Entrez votre nom") },
        placeholder = { Text("Nom...") }
    )
}
```

### 3.4 Layouts

#### Column (Vertical)

```kotlin
@Composable
fun ColumnExemple() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Premier")
        Text("Deuxième")
        Text("Troisième")
    }
}
```

#### Row (Horizontal)

```kotlin
@Composable
fun RowExemple() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Gauche")
        Text("Centre")
        Text("Droite")
    }
}
```

#### Box (Superposition)

```kotlin
@Composable
fun BoxExemple() {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null
        )
        Text(
            text = "Texte superposé",
            color = Color.White
        )
    }
}
```

### 3.5 Modifier

Le `Modifier` est l'élément clé pour personnaliser les composables :

```kotlin
@Composable
fun ModifierExemple() {
    Text(
        text = "Texte stylisé",
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clickable { /* action */ }
    )
}
```

**Ordre des modifiers important !** Ils s'appliquent de haut en bas.

---

## 4. Gestion de l'État Local

### 4.1 State et Recomposition

Compose recrée (recompose) l'UI automatiquement quand l'état change.

```kotlin
@Composable
fun Compteur() {
    var count by remember { mutableStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Compteur: $count",
            fontSize = 24.sp
        )

        Row {
            Button(onClick = { count-- }) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { count++ }) {
                Text("+")
            }
        }
    }
}
```

### 4.2 remember vs rememberSaveable

```kotlin
// remember : survit aux recompositions
var state by remember { mutableStateOf("") }

// rememberSaveable : survit aux changements de configuration (rotation)
var state by rememberSaveable { mutableStateOf("") }
```

### 4.3 State Hoisting (Élévation d'État)

Pattern recommandé : séparer l'état de sa représentation.

```kotlin
// Composable stateless (réutilisable)
@Composable
fun StatelessCounter(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row {
        Button(onClick = onDecrement) { Text("-") }
        Text("$count")
        Button(onClick = onIncrement) { Text("+") }
    }
}

// Composable stateful (gère l'état)
@Composable
fun StatefulCounter() {
    var count by remember { mutableStateOf(0) }

    StatelessCounter(
        count = count,
        onIncrement = { count++ },
        onDecrement = { count-- }
    )
}
```

### 4.4 Limites de l'état local

L'état local avec `remember` et `mutableStateOf` convient pour :

- État simple et temporaire
- État propre à un seul composable

**Problèmes :**

- Perdu lors de la destruction de l'activité
- Difficile à partager entre composables
- Mélange logique métier et UI
- Spécifique à Compose (non portable)

➡️ **Solution : ViewModel + StateFlow**

---

## 5. Kotlin Flow et StateFlow

### 5.1 Introduction aux Flows

Un **Flow** est un flux de données asynchrone qui émet des valeurs séquentiellement.

```
┌─────────────┐    emit()    ┌─────────────┐    collect()    ┌─────────────┐
│   Source    │ ──────────▶  │    Flow     │  ────────────▶  │  Collecteur │
│   (API/DB)  │              │  (Pipeline) │                 │    (UI)     │
└─────────────┘              └─────────────┘                 └─────────────┘
```

**Types de Flow :**

| Type            | Description                               | Cas d'usage              |
| --------------- | ----------------------------------------- | ------------------------ |
| `Flow<T>`       | Flux froid, s'exécute à chaque collection | Requêtes API, lecture DB |
| `StateFlow<T>`  | Flux chaud avec état actuel               | État UI                  |
| `SharedFlow<T>` | Flux chaud multi-collecteurs              | Events one-shot          |

### 5.2 StateFlow en détail

`StateFlow` est un **flux d'état** optimisé pour représenter l'état d'un écran.

**Caractéristiques :**

- Toujours une valeur initiale
- Conserve uniquement la dernière valeur
- Émet automatiquement aux nouveaux collecteurs
- Ignore les valeurs dupliquées (equality check)

```kotlin
// Création
private val _state = MutableStateFlow(initialValue)
val state: StateFlow<T> = _state.asStateFlow()
```

### 5.3 MutableStateFlow vs StateFlow

```kotlin
// MutableStateFlow : lecture ET écriture (privé dans le ViewModel)
private val _uiState = MutableStateFlow(UiState())

// StateFlow : lecture seule (exposé à l'UI)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

**Pourquoi cette séparation ?**

- Encapsulation : l'UI ne peut pas modifier l'état directement
- Unidirectional Data Flow : les modifications passent par des fonctions du ViewModel

### 5.4 Méthodes de mise à jour

#### `.value` - Accès direct

```kotlin
// Lecture
val current = _uiState.value

// Écriture (⚠️ non thread-safe)
_uiState.value = UiState(count = 5)
```

#### `.update {}` - Mise à jour atomique (recommandé)

```kotlin
// Thread-safe, basé sur l'état actuel
_uiState.update { currentState ->
    currentState.copy(count = currentState.count + 1)
}
```

#### `.emit()` - Émission suspend

```kotlin
// Dans une coroutine
viewModelScope.launch {
    _uiState.emit(newState)
}
```

### 5.5 Opérateurs de transformation

Les opérateurs permettent de transformer les flux avant collection.

```kotlin
class UserViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)

    // map : transformer les valeurs
    val userName: StateFlow<String> = _user
        .map { it?.name ?: "Invité" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Invité")

    // filter : filtrer les valeurs
    val activeUsers: Flow<User> = _user
        .filterNotNull()
        .filter { it.isActive }

    // combine : fusionner plusieurs flows
    private val _searchQuery = MutableStateFlow("")
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())

    val filteredItems: StateFlow<List<Item>> = combine(
        _searchQuery,
        _allItems
    ) { query, items ->
        if (query.isBlank()) items
        else items.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

### 5.6 stateIn : Convertir un Flow en StateFlow

```kotlin
val myStateFlow: StateFlow<T> = myFlow.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = defaultValue
)
```

**Options de `SharingStarted` :**

| Option                     | Comportement                                      |
| -------------------------- | ------------------------------------------------- |
| `Eagerly`                  | Démarre immédiatement, ne s'arrête jamais         |
| `Lazily`                   | Démarre au premier collecteur, ne s'arrête jamais |
| `WhileSubscribed(timeout)` | Actif tant qu'il y a des collecteurs + délai      |

`WhileSubscribed(5000)` est recommandé : garde le flow actif 5 secondes après que l'UI passe en background (évite de relancer lors de rotation rapide).

### 5.7 Collection dans Compose

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val state by viewModel.uiState.collectAsState()
    // L'UI se recompose automatiquement quand state change
}
```

Le `collectAsState()` convertit le `StateFlow` en `State<T>` Compose, permettant à l'UI de se recomposer automatiquement à chaque nouvelle émission.

---

## 6. ViewModel et Architecture

### 6.1 Qu'est-ce qu'un ViewModel ?

Le `ViewModel` est un composant d'architecture qui :

- **Survit aux changements de configuration** (rotation d'écran)
- **Sépare la logique métier de l'UI**
- **Gère l'état de l'écran** de manière centralisée
- **Expose des flux de données** observables par l'UI

### 6.2 Flux de données unidirectionnel

```
┌─────────────────────────────────────────┐
│              UI (Compose)               │
│  observe state    │    envoie events    │
└─────────────────────────────────────────┘
         ↑                    ↓
┌─────────────────────────────────────────┐
│              ViewModel                  │
│  - UiState (StateFlow)                  │
│  - Actions / Events                     │
│  - Logique métier                       │
└─────────────────────────────────────────┘
         ↑                    ↓
┌─────────────────────────────────────────┐
│           Repository / Data             │
└─────────────────────────────────────────┘
```

### 6.3 UI State

Définir un état immutable représentant l'écran :

```kotlin
data class CounterUiState(
    val count: Int = 0,
    val isLoading: Boolean = false
)
```

### 6.4 Création d'un ViewModel

```kotlin
class CounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    fun increment() {
        _uiState.update { currentState ->
            currentState.copy(count = currentState.count + 1)
        }
    }

    fun decrement() {
        _uiState.update { currentState ->
            currentState.copy(count = currentState.count - 1)
        }
    }

    fun reset() {
        _uiState.update { it.copy(count = 0) }
    }
}
```

### 6.5 Intégration avec Compose

```kotlin
@Composable
fun CounterScreen(
    viewModel: CounterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Compteur: ${uiState.count}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::decrement) {
                Text("-")
            }
            Button(onClick = viewModel::reset) {
                Text("Reset")
            }
            Button(onClick = viewModel::increment) {
                Text("+")
            }
        }
    }
}
```

### 6.6 Schéma récapitulatif du flux de données

```
┌──────────────────────────────────────────────────────────────────┐
│                           COMPOSE UI                             │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  val state by viewModel.uiState.collectAsState()            │  │
│  │                          │                                  │  │
│  │                          ▼                                  │  │
│  │              Text("Count: ${state.count}")                  │  │
│  │                                                             │  │
│  │              Button(onClick = viewModel::increment)         │  │
│  └────────────────────────────────────────────────────────────┘  │
│                             │                                    │
│                             │ onClick                            │
└─────────────────────────────┼────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                          VIEWMODEL                               │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  private val _uiState = MutableStateFlow(UiState())        │  │
│  │  val uiState: StateFlow<UiState> = _uiState.asStateFlow()  │  │
│  │                                                             │  │
│  │  fun increment() {                                          │  │
│  │      _uiState.update { it.copy(count = it.count + 1) }     │  │
│  │  }                   │                                      │  │
│  └──────────────────────┼─────────────────────────────────────┘  │
│                         │                                        │
│                         │ update                                 │
│                         ▼                                        │
│              ┌─────────────────────┐                             │
│              │  StateFlow émet     │──────▶ UI recompose         │
│              │  nouvelle valeur    │                             │
│              └─────────────────────┘                             │
└──────────────────────────────────────────────────────────────────┘
```

### 6.7 Exemple Complet : ViewModel Partagé (KMP)

C'est ici que StateFlow devient obligatoire (pas de `mutableStateOf` car c'est Compose-only).

#### Le ViewModel partagé

```kotlin
// shared/src/commonMain/kotlin/presentation/TodoViewModel.kt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// État de l'UI
data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TodoViewModel(
    private val repository: TodoRepository,
    private val scope: CoroutineScope  // Injecté par la plateforme
) {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    fun loadTodos() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val todos = repository.getTodos()
                _uiState.value = _uiState.value.copy(
                    todos = todos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addTodo(title: String) {
        scope.launch {
            val newTodo = Todo(title = title)
            _uiState.value = _uiState.value.copy(
                todos = _uiState.value.todos + newTodo
            )
        }
    }
}
```

#### 📱 Côté Android

```kotlin
// androidApp/src/main/kotlin/ui/TodoScreen.kt
import androidx.lifecycle.viewModelScope

// Wrapper Android pour le ViewModel partagé
class AndroidTodoViewModel : ViewModel() {
    private val repository = TodoRepository(ApiClient())
    val shared = TodoViewModel(repository, viewModelScope)
}

@Composable
fun TodoScreen(viewModel: AndroidTodoViewModel = viewModel()) {
    val uiState by viewModel.shared.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.shared.loadTodos()
    }

    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.error != null -> Text("Error: ${uiState.error}")
        else -> {
            LazyColumn {
                items(uiState.todos) { todo ->
                    Text(todo.title)
                }
            }
        }
    }
}
```

### 6.8 Gestion d'états multiples

```kotlin
data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val user = userRepository.getUser()
                val posts = userRepository.getPosts()

                _uiState.update {
                    it.copy(user = user, posts = posts, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun retry() = loadProfile()
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> LoadingIndicator()
        state.error != null -> ErrorMessage(state.error!!, onRetry = viewModel::retry)
        else -> ProfileContent(state.user, state.posts)
    }
}
```

---

## 7. Dépendances Gradle

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Coroutines (pour Flow)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
```

---

## 8. Ressources

- [Documentation officielle Kotlin](https://kotlinlang.org/docs/home.html)
- [Documentation Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Codelabs Android](https://developer.android.com/courses)
- [Material Design 3](https://m3.material.io/)

---

## Résumé du Jour 1

| Concept            | Description                                                   |
| ------------------ | ------------------------------------------------------------- |
| Kotlin             | Langage concis, sûr et fonctionnel                            |
| Composable         | Fonction annotée `@Composable` décrivant l'UI                 |
| Modifier           | Chaîne de transformations pour personnaliser les composables  |
| remember           | Préserve l'état local entre les recompositions                |
| State Hoisting     | Pattern séparant l'état de sa représentation                  |
| Flow               | Flux de données asynchrone émettant des valeurs séquentielles |
| StateFlow          | Flow avec état actuel, optimisé pour l'UI                     |
| MutableStateFlow   | StateFlow modifiable (privé dans le ViewModel)                |
| `.update {}`       | Mise à jour atomique et thread-safe du StateFlow              |
| ViewModel          | Composant gérant l'état et la logique métier de l'écran       |
| `collectAsState()` | Convertit un StateFlow en State Compose                       |

---

## Progression Logique

```
┌─────────────────────────────────────────────────────────────────┐
│  1. KOTLIN         Fondamentaux du langage                      │
│         ↓                                                       │
│  2. ANDROID        Contexte et architecture                     │
│         ↓                                                       │
│  3. COMPOSE        Construire des interfaces                    │
│         ↓                                                       │
│  4. ÉTAT LOCAL     remember, mutableStateOf                     │
│         ↓                                                       │
│  5. FLOW           Flux de données réactifs                     │
│         ↓                                                       │
│  6. VIEWMODEL      Architecture complète                        │
└─────────────────────────────────────────────────────────────────┘
```
