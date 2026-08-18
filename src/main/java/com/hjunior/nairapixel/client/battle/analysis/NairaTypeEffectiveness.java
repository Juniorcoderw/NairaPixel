package com.hjunior.nairapixel.client.battle.analysis;

import com.pixelmonmod.pixelmon.api.pokemon.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NairaTypeEffectiveness{
    private static final float EPSILON=0.001F;

    private NairaTypeEffectiveness(){}

    public static float calcular(List<Element> defensores,Element atacante){
        if(defensores==null||defensores.isEmpty()||atacante==null)return 1F;
        return Element.getTotalEffectiveness(defensores,atacante);
    }

    public static boolean esInmune(float valor){
        return valor<=EPSILON;
    }

    public static boolean esDebilidad(float valor){
        return valor>1F+EPSILON;
    }

    public static boolean esResistencia(float valor){
        return valor>EPSILON&&valor<1F-EPSILON;
    }

    public static Map<Element,Float> debilidades(List<Element> defensores){
        List<Element> tipos=new ArrayList<>(Element.getAllTypes());
        tipos.sort((a,b)->Float.compare(calcular(defensores,b),calcular(defensores,a)));

        Map<Element,Float> resultado=new LinkedHashMap<>();
        for(Element tipo:tipos){
            float valor=calcular(defensores,tipo);
            if(esDebilidad(valor))resultado.put(tipo,valor);
        }
        return resultado;
    }

    public static Map<Element,Float> resistencias(List<Element> defensores){
        List<Element> tipos=new ArrayList<>(Element.getAllTypes());
        tipos.sort((a,b)->Float.compare(calcular(defensores,a),calcular(defensores,b)));

        Map<Element,Float> resultado=new LinkedHashMap<>();
        for(Element tipo:tipos){
            float valor=calcular(defensores,tipo);
            if(esResistencia(valor))resultado.put(tipo,valor);
        }
        return resultado;
    }

    public static Map<Element,Float> inmunidades(List<Element> defensores){
        Map<Element,Float> resultado=new LinkedHashMap<>();
        for(Element tipo:Element.getAllTypes()){
            float valor=calcular(defensores,tipo);
            if(esInmune(valor))resultado.put(tipo,valor);
        }
        return resultado;
    }
}
