package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public final class NairaHelpScreen extends Screen {
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

    private int panelX;
    private int panelY;
    private int panelW=470;
    private int panelH=318;

    private int volverX;
    private int volverY;
    private int volverW=58;
    private int volverH=18;

    public NairaHelpScreen(
            Screen anterior
    ){
        super(
                new StringTextComponent(
                        "Ayuda de NairaPixel"
                )
        );

        this.anterior=anterior;
    }

    @Override
    protected void init(){
        super.init();

        panelW=
                Math.min(
                        470,
                        width-24
                );

        panelH=
                Math.min(
                        318,
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
                "AYUDA DE NAIRAPIXEL",
                panelX+14,
                panelY+12,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                "Guía rápida de las funciones principales.",
                panelX+14,
                panelY+27,
                TEXTO_SECUNDARIO
        );

        int x=
                panelX+12;

        int w=
                panelW-24;

        int y=
                panelY+47;

        y=
                dibujarModulo(
                        matrixStack,
                        x,
                        y,
                        w,
                        "NAIRADEX",
                        "Escanea el Pokémon al apuntarlo y muestra los datos",
                        "que el cliente tenga disponibles.",
                        ACENTO
                )+6;

        y=
                dibujarObjetivos(
                        matrixStack,
                        x,
                        y,
                        w
                )+6;

        y=
                dibujarModulo(
                        matrixStack,
                        x,
                        y,
                        w,
                        "NAIRAHUNT",
                        "Compara tus objetivos con el entorno actual.",
                        "Te indica qué condición falta para su aparición.",
                        TEXTO_VERDE
                )+6;

        y=
                dibujarModulo(
                        matrixStack,
                        x,
                        y,
                        w,
                        "NAIRASIGHT",
                        "Marca objetivos detectados con borde, distancia y flecha.",
                        "Solo trabaja con entidades que tu cliente ya puede ver.",
                        ACENTO
                )+6;

        dibujarLeyenda(
                matrixStack,
                x,
                y,
                w
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
            int x,
            int y,
            int w,
            String titulo,
            String linea1,
            String linea2,
            int color
    ){
        int h=48;

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
                x+3,
                y+h,
                color
        );

        drawString(
                matrixStack,
                font,
                titulo,
                x+10,
                y+7,
                color
        );

        drawString(
                matrixStack,
                font,
                linea1,
                x+10,
                y+21,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                linea2,
                x+10,
                y+34,
                TEXTO_SECUNDARIO
        );

        return y+h;
    }

    private int dibujarObjetivos(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
    ){
        int h=58;

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
                "OBJETIVOS",
                x+12,
                y+7,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                "Puedes seguir hasta 3 Pokémon al mismo tiempo.",
                x+12,
                y+21,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                "Uno es PRINCIPAL y muestra el diagnóstico completo.",
                x+12,
                y+34,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Cian · Dorado · Magenta",
                x+12,
                y+47,
                TEXTO_SECUNDARIO
        );

        return y+h;
    }

    private void dibujarLeyenda(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
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
                "LEYENDA",
                x+10,
                y+7,
                ACENTO
        );

        int yy=y+22;

        drawString(
                matrixStack,
                font,
                "● 1",
                x+10,
                yy,
                NairaDexObjectivesService.COLOR_OBJETIVO_1
        );

        drawString(
                matrixStack,
                font,
                "● 2",
                x+47,
                yy,
                NairaDexObjectivesService.COLOR_OBJETIVO_2
        );

        drawString(
                matrixStack,
                font,
                "● 3",
                x+84,
                yy,
                NairaDexObjectivesService.COLOR_OBJETIVO_3
        );

        drawString(
                matrixStack,
                font,
                "✓ Compatible",
                x+132,
                yy,
                TEXTO_VERDE
        );

        drawString(
                matrixStack,
                font,
                "✕ Faltan condiciones",
                x+226,
                yy,
                TEXTO_ROJO
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ){
        if(button==0&&
                dentro(
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

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
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
