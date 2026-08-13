package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid="nairapixel",
        value=Dist.CLIENT,
        bus=Mod.EventBusSubscriber.Bus.FORGE
)
public final class LegendaryCandidateNavigation {
    private LegendaryCandidateNavigation(){}

    @SubscribeEvent
    public static void onMouseScroll(
            InputEvent.MouseScrollEvent event
    ){
        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null||
                mc.screen!=null||
                !LegendaryKeyHandler.isExpandido()){
            return;
        }

        if(!shiftPresionado(mc)){
            return;
        }

        double delta=
                event.getScrollDelta();

        if(delta==0.0){
            return;
        }

        int direccion=
                delta<0.0
                        ?1
                        :-1;

        boolean usado=
                LegendaryHudRenderer
                        .procesarScrollLista(
                                direccion
                        );

        if(usado){
            event.setCanceled(true);
        }
    }

    private static boolean shiftPresionado(
            Minecraft mc
    ){
        long ventana=
                mc.getWindow()
                        .getWindow();

        return GLFW.glfwGetKey(
                ventana,
                GLFW.GLFW_KEY_LEFT_SHIFT
        )==GLFW.GLFW_PRESS||
                GLFW.glfwGetKey(
                        ventana,
                        GLFW.GLFW_KEY_RIGHT_SHIFT
                )==GLFW.GLFW_PRESS;
    }
}