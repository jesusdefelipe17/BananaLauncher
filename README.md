# 🎮 EmuLauncher - Launcher de Emuladores y ROMs

Un launcher moderno para Android que centraliza todos tus emuladores y ROMs con un diseño elegante inspirado en PS5 y Nintendo Switch.

## ✨ Características

### 🎯 Funcionalidades Principales

- **Escáner de Emuladores**: Detecta automáticamente emuladores instalados (RetroArch, PPSSPP, Dolphin, DraStic, etc.)
- **Biblioteca de ROMs**: Escanea múltiples carpetas en busca de ROMs con soporte para múltiples formatos
- **Sistema de Favoritos**: Marca tus juegos favoritos con persistencia usando SharedPreferences
- **Vista en Grid**: Diseño tipo galería con tarjetas grandes y atractivas
- **Pantalla de Detalles**: Información completa de cada ROM con portadas y datos del archivo
- **Búsqueda y Filtros**: Busca por nombre o plataforma, filtra por favoritos

### 🎨 Diseño Moderno

- Tema oscuro elegante con colores tipo PS5
- Animaciones fluidas (fade, scale, slide)
- Material Design 3
- Tarjetas con sombras y elevación dinámica
- Efectos hover en las tarjetas
- Transiciones suaves entre pantallas
- Iconos de plataforma personalizados con colores únicos

### 🕹️ Plataformas Soportadas

- Game Boy Advance (GBA)
- Nintendo NES
- Super Nintendo (SNES)
- Nintendo 64
- Nintendo DS
- PlayStation (PSX)
- PlayStation Portable (PSP)
- Game Boy / Game Boy Color
- Sega Genesis / Mega Drive
- Arcade

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/afo/
│
├── MainActivity.kt                    # Actividad principal con navegación
├── AppScanner.kt                     # Escaneo de emuladores instalados
├── RomScanner.kt                     # Escaneo de ROMs en el almacenamiento
├── FavoritesManager.kt               # Gestión de favoritos
│
├── models/
│   ├── Platform.kt                   # Enum de plataformas con colores
│   ├── EmulatorApp.kt               # Modelo de emulador
│   └── RomFile.kt                   # Modelo de ROM
│
└── ui/
    ├── theme/
    │   ├── Theme.kt                 # Tema Material 3 personalizado
    │   ├── Color.kt                 # Paleta de colores
    │   └── Type.kt                  # Tipografía
    │
    └── screens/
        ├── Components.kt            # Componentes reutilizables
        ├── EmulatorListScreen.kt   # Pantalla de emuladores
        ├── RomGridScreen.kt        # Pantalla de biblioteca de ROMs
        └── RomDetailScreen.kt      # Pantalla de detalles de ROM
```

## 🚀 Instalación

### Requisitos

- Android Studio Hedgehog o superior
- Kotlin 1.9+
- API mínima: 26 (Android 8.0)
- API objetivo: 34 (Android 14)

### Pasos

1. **Clonar o abrir el proyecto** en Android Studio
2. **Sincronizar Gradle**: El proyecto usa Compose BOM para gestionar dependencias
3. **Compilar**: Build > Make Project
4. **Ejecutar**: Run > Run 'app'

## 📱 Configuración y Uso

### Permisos

La aplicación solicitará permisos de almacenamiento al iniciar:
- Android 10 y anteriores: READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
- Android 11+: MANAGE_EXTERNAL_STORAGE

### Carpetas de ROMs

La app escanea automáticamente estas ubicaciones:

```
/storage/emulated/0/Download/roms
/storage/emulated/0/roms
/storage/emulated/0/Games
/storage/emulated/0/ROMs
/storage/emulated/0/Emuladores
/storage/emulated/0/RetroArch/roms
```

### Portadas de Juegos

Para que aparezcan las portadas, coloca imágenes con el mismo nombre que el ROM:

```
/roms/Pokemon Emerald.gba
/roms/Pokemon Emerald.png    ← Se detectará automáticamente
```

O en una subcarpeta `covers`:

```
/roms/covers/Pokemon Emerald.png
```

Formatos soportados: PNG, JPG, JPEG, WEBP

## 🎨 Paleta de Colores

```kotlin
DarkBackground     = #0A0E27  // Fondo principal
DarkSurface        = #151B3D  // Superficies
CardBackground     = #1A2142  // Tarjetas
PrimaryBlue        = #4A90E2  // Azul principal
AccentPurple       = #9B4DFF  // Púrpura acento
FavoriteGold       = #FFB300  // Dorado para favoritos
TextPrimary        = #FFFFFF  // Texto principal
TextSecondary      = #B0B8D4  // Texto secundario
```

## 🔧 Características Técnicas

### Animaciones

- **Scale Animation**: Efecto de presión en botones y tarjetas
- **Fade In/Out**: Transiciones suaves al cambiar de pantalla
- **Slide Transitions**: Deslizamiento al entrar a detalles de ROM
- **Spring Physics**: Animaciones con efecto rebote natural
- **Elevation Animation**: Sombras dinámicas al hacer hover

### Optimizaciones

- **LazyVerticalGrid**: Renderizado eficiente de listas grandes
- **remember**: Evita recomposiciones innecesarias
- **derivedStateOf**: Cálculos optimizados para filtros
- **Coil/BitmapFactory**: Carga eficiente de imágenes

## 📝 Extensiones Soportadas por Plataforma

| Plataforma | Extensiones |
|------------|-------------|
| GBA | .gba |
| NES | .nes |
| SNES | .smc, .sfc, .snes |
| N64 | .n64, .z64, .v64 |
| NDS | .nds |
| PSX | .bin, .cue, .img, .iso, .pbp |
| PSP | .iso, .cso |
| GB | .gb |
| GBC | .gbc |
| Genesis | .md, .gen, .smd |
| Arcade | .zip, .7z |

## 🐛 Solución de Problemas

### No aparecen ROMs

1. Verificar permisos de almacenamiento
2. Revisar que los archivos estén en las carpetas correctas
3. Verificar que las extensiones sean soportadas

### No aparecen portadas

1. Verificar que la imagen tenga exactamente el mismo nombre que la ROM
2. Usar formatos PNG, JPG o WEBP
3. Colocar en la misma carpeta o en subcarpeta `covers/`

### Emuladores no detectados

1. Verificar que el emulador esté instalado
2. La app detecta los paquetes más comunes
3. Puedes añadir más emuladores en `AppScanner.kt`

## 🎯 Próximas Características (Roadmap)

- [ ] Asociación automática ROM → Emulador
- [ ] Descarga de portadas desde API (TheGamesDB, IGDB)
- [ ] Historial de juegos jugados recientemente
- [ ] Tiempo de juego por ROM
- [ ] Categorías personalizadas
- [ ] Temas personalizables
- [ ] Widget de inicio
- [ ] Atajos de aplicación

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Puedes usarlo, modificarlo y distribuirlo libremente.

## 🤝 Contribuciones

¡Las contribuciones son bienvenidas! Si encuentras bugs o quieres añadir features:

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 👨‍💻 Desarrollado con

- Kotlin
- Jetpack Compose
- Material Design 3
- Android Studio

---

**Disfruta de tu biblioteca de juegos retro! 🎮✨**

