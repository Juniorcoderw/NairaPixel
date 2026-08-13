package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid="nairapixel",
        value=Dist.CLIENT,
        bus=Mod.EventBusSubscriber.Bus.MOD
)
public final class LegendaryKeyHandler {
    private static final String CATEGORIA=
            "key.categories.nairapixel";

    private static final String ACCION=
            "key.nairapixel.legend.toggle";

    private static KeyBinding teclaExpandir;

    private static boolean expandido=false;
    private static boolean presionAnterior=false;

    private LegendaryKeyHandler(){}

    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event
    ){
        teclaExpandir=
                new KeyBinding(
                        ACCION,
                        KeyConflictContext.IN_GAME,
                        KeyModifier.SHIFT,
                        InputMappings.Type.KEYSYM,
                        GLFW.GLFW_KEY_X,
                        CATEGORIA
                );

        ClientRegistry.registerKeyBinding(
                teclaExpandir
        );
    }

    public static boolean isExpandido(){
        return expandido;
    }

    public static void contraer(){
        expandido=false;
    }

    private static void actualizar(){
        Minecraft mc=
                Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null||
                teclaExpandir==null){

            expandido=false;
            presionAnterior=false;
            return;
        }

        boolean presion=
                teclaExpandir.isDown();

        if(presion&&
                !presionAnterior){

            expandido=
                    !expandido;
        }

        presionAnterior=
                presion;
    }

    @Mod.EventBusSubscriber(
            modid="nairapixel",
            value=Dist.CLIENT,
            bus=Mod.EventBusSubscriber.Bus.FORGE
    )
    public static final class ForgeEvents {
        private ForgeEvents(){}

        @SubscribeEvent
        public static void onClientTick(
                TickEvent.ClientTickEvent event
        ){
            if(event.phase==
                    TickEvent.Phase.END){

                actualizar();
            }
        }
    }
}