package com.hjunior.nairapixel.client.hunt;

public final class NairaHuntCondition {
    public enum Tipo {
        HORARIO,
        BIOMA,
        ALTURA,
        CLIMA,
        LUNA,
        SUELO
    }

    private final Tipo tipo;
    private final String etiqueta;
    private final String actual;
    private final String requerido;
    private final boolean cumple;

    public NairaHuntCondition(
            Tipo tipo,
            String etiqueta,
            String actual,
            String requerido,
            boolean cumple
    ){
        this.tipo=tipo;
        this.etiqueta=etiqueta;
        this.actual=actual;
        this.requerido=requerido;
        this.cumple=cumple;
    }

    public Tipo getTipo(){
        return tipo;
    }

    public String getEtiqueta(){
        return etiqueta;
    }

    public String getActual(){
        return actual;
    }

    public String getRequerido(){
        return requerido;
    }

    public boolean isCumple(){
        return cumple;
    }
}
