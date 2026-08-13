package com.hjunior.nairapixel.client.legend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum PixelmonTimePeriod {
    DAWN("Dawn","Amanecer",
            new int[][]{{22500,24000},{0,300}}),

    MORNING("Morning","Mañana",
            new int[][]{{22500,24000},{0,6000}}),

    DAY("Day","Día",
            new int[][]{{0,12000}}),

    MIDDAY("Midday","Mediodía",
            new int[][]{{5500,6500}}),

    AFTERNOON("Afternoon","Tarde",
            new int[][]{{6000,12000}}),

    DUSK("Dusk","Anochecer",
            new int[][]{{12000,13800}}),

    NIGHT("Night","Noche",
            new int[][]{{13450,22550}}),

    MIDNIGHT("Midnight","Medianoche",
            new int[][]{{17500,18500}});

    private final String key;
    private final String nombre;
    private final int[][] rangos;

    PixelmonTimePeriod(
            String key,
            String nombre,
            int[][] rangos
    ){
        this.key=key;
        this.nombre=nombre;
        this.rangos=rangos;
    }

    public String getKey(){
        return key;
    }

    public String getNombre(){
        return nombre;
    }

    public boolean estaActivo(long ticks){
        long valor=Math.floorMod(ticks,24000L);

        for(int[] rango:rangos){
            if(valor>=rango[0]&&valor<rango[1]){
                return true;
            }
        }

        return false;
    }

    public boolean intersecta(long inicio,long fin){
        inicio=Math.floorMod(inicio,24000L);
        fin=Math.floorMod(fin,24000L);

        if(inicio==fin){
            return estaActivo(inicio);
        }

        if(inicio<fin){
            return intersectaRango(inicio,fin);
        }

        return intersectaRango(inicio,24000L)||
                intersectaRango(0,fin);
    }

    private boolean intersectaRango(long inicio,long fin){
        for(int[] rango:rangos){
            if(inicio<rango[1]&&fin>rango[0]){
                return true;
            }
        }

        return false;
    }

    public static PixelmonTimePeriod fromKey(String key){
        if(key==null||key.trim().isEmpty())return null;

        String buscado=key
                .trim()
                .toLowerCase(Locale.ROOT);

        for(PixelmonTimePeriod periodo:values()){
            if(periodo.key
                    .toLowerCase(Locale.ROOT)
                    .equals(buscado)){

                return periodo;
            }

            if(periodo.name()
                    .toLowerCase(Locale.ROOT)
                    .equals(buscado)){

                return periodo;
            }
        }

        return null;
    }

    public static List<PixelmonTimePeriod> getActivos(long ticks){
        List<PixelmonTimePeriod> resultado=new ArrayList<>();

        for(PixelmonTimePeriod periodo:values()){
            if(periodo.estaActivo(ticks)){
                resultado.add(periodo);
            }
        }

        return Collections.unmodifiableList(resultado);
    }

    public static List<PixelmonTimePeriod> getEnVentana(
            long inicio,
            long fin
    ){
        List<PixelmonTimePeriod> resultado=new ArrayList<>();

        for(PixelmonTimePeriod periodo:values()){
            if(periodo.intersecta(inicio,fin)){
                resultado.add(periodo);
            }
        }

        return Collections.unmodifiableList(resultado);
    }

    public static String formatear(
            List<PixelmonTimePeriod> periodos
    ){
        if(periodos==null||periodos.isEmpty()){
            return "—";
        }

        StringBuilder texto=new StringBuilder();

        for(PixelmonTimePeriod periodo:periodos){
            if(periodo==null)continue;

            if(texto.length()>0){
                texto.append(" · ");
            }

            texto.append(periodo.getNombre());
        }

        return texto.length()==0
                ?"—"
                :texto.toString();
    }
}