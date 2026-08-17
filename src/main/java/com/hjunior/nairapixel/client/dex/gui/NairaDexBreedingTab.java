package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.breeding.PokemonBreedingData;
import com.hjunior.nairapixel.core.pixelmon.moves.MoveLearnSource;
import com.hjunior.nairapixel.core.pixelmon.moves.PokemonMoveData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class NairaDexBreedingTab extends AbstractGui {
    private static final int FONDO_CARD=0xE3131A22;
    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_ROJO=0xFFE57373;

    private FontRenderer font;

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            String pokemon,
            String forma,
            PokemonBreedingData datos,
            List<PokemonMoveData> movimientos,
            int x,
            int y,
            int w,
            int h
    ){
        this.font=font;

        drawString(
                matrixStack,
                font,
                "CRIANZA",
                x,
                y,
                TEXTO_ACENTO
        );

        int cardY=y+17;
        int cardH=Math.max(
                54,
                h-17
        );

        fill(
                matrixStack,
                x,
                cardY,
                x+w,
                cardY+cardH,
                FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                cardY,
                w,
                cardH,
                BORDE
        );

        fill(
                matrixStack,
                x,
                cardY,
                x+2,
                cardY+cardH,
                ACENTO
        );

        if(datos==null){
            dibujarSinDatos(
                    matrixStack,
                    pokemon,
                    forma,
                    x,
                    cardY,
                    w,
                    cardH
            );
            return;
        }

        dibujarContenido(
                matrixStack,
                pokemon,
                forma,
                datos,
                movimientos==null
                        ?Collections.emptyList()
                        :movimientos,
                x,
                cardY,
                w,
                cardH
        );
    }

    private void dibujarContenido(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            PokemonBreedingData datos,
            List<PokemonMoveData> movimientos,
            int x,
            int y,
            int w,
            int h
    ){
        int lateralW=
                Math.min(
                        132,
                        Math.max(
                                108,
                                w/5
                        )
                );

        int divisorX=
                x+lateralW;

        fill(
                matrixStack,
                divisorX,
                y+8,
                divisorX+1,
                y+h-8,
                BORDE
        );

        dibujarPokemon(
                matrixStack,
                pokemon,
                forma,
                x,
                y,
                lateralW,
                h
        );

        int contenidoX=
                divisorX+14;

        int contenidoW=
                Math.max(
                        120,
                        x+w-contenidoX-12
                );

        int yy=y+15;

        drawString(
                matrixStack,
                font,
                "GRUPOS HUEVO",
                contenidoX,
                yy,
                TEXTO_ACENTO
        );

        yy+=16;

        String grupos=
                gruposHuevo(
                        datos.getGruposHuevo()
                );

        List<String> lineasGrupos=
                envolverTexto(
                        grupos,
                        contenidoW
                );

        if(lineasGrupos.isEmpty()){
            lineasGrupos=
                    Collections.singletonList(
                            "-"
                    );
        }

        int maxGrupo=
                Math.min(
                        2,
                        lineasGrupos.size()
                );

        for(int i=0;i<maxGrupo;i++){
            drawString(
                    matrixStack,
                    font,
                    lineasGrupos.get(i),
                    contenidoX,
                    yy+(i*11),
                    TEXTO
            );
        }

        yy+=
                Math.max(
                        11,
                        maxGrupo*11
                )+
                        10;

        fill(
                matrixStack,
                contenidoX,
                yy,
                contenidoX+contenidoW,
                yy+1,
                BORDE
        );

        yy+=13;

        EstadoCrianza estado=
                obtenerEstadoCrianza(
                        datos.getGruposHuevo()
                );

        int col1=contenidoX;
        int col2=
                contenidoX+
                        Math.max(
                                170,
                                contenidoW/2
                        );

        dibujarDato(
                matrixStack,
                col1,
                yy,
                "Ciclos de huevo",
                String.valueOf(
                        datos.getCiclosHuevo()
                ),
                TEXTO
        );

        dibujarDato(
                matrixStack,
                col2,
                yy,
                "Crianza",
                estado.texto,
                estado.color
        );

        yy+=21;

        int eggMoves=
                contarMovimientosHuevo(
                        movimientos
                );

        dibujarDato(
                matrixStack,
                col1,
                yy,
                "Movimientos huevo",
                String.valueOf(
                        eggMoves
                ),
                eggMoves>0
                        ?TEXTO_ACENTO
                        :TEXTO
        );

        yy+=24;

        fill(
                matrixStack,
                contenidoX,
                yy,
                contenidoX+contenidoW,
                yy+1,
                BORDE
        );

        yy+=13;

        drawString(
                matrixStack,
                font,
                "INFORMACIÓN",
                contenidoX,
                yy,
                TEXTO_ACENTO
        );

        yy+=16;

        String explicacion=
                crearExplicacion(
                        estado,
                        eggMoves
                );

        List<String> lineas=
                envolverTexto(
                        explicacion,
                        contenidoW
                );

        int limiteY=
                y+h-10;

        for(String linea:
                lineas){

            if(yy+10>limiteY){
                break;
            }

            drawString(
                    matrixStack,
                    font,
                    linea,
                    contenidoX,
                    yy,
                    TEXTO_SECUNDARIO
            );

            yy+=11;
        }
    }

    private void dibujarPokemon(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            int x,
            int y,
            int w,
            int h
    ){
        int sprite=58;

        int spriteX=
                x+
                        (w-sprite)/2;

        int spriteY=
                y+
                        Math.max(
                                12,
                                (h-sprite)/2-16
                        );

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                pokemon,
                forma==null
                        ?""
                        :forma,
                spriteX,
                spriteY,
                sprite
        );

        String nombre=
                pokemon==null||
                        pokemon.trim().isEmpty()
                        ?"Pokémon"
                        :pokemon;

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        nombre,
                        w-12
                ),
                x+6,
                spriteY+sprite+8,
                TEXTO
        );

        String formaTexto=
                formaVisible(
                        forma
                );

        if(!formaTexto.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            formaTexto,
                            w-12
                    ),
                    x+6,
                    spriteY+sprite+21,
                    TEXTO_SECUNDARIO
            );
        }
    }

    private void dibujarSinDatos(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            int x,
            int y,
            int w,
            int h
    ){
        int sprite=58;

        int spriteX=
                x+
                        (w-sprite)/2;

        int spriteY=
                y+
                        Math.max(
                                12,
                                (h-sprite)/2-18
                        );

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                pokemon,
                forma==null
                        ?""
                        :forma,
                spriteX,
                spriteY,
                sprite
        );

        String mensaje=
                "Sin datos de crianza disponibles.";

        drawString(
                matrixStack,
                font,
                mensaje,
                x+
                        (w-font.width(mensaje))/2,
                spriteY+sprite+15,
                TEXTO_SECUNDARIO
        );
    }

    private void dibujarDato(
            MatrixStack matrixStack,
            int x,
            int y,
            String etiqueta,
            String valor,
            int colorValor
    ){
        drawString(
                matrixStack,
                font,
                etiqueta+":",
                x,
                y,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                valor==null||
                        valor.trim().isEmpty()
                        ?"-"
                        :valor,
                x+
                        font.width(
                                etiqueta+":"
                        )+
                        5,
                y,
                colorValor
        );
    }

    private String gruposHuevo(
            List<String> grupos
    ){
        if(grupos==null||
                grupos.isEmpty()){

            return "-";
        }

        List<String> traducidos=
                new ArrayList<>();

        for(String grupo:
                grupos){

            if(grupo==null||
                    grupo.trim().isEmpty()){

                continue;
            }

            String valor=
                    PokemonTranslator.grupoHuevo(
                            grupo
                    );

            if(valor!=null&&
                    !valor.trim().isEmpty()&&
                    !traducidos.contains(
                            valor
                    )){

                traducidos.add(
                        valor
                );
            }
        }

        return traducidos.isEmpty()
                ?"-"
                :String.join(
                " · ",
                traducidos
        );
    }

    private int contarMovimientosHuevo(
            List<PokemonMoveData> movimientos
    ){
        if(movimientos==null||
                movimientos.isEmpty()){

            return 0;
        }

        int total=0;

        for(PokemonMoveData movimiento:
                movimientos){

            if(movimiento==null||
                    movimiento.getFuentes()==null){

                continue;
            }

            boolean egg=false;

            for(MoveLearnSource fuente:
                    movimiento.getFuentes()){

                if(fuente==null||
                        fuente.getMetodo()==null){

                    continue;
                }

                String metodo=
                        fuente.getMetodo()
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );

                if(metodo.contains(
                        "EGG"
                )){
                    egg=true;
                    break;
                }
            }

            if(egg){
                total++;
            }
        }

        return total;
    }

    private EstadoCrianza obtenerEstadoCrianza(
            List<String> grupos
    ){
        if(grupos==null||
                grupos.isEmpty()){

            return new EstadoCrianza(
                    "No determinado",
                    TEXTO_SECUNDARIO
            );
        }

        for(String grupo:
                grupos){

            String key=
                    normalizarGrupo(
                            grupo
                    );

            if(key.contains("undiscovered")||
                    key.contains("noeggs")||
                    key.contains("noegg")||
                    key.contains("unbreedable")){

                return new EstadoCrianza(
                        "No disponible",
                        TEXTO_ROJO
                );
            }
        }

        return new EstadoCrianza(
                "Disponible",
                TEXTO_VERDE
        );
    }

    private String crearExplicacion(
            EstadoCrianza estado,
            int eggMoves
    ){
        if("No disponible".equals(
                estado.texto
        )){
            return "Este grupo huevo está marcado por Pixelmon como no disponible para crianza.";
        }

        if("No determinado".equals(
                estado.texto
        )){
            return "Pixelmon no proporciona un grupo huevo suficiente para determinar el estado de crianza.";
        }

        if(eggMoves>0){
            return "Dispone de "+
                    eggMoves+
                    " movimientos que Pixelmon registra con método Egg para esta forma.";
        }

        return "No se registran movimientos con método Egg para esta forma.";
    }

    private String formaVisible(
            String forma
    ){
        if(forma==null||
                forma.trim().isEmpty()||
                forma.equalsIgnoreCase("base")){

            return "";
        }

        return PokemonTranslator.forma(
                forma
        );
    }

    private String normalizarGrupo(
            String valor
    ){
        if(valor==null){
            return "";
        }

        String lower=
                valor.toLowerCase(
                        Locale.ROOT
                );

        StringBuilder out=
                new StringBuilder();

        for(int i=0;i<lower.length();i++){
            char c=lower.charAt(i);

            if(Character.isLetterOrDigit(c)){
                out.append(c);
            }
        }

        return out.toString();
    }

    private List<String> envolverTexto(
            String texto,
            int maxAncho
    ){
        List<String> lineas=
                new ArrayList<>();

        if(texto==null||
                texto.trim().isEmpty()){

            return lineas;
        }

        String[] palabras=
                texto.trim()
                        .split("\\s+");

        StringBuilder actual=
                new StringBuilder();

        for(String palabra:
                palabras){

            String prueba=
                    actual.length()==0
                            ?palabra
                            :actual+" "+palabra;

            if(font.width(prueba)<=maxAncho){
                if(actual.length()>0){
                    actual.append(' ');
                }

                actual.append(
                        palabra
                );

            }else{
                if(actual.length()>0){
                    lineas.add(
                            actual.toString()
                    );

                    actual.setLength(0);
                }

                actual.append(
                        palabra
                );
            }
        }

        if(actual.length()>0){
            lineas.add(
                    actual.toString()
            );
        }

        return lineas;
    }

    private String limitarTexto(
            String texto,
            int maxAncho
    ){
        if(texto==null||
                texto.isEmpty()){

            return "-";
        }

        if(font.width(texto)<=maxAncho){
            return texto;
        }

        String sufijo="...";

        if(font.width(sufijo)>maxAncho){
            return "";
        }

        String actual=texto;

        while(!actual.isEmpty()&&
                font.width(actual+sufijo)>maxAncho){

            actual=
                    actual.substring(
                            0,
                            actual.length()-1
                    );
        }

        return actual+sufijo;
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

    private static final class EstadoCrianza{
        private final String texto;
        private final int color;

        private EstadoCrianza(
                String texto,
                int color
        ){
            this.texto=texto;
            this.color=color;
        }
    }
}
