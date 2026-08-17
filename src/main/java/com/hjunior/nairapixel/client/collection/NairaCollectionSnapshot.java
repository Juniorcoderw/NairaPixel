package com.hjunior.nairapixel.client.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class NairaCollectionSnapshot {
    private final List<OwnedPokemonData> pokemon;
    private final Map<String,List<OwnedPokemonData>> porEspecie;

    private final int totalPC;
    private final int totalEquipo;
    private final int totalShiny;

    public NairaCollectionSnapshot(List<OwnedPokemonData> origen){
        List<OwnedPokemonData> lista=new ArrayList<>();
        Map<String,List<OwnedPokemonData>> especies=new LinkedHashMap<>();
        Set<UUID> uuids=new HashSet<>();

        int pc=0;
        int equipo=0;
        int shiny=0;

        if(origen!=null){
            for(OwnedPokemonData dato:origen){
                if(dato==null)continue;

                UUID uuid=dato.getUuid();

                if(uuid!=null&&!uuids.add(uuid)){
                    continue;
                }

                lista.add(dato);

                String key=normalizar(dato.getPokemon());

                if(!key.isEmpty()){
                    especies.computeIfAbsent(
                            key,
                            k->new ArrayList<>()
                    ).add(dato);
                }

                if(dato.estaEnPC())pc++;
                if(dato.estaEnEquipo())equipo++;
                if(dato.isShiny())shiny++;
            }
        }

        Map<String,List<OwnedPokemonData>> indice=
                new LinkedHashMap<>();

        for(Map.Entry<String,List<OwnedPokemonData>> entrada:
                especies.entrySet()){

            indice.put(
                    entrada.getKey(),
                    Collections.unmodifiableList(
                            new ArrayList<>(entrada.getValue())
                    )
            );
        }

        this.pokemon=Collections.unmodifiableList(lista);
        this.porEspecie=Collections.unmodifiableMap(indice);
        this.totalPC=pc;
        this.totalEquipo=equipo;
        this.totalShiny=shiny;
    }

    public static NairaCollectionSnapshot vacio(){
        return new NairaCollectionSnapshot(
                Collections.emptyList()
        );
    }

    public List<OwnedPokemonData> getPokemon(){
        return pokemon;
    }

    public int getTotalPokemon(){
        return pokemon.size();
    }

    public int getTotalPC(){
        return totalPC;
    }

    public int getTotalEquipo(){
        return totalEquipo;
    }

    public int getTotalShiny(){
        return totalShiny;
    }

    public int getEspeciesDistintas(){
        return porEspecie.size();
    }

    public boolean tiene(String pokemon){
        return getCantidad(pokemon)>0;
    }

    public int getCantidad(String pokemon){
        return getEjemplares(pokemon).size();
    }

    public List<OwnedPokemonData> getEjemplares(String pokemon){
        String key=normalizar(pokemon);

        if(key.isEmpty()){
            return Collections.emptyList();
        }

        List<OwnedPokemonData> encontrados=
                porEspecie.get(key);

        return encontrados==null
                ?Collections.emptyList()
                :encontrados;
    }

    public int getCantidadPC(String pokemon){
        int total=0;

        for(OwnedPokemonData dato:getEjemplares(pokemon)){
            if(dato.estaEnPC())total++;
        }

        return total;
    }

    public int getCantidadEquipo(String pokemon){
        int total=0;

        for(OwnedPokemonData dato:getEjemplares(pokemon)){
            if(dato.estaEnEquipo())total++;
        }

        return total;
    }

    public int getCantidadShiny(String pokemon){
        int total=0;

        for(OwnedPokemonData dato:getEjemplares(pokemon)){
            if(dato.isShiny())total++;
        }

        return total;
    }

    public int getNivelMaximo(String pokemon){
        int maximo=0;

        for(OwnedPokemonData dato:getEjemplares(pokemon)){
            if(dato.getNivel()>maximo){
                maximo=dato.getNivel();
            }
        }

        return maximo;
    }

    public List<String> getFormas(String pokemon){
        Set<String> formas=new LinkedHashSet<>();

        for(OwnedPokemonData dato:getEjemplares(pokemon)){
            formas.add(dato.getForma());
        }

        return Collections.unmodifiableList(
                new ArrayList<>(formas)
        );
    }

    public List<String> getEspecies(){
        List<String> resultado=new ArrayList<>();

        for(List<OwnedPokemonData> ejemplares:
                porEspecie.values()){

            if(ejemplares.isEmpty())continue;

            resultado.add(
                    ejemplares.get(0).getPokemon()
            );
        }

        return Collections.unmodifiableList(resultado);
    }

    private static String normalizar(String texto){
        if(texto==null)return "";

        String valor=texto.toLowerCase(Locale.ROOT);
        StringBuilder resultado=new StringBuilder();

        for(int i=0;i<valor.length();i++){
            char c=valor.charAt(i);

            if(Character.isLetterOrDigit(c)){
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}