package com.hjunior.nairapixel.client.hunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NairaHuntSnapshot {
    public enum Estado {
        SIN_OBJETIVO,
        SIN_REGLAS,
        CONDICIONES_INCOMPLETAS,
        ZONA_COMPATIBLE
    }

    private final String pokemon;
    private final String forma;
    private final Estado estado;
    private final int reglasCompatibles;
    private final int totalReglas;
    private final List<NairaHuntCondition> condiciones;
    private final String resumen;

    private NairaHuntSnapshot(
            String pokemon,
            String forma,
            Estado estado,
            int reglasCompatibles,
            int totalReglas,
            List<NairaHuntCondition> condiciones,
            String resumen
    ){
        this.pokemon=pokemon==null?"":pokemon;
        this.forma=forma==null?"":forma;
        this.estado=estado;
        this.reglasCompatibles=reglasCompatibles;
        this.totalReglas=totalReglas;
        this.condiciones=Collections.unmodifiableList(
                new ArrayList<>(
                        condiciones==null
                                ?Collections.emptyList()
                                :condiciones
                )
        );
        this.resumen=resumen==null?"":resumen;
    }

    public static NairaHuntSnapshot sinObjetivo(){
        return new NairaHuntSnapshot(
                "",
                "",
                Estado.SIN_OBJETIVO,
                0,
                0,
                Collections.emptyList(),
                "Sin objetivo activo"
        );
    }

    public static NairaHuntSnapshot crear(
            String pokemon,
            String forma,
            Estado estado,
            int reglasCompatibles,
            int totalReglas,
            List<NairaHuntCondition> condiciones,
            String resumen
    ){
        return new NairaHuntSnapshot(
                pokemon,
                forma,
                estado,
                reglasCompatibles,
                totalReglas,
                condiciones,
                resumen
        );
    }

    public String getPokemon(){
        return pokemon;
    }

    public String getForma(){
        return forma;
    }

    public Estado getEstado(){
        return estado;
    }

    public int getReglasCompatibles(){
        return reglasCompatibles;
    }

    public int getTotalReglas(){
        return totalReglas;
    }

    public List<NairaHuntCondition> getCondiciones(){
        return condiciones;
    }

    public String getResumen(){
        return resumen;
    }

    public boolean tieneObjetivo(){
        return estado!=Estado.SIN_OBJETIVO;
    }

    public boolean isZonaCompatible(){
        return estado==Estado.ZONA_COMPATIBLE;
    }

    public List<NairaHuntCondition> getFaltantes(){
        List<NairaHuntCondition> faltantes=
                new ArrayList<>();

        for(NairaHuntCondition condicion:
                condiciones){

            if(condicion!=null&&
                    !condicion.isCumple()){

                faltantes.add(
                        condicion
                );
            }
        }

        return Collections.unmodifiableList(
                faltantes
        );
    }
}
