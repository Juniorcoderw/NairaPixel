package com.hjunior.nairapixel.client.dex.spawn;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.core.pixelmon.spawn.PixelmonSpawnProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public final class PixelmonSpawnBootstrap {
    private PixelmonSpawnBootstrap(){}

    @SubscribeEvent
    public static void onLoggedIn(
            ClientPlayerNetworkEvent.LoggedInEvent event
    ){
        PixelmonSpawnProvider.reiniciar();
        PixelmonSpawnProvider.preparar();
    }

    @SubscribeEvent
    public static void onLoggedOut(
            ClientPlayerNetworkEvent.LoggedOutEvent event
    ){
        PixelmonSpawnProvider.reiniciar();
    }
}