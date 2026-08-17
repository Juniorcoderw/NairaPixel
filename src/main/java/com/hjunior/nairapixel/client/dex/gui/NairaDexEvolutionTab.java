package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.evolution.PixelmonEvolutionProvider;
import com.hjunior.nairapixel.core.pixelmon.evolution.PokemonEvolutionData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class NairaDexEvolutionTab extends AbstractGui {
    private static final int FONDO_CARD=0xE3131A22;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;

    private FontRenderer font;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private int scroll;

    private String cacheKey="";
    private List<RutaEvolutiva> rutasCache=
            Collections.emptyList();

    private List<GrupoDestino> destinosRaizCache=
            Collections.emptyList();

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            String pokemon,
            String forma,
            List<PokemonEvolutionData> evoluciones,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        this.font=font;
        this.panelX=x;
        this.panelY=y;
        this.panelW=w;
        this.panelH=h;

        asegurarRutas(
                pokemon,
                forma,
                evoluciones
        );

        drawString(
                matrixStack,
                font,
                "EVOLUCIÓN",
                x,
                y,
                TEXTO_ACENTO
        );

        String resumen;

        if(rutasCache.isEmpty()){
            resumen="Etapa final";
        }else if(destinosRaizCache.size()>1){
            resumen=
                    destinosRaizCache.size()+
                            " evoluciones";
        }else if(rutasCache.size()==1){
            resumen="1 ruta";
        }else{
            resumen=
                    rutasCache.size()+
                            " rutas";
        }

        drawString(
                matrixStack,
                font,
                resumen,
                x+68,
                y,
                TEXTO_SECUNDARIO
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

        if(rutasCache.isEmpty()){
            dibujarEtapaFinal(
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

        dibujarFamilia(
                matrixStack,
                pokemon,
                forma,
                x,
                cardY,
                w,
                cardH,
                mouseX,
                mouseY
        );
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta,
            List<PokemonEvolutionData> evoluciones
    ){
        if(!dentro(
                mouseX,
                mouseY,
                panelX,
                panelY,
                panelW,
                panelH
        )){
            return false;
        }

        if(delta>0){
            scroll--;
        }else if(delta<0){
            scroll++;
        }

        if(usarVistaRamificada()){
            limitarScroll(
                    getFilasRamasTotales(),
                    getFilasRamasVisibles()
            );
        }else{
            limitarScroll(
                    rutasCache.size(),
                    getRutasVisibles()
            );
        }

        return true;
    }

    public void resetScroll(){
        scroll=0;
    }

    private void asegurarRutas(
            String pokemon,
            String forma,
            List<PokemonEvolutionData> evoluciones
    ){
        String key=
                normalizar(
                        pokemon
                )+
                        "|"+
                        normalizar(
                                forma
                        );

        if(key.equals(cacheKey)){
            return;
        }

        cacheKey=key;
        scroll=0;

        List<PokemonEvolutionData> raiz=
                evoluciones==null
                        ?Collections.emptyList()
                        :evoluciones;

        List<RutaEvolutiva> nuevas=
                new ArrayList<>();

        List<PasoEvolutivo> actual=
                new ArrayList<>();

        Set<String> visitados=
                new HashSet<>();

        visitados.add(
                key
        );

        construirRutas(
                pokemon,
                forma,
                raiz,
                actual,
                nuevas,
                visitados,
                0
        );

        rutasCache=
                Collections.unmodifiableList(
                        nuevas
                );

        destinosRaizCache=
                Collections.unmodifiableList(
                        agruparDestinosRaiz(
                                raiz
                        )
                );
    }

    private List<GrupoDestino> agruparDestinosRaiz(
            List<PokemonEvolutionData> evoluciones
    ){
        List<GrupoDestino> grupos=
                new ArrayList<>();

        if(evoluciones==null||
                evoluciones.isEmpty()){

            return grupos;
        }

        for(PokemonEvolutionData evolucion:
                evoluciones){

            if(evolucion==null||
                    evolucion.getDestino()==null||
                    evolucion.getDestino()
                            .trim()
                            .isEmpty()){

                continue;
            }

            String destino=
                    evolucion.getDestino();

            String forma=
                    evolucion.getFormaDestino()==null
                            ?""
                            :evolucion.getFormaDestino();

            String key=
                    normalizar(
                            destino
                    )+
                            "|"+
                            normalizar(
                                    forma
                            );

            GrupoDestino existente=null;

            for(GrupoDestino grupo:
                    grupos){

                if(grupo.getKey()
                        .equals(key)){

                    existente=grupo;
                    break;
                }
            }

            if(existente==null){
                existente=
                        new GrupoDestino(
                                key,
                                destino,
                                forma
                        );

                grupos.add(
                        existente
                );
            }

            existente.agregar(
                    evolucion
            );
        }

        return grupos;
    }

    private boolean usarVistaRamificada(){
        return destinosRaizCache.size()>1;
    }

    private void construirRutas(
            String pokemon,
            String forma,
            List<PokemonEvolutionData> salidas,
            List<PasoEvolutivo> actual,
            List<RutaEvolutiva> resultado,
            Set<String> visitados,
            int profundidad
    ){
        if(profundidad>=4||
                salidas==null||
                salidas.isEmpty()){

            if(!actual.isEmpty()){
                resultado.add(
                        new RutaEvolutiva(
                                actual
                        )
                );
            }

            return;
        }

        boolean agregada=false;

        for(PokemonEvolutionData evolucion:
                salidas){

            if(evolucion==null||
                    evolucion.getDestino()==null||
                    evolucion.getDestino()
                            .trim()
                            .isEmpty()){

                continue;
            }

            String destino=
                    evolucion.getDestino();

            String formaDestino=
                    evolucion.getFormaDestino()==null
                            ?""
                            :evolucion.getFormaDestino();

            String destinoKey=
                    normalizar(
                            destino
                    )+
                            "|"+
                            normalizar(
                                    formaDestino
                            );

            if(visitados.contains(
                    destinoKey
            )){
                continue;
            }

            agregada=true;

            List<PasoEvolutivo> siguiente=
                    new ArrayList<>(
                            actual
                    );

            siguiente.add(
                    new PasoEvolutivo(
                            evolucion
                    )
            );

            Set<String> visitadosRama=
                    new HashSet<>(
                            visitados
                    );

            visitadosRama.add(
                    destinoKey
            );

            List<PokemonEvolutionData> salidasDestino;

            try{
                salidasDestino=
                        PixelmonEvolutionProvider
                                .getEvoluciones(
                                        destino,
                                        formaDestino
                                );
            }catch(Exception e){
                salidasDestino=
                        Collections.emptyList();
            }

            if(salidasDestino==null||
                    salidasDestino.isEmpty()){

                resultado.add(
                        new RutaEvolutiva(
                                siguiente
                        )
                );
            }else{
                construirRutas(
                        destino,
                        formaDestino,
                        salidasDestino,
                        siguiente,
                        resultado,
                        visitadosRama,
                        profundidad+1
                );
            }
        }

        if(!agregada&&
                !actual.isEmpty()){

            resultado.add(
                    new RutaEvolutiva(
                            actual
                    )
            );
        }
    }

    private void dibujarFamilia(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        int origenW=
                Math.min(
                        118,
                        Math.max(
                                96,
                                w/5
                        )
                );

        int divisorX=
                x+origenW;

        fill(
                matrixStack,
                divisorX,
                y+8,
                divisorX+1,
                y+h-8,
                BORDE
        );

        dibujarOrigen(
                matrixStack,
                pokemon,
                forma,
                x,
                y,
                origenW,
                h
        );

        if(usarVistaRamificada()){
            dibujarDestinosRamificados(
                    matrixStack,
                    divisorX+9,
                    y+8,
                    Math.max(
                            110,
                            x+w-(divisorX+9)-7
                    ),
                    Math.max(
                            48,
                            h-16
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }

        int rutasX=
                divisorX+9;

        int rutasY=
                y+8;

        int rutasW=
                Math.max(
                        110,
                        x+w-rutasX-7
                );

        int rutasH=
                Math.max(
                        48,
                        h-16
                );

        int filaH=62;

        int visibles=
                Math.max(
                        1,
                        rutasH/filaH
                );

        limitarScroll(
                rutasCache.size(),
                visibles
        );

        int inicioY;

        if(rutasCache.size()==1){
            inicioY=
                    rutasY+
                            Math.max(
                                    0,
                                    (rutasH-filaH)/2
                            );
        }else{
            inicioY=rutasY;
        }

        for(int fila=0;
            fila<visibles;
            fila++){

            int indice=
                    scroll+fila;

            if(indice>=rutasCache.size()){
                break;
            }

            int yy=
                    inicioY+
                            (fila*filaH);

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            rutasX,
                            yy,
                            rutasW,
                            filaH-5
                    );

            dibujarRuta(
                    matrixStack,
                    rutasCache.get(indice),
                    rutasX,
                    yy,
                    rutasW,
                    filaH-5,
                    hover
            );
        }

        dibujarScrollbar(
                matrixStack,
                x+w-4,
                rutasY,
                rutasH,
                rutasCache.size(),
                visibles
        );
    }

    private void dibujarDestinosRamificados(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        int columnas=
                w>=360
                        ?2
                        :1;

        int gapX=7;
        int gapY=6;

        int cardW=
                Math.max(
                        110,
                        (w-
                                (gapX*(columnas-1))-
                                4)/
                                columnas
                );

        int cardH=53;

        int filasVisibles=
                Math.max(
                        1,
                        (h+gapY)/
                                (cardH+gapY)
                );

        limitarScroll(
                getFilasRamasTotales(),
                filasVisibles
        );

        int indiceInicial=
                scroll*
                        columnas;

        int maxItems=
                filasVisibles*
                        columnas;

        for(int local=0;
            local<maxItems;
            local++){

            int indice=
                    indiceInicial+
                            local;

            if(indice>=
                    destinosRaizCache.size()){

                break;
            }

            int fila=
                    local/
                            columnas;

            int columna=
                    local%
                            columnas;

            int xx=
                    x+
                            columna*
                                    (cardW+gapX);

            int yy=
                    y+
                            fila*
                                    (cardH+gapY);

            GrupoDestino grupo=
                    destinosRaizCache.get(
                            indice
                    );

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            xx,
                            yy,
                            cardW,
                            cardH
                    );

            dibujarDestinoRamificado(
                    matrixStack,
                    grupo,
                    xx,
                    yy,
                    cardW,
                    cardH,
                    hover
            );
        }

        dibujarScrollbar(
                matrixStack,
                x+w-2,
                y,
                h,
                getFilasRamasTotales(),
                filasVisibles
        );
    }

    private void dibujarDestinoRamificado(
            MatrixStack matrixStack,
            GrupoDestino grupo,
            int x,
            int y,
            int w,
            int h,
            boolean hover
    ){
        if(hover){
            fill(
                    matrixStack,
                    x,
                    y,
                    x+w,
                    y+h,
                    FONDO_HOVER
            );
        }

        int sprite=34;

        int spriteX=x+5;
        int spriteY=
                y+
                        (h-sprite)/2;

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                grupo.getDestino(),
                grupo.getForma(),
                spriteX,
                spriteY,
                sprite
        );

        int textoX=
                spriteX+
                        sprite+
                        7;

        int textoW=
                Math.max(
                        40,
                        w-
                                (textoX-x)-
                                5
                );

        String nombre=
                grupo.getDestino();

        String forma=
                formaVisible(
                        grupo.getForma()
                );

        if(!forma.isEmpty()){
            nombre+=
                    " ("+
                            forma+
                            ")";
        }

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        nombre,
                        textoW
                ),
                textoX,
                y+7,
                TEXTO
        );

        String requisito=
                describirRequisitosGrupo(
                        grupo
                );

        List<String> lineas=
                envolverTexto(
                        requisito,
                        textoW
                );

        int max=
                Math.min(
                        2,
                        lineas.size()
                );

        for(int i=0;i<max;i++){
            drawString(
                    matrixStack,
                    font,
                    lineas.get(i),
                    textoX,
                    y+22+(i*11),
                    i==0
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );
        }
    }

    private String describirRequisitosGrupo(
            GrupoDestino grupo
    ){
        List<String> requisitos=
                new ArrayList<>();

        for(PokemonEvolutionData evolucion:
                grupo.getReglas()){

            String requisito=
                    describirRequisito(
                            evolucion
                    );

            if(requisito!=null&&
                    !requisito.trim().isEmpty()&&
                    !requisitos.contains(
                            requisito
                    )){

                requisitos.add(
                        requisito
                );
            }
        }

        if(requisitos.isEmpty()){
            return "Condición especial";
        }

        if(requisitos.size()==1){
            return requisitos.get(0);
        }

        StringBuilder texto=
                new StringBuilder();

        int max=
                Math.min(
                        2,
                        requisitos.size()
                );

        for(int i=0;i<max;i++){
            if(i>0){
                texto.append(
                        " / "
                );
            }

            texto.append(
                    requisitos.get(i)
            );
        }

        if(requisitos.size()>max){
            texto.append(
                    " +"
            );

            texto.append(
                    requisitos.size()-max
            );
        }

        return texto.toString();
    }

    private int getFilasRamasTotales(){
        int columnas=
                panelW>=500
                        ?2
                        :1;

        return (destinosRaizCache.size()+
                columnas-1)/
                columnas;
    }

    private int getFilasRamasVisibles(){
        int cardH=
                Math.max(
                        54,
                        panelH-17
                );

        int contenidoH=
                Math.max(
                        48,
                        cardH-16
                );

        return Math.max(
                1,
                (contenidoH+6)/
                        59
        );
    }

    private void dibujarOrigen(
            MatrixStack matrixStack,
            String pokemon,
            String forma,
            int x,
            int y,
            int w,
            int h
    ){
        int sprite=52;

        int spriteX=
                x+
                        (w-sprite)/2;

        int spriteY=
                y+
                        Math.max(
                                12,
                                (h-sprite)/2-12
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
                spriteY+sprite+7,
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
                    spriteY+sprite+20,
                    TEXTO_SECUNDARIO
            );
        }
    }

    private void dibujarRuta(
            MatrixStack matrixStack,
            RutaEvolutiva ruta,
            int x,
            int y,
            int w,
            int h,
            boolean hover
    ){
        if(hover){
            fill(
                    matrixStack,
                    x,
                    y,
                    x+w,
                    y+h,
                    FONDO_HOVER
            );
        }

        List<PasoEvolutivo> pasos=
                ruta.getPasos();

        if(pasos.isEmpty()){
            return;
        }

        int cantidad=
                pasos.size();

        int gap=8;

        int bloqueW=
                Math.max(
                        92,
                        Math.min(
                                150,
                                (w-(gap*(cantidad-1)))/
                                        cantidad
                        )
                );

        int totalW=
                (bloqueW*cantidad)+
                        (gap*(cantidad-1));

        if(totalW>w){
            bloqueW=
                    Math.max(
                            82,
                            (w-(gap*(cantidad-1)))/
                                    cantidad
                    );
        }

        int xx=x;

        for(int i=0;i<cantidad;i++){
            PasoEvolutivo paso=
                    pasos.get(i);

            if(i>0){
                dibujarConector(
                        matrixStack,
                        xx-6,
                        y+h/2
                );
            }

            dibujarPaso(
                    matrixStack,
                    paso,
                    xx,
                    y,
                    bloqueW,
                    h
            );

            xx+=bloqueW+gap;
        }
    }

    private void dibujarPaso(
            MatrixStack matrixStack,
            PasoEvolutivo paso,
            int x,
            int y,
            int w,
            int h
    ){
        PokemonEvolutionData evolucion=
                paso.getEvolucion();

        int sprite=34;

        int spriteX=
                x+4;

        int spriteY=
                y+
                        Math.max(
                                4,
                                (h-sprite)/2
                        );

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                evolucion.getDestino(),
                evolucion.getFormaDestino()==null
                        ?""
                        :evolucion.getFormaDestino(),
                spriteX,
                spriteY,
                sprite
        );

        int textoX=
                spriteX+sprite+6;

        int textoW=
                Math.max(
                        28,
                        w-(textoX-x)-3
                );

        String destino=
                evolucion.getDestino()==null
                        ?"Destino"
                        :evolucion.getDestino();

        String forma=
                formaVisible(
                        evolucion.getFormaDestino()
                );

        if(!forma.isEmpty()){
            destino+=
                    " ("+
                            forma+
                            ")";
        }

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        destino,
                        textoW
                ),
                textoX,
                y+8,
                TEXTO
        );

        String requisito=
                describirRequisito(
                        evolucion
                );

        List<String> lineas=
                envolverTexto(
                        requisito,
                        textoW
                );

        int max=
                Math.min(
                        2,
                        lineas.size()
                );

        for(int i=0;i<max;i++){
            drawString(
                    matrixStack,
                    font,
                    lineas.get(i),
                    textoX,
                    y+23+(i*11),
                    i==0
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );
        }
    }

    private void dibujarConector(
            MatrixStack matrixStack,
            int x,
            int y
    ){
        drawString(
                matrixStack,
                font,
                "→",
                x-4,
                y-4,
                TEXTO_ACENTO
        );
    }

    private void dibujarEtapaFinal(
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

        String nombre=
                pokemon==null||
                        pokemon.trim().isEmpty()
                        ?"Pokémon"
                        :pokemon;

        String formaTexto=
                formaVisible(
                        forma
                );

        if(!formaTexto.isEmpty()){
            nombre+=
                    " ("+
                            formaTexto+
                            ")";
        }

        drawString(
                matrixStack,
                font,
                nombre,
                x+
                        (w-font.width(nombre))/2,
                spriteY+sprite+8,
                TEXTO
        );

        String mensaje=
                "Etapa evolutiva final";

        drawString(
                matrixStack,
                font,
                mensaje,
                x+
                        (w-font.width(mensaje))/2,
                spriteY+sprite+23,
                TEXTO_SECUNDARIO
        );
    }

    private String describirRequisito(
            PokemonEvolutionData ruta
    ){
        List<String> partes=
                new ArrayList<>();

        if(ruta.tieneNivel()){
            partes.add(
                    "Nivel "+
                            ruta.getNivel()
            );
        }

        if(ruta.tieneObjeto()){
            partes.add(
                    limpiarDato(
                            ruta.getObjeto()
                    )
            );
        }

        if(ruta.tieneIntercambioCon()){
            partes.add(
                    "Intercambio con "+
                            limpiarDato(
                                    ruta.getIntercambioCon()
                            )
            );
        }

        if(ruta.tieneCondiciones()){
            for(String condicion:
                    ruta.getCondiciones()){

                String texto=
                        traducirCondicion(
                                condicion
                        );

                if(!texto.isEmpty()&&
                        !partes.contains(texto)){

                    partes.add(texto);
                }
            }
        }

        if(partes.isEmpty()){
            String tipo=
                    traducirTipoEvolucion(
                            ruta.getTipo()
                    );

            if(!tipo.isEmpty()){
                partes.add(tipo);
            }
        }

        return partes.isEmpty()
                ?"Condición especial"
                :String.join(
                " · ",
                partes
        );
    }

    private String traducirTipoEvolucion(
            String tipo
    ){
        if(tipo==null||
                tipo.trim().isEmpty()){

            return "";
        }

        String key=
                tipo.trim()
                        .toUpperCase(Locale.ROOT);

        if(key.contains("LEVEL")){
            return "Subir de nivel";
        }

        if(key.contains("TRADE")){
            return "Intercambio";
        }

        if(key.contains("INTERACT")||
                key.contains("ITEM")){

            return "Usar objeto";
        }

        return limpiarDato(
                tipo
        );
    }

    private String traducirCondicion(
            String condicion
    ){
        if(condicion==null||
                condicion.trim().isEmpty()){

            return "";
        }

        String original=
                condicion.trim();

        String normal=
                original.toLowerCase(
                        Locale.ROOT
                );

        String valor=
                extraerValor(
                        original
                );

        if(normal.contains("friendship")){
            return valor.isEmpty()
                    ?"Amistad alta"
                    :"Amistad ≥ "+valor;
        }

        if(normal.contains("timeofday")||
                normal.startsWith("time")){

            return "Horario: "+
                    traducirHorario(
                            valor
                    );
        }

        if(normal.contains("movetype")){
            return "Movimiento tipo "+
                    PokemonTranslator.tipo(
                            valor
                    );
        }

        if(normal.startsWith("move")||
                normal.contains("knownmove")){

            return "Conoce "+
                    limpiarDato(
                            valor
                    );
        }

        if(normal.contains("evorock")){
            return "Cerca de "+
                    limpiarDato(
                            valor
                    );
        }

        if(normal.contains("partypokemon")){
            return "Equipo: "+
                    limpiarDato(
                            valor
                    );
        }

        if(normal.contains("partytype")){
            return "Equipo tipo "+
                    PokemonTranslator.tipo(
                            valor
                    );
        }

        if(normal.contains("emptyhand")){
            return "Mano vacía";
        }

        if(normal.startsWith("form")||
                normal.contains("form:")||
                normal.contains("form=")){

            return "Forma "+
                    PokemonTranslator.forma(
                            valor
                    );
        }

        if(normal.contains("palette")){
            return "Paleta "+
                    limpiarDato(
                            valor
                    );
        }

        return limpiarCondicionGenerica(
                original
        );
    }

    private String limpiarCondicionGenerica(
            String condicion
    ){
        String texto=
                condicion.replace('=', ':');

        int separador=
                texto.indexOf(':');

        if(separador>0&&
                separador<texto.length()-1){

            String clave=
                    texto.substring(
                            0,
                            separador
                    );

            String valor=
                    texto.substring(
                            separador+1
                    );

            return limpiarDato(clave)+
                    ": "+
                    limpiarDato(valor);
        }

        return limpiarDato(
                texto
        );
    }

    private String extraerValor(
            String texto
    ){
        if(texto==null){
            return "";
        }

        int dosPuntos=
                texto.indexOf(':');

        int igual=
                texto.indexOf('=');

        int espacio=
                texto.indexOf(' ');

        int indice=-1;

        if(dosPuntos>=0){
            indice=dosPuntos;
        }

        if(igual>=0&&
                (indice<0||igual<indice)){

            indice=igual;
        }

        if(espacio>=0&&
                (indice<0||espacio<indice)){

            indice=espacio;
        }

        if(indice<0||
                indice>=texto.length()-1){

            return "";
        }

        return texto.substring(
                indice+1
        ).trim();
    }

    private String traducirHorario(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "especial";
        }

        String key=
                valor.trim()
                        .toUpperCase(Locale.ROOT);

        if(key.contains("DAY")){
            return "Día";
        }

        if(key.contains("NIGHT")){
            return "Noche";
        }

        if(key.contains("DAWN")){
            return "Amanecer";
        }

        if(key.contains("DUSK")){
            return "Atardecer";
        }

        return limpiarDato(
                valor
        );
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

    private String limpiarDato(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "-";
        }

        String limpio=
                valor.trim();

        int dosPuntos=
                limpio.indexOf(':');

        if(dosPuntos>=0&&
                dosPuntos<limpio.length()-1){

            String namespace=
                    limpio.substring(
                            0,
                            dosPuntos
                    );

            if(namespace.equalsIgnoreCase("pixelmon")||
                    namespace.equalsIgnoreCase("minecraft")){

                limpio=
                        limpio.substring(
                                dosPuntos+1
                        );
            }
        }

        return PokemonTranslator.formatear(
                limpio
        );
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

    private void dibujarScrollbar(
            MatrixStack matrixStack,
            int x,
            int y,
            int h,
            int total,
            int visibles
    ){
        if(total<=visibles){
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
                                        (visibles/(float)total)
                        )
                );

        int maxScroll=
                Math.max(
                        1,
                        total-visibles
                );

        int recorrido=
                h-thumbH;

        int thumbY=
                y+
                        (int)(
                                recorrido*
                                        (scroll/(float)maxScroll)
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

    private int getRutasVisibles(){
        int cardH=
                Math.max(
                        54,
                        panelH-17
                );

        int rutasH=
                Math.max(
                        48,
                        cardH-16
                );

        return Math.max(
                1,
                rutasH/62
        );
    }

    private void limitarScroll(
            int total,
            int visibles
    ){
        int max=
                Math.max(
                        0,
                        total-visibles
                );

        if(scroll<0){
            scroll=0;
        }

        if(scroll>max){
            scroll=max;
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

    private static String normalizar(
            String valor
    ){
        return valor==null
                ?""
                :valor.trim()
                .toLowerCase(
                        Locale.ROOT
                );
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
        fill(matrixStack,x,y,x+w,y+1,color);
        fill(matrixStack,x,y+h-1,x+w,y+h,color);
        fill(matrixStack,x,y,x+1,y+h,color);
        fill(matrixStack,x+w-1,y,x+w,y+h,color);
    }

    private static final class GrupoDestino{
        private final String key;
        private final String destino;
        private final String forma;
        private final List<PokemonEvolutionData> reglas=
                new ArrayList<>();

        private GrupoDestino(
                String key,
                String destino,
                String forma
        ){
            this.key=key;
            this.destino=destino;
            this.forma=forma==null
                    ?""
                    :forma;
        }

        private void agregar(
                PokemonEvolutionData evolucion
        ){
            if(evolucion!=null){
                reglas.add(
                        evolucion
                );
            }
        }

        private String getKey(){
            return key;
        }

        private String getDestino(){
            return destino;
        }

        private String getForma(){
            return forma;
        }

        private List<PokemonEvolutionData> getReglas(){
            return reglas;
        }
    }

    private static final class PasoEvolutivo{
        private final PokemonEvolutionData evolucion;

        private PasoEvolutivo(
                PokemonEvolutionData evolucion
        ){
            this.evolucion=evolucion;
        }

        private PokemonEvolutionData getEvolucion(){
            return evolucion;
        }
    }

    private static final class RutaEvolutiva{
        private final List<PasoEvolutivo> pasos;

        private RutaEvolutiva(
                List<PasoEvolutivo> pasos
        ){
            this.pasos=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    pasos
                            )
                    );
        }

        private List<PasoEvolutivo> getPasos(){
            return pasos;
        }
    }
}
