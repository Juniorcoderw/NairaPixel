package com.hjunior.nairapixel.client.collection;

import com.hjunior.nairapixel.NairaPixel;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import com.pixelmonmod.pixelmon.client.storage.ClientStorageManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public final class NairaCollectionSyncHandler {
    private static final Logger LOGGER=
            LogManager.getLogger("NairaCollection");

    private static PCScreen ultimaPantalla;

    private NairaCollectionSyncHandler(){}

    @SubscribeEvent
    public static void onDraw(GuiScreenEvent.DrawScreenEvent.Pre event){
        if(!(event.getGui() instanceof PCScreen)){
            ultimaPantalla=null;
            return;
        }

        PCScreen screen=(PCScreen)event.getGui();

        if(screen==ultimaPantalla)return;

        ultimaPantalla=screen;

        sincronizar(screen);
    }

    private static void sincronizar(PCScreen screen){
        PCStorage pc=obtenerPC(screen);
        PlayerPartyStorage party=ClientStorageManager.party;

        if(pc==null){
            LOGGER.warn(
                    "No se pudo sincronizar NairaCollection: PCStorage no disponible"
            );
            return;
        }

        NairaCollectionService.actualizar(
                pc,
                party
        );

        NairaCollectionSnapshot snapshot=
                NairaCollectionService.getSnapshot();

        LOGGER.info(
                "Sincronizada | Total={} | Especies={} | PC={} | Equipo={} | Shiny={}",
                snapshot.getTotalPokemon(),
                snapshot.getEspeciesDistintas(),
                snapshot.getTotalPC(),
                snapshot.getTotalEquipo(),
                snapshot.getTotalShiny()
        );
    }

    private static PCStorage obtenerPC(PCScreen screen){
        if(ClientStorageManager.openPC!=null){
            return ClientStorageManager.openPC;
        }

        PokemonStorage storage=obtenerStorage(screen);

        if(storage instanceof PCStorage){
            return (PCStorage)storage;
        }

        return null;
    }

    private static PokemonStorage obtenerStorage(PCScreen screen){
        Class<?> clase=screen.getClass();

        while(clase!=null){
            for(Field campo:clase.getDeclaredFields()){
                if(!PokemonStorage.class
                        .isAssignableFrom(campo.getType())){
                    continue;
                }

                try{
                    campo.setAccessible(true);

                    Object valor=campo.get(screen);

                    if(valor instanceof PokemonStorage){
                        return (PokemonStorage)valor;
                    }
                }catch(Exception ignored){
                }
            }

            clase=clase.getSuperclass();
        }

        return null;
    }
}