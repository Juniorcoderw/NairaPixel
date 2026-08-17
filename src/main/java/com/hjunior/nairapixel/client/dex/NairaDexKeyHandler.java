package com.hjunior.nairapixel.client.dex;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.dex.gui.NairaDexScreen;
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
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT,
        bus=Mod.EventBusSubscriber.Bus.MOD
)
public final class NairaDexKeyHandler {
    private static final String CATEGORIA=
            "key.categories.nairapixel";

    private static final String ACCION=
            "key.nairapixel.dex.open";

    private static KeyBinding teclaAbrir;
    private static boolean presionAnterior;

    private NairaDexKeyHandler(){}

    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event
    ){
        teclaAbrir=
                new KeyBinding(
                        ACCION,
                        KeyConflictContext.IN_GAME,
                        KeyModifier.NONE,
                        InputMappings.Type.KEYSYM,
                        GLFW.GLFW_KEY_UNKNOWN,
                        CATEGORIA
                );

        ClientRegistry.registerKeyBinding(
                teclaAbrir
        );
    }

    private static void actualizar(){
        Minecraft mc=
                Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null||
                teclaAbrir==null){

            presionAnterior=false;
            return;
        }

        boolean presion=
                teclaAbrir.isDown();

        if(presion&&!presionAnterior){
            if(mc.screen==null){
                mc.setScreen(
                        new NairaDexScreen()
                );
            }
        }

        presionAnterior=presion;
    }

    @Mod.EventBusSubscriber(
            modid=NairaPixel.MOD_ID,
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