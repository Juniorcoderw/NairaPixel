package com.hjunior.nairapixel.client.dex.model;

import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.core.pixelmon.forms.PokemonFormData;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class NairaDexSummaryProvider {
    private NairaDexSummaryProvider(){}

    public static Optional<NairaDexPokemonSummary> crear(
            PokemonSpeciesData species,
            List<PokemonFormData> formas,
            String formaSeleccionada,
            NairaCollectionSnapshot coleccion
    ){
        if(species==null){
            return Optional.empty();
        }

        PokemonFormData forma=
                buscarForma(
                        formas,
                        formaSeleccionada
                );

        int cantidad=
                coleccion==null
                        ?0
                        :coleccion.getCantidad(
                        species.getNombre()
                );

        int shiny=
                coleccion==null
                        ?0
                        :coleccion.getCantidadShiny(
                        species.getNombre()
                );

        if(forma!=null){
            return Optional.of(
                    new NairaDexPokemonSummary(
                            species.getNumeroDex(),
                            species.getGeneracion(),
                            species.getNombre(),
                            forma.getForma(),
                            forma.getRegion(),
                            categoria(species),
                            forma.isFormaBase(),
                            forma.isTemporal(),
                            cantidad>0,
                            cantidad,
                            shiny,
                            forma.getTipos(),
                            forma.getHabilidades(),
                            forma.getHabilidadesOcultas(),
                            forma.getPS(),
                            forma.getAtaque(),
                            forma.getDefensa(),
                            forma.getAtaqueEspecial(),
                            forma.getDefensaEspecial(),
                            forma.getVelocidad()
                    )
            );
        }

        return Optional.of(
                new NairaDexPokemonSummary(
                        species.getNumeroDex(),
                        species.getGeneracion(),
                        species.getNombre(),
                        species.getFormaBase(),
                        "",
                        categoria(species),
                        true,
                        false,
                        cantidad>0,
                        cantidad,
                        shiny,
                        species.getTipos(),
                        species.getHabilidades(),
                        species.getHabilidadesOcultas(),
                        species.getPS(),
                        species.getAtaque(),
                        species.getDefensa(),
                        species.getAtaqueEspecial(),
                        species.getDefensaEspecial(),
                        species.getVelocidad()
                )
        );
    }

    private static PokemonFormData buscarForma(
            List<PokemonFormData> formas,
            String formaSeleccionada
    ){
        if(formas==null||formas.isEmpty()){
            return null;
        }

        String seleccion=
                normalizarForma(
                        formaSeleccionada
                );

        if(seleccion.isEmpty()){
            for(PokemonFormData forma:formas){
                if(forma!=null&&forma.isFormaBase()){
                    return forma;
                }
            }

            for(PokemonFormData forma:formas){
                if(forma!=null){
                    return forma;
                }
            }

            return null;
        }

        for(PokemonFormData forma:formas){
            if(forma==null)continue;

            if(normalizarForma(
                    forma.getForma()
            ).equals(seleccion)){
                return forma;
            }
        }

        for(PokemonFormData forma:formas){
            if(forma!=null&&forma.isFormaBase()){
                return forma;
            }
        }

        return null;
    }

    private static String categoria(
            PokemonSpeciesData species
    ){
        if(species.isLegendario()){
            return "Legendario";
        }

        if(species.isMitico()){
            return "Mítico";
        }

        if(species.isUltraente()){
            return "Ultraente";
        }

        return "";
    }

    private static String normalizarForma(
            String texto
    ){
        String valor=
                normalizar(texto);

        if(valor.equals("base")){
            return "";
        }

        return valor;
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