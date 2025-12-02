# 🎨 Guía de Iconos de Plataforma para EmuLauncher

## Iconos Vectoriales Personalizados

EmuLauncher utiliza iconos circulares con colores distintivos para cada plataforma de videojuegos. Actualmente, los iconos se generan con texto (por ejemplo, "GBA", "PSX", etc.), pero puedes reemplazarlos con iconos vectoriales personalizados.

## Colores por Plataforma

```kotlin
GBA      → Púrpura  (#6B4C9A)
NES      → Rojo     (#E60012)
SNES     → Gris     (#8F8F8F)
N64      → Azul     (#0E4C92)
NDS      → Rojo DS  (#D12228)
PSX      → Azul PS  (#003791)
PSP      → Negro    (#000000)
GB       → Verde GB (#8BAC0F)
GBC      → Púrpura  (#9B30FF)
Genesis  → Azul     (#0089CF)
Arcade   → Naranja  (#FF6B00)
```

## Ubicación de Iconos Vectoriales

### Opción 1: Drawable XML (Recomendado)

Crear archivos XML en: `app/src/main/res/drawable/`

Ejemplo: `ic_platform_gba.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#6B4C9A"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z"/>
    <path
        android:fillColor="#6B4C9A"
        android:pathData="M9,8h2v8H9zM13,8h2v8h-2z"/>
</vector>
```

### Opción 2: Material Icons Extendidos

Añadir al `build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
}
```

Usar en Compose:

```kotlin
Icon(
    imageVector = Icons.Filled.VideogameAsset,
    contentDescription = "GBA"
)
```

## Iconos Sugeridos por Plataforma

### Game Boy Advance (GBA)
- **Símbolo**: Consola portátil rectangular
- **Color**: Púrpura (#6B4C9A)
- **Alternativa Material Icon**: `Icons.Filled.VideogameAsset`

### Nintendo NES
- **Símbolo**: Control NES con cruz direccional
- **Color**: Rojo (#E60012)
- **Alternativa Material Icon**: `Icons.Filled.Gamepad`

### Super Nintendo (SNES)
- **Símbolo**: Control SNES con botones de colores
- **Color**: Gris (#8F8F8F)
- **Alternativa Material Icon**: `Icons.Filled.SportsEsports`

### Nintendo 64
- **Símbolo**: Logo "N64" o control tridente
- **Color**: Azul (#0E4C92)
- **Alternativa Material Icon**: `Icons.Filled.SportsEsports`

### Nintendo DS
- **Símbolo**: Doble pantalla plegable
- **Color**: Rojo (#D12228)
- **Alternativa Material Icon**: `Icons.Filled.PhoneAndroid`

### PlayStation (PSX)
- **Símbolo**: Botones △○✕☐
- **Color**: Azul (#003791)
- **Alternativa Material Icon**: `Icons.Filled.SportsEsports`

### PlayStation Portable (PSP)
- **Símbolo**: Consola portátil horizontal
- **Color**: Negro (#000000)
- **Alternativa Material Icon**: `Icons.Filled.TabletAndroid`

### Game Boy / Game Boy Color
- **Símbolo**: Consola portátil vertical clásica
- **Color**: Verde/Púrpura (#8BAC0F/#9B30FF)
- **Alternativa Material Icon**: `Icons.Filled.VideogameAsset`

### Sega Genesis / Mega Drive
- **Símbolo**: Logo "MD" o control de 3 botones
- **Color**: Azul (#0089CF)
- **Alternativa Material Icon**: `Icons.Filled.Gamepad`

### Arcade
- **Símbolo**: Máquina arcade o joystick
- **Color**: Naranja (#FF6B00)
- **Alternativa Material Icon**: `Icons.Filled.Casino`

## Cómo Implementar Iconos Personalizados

### 1. Crear los archivos SVG/XML

Usa herramientas como:
- **Android Studio**: Vector Asset Studio
- **Figma**: Exportar como SVG → Convertir en Android Studio
- **SVG to Android XML**: https://svg2vector.com/

### 2. Modificar el componente PlatformIcon

En `ui/screens/Components.kt`:

```kotlin
@Composable
fun PlatformIcon(
    platform: Platform,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(6.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        platform.color,
                        platform.color.copy(alpha = 0.7f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Opción 1: Icono personalizado desde drawable
        Icon(
            painter = painterResource(id = when (platform) {
                Platform.GBA -> R.drawable.ic_platform_gba
                Platform.NES -> R.drawable.ic_platform_nes
                Platform.SNES -> R.drawable.ic_platform_snes
                Platform.PSX -> R.drawable.ic_platform_psx
                // ... más plataformas
                else -> R.drawable.ic_platform_default
            }),
            contentDescription = platform.displayName,
            tint = Color.White,
            modifier = Modifier.size((size * 0.6f).dp)
        )
        
        // Opción 2: Material Icon
        /*
        Icon(
            imageVector = when (platform) {
                Platform.GBA -> Icons.Filled.VideogameAsset
                Platform.PSX -> Icons.Filled.SportsEsports
                // ... más
                else -> Icons.Filled.QuestionMark
            },
            contentDescription = platform.displayName,
            tint = Color.White,
            modifier = Modifier.size((size * 0.6f).dp)
        )
        */
    }
}
```

### 3. Añadir imports necesarios

```kotlin
import androidx.compose.ui.res.painterResource
import com.example.afo.R
```

## Recursos Gratuitos de Iconos

### Sitios Web Recomendados

1. **Material Design Icons**: https://fonts.google.com/icons
2. **Flaticon**: https://www.flaticon.com/ (Gaming icons)
3. **Icons8**: https://icons8.com/icons/set/gaming
4. **Game Icons**: https://game-icons.net/
5. **Font Awesome**: https://fontawesome.com/icons/gamepad

### Packs de Iconos de Consolas

- **RetroArch Icons**: https://github.com/libretro/retroarch-assets
- **EmulationStation Themes**: Contienen iconos de consolas
- **Pegasus Frontend**: https://github.com/mmatyas/pegasus-frontend

## Ejemplos de Implementación Completa

### ic_platform_gba.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M5,9v6h2v-2h10v2h2v-6h-2v2H7V9H5z M9,11h2v2H9V11z M13,11h2v2h-2V11z"/>
</vector>
```

### ic_platform_psx.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <!-- Triángulo -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,3l-3,5h6z"/>
    <!-- Círculo -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M18,12m-2.5,0a2.5,2.5 0,1 1,5 0a2.5,2.5 0,1 1,-5 0"/>
    <!-- X -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M10,18l-2,2 2,2 2,-2z"/>
    <!-- Cuadrado -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M3,10h5v5H3z"/>
</vector>
```

## Tips de Diseño

1. **Mantén la simplicidad**: Iconos reconocibles a 40dp
2. **Usa el color de plataforma**: Ya definido en el enum Platform
3. **Contraste con el fondo**: Usa blanco (#FFFFFF) para los paths
4. **Tamaño consistente**: Todos los iconos deben verse bien a la misma escala
5. **Test en diferentes tamaños**: 24dp (pequeño), 40dp (normal), 80dp (grande)

## Implementación Rápida (Sin crear archivos XML)

Si prefieres no crear archivos XML, puedes usar el sistema actual con emojis Unicode:

```kotlin
Text(
    text = when (platform) {
        Platform.GBA -> "🎮"
        Platform.NES -> "🕹️"
        Platform.PSX -> "🎯"
        Platform.PSP -> "📱"
        Platform.N64 -> "🎲"
        else -> "🎮"
    },
    fontSize = (size / 2).sp
)
```

---

**¡Personaliza los iconos según tu estilo! 🎨✨**

