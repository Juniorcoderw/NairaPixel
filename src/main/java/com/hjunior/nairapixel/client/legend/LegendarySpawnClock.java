package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;

public final class LegendarySpawnClock {
    private static final long MINUTO_TICKS=1200L;

    private static int ultimoMinuto=-1;
    private static long objetivoTicks=-1L;
    private static boolean sincronizado=false;

    private LegendarySpawnClock(){}

    public static void actualizar(int minutos){
        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null||minutos<0){
            reiniciar();
            return;
        }

        long ahora=mc.level.getDayTime();

        if(ultimoMinuto<0){
            estimarInicial(minutos,ahora);
            ultimoMinuto=minutos;
            return;
        }

        if(minutos==ultimoMinuto){
            return;
        }

        if(minutos>ultimoMinuto){
            iniciarNuevoCiclo(minutos,ahora);
        }else if(minutos==ultimoMinuto-1&&!sincronizado){
            sincronizar(minutos,ahora);
        }

        ultimoMinuto=minutos;
    }

    private static void estimarInicial(
            int minutos,
            long ahora
    ){
        objetivoTicks=
                ahora+
                        minutos*MINUTO_TICKS+
                        MINUTO_TICKS/2;

        sincronizado=false;
    }

    private static void sincronizar(
            int minutos,
            long ahora
    ){
        objetivoTicks=
                ahora+
                        (minutos+1L)*MINUTO_TICKS;

        sincronizado=true;
    }

    private static void iniciarNuevoCiclo(
            int minutos,
            long ahora
    ){
        objetivoTicks=
                ahora+
                        (minutos+1L)*MINUTO_TICKS;

        sincronizado=true;
    }

    public static long getObjetivoTicks(){
        return objetivoTicks;
    }

    public static boolean tieneObjetivo(){
        return objetivoTicks>=0L;
    }

    public static boolean isSincronizado(){
        return sincronizado;
    }

    public static int getUltimoMinuto(){
        return ultimoMinuto;
    }

    public static void reiniciar(){
        ultimoMinuto=-1;
        objetivoTicks=-1L;
        sincronizado=false;
    }
}