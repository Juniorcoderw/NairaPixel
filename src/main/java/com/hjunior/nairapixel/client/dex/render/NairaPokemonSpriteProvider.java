package com.hjunior.nairapixel.client.dex.render;

import com.hjunior.nairapixel.core.pixelmon.forms.PixelmonFormResolver;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class NairaPokemonSpriteProvider {
    private static final Map<String,ResourceLocation> cache=
            new HashMap<>();

    private static final Set<String> noEncontrados=
            new HashSet<>();

    private NairaPokemonSpriteProvider(){}

    public static Optional<ResourceLocation> getSprite(
            String pokemon,
            String forma
    ){
        String key=
                crearKey(
                        pokemon,
                        forma
                );

        if(key.isEmpty()){
            return Optional.empty();
        }

        synchronized(cache){
            ResourceLocation guardado=
                    cache.get(key);

            if(guardado!=null){
                return Optional.of(guardado);
            }
        }

        synchronized(noEncontrados){
            if(noEncontrados.contains(key)){
                return Optional.empty();
            }
        }

        Optional<ResourceLocation> resultado=
                cargar(
                        pokemon,
                        forma
                );

        if(resultado.isPresent()){
            synchronized(cache){
                cache.put(
                        key,
                        resultado.get()
                );
            }
        }else{
            synchronized(noEncontrados){
                noEncontrados.add(key);
            }
        }

        return resultado;
    }

    private static Optional<ResourceLocation> cargar(
            String pokemon,
            String forma
    ){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(pokemon);

        if(!encontrado.isPresent()){
            return Optional.empty();
        }

        Species species=
                encontrado.get();

        Optional<Stats> statsEncontrados=
                PixelmonFormResolver.resolver(
                        species,
                        forma
                );

        if(!statsEncontrados.isPresent()){
            return Optional.empty();
        }

        Stats stats=
                statsEncontrados.get();

        try{
            Pokemon visual=
                    PokemonBuilder.builder()
                            .species(species)
                            .form(stats)
                            .build();

            if(visual==null){
                return Optional.empty();
            }

            ResourceLocation sprite=
                    visual.getSprite();

            return Optional.ofNullable(sprite);

        }catch(Exception e){
            return Optional.empty();
        }
    }

    public static void limpiarCache(){
        synchronized(cache){
            cache.clear();
        }

        synchronized(noEncontrados){
            noEncontrados.clear();
        }
    }

    private static String crearKey(
            String pokemon,
            String forma
    ){
        String pokemonKey=
                normalizar(pokemon);

        if(pokemonKey.isEmpty()){
            return "";
        }

        String formaKey=
                normalizar(forma);

        if(formaKey.isEmpty()||
                formaKey.equals("base")){

            formaKey="base";
        }

        return pokemonKey+"|"+formaKey;
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