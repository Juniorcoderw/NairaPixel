# AGENTS.md — NairaPixel

## Objetivo actual
Trabajar únicamente en **NairaPixel Scanner v1** para Minecraft 1.16.5 + Pixelmon 9.1.13.

## Reglas
- Mantener el código simple, modular y fácil de depurar.
- No modificar el JAR original de Pixelmon.
- No agregar funciones fuera del alcance del Scanner v1 sin indicación explícita.
- Toda información visible al jugador debe mostrarse en español.
- Naturalezas y habilidades pueden mostrar el nombre inglés entre paréntesis cuando sea útil.
- Validar los datos reales antes de construir el HUD definitivo.
- No suponer IV, naturaleza, habilidad u otros datos si el cliente no los conoce realmente.
- Evitar dependencias innecesarias; Jade y PixelmonInformation son solo referencias por ahora.
- No incluir `Pixelmon-1.16.5-9.1.13-universal.jar` en Git.
- Antes de dar una tarea por terminada, compilar o ejecutar la prueba correspondiente cuando sea posible.

## Flujo
Diseñar -> Implementar -> Probar -> Validar -> Commit.
