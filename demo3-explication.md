# Demo 3 - Fichiers, Médias, PDF & Sécurité

## Objectif

Cette démonstration montre comment :

1. **Gérer les fichiers** (téléchargement, création, lecture)
2. **Accéder aux médias** (caméra, galerie photos)
3. **Créer et visualiser des PDF**
4. **Empêcher la capture d'écran**

---

## Architecture

```
App4/
├── features/
│   ├── files/
│   │   ├── FileHelper.kt         ← Téléchargement et gestion fichiers
│   │   └── FileScreen.kt         ← Interface utilisateur
│   ├── media/
│   │   ├── MediaHelper.kt        ← Caméra et galerie
│   │   └── MediaScreen.kt        ← Interface utilisateur
│   ├── pdf/
│   │   ├── PdfHelper.kt          ← Création et rendu PDF
│   │   └── PdfScreen.kt          ← Interface utilisateur
│   └── security/
│       ├── SecurityHelper.kt     ← Protection capture écran
│       └── SecurityScreen.kt     ← Interface utilisateur
├── ui/screens/
│   └── MainScreen.kt             ← Navigation par onglets
└── MainActivity.kt
```

---

## 1. Gestion des Fichiers

### Permissions requises

```xml
<!-- AndroidManifest.xml -->

<!-- Lecture fichiers (Android < 13) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Écriture fichiers (Android < 10) -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />

<!-- Lecture médias (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Internet pour téléchargement -->
<uses-permission android:name="android.permission.INTERNET" />
```

### Évolution des permissions stockage

| Android        | Comportement                              |
| -------------- | ----------------------------------------- |
| < 10 (Q)       | `READ/WRITE_EXTERNAL_STORAGE` pour tout   |
| 10-12          | Scoped Storage, `MediaStore` pour médias  |
| 13+ (Tiramisu) | Permissions granulaires par type de média |

### Télécharger un fichier

```kotlin
suspend fun downloadFile(
    url: String,
    fileName: String,
    onProgress: (Int) -> Unit
): Result<Uri> = withContext(Dispatchers.IO) {
    try {
        // Connexion HTTP
        val connection = URL(url).openConnection()
        connection.connect()
        val totalSize = connection.contentLength
        val inputStream = connection.getInputStream()

        // Créer le fichier destination
        val uri = createDownloadUri(fileName)

        uri?.let { destinationUri ->
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    // Calculer et reporter la progression
                    if (totalSize > 0) {
                        val progress = ((totalBytesRead * 100) / totalSize).toInt()
                        onProgress(progress)
                    }
                }
            }
            Result.success(destinationUri)
        } ?: Result.failure(Exception("Impossible de créer le fichier"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Créer un fichier dans Downloads (Android 10+)

```kotlin
private fun createDownloadUri(fileName: String): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Scoped Storage : utiliser MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, getMimeType(fileName))
            put(MediaStore.Downloads.IS_PENDING, 1)  // Fichier en cours d'écriture
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        // Après écriture, marquer comme terminé
        uri?.let {
            val updateValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            context.contentResolver.update(it, updateValues, null, null)
        }

        uri
    } else {
        // Ancienne méthode : accès direct au système de fichiers
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        Uri.fromFile(File(downloadsDir, fileName))
    }
}
```

### Lire un fichier texte

```kotlin
fun readTextFile(uri: Uri): Result<String> {
    return try {
        val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().readText()
        }
        content?.let { Result.success(it) }
            ?: Result.failure(Exception("Impossible de lire le fichier"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Ouvrir le sélecteur de fichiers

```kotlin
// Dans le Composable
val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    uri?.let {
        // Traiter le fichier sélectionné
        val content = fileHelper.readTextFile(it)
    }
}

// Lancer le sélecteur
Button(onClick = {
    filePickerLauncher.launch(arrayOf("*/*"))  // Tous types
    // ou arrayOf("text/*") pour texte uniquement
    // ou arrayOf("application/pdf") pour PDF uniquement
})
```

---

## 2. Caméra et Galerie

### Permissions requises

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

<!-- Android 13+ pour images -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### FileProvider pour la caméra

La caméra a besoin d'une URI pour sauvegarder la photo. On utilise `FileProvider` :

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

```xml
<!-- res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="external" path="." />
    <external-files-path name="external_files" path="." />
    <cache-path name="cache" path="." />
    <files-path name="files" path="." />
</paths>
```

### Créer une URI pour la photo

```kotlin
fun createImageUri(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ : MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${timestamp}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    } else {
        // Ancienne méthode : FileProvider
        val imageFile = File.createTempFile(
            "IMG_${timestamp}_",
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
}
```

### Prendre une photo

```kotlin
// Stocker l'URI temporairement
var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success: Boolean ->
    if (success) {
        // La photo est sauvegardée dans tempCameraUri
        capturedImageUri = tempCameraUri
    }
}

// Lancer la caméra
Button(onClick = {
    tempCameraUri = mediaHelper.createImageUri()
    tempCameraUri?.let { uri ->
        cameraLauncher.launch(uri)
    }
})
```

### Sélectionner depuis la galerie

```kotlin
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    selectedImageUri = uri
}

// Lancer la galerie
Button(onClick = {
    galleryLauncher.launch("image/*")
})
```

### Afficher les photos récentes

```kotlin
fun getRecentImages(limit: Int = 20): List<MediaItem> {
    val images = mutableListOf<MediaItem>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.SIZE
    )

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"  // Plus récent d'abord
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

        while (cursor.moveToNext() && images.size < limit) {
            val id = cursor.getLong(idColumn)
            val uri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )
            images.add(MediaItem(uri, ...))
        }
    }

    return images
}
```

### Afficher une image avec Coil

```kotlin
// Dépendance
implementation("io.coil-kt:coil-compose:2.5.0")
```

```kotlin
AsyncImage(
    model = imageUri,
    contentDescription = "Image",
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .clip(RoundedCornerShape(8.dp)),
    contentScale = ContentScale.Crop
)
```

---

## 3. Création et Visualisation PDF

### Créer un PDF avec PdfDocument

```kotlin
fun createSamplePdf(title: String, content: String): Result<Uri> {
    val pdfDocument = PdfDocument()

    // Créer une page A4 (595 x 842 points)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Définir les styles
    val titlePaint = Paint().apply {
        color = Color.parseColor("#673AB7")
        textSize = 24f
        isFakeBoldText = true
    }

    val contentPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
    }

    // Dessiner le contenu
    canvas.drawText(title, 50f, 80f, titlePaint)

    // Gérer le retour à la ligne
    var yPosition = 120f
    content.split("\n").forEach { line ->
        canvas.drawText(line, 50f, yPosition, contentPaint)
        yPosition += 20f
    }

    pdfDocument.finishPage(page)

    // Sauvegarder
    val uri = savePdfToDownloads(pdfDocument, "document.pdf")
    pdfDocument.close()

    return uri?.let { Result.success(it) }
        ?: Result.failure(Exception("Erreur de sauvegarde"))
}
```

### Visualiser un PDF avec PdfRenderer

```kotlin
fun renderPdfPage(uri: Uri, pageIndex: Int = 0): Bitmap? {
    return try {
        // Ouvrir le fichier PDF
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")

        fileDescriptor?.let { fd ->
            val renderer = PdfRenderer(fd)

            if (pageIndex < renderer.pageCount) {
                val page = renderer.openPage(pageIndex)

                // Créer un bitmap pour le rendu (x2 pour meilleure qualité)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(Color.WHITE)  // Fond blanc

                // Rendre la page
                page.render(
                    bitmap,
                    null,  // Clip rect (null = toute la page)
                    null,  // Transform matrix
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                page.close()
                renderer.close()
                bitmap
            } else {
                renderer.close()
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
```

### Obtenir le nombre de pages

```kotlin
fun getPdfPageCount(uri: Uri): Int {
    return try {
        val fd = context.contentResolver.openFileDescriptor(uri, "r")
        fd?.let {
            val renderer = PdfRenderer(it)
            val count = renderer.pageCount
            renderer.close()
            count
        } ?: 0
    } catch (e: Exception) {
        0
    }
}
```

### Navigation entre pages

```kotlin
@Composable
fun PdfViewer(uri: Uri) {
    val pdfHelper = remember { PdfHelper(context) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        totalPages = pdfHelper.getPdfPageCount(uri)
        bitmap = pdfHelper.renderPdfPage(uri, 0)
    }

    Column {
        // Affichage
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "PDF Page"
            )
        }

        // Navigation
        Row {
            Button(
                onClick = {
                    if (currentPage > 0) {
                        currentPage--
                        bitmap = pdfHelper.renderPdfPage(uri, currentPage)
                    }
                },
                enabled = currentPage > 0
            ) {
                Text("Précédent")
            }

            Text("Page ${currentPage + 1} / $totalPages")

            Button(
                onClick = {
                    if (currentPage < totalPages - 1) {
                        currentPage++
                        bitmap = pdfHelper.renderPdfPage(uri, currentPage)
                    }
                },
                enabled = currentPage < totalPages - 1
            ) {
                Text("Suivant")
            }
        }
    }
}
```

---

## 4. Protection Capture d'Écran

### Le flag FLAG_SECURE

```kotlin
object SecurityHelper {

    fun enableScreenCaptureProtection(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disableScreenCaptureProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun isScreenCaptureProtected(activity: Activity): Boolean {
        return (activity.window.attributes.flags and
                WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
}
```

### Utilisation dans Compose

```kotlin
@Composable
fun SecurityScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    var isProtected by remember {
        mutableStateOf(
            activity?.let { SecurityHelper.isScreenCaptureProtected(it) } ?: false
        )
    }

    Button(onClick = {
        activity?.let {
            if (isProtected) {
                SecurityHelper.disableScreenCaptureProtection(it)
            } else {
                SecurityHelper.enableScreenCaptureProtection(it)
            }
            isProtected = !isProtected
        }
    }) {
        Text(if (isProtected) "Désactiver" else "Activer")
    }
}
```

### Ce que FLAG_SECURE bloque

| Action                        | Bloqué ?            |
| ----------------------------- | ------------------- |
| Capture d'écran (screenshot)  | ✅ Oui              |
| Enregistrement d'écran        | ✅ Oui              |
| Aperçu dans Recent Apps       | ✅ Oui (écran noir) |
| Partage d'écran (Cast)        | ✅ Oui              |
| Assistants (Google Assistant) | ✅ Oui              |

### Activation au démarrage

Pour protéger toute l'app dès le lancement :

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activer la protection avant setContent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            // ...
        }
    }
}
```

### Cas d'usage

- 🏦 Applications bancaires
- 🔐 Gestionnaires de mots de passe
- 💳 Affichage de données de paiement
- 📋 Documents confidentiels
- 🎬 Contenu protégé par DRM

---

## Permissions - Résumé

```xml
<!-- AndroidManifest.xml -->

<!-- Fichiers -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />

<!-- Médias Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- Caméra -->
<uses-permission android:name="android.permission.CAMERA" />
```

---

## Bonnes pratiques

### Fichiers

- ✅ Utiliser `MediaStore` pour Android 10+
- ✅ Gérer les erreurs réseau (timeout, pas de connexion)
- ✅ Afficher la progression pour les gros fichiers
- ✅ Vérifier l'espace disponible avant téléchargement

### Médias

- ✅ Vérifier la disponibilité de la caméra
- ✅ Gérer le cas où l'utilisateur refuse la permission
- ✅ Compresser les images si nécessaire
- ✅ Utiliser des miniatures pour les listes

### PDF

- ✅ Libérer les ressources (`close()`) après utilisation
- ✅ Rendre en background pour ne pas bloquer l'UI
- ✅ Gérer les PDF corrompus ou protégés

### Sécurité

- ✅ Activer `FLAG_SECURE` pour les données sensibles
- ✅ Informer l'utilisateur que la capture est bloquée
- ✅ Ne pas compter uniquement sur `FLAG_SECURE` (pas infaillible)

---

## Test sur émulateur

### Fichiers

- Le téléchargement fonctionne normalement
- Vérifiez le dossier Downloads dans l'app Files

### Caméra

- L'émulateur simule une caméra avec une scène animée
- Utilisez Extended Controls → Camera pour changer la source

### PDF

- Créez un PDF puis ouvrez-le avec le viewer intégré
- Testez avec des PDF externes via le file picker

### Capture d'écran

1. Activez la protection
2. Appuyez sur Power + Volume Down
3. La capture sera un écran noir

---

## Résumé des APIs

```
┌─────────────────────────────────────────────────────────────┐
│                    FICHIERS                                 │
│                                                             │
│  MediaStore.Downloads    ──▶  Dossier Downloads             │
│  ContentResolver         ──▶  Lecture/Écriture via URI      │
│  OpenDocument            ──▶  Sélecteur de fichiers         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    MÉDIAS                                   │
│                                                             │
│  TakePicture             ──▶  Capture photo                 │
│  GetContent              ──▶  Sélection galerie             │
│  MediaStore.Images       ──▶  Accès photos existantes       │
│  FileProvider            ──▶  Partage URI sécurisé          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    PDF                                      │
│                                                             │
│  PdfDocument             ──▶  Création de PDF               │
│  PdfRenderer             ──▶  Rendu de PDF existant         │
│  Canvas                  ──▶  Dessin sur page PDF           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    SÉCURITÉ                                 │
│                                                             │
│  FLAG_SECURE             ──▶  Bloque capture écran          │
│  WindowManager           ──▶  Gestion flags fenêtre         │
└─────────────────────────────────────────────────────────────┘
```

---

## Pour aller plus loin

- **DownloadManager** : Téléchargements en arrière-plan avec notifications
- **WorkManager** : Uploads/Downloads même app fermée
- **CameraX** : API caméra moderne avec preview dans Compose
- **iText/Apache PDFBox** : Bibliothèques PDF avancées
- **ExoPlayer** : Lecture vidéo avec protection DRM
