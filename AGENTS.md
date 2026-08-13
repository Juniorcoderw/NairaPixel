# AGENTS.md — NairaPixel

## Proyecto
NairaPixel es un mod cliente modular para Minecraft Java 1.16.5 + Forge 36.2.34 + Pixelmon 9.1.13.

## Estado de módulos
- Scanner: estable.
- Naira Legend: estable.
- NairaDex: próximo módulo.
- NairaHunt: pendiente.
- NairaSight: pendiente.
- Naira Menu / HudManager: pendiente.

## Reglas generales
- Mantener el código simple, modular, escalable y fácil de depurar.
- No romper módulos estables al desarrollar uno nuevo.
- No modificar el JAR original de Pixelmon.
- No duplicar lectores o parsers de Pixelmon si la información puede centralizarse en una capa común.
- Toda información visible al jugador debe mostrarse en español.
- Los nombres ingleses pueden mostrarse entre paréntesis cuando ayuden a identificar movimientos, naturalezas o habilidades.
- No mostrar como real un dato supuesto, no sincronizado o no disponible en el cliente.
- Antes de dar una tarea por terminada, compilar o ejecutar la prueba correspondiente cuando sea posible.
- No incluir el JAR de Pixelmon, extracciones del JAR ni archivos de análisis local en Git.

## Regla de código
- Código limpio, compacto y legible.
- Evitar espacios y saltos de línea innecesarios sin sacrificar claridad.
- No agregar comentarios decorativos, repetitivos, obvios o explicaciones línea por línea.
- Usar comentarios solo cuando aporten contexto real: títulos breves de una sección importante, una limitación técnica, una decisión no obvia o una advertencia necesaria.
- No usar banners gigantes, bloques de asteriscos, emojis ni comentarios tipo tutorial dentro del código.
- Los nombres de clases, métodos y variables deben explicar por sí mismos la intención siempre que sea posible.
- No reducir código únicamente para tener menos líneas si eso perjudica legibilidad, estabilidad o funcionalidad.

## Arquitectura
Los módulos de interfaz no deben convertirse en fuentes de datos. La información común de Pixelmon debe tender a una capa central reutilizable.

Objetivo:

```text
PixelmonData / Core
        |
        +-- Scanner
        +-- Legend
        +-- Dex
        +-- Hunt
        +-- Sight
```

## Git y ramas
- `main` debe permanecer estable y compilable.
- Cada módulo grande se desarrolla en una rama `feature/...`.
- No iniciar otro módulo grande dentro de una rama no terminada.
- Flujo: Diseñar -> Implementar -> Probar -> Validar -> Commit -> Merge.

## Regla de validación
Dato leído -> dato validado -> dato mostrado.
