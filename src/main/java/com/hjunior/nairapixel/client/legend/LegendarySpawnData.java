package com.hjunior.nairapixel.client.legend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegendarySpawnData {
    private final String pokemon;
    private final String displayBiome;

    private final List<String> times;
    private final List<String> biomes;
    private final List<String> weathers;
    private final List<String> baseBlocks;

    private final Integer minY;
    private final Integer maxY;
    private final Integer moonPhase;

    private final String legacyCondition;

    // Constructor temporal compatible con el provider actual
    public LegendarySpawnData(
            String pokemon,
            String displayBiome,
            String condition,
            List<String> times,
            List<String> biomes
    ){
        this(
                pokemon,
                displayBiome,
                times,
                biomes,
                Collections.emptyList(),
                null,
                null,
                null,
                Collections.emptyList(),
                condition
        );
    }

    // Constructor definitivo para datos estructurados
    public LegendarySpawnData(
            String pokemon,
            String displayBiome,
            List<String> times,
            List<String> biomes,
            List<String> weathers,
            Integer minY,
            Integer maxY,
            Integer moonPhase,
            List<String> baseBlocks
    ){
        this(
                pokemon,
                displayBiome,
                times,
                biomes,
                weathers,
                minY,
                maxY,
                moonPhase,
                baseBlocks,
                ""
        );
    }

    private LegendarySpawnData(
            String pokemon,
            String displayBiome,
            List<String> times,
            List<String> biomes,
            List<String> weathers,
            Integer minY,
            Integer maxY,
            Integer moonPhase,
            List<String> baseBlocks,
            String legacyCondition
    ){
        this.pokemon=limpiar(pokemon);
        this.displayBiome=limpiar(displayBiome);

        this.times=copiarLista(times);
        this.biomes=copiarLista(biomes);
        this.weathers=copiarLista(weathers);
        this.baseBlocks=copiarLista(baseBlocks);

        this.minY=minY;
        this.maxY=maxY;
        this.moonPhase=moonPhase;

        this.legacyCondition=limpiar(legacyCondition);
    }

    public String getPokemon(){
        return pokemon;
    }

    public String getDisplayBiome(){
        return displayBiome.isEmpty()
                ?"Anywhere"
                :displayBiome;
    }

    public List<String> getTimes(){
        return times;
    }

    public List<String> getBiomes(){
        return biomes;
    }

    public List<String> getWeathers(){
        return weathers;
    }

    public List<String> getBaseBlocks(){
        return baseBlocks;
    }

    public Integer getMinY(){
        return minY;
    }

    public Integer getMaxY(){
        return maxY;
    }

    public Integer getMoonPhase(){
        return moonPhase;
    }

    public boolean tieneHorarios(){
        return !times.isEmpty();
    }

    public boolean tieneBiomas(){
        return !biomes.isEmpty();
    }

    public boolean tieneClima(){
        return !weathers.isEmpty();
    }

    public boolean tieneAlturaMinima(){
        return minY!=null;
    }

    public boolean tieneAlturaMaxima(){
        return maxY!=null;
    }

    public boolean tieneFaseLunar(){
        return moonPhase!=null;
    }

    public boolean tieneBloquesBase(){
        return !baseBlocks.isEmpty();
    }

    public boolean tieneCondicion(){
        return !getCondition().isEmpty();
    }

    // Se mantiene porque Watcher/Predictor todavía lo utilizan
    public String getCondition(){
        if(!legacyCondition.isEmpty()){
            return legacyCondition;
        }

        List<String> partes=new ArrayList<>();

        if(!weathers.isEmpty()){
            partes.add(unir(weathers," / "));
        }

        if(minY!=null){
            partes.add("Y >= "+minY);
        }

        if(maxY!=null){
            partes.add("Y <= "+maxY);
        }

        if(moonPhase!=null){
            partes.add("Moon "+moonPhase);
        }

        if(!baseBlocks.isEmpty()){
            partes.add(
                    "Base: "+unir(baseBlocks,", ")
            );
        }

        return unir(partes," · ");
    }

    private static List<String> copiarLista(
            List<String> origen
    ){
        if(origen==null||origen.isEmpty()){
            return Collections.emptyList();
        }

        List<String> copia=new ArrayList<>();

        for(String valor:origen){
            if(valor==null)continue;

            String limpio=valor.trim();

            if(limpio.isEmpty())continue;

            if(!copia.contains(limpio)){
                copia.add(limpio);
            }
        }

        return Collections.unmodifiableList(copia);
    }

    private static String limpiar(String texto){
        return texto==null
                ?""
                :texto.trim();
    }

    private static String unir(
            List<String> valores,
            String separador
    ){
        StringBuilder texto=new StringBuilder();

        for(String valor:valores){
            if(valor==null||valor.isEmpty()){
                continue;
            }

            if(texto.length()>0){
                texto.append(separador);
            }

            texto.append(valor);
        }

        return texto.toString();
    }
}