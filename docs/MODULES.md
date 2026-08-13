# Módulos de NairaPixel

## Scanner
Estado: estable.

Muestra información del Pokémon concreto al que apunta el jugador.

Responsabilidades:
- Detectar el Pokémon objetivo bajo la mira.
- Leer únicamente información conocida por el cliente.
- Traducir y mostrar los datos en español.
- Mantener un HUD ligero y no invasivo.

## Naira Legend
Estado: estable.

Gestiona el ciclo de aparición de legendarios en PokeGalaxia.

Responsabilidades:
- Leer el contador `Legend` del scoreboard.
- Sincronizar el ciclo real del servidor.
- Consultar reglas reales de aparición de Pixelmon.
- Evaluar condiciones del entorno.
- Mostrar previsión, candidatos y resultados.
- Avisar con sonido en eventos importantes.

## NairaDex
Estado: próximo módulo.

Pokédex informativa basada en datos reales de Pixelmon.

Objetivo inicial:
- Buscar Pokémon por nombre.
- Mostrar tipos, habilidades, estadísticas y otra información útil disponible.
- Mostrar condiciones reales de aparición.
- Servir como fuente para iniciar una búsqueda con NairaHunt.

## NairaHunt
Estado: pendiente.

Convierte un Pokémon seleccionado en NairaDex en un objetivo activo.

Objetivo previsto:
- Mostrar biomas, horario, clima, altura y otras condiciones relevantes.
- Comparar las condiciones requeridas con el entorno actual.
- Indicar cuándo el jugador está en una zona favorable.
- Mantener un solo objetivo activo.

## NairaSight
Estado: pendiente.

Marca visualmente el Pokémon objetivo cuando la entidad ya existe y el cliente la conoce.

Objetivo previsto:
- Detectar únicamente el objetivo activo.
- Mostrar una marca visual clara y discreta.
- Mostrar distancia cuando sea útil.
- No afirmar que existe una entidad que el cliente no ha recibido.

## Naira Menu / HudManager
Estado: pendiente.

Coordinará la convivencia de todos los módulos.

Objetivo previsto:
- Activar o desactivar módulos.
- Activar o desactivar sonidos.
- Evitar solapamientos innecesarios entre HUDs.
- Gestionar prioridades temporales de interfaz.
- Centralizar opciones visuales comunes.

## Regla común
Cada módulo debe encargarse de su función. Los datos compartidos y la lógica reutilizable deben tender a `core` en lugar de duplicarse entre módulos.
