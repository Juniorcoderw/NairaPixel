package com.hjunior.nairapixel.client.battle.analysis;

import com.hjunior.nairapixel.client.battle.NairaBattleMove;

public class NairaMoveAnalysis{
    public enum Valoracion{
        NO_DISPONIBLE,
        NULA,
        LIMITADA,
        BAJA,
        MEDIA,
        BUENA,
        MUY_BUENA,
        EXCELENTE,
        ESTADO
    }

    public final NairaBattleMove movimiento;
    public final float efectividad;
    public final boolean stab;
    public final boolean disponible;
    public final boolean ofensivo;
    public final boolean limitado;
    public final double scoreOfensivo;
    public final double scoreSeguro;
    public final Valoracion valoracion;

    public NairaMoveAnalysis(
            NairaBattleMove movimiento,
            float efectividad,
            boolean stab,
            boolean disponible,
            boolean ofensivo,
            boolean limitado,
            double scoreOfensivo,
            double scoreSeguro,
            Valoracion valoracion){
        this.movimiento=movimiento;
        this.efectividad=efectividad;
        this.stab=stab;
        this.disponible=disponible;
        this.ofensivo=ofensivo;
        this.limitado=limitado;
        this.scoreOfensivo=scoreOfensivo;
        this.scoreSeguro=scoreSeguro;
        this.valoracion=valoracion;
    }

    public boolean inmune(){
        return ofensivo&&NairaTypeEffectiveness.esInmune(efectividad);
    }

    public int precision(){
        return movimiento==null?0:movimiento.precisionEfectiva();
    }
}
