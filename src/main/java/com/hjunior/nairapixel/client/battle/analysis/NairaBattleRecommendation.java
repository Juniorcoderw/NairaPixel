package com.hjunior.nairapixel.client.battle.analysis;

import com.hjunior.nairapixel.client.battle.NairaBattlePokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NairaBattleRecommendation{
    public final List<NairaMoveAnalysis> movimientos;
    public final NairaMoveAnalysis mejorOfensiva;
    public final NairaMoveAnalysis alternativaSegura;

    private NairaBattleRecommendation(
            List<NairaMoveAnalysis> movimientos,
            NairaMoveAnalysis mejorOfensiva,
            NairaMoveAnalysis alternativaSegura){
        this.movimientos=Collections.unmodifiableList(new ArrayList<>(movimientos));
        this.mejorOfensiva=mejorOfensiva;
        this.alternativaSegura=alternativaSegura;
    }

    public static NairaBattleRecommendation analizar(
            NairaBattlePokemon atacante,
            NairaBattlePokemon defensor){

        List<NairaMoveAnalysis> analisis=NairaMoveAnalyzer.analizar(atacante,defensor);
        List<NairaMoveAnalysis> ofensivos=new ArrayList<>();

        for(NairaMoveAnalysis movimiento:analisis){
            if(esCandidato(movimiento))ofensivos.add(movimiento);
        }

        ofensivos.sort(new Comparator<NairaMoveAnalysis>(){
            @Override
            public int compare(NairaMoveAnalysis a,NairaMoveAnalysis b){
                return Double.compare(b.scoreOfensivo,a.scoreOfensivo);
            }
        });

        NairaMoveAnalysis mejor=ofensivos.isEmpty()?null:ofensivos.get(0);
        NairaMoveAnalysis segura=buscarSegura(ofensivos,mejor);

        return new NairaBattleRecommendation(analisis,mejor,segura);
    }

    private static boolean esCandidato(NairaMoveAnalysis a){
        return a!=null&&
                a.disponible&&
                a.ofensivo&&
                !a.limitado&&
                !a.inmune()&&
                a.scoreOfensivo>0D;
    }

    private static NairaMoveAnalysis buscarSegura(
            List<NairaMoveAnalysis> candidatos,
            NairaMoveAnalysis mejor){

        if(mejor==null||candidatos.size()<2)return null;

        List<NairaMoveAnalysis> seguras=new ArrayList<>();
        float efectividadMinima=Math.max(1F,mejor.efectividad/2F);

        for(NairaMoveAnalysis candidato:candidatos){
            if(candidato==mejor)continue;

            boolean masPreciso=candidato.precision()>=mejor.precision()+10;
            boolean masPP=candidato.movimiento.ppMax>=mejor.movimiento.ppMax+10;
            boolean suficientementeFuerte=candidato.scoreSeguro>=mejor.scoreSeguro*0.20D;
            boolean efectividadRazonable=candidato.efectividad+0.001F>=efectividadMinima;

            if((masPreciso||masPP)&&suficientementeFuerte&&efectividadRazonable){
                seguras.add(candidato);
            }
        }

        return seguras.isEmpty()?null:masSegura(seguras);
    }

    private static NairaMoveAnalysis masSegura(List<NairaMoveAnalysis> candidatos){
        candidatos.sort(new Comparator<NairaMoveAnalysis>(){
            @Override
            public int compare(NairaMoveAnalysis a,NairaMoveAnalysis b){
                int precision=Integer.compare(b.precision(),a.precision());
                if(precision!=0)return precision;

                int pp=Integer.compare(b.movimiento.ppMax,a.movimiento.ppMax);
                if(pp!=0)return pp;

                int efectividad=Float.compare(b.efectividad,a.efectividad);
                if(efectividad!=0)return efectividad;

                return Double.compare(b.scoreSeguro,a.scoreSeguro);
            }
        });

        return candidatos.get(0);
    }
}
