package com.hjunior.nairapixel.core.pixelmon.evolution;

import com.hjunior.nairapixel.core.pixelmon.forms.PixelmonFormResolver;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.EvoRockCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.FriendshipCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.MoveTypeCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.PartyCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.TimeCondition;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.InteractEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.TradeEvolution;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PixelmonEvolutionProvider {
    private static final Map<String,List<PokemonEvolutionData>> cache=
            new HashMap<>();

    private PixelmonEvolutionProvider(){}

    public static List<PokemonEvolutionData> getEvoluciones(String pokemon){
        return getEvoluciones(pokemon,"");
    }

    public static List<PokemonEvolutionData> getEvoluciones(
            String pokemon,
            String forma
    ){
        String key=crearKey(pokemon,forma);

        if(key.isEmpty()){
            return Collections.emptyList();
        }

        synchronized(cache){
            List<PokemonEvolutionData> guardado=
                    cache.get(key);

            if(guardado!=null){
                return guardado;
            }
        }

        List<PokemonEvolutionData> resultado=
                cargar(pokemon,forma);

        synchronized(cache){
            cache.put(key,resultado);
        }

        return resultado;
    }

    private static List<PokemonEvolutionData> cargar(
            String nombre,
            String forma
    ){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(nombre);

        if(!encontrado.isPresent()){
            return Collections.emptyList();
        }

        Species species=encontrado.get();

        Optional<Stats> statsEncontrados=
                PixelmonFormResolver.resolver(
                        species,
                        forma
                );

        if(!statsEncontrados.isPresent()){
            return Collections.emptyList();
        }

        Stats stats=statsEncontrados.get();

        if(stats.getEvolutions()==null){
            return Collections.emptyList();
        }

        List<PokemonEvolutionData> resultado=
                new ArrayList<>();

        for(Evolution evolucion:stats.getEvolutions()){
            if(evolucion==null)continue;

            Destino destino=leerDestino(
                    evolucion.to==null
                            ?""
                            :evolucion.to.toString()
            );

            Integer nivel=null;
            String objeto="";
            String intercambioCon="";

            if(evolucion instanceof LevelingEvolution){
                nivel=
                        ((LevelingEvolution)evolucion).level;
            }

            if(evolucion instanceof InteractEvolution){
                InteractEvolution interact=
                        (InteractEvolution)evolucion;

                objeto=leerObjeto(interact);
            }

            if(evolucion instanceof TradeEvolution){
                TradeEvolution trade=
                        (TradeEvolution)evolucion;

                if(trade.with!=null){
                    intercambioCon=
                            trade.with.toString();
                }
            }

            List<String> condiciones=
                    leerCondiciones(evolucion);

            if(evolucion instanceof InteractEvolution){
                InteractEvolution interact=
                        (InteractEvolution)evolucion;

                if(Boolean.TRUE.equals(interact.emptyHand)){
                    agregarUnico(
                            condiciones,
                            "emptyHand:true"
                    );
                }
            }

            resultado.add(
                    new PokemonEvolutionData(
                            species.getName(),
                            stats.getName(),
                            destino.pokemon,
                            destino.forma,
                            tipo(evolucion),
                            nivel,
                            objeto,
                            intercambioCon,
                            condiciones
                    )
            );
        }

        return Collections.unmodifiableList(resultado);
    }

    private static List<String> leerCondiciones(
            Evolution evolucion
    ){
        List<String> resultado=
                new ArrayList<>();

        if(evolucion.conditions==null){
            return resultado;
        }

        for(EvoCondition condicion:evolucion.conditions){
            if(condicion==null)continue;

            if(condicion instanceof FriendshipCondition){
                FriendshipCondition c=
                        (FriendshipCondition)condicion;

                agregarUnico(
                        resultado,
                        "friendship:"+c.friendship
                );

                continue;
            }

            if(condicion instanceof TimeCondition){
                TimeCondition c=
                        (TimeCondition)condicion;

                if(c.times!=null&&!c.times.isEmpty()){
                    for(Object tiempo:c.times){
                        if(tiempo==null)continue;

                        agregarUnico(
                                resultado,
                                "time:"+tiempo
                        );
                    }
                }else if(c.time!=null){
                    agregarUnico(
                            resultado,
                            "time:"+c.time
                    );
                }

                continue;
            }

            if(condicion instanceof MoveTypeCondition){
                MoveTypeCondition c=
                        (MoveTypeCondition)condicion;

                if(c.type!=null){
                    agregarUnico(
                            resultado,
                            "moveType:"+c.type.getName()
                    );
                }

                continue;
            }

            if(condicion instanceof EvoRockCondition){
                EvoRockCondition c=
                        (EvoRockCondition)condicion;

                if(c.evolutionRock!=null){
                    agregarUnico(
                            resultado,
                            "evoRock:"+c.evolutionRock
                    );
                }

                continue;
            }

            if(condicion instanceof PartyCondition){
                leerParty(
                        (PartyCondition)condicion,
                        resultado
                );

                continue;
            }

            String descripcion=
                    descripcion(condicion);

            if(descripcion.isEmpty()){
                descripcion=
                        condicion.getClass()
                                .getSimpleName();
            }

            agregarUnico(
                    resultado,
                    descripcion
            );
        }

        return resultado;
    }

    private static void leerParty(
            PartyCondition condicion,
            List<String> resultado
    ){
        if(condicion.withPokemon!=null){
            for(Object pokemon:condicion.withPokemon){
                if(pokemon==null)continue;

                agregarUnico(
                        resultado,
                        "partyPokemon:"+pokemon
                );
            }
        }

        if(condicion.withTypes!=null){
            for(Element tipo:condicion.withTypes){
                if(tipo==null)continue;

                agregarUnico(
                        resultado,
                        "partyType:"+tipo.getName()
                );
            }
        }

        if(condicion.withForms!=null){
            for(String forma:condicion.withForms){
                agregarUnico(
                        resultado,
                        "partyForm:"+forma
                );
            }
        }

        if(condicion.withPalettes!=null){
            for(String paleta:condicion.withPalettes){
                agregarUnico(
                        resultado,
                        "partyPalette:"+paleta
                );
            }
        }
    }

    private static String leerObjeto(
            InteractEvolution evolucion
    ){
        if(evolucion==null||evolucion.item==null){
            return "";
        }

        try{
            Method metodo=
                    evolucion.item
                            .getClass()
                            .getMethod("getItemStack");

            Object valor=
                    metodo.invoke(evolucion.item);

            if(valor instanceof ItemStack){
                ItemStack stack=
                        (ItemStack)valor;

                if(!stack.isEmpty()){
                    ResourceLocation id=
                            stack.getItem()
                                    .getRegistryName();

                    if(id!=null){
                        return id.toString();
                    }
                }
            }
        }catch(Exception ignored){
        }

        try{
            Field campo=
                    evolucion.item
                            .getClass()
                            .getField("itemID");

            Object valor=
                    campo.get(evolucion.item);

            if(valor!=null){
                return String.valueOf(valor);
            }
        }catch(Exception ignored){
        }

        return "";
    }

    private static String descripcion(
            EvoCondition condicion
    ){
        try{
            if(condicion.getDescription()==null){
                return "";
            }

            return condicion
                    .getDescription()
                    .getString()
                    .trim();

        }catch(Exception e){
            return "";
        }
    }

    private static Destino leerDestino(String spec){
        if(spec==null){
            return new Destino("","");
        }

        String pokemon="";
        String forma="";

        for(String parte:spec.trim().split("\\s+")){
            if(parte.isEmpty())continue;

            String lower=
                    parte.toLowerCase(Locale.ROOT);

            if(lower.startsWith("species:")){
                pokemon=
                        parte.substring(
                                "species:".length()
                        ).trim();

                continue;
            }

            if(lower.startsWith("form:")){
                forma=
                        parte.substring(
                                "form:".length()
                        ).trim();

                continue;
            }

            if(pokemon.isEmpty()&&!parte.contains(":")){
                pokemon=parte.trim();
            }
        }

        return new Destino(
                pokemon,
                forma
        );
    }

    private static String tipo(Evolution evolucion){
        if(evolucion.evoType!=null&&
                !evolucion.evoType.trim().isEmpty()){

            return evolucion.evoType.trim();
        }

        return evolucion
                .getClass()
                .getSimpleName();
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

    private static final class Destino {
        private final String pokemon;
        private final String forma;

        private Destino(
                String pokemon,
                String forma
        ){
            this.pokemon=
                    pokemon==null
                            ?""
                            :pokemon.trim();

            this.forma=
                    forma==null
                            ?""
                            :forma.trim();
        }
    }
}