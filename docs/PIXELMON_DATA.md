# Datos de Pixelmon en NairaPixel

## Principio
NairaPixel debe usar datos reales disponibles en Pixelmon o conocidos por el cliente. No se deben inventar valores para completar una interfaz.

## Fuente local
Pixelmon 9.1.13 se usa como dependencia local y no se incluye en Git.

Cuando sea necesario leer datos empaquetados, NairaPixel puede localizar el JAR instalado y consultar sus recursos sin modificarlo.

## Estado actual
Naira Legend ya utiliza reglas reales de aparición desde los recursos de Pixelmon y ha validado su funcionamiento en servidor real.

## Dirección futura
La lectura común debe tender a una capa reutilizable:

```text
core/pixelmon/
    PixelmonDataService
    PokemonData
    PokemonSpawnData
    PokemonAbilityData
    PokemonMoveData
```

Los nombres definitivos pueden cambiar durante la implementación si existe una estructura más simple o correcta.

## Datos previstos para NairaDex
Según lo que Pixelmon permita obtener de forma fiable:
- Identidad y número del Pokémon.
- Tipos.
- Estadísticas base.
- Habilidades.
- Formas y variantes relevantes.
- Evoluciones.
- Movimientos.
- Condiciones de aparición.
- Biomas.
- Horarios.
- Clima.
- Altura.
- Tipo de ubicación.
- Bloques o condiciones especiales cuando existan.

## Spawning
Las reglas de aparición pueden contener condiciones distintas según especie o forma. El sistema debe conservar esa estructura y no resumirla de forma que produzca información falsa.

## Servidor
Los recursos locales de Pixelmon representan la configuración base del mod. Un servidor puede modificar comportamiento, configuración o contenido.

Cuando PokeGalaxia tenga una diferencia comprobada respecto a Pixelmon base, debe documentarse y tratarse explícitamente en lugar de asumir que ambas fuentes son idénticas.

## Regla de validación
Antes de reutilizar un nuevo tipo de dato en HUDs o búsquedas:

`leer -> interpretar -> comparar -> validar -> mostrar`
