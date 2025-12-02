# 🌐 Sistema Social - Banana Launcher

## 📋 Descripción General

He implementado la estructura completa para un sistema social en Banana Launcher que incluye:

- ✅ **Gestión de amigos**: Agregar, aceptar/rechazar solicitudes
- ✅ **Estado en tiempo real**: Ver amigos online/offline
- ✅ **Sistema de mensajería**: Chat en tiempo real entre amigos
- ✅ **Actividad de juego**: Ver qué están jugando tus amigos
- ✅ **Interfaz completa**: Pantallas para amigos, solicitudes y mensajes

## 🏗️ Arquitectura Actual

### Frontend (Ya implementado ✅)

- `SocialManager.kt`: Gestor de toda la funcionalidad social
- `SocialScreen.kt`: UI completa con 3 tabs (Amigos, Solicitudes, Mensajes)
- Modelos de datos: User, Friend, Message, FriendRequest

### Backend (Necesita implementación 🔧)

Para que funcione completamente, necesitas implementar un backend. Aquí tienes 3 opciones:

---

## 🚀 Opción 1: Firebase (Recomendado para empezar)

### Ventajas
- ✅ Gratis hasta 10GB de transferencia/mes
- ✅ Tiempo real integrado
- ✅ Autenticación incluida
- ✅ Fácil de configurar

### Pasos de Implementación

#### 1. Agregar Firebase al proyecto

En `build.gradle.kts` (nivel proyecto):
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

En `build.gradle.kts` (nivel app):
```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // Coroutines para Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

#### 2. Configurar Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto
3. Agrega tu app Android
4. Descarga `google-services.json` y colócalo en `app/`
5. Habilita:
   - **Authentication** → Email/Password
   - **Firestore Database**
   - **Realtime Database** (para presencia online)

#### 3. Estructura de Firestore

```
users/
  {userId}/
    username: string
    avatarUrl: string
    currentGame: string
    createdAt: timestamp

friends/
  {userId}/
    friends/
      {friendId}/
        friendSince: timestamp
        isFavorite: boolean

friendRequests/
  {userId}/
    {requestId}/
      fromUserId: string
      timestamp: timestamp
      status: "pending" | "accepted" | "rejected"

messages/
  {chatId}/  // chatId = sorted([userId1, userId2]).join("_")
    {messageId}/
      senderId: string
      content: string
      timestamp: timestamp
      isRead: boolean

presence/
  {userId}/
    isOnline: boolean
    lastSeen: timestamp
    currentGame: string
```

#### 4. Implementar SocialManager con Firebase

```kotlin
class SocialManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user!!.uid
            
            // Obtener datos del usuario
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = User(
                id = userId,
                username = userDoc.getString("username") ?: "",
                avatarUrl = userDoc.getString("avatarUrl"),
                isOnline = true
            )
            
            // Actualizar presencia
            updatePresence(userId, true)
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user!!.uid
            
            // Crear documento de usuario
            val userData = hashMapOf(
                "username" to username,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(userId).set(userData).await()
            
            val user = User(id = userId, username = username, isOnline = true)
            _currentUser.value = user
            
            updatePresence(userId, true)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun updatePresence(userId: String, isOnline: Boolean) {
        val presenceRef = database.getReference("presence/$userId")
        
        if (isOnline) {
            // Configurar desconexión automática
            presenceRef.onDisconnect().setValue(
                mapOf(
                    "isOnline" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )
            
            // Marcar como online
            presenceRef.setValue(
                mapOf(
                    "isOnline" to true,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )
        } else {
            presenceRef.setValue(
                mapOf(
                    "isOnline" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )
        }
    }
    
    fun observeFriends(userId: String) {
        firestore.collection("friends").document(userId)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                scope.launch {
                    val friends = snapshot?.documents?.mapNotNull { doc ->
                        val friendId = doc.id
                        val friendDoc = firestore.collection("users").document(friendId).get().await()
                        
                        // Obtener estado de presencia
                        val presenceSnapshot = database.getReference("presence/$friendId").get().await()
                        
                        Friend(
                            user = User(
                                id = friendId,
                                username = friendDoc.getString("username") ?: "",
                                avatarUrl = friendDoc.getString("avatarUrl"),
                                isOnline = presenceSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                                currentGame = presenceSnapshot.child("currentGame").getValue(String::class.java)
                            ),
                            isFavorite = doc.getBoolean("isFavorite") ?: false,
                            friendSince = doc.getTimestamp("friendSince")?.toDate()?.time ?: 0
                        )
                    } ?: emptyList()
                    
                    _friends.value = friends
                }
            }
    }
}
```

---

## 🔥 Opción 2: Supabase (Backend como servicio moderno)

### Ventajas
- ✅ PostgreSQL (base de datos relacional)
- ✅ Tiempo real con WebSockets
- ✅ REST API automática
- ✅ Gratis hasta 500MB de BD

### Configuración

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
    implementation("io.ktor:ktor-client-android:2.3.7")
}

// Inicializar Supabase
val supabase = createSupabaseClient(
    supabaseUrl = "https://tu-proyecto.supabase.co",
    supabaseKey = "tu-anon-key"
) {
    install(Postgrest)
    install(Realtime)
}
```

---

## 🖥️ Opción 3: Servidor Propio (Máximo control)

### Stack sugerido
- **Backend**: Ktor (Kotlin) o Node.js con Express
- **Base de datos**: PostgreSQL o MongoDB
- **Tiempo real**: WebSockets o Socket.IO
- **Deploy**: Railway, Render, o DigitalOcean

### Ejemplo con Ktor

```kotlin
// Server.kt
fun Application.module() {
    install(WebSockets)
    install(ContentNegotiation) {
        json()
    }
    
    routing {
        // Autenticación
        post("/api/auth/register") { /* ... */ }
        post("/api/auth/login") { /* ... */ }
        
        // Amigos
        get("/api/friends") { /* ... */ }
        post("/api/friends/request") { /* ... */ }
        post("/api/friends/accept/{requestId}") { /* ... */ }
        
        // Mensajes
        webSocket("/api/messages/{userId}") {
            // Manejar mensajes en tiempo real
        }
    }
}
```

---

## 📱 Características Implementadas en la App

### 1. Gestión de Amigos
- Ver lista de amigos online/offline
- Enviar solicitudes de amistad
- Aceptar/rechazar solicitudes
- Ver qué juego está jugando cada amigo

### 2. Sistema de Mensajería
- Chat en tiempo real con cada amigo
- Notificación de mensajes no leídos
- Historial de conversaciones
- Estado "en línea" de cada amigo

### 3. Interfaz de Usuario
- 3 tabs: Amigos, Solicitudes, Mensajes
- Diseño moderno con Material 3
- Animaciones fluidas
- Indicadores visuales de estado

---

## 🎮 Integración con el Juego

La app actualiza automáticamente el juego actual cuando abres una ROM:

```kotlin
// En RomDetailScreen cuando se lanza un juego
socialManager.updateCurrentGame(rom.name)

// Tus amigos verán: "🎮 Jugando Super Mario Bros"
```

---

## 🔐 Seguridad

**Importante**: Implementa estas reglas en tu backend:

1. **Autenticación**: Solo usuarios logueados pueden acceder
2. **Autorización**: Los usuarios solo pueden ver/modificar sus propios datos
3. **Validación**: Valida todos los inputs en el servidor
4. **Rate limiting**: Previene spam de solicitudes

### Ejemplo de reglas de Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Solo usuarios autenticados
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Los usuarios solo pueden ver sus propios datos
    match /users/{userId} {
      allow read: if true;  // Los perfiles son públicos
      allow write: if request.auth.uid == userId;
    }
    
    match /friends/{userId}/{document=**} {
      allow read, write: if request.auth.uid == userId;
    }
    
    match /messages/{chatId}/{document=**} {
      allow read: if request.auth.uid in chatId.split('_');
      allow write: if request.auth.uid in chatId.split('_');
    }
  }
}
```

---

## 📊 Próximos Pasos

1. ✅ **Ya hecho**: Frontend completo con UI
2. 🔧 **Necesitas hacer**: Elegir backend (recomiendo Firebase para empezar)
3. 🔧 **Necesitas hacer**: Implementar autenticación
4. 🔧 **Necesitas hacer**: Conectar SocialManager con el backend elegido
5. 🔧 **Necesitas hacer**: Probar con múltiples usuarios

---

## 💡 Consejos

- **Empieza simple**: Implementa primero login y lista de amigos
- **Prueba local**: Usa el emulador de Firebase para desarrollo
- **Optimiza después**: No te preocupes por el rendimiento al inicio
- **Iteración**: Agrega características gradualmente

---

## 🆘 ¿Necesitas ayuda?

Si eliges Firebase y necesitas ayuda implementando, puedo ayudarte con:
1. Configuración del proyecto Firebase
2. Implementación de autenticación
3. Estructuración de la base de datos
4. Sistema de tiempo real para presencia

¡La estructura está lista, solo falta conectar el backend! 🚀

