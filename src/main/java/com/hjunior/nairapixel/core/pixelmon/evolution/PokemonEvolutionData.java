package com.hjunior.nairapixel.core.pixelmon.evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonEvolutionData {
    private final String pokemonOrigen;
    private final String formaOrigen;
    private final String destino;
    private final String formaDestino;
    private final String tipo;
    private final Integer nivel;
    private final String objeto;
    private final String intercambioCon;
    private final List<String> condiciones;

    public PokemonEvolutionData(
            String pokemonOrigen,
            String formaOrigen,
            String destino,
            String formaDestino,
            String tipo,
            Integer nivel,
            String objeto,
            String intercambioCon,
            List<String> condiciones
    ){
        this.pokemonOrigen=limpiar(pokemonOrigen);
        this.formaOrigen=limpiar(formaOrigen);
        this.destino=limpiar(destino);
        this.formaDestino=limpiar(formaDestino);
        this.tipo=limpiar(tipo);
        this.nivel=nivel;
        this.objeto=limpiar(objeto);
        this.intercambioCon=limpiar(intercambioCon);
        this.condiciones=copiar(condiciones);
    }

    public String getPokemonOrigen(){return pokemonOrigen;}
    public String getFormaOrigen(){return formaOrigen;}
    public String getDestino(){return destino;}
    public String getFormaDestino(){return formaDestino;}
    public String getTipo(){return tipo;}
    public Integer getNivel(){return nivel;}
    public String getObjeto(){return objeto;}
    public String getIntercambioCon(){return intercambioCon;}
    public List<String> getCondiciones(){return condiciones;}

    public boolean tieneFormaDestino(){return !formaDestino.isEmpty();}
    public boolean tieneNivel(){return nivel!=null;}
    public boolean tieneObjeto(){return !objeto.isEmpty();}
    public boolean tieneIntercambioCon(){return !intercambioCon.isEmpty();}
    public boolean tieneCondiciones(){return !condiciones.isEmpty();}

    private static List<String> copiar(List<String> valores){
        if(valores==null||valores.isEmpty())return Collections.emptyList();

        List<String> copia=new ArrayList<>();

        for(String valor:valores){
            String limpio=limpiar(valor);

            if(!limpio.isEmpty()&&!copia.contains(limpio)){
                copia.add(limpio);
            }
        }

        return Collections.unmodifiableList(copia);
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}