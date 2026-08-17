package com.hjunior.nairapixel.client.sight;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public final class NairaSightWorldRenderer {
    private static final float A=0.95F;

    private NairaSightWorldRenderer(){}

    @SubscribeEvent
    public static void onRenderWorldLast(
            RenderWorldLastEvent event
    ){
        Minecraft mc=
                Minecraft.getInstance();

        if(mc.player==null||
                mc.level==null||
                mc.screen!=null){

            return;
        }

        if(!NairaDexObjectivesService.get()
                .isNairaSightActivo()){

            return;
        }

        for(NairaSightDetection actual:
                NairaSightService.get()
                        .getDetecciones()){

            if(actual==null||
                    actual.getEntity()==null||
                    !actual.getEntity().isAlive()){

                continue;
            }

            renderMarcador(
                    event,
                    mc,
                    actual
            );
        }
    }

    private static void renderMarcador(
            RenderWorldLastEvent event,
            Minecraft mc,
            NairaSightDetection actual
    ){
        float partialTicks=
                event.getPartialTicks();

        double x=
                interpolar(
                        actual.getEntity().xOld,
                        actual.getEntity().getX(),
                        partialTicks
                );

        double y=
                interpolar(
                        actual.getEntity().yOld,
                        actual.getEntity().getY(),
                        partialTicks
                );

        double z=
                interpolar(
                        actual.getEntity().zOld,
                        actual.getEntity().getZ(),
                        partialTicks
                );

        AxisAlignedBB actualBox=
                actual.getEntity()
                        .getBoundingBox();

        AxisAlignedBB box=
                actualBox.move(
                        x-actual.getEntity().getX(),
                        y-actual.getEntity().getY(),
                        z-actual.getEntity().getZ()
                ).inflate(
                        actual.isPrincipal()
                                ?0.15D
                                :0.11D
                );

        ActiveRenderInfo camera=
                mc.gameRenderer
                        .getMainCamera();

        Vector3d camPos=
                camera.getPosition();

        MatrixStack matrixStack=
                event.getMatrixStack();

        renderCajaEsquinas(
                matrixStack,
                camPos,
                box,
                actual.getColor(),
                actual.isPrincipal()
        );

        renderEtiqueta(
                matrixStack,
                mc,
                camPos,
                x,
                y+
                        actual.getEntity()
                                .getBbHeight()+
                        0.55D,
                z,
                actual
        );
    }

    private static void renderCajaEsquinas(
            MatrixStack matrixStack,
            Vector3d camPos,
            AxisAlignedBB box,
            int color,
            boolean principal
    ){
        matrixStack.pushPose();

        matrixStack.translate(
                -camPos.x,
                -camPos.y,
                -camPos.z
        );

        Matrix4f pose=
                matrixStack.last()
                        .pose();

        float r=
                ((color>>16)&255)/255.0F;

        float g=
                ((color>>8)&255)/255.0F;

        float b=
                (color&255)/255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(
                false
        );
        RenderSystem.lineWidth(
                principal
                        ?2.5F
                        :2.0F
        );

        Tessellator tessellator=
                Tessellator.getInstance();

        BufferBuilder buffer=
                tessellator.getBuilder();

        buffer.begin(
                GL11.GL_LINES,
                DefaultVertexFormats.POSITION_COLOR
        );

        dibujarEsquinas(
                pose,
                buffer,
                box,
                r,
                g,
                b,
                principal
        );

        double cx=
                (box.minX+box.maxX)/2.0D;

        double cz=
                (box.minZ+box.maxZ)/2.0D;

        linea(
                pose,
                buffer,
                cx,
                box.maxY,
                cz,
                cx,
                box.maxY+
                        (principal
                                ?0.38D
                                :0.30D),
                cz,
                r,
                g,
                b,
                A
        );

        tessellator.end();

        RenderSystem.lineWidth(
                1.0F
        );
        RenderSystem.depthMask(
                true
        );
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        matrixStack.popPose();
    }

    private static void dibujarEsquinas(
            Matrix4f pose,
            BufferBuilder buffer,
            AxisAlignedBB box,
            float r,
            float g,
            float b,
            boolean principal
    ){
        double factor=
                principal
                        ?0.34D
                        :0.28D;

        double limite=
                principal
                        ?0.58D
                        :0.48D;

        double lx=
                Math.min(
                        (box.maxX-box.minX)*factor,
                        limite
                );

        double ly=
                Math.min(
                        (box.maxY-box.minY)*
                                (principal
                                        ?0.27D
                                        :0.22D),
                        limite
                );

        double lz=
                Math.min(
                        (box.maxZ-box.minZ)*factor,
                        limite
                );

        for(int xi=0;xi<2;xi++){
            for(int yi=0;yi<2;yi++){
                for(int zi=0;zi<2;zi++){
                    double x=
                            xi==0
                                    ?box.minX
                                    :box.maxX;

                    double y=
                            yi==0
                                    ?box.minY
                                    :box.maxY;

                    double z=
                            zi==0
                                    ?box.minZ
                                    :box.maxZ;

                    double x2=
                            xi==0
                                    ?x+lx
                                    :x-lx;

                    double y2=
                            yi==0
                                    ?y+ly
                                    :y-ly;

                    double z2=
                            zi==0
                                    ?z+lz
                                    :z-lz;

                    linea(
                            pose,
                            buffer,
                            x,y,z,
                            x2,y,z,
                            r,g,b,A
                    );

                    linea(
                            pose,
                            buffer,
                            x,y,z,
                            x,y2,z,
                            r,g,b,A
                    );

                    linea(
                            pose,
                            buffer,
                            x,y,z,
                            x,y,z2,
                            r,g,b,A
                    );
                }
            }
        }
    }

    private static void renderEtiqueta(
            MatrixStack matrixStack,
            Minecraft mc,
            Vector3d camPos,
            double x,
            double y,
            double z,
            NairaSightDetection actual
    ){
        matrixStack.pushPose();

        matrixStack.translate(
                x-camPos.x,
                y-camPos.y,
                z-camPos.z
        );

        matrixStack.mulPose(
                mc.getEntityRenderDispatcher()
                        .cameraOrientation()
        );

        float escala=
                actual.isPrincipal()
                        ?0.027F
                        :0.024F;

        matrixStack.scale(
                -escala,
                -escala,
                escala
        );

        String etiqueta=
                actual.getPokemon()+
                        " · "+
                        actual.getDistanciaRedondeada()+
                        " m";

        FontRenderer font=
                mc.font;

        float textoX=
                -font.width(
                        etiqueta
                )/2.0F;

        IRenderTypeBuffer.Impl buffer=
                mc.renderBuffers()
                        .bufferSource();

        font.drawInBatch(
                etiqueta,
                textoX,
                0.0F,
                actual.getColor(),
                false,
                matrixStack.last()
                        .pose(),
                buffer,
                true,
                0x700A0E14,
                15728880
        );

        buffer.endBatch();

        matrixStack.popPose();
    }

    private static void linea(
            Matrix4f pose,
            BufferBuilder buffer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            float r,
            float g,
            float b,
            float a
    ){
        buffer.vertex(
                pose,
                (float)x1,
                (float)y1,
                (float)z1
        ).color(
                r,g,b,a
        ).endVertex();

        buffer.vertex(
                pose,
                (float)x2,
                (float)y2,
                (float)z2
        ).color(
                r,g,b,a
        ).endVertex();
    }

    private static double interpolar(
            double anterior,
            double actual,
            float parcial
    ){
        return anterior+
                (actual-anterior)*
                        parcial;
    }
}
