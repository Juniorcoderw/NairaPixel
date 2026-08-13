# Arquitectura de NairaPixel

## Objetivo
NairaPixel debe crecer como una plataforma modular y no como un conjunto de funciones aisladas.

## Capas

```text
NairaPixel
|
+-- core/
|   +-- pixelmon/
|   +-- config/
|   +-- hud/
|   +-- util/
|
+-- scanner/
+-- legend/
+-- dex/
+-- hunt/
+-- sight/
+-- menu/
```

## Core
La capa `core` contiene lógica reutilizable y no debe depender de un HUD concreto.

Responsabilidades previstas:
- Acceso común a datos de Pixelmon.
- Modelos de datos compartidos.
- Configuración del mod.
- Coordinación futura de HUDs.
- Utilidades comunes.

## Módulos
### Scanner
Lee y muestra información del Pokémon concreto al que apunta el jugador.

### Legend
Gestiona contador, previsión, condiciones, anuncios y resultado de ciclos legendarios.

### Dex
Consulta informativa de Pokémon: datos generales y condiciones reales de aparición.

### Hunt
Convierte un Pokémon del Dex en objetivo activo y guía al jugador según sus condiciones de aparición.

### Sight
Detecta visualmente al objetivo cuando la entidad ya existe y el cliente la conoce.

### Menu / HudManager
Gestionará módulos activables, sonidos, prioridad y convivencia de HUDs.

## Dependencias deseadas

```text
              core / PixelmonData
                 /    |     \
                /     |      \
          Scanner   Legend    Dex
                              |
                             Hunt
                              |
                            Sight

Menu / HudManager coordina presentación y activación.
```

## Reglas
- Un HUD no debe convertirse en parser de datos si esos datos pueden reutilizarse.
- No duplicar lógica entre módulos.
- Los módulos estables se modifican solo por bug real, integración necesaria o decisión explícita.
- La lógica de negocio debe mantenerse separada del renderizado cuando sea razonable.
- El cliente solo puede actuar sobre información que realmente conoce o puede leer.
