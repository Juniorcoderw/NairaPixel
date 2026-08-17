package com.hjunior.nairapixel.client.sight;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public final class NairaSightHud extends AbstractGui {
    private static final NairaSightHud INSTANCE=
            new NairaSightHud();

    private static final int FONDO=0xE30A0E14;
    private static final int FONDO_SUAVE=0xC70A0E14;
    private static final int BORDE=0xFF35414D;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ROJO=0xFFE57373;

    private NairaSightHud(){}

    @SubscribeEvent
    public static void onRender(
            RenderGameOverlayEvent.Post event
    ){
        if(event.getType()!=
                RenderGameOverlayEvent.ElementType.ALL){

            return;
        }

        Minecraft mc=
                Minecraft.getInstance();

        if(mc.player==null||
                mc.level==null||
                mc.screen!=null){

            return;
        }

        NairaDexObjectivesService objectives=
                NairaDexObjectivesService.get();

        if(!objectives.isNairaSightActivo()){
            return;
        }

        NairaSightService service=
                NairaSightService.get();

        MatrixStack matrixStack=
                event.getMatrixStack();

        if(service.isAlertaActiva()&&
                objectives.isAvisosNairaSightActivos()){

            INSTANCE.renderAlerta(
                    matrixStack,
                    mc,
                    service.getAlertaActual()
            );
        }else if(service.isPerdidoActivo()&&
                objectives.isAvisosNairaSightActivos()){

            INSTANCE.renderPerdido(
                    matrixStack,
                    mc,
                    service.getPokemonPerdido(),
                    service.getColorPerdido()
            );
        }

        List<NairaSightDetection> fuera=
                new ArrayList<>();

        for(NairaSightDetection deteccion:
                service.getDetecciones()){

            if(deteccion!=null&&
                    !deteccion.estaEnPantallaAproximada()){

                fuera.add(
                        deteccion
                );
            }
        }

        for(int i=0;i<fuera.size();i++){
            INSTANCE.renderIndicadorBorde(
                    matrixStack,
                    mc,
                    fuera.get(i),
                    i,
                    fuera.size()
            );
        }
    }

    private void renderAlerta(
            MatrixStack matrixStack,
            Minecraft mc,
            NairaSightDetection actual
    ){
        if(actual==null){
            return;
        }

        FontRenderer font=
                mc.font;

        int w=228;
        int h=48;

        int x=
                (mc.getWindow()
                        .getGuiScaledWidth()-w)/2;

        int y=12;

        int color=
                actual.getColor();

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                BORDE
        );

        fill(
                matrixStack,
                x,
                y,
                x+3,
                y+h,
                color
        );

        fill(
                matrixStack,
                x+w-3,
                y,
                x+w,
                y+h,
                color
        );

        String titulo=
                "NAIRASIGHT";

        font.drawShadow(
                matrixStack,
                titulo,
                x+(w-font.width(titulo))/2.0F,
                y+7,
                color
        );

        String detectado=
                "OBJETIVO DETECTADO · "+
                        actual.getPokemon();

        String salida=
                limitar(
                        font,
                        detectado,
                        w-18
                );

        font.drawShadow(
                matrixStack,
                salida,
                x+(w-font.width(salida))/2.0F,
                y+21,
                TEXTO
        );

        String detalle=
                actual.getDistanciaRedondeada()+
                        " m · "+
                        actual.getDireccion();

        if(Math.abs(
                actual.getDiferenciaY()
        )>=4.0D){

            int dy=
                    actual.getDiferenciaYRedondeada();

            detalle+=
                    " · Y "+
                            (dy>0?"+":"")+
                            dy;
        }

        font.drawShadow(
                matrixStack,
                detalle,
                x+(w-font.width(detalle))/2.0F,
                y+34,
                TEXTO_SECUNDARIO
        );
    }

    private void renderPerdido(
            MatrixStack matrixStack,
            Minecraft mc,
            String pokemon,
            int color
    ){
        FontRenderer font=
                mc.font;

        String texto=
                pokemon==null||
                        pokemon.isEmpty()
                        ?"OBJETIVO PERDIDO"
                        :pokemon+
                        " · OBJETIVO PERDIDO";

        int w=
                Math.max(
                        150,
                        font.width(texto)+24
                );

        w=
                Math.min(
                        w,
                        230
                );

        int h=28;

        int x=
                (mc.getWindow()
                        .getGuiScaledWidth()-w)/2;

        int y=12;

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                BORDE
        );

        fill(
                matrixStack,
                x,
                y,
                x+3,
                y+h,
                color
        );

        String salida=
                limitar(
                        font,
                        texto,
                        w-18
                );

        font.drawShadow(
                matrixStack,
                salida,
                x+(w-font.width(salida))/2.0F,
                y+10,
                TEXTO_ROJO
        );
    }

    private void renderIndicadorBorde(
            MatrixStack matrixStack,
            Minecraft mc,
            NairaSightDetection actual,
            int indice,
            int total
    ){
        FontRenderer font=
                mc.font;

        int screenW=
                mc.getWindow()
                        .getGuiScaledWidth();

        int screenH=
                mc.getWindow()
                        .getGuiScaledHeight();

        double yaw=
                Math.toRadians(
                        actual.getAnguloHorizontal()
                );

        double pitch=
                Math.toRadians(
                        actual.getAnguloVertical()
                );

        double vx=
                Math.sin(
                        yaw
                );

        double vy=
                Math.sin(
                        pitch
                );

        if(Math.abs(vx)<0.04D&&
                Math.abs(vy)<0.04D){

            vx=
                    actual.getAnguloHorizontal()>=0.0F
                            ?1.0D
                            :-1.0D;
        }

        double radioX=
                Math.max(
                        30.0D,
                        screenW/2.0D-30.0D
                );

        double radioY=
                Math.max(
                        30.0D,
                        screenH/2.0D-34.0D
                );

        double escalaX=
                Math.abs(vx)<0.0001D
                        ?Double.MAX_VALUE
                        :radioX/Math.abs(vx);

        double escalaY=
                Math.abs(vy)<0.0001D
                        ?Double.MAX_VALUE
                        :radioY/Math.abs(vy);

        double escala=
                Math.min(
                        escalaX,
                        escalaY
                );

        int cx=screenW/2;
        int cy=screenH/2;

        int x=
                (int)Math.round(
                        cx+
                                vx*escala
                );

        int y=
                (int)Math.round(
                        cy+
                                vy*escala
                );

        if(total>1){
            int separacion=
                    18;

            int offset=
                    (indice-
                            (total-1)/2)*
                            separacion;

            if(Math.abs(vx)>=Math.abs(vy)){
                y+=offset;
            }else{
                x+=offset;
            }
        }

        x=
                limitarEntero(
                        x,
                        24,
                        screenW-24
                );

        y=
                limitarEntero(
                        y,
                        24,
                        screenH-24
                );

        int caja=
                actual.isPrincipal()
                        ?26
                        :23;

        int color=
                actual.getColor();

        fill(
                matrixStack,
                x-caja/2,
                y-caja/2,
                x+caja/2,
                y+caja/2,
                FONDO_SUAVE
        );

        esquinas2D(
                matrixStack,
                x-caja/2,
                y-caja/2,
                caja,
                caja,
                color,
                actual.isPrincipal()
                        ?7
                        :6
        );

        float angulo=
                (float)Math.toDegrees(
                        Math.atan2(
                                vy,
                                vx
                        )
                );

        matrixStack.pushPose();

        matrixStack.translate(
                x,
                y,
                0.0D
        );

        matrixStack.mulPose(
                Vector3f.ZP.rotationDegrees(
                        angulo
                )
        );

        matrixStack.scale(
                actual.isPrincipal()
                        ?1.45F
                        :1.25F,
                actual.isPrincipal()
                        ?1.45F
                        :1.25F,
                1.0F
        );

        font.drawShadow(
                matrixStack,
                ">",
                -font.width(">")/2.0F,
                -4.0F,
                color
        );

        matrixStack.popPose();

        String info=
                actual.getPokemon()+
                        " · "+
                        actual.getDistanciaRedondeada()+
                        " m";

        if(Math.abs(
                actual.getDiferenciaY()
        )>=6.0D){

            int dy=
                    actual.getDiferenciaYRedondeada();

            info+=
                    " · Y "+
                            (dy>0?"+":"")+
                            dy;
        }

        info=
                limitar(
                        font,
                        info,
                        145
                );

        int infoW=
                font.width(info)+10;

        int infoX=
                limitarEntero(
                        x-infoW/2,
                        4,
                        screenW-infoW-4
                );

        int infoY=
                y>screenH/2
                        ?y-28
                        :y+17;

        fill(
                matrixStack,
                infoX,
                infoY,
                infoX+infoW,
                infoY+14,
                FONDO_SUAVE
        );

        fill(
                matrixStack,
                infoX,
                infoY,
                infoX+2,
                infoY+14,
                color
        );

        font.drawShadow(
                matrixStack,
                info,
                infoX+5,
                infoY+3,
                TEXTO
        );
    }

    private void esquinas2D(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int color,
            int l
    ){
        int t=1;

        fill(matrixStack,x,y,x+l,y+t,color);
        fill(matrixStack,x,y,x+t,y+l,color);

        fill(matrixStack,x+w-l,y,x+w,y+t,color);
        fill(matrixStack,x+w-t,y,x+w,y+l,color);

        fill(matrixStack,x,y+h-t,x+l,y+h,color);
        fill(matrixStack,x,y+h-l,x+t,y+h,color);

        fill(matrixStack,x+w-l,y+h-t,x+w,y+h,color);
        fill(matrixStack,x+w-t,y+h-l,x+w,y+h,color);
    }

    private void borde(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int color
    ){
        fill(matrixStack,x,y,x+w,y+1,color);
        fill(matrixStack,x,y+h-1,x+w,y+h,color);
        fill(matrixStack,x,y,x+1,y+h,color);
        fill(matrixStack,x+w-1,y,x+w,y+h,color);
    }

    private String limitar(
            FontRenderer font,
            String texto,
            int maxAncho
    ){
        if(texto==null){
            return "";
        }

        if(font.width(texto)<=maxAncho){
            return texto;
        }

        String sufijo="...";

        String actual=
                texto;

        while(!actual.isEmpty()&&
                font.width(
                        actual+sufijo
                )>maxAncho){

            actual=
                    actual.substring(
                            0,
                            actual.length()-1
                    );
        }

        return actual+sufijo;
    }

    private int limitarEntero(
            int valor,
            int min,
            int max
    ){
        return Math.max(
                min,
                Math.min(
                        max,
                        valor
                )
        );
    }
}
