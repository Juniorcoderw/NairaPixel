package com.hjunior.nairapixel.core.pixelmon;

import com.hjunior.nairapixel.core.pixelmon.breeding.PixelmonBreedingProvider;
import com.hjunior.nairapixel.core.pixelmon.breeding.PokemonBreedingData;
import com.hjunior.nairapixel.core.pixelmon.evolution.PixelmonEvolutionProvider;
import com.hjunior.nairapixel.core.pixelmon.evolution.PokemonEvolutionData;
import com.hjunior.nairapixel.core.pixelmon.forms.PixelmonFormProvider;
import com.hjunior.nairapixel.core.pixelmon.forms.PokemonFormData;
import com.hjunior.nairapixel.core.pixelmon.moves.PixelmonMoveProvider;
import com.hjunior.nairapixel.core.pixelmon.moves.PokemonMoveData;
import com.hjunior.nairapixel.core.pixelmon.spawn.PixelmonSpawnProvider;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import com.hjunior.nairapixel.core.pixelmon.species.PixelmonSpeciesProvider;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;

import java.util.List;
import java.util.Optional;

public final class PixelmonDataService {
    private PixelmonDataService(){}

    // Species
    public static Optional<PokemonSpeciesData> getPokemon(
            String nombre
    ){
        return PixelmonSpeciesProvider.buscar(nombre);
    }

    public static Optional<PokemonSpeciesData> getPokemon(
            int numeroDex
    ){
        return PixelmonSpeciesProvider.buscar(numeroDex);
    }

    public static List<PokemonSpeciesData> getPokemon(){
        return PixelmonSpeciesProvider.getTodas();
    }

    public static int getCantidadPokemon(){
        return PixelmonSpeciesProvider.size();
    }

    // Formas
    public static List<PokemonFormData> getFormas(
            String pokemon
    ){
        return PixelmonFormProvider.getFormas(pokemon);
    }

    // Spawn
    public static List<PokemonSpawnRule> getSpawns(
            String pokemon
    ){
        return PixelmonSpawnProvider.getReglas(pokemon);
    }

    public static List<PokemonSpawnRule> getSpawns(
            String pokemon,
            String forma
    ){
        return PixelmonSpawnProvider.getReglas(
                pokemon,
                forma
        );
    }

    public static int getCantidadReglasSpawn(){
        return PixelmonSpawnProvider.getCantidadReglas();
    }

    // Movimientos
    public static List<PokemonMoveData> getMovimientos(
            String pokemon
    ){
        return PixelmonMoveProvider.getMovimientos(pokemon);
    }

    public static List<PokemonMoveData> getMovimientos(
            String pokemon,
            String forma
    ){
        return PixelmonMoveProvider.getMovimientos(
                pokemon,
                forma
        );
    }

    // Evoluciones
    public static List<PokemonEvolutionData> getEvoluciones(
            String pokemon
    ){
        return PixelmonEvolutionProvider.getEvoluciones(pokemon);
    }

    public static List<PokemonEvolutionData> getEvoluciones(
            String pokemon,
            String forma
    ){
        return PixelmonEvolutionProvider.getEvoluciones(
                pokemon,
                forma
        );
    }

    // Crianza / captura / EV yield
    public static Optional<PokemonBreedingData> getCrianza(
            String pokemon
    ){
        return PixelmonBreedingProvider.getDatos(pokemon);
    }

    public static Optional<PokemonBreedingData> getCrianza(
            String pokemon,
            String forma
    ){
        return PixelmonBreedingProvider.getDatos(
                pokemon,
                forma
        );
    }
}