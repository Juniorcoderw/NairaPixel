package com.hjunior.nairapixel.core.pixelmon.breeding;

import com.hjunior.nairapixel.core.pixelmon.forms.PixelmonFormResolver;
import com.pixelmonmod.pixelmon.api.pokemon.egg.EggGroup;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.evs.EVYields;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PixelmonBreedingProvider {
    private static final Map<String,PokemonBreedingData> cache=
            new HashMap<>();

    private PixelmonBreedingProvider(){}

    public static Optional<PokemonBreedingData> getDatos(String pokemon){
        return getDatos(pokemon,"");
    }

    public static Optional<PokemonBreedingData> getDatos(
            String pokemon,
            String forma
    ){
        String key=crearKey(pokemon,forma);

        if(key.isEmpty()){
            return Optional.empty();
        }

        synchronized(cache){
            PokemonBreedingData guardado=
                    cache.get(key);

            if(guardado!=null){
                return Optional.of(guardado);
            }
        }

        Optional<PokemonBreedingData> resultado=
                cargar(pokemon,forma);

        if(resultado.isPresent()){
            synchronized(cache){
                cache.put(
                        key,
                        resultado.get()
                );
            }
        }

        return resultado;
    }

    private static Optional<PokemonBreedingData> cargar(
            String pokemon,
            String forma
    ){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(pokemon);

        if(!encontrado.isPresent()){
            return Optional.empty();
        }

        Species species=encontrado.get();

        Optional<Stats> formaEncontrada=
                PixelmonFormResolver.resolver(
                        species,
                        forma
                );

        if(!formaEncontrada.isPresent()){
            return Optional.empty();
        }

        Stats stats=formaEncontrada.get();

        List<String> grupos=
                leerGruposHuevo(stats);

        EVYields evs=
                stats.getEVYields();

        return Optional.of(
                new PokemonBreedingData(
                        species.getName(),
                        stats.getName(),
                        grupos,
                        stats.getEggCycles(),
                        stats.getCatchRate(),
                        leerEV(
                                evs,
                                BattleStatsType.HP
                        ),
                        leerEV(
                                evs,
                                BattleStatsType.ATTACK
                        ),
                        leerEV(
                                evs,
                                BattleStatsType.DEFENSE
                        ),
                        leerEV(
                                evs,
                                BattleStatsType.SPECIAL_ATTACK
                        ),
                        leerEV(
                                evs,
                                BattleStatsType.SPECIAL_DEFENSE
                        ),
                        leerEV(
                                evs,
                                BattleStatsType.SPEED
                        )
                )
        );
    }

    private static List<String> leerGruposHuevo(
            Stats forma
    ){
        List<String> resultado=
                new ArrayList<>();

        if(forma.getEggGroups()==null){
            return resultado;
        }

        for(EggGroup grupo:forma.getEggGroups()){
            if(grupo==null)continue;

            agregarUnico(
                    resultado,
                    grupo.getKey()
            );
        }

        return resultado;
    }

    private static int leerEV(
            EVYields evs,
            BattleStatsType tipo
    ){
        if(evs==null)return 0;

        return evs.getYield(tipo);
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

    private static void agregarUnico(
            List<String> lista,
            String valor
    ){
        if(valor==null)return;

        String limpio=
                valor.trim();

        if(!limpio.isEmpty()&&
                !lista.contains(limpio)){

            lista.add(limpio);
        }
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