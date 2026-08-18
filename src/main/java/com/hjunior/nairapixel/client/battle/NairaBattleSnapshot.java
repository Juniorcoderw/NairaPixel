package com.hjunior.nairapixel.client.battle;

import com.pixelmonmod.pixelmon.api.battles.BattleType;

import java.util.ArrayList;
import java.util.List;

public class NairaBattleSnapshot{
    public NairaBattleContext contexto=NairaBattleContext.UNKNOWN;
    public BattleType formato=BattleType.SINGLE;
    public int turno;

    public String clima="";
    public String terreno="";

    public boolean puedeCambiar;
    public boolean puedeHuir;
    public boolean esperando;
    public boolean espectando;

    public boolean dynamax;
    public int turnosDynamax;
    public boolean gigantamax;
    public boolean mega;
    public boolean zMoves;

    public final List<NairaBattlePokemon> propios=new ArrayList<>();
    public final List<NairaBattlePokemon> aliados=new ArrayList<>();
    public final List<NairaBattlePokemon> rivales=new ArrayList<>();

    public NairaBattlePokemon propioActivo(){
        return propios.isEmpty()?null:propios.get(0);
    }

    public NairaBattlePokemon rivalActivo(){
        return rivales.isEmpty()?null:rivales.get(0);
    }

    public boolean esRaid(){
        return contexto==NairaBattleContext.RAID||formato==BattleType.RAID;
    }

    public boolean esBoss(){
        if(contexto==NairaBattleContext.BOSS)return true;
        for(NairaBattlePokemon rival:rivales){
            if(rival!=null&&rival.esBoss())return true;
        }
        return false;
    }

    public boolean esPvp(){
        return contexto==NairaBattleContext.PVP;
    }

    public boolean esMultiple(){
        return formato==BattleType.DOUBLE||
                formato==BattleType.TRIPLE||
                formato==BattleType.ROTATION||
                formato==BattleType.HORDE||
                aliados.size()>0||
                rivales.size()>1;
    }
}
