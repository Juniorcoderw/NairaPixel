package com.hjunior.nairapixel.client.dex.controller;

import com.hjunior.nairapixel.client.collection.NairaCollectionService;
import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.client.collection.OwnedPokemonData;
import com.hjunior.nairapixel.client.dex.catalog.NairaDexCatalog;
import com.hjunior.nairapixel.client.dex.model.NairaDexPokemonSummary;
import com.hjunior.nairapixel.client.dex.model.NairaDexSummaryProvider;
import com.hjunior.nairapixel.client.dex.state.NairaDexState;
import com.hjunior.nairapixel.core.pixelmon.PixelmonDataService;
import com.hjunior.nairapixel.core.pixelmon.breeding.PokemonBreedingData;
import com.hjunior.nairapixel.core.pixelmon.evolution.PokemonEvolutionData;
import com.hjunior.nairapixel.core.pixelmon.forms.PokemonFormData;
import com.hjunior.nairapixel.core.pixelmon.moves.PokemonMoveData;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class NairaDexController {
    private static final NairaDexController INSTANCIA=
            new NairaDexController();

    private final NairaDexState state=
            NairaDexState.get();

    private NairaDexController(){}

    public static NairaDexController get(){
        return INSTANCIA;
    }

    public NairaDexState getState(){
        return state;
    }

    public void seleccionarPokemon(String pokemon){
        state.seleccionarPokemon(pokemon);
    }

    public void seleccionarPokemon(
            String pokemon,
            String forma
    ){
        state.seleccionarPokemon(
                pokemon,
                forma
        );
    }

    public void seleccionarForma(String forma){
        state.seleccionarForma(forma);
    }

    public void seleccionarPestana(
            NairaDexState.Pestana pestana
    ){
        state.setPestana(pestana);
    }

    public void seleccionarSeccion(
            NairaDexState.Seccion seccion
    ){
        state.setSeccion(seccion);
    }

    public void setBusqueda(String busqueda){
        state.setBusqueda(busqueda);
    }

    public void setFiltroColeccion(
            NairaDexState.FiltroColeccion filtro
    ){
        state.setFiltroColeccion(filtro);
    }

    public void alternarVistaCatalogo(){
        state.alternarVistaCatalogo();
    }

    public Optional<PokemonSpeciesData> getPokemonActual(){
        if(!state.tienePokemonSeleccionado()){
            return Optional.empty();
        }

        return PixelmonDataService.getPokemon(
                state.getPokemonSeleccionado()
        );
    }

    public Optional<NairaDexPokemonSummary> getResumenActual(){
        Optional<PokemonSpeciesData> pokemon=
                getPokemonActual();

        if(!pokemon.isPresent()){
            return Optional.empty();
        }

        return NairaDexSummaryProvider.crear(
                pokemon.get(),
                getFormasActuales(),
                state.getFormaSeleccionada(),
                getColeccion()
        );
    }

    public List<PokemonFormData> getFormasActuales(){
        if(!state.tienePokemonSeleccionado()){
            return Collections.emptyList();
        }

        return PixelmonDataService.getFormas(
                state.getPokemonSeleccionado()
        );
    }

    public List<PokemonSpawnRule> getSpawnsActuales(){
        if(!state.tienePokemonSeleccionado()){
            return Collections.emptyList();
        }

        return PixelmonDataService.getSpawns(
                state.getPokemonSeleccionado(),
                state.getFormaSeleccionada()
        );
    }

    public List<PokemonMoveData> getMovimientosActuales(){
        if(!state.tienePokemonSeleccionado()){
            return Collections.emptyList();
        }

        return PixelmonDataService.getMovimientos(
                state.getPokemonSeleccionado(),
                state.getFormaSeleccionada()
        );
    }

    public List<PokemonEvolutionData> getEvolucionesActuales(){
        if(!state.tienePokemonSeleccionado()){
            return Collections.emptyList();
        }

        return PixelmonDataService.getEvoluciones(
                state.getPokemonSeleccionado(),
                state.getFormaSeleccionada()
        );
    }

    public Optional<PokemonBreedingData> getCrianzaActual(){
        if(!state.tienePokemonSeleccionado()){
            return Optional.empty();
        }

        return PixelmonDataService.getCrianza(
                state.getPokemonSeleccionado(),
                state.getFormaSeleccionada()
        );
    }

    public List<PokemonSpeciesData> getCatalogoCompleto(){
        return PixelmonDataService.getPokemon();
    }

    public List<PokemonSpeciesData> getCatalogoActual(){
        return NairaDexCatalog.filtrar(
                PixelmonDataService.getPokemon(),
                getColeccion(),
                state.getBusqueda(),
                state.getFiltroColeccion()
        );
    }

    public int getCantidadPokemonDex(){
        return PixelmonDataService.getCantidadPokemon();
    }

    public int getCantidadReglasSpawn(){
        return PixelmonDataService.getCantidadReglasSpawn();
    }

    public NairaCollectionSnapshot getColeccion(){
        return NairaCollectionService.getSnapshot();
    }

    public boolean coleccionSincronizada(){
        return NairaCollectionService.estaSincronizada();
    }

    public boolean tengoPokemonActual(){
        if(!state.tienePokemonSeleccionado()){
            return false;
        }

        return getColeccion().tiene(
                state.getPokemonSeleccionado()
        );
    }

    public int getCantidadPokemonActual(){
        if(!state.tienePokemonSeleccionado()){
            return 0;
        }

        return getColeccion().getCantidad(
                state.getPokemonSeleccionado()
        );
    }

    public List<OwnedPokemonData> getEjemplaresActuales(){
        if(!state.tienePokemonSeleccionado()){
            return Collections.emptyList();
        }

        return getColeccion().getEjemplares(
                state.getPokemonSeleccionado()
        );
    }

    public int getTotalColeccion(){
        return getColeccion().getTotalPokemon();
    }

    public int getEspeciesColeccion(){
        return getColeccion().getEspeciesDistintas();
    }

    public int getShinyColeccion(){
        return getColeccion().getTotalShiny();
    }

    public void limpiarSeleccion(){
        state.limpiarSeleccion();
    }
}