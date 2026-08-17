package com.hjunior.nairapixel.client.dex.catalog;

import com.hjunior.nairapixel.core.pixelmon.forms.PokemonFormData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NairaDexFormCatalog {
    private NairaDexFormCatalog(){}

    public static List<PokemonFormData> getPrincipales(
            List<PokemonFormData> formas
    ){
        if(formas==null||formas.isEmpty()){
            return Collections.emptyList();
        }

        List<PokemonFormData> resultado=
                new ArrayList<>();

        for(PokemonFormData forma:formas){
            if(forma==null)continue;

            if(!forma.isTemporal()){
                resultado.add(forma);
            }
        }

        return Collections.unmodifiableList(resultado);
    }

    public static List<PokemonFormData> getTemporales(
            List<PokemonFormData> formas
    ){
        if(formas==null||formas.isEmpty()){
            return Collections.emptyList();
        }

        List<PokemonFormData> resultado=
                new ArrayList<>();

        for(PokemonFormData forma:formas){
            if(forma==null)continue;

            if(forma.isTemporal()){
                resultado.add(forma);
            }
        }

        return Collections.unmodifiableList(resultado);
    }
}