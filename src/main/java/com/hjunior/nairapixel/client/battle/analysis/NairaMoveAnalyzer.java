package com.hjunior.nairapixel.client.battle.analysis;

import com.hjunior.nairapixel.client.battle.NairaBattleMove;
import com.hjunior.nairapixel.client.battle.NairaBattlePokemon;

import java.util.ArrayList;
import java.util.List;

public final class NairaMoveAnalyzer{
    private NairaMoveAnalyzer(){}

    public static List<NairaMoveAnalysis> analizar(
            NairaBattlePokemon atacante,
            NairaBattlePokemon defensor){

        List<NairaMoveAnalysis> resultado=new ArrayList<>();
        if(atacante==null||defensor==null)return resultado;

        for(NairaBattleMove movimiento:atacante.movimientos){
            if(movimiento==null)continue;
            resultado.add(analizarMovimiento(atacante,defensor,movimiento));
        }

        return resultado;
    }

    public static NairaMoveAnalysis analizarMovimiento(
            NairaBattlePokemon atacante,
            NairaBattlePokemon defensor,
            NairaBattleMove movimiento){

        boolean disponible=movimiento.disponible();
        boolean ofensivo=movimiento.esOfensivo();
        boolean limitado=ofensivo&&movimiento.potencia<=0;
        boolean stab=ofensivo&&movimiento.tipo!=null&&atacante.tieneTipo(movimiento.tipo);

        float efectividad=ofensivo
                ?NairaTypeEffectiveness.calcular(defensor.tipos,movimiento.tipo)
                :1F;

        if(!disponible){
            return nuevo(movimiento,efectividad,stab,false,ofensivo,limitado,0D,0D,
                    NairaMoveAnalysis.Valoracion.NO_DISPONIBLE);
        }

        if(movimiento.esEstado()){
            return nuevo(movimiento,1F,false,true,false,false,0D,0D,
                    NairaMoveAnalysis.Valoracion.ESTADO);
        }

        if(NairaTypeEffectiveness.esInmune(efectividad)){
            return nuevo(movimiento,efectividad,stab,true,true,false,0D,0D,
                    NairaMoveAnalysis.Valoracion.NULA);
        }

        if(limitado){
            return nuevo(movimiento,efectividad,stab,true,true,true,0D,0D,
                    NairaMoveAnalysis.Valoracion.LIMITADA);
        }

        double stabFactor=stab?1.5D:1D;
        double precision=movimiento.precisionEfectiva()/100D;
        double impacto=movimiento.potencia*efectividad*stabFactor;

        double scoreOfensivo=impacto*(0.70D+0.30D*precision);
        double ppFactor=0.85D+0.15D*movimiento.proporcionPP();
        double scoreSeguro=impacto*precision*precision*ppFactor;

        return nuevo(
                movimiento,
                efectividad,
                stab,
                true,
                true,
                false,
                scoreOfensivo,
                scoreSeguro,
                valorar(scoreOfensivo)
        );
    }

    private static NairaMoveAnalysis nuevo(
            NairaBattleMove movimiento,
            float efectividad,
            boolean stab,
            boolean disponible,
            boolean ofensivo,
            boolean limitado,
            double scoreOfensivo,
            double scoreSeguro,
            NairaMoveAnalysis.Valoracion valoracion){

        return new NairaMoveAnalysis(
                movimiento,
                efectividad,
                stab,
                disponible,
                ofensivo,
                limitado,
                scoreOfensivo,
                scoreSeguro,
                valoracion
        );
    }

    private static NairaMoveAnalysis.Valoracion valorar(double score){
        if(score>=350D)return NairaMoveAnalysis.Valoracion.EXCELENTE;
        if(score>=220D)return NairaMoveAnalysis.Valoracion.MUY_BUENA;
        if(score>=130D)return NairaMoveAnalysis.Valoracion.BUENA;
        if(score>=70D)return NairaMoveAnalysis.Valoracion.MEDIA;
        return NairaMoveAnalysis.Valoracion.BAJA;
    }
}
