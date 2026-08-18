package com.hjunior.nairapixel.client.battle;

import com.hjunior.nairapixel.NairaPixel;
import com.pixelmonmod.pixelmon.client.ClientProxy;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public final class NairaBattleClientHandler{
    private static final Logger LOGGER=LogManager.getLogger("NairaBattle");
    private static boolean estabaEnBatalla;
    private static String ultimaFirma="";
    private static String ultimoError="";

    private NairaBattleClientHandler(){}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;

        try{
            ClientBattleManager bm=ClientProxy.battleManager;
            boolean enBatalla=bm!=null&&bm.isBattling();

            if(!enBatalla){
                if(estabaEnBatalla)NairaBattleService.limpiar();
                estabaEnBatalla=false;
                ultimaFirma="";
                ultimoError="";
                return;
            }

            String firma=NairaBattleService.firma(bm);
            if(!estabaEnBatalla||!firma.equals(ultimaFirma)){
                NairaBattleService.actualizar(bm);
                ultimaFirma=firma;
            }else{
                NairaBattleService.refrescarDinamicos(bm);
            }

            estabaEnBatalla=true;
            ultimoError="";
        }catch(Throwable e){
            String error=e.getClass().getName()+": "+String.valueOf(e.getMessage());
            if(!error.equals(ultimoError)){
                LOGGER.warn("[NairaBattle] No se pudo actualizar el estado de batalla: {}",error);
                ultimoError=error;
            }
        }
    }
}
