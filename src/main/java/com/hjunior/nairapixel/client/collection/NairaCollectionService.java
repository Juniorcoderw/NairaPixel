package com.hjunior.nairapixel.client.collection;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;

import java.util.ArrayList;
import java.util.List;

public final class NairaCollectionService {
    private static NairaCollectionSnapshot snapshot=
            NairaCollectionSnapshot.vacio();

    private static long ultimaSincronizacion;

    private NairaCollectionService(){}

    public static void actualizar(
            PCStorage pc,
            PlayerPartyStorage party
    ){
        List<OwnedPokemonData> resultado=
                new ArrayList<>();

        cargarPC(pc,resultado);
        cargarEquipo(party,resultado);

        snapshot=new NairaCollectionSnapshot(resultado);
        ultimaSincronizacion=System.currentTimeMillis();
    }

    public static NairaCollectionSnapshot getSnapshot(){
        return snapshot;
    }

    public static boolean estaSincronizada(){
        return ultimaSincronizacion>0;
    }

    public static long getUltimaSincronizacion(){
        return ultimaSincronizacion;
    }

    public static void limpiar(){
        snapshot=NairaCollectionSnapshot.vacio();
        ultimaSincronizacion=0;
    }

    private static void cargarPC(
            PCStorage pc,
            List<OwnedPokemonData> destino
    ){
        if(pc==null)return;

        for(int cajaIndex=0;
            cajaIndex<pc.getBoxCount();
            cajaIndex++){

            PCBox caja=pc.getBox(cajaIndex);

            if(caja==null)continue;

            Pokemon[] pokemonCaja=caja.getAll();

            if(pokemonCaja==null)continue;

            for(int slotIndex=0;
                slotIndex<pokemonCaja.length;
                slotIndex++){

                Pokemon pokemon=pokemonCaja[slotIndex];

                if(pokemon==null)continue;

                destino.add(
                        crear(
                                pokemon,
                                OwnedPokemonData.Ubicacion.PC,
                                cajaIndex+1,
                                slotIndex+1
                        )
                );
            }
        }
    }

    private static void cargarEquipo(
            PlayerPartyStorage party,
            List<OwnedPokemonData> destino
    ){
        if(party==null)return;

        Pokemon[] equipo=party.getAll();

        if(equipo==null)return;

        for(int slotIndex=0;
            slotIndex<equipo.length;
            slotIndex++){

            Pokemon pokemon=equipo[slotIndex];

            if(pokemon==null)continue;

            destino.add(
                    crear(
                            pokemon,
                            OwnedPokemonData.Ubicacion.EQUIPO,
                            0,
                            slotIndex+1
                    )
            );
        }
    }

    private static OwnedPokemonData crear(
            Pokemon pokemon,
            OwnedPokemonData.Ubicacion ubicacion,
            int caja,
            int slot
    ){
        String especie="";

        if(pokemon.getSpecies()!=null){
            especie=pokemon.getSpecies().getName();
        }

        String forma="";

        if(pokemon.getForm()!=null){
            forma=pokemon.getForm().getName();
        }

        return new OwnedPokemonData(
                pokemon.getUUID(),
                especie,
                forma,
                pokemon.getPokemonLevel(),
                pokemon.isShiny(),
                ubicacion,
                caja,
                slot
        );
    }
}