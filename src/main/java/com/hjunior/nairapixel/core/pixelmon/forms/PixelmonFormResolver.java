package com.hjunior.nairapixel.core.pixelmon.forms;

import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PixelmonFormResolver {
    private PixelmonFormResolver(){}

    public static Optional<Stats> resolver(
            String pokemon,
            String forma
    ){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(pokemon);

        if(!encontrado.isPresent()){
            return Optional.empty();
        }

        return resolver(
                encontrado.get(),
                forma
        );
    }

    public static Optional<Stats> resolver(
            Species species,
            String forma
    ){
        if(species==null){
            return Optional.empty();
        }

        String formaKey=normalizar(forma);

        if(formaKey.isEmpty()||formaKey.equals("base")){
            Stats base=species.getFirstForm();

            return Optional.ofNullable(base);
        }

        List<Stats> formas=species.getForms(true);

        if(formas==null||formas.isEmpty()){
            return Optional.empty();
        }

        for(Stats stats:formas){
            if(stats==null)continue;

            String nombreKey=
                    normalizar(stats.getName());

            if(formaKey.equals(nombreKey)){
                return Optional.of(stats);
            }
        }

        return Optional.empty();
    }

    public static boolean existe(
            String pokemon,
            String forma
    ){
        return resolver(pokemon,forma).isPresent();
    }

    private static String normalizar(String texto){
        if(texto==null)return "";

        String valor=
                texto.toLowerCase(Locale.ROOT);

        StringBuilder resultado=
                new StringBuilder();

        for(int i=0;i<valor.length();i++){
            char c=valor.charAt(i);

            if(Character.isLetterOrDigit(c)){
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}