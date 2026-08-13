package com.hjunior.nairapixel.client;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.data.PokemonSnapshot;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public class WorldHudRenderer {
    private static final int ANCHO=184;
    private static final int MARGEN=7;
    private static final int PADDING=6;

    private static final int FONDO=0xB6090D13;
    private static final int CABECERA=0xD0121921;
    private static final int BORDE=0x9060CFE8;
    private static final int SOMBRA=0x50000000;
    private static final int DIVISOR=0x405C7180;

    private static final int COLOR_MACHO=0x69C7FF;
    private static final int COLOR_HEMBRA=0xFF86C8;
    private static final int COLOR_SIN_GENERO=0xB8BDC4;
    private static final int COLOR_VALOR=0xC8E8F0;
    private static final int COLOR_IV_PERFECTO=0xFFD659;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event){
        if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL)return;

        Minecraft mc=Minecraft.getInstance();

        if(mc.player==null||mc.level==null)return;
        if(mc.options.hideGui)return;
        if(mc.screen!=null)return;

        PokemonSnapshot pokemon=PokemonTargetScanner.getSnapshotActual();
        if(pokemon==null)return;

        render(
                event.getMatrixStack(),
                event.getWindow().getGuiScaledWidth(),
                mc.font,
                pokemon
        );
    }

    private static void render(MatrixStack matrix,int pantalla,FontRenderer font,PokemonSnapshot p){
        int ancho=Math.min(ANCHO,pantalla-(MARGEN*2));
        int usable=ancho-(PADDING*2);

        int filasEtiquetas=contarFilasEtiquetas(font,p,usable);
        int alto=calcularAlto(p,filasEtiquetas);

        int x=pantalla-ancho-MARGEN;
        int y=MARGEN;

        dibujarPanel(matrix,x,y,ancho,alto);
        dibujarTitulo(matrix,font,x,y,ancho);

        int cy=y+21;

        dibujarCabeceraPokemon(matrix,font,p,x,cy,ancho);
        cy+=12;

        cy=dibujarEtiquetas(
                matrix,
                font,
                p,
                x+PADDING,
                cy,
                usable
        );

        dibujarGeneroTamano(
                matrix,
                font,
                p,
                x+PADDING,
                cy,
                usable
        );

        cy+=11;

        String especial=crearLineaEspecial(p);

        if(!especial.isEmpty()){
            font.drawShadow(
                    matrix,
                    especial,
                    x+PADDING,
                    cy,
                    0xFFFFFF
            );

            cy+=11;
        }

        if(p.tieneNaturaleza()||p.tieneHabilidad()){
            dibujarDivisor(matrix,x+PADDING,cy,usable);
            cy+=6;

            if(p.tieneNaturaleza()){
                dibujarCampo(
                        matrix,
                        font,
                        x+PADDING,
                        cy,
                        usable,
                        "Naturaleza:",
                        p.naturaleza
                );

                cy+=11;
            }

            if(p.tieneHabilidad()){
                String habilidad=p.habilidad;

                if(Boolean.TRUE.equals(p.habilidadOculta)){
                    habilidad+=" [HA]";
                }

                dibujarCampo(
                        matrix,
                        font,
                        x+PADDING,
                        cy,
                        usable,
                        "Habilidad:",
                        habilidad
                );

                cy+=11;
            }
        }

        if(p.tieneIVs()){
            dibujarDivisor(matrix,x+PADDING,cy,usable);
            cy+=6;

            dibujarIVTotal(
                    matrix,
                    font,
                    p,
                    x,
                    cy,
                    ancho
            );

            cy+=12;

            int columna=usable/3;

            dibujarIV(matrix,font,x+PADDING,cy,"PS",p.ivPS);
            dibujarIV(matrix,font,x+PADDING+columna,cy,"ATQ",p.ivATQ);
            dibujarIV(matrix,font,x+PADDING+(columna*2),cy,"DEF",p.ivDEF);

            cy+=11;

            dibujarIV(matrix,font,x+PADDING,cy,"AE",p.ivATQESP);
            dibujarIV(matrix,font,x+PADDING+columna,cy,"DE",p.ivDEFESP);
            dibujarIV(matrix,font,x+PADDING+(columna*2),cy,"VEL",p.ivVEL);
        }

        dibujarFirma(matrix,font,x,y,ancho,alto);
    }

    private static void dibujarPanel(
            MatrixStack matrix,
            int x,
            int y,
            int ancho,
            int alto
    ){
        AbstractGui.fill(
                matrix,
                x+2,
                y+2,
                x+ancho+2,
                y+alto+2,
                SOMBRA
        );

        AbstractGui.fill(
                matrix,
                x,
                y,
                x+ancho,
                y+alto,
                FONDO
        );

        AbstractGui.fill(
                matrix,
                x,
                y,
                x+ancho,
                y+17,
                CABECERA
        );

        dibujarBorde(matrix,x,y,ancho,alto);
    }

    private static void dibujarTitulo(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        String texto="\u2726 NAIRAPIXEL \u2726";

        String titulo=
                TextFormatting.AQUA.toString()+
                        TextFormatting.BOLD+
                        texto+
                        TextFormatting.RESET;

        int tx=x+(ancho-font.width(titulo))/2;

        font.drawShadow(
                matrix,
                titulo,
                tx,
                y+4,
                0xFFFFFF
        );
    }

    private static void dibujarCabeceraPokemon(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        int derecha=x+ancho-PADDING;

        String nivel="Nv."+p.nivel;
        int nivelAncho=font.width(nivel);

        String propietario="["+
                (p.propietario==null||p.propietario.isEmpty()
                        ?"?"
                        :p.propietario)+
                "]";

        propietario=recortar(font,propietario,55);

        int propietarioAncho=font.width(propietario);
        int propietarioX=derecha-nivelAncho-propietarioAncho-5;

        int disponibleNombre=
                propietarioX-(x+PADDING)-4;

        String nombre=p.nombre==null
                ?""
                :p.nombre.toUpperCase(Locale.ROOT);

        nombre=recortar(
                font,
                nombre,
                Math.max(30,disponibleNombre)
        );

        font.drawShadow(
                matrix,
                TextFormatting.BOLD+nombre,
                x+PADDING,
                y,
                0xFFFFFF
        );

        int colorPropietario=
                "Salvaje".equalsIgnoreCase(p.propietario)
                        ?0xB8BDC4
                        :0x62DCE8;

        font.drawShadow(
                matrix,
                propietario,
                propietarioX,
                y,
                colorPropietario
        );

        font.drawShadow(
                matrix,
                nivel,
                derecha-nivelAncho,
                y,
                0xFFD659
        );
    }

    private static void dibujarGeneroTamano(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        String genero=p.genero==null
                ?""
                :p.genero;

        int colorGenero=COLOR_SIN_GENERO;

        if(genero.contains("Macho")){
            colorGenero=COLOR_MACHO;
        }else if(genero.contains("Hembra")){
            colorGenero=COLOR_HEMBRA;
        }

        String separador=" \u00B7 ";
        String etiqueta="Tama\u00F1o: ";
        String tamano=p.tamano==null
                ?""
                :p.tamano;

        int actualX=x;

        font.drawShadow(
                matrix,
                genero,
                actualX,
                y,
                colorGenero
        );

        actualX+=font.width(genero);

        font.drawShadow(
                matrix,
                separador,
                actualX,
                y,
                0x666D75
        );

        actualX+=font.width(separador);

        font.drawShadow(
                matrix,
                etiqueta,
                actualX,
                y,
                0xA9ADB3
        );

        actualX+=font.width(etiqueta);

        int disponible=(x+ancho)-actualX;
        tamano=recortar(font,tamano,disponible);

        font.drawShadow(
                matrix,
                tamano,
                actualX,
                y,
                0xF1F1F1
        );
    }

    private static int dibujarEtiquetas(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        List<Etiqueta> etiquetas=crearEtiquetas(p);

        if(etiquetas.isEmpty())return y;

        int inicio=x;
        int actualX=x;
        int actualY=y;

        for(Etiqueta etiqueta:etiquetas){
            int w=font.width(etiqueta.texto)+8;

            if(actualX>inicio&&
                    actualX+w>inicio+ancho){

                actualX=inicio;
                actualY+=11;
            }

            AbstractGui.fill(
                    matrix,
                    actualX,
                    actualY-1,
                    actualX+w,
                    actualY+9,
                    etiqueta.color
            );

            font.drawShadow(
                    matrix,
                    TextFormatting.BOLD+etiqueta.texto,
                    actualX+4,
                    actualY,
                    0xFFFFFF
            );

            actualX+=w+5;
        }

        return actualY+12;
    }

    private static List<Etiqueta> crearEtiquetas(PokemonSnapshot p){
        List<Etiqueta> etiquetas=new ArrayList<>();

        for(String tipo:p.tipos){
            etiquetas.add(
                    new Etiqueta(
                            tipo.toUpperCase(Locale.ROOT),
                            colorTipo(tipo)
                    )
            );
        }

        if(p.tieneCategoria()){
            etiquetas.add(
                    new Etiqueta(
                            p.categoria.toUpperCase(Locale.ROOT),
                            colorCategoria(p.categoria)
                    )
            );
        }

        if(p.esBoss()){
            etiquetas.add(
                    new Etiqueta(
                            "BOSS: "+p.boss.toUpperCase(Locale.ROOT),
                            0xDDD04C4C
                    )
            );
        }

        return etiquetas;
    }

    private static int contarFilasEtiquetas(
            FontRenderer font,
            PokemonSnapshot p,
            int ancho
    ){
        List<Etiqueta> etiquetas=crearEtiquetas(p);

        if(etiquetas.isEmpty())return 0;

        int filas=1;
        int usado=0;

        for(Etiqueta etiqueta:etiquetas){
            int w=font.width(etiqueta.texto)+13;

            if(usado>0&&usado+w>ancho){
                filas++;
                usado=0;
            }

            usado+=w;
        }

        return filas;
    }

    private static String crearLineaEspecial(PokemonSnapshot p){
        StringBuilder texto=new StringBuilder();

        if(p.shiny){
            texto.append(TextFormatting.GOLD)
                    .append("\u2605 Shiny")
                    .append(TextFormatting.RESET);
        }

        if(p.tieneForma()){
            agregarSeparador(texto);

            texto.append(TextFormatting.LIGHT_PURPLE)
                    .append(p.forma)
                    .append(TextFormatting.RESET);
        }

        if(p.tienePaleta()){
            agregarSeparador(texto);

            texto.append(TextFormatting.AQUA)
                    .append(p.paleta)
                    .append(TextFormatting.RESET);
        }

        return texto.toString();
    }

    private static void agregarSeparador(StringBuilder texto){
        if(texto.length()==0)return;

        texto.append(TextFormatting.DARK_GRAY)
                .append(" \u00B7 ")
                .append(TextFormatting.RESET);
    }

    private static void dibujarCampo(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            String etiqueta,
            String valor
    ){
        font.drawShadow(
                matrix,
                etiqueta,
                x,
                y,
                0xA9ADB3
        );

        int etiquetaAncho=font.width(etiqueta)+4;
        int disponible=ancho-etiquetaAncho;

        String texto=recortar(
                font,
                valor,
                disponible
        );

        font.drawShadow(
                matrix,
                texto,
                x+etiquetaAncho,
                y,
                COLOR_VALOR
        );
    }

    private static void dibujarIVTotal(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        String porcentaje=
                String.format(Locale.US,"%.1f%%",p.ivTotal);

        String titulo=
                TextFormatting.BOLD+"IV TOTAL";

        font.drawShadow(
                matrix,
                titulo,
                x+PADDING,
                y,
                0xA9ADB3
        );

        font.drawShadow(
                matrix,
                TextFormatting.BOLD+porcentaje,
                x+ancho-PADDING-font.width(porcentaje),
                y,
                0x67E6EF
        );
    }

    private static void dibujarIV(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            String nombre,
            int valor
    ){
        font.drawShadow(
                matrix,
                nombre,
                x,
                y,
                0xA9ADB3
        );

        String texto;
        int color;

        if(valor==31){
            texto="31\u2605";
            color=COLOR_IV_PERFECTO;
        }else{
            texto=String.valueOf(valor);
            color=0xF2F2F2;
        }

        font.drawShadow(
                matrix,
                texto,
                x+font.width(nombre)+3,
                y,
                color
        );
    }

    private static void dibujarFirma(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int alto
    ){
        String prefijo="by ";
        String nombre="HJunior";

        int total=
                font.width(prefijo)+
                        font.width(nombre);

        int sx=x+ancho-PADDING-total;
        int sy=y+alto-10;

        font.draw(
                matrix,
                prefijo,
                sx,
                sy,
                0x707981
        );

        sx+=font.width(prefijo);

        dibujarTextoDegradado(
                matrix,
                font,
                nombre,
                sx,
                sy
        );
    }

    private static void dibujarTextoDegradado(
            MatrixStack matrix,
            FontRenderer font,
            String texto,
            int x,
            int y
    ){
        int actualX=x;

        int cian=0x6ED9E8;
        int azul=0x789FE5;
        int violeta=0xA18BD2;

        for(int i=0;i<texto.length();i++){
            String caracter=
                    String.valueOf(texto.charAt(i));

            float progreso=
                    texto.length()<=1
                            ?0
                            :(float)i/(texto.length()-1);

            int color;

            if(progreso<=0.5f){
                color=interpolarColor(
                        cian,
                        azul,
                        progreso*2.0f
                );
            }else{
                color=interpolarColor(
                        azul,
                        violeta,
                        (progreso-0.5f)*2.0f
                );
            }

            font.draw(
                    matrix,
                    caracter,
                    actualX+1,
                    y+1,
                    0x30343A
            );

            font.draw(
                    matrix,
                    caracter,
                    actualX,
                    y,
                    color
            );

            actualX+=font.width(caracter);
        }
    }

    private static int interpolarColor(
            int inicio,
            int fin,
            float t
    ){
        int r1=(inicio>>16)&0xFF;
        int g1=(inicio>>8)&0xFF;
        int b1=inicio&0xFF;

        int r2=(fin>>16)&0xFF;
        int g2=(fin>>8)&0xFF;
        int b2=fin&0xFF;

        int r=(int)(r1+(r2-r1)*t);
        int g=(int)(g1+(g2-g1)*t);
        int b=(int)(b1+(b2-b1)*t);

        return (r<<16)|(g<<8)|b;
    }

    private static void dibujarDivisor(
            MatrixStack matrix,
            int x,
            int y,
            int ancho
    ){
        AbstractGui.fill(
                matrix,
                x,
                y+2,
                x+ancho,
                y+3,
                DIVISOR
        );
    }

    private static int calcularAlto(
            PokemonSnapshot p,
            int filasEtiquetas
    ){
        int alto=21;

        alto+=12;
        alto+=(filasEtiquetas*11)+2;
        alto+=11;

        if(p.shiny||p.tieneForma()||p.tienePaleta()){
            alto+=11;
        }

        if(p.tieneNaturaleza()||p.tieneHabilidad()){
            alto+=6;

            if(p.tieneNaturaleza()){
                alto+=11;
            }

            if(p.tieneHabilidad()){
                alto+=11;
            }
        }

        if(p.tieneIVs()){
            alto+=6;
            alto+=12;
            alto+=22;
        }

        return alto+14;
    }

    private static void dibujarBorde(
            MatrixStack matrix,
            int x,
            int y,
            int w,
            int h
    ){
        AbstractGui.fill(
                matrix,
                x,
                y,
                x+w,
                y+1,
                BORDE
        );

        AbstractGui.fill(
                matrix,
                x,
                y+h-1,
                x+w,
                y+h,
                BORDE
        );

        AbstractGui.fill(
                matrix,
                x,
                y,
                x+1,
                y+h,
                BORDE
        );

        AbstractGui.fill(
                matrix,
                x+w-1,
                y,
                x+w,
                y+h,
                BORDE
        );

        AbstractGui.fill(
                matrix,
                x,
                y+16,
                x+w,
                y+17,
                0x8057C7E8
        );
    }

    private static int colorTipo(String tipo){
        switch(normalizar(tipo)){
            case "normal":
                return 0xDDA8A878;

            case "fuego":
                return 0xDDF08030;

            case "agua":
                return 0xDD6890F0;

            case "electrico":
                return 0xDDF8D030;

            case "planta":
                return 0xDD78C850;

            case "hielo":
                return 0xDD98D8D8;

            case "lucha":
                return 0xDDC03028;

            case "veneno":
                return 0xDDA040A0;

            case "tierra":
                return 0xDDE0C068;

            case "volador":
                return 0xDDA890F0;

            case "psiquico":
                return 0xDDF85888;

            case "bicho":
                return 0xDDA8B820;

            case "roca":
                return 0xDDB8A038;

            case "fantasma":
                return 0xDD705898;

            case "dragon":
                return 0xDD7038F8;

            case "siniestro":
                return 0xDD705848;

            case "acero":
                return 0xDDB8B8D0;

            case "hada":
                return 0xDDEE99AC;

            default:
                return 0xDD65727E;
        }
    }

    private static int colorCategoria(String categoria){
        switch(normalizar(categoria)){
            case "legendario":
                return 0xDDBA8A18;

            case "mitico":
                return 0xDDA64BC5;

            case "ultraente":
                return 0xDD6745AF;

            case "pseudo":
                return 0xDD4567B7;

            default:
                return 0xDD65727E;
        }
    }

    private static String normalizar(String texto){
        if(texto==null)return "";

        return Normalizer
                .normalize(texto,Normalizer.Form.NFD)
                .replaceAll("\\p{M}","")
                .toLowerCase(Locale.ROOT)
                .replace(" ","")
                .replace("-","");
    }

    private static String recortar(
            FontRenderer font,
            String texto,
            int ancho
    ){
        if(texto==null)return "";

        if(font.width(texto)<=ancho){
            return texto;
        }

        String puntos="...";
        int disponible=Math.max(
                0,
                ancho-font.width(puntos)
        );

        return font.plainSubstrByWidth(
                texto,
                disponible
        )+puntos;
    }

    private static class Etiqueta {
        final String texto;
        final int color;

        Etiqueta(String texto,int color){
            this.texto=texto;
            this.color=color;
        }
    }
}