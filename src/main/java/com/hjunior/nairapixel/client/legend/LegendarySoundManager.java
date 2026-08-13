package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid="nairapixel",
        value=Dist.CLIENT,
        bus=Mod.EventBusSubscriber.Bus.FORGE
)
public final class LegendarySoundManager {
    private static Object mundoActual;

    private static LegendaryWatcher.Estado ultimoEstado=
            LegendaryWatcher.Estado.DESCONOCIDO;

    private static LegendaryWatcher.Resultado ultimoResultado=
            LegendaryWatcher.Resultado.NINGUNO;

    private LegendarySoundManager(){}

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ){
        if(event.phase!=TickEvent.Phase.END){
            return;
        }

        Minecraft mc=
                Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null){

            reiniciar();
            return;
        }

        if(mundoActual!=mc.level){
            reiniciar();
            mundoActual=mc.level;
        }

        LegendaryWatcher.Estado estado=
                LegendaryWatcher.getEstado();

        LegendaryWatcher.Resultado resultado=
                LegendaryWatcher.getResultado();

        comprobarEstado(
                mc,
                estado
        );

        comprobarResultado(
                mc,
                resultado
        );

        ultimoEstado=estado;
        ultimoResultado=resultado;
    }

    private static void comprobarEstado(
            Minecraft mc,
            LegendaryWatcher.Estado estado
    ){
        if(estado==
                LegendaryWatcher.Estado.INMINENTE&&
                ultimoEstado!=
                        LegendaryWatcher.Estado.INMINENTE){

            sonarInicioCero(mc);
        }
    }

    private static void comprobarResultado(
            Minecraft mc,
            LegendaryWatcher.Resultado resultado
    ){
        if(resultado==
                LegendaryWatcher.Resultado.SPAWN&&
                ultimoResultado!=
                        LegendaryWatcher.Resultado.SPAWN){

            sonarSpawn(mc);
            return;
        }

        if(resultado==
                LegendaryWatcher.Resultado.SIN_SPAWN&&
                ultimoResultado!=
                        LegendaryWatcher.Resultado.SIN_SPAWN){

            sonarSinSpawn(mc);
        }
    }

    private static void sonarInicioCero(
            Minecraft mc
    ){
        reproducir(
                mc,
                SoundEvents.BEACON_ACTIVATE,
                0.48F,
                1.15F
        );
    }

    private static void sonarSpawn(
            Minecraft mc
    ){
        reproducir(
                mc,
                SoundEvents.PLAYER_LEVELUP,
                0.38F,
                1.25F
        );
    }

    private static void sonarSinSpawn(
            Minecraft mc
    ){
        reproducir(
                mc,
                SoundEvents.NOTE_BLOCK_BASS,
                0.24F,
                0.82F
        );
    }

    private static void reproducir(
            Minecraft mc,
            SoundEvent sonido,
            float volumen,
            float tono
    ){
        if(mc.level==null||
                mc.player==null){
            return;
        }

        mc.level.playLocalSound(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                sonido,
                SoundCategory.MASTER,
                volumen,
                tono,
                false
        );
    }

    private static void reiniciar(){
        mundoActual=null;

        ultimoEstado=
                LegendaryWatcher.Estado.DESCONOCIDO;

        ultimoResultado=
                LegendaryWatcher.Resultado.NINGUNO;
    }
}