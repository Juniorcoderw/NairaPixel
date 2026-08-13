package com.hjunior.nairapixel.client;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.data.PokemonSnapshot;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public class PCHudRenderer {
    private static final int ANCHO=190;
    private static final int PADDING=6;
    private static final int SEPARACION_CURSOR=14;
    private static final int MARGEN=5;

    private static final int FONDO=0xE20A0E14;
    private static final int CABECERA=0xEE121921;
    private static final int BORDE=0xA060CFE8;
    private static final int SOMBRA=0x60000000;
    private static final int DIVISOR=0x505C7180;

    private static final int COLOR_MACHO=0x69C7FF;
    private static final int COLOR_HEMBRA=0xFF86C8;
    private static final int COLOR_SIN_GENERO=0xB8BDC4;
    private static final int COLOR_VALOR=0xC8E8F0;
    private static final int COLOR_IV_PERFECTO=0xFFD659;
    private static final int COLOR_EV=0x72E39A;

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event){
        PokemonSnapshot p=PCQuickReader.getSnapshotActual();
        if(p==null)return;

        Minecraft mc=Minecraft.getInstance();
        if(mc.screen==null)return;

        int pantallaW=mc.getWindow().getGuiScaledWidth();
        int pantallaH=mc.getWindow().getGuiScaledHeight();

        render(
                event.getMatrixStack(),
                mc.font,
                p,
                event.getMouseX(),
                event.getMouseY(),
                pantallaW,
                pantallaH
        );
    }

    private static void render(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int mouseX,
            int mouseY,
            int pantallaW,
            int pantallaH
    ){
        int usable=ANCHO-(PADDING*2);
        int filasEtiquetas=contarFilasEtiquetas(font,p,usable);
        int alto=calcularAlto(p,filasEtiquetas);

        int x;

        if(mouseX<pantallaW/2){
            x=mouseX+SEPARACION_CURSOR;
        }else{
            x=mouseX-ANCHO-SEPARACION_CURSOR;
        }

        int y=mouseY+8;

        if(x+ANCHO>pantallaW-MARGEN){
            x=mouseX-ANCHO-SEPARACION_CURSOR;
        }

        if(x<MARGEN){
            x=mouseX+SEPARACION_CURSOR;
        }

        if(x+ANCHO>pantallaW-MARGEN){
            x=pantallaW-ANCHO-MARGEN;
        }

        if(x<MARGEN)x=MARGEN;

        if(y+alto>pantallaH-MARGEN){
            y=mouseY-alto-8;
        }

        if(y<MARGEN){
            y=MARGEN;
        }

        dibujarPanel(matrix,x,y,ANCHO,alto);
        dibujarTitulo(matrix,font,x,y,ANCHO);

        int cy=y+21;

        dibujarCabecera(matrix,font,p,x,cy,ANCHO);
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

        if(p.tieneCrianza()){
            dibujarCrianza(
                    matrix,
                    font,
                    p,
                    x+PADDING,
                    cy,
                    usable
            );

            cy+=11;
        }

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

        dibujarDivisor(matrix,x+PADDING,cy,usable);
        cy+=6;

        dibujarIVEV(
                matrix,
                font,
                p,
                x+PADDING,
                cy,
                usable
        );

        cy+=45;

        dibujarDivisor(matrix,x+PADDING,cy,usable);
        cy+=6;

        dibujarDatosExtra(
                matrix,
                font,
                p,
                x+PADDING,
                cy,
                usable
        );

        cy+=calcularAltoExtras(p);

        if(!p.movimientos.isEmpty()){
            dibujarDivisor(matrix,x+PADDING,cy,usable);
            cy+=6;

            dibujarMovimientos(
                    matrix,
                    font,
                    p,
                    x+PADDING,
                    cy,
                    usable
            );
        }

        dibujarFirma(matrix,font,x,y,ANCHO,alto);
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
                x+2,y+2,
                x+ancho+2,y+alto+2,
                SOMBRA
        );

        AbstractGui.fill(
                matrix,
                x,y,
                x+ancho,y+alto,
                FONDO
        );

        AbstractGui.fill(
                matrix,
                x,y,
                x+ancho,y+17,
                CABECERA
        );

        AbstractGui.fill(matrix,x,y,x+ancho,y+1,BORDE);
        AbstractGui.fill(matrix,x,y+alto-1,x+ancho,y+alto,BORDE);
        AbstractGui.fill(matrix,x,y,x+1,y+alto,BORDE);
        AbstractGui.fill(matrix,x+ancho-1,y,x+ancho,y+alto,BORDE);

        AbstractGui.fill(
                matrix,
                x,y+16,
                x+ancho,y+17,
                0x8057C7E8
        );
    }

    private static void dibujarTitulo(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        String texto="\u2726 NAIRAPIXEL PC \u2726";

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

    private static void dibujarCabecera(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        String nivel="Nv."+p.nivel;
        int nivelW=font.width(nivel);

        int disponible=
                ancho-(PADDING*2)-nivelW-8;

        String nombre=p.nombre==null
                ?""
                :p.nombre.toUpperCase(Locale.ROOT);

        nombre=recortar(font,nombre,disponible);

        font.drawShadow(
                matrix,
                TextFormatting.BOLD+nombre,
                x+PADDING,
                y,
                0xFFFFFF
        );

        font.drawShadow(
                matrix,
                nivel,
                x+ancho-PADDING-nivelW,
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
        String genero=p.genero==null?"":p.genero;
        int colorGenero=COLOR_SIN_GENERO;

        if(genero.contains("Macho")){
            colorGenero=COLOR_MACHO;
        }else if(genero.contains("Hembra")){
            colorGenero=COLOR_HEMBRA;
        }

        int actualX=x;

        font.drawShadow(
                matrix,
                genero,
                actualX,
                y,
                colorGenero
        );

        actualX+=font.width(genero);

        String separador=" \u00B7 ";

        font.drawShadow(
                matrix,
                separador,
                actualX,
                y,
                0x666D75
        );

        actualX+=font.width(separador);

        String label="Tama\u00F1o: ";

        font.drawShadow(
                matrix,
                label,
                actualX,
                y,
                0xA9ADB3
        );

        actualX+=font.width(label);

        String tamano=recortar(
                font,
                p.tamano,
                (x+ancho)-actualX
        );

        font.drawShadow(
                matrix,
                tamano,
                actualX,
                y,
                0xF1F1F1
        );
    }

    private static void dibujarCrianza(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        String estado=Boolean.TRUE.equals(p.criable)
                ?"S\u00ED"
                :"No";

        int color=Boolean.TRUE.equals(p.criable)
                ?0x72E39A
                :0xE57676;

        String label="Cr\u00EDa: ";

        font.drawShadow(
                matrix,
                label,
                x,
                y,
                0xA9ADB3
        );

        int actualX=x+font.width(label);

        font.drawShadow(
                matrix,
                estado,
                actualX,
                y,
                color
        );

        actualX+=font.width(estado);

        String grupos=" \u00B7 "+p.gruposHuevoTexto();

        grupos=recortar(
                font,
                grupos,
                (x+ancho)-actualX
        );

        font.drawShadow(
                matrix,
                grupos,
                actualX,
                y,
                0xD7DCE1
        );
    }

    private static void dibujarIVEV(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        int mitad=ancho/2;
        int evX=x+mitad+5;

        font.drawShadow(
                matrix,
                TextFormatting.BOLD+"IVs",
                x,
                y,
                0x67E6EF
        );

        if(p.tieneIVs()){
            String total=String.format(
                    Locale.US,
                    "%.1f%%",
                    p.ivTotal
            );

            font.drawShadow(
                    matrix,
                    total,
                    x+mitad-font.width(total)-5,
                    y,
                    0x67E6EF
            );
        }

        font.drawShadow(
                matrix,
                TextFormatting.BOLD+"EVs",
                evX,
                y,
                COLOR_EV
        );

        y+=12;

        int segundaIV=x+38;
        int segundaEV=evX+38;

        dibujarStat(matrix,font,x,y,"PS",p.ivPS,true);
        dibujarStat(matrix,font,segundaIV,y,"AE",p.ivATQESP,true);

        dibujarStat(matrix,font,evX,y,"PS",p.evPS,false);
        dibujarStat(matrix,font,segundaEV,y,"AE",p.evATQESP,false);

        dibujarStat(matrix,font,x,y+10,"ATQ",p.ivATQ,true);
        dibujarStat(matrix,font,segundaIV,y+10,"DE",p.ivDEFESP,true);

        dibujarStat(matrix,font,evX,y+10,"ATQ",p.evATQ,false);
        dibujarStat(matrix,font,segundaEV,y+10,"DE",p.evDEFESP,false);

        dibujarStat(matrix,font,x,y+20,"DEF",p.ivDEF,true);
        dibujarStat(matrix,font,segundaIV,y+20,"VEL",p.ivVEL,true);

        dibujarStat(matrix,font,evX,y+20,"DEF",p.evDEF,false);
        dibujarStat(matrix,font,segundaEV,y+20,"VEL",p.evVEL,false);
    }

    private static void dibujarStat(
            MatrixStack matrix,
            FontRenderer font,
            int x,
            int y,
            String nombre,
            int valor,
            boolean iv
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

        if(iv&&valor<0){
            texto="\u2014";
            color=0x7B838B;
        }else if(iv&&valor==31){
            texto="31\u2605";
            color=COLOR_IV_PERFECTO;
        }else{
            texto=String.valueOf(Math.max(valor,0));
            color=iv?0xF2F2F2:COLOR_EV;
        }

        font.drawShadow(
                matrix,
                texto,
                x+font.width(nombre)+3,
                y,
                color
        );
    }

    private static void dibujarDatosExtra(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        if(p.amistad!=null){
            dibujarCampo(
                    matrix,font,
                    x,y,ancho,
                    "Amistad:",
                    String.valueOf(p.amistad)
            );

            y+=10;
        }

        if(p.pokeball!=null&&!p.pokeball.isEmpty()){
            dibujarCampo(
                    matrix,font,
                    x,y,ancho,
                    "Pok\u00E9 Ball:",
                    p.pokeball
            );

            y+=10;
        }

        if(p.objeto!=null&&!p.objeto.isEmpty()){
            dibujarCampo(
                    matrix,font,
                    x,y,ancho,
                    "Objeto:",
                    p.objeto
            );

            y+=10;
        }

        if(p.ot!=null&&!p.ot.isEmpty()){
            dibujarCampo(
                    matrix,font,
                    x,y,ancho,
                    "OT:",
                    p.ot
            );
        }
    }

    private static void dibujarMovimientos(
            MatrixStack matrix,
            FontRenderer font,
            PokemonSnapshot p,
            int x,
            int y,
            int ancho
    ){
        font.drawShadow(
                matrix,
                TextFormatting.BOLD+"Movimientos",
                x,
                y,
                0xA9ADB3
        );

        y+=11;

        int mitad=ancho/2;

        for(int i=0;i<p.movimientos.size()&&i<4;i++){
            int columna=i%2;
            int fila=i/2;

            int px=x+(columna*mitad);
            int py=y+(fila*10);

            String movimiento=recortar(
                    font,
                    p.movimientos.get(i),
                    mitad-8
            );

            font.drawShadow(
                    matrix,
                    "\u2022 "+movimiento,
                    px,
                    py,
                    0xC8E8F0
            );
        }
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

        int labelW=font.width(etiqueta)+4;

        String texto=recortar(
                font,
                valor,
                ancho-labelW
        );

        font.drawShadow(
                matrix,
                texto,
                x+labelW,
                y,
                COLOR_VALOR
        );
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
            String c=String.valueOf(texto.charAt(i));

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
                    c,
                    actualX+1,
                    y+1,
                    0x30343A
            );

            font.draw(
                    matrix,
                    c,
                    actualX,
                    y,
                    color
            );

            actualX+=font.width(c);
        }
    }

    private static int interpolarColor(int inicio,int fin,float t){
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

    private static int calcularAltoExtras(PokemonSnapshot p){
        int alto=0;

        if(p.amistad!=null)alto+=10;
        if(p.pokeball!=null&&!p.pokeball.isEmpty())alto+=10;
        if(p.objeto!=null&&!p.objeto.isEmpty())alto+=10;
        if(p.ot!=null&&!p.ot.isEmpty())alto+=10;

        return alto;
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

        if(p.tieneCrianza()){
            alto+=11;
        }

        alto+=6;

        if(p.tieneNaturaleza())alto+=11;
        if(p.tieneHabilidad())alto+=11;

        alto+=6;
        alto+=45;

        alto+=6;
        alto+=calcularAltoExtras(p);

        if(!p.movimientos.isEmpty()){
            alto+=6;
            alto+=31;
        }

        return alto+14;
    }

    private static int colorTipo(String tipo){
        switch(normalizar(tipo)){
            case "normal": return 0xDDA8A878;
            case "fuego": return 0xDDF08030;
            case "agua": return 0xDD6890F0;
            case "electrico": return 0xDDF8D030;
            case "planta": return 0xDD78C850;
            case "hielo": return 0xDD98D8D8;
            case "lucha": return 0xDDC03028;
            case "veneno": return 0xDDA040A0;
            case "tierra": return 0xDDE0C068;
            case "volador": return 0xDDA890F0;
            case "psiquico": return 0xDDF85888;
            case "bicho": return 0xDDA8B820;
            case "roca": return 0xDDB8A038;
            case "fantasma": return 0xDD705898;
            case "dragon": return 0xDD7038F8;
            case "siniestro": return 0xDD705848;
            case "acero": return 0xDDB8B8D0;
            case "hada": return 0xDDEE99AC;
            default: return 0xDD65727E;
        }
    }

    private static int colorCategoria(String categoria){
        switch(normalizar(categoria)){
            case "legendario": return 0xDDBA8A18;
            case "mitico": return 0xDDA64BC5;
            case "ultraente": return 0xDD6745AF;
            case "pseudo": return 0xDD4567B7;
            default: return 0xDD65727E;
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