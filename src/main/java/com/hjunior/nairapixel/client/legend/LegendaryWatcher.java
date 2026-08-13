package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(
        modid="nairapixel",
        value=Dist.CLIENT,
        bus=Mod.EventBusSubscriber.Bus.FORGE
)
public final class LegendaryWatcher {
    private static final Logger LOGGER=
            LogManager.getLogger("NairaLegend");

    private static final int INTERVALO_LECTURA_TICKS=20;
    private static final int MAX_LECTURAS_PERDIDAS=5;

    private static final long GRACIA_RESULTADO_MS=3000L;
    private static final long DURACION_SPAWN_MS=5000L;
    private static final long DURACION_SIN_SPAWN_MS=3000L;

    private static int ticks=0;
    private static int ultimoContador=-1;
    private static int proximoContador=-1;
    private static int lecturasPerdidas=0;

    private static Estado estado=Estado.DESCONOCIDO;
    private static Resultado resultado=Resultado.NINGUNO;

    private static LegendaryEvent ultimoSpawn;

    private static boolean esperandoResultado=false;
    private static long graciaHastaMs=0L;
    private static long resultadoHastaMs=0L;

    private static Object mundoActual;

    private LegendaryWatcher(){}

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ){
        if(event.phase!=TickEvent.Phase.END){
            return;
        }

        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null){

            reiniciarSesion();
            return;
        }

        if(mundoActual!=mc.level){
            iniciarSesion(mc.level);
        }

        long ahora=
                System.currentTimeMillis();

        actualizarResultado(ahora);

        ticks++;

        if(ticks<INTERVALO_LECTURA_TICKS){
            return;
        }

        ticks=0;

        int contador=
                LegendaryScoreboardReader
                        .leerMinutos();

        if(contador<0){
            procesarLecturaPerdida();
            return;
        }

        lecturasPerdidas=0;

        procesarContador(
                contador,
                ahora
        );
    }

    private static void procesarContador(
            int contador,
            long ahora
    ){
        if(ultimoContador<0){
            ultimoContador=contador;
            actualizarEstado(contador);
            return;
        }

        if(esNuevoCiclo(contador)){
            proximoContador=contador;
            ultimoContador=contador;

            iniciarVerificacion(ahora);
            return;
        }

        ultimoContador=contador;

        if(!esperandoResultado){
            actualizarEstado(contador);
        }
    }

    private static boolean esNuevoCiclo(
            int contador
    ){
        if(contador<=0){
            return false;
        }

        if(ultimoContador==0){
            return true;
        }

        return estado==Estado.INMINENTE&&
                contador>ultimoContador;
    }

    private static void actualizarEstado(
            int contador
    ){
        Estado nuevo;

        if(contador<0){
            nuevo=Estado.DESCONOCIDO;
        }else if(contador==0){
            nuevo=Estado.INMINENTE;
        }else if(contador==1){
            nuevo=Estado.PREPARACION;
        }else{
            nuevo=Estado.NORMAL;
        }

        cambiarEstado(nuevo);
    }

    private static void iniciarVerificacion(
            long ahora
    ){
        esperandoResultado=true;

        graciaHastaMs=
                ahora+
                        GRACIA_RESULTADO_MS;

        resultado=
                Resultado.VERIFICANDO;

        resultadoHastaMs=0L;
        ultimoSpawn=null;

        cambiarEstado(
                Estado.NUEVO_CICLO
        );

        log(
                "Verificando resultado | proximo ciclo "+
                        proximoContador+
                        "m"
        );
    }

    private static void actualizarResultado(
            long ahora
    ){
        if(esperandoResultado&&
                ahora>=graciaHastaMs){

            registrarSinSpawn(ahora);
        }

        if(!esperandoResultado&&
                resultado!=Resultado.NINGUNO&&
                resultado!=Resultado.VERIFICANDO&&
                resultadoHastaMs>0L&&
                ahora>=resultadoHastaMs){

            resultado=
                    Resultado.NINGUNO;

            resultadoHastaMs=0L;
            ultimoSpawn=null;
        }
    }

    public static void registrarSpawn(
            LegendaryEvent evento
    ){
        if(evento==null){
            return;
        }

        Minecraft mc=
                Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null){
            return;
        }

        long ahora=
                System.currentTimeMillis();

        int contador=
                LegendaryScoreboardReader
                        .leerMinutos();

        if(!esperandoResultado){
            if(!detectarTransicionDesdeChat(
                    contador,
                    ahora
            )){
                return;
            }
        }

        confirmarSpawn(
                evento,
                contador,
                ahora
        );
    }

    private static boolean detectarTransicionDesdeChat(
            int contador,
            long ahora
    ){
        if(estado!=Estado.INMINENTE){
            return false;
        }

        if(ultimoContador!=0){
            return false;
        }

        if(contador<=0){
            return false;
        }

        proximoContador=contador;
        ultimoContador=contador;

        iniciarVerificacion(ahora);

        log(
                "Cambio de ciclo detectado desde chat | "+
                        contador+
                        "m"
        );

        return true;
    }

    private static void confirmarSpawn(
            LegendaryEvent evento,
            int contador,
            long ahora
    ){
        esperandoResultado=false;
        graciaHastaMs=0L;

        resultado=
                Resultado.SPAWN;

        resultadoHastaMs=
                ahora+
                        DURACION_SPAWN_MS;

        ultimoSpawn=evento;

        if(contador>=0){
            ultimoContador=contador;
            actualizarEstado(contador);
        }

        log(
                "Spawn confirmado | "+
                        evento.getPokemon()+
                        " | "+
                        evento.getBioma()+
                        " | cerca de "+
                        evento.getJugador()
        );
    }

    private static void registrarSinSpawn(
            long ahora
    ){
        esperandoResultado=false;
        graciaHastaMs=0L;

        resultado=
                Resultado.SIN_SPAWN;

        resultadoHastaMs=
                ahora+
                        DURACION_SIN_SPAWN_MS;

        ultimoSpawn=null;

        actualizarEstado(
                ultimoContador
        );

        log(
                "Sin spawn | proximo ciclo "+
                        proximoContador+
                        "m"
        );
    }

    private static void procesarLecturaPerdida(){
        lecturasPerdidas++;

        if(lecturasPerdidas<=
                MAX_LECTURAS_PERDIDAS){
            return;
        }

        ultimoContador=-1;
        proximoContador=-1;

        esperandoResultado=false;
        graciaHastaMs=0L;

        resultado=Resultado.NINGUNO;
        resultadoHastaMs=0L;
        ultimoSpawn=null;

        cambiarEstado(
                Estado.DESCONOCIDO
        );

        LegendarySpawnClock.reiniciar();
    }

    private static void iniciarSesion(
            Object mundo
    ){
        reiniciarEstado();

        mundoActual=mundo;

        int cantidad=
                PixelmonLegendaryProvider
                        .getLegendarios()
                        .size();

        log(
                "Sesion iniciada | legendarios "+
                        cantidad
        );
    }

    private static void reiniciarSesion(){
        if(mundoActual==null&&
                ultimoContador<0&&
                estado==Estado.DESCONOCIDO){
            return;
        }

        reiniciarEstado();

        mundoActual=null;

        LegendarySpawnClock.reiniciar();
        LegendaryKeyHandler.contraer();
    }

    private static void reiniciarEstado(){
        ticks=0;

        ultimoContador=-1;
        proximoContador=-1;
        lecturasPerdidas=0;

        estado=Estado.DESCONOCIDO;
        resultado=Resultado.NINGUNO;

        ultimoSpawn=null;

        esperandoResultado=false;
        graciaHastaMs=0L;
        resultadoHastaMs=0L;
    }

    private static void cambiarEstado(
            Estado nuevo
    ){
        if(nuevo==null||
                estado==nuevo){
            return;
        }

        estado=nuevo;

        if(nuevo==Estado.PREPARACION){
            log(
                    "Preparacion | 1m"
            );
        }else if(nuevo==Estado.INMINENTE){
            log(
                    "Inminente | 0m"
            );
        }
    }

    private static void log(
            String mensaje
    ){
        LOGGER.info(mensaje);
    }

    public static Estado getEstado(){
        return estado;
    }

    public static Resultado getResultado(){
        return resultado;
    }

    public static int getContador(){
        return ultimoContador;
    }

    public static int getProximoContador(){
        return proximoContador;
    }

    public static LegendaryEvent getUltimoSpawn(){
        return ultimoSpawn;
    }

    public static boolean tieneContador(){
        return ultimoContador>=0;
    }

    public static boolean estaVerificando(){
        return resultado==
                Resultado.VERIFICANDO;
    }

    public static boolean haySpawnConfirmado(){
        return resultado==
                Resultado.SPAWN;
    }

    public static boolean haySinSpawn(){
        return resultado==
                Resultado.SIN_SPAWN;
    }

    public static long getResultadoRestanteMs(){
        if(resultadoHastaMs<=0L){
            return 0L;
        }

        return Math.max(
                0L,
                resultadoHastaMs-
                        System.currentTimeMillis()
        );
    }

    public enum Estado {
        DESCONOCIDO,
        NORMAL,
        PREPARACION,
        INMINENTE,
        NUEVO_CICLO
    }

    public enum Resultado {
        NINGUNO,
        VERIFICANDO,
        SPAWN,
        SIN_SPAWN
    }
}