package com.hjunior.nairapixel.client.legend;

public class LegendaryEvent {
    private final String pokemon;
    private final String bioma;
    private final String jugador;

    public LegendaryEvent(
            String pokemon,
            String bioma,
            String jugador
    ){
        this.pokemon=limpiar(pokemon);
        this.bioma=limpiar(bioma);
        this.jugador=limpiar(jugador);
    }

    public String getPokemon(){
        return pokemon;
    }

    public String getBioma(){
        return bioma;
    }

    public String getJugador(){
        return jugador;
    }

    private static String limpiar(String texto){
        return texto==null
                ?""
                :texto.trim();
    }
}