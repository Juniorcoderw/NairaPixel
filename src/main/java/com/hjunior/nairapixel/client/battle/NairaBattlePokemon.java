package com.hjunior.nairapixel.client.battle;

import com.pixelmonmod.pixelmon.api.pokemon.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NairaBattlePokemon{
    public UUID uuid;
    public String nombre="";
    public String forma="";
    public int posicion=-1;
    public int nivel;
    public final List<Element> tipos=new ArrayList<>();

    public double hp=-1D;
    public double hpMax=-1D;
    public int estado=-1;

    public boolean boss;
    public String bossTier="";
    public int escudos;
    public int escudosMax;
    public boolean escudoPerdido;

    public final List<NairaBattleMove> movimientos=new ArrayList<>();

    public boolean tieneTipo(Element tipo){
        return tipo!=null&&tipos.contains(tipo);
    }

    public boolean tieneHP(){
        return hp>=0D&&hpMax>0D;
    }

    public double porcentajeHP(){
        if(!tieneHP())return -1D;
        return Math.max(0D,Math.min(1D,hp/hpMax));
    }

    public boolean debilitado(){
        return tieneHP()&&hp<=0D;
    }

    public boolean esBoss(){
        return boss;
    }

    public boolean tieneEscudos(){
        return escudosMax>0;
    }
}
