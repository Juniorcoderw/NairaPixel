package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

public class MinecraftTimeReader {
    private static final long TICKS_DIA=24000L;
    private static final long TICKS_POR_SEGUNDO=20L;

    public static long getDayTicks(){
        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null)return -1;

        return Math.floorMod(
                mc.level.getDayTime(),
                TICKS_DIA
        );
    }

    public static String getHoraActual(){
        long ticks=getDayTicks();

        return ticks<0
                ?"--:--"
                :formatearHora(ticks);
    }

    public static List<PixelmonTimePeriod> getPeriodosActuales(){
        long ticks=getDayTicks();

        if(ticks<0){
            return Collections.emptyList();
        }

        return PixelmonTimePeriod.getActivos(ticks);
    }

    public static String getPeriodosActualesTexto(){
        return PixelmonTimePeriod.formatear(
                getPeriodosActuales()
        );
    }

    public static boolean esPeriodoActivo(
            String key
    ){
        if(key==null||
                key.trim().isEmpty()){

            return true;
        }

        long ticks=getDayTicks();

        if(ticks<0){
            return false;
        }

        PixelmonTimePeriod periodo=
                PixelmonTimePeriod.fromKey(key);

        return periodo!=null&&
                periodo.estaActivo(ticks);
    }

    public static VentanaTiempo calcularVentana(
            int segundosMin,
            int segundosMax
    ){
        long actual=getDayTicks();

        if(actual<0)return null;

        int minimo=Math.max(0,segundosMin);
        int maximo=Math.max(0,segundosMax);

        if(minimo>maximo){
            int temp=minimo;
            minimo=maximo;
            maximo=temp;
        }

        long tickMinimo=Math.floorMod(
                actual+(minimo*TICKS_POR_SEGUNDO),
                TICKS_DIA
        );

        long tickMaximo=Math.floorMod(
                actual+(maximo*TICKS_POR_SEGUNDO),
                TICKS_DIA
        );

        return new VentanaTiempo(
                actual,
                tickMinimo,
                tickMaximo,
                minimo,
                maximo
        );
    }

    public static String formatearHora(long ticks){
        long valor=Math.floorMod(
                ticks,
                TICKS_DIA
        );

        long desdeMedianoche=
                Math.floorMod(
                        valor+6000L,
                        TICKS_DIA
                );

        int minutosTotales=(int)(
                desdeMedianoche*1440L/TICKS_DIA
        );

        int hora=(minutosTotales/60)%24;
        int minuto=minutosTotales%60;

        return String.format(
                "%02d:%02d",
                hora,
                minuto
        );
    }

    public static class VentanaTiempo {
        private final long actual;
        private final long minimo;
        private final long maximo;

        private final int segundosMin;
        private final int segundosMax;

        public VentanaTiempo(
                long actual,
                long minimo,
                long maximo,
                int segundosMin,
                int segundosMax
        ){
            this.actual=Math.floorMod(actual,TICKS_DIA);
            this.minimo=Math.floorMod(minimo,TICKS_DIA);
            this.maximo=Math.floorMod(maximo,TICKS_DIA);

            this.segundosMin=segundosMin;
            this.segundosMax=segundosMax;
        }

        public long getActual(){
            return actual;
        }

        public long getMinimo(){
            return minimo;
        }

        public long getMaximo(){
            return maximo;
        }

        public int getSegundosMin(){
            return segundosMin;
        }

        public int getSegundosMax(){
            return segundosMax;
        }

        public String getHoraActual(){
            return formatearHora(actual);
        }

        public String getHoraMinima(){
            return formatearHora(minimo);
        }

        public String getHoraMaxima(){
            return formatearHora(maximo);
        }

        public String getTextoVentana(){
            return getHoraMinima()+
                    " - "+
                    getHoraMaxima();
        }

        public List<PixelmonTimePeriod> getPeriodos(){
            return PixelmonTimePeriod.getEnVentana(
                    minimo,
                    maximo
            );
        }

        public String getPeriodosTexto(){
            return PixelmonTimePeriod.formatear(
                    getPeriodos()
            );
        }
    }
}