package com.hjunior.nairapixel;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(NairaPixel.MOD_ID)
public final class NairaPixel {
    public static final String MOD_ID="nairapixel";

    public NairaPixel(){
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (remoteVersion,isServer) -> true
                )
        );
    }
}