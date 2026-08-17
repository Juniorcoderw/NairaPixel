package com.hjunior.nairapixel.client.sight;

import com.hjunior.nairapixel.NairaPixel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public final class NairaSightClientHandler {
    private NairaSightClientHandler(){}

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ){
        if(event.phase!=TickEvent.Phase.END){
            return;
        }

        NairaSightService.get()
                .tick();
    }
}
