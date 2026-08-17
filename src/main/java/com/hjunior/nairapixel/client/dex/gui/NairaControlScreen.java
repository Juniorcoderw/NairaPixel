package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public final class NairaControlScreen extends Screen {
    private static final int FONDO=0xF20A0E14;
    private static final int FONDO_CARD=0xE319202A;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_ROJO=0xFFE57373;

    private final Screen anterior;

    private final NairaDexObjectivesService service=
            NairaDexObjectivesService.get();

    private int panelX;
    private int panelY;
    private int panelW=430;
    private int panelH=330;

    private final int[] toggleX=new int[4];
    private final int[] toggleY=new int[4];
    private final int[] toggleW=new int[4];
    private final int[] toggleH=new int[4];

    private int volverX;
    private int volverY;
    private int volverW=58;
    private int volverH=18;

    public NairaControlScreen(
            Screen anterior
    ){
        super(
                new StringTextComponent(
                        "Configuración de NairaPixel"
                )
        );

        this.anterior=anterior;
    }

    @Override
    protected void init(){
        super.init();

        panelW=
                Math.min(
                        430,
                        width-24
                );

        panelH=
                Math.min(
                        330,
                        height-24
                );

        panelX=
                (width-panelW)/2;

        panelY=
                (height-panelH)/2;
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY,
            float partialTicks
    ){
        renderBackground(
                matrixStack
        );

        fill(
                matrixStack,
                panelX,
                panelY,
                panelX+panelW,
                panelY+panelH,
                FONDO
        );

        borde(
                matrixStack,
                panelX,
                panelY,
                panelW,
                panelH,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                "CONFIGURACIÓN DE NAIRAPIXEL",
                panelX+14,
                panelY+12,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                "Controles principales. Sin opciones innecesarias.",
                panelX+14,
                panelY+27,
                TEXTO_SECUNDARIO
        );

        int y=
                panelY+48;

        y=
                dibujarModulo(
                        matrixStack,
                        mouseX,
                        mouseY,
                        0,
                        panelX+12,
                        y,
                        panelW-24,
                        "NAIRADEX",
                        "Información rápida al apuntar un Pokémon.",
                        "Scanner al apuntar",
                        service.isScannerActivo()
                )+7;

        y=
                dibujarModuloInfo(
                        matrixStack,
                        panelX+12,
                        y,
                        panelW-24,
                        "OBJETIVOS",
                        "Hasta 3 Pokémon activos con color propio.",
                        "El objetivo principal muestra el diagnóstico completo."
                )+7;

        y=
                dibujarModulo(
                        matrixStack,
                        mouseX,
                        mouseY,
                        1,
                        panelX+12,
                        y,
                        panelW-24,
                        "NAIRAHUNT",
                        "Guía compacta de condiciones de aparición.",
                        "Guía de condiciones",
                        service.isHudNairaHuntActivo()
                )+7;

        y=
                dibujarModulo(
                        matrixStack,
                        mouseX,
                        mouseY,
                        2,
                        panelX+12,
                        y,
                        panelW-24,
                        "NAIRASIGHT",
                        "Flechas y marcador de los objetivos detectados.",
                        "Rastreo visual",
                        service.isNairaSightActivo()
                )+7;

        y=
                dibujarModulo(
                        matrixStack,
                        mouseX,
                        mouseY,
                        3,
                        panelX+12,
                        y,
                        panelW-24,
                        "AVISOS",
                        "Mensaje breve al detectar o perder un objetivo.",
                        "Avisos de detección",
                        service.isAvisosNairaSightActivos()
                );

        volverX=
                panelX+
                        panelW-
                        volverW-
                        12;

        volverY=
                panelY+
                        panelH-
                        volverH-
                        10;

        dibujarBoton(
                matrixStack,
                volverX,
                volverY,
                volverW,
                volverH,
                "VOLVER",
                dentro(
                        mouseX,
                        mouseY,
                        volverX,
                        volverY,
                        volverW,
                        volverH
                )
        );

        super.render(
                matrixStack,
                mouseX,
                mouseY,
                partialTicks
        );
    }

    private int dibujarModulo(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY,
            int indice,
            int x,
            int y,
            int w,
            String titulo,
            String descripcion,
            String opcion,
            boolean activo
    ){
        int h=42;

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                BORDE
        );

        drawString(
                matrixStack,
                font,
                titulo,
                x+9,
                y+7,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                descripcion,
                x+9,
                y+21,
                TEXTO_SECUNDARIO
        );

        int tw=72;
        int th=18;
        int tx=x+w-tw-8;
        int ty=y+(h-th)/2;

        toggleX[indice]=tx;
        toggleY[indice]=ty;
        toggleW[indice]=tw;
        toggleH[indice]=th;

        boolean hover=
                dentro(
                        mouseX,
                        mouseY,
                        tx,
                        ty,
                        tw,
                        th
                );

        fill(
                matrixStack,
                tx,
                ty,
                tx+tw,
                ty+th,
                hover
                        ?FONDO_HOVER
                        :FONDO
        );

        borde(
                matrixStack,
                tx,
                ty,
                tw,
                th,
                activo
                        ?TEXTO_VERDE
                        :TEXTO_ROJO
        );

        String estado=
                activo
                        ?"ACTIVADO"
                        :"DESACT.";

        int estadoColor=
                activo
                        ?TEXTO_VERDE
                        :TEXTO_ROJO;

        drawString(
                matrixStack,
                font,
                estado,
                tx+(tw-font.width(estado))/2,
                ty+5,
                estadoColor
        );

        return y+h;
    }

    private int dibujarModuloInfo(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String titulo,
            String descripcion,
            String detalle
    ){
        int h=42;

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO_CARD
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
                x+2,
                y+h,
                NairaDexObjectivesService.COLOR_OBJETIVO_1
        );

        fill(
                matrixStack,
                x+2,
                y,
                x+4,
                y+h,
                NairaDexObjectivesService.COLOR_OBJETIVO_2
        );

        fill(
                matrixStack,
                x+4,
                y,
                x+6,
                y+h,
                NairaDexObjectivesService.COLOR_OBJETIVO_3
        );

        drawString(
                matrixStack,
                font,
                titulo,
                x+11,
                y+7,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                descripcion,
                x+11,
                y+21,
                TEXTO
        );

        int dx=
                x+w-
                        font.width(detalle)-
                        9;

        if(dx>x+160){
            drawString(
                    matrixStack,
                    font,
                    detalle,
                    dx,
                    y+21,
                    TEXTO_SECUNDARIO
            );
        }

        return y+h;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ){
        if(button==0){
            for(int i=0;i<4;i++){
                if(dentro(
                        mouseX,
                        mouseY,
                        toggleX[i],
                        toggleY[i],
                        toggleW[i],
                        toggleH[i]
                )){
                    cambiar(
                            i
                    );
                    return true;
                }
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    volverX,
                    volverY,
                    volverW,
                    volverH
            )){
                onClose();
                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private void cambiar(
            int indice
    ){
        switch(indice){
            case 0:
                service.alternarScanner();
                break;

            case 1:
                service.alternarHudNairaHunt();
                break;

            case 2:
                service.alternarNairaSight();
                break;

            case 3:
                service.alternarAvisosNairaSight();
                break;

            default:
                break;
        }
    }

    @Override
    public void onClose(){
        if(minecraft!=null){
            minecraft.setScreen(
                    anterior
            );
        }
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    private void dibujarBoton(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            String texto,
            boolean hover
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                hover
                        ?FONDO_HOVER
                        :FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                hover
                        ?ACENTO
                        :BORDE
        );

        drawString(
                matrixStack,
                font,
                texto,
                x+(w-font.width(texto))/2,
                y+5,
                hover
                        ?ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void borde(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int color
    ){
        AbstractGui.fill(matrixStack,x,y,x+w,y+1,color);
        AbstractGui.fill(matrixStack,x,y+h-1,x+w,y+h,color);
        AbstractGui.fill(matrixStack,x,y,x+1,y+h,color);
        AbstractGui.fill(matrixStack,x+w-1,y,x+w,y+h,color);
    }

    private boolean dentro(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int w,
            int h
    ){
        return mouseX>=x&&
                mouseX<x+w&&
                mouseY>=y&&
                mouseY<y+h;
    }
}
