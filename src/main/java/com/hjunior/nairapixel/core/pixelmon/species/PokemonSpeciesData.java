package com.hjunior.nairapixel.core.pixelmon.species;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonSpeciesData {
    private final int numeroDex;
    private final int generacion;
    private final String nombre;
    private final String formaBase;

    private final List<String> tipos;
    private final List<String> habilidades;
    private final List<String> habilidadesOcultas;

    private final int ps;
    private final int ataque;
    private final int defensa;
    private final int ataqueEspecial;
    private final int defensaEspecial;
    private final int velocidad;

    private final boolean legendario;
    private final boolean mitico;
    private final boolean ultraente;

    public PokemonSpeciesData(
            int numeroDex,
            int generacion,
            String nombre,
            String formaBase,
            List<String> tipos,
            List<String> habilidades,
            List<String> habilidadesOcultas,
            int ps,
            int ataque,
            int defensa,
            int ataqueEspecial,
            int defensaEspecial,
            int velocidad,
            boolean legendario,
            boolean mitico,
            boolean ultraente
    ){
        this.numeroDex=numeroDex;
        this.generacion=generacion;
        this.nombre=limpiar(nombre);
        this.formaBase=limpiar(formaBase);
        this.tipos=copiar(tipos);
        this.habilidades=copiar(habilidades);
        this.habilidadesOcultas=copiar(habilidadesOcultas);
        this.ps=ps;
        this.ataque=ataque;
        this.defensa=defensa;
        this.ataqueEspecial=ataqueEspecial;
        this.defensaEspecial=defensaEspecial;
        this.velocidad=velocidad;
        this.legendario=legendario;
        this.mitico=mitico;
        this.ultraente=ultraente;
    }

    public int getNumeroDex(){
        return numeroDex;
    }

    public int getGeneracion(){
        return generacion;
    }

    public String getNombre(){
        return nombre;
    }

    public String getFormaBase(){
        return formaBase;
    }

    public List<String> getTipos(){
        return tipos;
    }

    public List<String> getHabilidades(){
        return habilidades;
    }

    public List<String> getHabilidadesOcultas(){
        return habilidadesOcultas;
    }

    public int getPS(){
        return ps;
    }

    public int getAtaque(){
        return ataque;
    }

    public int getDefensa(){
        return defensa;
    }

    public int getAtaqueEspecial(){
        return ataqueEspecial;
    }

    public int getDefensaEspecial(){
        return defensaEspecial;
    }

    public int getVelocidad(){
        return velocidad;
    }

    public int getBST(){
        return ps+
                ataque+
                defensa+
                ataqueEspecial+
                defensaEspecial+
                velocidad;
    }

    public boolean isLegendario(){
        return legendario;
    }

    public boolean isMitico(){
        return mitico;
    }

    public boolean isUltraente(){
        return ultraente;
    }

    private static List<String> copiar(List<String> valores){
        if(valores==null||valores.isEmpty()){
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(valores)
        );
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}