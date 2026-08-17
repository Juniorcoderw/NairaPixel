package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.client.util.PixelmonSpanishLang;
import com.hjunior.nairapixel.core.pixelmon.moves.MoveLearnSource;
import com.hjunior.nairapixel.core.pixelmon.moves.PokemonMoveData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

final class NairaDexMovesTab extends AbstractGui {
    private static final int FONDO_NAV=0xF20A0E14;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int FONDO_ACTIVO=0xE32B3440;
    private static final int FONDO_CARD=0xE3131A22;
    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;
    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;

    private int x;
    private int y;
    private int w;
    private int h;
    private int scroll;

    private Filtro filtro=Filtro.TODOS;

    private final int[] filtroX=new int[6];
    private final int[] filtroW=new int[6];
    private int filtroY;
    private final int filtroH=18;

    private FontRenderer font;

    private final Map<String,String> nombresTraducidos=
            new HashMap<>();

    private PokemonMoveData movimientoSeleccionado;

    private List<PokemonMoveData> ultimosVisibles=
            Collections.emptyList();

    private int listaY;
    private int filaH=18;
    private int filasVisibles;

    private int volverX;
    private int volverY;
    private int volverW=48;
    private int volverH=18;

    private String busquedaMoves="";
    private boolean busquedaActiva;
    private int busquedaX;
    private int busquedaY;
    private int busquedaW;
    private int busquedaH=18;

    NairaDexMovesTab(){
        PixelmonSpanishLang.cargar();
    }

    private enum Filtro{
        TODOS,
        NIVEL,
        TMTR,
        TUTOR,
        EGG,
        TRANSFER
    }

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            List<PokemonMoveData> movimientos,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        this.font=font;
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;

        List<PokemonMoveData> origen=
                movimientos==null
                        ?Collections.emptyList()
                        :movimientos;

        List<PokemonMoveData> visibles=
                getMovimientosFiltrados(
                        origen
                );

        ultimosVisibles=visibles;

        if(movimientoSeleccionado!=null){
            dibujarDetalleMovimiento(
                    matrixStack,
                    movimientoSeleccionado,
                    x,
                    y,
                    w,
                    h,
                    mouseX,
                    mouseY
            );

            return;
        }

        drawString(
                matrixStack,
                font,
                "MOVIMIENTOS",
                x,
                y,
                TEXTO_ACENTO
        );

        String contador=
                visibles.size()+
                        "/"+
                        origen.size();

        drawString(
                matrixStack,
                font,
                contador,
                x+82,
                y,
                TEXTO_SECUNDARIO
        );

        busquedaW=
                Math.min(
                        190,
                        Math.max(
                                120,
                                w/3
                        )
                );

        busquedaX=
                x+w-busquedaW;

        busquedaY=
                y-4;

        boolean hoverBusqueda=
                dentro(
                        mouseX,
                        mouseY,
                        busquedaX,
                        busquedaY,
                        busquedaW,
                        busquedaH
                );

        fill(
                matrixStack,
                busquedaX,
                busquedaY,
                busquedaX+busquedaW,
                busquedaY+busquedaH,
                busquedaActiva
                        ?FONDO_ACTIVO
                        :hoverBusqueda
                        ?FONDO_HOVER
                        :FONDO_NAV
        );

        borde(
                matrixStack,
                busquedaX,
                busquedaY,
                busquedaW,
                busquedaH,
                busquedaActiva
                        ?ACENTO
                        :BORDE
        );

        String textoBusqueda;

        if(busquedaMoves.isEmpty()){
            textoBusqueda=
                    busquedaActiva
                            ?"_"
                            :"Buscar movimiento...";
        }else{
            textoBusqueda=
                    busquedaMoves+
                            (busquedaActiva
                                    ?"_"
                                    :"");
        }

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        textoBusqueda,
                        busquedaW-10
                ),
                busquedaX+5,
                busquedaY+5,
                busquedaMoves.isEmpty()&&
                        !busquedaActiva
                        ?TEXTO_SECUNDARIO
                        :TEXTO
        );

        filtroY=y+15;

        String[] nombres={
                "Todos",
                "Nivel",
                "TM/TR",
                "Tutor",
                "Egg",
                "Transfer"
        };

        Filtro[] valores={
                Filtro.TODOS,
                Filtro.NIVEL,
                Filtro.TMTR,
                Filtro.TUTOR,
                Filtro.EGG,
                Filtro.TRANSFER
        };

        int[] anchos={
                38,
                38,
                43,
                40,
                32,
                52
        };

        int xx=x;

        for(int i=0;i<nombres.length;i++){
            filtroX[i]=xx;
            filtroW[i]=anchos[i];

            boolean activo=
                    filtro==valores[i];

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            xx,
                            filtroY,
                            anchos[i],
                            filtroH
                    );

            fill(
                    matrixStack,
                    xx,
                    filtroY,
                    xx+anchos[i],
                    filtroY+filtroH,
                    activo
                            ?FONDO_ACTIVO
                            :hover
                            ?FONDO_HOVER
                            :FONDO_NAV
            );

            borde(
                    matrixStack,
                    xx,
                    filtroY,
                    anchos[i],
                    filtroH,
                    activo
                            ?ACENTO
                            :BORDE
            );

            int tx=
                    xx+
                            (anchos[i]-
                                    font.width(
                                            nombres[i]
                                    ))/2;

            drawString(
                    matrixStack,
                    font,
                    nombres[i],
                    tx,
                    filtroY+5,
                    activo
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );

            xx+=anchos[i]+4;
        }

        int tablaY=
                filtroY+25;

        int headerH=14;
        filaH=18;

        dibujarCabecera(
                matrixStack,
                x,
                tablaY,
                w
        );

        listaY=
                tablaY+headerH;

        int listaH=
                Math.max(
                        18,
                        h-(listaY-y)
                );

        filasVisibles=
                Math.max(
                        1,
                        listaH/filaH
                );

        limitarScroll(
                visibles.size(),
                filasVisibles
        );

        if(visibles.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    "Sin movimientos para esta búsqueda o filtro.",
                    x,
                    listaY+12,
                    TEXTO_SECUNDARIO
            );

            return;
        }

        for(int fila=0;
            fila<filasVisibles;
            fila++){

            int indice=
                    scroll+fila;

            if(indice>=visibles.size()){
                break;
            }

            PokemonMoveData movimiento=
                    visibles.get(indice);

            int yy=
                    listaY+
                            (fila*filaH);

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            x,
                            yy,
                            w-5,
                            filaH
                    );

            if(hover){
                fill(
                        matrixStack,
                        x,
                        yy,
                        x+w-5,
                        yy+filaH,
                        FONDO_HOVER
                );
            }

            dibujarFila(
                    matrixStack,
                    movimiento,
                    x,
                    yy+5,
                    w
            );
        }

        dibujarScrollbar(
                matrixStack,
                x+w-3,
                listaY,
                listaH,
                visibles.size(),
                filasVisibles
        );
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY
    ){
        if(movimientoSeleccionado!=null){
            if(dentro(
                    mouseX,
                    mouseY,
                    volverX,
                    volverY,
                    volverW,
                    volverH
            )){
                movimientoSeleccionado=null;
                return true;
            }

            return false;
        }

        if(dentro(
                mouseX,
                mouseY,
                busquedaX,
                busquedaY,
                busquedaW,
                busquedaH
        )){
            busquedaActiva=true;
            return true;
        }

        busquedaActiva=false;

        if(mouseY>=listaY&&
                filaH>0&&
                filasVisibles>0){

            int fila=
                    (int)(
                            (mouseY-listaY)/
                                    filaH
                    );

            if(fila>=0&&
                    fila<filasVisibles){

                int indice=
                        scroll+fila;

                if(indice>=0&&
                        indice<ultimosVisibles.size()){

                    movimientoSeleccionado=
                            ultimosVisibles.get(indice);

                    busquedaActiva=false;

                    return true;
                }
            }
        }

        if(mouseY<filtroY||
                mouseY>=filtroY+filtroH){

            return false;
        }

        Filtro[] valores={
                Filtro.TODOS,
                Filtro.NIVEL,
                Filtro.TMTR,
                Filtro.TUTOR,
                Filtro.EGG,
                Filtro.TRANSFER
        };

        for(int i=0;i<filtroX.length;i++){
            if(dentro(
                    mouseX,
                    mouseY,
                    filtroX[i],
                    filtroY,
                    filtroW[i],
                    filtroH
            )){
                filtro=valores[i];
                scroll=0;
                return true;
            }
        }

        return false;
    }

    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ){
        if(movimientoSeleccionado!=null){
            if(keyCode==256){
                movimientoSeleccionado=null;
                return true;
            }

            return false;
        }

        if(!busquedaActiva){
            return false;
        }

        if(keyCode==256){
            busquedaActiva=false;
            return true;
        }

        if(keyCode==257||
                keyCode==335){
            busquedaActiva=false;
            return true;
        }

        if(keyCode==259){
            if(!busquedaMoves.isEmpty()){
                busquedaMoves=
                        busquedaMoves.substring(
                                0,
                                busquedaMoves.length()-1
                        );

                scroll=0;
            }

            return true;
        }

        if(keyCode==261){
            busquedaMoves="";
            scroll=0;
            return true;
        }

        return false;
    }

    public boolean charTyped(
            char codePoint,
            int modifiers
    ){
        if(!busquedaActiva||
                Character.isISOControl(
                        codePoint
                )){

            return false;
        }

        if(busquedaMoves.length()>=40){
            return true;
        }

        String candidato=
                busquedaMoves+
                        codePoint;

        if(font!=null&&
                font.width(
                        candidato+"_"
                )>
                        busquedaW-10){

            return true;
        }

        busquedaMoves=
                candidato;

        scroll=0;

        return true;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta,
            List<PokemonMoveData> movimientos
    ){
        if(movimientoSeleccionado!=null){
            return false;
        }

        if(!dentro(
                mouseX,
                mouseY,
                x,
                y,
                w,
                h
        )){
            return false;
        }

        if(delta>0){
            scroll-=3;
        }else if(delta<0){
            scroll+=3;
        }

        List<PokemonMoveData> visibles=
                getMovimientosFiltrados(
                        movimientos==null
                                ?Collections.emptyList()
                                :movimientos
                );

        int listaH=
                Math.max(
                        18,
                        h-57
                );

        int filasVisibles=
                Math.max(
                        1,
                        listaH/18
                );

        limitarScroll(
                visibles.size(),
                filasVisibles
        );

        return true;
    }

    public void resetScroll(){
        scroll=0;
        movimientoSeleccionado=null;
        busquedaMoves="";
        busquedaActiva=false;
    }

    private void dibujarDetalleMovimiento(
            MatrixStack matrixStack,
            PokemonMoveData movimiento,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        Optional<ImmutableAttack> ataque=
                obtenerAtaque(
                        movimiento
                );

        String nombreIngles=
                movimiento.getNombre()==null
                        ?""
                        :movimiento.getNombre();

        String nombre=
                obtenerNombreTraducido(
                        ataque,
                        nombreIngles
                );

        String titulo=
                nombre;

        if(!nombreIngles.isEmpty()&&
                !nombre.equalsIgnoreCase(
                        nombreIngles
                )){
            titulo=
                    nombre+
                            " ("+
                            nombreIngles+
                            ")";
        }

        String tipo=
                PokemonTranslator.tipo(
                        movimiento.getTipo()
                );

        String categoria=
                traducirCategoria(
                        movimiento.getCategoria()
                );

        if(ataque.isPresent()){
            categoria=
                    traducirCategoria(
                            String.valueOf(
                                    ataque.get()
                                            .getAttackCategory()
                            )
                    );
        }

        int panelX=x;
        int panelY=y;
        int panelW=w;
        int panelH=h;

        fill(
                matrixStack,
                panelX,
                panelY,
                panelX+panelW,
                panelY+panelH,
                FONDO_CARD
        );

        borde(
                matrixStack,
                panelX,
                panelY,
                panelW,
                panelH,
                BORDE
        );

        fill(
                matrixStack,
                panelX,
                panelY,
                panelX+2,
                panelY+panelH,
                ACENTO
        );

        int tx=panelX+10;
        int right=panelX+panelW-10;

        // ------------------------------------------------------------
        // CABECERA
        // ------------------------------------------------------------
        volverW=50;
        volverH=18;
        volverX=right-volverW;
        volverY=panelY+7;

        boolean hoverVolver=
                dentro(
                        mouseX,
                        mouseY,
                        volverX,
                        volverY,
                        volverW,
                        volverH
                );

        fill(
                matrixStack,
                volverX,
                volverY,
                volverX+volverW,
                volverY+volverH,
                hoverVolver
                        ?FONDO_HOVER
                        :FONDO_NAV
        );

        borde(
                matrixStack,
                volverX,
                volverY,
                volverW,
                volverH,
                hoverVolver
                        ?ACENTO
                        :BORDE
        );

        drawString(
                matrixStack,
                font,
                "← Volver",
                volverX+6,
                volverY+5,
                hoverVolver
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );

        int headerY=
                panelY+12;

        int tituloW=
                Math.max(
                        80,
                        volverX-tx-8
                );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        titulo,
                        tituloW
                ),
                tx,
                headerY,
                TEXTO
        );

        int aprendizajeTituloY=
                panelY+34;

        drawString(
                matrixStack,
                font,
                "APRENDIZAJE",
                tx,
                aprendizajeTituloY,
                TEXTO_ACENTO
        );

        String aprendizaje=
                formatearFuentesResumen(
                        movimiento.getFuentes()
                );

        int aprendizajeX=
                tx+font.width(
                        "APRENDIZAJE"
                )+10;

        int aprendizajeW=
                Math.max(
                        60,
                        right-aprendizajeX
                );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        aprendizaje,
                        aprendizajeW
                ),
                aprendizajeX,
                aprendizajeTituloY,
                TEXTO
        );

        int separadorCabecera=
                panelY+51;

        fill(
                matrixStack,
                tx,
                separadorCabecera,
                right,
                separadorCabecera+1,
                BORDE
        );

        // ------------------------------------------------------------
        // DATOS EN DOS FILAS
        // ------------------------------------------------------------
        int datosY=
                separadorCabecera+10;

        int col1=tx;
        int col2=tx+(int)(panelW*0.25F);
        int col3=tx+(int)(panelW*0.50F);
        int col4=tx+(int)(panelW*0.73F);

        dibujarDatoCompacto(
                matrixStack,
                col1,
                datosY,
                "Tipo",
                tipo
        );

        dibujarDatoCompacto(
                matrixStack,
                col2,
                datosY,
                "Categoría",
                categoria
        );

        dibujarDatoCompacto(
                matrixStack,
                col3,
                datosY,
                "Pot.",
                numero(
                        movimiento.getPotencia()
                )
        );

        dibujarDatoCompacto(
                matrixStack,
                col4,
                datosY,
                "Prec.",
                numero(
                        movimiento.getPrecision()
                )
        );

        int datosY2=
                datosY+17;

        dibujarDatoCompacto(
                matrixStack,
                col1,
                datosY2,
                "PP",
                numero(
                        movimiento.getPP()
                )
        );

        if(ataque.isPresent()){
            ImmutableAttack real=
                    ataque.get();

            dibujarDatoCompacto(
                    matrixStack,
                    col2,
                    datosY2,
                    "Contacto",
                    real.getMakesContact()
                            ?"Sí"
                            :"No"
            );

            dibujarDatoCompacto(
                    matrixStack,
                    col3,
                    datosY2,
                    "Efecto sec.",
                    real.hasSecondaryEffect()
                            ?"Sí"
                            :"No"
            );
        }

        int separadorDatos=
                datosY2+17;

        fill(
                matrixStack,
                tx,
                separadorDatos,
                right,
                separadorDatos+1,
                BORDE
        );

        // ------------------------------------------------------------
        // DESCRIPCIÓN
        // ------------------------------------------------------------
        int descripcionTituloY=
                separadorDatos+9;

        drawString(
                matrixStack,
                font,
                "DESCRIPCIÓN",
                tx,
                descripcionTituloY,
                TEXTO_ACENTO
        );

        int descripcionY=
                descripcionTituloY+15;

        String descripcion=
                obtenerDescripcionTraducida(
                        ataque
                );

        if(descripcion.isEmpty()){
            descripcion=
                    "No hay una descripción en español disponible para este movimiento.";
        }

        int anchoDescripcion=
                Math.max(
                        100,
                        panelW-20
                );

        List<String> lineasDescripcion=
                envolverTexto(
                        descripcion,
                        anchoDescripcion
                );

        int limiteY=
                panelY+
                        panelH-
                        10;

        int maxLineasDescripcion=
                Math.max(
                        0,
                        (limiteY-descripcionY)/11
                );

        for(int i=0;
            i<lineasDescripcion.size()&&
                    i<maxLineasDescripcion;
            i++){

            String linea=
                    lineasDescripcion.get(i);

            if(i==maxLineasDescripcion-1&&
                    lineasDescripcion.size()>
                            maxLineasDescripcion){

                linea=
                        limitarTexto(
                                linea+"…",
                                anchoDescripcion
                        );
            }

            drawString(
                    matrixStack,
                    font,
                    linea,
                    tx,
                    descripcionY+
                            (i*11),
                    TEXTO_SECUNDARIO
            );
        }
    }

    private Optional<ImmutableAttack> obtenerAtaque(
            PokemonMoveData movimiento
    ){
        if(movimiento==null||
                movimiento.getNombre()==null||
                movimiento.getNombre()
                        .trim()
                        .isEmpty()){

            return Optional.empty();
        }

        try{
            return AttackRegistry
                    .getAttackBaseFromEnglishName(
                            movimiento.getNombre()
                    );
        }catch(Exception e){
            return Optional.empty();
        }
    }

    private String obtenerNombreTraducido(
            Optional<ImmutableAttack> ataque,
            String fallback
    ){
        if(ataque!=null&&
                ataque.isPresent()){

            try{
                String key=
                        ataque.get()
                                .getTranslationKey();

                return PixelmonSpanishLang.traducir(
                        key,
                        PokemonTranslator.formatear(
                                fallback
                        )
                );

            }catch(Exception ignored){}
        }

        return PokemonTranslator.formatear(
                fallback
        );
    }

    private String obtenerDescripcionTraducida(
            Optional<ImmutableAttack> ataque
    ){
        if(ataque==null||
                !ataque.isPresent()){

            return "";
        }

        try{
            ImmutableAttack real=
                    ataque.get();

            String descriptionKey=
                    real.getDescriptionKey();

            String traducido=
                    PixelmonSpanishLang.traducir(
                            descriptionKey,
                            ""
                    );

            if(traducido!=null&&
                    !traducido.trim().isEmpty()){

                return traducido.trim();
            }

            String translationKey=
                    real.getTranslationKey();

            if(translationKey!=null&&
                    !translationKey.trim().isEmpty()){

                traducido=
                        PixelmonSpanishLang.traducir(
                                translationKey+
                                        ".description",
                                ""
                        );

                if(traducido!=null&&
                        !traducido.trim().isEmpty()){

                    return traducido.trim();
                }
            }

            String nombre=
                    real.getAttackName();

            if(nombre!=null&&
                    !nombre.trim().isEmpty()){

                String keyNombre=
                        "attack."+
                                nombre.trim()
                                        .toLowerCase(Locale.ROOT)
                                        .replace(" ", "_")+
                                ".description";

                traducido=
                        PixelmonSpanishLang.traducir(
                                keyNombre,
                                ""
                        );

                if(traducido!=null&&
                        !traducido.trim().isEmpty()){

                    return traducido.trim();
                }
            }

        }catch(Exception ignored){}

        return "";
    }

    private String traducirCategoria(
            String categoria
    ){
        if(categoria==null||
                categoria.trim().isEmpty()){

            return "-";
        }

        String key=
                categoria.trim()
                        .toUpperCase(Locale.ROOT);

        if(key.contains("PHYSICAL")){
            return "Físico";
        }

        if(key.contains("SPECIAL")){
            return "Especial";
        }

        if(key.contains("STATUS")){
            return "Estado";
        }

        return PokemonTranslator.formatear(
                categoria
        );
    }

    private void dibujarDatoCompacto(
            MatrixStack matrixStack,
            int x,
            int y,
            String etiqueta,
            String valor
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
                        valor.isEmpty()
                        ?"-"
                        :valor,
                x+font.width(
                        etiqueta+":"
                )+4,
                y,
                TEXTO
        );
    }

    private String formatearFuentesResumen(
            List<MoveLearnSource> fuentes
    ){
        if(fuentes==null||
                fuentes.isEmpty()){

            return "-";
        }

        List<Integer> niveles=
                new ArrayList<>();

        List<String> tecnicos=
                new ArrayList<>();

        boolean tutor=false;
        boolean egg=false;
        boolean transfer=false;

        List<String> otros=
                new ArrayList<>();

        for(MoveLearnSource fuente:
                fuentes){

            if(fuente==null){
                continue;
            }

            String metodo=
                    normalizarMetodo(
                            fuente.getMetodo()
                    );

            if(metodo.equals("LEVEL")){
                if(fuente.getNivel()!=null&&
                        !niveles.contains(
                                fuente.getNivel()
                        )){

                    niveles.add(
                            fuente.getNivel()
                    );
                }

                continue;
            }

            if(metodo.startsWith("TM")||
                    metodo.startsWith("TR")){

                StringBuilder tecnico=
                        new StringBuilder();

                tecnico.append(
                        metodo.startsWith("TR")
                                ?"TR"
                                :"TM"
                );

                if(fuente.getNumero()!=null){
                    tecnico.append(
                            fuente.getNumero()
                    );
                }

                if(fuente.getGeneracion()!=null){
                    tecnico.append(
                            " (G"
                    );

                    tecnico.append(
                            fuente.getGeneracion()
                    );

                    tecnico.append(
                            ")"
                    );
                }

                String texto=
                        tecnico.toString();

                if(!tecnicos.contains(texto)){
                    tecnicos.add(texto);
                }

                continue;
            }

            if(metodo.contains("TUTOR")){
                tutor=true;
                continue;
            }

            if(metodo.contains("EGG")){
                egg=true;
                continue;
            }

            if(metodo.contains("TRANSFER")){
                transfer=true;
                continue;
            }

            String otro=
                    PokemonTranslator.formatear(
                            metodo
                    );

            if(!otro.isEmpty()&&
                    !otros.contains(otro)){

                otros.add(otro);
            }
        }

        Collections.sort(
                niveles
        );

        List<String> partes=
                new ArrayList<>();

        if(!niveles.isEmpty()){
            if(niveles.size()==1){
                partes.add(
                        "Nivel "+
                                niveles.get(0)
                );
            }else{
                StringBuilder texto=
                        new StringBuilder(
                                "Niveles "
                        );

                for(int i=0;i<niveles.size();i++){
                    if(i>0){
                        texto.append(
                                ", "
                        );
                    }

                    texto.append(
                            niveles.get(i)
                    );

                    if(i==4&&
                            niveles.size()>5){

                        texto.append(
                                " +"
                        );

                        texto.append(
                                niveles.size()-5
                        );

                        break;
                    }
                }

                partes.add(
                        texto.toString()
                );
            }
        }

        if(!tecnicos.isEmpty()){
            StringBuilder texto=
                    new StringBuilder();

            int max=
                    Math.min(
                            4,
                            tecnicos.size()
                    );

            for(int i=0;i<max;i++){
                if(i>0){
                    texto.append(
                            ", "
                    );
                }

                texto.append(
                        tecnicos.get(i)
                );
            }

            if(tecnicos.size()>max){
                texto.append(
                        " +"
                );

                texto.append(
                        tecnicos.size()-max
                );
            }

            partes.add(
                    texto.toString()
            );
        }

        if(tutor){
            partes.add(
                    "Tutor"
            );
        }

        if(egg){
            partes.add(
                    "Huevo"
            );
        }

        if(transfer){
            partes.add(
                    "Transferencia"
            );
        }

        partes.addAll(
                otros
        );

        if(partes.isEmpty()){
            return "-";
        }

        return String.join(
                "  ·  ",
                partes
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

        for(String palabra:palabras){
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

    private void dibujarCabecera(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+13,
                FONDO_NAV
        );

        int nombreX=x+5;
        int tipoX=x+(int)(w*0.40F);
        int potX=x+(int)(w*0.56F);
        int preX=x+(int)(w*0.65F);
        int ppX=x+(int)(w*0.75F);
        int fuenteX=x+(int)(w*0.82F);

        drawString(
                matrixStack,
                font,
                "Movimiento",
                nombreX,
                y+3,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Tipo",
                tipoX,
                y+3,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Pot.",
                potX,
                y+3,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Prec.",
                preX,
                y+3,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "PP",
                ppX,
                y+3,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Fuente",
                fuenteX,
                y+3,
                TEXTO_SECUNDARIO
        );
    }

    private void dibujarFila(
            MatrixStack matrixStack,
            PokemonMoveData movimiento,
            int x,
            int y,
            int w
    ){
        if(movimiento==null){
            return;
        }

        int nombreX=x+5;
        int tipoX=x+(int)(w*0.40F);
        int potX=x+(int)(w*0.56F);
        int preX=x+(int)(w*0.65F);
        int ppX=x+(int)(w*0.75F);
        int fuenteX=x+(int)(w*0.82F);

        int anchoNombre=
                Math.max(
                        30,
                        tipoX-nombreX-6
                );

        int anchoTipo=
                Math.max(
                        20,
                        potX-tipoX-5
                );

        int anchoFuente=
                Math.max(
                        25,
                        x+w-fuenteX-6
                );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        getNombreMovimiento(
                                movimiento
                        ),
                        anchoNombre
                ),
                nombreX,
                y,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        PokemonTranslator.tipo(
                                movimiento.getTipo()
                        ),
                        anchoTipo
                ),
                tipoX,
                y,
                TEXTO_ACENTO
        );

        drawString(
                matrixStack,
                font,
                numero(
                        movimiento.getPotencia()
                ),
                potX,
                y,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                numero(
                        movimiento.getPrecision()
                ),
                preX,
                y,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                numero(
                        movimiento.getPP()
                ),
                ppX,
                y,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        formatearFuentes(
                                movimiento.getFuentes()
                        ),
                        anchoFuente
                ),
                fuenteX,
                y,
                TEXTO_SECUNDARIO
        );
    }

    private String getNombreMovimiento(
            PokemonMoveData movimiento
    ){
        if(movimiento==null||
                movimiento.getNombre()==null){

            return "-";
        }

        String key=
                movimiento.getNombre()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String cache=
                nombresTraducidos.get(
                        key
                );

        if(cache!=null){
            return cache;
        }

        String nombre=
                obtenerNombreTraducido(
                        obtenerAtaque(
                                movimiento
                        ),
                        movimiento.getNombre()
                );

        nombresTraducidos.put(
                key,
                nombre
        );

        return nombre;
    }

    private String numero(
            int valor
    ){
        return valor<=0
                ?"-"
                :String.valueOf(valor);
    }

    private List<PokemonMoveData> getMovimientosFiltrados(
            List<PokemonMoveData> movimientos
    ){
        if(movimientos==null||
                movimientos.isEmpty()){

            return Collections.emptyList();
        }

        List<PokemonMoveData> resultado=
                new ArrayList<>();

        for(PokemonMoveData movimiento:
                movimientos){

            if(movimiento==null){
                continue;
            }

            boolean coincideFuente=
                    filtro==Filtro.TODOS||
                            movimientoCoincideFiltro(
                                    movimiento,
                                    filtro
                            );

            if(coincideFuente&&
                    movimientoCoincideBusqueda(
                            movimiento
                    )){

                resultado.add(movimiento);
            }
        }

        ordenar(
                resultado
        );

        return resultado;
    }

    private boolean movimientoCoincideBusqueda(
            PokemonMoveData movimiento
    ){
        if(busquedaMoves==null||
                busquedaMoves.trim().isEmpty()){

            return true;
        }

        if(movimiento==null){
            return false;
        }

        String buscar=
                PokemonTranslator.normalizar(
                        busquedaMoves
                );

        if(buscar==null||
                buscar.trim().isEmpty()){

            return true;
        }

        String ingles=
                PokemonTranslator.normalizar(
                        movimiento.getNombre()
                );

        String espanol=
                PokemonTranslator.normalizar(
                        getNombreMovimiento(
                                movimiento
                        )
                );

        return (ingles!=null&&
                ingles.contains(buscar))||
                (espanol!=null&&
                        espanol.contains(buscar));
    }

    private boolean movimientoCoincideFiltro(
            PokemonMoveData movimiento,
            Filtro filtro
    ){
        if(movimiento==null||
                filtro==null||
                filtro==Filtro.TODOS){

            return true;
        }

        List<MoveLearnSource> fuentes=
                movimiento.getFuentes();

        if(fuentes==null||
                fuentes.isEmpty()){

            return false;
        }

        for(MoveLearnSource fuente:
                fuentes){

            if(fuente==null){
                continue;
            }

            String metodo=
                    normalizarMetodo(
                            fuente.getMetodo()
                    );

            if(filtro==Filtro.NIVEL&&
                    metodo.equals("LEVEL")){

                return true;
            }

            if(filtro==Filtro.TMTR&&
                    (metodo.startsWith("TM")||
                            metodo.startsWith("TR"))){

                return true;
            }

            if(filtro==Filtro.TUTOR&&
                    metodo.contains("TUTOR")){

                return true;
            }

            if(filtro==Filtro.EGG&&
                    metodo.contains("EGG")){

                return true;
            }

            if(filtro==Filtro.TRANSFER&&
                    metodo.contains("TRANSFER")){

                return true;
            }
        }

        return false;
    }

    private void ordenar(
            List<PokemonMoveData> datos
    ){
        if(datos==null||
                datos.size()<=1){

            return;
        }

        if(filtro==Filtro.NIVEL){
            datos.sort(
                    Comparator
                            .comparingInt(
                                    this::nivelMinimo
                            )
                            .thenComparing(
                                    movimiento->
                                            movimiento.getNombre()==null
                                                    ?""
                                                    :movimiento.getNombre(),
                                    String.CASE_INSENSITIVE_ORDER
                            )
            );

            return;
        }

        if(filtro==Filtro.TMTR){
            datos.sort(
                    Comparator
                            .comparingInt(
                                    this::numeroTecnico
                            )
                            .thenComparing(
                                    movimiento->
                                            movimiento.getNombre()==null
                                                    ?""
                                                    :movimiento.getNombre(),
                                    String.CASE_INSENSITIVE_ORDER
                            )
            );

            return;
        }

        datos.sort(
                Comparator.comparing(
                        movimiento->
                                movimiento.getNombre()==null
                                        ?""
                                        :movimiento.getNombre(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );
    }

    private int nivelMinimo(
            PokemonMoveData movimiento
    ){
        int mejor=Integer.MAX_VALUE;

        if(movimiento==null||
                movimiento.getFuentes()==null){

            return mejor;
        }

        for(MoveLearnSource fuente:
                movimiento.getFuentes()){

            if(fuente==null||
                    !"LEVEL".equals(
                            normalizarMetodo(
                                    fuente.getMetodo()
                            )
                    )||
                    fuente.getNivel()==null){

                continue;
            }

            mejor=
                    Math.min(
                            mejor,
                            fuente.getNivel()
                    );
        }

        return mejor;
    }

    private int numeroTecnico(
            PokemonMoveData movimiento
    ){
        int mejor=Integer.MAX_VALUE;

        if(movimiento==null||
                movimiento.getFuentes()==null){

            return mejor;
        }

        for(MoveLearnSource fuente:
                movimiento.getFuentes()){

            if(fuente==null){
                continue;
            }

            String metodo=
                    normalizarMetodo(
                            fuente.getMetodo()
                    );

            if(!(metodo.startsWith("TM")||
                    metodo.startsWith("TR"))){

                continue;
            }

            if(fuente.getNumero()!=null){
                mejor=
                        Math.min(
                                mejor,
                                fuente.getNumero()
                        );
            }
        }

        return mejor;
    }

    private String formatearFuentes(
            List<MoveLearnSource> fuentes
    ){
        if(fuentes==null||
                fuentes.isEmpty()){

            return "-";
        }

        List<String> partes=
                new ArrayList<>();

        for(MoveLearnSource fuente:
                fuentes){

            if(fuente==null){
                continue;
            }

            if(filtro!=Filtro.TODOS&&
                    !fuenteCoincideFiltro(
                            fuente,
                            filtro
                    )){

                continue;
            }

            String texto=
                    formatearFuente(
                            fuente
                    );

            if(!texto.isEmpty()&&
                    !partes.contains(texto)){

                partes.add(texto);
            }

            if(partes.size()>=2){
                break;
            }
        }

        if(partes.isEmpty()&&
                filtro!=Filtro.TODOS){

            for(MoveLearnSource fuente:
                    fuentes){

                if(fuente==null){
                    continue;
                }

                String texto=
                        formatearFuente(
                                fuente
                        );

                if(!texto.isEmpty()){
                    partes.add(texto);
                    break;
                }
            }
        }

        if(partes.isEmpty()){
            return "-";
        }

        return String.join(
                " / ",
                partes
        );
    }

    private boolean fuenteCoincideFiltro(
            MoveLearnSource fuente,
            Filtro filtro
    ){
        if(fuente==null||
                filtro==null||
                filtro==Filtro.TODOS){

            return true;
        }

        String metodo=
                normalizarMetodo(
                        fuente.getMetodo()
                );

        if(filtro==Filtro.NIVEL){
            return metodo.equals("LEVEL");
        }

        if(filtro==Filtro.TMTR){
            return metodo.startsWith("TM")||
                    metodo.startsWith("TR");
        }

        if(filtro==Filtro.TUTOR){
            return metodo.contains("TUTOR");
        }

        if(filtro==Filtro.EGG){
            return metodo.contains("EGG");
        }

        if(filtro==Filtro.TRANSFER){
            return metodo.contains("TRANSFER");
        }

        return true;
    }

    private String formatearFuente(
            MoveLearnSource fuente
    ){
        if(fuente==null){
            return "";
        }

        String metodo=
                normalizarMetodo(
                        fuente.getMetodo()
                );

        if(metodo.equals("LEVEL")){
            return fuente.getNivel()==null
                    ?"Nivel"
                    :"Nv. "+fuente.getNivel();
        }

        if(metodo.startsWith("TM")||
                metodo.startsWith("TR")){

            String base=
                    metodo;

            if(fuente.getNumero()!=null){
                base+=fuente.getNumero();
            }

            return base;
        }

        if(metodo.contains("TUTOR")){
            return "Tutor";
        }

        if(metodo.contains("EGG")){
            return "Egg";
        }

        if(metodo.contains("TRANSFER")){
            return "Transfer";
        }

        if(metodo.isEmpty()){
            return "-";
        }

        return PokemonTranslator.formatear(
                metodo
        );
    }

    private String normalizarMetodo(
            String metodo
    ){
        if(metodo==null){
            return "";
        }

        return metodo.trim()
                .toUpperCase(Locale.ROOT);
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

    private void limitarScroll(
            int total,
            int visibles
    ){
        int maximo=
                Math.max(
                        0,
                        total-visibles
                );

        if(scroll<0){
            scroll=0;
        }

        if(scroll>maximo){
            scroll=maximo;
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
