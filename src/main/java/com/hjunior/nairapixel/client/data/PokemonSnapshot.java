package com.hjunior.nairapixel.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PokemonSnapshot {
    public UUID uuid;

    public String nombre="";
    public String propietario="";
    public int nivel;

    public final List<String> tipos=new ArrayList<>();

    public String genero="";
    public String tamano="";
    public String categoria="";

    public boolean shiny;

    public String forma="";
    public String paleta="";
    public String boss="";

    public String naturaleza="";
    public String habilidad="";
    public Boolean habilidadOculta;

    public int ivPS=-1;
    public int ivATQ=-1;
    public int ivDEF=-1;
    public int ivATQESP=-1;
    public int ivDEFESP=-1;
    public int ivVEL=-1;
    public double ivTotal=-1;

    public int evPS=-1;
    public int evATQ=-1;
    public int evDEF=-1;
    public int evATQESP=-1;
    public int evDEFESP=-1;
    public int evVEL=-1;

    public Integer amistad;

    public String ot="";
    public String pokeball="";
    public String objeto="";

    public Boolean criable;
    public final List<String> gruposHuevo=new ArrayList<>();

    public final List<String> movimientos=new ArrayList<>();

    public boolean tieneIVs(){
        return ivPS>=0&&
                ivATQ>=0&&
                ivDEF>=0&&
                ivATQESP>=0&&
                ivDEFESP>=0&&
                ivVEL>=0;
    }

    public boolean tieneEVs(){
        return evPS>=0&&
                evATQ>=0&&
                evDEF>=0&&
                evATQESP>=0&&
                evDEFESP>=0&&
                evVEL>=0;
    }

    public boolean tieneCategoria(){
        return categoria!=null&&!categoria.isEmpty();
    }

    public boolean tieneForma(){
        return forma!=null&&!forma.isEmpty();
    }

    public boolean tienePaleta(){
        return paleta!=null&&!paleta.isEmpty();
    }

    public boolean esBoss(){
        return boss!=null&&!boss.isEmpty();
    }

    public boolean tieneNaturaleza(){
        return naturaleza!=null&&!naturaleza.isEmpty();
    }

    public boolean tieneHabilidad(){
        return habilidad!=null&&!habilidad.isEmpty();
    }

    public boolean tieneCrianza(){
        return criable!=null;
    }

    public String tiposTexto(){
        if(tipos.isEmpty())return "—";

        StringBuilder texto=new StringBuilder();

        for(String tipo:tipos){
            if(texto.length()>0)texto.append(" / ");
            texto.append(tipo);
        }

        return texto.toString();
    }

    public String gruposHuevoTexto(){
        if(gruposHuevo.isEmpty())return "—";

        StringBuilder texto=new StringBuilder();

        for(String grupo:gruposHuevo){
            if(texto.length()>0)texto.append(" / ");
            texto.append(grupo);
        }

        return texto.toString();
    }
}