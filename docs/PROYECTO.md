# NairaPixel — Scanner v1

## Objetivo
Mostrar automáticamente información útil del Pokémon concreto al que apunta el jugador.

**Apuntar -> ver información -> dejar de apuntar -> ocultar.**

No requiere teclas, menús ni ventanas adicionales.

## Información visible
- Nombre
- Nivel
- Tipo(s)
- Género
- Tamaño (Growth)
- IV total en porcentaje
- PS, Ataque, Defensa, Ataque Especial, Defensa Especial y Velocidad
- Naturaleza
- Habilidad
- Indicador HA si corresponde
- Shiny, forma o paleta especial solo cuando aplique

Los IV con valor 31 se marcarán con `★`.

## Idioma
La interfaz será en español. Los datos internos de Pixelmon se traducirán antes de mostrarse.

Ejemplos:
- `MALE` -> Macho
- `ENORMOUS` -> Enorme
- `JOLLY` -> Alegre (Jolly)
- `AIR_LOCK` -> Bucle Aire (Air Lock)

## Diseño aproximado

```text
┌────────────── NAIRAPIXEL ──────────────┐
│ RAYQUAZA                       Nv. 73  │
│ Dragón / Volador          ♂ Macho     │
│ Tamaño: Enorme                         │
│                                        │
│ IV TOTAL                      93.5%    │
│                                        │
│ PS       31 ★       DEF        28      │
│ ATQ      31 ★       DEF.ESP    30      │
│ ATQ.ESP  17         VEL        31 ★    │
│                                        │
│ Naturaleza: Alegre (Jolly)             │
│ Habilidad: Bucle Aire (Air Lock)       │
│                              by HJunior │
└────────────────────────────────────────┘
```

## Principios
- Útil
- Simple
- Bonito
- Ligero
- Rápido
- Confiable

## Estrategia técnica
1. Preparar Forge 1.16.5.
2. Integrar Pixelmon 9.1.13 como dependencia local.
3. Detectar `PixelmonEntity` bajo la mira.
4. Leer los datos disponibles.
5. Validarlos contra información real.
6. Traducirlos al español.
7. Construir el HUD únicamente cuando los datos sean correctos.

## Regla de validación
**Dato leído -> dato validado -> dato mostrado.**

Nunca mostrar como real un valor supuesto o no sincronizado.
