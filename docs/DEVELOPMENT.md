# Desarrollo de NairaPixel

## Entorno objetivo
- Minecraft Java 1.16.5
- Forge 36.2.34
- Pixelmon 9.1.13 Universal
- Java 8
- Gradle Wrapper del proyecto

## Flujo de trabajo
1. Diseñar el cambio.
2. Implementarlo en la rama correspondiente.
3. Compilar.
4. Probar en juego cuando aplique.
5. Validar el comportamiento real.
6. Hacer commit.
7. Fusionar a `main` solo cuando esté estable.

## Ramas
- `main`: estable y compilable.
- Módulos grandes: `feature/<nombre>`.
- Correcciones puntuales: `fix/<nombre>` cuando sea necesario.

Ejemplos:
- `feature/naira-dex`
- `feature/naira-hunt`
- `feature/naira-sight`
- `feature/naira-menu`

## Regla de código
- Código limpio, compacto y legible.
- Sin comentarios decorativos ni explicaciones obvias.
- Comentarios solo para títulos breves útiles, limitaciones técnicas, decisiones no evidentes o advertencias necesarias.
- Sin banners, emojis ni bloques de comentarios tipo tutorial.
- Preferir nombres claros de clases, métodos y variables.
- No reducir líneas si eso perjudica claridad o estabilidad.
- Evitar duplicar lógica existente.

## Validación
Regla general:

`Dato leído -> dato validado -> dato mostrado.`

No mostrar como real información que el cliente no conoce o que no se haya validado.

## Compilación rápida
```powershell
cd C:\Proyectos\NairaPixel
.\gradlew compileJava
```

## Build estable
```powershell
.\gradlew clean build
```

## Git básico
Antes de trabajar:
```powershell
git status
git pull --ff-only origin main
```

Antes de hacer commit:
```powershell
git status
.\gradlew compileJava
```

## Archivos locales que no deben subirse
- JAR original de Pixelmon.
- Extracciones del JAR.
- Carpetas de análisis temporal.
- Logs.
- Builds generados.
