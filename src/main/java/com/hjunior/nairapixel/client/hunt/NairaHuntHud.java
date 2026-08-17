package com.hjunior.nairapixel.client.hunt;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.sight.NairaSightDetection;
import com.hjunior.nairapixel.client.sight.NairaSightService;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public final class NairaHuntHud extends AbstractGui {
    private static final NairaHuntHud INSTANCE=
            new NairaHuntHud();

    private static final int FONDO=0xD90A0E14;
    private static final int BORDE=0xFF384653;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_AMARILLO=0xFFE0C26C;

    private static int ticks;

    private NairaHuntHud(){}

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ){
        if(event.phase!=TickEvent.Phase.END){
            return;
        }

        Minecraft mc=
                Minecraft.getInstance();

        if(mc.player==null||
                mc.level==null){

            ticks=0;
            return;
        }

        ticks++;

        if(ticks>=10){
            ticks=0;

            NairaHuntService.get()
                    .actualizar();
        }
    }

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

        if(!objectives.isHudNairaHuntActivo()){
            return;
        }

        List<NairaHuntSnapshot> snapshots=
                NairaHuntService.get()
                        .getSnapshots();

        if(snapshots==null||
                snapshots.isEmpty()){

            return;
        }

        INSTANCE.renderHud(
                event.getMatrixStack(),
                mc,
                snapshots,
                objectives
        );
    }

    private void renderHud(
            MatrixStack matrixStack,
            Minecraft mc,
            List<NairaHuntSnapshot> snapshots,
            NairaDexObjectivesService objectives
    ){
        FontRenderer font=
                mc.font;

        int visibles=
                Math.min(
                        NairaDexObjectivesService.MAX_OBJETIVOS,
                        snapshots.size()
                );

        int w=224;
        int headerH=19;
        int filaH=18;
        int h=
                headerH+
                        visibles*filaH+
                        7;

        int x=
                mc.getWindow()
                        .getGuiScaledWidth()-
                        w-
                        10;

        int y=
                mc.getWindow()
                        .getGuiScaledHeight()-
                        h-
                        10;

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

        font.drawShadow(
                matrixStack,
                "NAIRAHUNT",
                x+8,
                y+6,
                TEXTO_SECUNDARIO
        );

        for(int i=0;i<visibles;i++){
            NairaHuntSnapshot snapshot=
                    snapshots.get(i);

            int filaY=
                    y+
                            headerH+
                            i*filaH;

            renderFila(
                    matrixStack,
                    font,
                    x,
                    filaY,
                    w,
                    snapshot,
                    objectives
            );
        }
    }

    private void renderFila(
            MatrixStack matrixStack,
            FontRenderer font,
            int x,
            int y,
            int w,
            NairaHuntSnapshot snapshot,
            NairaDexObjectivesService objectives
    ){
        int color=
                objectives.getColorObjetivo(
                        snapshot.getPokemon(),
                        snapshot.getForma()
                );

        fill(
                matrixStack,
                x+1,
                y,
                x+3,
                y+16,
                color
        );

        String nombre=
                limitar(
                        font,
                        snapshot.getPokemon(),
                        66
                );

        font.drawShadow(
                matrixStack,
                nombre,
                x+8,
                y+4,
                color
        );

        NairaSightDetection detectado=
                NairaSightService.get()
                        .getDeteccion(
                                snapshot.getPokemon(),
                                snapshot.getForma()
                        );

        String estado;
        int colorEstado;

        if(detectado!=null){
            estado=
                    "Detectado · "+
                            detectado.getDistanciaRedondeada()+
                            " m";

            colorEstado=
                    TEXTO_VERDE;
        }else{
            switch(snapshot.getEstado()){
                case ZONA_COMPATIBLE:
                    estado="✓ Compatible · esperando";
                    colorEstado=TEXTO_VERDE;
                    break;

                case SIN_REGLAS:
                    estado="Sin reglas de spawn";
                    colorEstado=TEXTO_SECUNDARIO;
                    break;

                case CONDICIONES_INCOMPLETAS:
                    estado=
                            crearFalta(
                                    snapshot.getFaltantes()
                            );

                    colorEstado=TEXTO_AMARILLO;
                    break;

                default:
                    return;
            }
        }

        int estadoX=x+80;

        font.drawShadow(
                matrixStack,
                limitar(
                        font,
                        estado,
                        w-(estadoX-x)-8
                ),
                estadoX,
                y+4,
                colorEstado
        );
    }

    private String crearFalta(
            List<NairaHuntCondition> faltantes
    ){
        if(faltantes==null||
                faltantes.isEmpty()){

            return "Condiciones incompletas";
        }

        StringBuilder out=
                new StringBuilder(
                        "Falta: "
                );

        int limite=
                Math.min(
                        2,
                        faltantes.size()
                );

        for(int i=0;i<limite;i++){
            if(i>0){
                out.append(" · ");
            }

            NairaHuntCondition condicion=
                    faltantes.get(i);

            String requerido=
                    condicion==null
                            ?""
                            :condicion.getRequerido();

            if(requerido==null||
                    requerido.trim().isEmpty()){

                requerido=
                        condicion==null
                                ?""
                                :condicion.getEtiqueta();
            }

            out.append(
                    requerido
            );
        }

        if(faltantes.size()>limite){
            out.append(
                    " · +"
            ).append(
                    faltantes.size()-limite
            );
        }

        return out.toString();
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
}
