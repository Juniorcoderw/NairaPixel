package com.hjunior.nairapixel.core.pixelmon.moves;

public final class MoveLearnSource {
    private final String metodo;
    private final Integer nivel;
    private final Integer generacion;
    private final Integer numero;

    public MoveLearnSource(
            String metodo,
            Integer nivel,
            Integer generacion,
            Integer numero
    ){
        this.metodo=metodo==null?"":metodo.trim();
        this.nivel=nivel;
        this.generacion=generacion;
        this.numero=numero;
    }

    public String getMetodo(){return metodo;}
    public Integer getNivel(){return nivel;}
    public Integer getGeneracion(){return generacion;}
    public Integer getNumero(){return numero;}
}