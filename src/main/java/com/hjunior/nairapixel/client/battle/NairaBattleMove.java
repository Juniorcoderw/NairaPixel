package com.hjunior.nairapixel.client.battle;

import com.pixelmonmod.pixelmon.api.battles.AttackCategory;
import com.pixelmonmod.pixelmon.api.pokemon.Element;

public class NairaBattleMove{
    public String nombre="";
    public String nombreIngles="";
    public Element tipo;
    public AttackCategory categoria;
    public int potencia;
    public int precision;
    public int pp;
    public int ppMax;
    public boolean nuncaFalla;
    public boolean deshabilitado;

    public boolean esEstado(){
        return categoria==AttackCategory.STATUS;
    }

    public boolean esOfensivo(){
        return categoria!=null&&categoria!=AttackCategory.STATUS;
    }

    public boolean disponible(){
        return !deshabilitado&&pp>0;
    }

    public int precisionEfectiva(){
        if(nuncaFalla||precision<=0)return 100;
        return Math.min(100,precision);
    }

    public double proporcionPP(){
        if(ppMax<=0)return 1D;
        return Math.max(0D,Math.min(1D,(double)pp/ppMax));
    }
}
