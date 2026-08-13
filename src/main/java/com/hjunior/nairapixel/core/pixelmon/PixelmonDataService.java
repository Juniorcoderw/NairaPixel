package com.hjunior.nairapixel.core.pixelmon;

import com.hjunior.nairapixel.core.pixelmon.species.PixelmonSpeciesProvider;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;

import java.util.List;
import java.util.Optional;

public final class PixelmonDataService {
    private PixelmonDataService(){}

    public static Optional<PokemonSpeciesData> getPokemon(String nombre){
        return PixelmonSpeciesProvider.buscar(nombre);
    }

    public static Optional<PokemonSpeciesData> getPokemon(int numeroDex){
        return PixelmonSpeciesProvider.buscar(numeroDex);
    }

    public static List<PokemonSpeciesData> getPokemon(){
        return PixelmonSpeciesProvider.getTodas();
    }

    public static int getCantidadPokemon(){
        return PixelmonSpeciesProvider.size();
    }
}