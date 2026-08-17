package com.hjunior.nairapixel.client.dex.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.client.gui.ScreenHelper;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public final class NairaPokemonSpriteRenderer {
    private NairaPokemonSpriteRenderer(){}

    public static boolean dibujar(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            float x,
            float y,
            float tamano
    ){
        Optional<ResourceLocation> sprite=
                NairaPokemonSpriteProvider.getSprite(
                        pokemon,
                        forma
                );

        if(!sprite.isPresent()){
            return false;
        }

        dibujar(
                matrixStack,
                sprite.get(),
                x,
                y,
                tamano
        );

        return true;
    }

    public static void dibujar(
            MatrixStack matrixStack,
            ResourceLocation sprite,
            float x,
            float y,
            float tamano
    ){
        if(matrixStack==null||
                sprite==null||
                tamano<=0){

            return;
        }

        ScreenHelper.drawImageQuad(
                sprite,
                matrixStack,
                x,
                y,
                tamano,
                tamano,
                0.0F,
                0.0F,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                1.0F,
                0.0F
        );
    }
}