package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class NairaDexSpawnTab extends AbstractGui {
    private static final int FONDO_NAV=0xF20A0E14;
    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_ROJO=0xFFE57373;

    private FontRenderer font;

    private List<PokemonSpawnRule> spawns=
            Collections.emptyList();

    private int spawnX;
    private int spawnY;
    private int spawnW;
    private int spawnH;
    private int scrollSpawn;

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            List<PokemonSpawnRule> spawns,
            int x,
            int y,
            int w,
            int h
    ){
        this.font=font;
        this.spawns=
                spawns==null
                        ?Collections.emptyList()
                        :spawns;

        dibujarSpawn(
                matrixStack,
                x,
                y,
                w,
                h
        );
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ){
        if(!dentro(
                mouseX,
                mouseY,
                spawnX,
                spawnY,
                spawnW,
                spawnH
        )){
            return false;
        }

        if(delta>0){
            scrollSpawn--;
        }else if(delta<0){
            scrollSpawn++;
        }

        int altoRegla=76;
        int listaH=Math.max(
                20,
                spawnH-67
        );
        int visibles=Math.max(
                1,
                listaH/altoRegla
        );

        limitarScrollSpawn(
                visibles
        );

        return true;
    }

    public void resetScroll(){
        scrollSpawn=0;
    }

    private void dibujarSpawn(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h
    ){
        spawnX=x;
        spawnY=y;
        spawnW=w;
        spawnH=h;

        NairaDexSpawnEvaluator.Evaluacion evaluacion=
                NairaDexSpawnEvaluator.evaluar(
                        spawns
                );

        drawString(
                matrixStack,
                font,
                "SPAWN",
                x,
                y,
                TEXTO_ACENTO
        );

        String resumenReglas=
                spawns.isEmpty()
                        ?"Sin reglas para esta forma"
                        :"Reglas: "+spawns.size();

        drawString(
                matrixStack,
                font,
                resumenReglas,
                x+50,
                y,
                spawns.isEmpty()
                        ?TEXTO_SECUNDARIO
                        :TEXTO
        );

        int estadoY=y+16;
        int estadoH=44;

        dibujarEstadoSpawnActual(
                matrixStack,
                x,
                estadoY,
                w-6,
                estadoH,
                evaluacion
        );

        int listaY=estadoY+estadoH+7;
        int listaH=Math.max(
                20,
                h-(listaY-y)
        );

        int altoRegla=76;
        int visibles=Math.max(
                1,
                listaH/altoRegla
        );

        limitarScrollSpawn(visibles);

        if(spawns.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    "Pixelmon no registra una regla de spawn",
                    x,
                    listaY+12,
                    TEXTO_SECUNDARIO
            );

            drawString(
                    matrixStack,
                    font,
                    "para la forma seleccionada.",
                    x,
                    listaY+25,
                    TEXTO_SECUNDARIO
            );

            return;
        }

        for(int fila=0;fila<visibles;fila++){
            int indice=
                    scrollSpawn+fila;

            if(indice>=spawns.size()){
                break;
            }

            PokemonSpawnRule regla=
                    spawns.get(indice);

            NairaDexSpawnEvaluator.ResultadoRegla resultado=
                    evaluacion.getResultado(indice);

            int yy=
                    listaY+(fila*altoRegla);

            dibujarReglaSpawn(
                    matrixStack,
                    regla,
                    resultado,
                    indice,
                    x,
                    yy,
                    w-6,
                    altoRegla-4
            );
        }

        dibujarScrollbarSpawn(
                matrixStack,
                x+w-3,
                listaY,
                listaH,
                visibles
        );
    }

    private void dibujarEstadoSpawnActual(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            NairaDexSpawnEvaluator.Evaluacion evaluacion
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO_NAV
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
                "AHORA",
                x+7,
                y+7,
                TEXTO_ACENTO
        );

        String estado;

        int colorEstado;

        if(!evaluacion.tieneReglas()){
            estado="SIN REGLAS";
            colorEstado=TEXTO_SECUNDARIO;
        }else if(evaluacion.isCompatibleAhora()){
            int reglaCompatible=
                    evaluacion.getPrimeraReglaCompatible();

            estado=
                    reglaCompatible>0
                            ?"PUEDE APARECER · REGLA "+reglaCompatible
                            :"PUEDE APARECER";

            colorEstado=TEXTO_VERDE;
        }else{
            estado="NO PUEDE APARECER AHORA";
            colorEstado=TEXTO_ROJO;
        }

        drawString(
                matrixStack,
                font,
                estado,
                x+55,
                y+7,
                colorEstado
        );

        String bioma=
                traducirIdentificadorSpawn(
                        evaluacion.getEntorno()
                                .getBioma()
                );

        if(bioma.isEmpty()){
            bioma="Desconocido";
        }

        String linea1=
                "Biome: "+bioma+
                        "  ·  Y "+evaluacion.getEntorno()
                        .getY()+
                        "  ·  "+evaluacion.getHora();

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        linea1,
                        w-14
                ),
                x+7,
                y+20,
                TEXTO
        );

        String clima=
                traducirClimaSpawn(
                        evaluacion.getEntorno()
                                .getClima()
                );

        String luna=
                evaluacion.getEntorno()
                        .tieneFaseLunar()
                        ?String.valueOf(
                        evaluacion.getEntorno()
                                .getFaseLunar()
                )
                        :"-";

        String compatibilidad=
                evaluacion.tieneReglas()
                        ?evaluacion.getCantidadCompatibles()+
                        "/"+
                        evaluacion.getTotalReglas()+
                        " reglas"
                        :"0 reglas";

        String linea2=
                "Clima: "+clima+
                        "  ·  Luna: "+luna+
                        "  ·  "+compatibilidad;

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        linea2,
                        w-14
                ),
                x+7,
                y+32,
                TEXTO_SECUNDARIO
        );
    }

    private void dibujarReglaSpawn(
            MatrixStack matrixStack,
            PokemonSpawnRule regla,
            NairaDexSpawnEvaluator.ResultadoRegla resultado,
            int indice,
            int x,
            int y,
            int w,
            int h
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                FONDO_NAV
        );

        int colorBorde=
                resultado!=null&&
                        resultado.isCompatible()
                        ?TEXTO_VERDE
                        :BORDE;

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                colorBorde
        );

        String titulo=
                "REGLA "+(indice+1);

        if(resultado!=null){
            titulo+=
                    resultado.isCompatible()
                            ?"  ✓"
                            :"  ✗";
        }

        drawString(
                matrixStack,
                font,
                titulo,
                x+7,
                y+7,
                resultado!=null&&
                        resultado.isCompatible()
                        ?TEXTO_VERDE
                        :TEXTO_ACENTO
        );

        String origen=
                traducirOrigenSpawn(
                        regla.getOrigen()
                );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        origen,
                        Math.max(20,w-92)
                ),
                x+84,
                y+7,
                TEXTO_SECUNDARIO
        );

        int mitad=
                Math.max(
                        100,
                        w/2
                );

        lineaSpawnEstado(
                matrixStack,
                x+7,
                y+20,
                mitad-12,
                "Horario",
                listaSpawn(
                        regla.getHorarios(),
                        TipoValorSpawn.HORARIO
                ),
                resultado==null||
                        resultado.isHorario()
        );

        lineaSpawnEstado(
                matrixStack,
                x+mitad,
                y+20,
                w-mitad-7,
                "Clima",
                listaSpawn(
                        regla.getClimas(),
                        TipoValorSpawn.CLIMA
                ),
                resultado==null||
                        resultado.isClima()
        );

        lineaSpawnEstado(
                matrixStack,
                x+7,
                y+33,
                w-14,
                "Bioma",
                listaSpawn(
                        regla.getBiomas(),
                        TipoValorSpawn.BIOMA
                ),
                resultado==null||
                        resultado.isBioma()
        );

        lineaSpawnEstado(
                matrixStack,
                x+7,
                y+46,
                mitad-12,
                "Altura",
                rangoAltura(regla),
                resultado==null||
                        resultado.isAltura()
        );

        lineaSpawnEstado(
                matrixStack,
                x+mitad,
                y+46,
                w-mitad-7,
                "Luna",
                faseLunar(regla),
                resultado==null||
                        resultado.isLuna()
        );

        if(regla.tieneBloquesBase()){
            lineaSpawnEstado(
                    matrixStack,
                    x+7,
                    y+59,
                    w-14,
                    "Suelo",
                    listaSpawn(
                            regla.getBloquesBase(),
                            TipoValorSpawn.BLOQUE
                    ),
                    resultado==null||
                            resultado.isSuelo()
            );
        }
    }

    private void lineaSpawnEstado(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String etiqueta,
            String valor,
            boolean compatible
    ){
        if(w<30){
            return;
        }

        drawString(
                matrixStack,
                font,
                etiqueta+":",
                x,
                y,
                TEXTO_SECUNDARIO
        );

        int valorX=
                x+font.width(etiqueta+":")+5;

        String visible=
                limitarTexto(
                        valor,
                        Math.max(
                                8,
                                w-(valorX-x)
                        )
                );

        drawString(
                matrixStack,
                font,
                visible,
                valorX,
                y,
                compatible
                        ?TEXTO_VERDE
                        :TEXTO_ROJO
        );
    }

    private void lineaSpawn(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String etiqueta,
            String valor
    ){
        if(w<30){
            return;
        }

        drawString(
                matrixStack,
                font,
                etiqueta+":",
                x,
                y,
                TEXTO_SECUNDARIO
        );

        int valorX=
                x+font.width(etiqueta+":")+5;

        String visible=
                limitarTexto(
                        valor,
                        Math.max(
                                8,
                                w-(valorX-x)
                        )
                );

        drawString(
                matrixStack,
                font,
                visible,
                valorX,
                y,
                TEXTO
        );
    }

    private enum TipoValorSpawn{
        HORARIO,
        CLIMA,
        BIOMA,
        BLOQUE
    }

    private String listaSpawn(
            List<String> valores,
            TipoValorSpawn tipo
    ){
        if(valores==null||
                valores.isEmpty()){

            return "Cualquiera";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String valor:valores){
            if(valor==null||
                    valor.trim().isEmpty()){

                continue;
            }

            String traducido;

            if(tipo==TipoValorSpawn.HORARIO){
                traducido=traducirHorarioSpawn(valor);
            }else if(tipo==TipoValorSpawn.CLIMA){
                traducido=traducirClimaSpawn(valor);
            }else if(tipo==TipoValorSpawn.BIOMA){
                traducido=traducirIdentificadorSpawn(valor);
            }else{
                traducido=traducirIdentificadorSpawn(valor);
            }

            if(!traducido.isEmpty()){
                visibles.add(traducido);
            }
        }

        if(visibles.isEmpty()){
            return "Cualquiera";
        }

        return String.join(
                " / ",
                visibles
        );
    }

    private String rangoAltura(
            PokemonSpawnRule regla
    ){
        Integer min=regla.getMinY();
        Integer max=regla.getMaxY();

        if(min==null&&max==null){
            return "Cualquiera";
        }

        if(min!=null&&max!=null){
            return "Y "+min+"-"+max;
        }

        if(min!=null){
            return "Y >= "+min;
        }

        return "Y <= "+max;
    }

    private String faseLunar(
            PokemonSpawnRule regla
    ){
        Integer fase=
                regla.getFaseLunar();

        if(fase==null){
            return "Cualquiera";
        }

        return String.valueOf(fase);
    }

    private String traducirOrigenSpawn(
            String origen
    ){
        String key=
                normalizarForma(origen);

        if(key.contains("standard")){
            return "Estándar";
        }

        if(key.contains("legendary")||
                key.contains("legendaries")){

            return "Legendario";
        }

        if(origen==null||
                origen.trim().isEmpty()){

            return "-";
        }

        return PokemonTranslator.formatear(origen);
    }

    private String traducirHorarioSpawn(
            String horario
    ){
        String key=
                normalizarForma(horario);

        if(key.equals("dawn")){
            return "Amanecer";
        }

        if(key.equals("day")||
                key.equals("daytime")){

            return "Día";
        }

        if(key.equals("dusk")){
            return "Atardecer";
        }

        if(key.equals("night")||
                key.equals("nighttime")){

            return "Noche";
        }

        if(key.equals("morning")){
            return "Mañana";
        }

        if(key.equals("afternoon")){
            return "Tarde";
        }

        if(horario==null||
                horario.trim().isEmpty()){

            return "Cualquiera";
        }

        return PokemonTranslator.formatear(horario);
    }

    private String traducirClimaSpawn(
            String clima
    ){
        String key=
                normalizarForma(clima);

        if(key.equals("clear")){
            return "Despejado";
        }

        if(key.equals("rain")||
                key.equals("raining")){

            return "Lluvia";
        }

        if(key.equals("thunder")||
                key.equals("thunderstorm")){

            return "Tormenta";
        }

        if(clima==null||
                clima.trim().isEmpty()){

            return "Cualquiera";
        }

        return PokemonTranslator.formatear(clima);
    }

    private String traducirIdentificadorSpawn(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "";
        }

        String limpio=
                valor.trim();

        int dosPuntos=
                limpio.indexOf(':');

        if(dosPuntos>=0&&
                dosPuntos<limpio.length()-1){

            limpio=
                    limpio.substring(
                            dosPuntos+1
                    );
        }

        limpio=
                limpio.replace('_',' ')
                        .replace('-',' ')
                        .trim();

        return formatearNombreOriginal(
                limpio
        );
    }

    private String formatearNombreOriginal(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "";
        }

        String[] partes=
                valor.trim()
                        .toLowerCase(Locale.ROOT)
                        .split("\\s+");

        StringBuilder resultado=
                new StringBuilder();

        for(String parte:partes){
            if(parte.isEmpty()){
                continue;
            }

            if(resultado.length()>0){
                resultado.append(' ');
            }

            resultado.append(
                    Character.toUpperCase(
                            parte.charAt(0)
                    )
            );

            if(parte.length()>1){
                resultado.append(
                        parte.substring(1)
                );
            }
        }

        return resultado.toString();
    }

    private void dibujarScrollbarSpawn(
            MatrixStack matrixStack,
            int x,
            int y,
            int h,
            int visibles
    ){
        if(spawns.size()<=visibles){
            return;
        }

        fill(
                matrixStack,
                x,
                y,
                x+2,
                y+h,
                BORDE
        );

        int thumbH=
                Math.max(
                        16,
                        (int)(
                                h*
                                        (visibles/(float)spawns.size())
                        )
                );

        int maxScroll=
                Math.max(
                        1,
                        spawns.size()-visibles
                );

        int recorrido=
                h-thumbH;

        int thumbY=
                y+
                        (int)(
                                recorrido*
                                        (scrollSpawn/(float)maxScroll)
                        );

        fill(
                matrixStack,
                x,
                thumbY,
                x+2,
                thumbY+thumbH,
                ACENTO
        );
    }

    private void limitarScrollSpawn(
            int visibles
    ){
        int maximo=
                Math.max(
                        0,
                        spawns.size()-visibles
                );

        if(scrollSpawn<0){
            scrollSpawn=0;
        }

        if(scrollSpawn>maximo){
            scrollSpawn=maximo;
        }
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

    private static String normalizarForma(
            String texto
    ){
        if(texto==null)return "";

        String valor=
                texto.toLowerCase(Locale.ROOT);

        StringBuilder resultado=
                new StringBuilder();

        for(int i=0;i<valor.length();i++){
            char c=valor.charAt(i);

            if(Character.isLetterOrDigit(c)){
                resultado.append(c);
            }
        }

        String key=
                resultado.toString();

        return key.equals("base")
                ?""
                :key;
    }

    private static boolean dentro(
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

    private void borde(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int color
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+1,
                color
        );

        fill(
                matrixStack,
                x,
                y+h-1,
                x+w,
                y+h,
                color
        );

        fill(
                matrixStack,
                x,
                y,
                x+1,
                y+h,
                color
        );

        fill(
                matrixStack,
                x+w-1,
                y,
                x+w,
                y+h,
                color
        );
    }
}
