# Roadmap de NairaPixel

## Base actual
- Scanner: estable.
- Naira Legend: estable.
- Arquitectura modular: definida.
- Reglas de código y Git: definidas.

## Fase 1 — NairaDex
Objetivo: construir una Pokédex informativa real y reutilizable.

Prioridades:
1. Definir modelo de datos común.
2. Cargar información de especies.
3. Implementar búsqueda por nombre.
4. Mostrar ficha básica.
5. Integrar condiciones reales de aparición.
6. Validar datos con Pixelmon 9.1.13.
7. Diseñar interfaz final cuando los datos estén estables.

## Fase 2 — NairaHunt
Objetivo: transformar un Pokémon del Dex en objetivo de búsqueda.

Prioridades:
1. Seleccionar un objetivo.
2. Interpretar sus reglas de aparición.
3. Compararlas con el entorno actual.
4. Mostrar condiciones cumplidas y faltantes.
5. Añadir un HUD compacto de seguimiento.

## Fase 3 — NairaSight
Objetivo: detectar y marcar visualmente el objetivo cuando la entidad exista y el cliente la conozca.

Prioridades:
1. Detectar la entidad correcta.
2. Validar alcance y comportamiento real en servidor.
3. Añadir marca visual discreta.
4. Mostrar distancia cuando aporte valor.
5. Evitar ruido visual y falsos positivos.

## Fase 4 — Naira Menu / HudManager
Objetivo: integrar los módulos sin llenar la pantalla de HUDs.

Prioridades:
1. Activar o desactivar módulos.
2. Activar o desactivar sonidos.
3. Definir prioridades de HUD.
4. Evitar solapamientos.
5. Centralizar opciones visuales comunes.

## Regla de avance
No iniciar una fase dependiente como estable hasta que su base esté validada.

Cada fase sigue:

`Diseñar -> Implementar -> Probar -> Validar -> Commit -> Merge`
