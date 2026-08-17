package com.hjunior.nairapixel.core.pixelmon.forms;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.abilities.Abilities;
import com.pixelmonmod.pixelmon.api.pokemon.species.stat.ImmutableBattleStats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PixelmonFormProvider {
    private static final Map<String,List<PokemonFormData>> cache=
            new HashMap<>();

    private PixelmonFormProvider(){}

    public static List<PokemonFormData> getFormas(String pokemon){
        String key=normalizar(pokemon);
        if(key.isEmpty())return Collections.emptyList();

        synchronized(cache){
            List<PokemonFormData> guardado=cache.get(key);
            if(guardado!=null)return guardado;
        }

        List<PokemonFormData> resultado=cargar(pokemon);

        synchronized(cache){
            cache.put(key,resultado);
        }

        return resultado;
    }

    private static List<PokemonFormData> cargar(String nombre){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(nombre);

        if(!encontrado.isPresent()){
            return Collections.emptyList();
        }

        Species species=encontrado.get();
        List<Stats> formas=species.getForms(true);

        if(formas==null||formas.isEmpty()){
            return Collections.emptyList();
        }

        List<PokemonFormData> resultado=
                new ArrayList<>();

        for(Stats forma:formas){
            if(forma==null)continue;

            List<String> tipos=
                    leerTipos(forma);

            List<String> habilidades=
                    new ArrayList<>();

            List<String> ocultas=
                    new ArrayList<>();

            Abilities abilities=
                    forma.getAbilities();

            if(abilities!=null){
                cargarHabilidades(
                        abilities.getAbilities(),
                        habilidades
                );

                cargarHabilidades(
                        abilities.getHiddenAbilities(),
                        ocultas
                );
            }

            ImmutableBattleStats stats=
                    forma.getBattleStats();

            resultado.add(
                    new PokemonFormData(
                            species.getName(),
                            forma.getName(),
                            forma.getRegionalTag(),
                            species.isDefaultForm(forma),
                            forma.isTemporary(),
                            tipos,
                            habilidades,
                            ocultas,
                            leerStat(stats,BattleStatsType.HP),
                            leerStat(stats,BattleStatsType.ATTACK),
                            leerStat(stats,BattleStatsType.DEFENSE),
                            leerStat(stats,BattleStatsType.SPECIAL_ATTACK),
                            leerStat(stats,BattleStatsType.SPECIAL_DEFENSE),
                            leerStat(stats,BattleStatsType.SPEED)
                    )
            );
        }

        return Collections.unmodifiableList(resultado);
    }

    private static List<String> leerTipos(Stats forma){
        List<String> resultado=
                new ArrayList<>();

        if(forma.getTypes()==null){
            return resultado;
        }

        for(Element tipo:forma.getTypes()){
            if(tipo==null)continue;

            agregarUnico(
                    resultado,
                    tipo.getName()
            );
        }

        return resultado;
    }

    private static void cargarHabilidades(
            Ability[] origen,
            List<String> destino
    ){
        if(origen==null)return;

        for(Ability habilidad:origen){
            if(habilidad==null)continue;

            agregarUnico(
                    destino,
                    habilidad.getName()
            );
        }
    }

    private static int leerStat(
            ImmutableBattleStats stats,
            BattleStatsType tipo
    ){
        if(stats==null)return 0;

        return stats.getStat(tipo);
    }

    private static void agregarUnico(
            List<String> lista,
            String valor
    ){
        if(valor==null)return;

        String limpio=valor.trim();

        if(!limpio.isEmpty()&&!lista.contains(limpio)){
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