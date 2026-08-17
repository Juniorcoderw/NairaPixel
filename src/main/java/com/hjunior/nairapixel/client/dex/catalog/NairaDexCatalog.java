package com.hjunior.nairapixel.client.dex.catalog;

import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.client.dex.state.NairaDexState;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class NairaDexCatalog {
    private NairaDexCatalog(){}

    public static List<PokemonSpeciesData> filtrar(
            List<PokemonSpeciesData> catalogo,
            NairaCollectionSnapshot coleccion,
            String busqueda,
            NairaDexState.FiltroColeccion filtro
    ){
        if(catalogo==null||catalogo.isEmpty()){
            return Collections.emptyList();
        }

        String texto=
                normalizarBusqueda(busqueda);

        NairaDexState.FiltroColeccion filtroReal=
                filtro==null
                        ?NairaDexState.FiltroColeccion.TODOS
                        :filtro;

        List<PokemonSpeciesData> resultado=
                new ArrayList<>();

        for(PokemonSpeciesData pokemon:catalogo){
            if(pokemon==null)continue;

            if(!coincideBusqueda(
                    pokemon,
                    texto
            )){
                continue;
            }

            if(!coincideColeccion(
                    pokemon,
                    coleccion,
                    filtroReal
            )){
                continue;
            }

            resultado.add(pokemon);
        }

        resultado.sort(
                Comparator.comparingInt(
                        PokemonSpeciesData::getNumeroDex
                )
        );

        return Collections.unmodifiableList(resultado);
    }

    private static boolean coincideBusqueda(
            PokemonSpeciesData pokemon,
            String busqueda
    ){
        if(busqueda.isEmpty()){
            return true;
        }

        String nombre=
                normalizar(
                        pokemon.getNombre()
                );

        if(nombre.contains(busqueda)){
            return true;
        }

        String numero=
                String.valueOf(
                        pokemon.getNumeroDex()
                );

        if(numero.startsWith(busqueda)){
            return true;
        }

        String numeroFormateado=
                String.format(
                        Locale.ROOT,
                        "%04d",
                        pokemon.getNumeroDex()
                );

        return numeroFormateado.startsWith(busqueda);
    }

    private static boolean coincideColeccion(
            PokemonSpeciesData pokemon,
            NairaCollectionSnapshot coleccion,
            NairaDexState.FiltroColeccion filtro
    ){
        if(filtro==
                NairaDexState.FiltroColeccion.TODOS){

            return true;
        }

        boolean obtenido=
                coleccion!=null&&
                        coleccion.tiene(
                                pokemon.getNombre()
                        );

        if(filtro==
                NairaDexState.FiltroColeccion.OBTENIDOS){

            return obtenido;
        }

        return !obtenido;
    }

    private static String normalizarBusqueda(
            String texto
    ){
        if(texto==null)return "";

        String limpio=
                texto.trim();

        while(limpio.startsWith("#")){
            limpio=
                    limpio.substring(1)
                            .trim();
        }

        return normalizar(limpio);
    }

    private static String normalizar(String texto){
        if(texto==null)return "";

        String valor=
                texto.toLowerCase(Locale.ROOT);

        StringBuilder resultado=
                new StringBuilder();

        for(int i=0;i<valor.length();i++){
            char c=valor.charAt(i);

            if(Character.isLetterOrDigit(c)){
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}