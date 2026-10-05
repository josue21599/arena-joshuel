# Arena Duel — juego de cartas y torres 1v1 contra la CPU (Android)

## Stack fijo (no cambiar sin preguntar)
- Kotlin, Android nativo, Gradle Kotlin DSL (proyecto creado con el wizard de Android Studio).
- Renderizado 2D con `SurfaceView` + `Canvas`. Sin motores ni librerías externas de juego.
- Sin Compose, sin red, sin base de datos. Todo local, partida contra la CPU.
- Gráficos con formas y colores dibujados en Canvas (sin assets de juegos existentes).
- Orientación vertical, pantalla completa.

## Estructura de paquetes
- `engine/`  → GameView, GameLoop (timestep fijo 60 UPS), entrada táctil
- `model/`   → GameState, Tower, Unit, Projectile, Team, Lane
- `cards/`   → CardDef (datos), Deck, Hand, ElixirBar
- `ai/`      → CpuPlayer
- `render/`  → Renderer (dibuja estado, no contiene lógica)
- `ui/`      → MainActivity, MenuActivity, pantallas de resultado

## Reglas de trabajo (ahorro de tokens)
- NO leer ni listar: `build/`, `.gradle/`, `.idea/`, `app/build/`, `*.apk`.
- Leer solo los archivos necesarios para la tarea actual.
- Editar archivos existentes con cambios mínimos; no reescribir archivos completos si no hace falta.
- No crear tests, documentación ni README salvo que se pida.
- Tras cada tarea: compilar con `./gradlew assembleDebug -q` y corregir errores hasta que compile.
- Respuesta final: resumen de máximo 5 líneas. Sin explicar el código.

## Diseño del juego
- Arena: 2 carriles, río central con 2 puentes. Cada bando: 2 torres laterales + 1 torre rey.
- Elixir: 0–10, +1 cada 2,8 s (doble en el último minuto).
- Mazo de 8 cartas, mano de 4, se repone en ciclo.
- Partida de 3 min. Gana quien destruya la torre rey o tenga más torres al final.
- Las cartas se definen como datos en `cards/CardDefs.kt` (coste, vida, daño, velocidad, alcance, objetivo).

## Estado actual
(Actualizar al final de cada fase en una línea)
- Fase 0: proyecto creado.
- Fase 1: GameView/GameLoop (60 UPS), GameState con 6 torres, Renderer de arena y MainActivity a pantalla completa (compila).
- Fase 2: Unit, Projectile, Targetable; movimiento por carril/puente, ataque melee/distancia, torres disparan, muerte (compila).
- Fase 3: cards/ (CardDef, CardDefs con 8 cartas, Deck con mano de 4 en ciclo, ElixirBar); HUD inferior con elixir y 4 cartas con coste; seleccionar carta + tocar mi mitad despliega; spawn de prueba eliminado (compila).
- Fase 4: ai/CpuPlayer (mazo y elixir propios, defensa con carta útil más barata, ataque por carril de torre más dañada, retardo aleatorio, dificultad EASY/MEDIUM/HARD, MEDIUM por defecto sin selector aún); GameState.spawn/cpu (compila).
- Fase 5: temporizador 3 min, elixir x2 último minuto, coronas, victoria/derrota/empate, MenuActivity (Jugar + dificultad), ResultActivity (revancha), pausa en onPause (compila).
- Fase 6: números de daño, destello al golpe, explosión de torre destruida; balance de cartas (Duendes/Esqueletos/Mini/Príncipe/Mosquetera nerfeados, Caballero/Arquera ajustados); APK debug generada (compila).
- Pendiente: pulido (no definido).
