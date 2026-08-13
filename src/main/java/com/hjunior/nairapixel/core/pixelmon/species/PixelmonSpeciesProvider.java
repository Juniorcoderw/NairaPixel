package com.hjunior.nairapixel.core.pixelmon.species;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PixelmonSpeciesProvider {
    private static Map<String,PokemonSpeciesData> porNombre;
    private static Map<Integer,PokemonSpeciesData> porDex;
    private static List<PokemonSpeciesData> especies;

    private PixelmonSpeciesProvider(){}

    public static Optional<PokemonSpeciesData> buscar(String nombre){
        asegurarCarga();

        String key=normalizar(nombre);

        if(key.isEmpty()){
            return Optional.empty();
        }

        return Optional.ofNullable(
                porNombre.get(key)
        );
    }

    public static Optional<PokemonSpeciesData> buscar(int dex){
        asegurarCarga();

        return Optional.ofNullable(
                porDex.get(dex)
        );
    }

    public static List<PokemonSpeciesData> getTodas(){
        asegurarCarga();
        return especies;
    }

    public static int size(){
        asegurarCarga();
        return especies.size();
    }

    private static synchronized void asegurarCarga(){
        if(especies!=null)return;

        List<Species> registradas=
                new ArrayList<>(
                        PixelmonSpecies.getAll()
                );

        registradas.sort(
                Comparator.comparingInt(
                        Species::getDex
                )
        );

        Map<String,PokemonSpeciesData> nombres=
                new LinkedHashMap<>();

        Map<Integer,PokemonSpeciesData> dex=
                new LinkedHashMap<>();

        List<PokemonSpeciesData> datos=
                new ArrayList<>();

        for(Species species:registradas){
            if(species==null||
                    species.getDex()<=0){
                continue;
            }

            PokemonSpeciesData pokemon=
                    convertir(species);

            if(pokemon==null)continue;

            datos.add(pokemon);

            dex.put(
                    pokemon.getNumeroDex(),
                    pokemon
            );

            agregarIndice(
                    nombres,
                    species.getName(),
                    pokemon
            );

            agregarIndice(
                    nombres,
                    species.getStrippedName(),
                    pokemon
            );
        }

        porNombre=Collections.unmodifiableMap(
                nombres
        );

        porDex=Collections.unmodifiableMap(
                dex
        );

        especies=Collections.unmodifiableList(
                datos
        );
    }

    private static PokemonSpeciesData convertir(
            Species species
    ){
        Stats forma=
                species.getFirstForm();

        if(forma==null){
            return null;
        }

        List<String> tipos=
                new ArrayList<>();

        for(Element tipo:forma.getTypes()){
            if(tipo!=null){
                tipos.add(
                        tipo.getName()
                );
            }
        }

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

        return new PokemonSpeciesData(
                species.getDex(),
                species.getGeneration(),
                species.getName(),
                forma.getName(),
                tipos,
                habilidades,
                ocultas,
                leerStat(
                        stats,
                        BattleStatsType.HP
                ),
                leerStat(
                        stats,
                        BattleStatsType.ATTACK
                ),
                leerStat(
                        stats,
                        BattleStatsType.DEFENSE
                ),
                leerStat(
                        stats,
                        BattleStatsType.SPECIAL_ATTACK
                ),
                leerStat(
                        stats,
                        BattleStatsType.SPECIAL_DEFENSE
                ),
                leerStat(
                        stats,
                        BattleStatsType.SPEED
                ),
                species.isLegendary(),
                species.isMythical(),
                species.isUltraBeast()
        );
    }

    private static int leerStat(
            ImmutableBattleStats stats,
            BattleStatsType tipo
    ){
        if(stats==null)return 0;

        return stats.getStat(tipo);
    }

    private static void cargarHabilidades(
            Ability[] origen,
            List<String> destino
    ){
        if(origen==null)return;

        for(Ability habilidad:origen){
            if(habilidad==null)continue;

            destino.add(
                    habilidad.getName()
            );
        }
    }

    private static void agregarIndice(
            Map<String,PokemonSpeciesData> indice,
            String nombre,
            PokemonSpeciesData pokemon
    ){
        String key=normalizar(nombre);

        if(key.isEmpty())return;

        indice.putIfAbsent(
                key,
                pokemon
        );
    }

    private static String normalizar(
            String texto
    ){
        if(texto==null)return "";

        String valor=
                texto.toLowerCase(
                        Locale.ROOT
                );

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