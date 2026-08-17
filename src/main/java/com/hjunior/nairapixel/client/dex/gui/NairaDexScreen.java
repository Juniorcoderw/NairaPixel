package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.client.dex.controller.NairaDexController;
import com.hjunior.nairapixel.client.dex.model.NairaDexPokemonSummary;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.client.dex.state.NairaDexState;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.breeding.PokemonBreedingData;
import com.hjunior.nairapixel.core.pixelmon.evolution.PokemonEvolutionData;
import com.hjunior.nairapixel.core.pixelmon.forms.PokemonFormData;
import com.hjunior.nairapixel.core.pixelmon.moves.PokemonMoveData;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class NairaDexScreen extends Screen {
    private static final int FONDO_PANEL=0xF20D1117;
    private static final int FONDO_NAV=0xF20A0E14;
    private static final int FONDO_CONTENIDO=0xE6131820;
    private static final int FONDO_CARD=0xE319202A;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int FONDO_ACTIVO=0xE32B3440;

    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;

    private static final int ALTO_FILA=28;

    private final NairaDexController controller=
            NairaDexController.get();

    private List<PokemonSpeciesData> catalogo=
            Collections.emptyList();

    private List<PokemonSpeciesData> catalogoCompleto=
            Collections.emptyList();

    private NairaCollectionSnapshot coleccion=
            NairaCollectionSnapshot.vacio();

    private List<PokemonFormData> formas=
            Collections.emptyList();

    private List<PokemonSpawnRule> spawns=
            Collections.emptyList();

    private List<PokemonMoveData> movimientos=
            Collections.emptyList();

    private List<PokemonEvolutionData> evoluciones=
            Collections.emptyList();

    private NairaDexPokemonSummary resumen;

    private TextFieldWidget campoBusqueda;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private int catalogoX;
    private int catalogoY;
    private int catalogoW;
    private int catalogoH;

    private int filtroY;
    private int scrollCatalogo;

    private int formaX;
    private int formaY;
    private int formaW;
    private int formaH;
    private boolean selectorFormaVisible;

    private final NairaDexSpawnTab spawnTab=
            new NairaDexSpawnTab();

    private final NairaDexMovesTab movesTab=
            new NairaDexMovesTab();

    private final NairaDexEvolutionTab evolutionTab=
            new NairaDexEvolutionTab();

    private final NairaDexBreedingTab breedingTab=
            new NairaDexBreedingTab();

    private final NairaDexCollectionPanel collectionPanel=
            new NairaDexCollectionPanel();

    private final NairaDexAlbumPanel albumPanel=
            new NairaDexAlbumPanel();

    private boolean vistaColeccionAlbum;

    private int vistaListaX;
    private int vistaAlbumX;
    private int vistaColeccionY;
    private int vistaColeccionW=58;
    private int vistaColeccionH=18;

    private final NairaDexObjectivesPanel objectivesPanel=
            new NairaDexObjectivesPanel();

    private final NairaDexObjectivesService objectivesService=
            NairaDexObjectivesService.get();

    private int guardarX;
    private int guardarY;
    private int guardarW=72;
    private int guardarH=18;

    private int objetivoX;
    private int objetivoY;
    private int objetivoW=102;
    private int objetivoH=18;

    private int ajustesX;
    private int ajustesY;
    private int ajustesW=62;
    private int ajustesH=18;

    private int ayudaX;
    private int ayudaY;
    private int ayudaW=54;
    private int ayudaH=18;

    private boolean accionesDexVisibles;

    private final int[] pestanaX=new int[5];
    private final int[] pestanaW=new int[5];
    private int pestanaY;
    private int pestanaH=24;

    public NairaDexScreen(){
        super(
                new StringTextComponent(
                        "NairaDex"
                )
        );
    }

    @Override
    protected void init(){
        super.init();

        calcularLayout();

        int navW=116;
        int headerH=30;
        int margen=12;

        int contenidoX=
                panelX+navW;

        int contenidoW=
                panelW-navW;

        int buscadorX=
                contenidoX+margen;

        int buscadorY=
                panelY+headerH+12;

        int buscadorW=
                contenidoW-(margen*2);

        campoBusqueda=
                new TextFieldWidget(
                        font,
                        buscadorX+6,
                        buscadorY+3,
                        buscadorW-12,
                        16,
                        new StringTextComponent(
                                "Buscar Pokémon"
                        )
                );

        campoBusqueda.setMaxLength(40);

        campoBusqueda.setValue(
                controller.getState()
                        .getBusqueda()
        );

        campoBusqueda.setResponder(
                valor->{
                    controller.setBusqueda(valor);
                    scrollCatalogo=0;
                    actualizarDatos();
                }
        );

        addButton(campoBusqueda);

        actualizarDatos();
    }

    private void calcularLayout(){
        panelW=
                Math.min(
                        width-20,
                        920
                );

        panelH=
                Math.min(
                        height-20,
                        540
                );

        panelX=
                (width-panelW)/2;

        panelY=
                (height-panelH)/2;
    }

    private void actualizarDatos(){
        catalogo=
                controller.getCatalogoActual();

        catalogoCompleto=
                controller.getCatalogoCompleto();

        if(catalogoCompleto==null){
            catalogoCompleto=
                    Collections.emptyList();
        }

        coleccion=
                controller.getColeccion();

        if(coleccion==null){
            coleccion=
                    NairaCollectionSnapshot.vacio();
        }

        formas=
                filtrarFormasPrincipales(
                        controller.getFormasActuales()
                );

        spawns=
                controller.getSpawnsActuales();

        if(spawns==null){
            spawns=Collections.emptyList();
        }

        movimientos=
                controller.getMovimientosActuales();

        if(movimientos==null){
            movimientos=Collections.emptyList();
        }

        evoluciones=
                controller.getEvolucionesActuales();

        if(evoluciones==null){
            evoluciones=Collections.emptyList();
        }

        Optional<NairaDexPokemonSummary> actual=
                controller.getResumenActual();

        resumen=
                actual.orElse(null);

        selectorFormaVisible=
                resumen!=null&&
                        formas!=null&&
                        formas.size()>1;

        limitarScroll();
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY,
            float partialTicks
    ){
        renderBackground(matrixStack);

        calcularLayout();

        int headerH=30;
        int navW=116;

        dibujarPanel(matrixStack);

        dibujarHeader(
                matrixStack,
                headerH
        );

        dibujarNavegacion(
                matrixStack,
                navW,
                headerH
        );

        dibujarContenido(
                matrixStack,
                navW,
                headerH,
                mouseX,
                mouseY
        );

        if(campoBusqueda!=null){
            campoBusqueda.visible=
                    controller.getState()
                            .getSeccion()!=
                            NairaDexState.Seccion.OBJETIVOS;
        }

        super.render(
                matrixStack,
                mouseX,
                mouseY,
                partialTicks
        );

        if(campoBusqueda!=null&&
                campoBusqueda.visible&&
                campoBusqueda.getValue()
                        .isEmpty()){

            int placeholderX=
                    panelX+
                            116+
                            12+
                            10;

            int placeholderY=
                    panelY+
                            30+
                            12+
                            7;

            drawString(
                    matrixStack,
                    font,
                    "Buscar Pokémon...",
                    placeholderX,
                    placeholderY,
                    0xFF66717C
            );
        }
    }

    private void dibujarPanel(
            MatrixStack matrixStack
    ){
        fill(
                matrixStack,
                panelX,
                panelY,
                panelX+panelW,
                panelY+panelH,
                FONDO_PANEL
        );

        borde(
                matrixStack,
                panelX,
                panelY,
                panelW,
                panelH,
                ACENTO
        );
    }

    private void dibujarHeader(
            MatrixStack matrixStack,
            int headerH
    ){
        fill(
                matrixStack,
                panelX+1,
                panelY+1,
                panelX+panelW-1,
                panelY+headerH,
                FONDO_NAV
        );

        drawString(
                matrixStack,
                font,
                "NAIRADEX",
                panelX+12,
                panelY+11,
                TEXTO_ACENTO
        );

        String firma=
                "by HJunior";

        int firmaX=
                panelX+
                        panelW-
                        12-
                        font.width(firma);

        drawString(
                matrixStack,
                font,
                firma,
                firmaX,
                panelY+11,
                TEXTO_SECUNDARIO
        );

        ajustesX=
                firmaX-
                        ajustesW-
                        10;

        ajustesY=
                panelY+6;

        ayudaX=
                ajustesX-
                        ayudaW-
                        6;

        ayudaY=
                panelY+6;

        fill(
                matrixStack,
                ayudaX,
                ayudaY,
                ayudaX+ayudaW,
                ayudaY+ayudaH,
                FONDO_PANEL
        );

        borde(
                matrixStack,
                ayudaX,
                ayudaY,
                ayudaW,
                ayudaH,
                BORDE
        );

        String ayuda=
                "AYUDA";

        drawString(
                matrixStack,
                font,
                ayuda,
                ayudaX+
                        (ayudaW-font.width(ayuda))/2,
                ayudaY+5,
                TEXTO_SECUNDARIO
        );

        fill(
                matrixStack,
                ajustesX,
                ajustesY,
                ajustesX+ajustesW,
                ajustesY+ajustesH,
                FONDO_PANEL
        );

        borde(
                matrixStack,
                ajustesX,
                ajustesY,
                ajustesW,
                ajustesH,
                BORDE
        );

        String ajustes=
                "AJUSTES";

        drawString(
                matrixStack,
                font,
                ajustes,
                ajustesX+
                        (ajustesW-font.width(ajustes))/2,
                ajustesY+5,
                TEXTO_SECUNDARIO
        );

        fill(
                matrixStack,
                panelX,
                panelY+headerH-1,
                panelX+panelW,
                panelY+headerH,
                BORDE
        );
    }

    private void dibujarNavegacion(
            MatrixStack matrixStack,
            int navW,
            int headerH
    ){
        int x=panelX;
        int y=panelY+headerH;
        int h=panelH-headerH;

        fill(
                matrixStack,
                x+1,
                y,
                x+navW,
                y+h-1,
                FONDO_NAV
        );

        fill(
                matrixStack,
                x+navW-1,
                y,
                x+navW,
                y+h,
                BORDE
        );

        int yy=y+18;

        etiquetaNav(
                matrixStack,
                x,
                yy,
                navW,
                "DEX",
                controller.getState()
                        .getSeccion()==
                        NairaDexState.Seccion.DEX
        );

        yy+=28;

        etiquetaNav(
                matrixStack,
                x,
                yy,
                navW,
                "COLECCIÓN",
                controller.getState()
                        .getSeccion()==
                        NairaDexState.Seccion.COLECCION
        );

        yy+=28;

        etiquetaNav(
                matrixStack,
                x,
                yy,
                navW,
                "OBJETIVOS",
                controller.getState()
                        .getSeccion()==
                        NairaDexState.Seccion.OBJETIVOS
        );

        int abajo=
                y+h-38;

        drawString(
                matrixStack,
                font,
                "Dex",
                x+12,
                abajo,
                TEXTO_SECUNDARIO
        );

        String progreso=
                controller.getEspeciesColeccion()
                        +"/"+
                        controller.getCantidadPokemonDex();

        drawString(
                matrixStack,
                font,
                progreso,
                x+12,
                abajo+12,
                controller.coleccionSincronizada()
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );
    }

    private void etiquetaNav(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String texto,
            boolean activo
    ){
        if(activo){
            fill(
                    matrixStack,
                    x+6,
                    y-6,
                    x+w-7,
                    y+15,
                    FONDO_ACTIVO
            );

            fill(
                    matrixStack,
                    x+6,
                    y-6,
                    x+8,
                    y+15,
                    ACENTO
            );
        }

        drawString(
                matrixStack,
                font,
                texto,
                x+15,
                y,
                activo
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void dibujarContenido(
            MatrixStack matrixStack,
            int navW,
            int headerH,
            int mouseX,
            int mouseY
    ){
        int x=
                panelX+navW;

        int y=
                panelY+headerH;

        int w=
                panelW-navW;

        int h=
                panelH-headerH;

        fill(
                matrixStack,
                x,
                y,
                x+w-1,
                y+h-1,
                FONDO_CONTENIDO
        );

        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.COLECCION){

            dibujarContenidoColeccion(
                    matrixStack,
                    x,
                    y,
                    w,
                    h,
                    mouseX,
                    mouseY
            );

            return;
        }

        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.OBJETIVOS){

            dibujarContenidoObjetivos(
                    matrixStack,
                    x,
                    y,
                    w,
                    h,
                    mouseX,
                    mouseY
            );

            return;
        }

        int margen=12;

        int buscadorX=
                x+margen;

        int buscadorY=
                y+12;

        int buscadorW=
                w-(margen*2);

        fill(
                matrixStack,
                buscadorX,
                buscadorY,
                buscadorX+buscadorW,
                buscadorY+22,
                FONDO_CARD
        );

        borde(
                matrixStack,
                buscadorX,
                buscadorY,
                buscadorW,
                22,
                BORDE
        );

        filtroY=
                buscadorY+31;

        NairaDexState.FiltroColeccion filtro=
                controller.getState()
                        .getFiltroColeccion();

        etiquetaFiltro(
                matrixStack,
                buscadorX,
                filtroY,
                42,
                "Todos",
                filtro==
                        NairaDexState.FiltroColeccion.TODOS
        );

        etiquetaFiltro(
                matrixStack,
                buscadorX+48,
                filtroY,
                64,
                "Obtenidos",
                filtro==
                        NairaDexState.FiltroColeccion.OBTENIDOS
        );

        etiquetaFiltro(
                matrixStack,
                buscadorX+118,
                filtroY,
                78,
                "No obtenidos",
                filtro==
                        NairaDexState.FiltroColeccion.NO_OBTENIDOS
        );

        int cuerpoY=
                filtroY+27;

        int cuerpoH=
                h-(cuerpoY-y)-12;

        int izquierdaW=
                Math.max(
                        165,
                        (int)(w*0.36F)
                );

        catalogoX=buscadorX;
        catalogoY=cuerpoY;
        catalogoW=izquierdaW;
        catalogoH=cuerpoH;

        int detalleX=
                catalogoX+catalogoW+8;

        int detalleW=
                buscadorW-catalogoW-8;

        dibujarCatalogo(
                matrixStack,
                mouseX,
                mouseY
        );

        dibujarDetalle(
                matrixStack,
                detalleX,
                cuerpoY,
                detalleW,
                cuerpoH,
                mouseX,
                mouseY
        );
    }

    private void dibujarContenidoColeccion(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        int margen=12;

        int buscadorX=x+margen;
        int buscadorY=y+12;
        int buscadorW=w-(margen*2);

        fill(
                matrixStack,
                buscadorX,
                buscadorY,
                buscadorX+buscadorW,
                buscadorY+22,
                FONDO_CARD
        );

        borde(
                matrixStack,
                buscadorX,
                buscadorY,
                buscadorW,
                22,
                BORDE
        );

        vistaColeccionY=
                buscadorY+31;

        vistaListaX=
                buscadorX;

        vistaAlbumX=
                vistaListaX+
                        vistaColeccionW+5;

        dibujarBotonVistaColeccion(
                matrixStack,
                vistaListaX,
                vistaColeccionY,
                vistaColeccionW,
                "LISTA",
                !vistaColeccionAlbum,
                dentro(
                        mouseX,
                        mouseY,
                        vistaListaX,
                        vistaColeccionY,
                        vistaColeccionW,
                        vistaColeccionH
                )
        );

        dibujarBotonVistaColeccion(
                matrixStack,
                vistaAlbumX,
                vistaColeccionY,
                vistaColeccionW,
                "ÁLBUM",
                vistaColeccionAlbum,
                dentro(
                        mouseX,
                        mouseY,
                        vistaAlbumX,
                        vistaColeccionY,
                        vistaColeccionW,
                        vistaColeccionH
                )
        );

        String modo=
                vistaColeccionAlbum
                        ?"Vista visual de progreso"
                        :"Tus Pokémon sincronizados";

        drawString(
                matrixStack,
                font,
                modo,
                vistaAlbumX+
                        vistaColeccionW+10,
                vistaColeccionY+5,
                TEXTO_SECUNDARIO
        );

        int contenidoY=
                vistaColeccionY+27;

        int contenidoH=
                Math.max(
                        90,
                        h-(contenidoY-y)-12
                );

        if(vistaColeccionAlbum){
            albumPanel.render(
                    matrixStack,
                    font,
                    coleccion,
                    catalogoCompleto,
                    controller.getState()
                            .getBusqueda(),
                    controller.getCantidadPokemonDex(),
                    controller.coleccionSincronizada(),
                    buscadorX,
                    contenidoY,
                    buscadorW,
                    contenidoH,
                    mouseX,
                    mouseY
            );
        }else{
            collectionPanel.render(
                    matrixStack,
                    font,
                    coleccion,
                    catalogoCompleto,
                    controller.getState()
                            .getBusqueda(),
                    controller.getCantidadPokemonDex(),
                    controller.coleccionSincronizada(),
                    buscadorX,
                    contenidoY,
                    buscadorW,
                    contenidoH,
                    mouseX,
                    mouseY
            );
        }
    }

    private void dibujarBotonVistaColeccion(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String texto,
            boolean activo,
            boolean hover
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+vistaColeccionH,
                activo
                        ?FONDO_ACTIVO
                        :hover
                                ?FONDO_HOVER
                                :FONDO_NAV
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                vistaColeccionH,
                activo
                        ?ACENTO
                        :BORDE
        );

        drawString(
                matrixStack,
                font,
                texto,
                x+(w-font.width(texto))/2,
                y+5,
                activo
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void dibujarContenidoObjetivos(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        int margen=12;

        objectivesPanel.render(
                matrixStack,
                font,
                x+margen,
                y+12,
                w-(margen*2),
                h-24,
                mouseX,
                mouseY
        );
    }

    private void etiquetaFiltro(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String texto,
            boolean activo
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+18,
                activo
                        ?FONDO_ACTIVO
                        :FONDO_NAV
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                18,
                activo
                        ?ACENTO
                        :BORDE
        );

        int tx=
                x+(w-font.width(texto))/2;

        drawString(
                matrixStack,
                font,
                texto,
                tx,
                y+5,
                activo
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void dibujarCatalogo(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        fill(
                matrixStack,
                catalogoX,
                catalogoY,
                catalogoX+catalogoW,
                catalogoY+catalogoH,
                FONDO_CARD
        );

        borde(
                matrixStack,
                catalogoX,
                catalogoY,
                catalogoW,
                catalogoH,
                BORDE
        );

        drawString(
                matrixStack,
                font,
                "CATÁLOGO",
                catalogoX+8,
                catalogoY+8,
                TEXTO_ACENTO
        );

        int inicioY=
                catalogoY+27;

        int visibles=
                getFilasVisibles();

        for(int fila=0;fila<visibles;fila++){
            int indice=
                    scrollCatalogo+fila;

            if(indice>=catalogo.size()){
                break;
            }

            PokemonSpeciesData pokemon=
                    catalogo.get(indice);

            int yy=
                    inicioY+(fila*ALTO_FILA);

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            catalogoX+4,
                            yy-4,
                            catalogoW-8,
                            ALTO_FILA
                    );

            boolean seleccionado=
                    esSeleccionado(pokemon);

            if(seleccionado||hover){
                fill(
                        matrixStack,
                        catalogoX+4,
                        yy-4,
                        catalogoX+catalogoW-4,
                        yy+ALTO_FILA-4,
                        seleccionado
                                ?FONDO_ACTIVO
                                :FONDO_HOVER
                );
            }

            if(seleccionado){
                fill(
                        matrixStack,
                        catalogoX+4,
                        yy-4,
                        catalogoX+6,
                        yy+ALTO_FILA-4,
                        ACENTO
                );
            }

            int spriteTamano=22;
            int spriteX=catalogoX+9;
            int spriteY=yy-1;

            NairaPokemonSpriteRenderer.dibujar(
                    matrixStack,
                    pokemon.getNombre(),
                    "",
                    spriteX,
                    spriteY,
                    spriteTamano
            );

            String numero=
                    String.format(
                            "#%04d",
                            pokemon.getNumeroDex()
                    );

            int textoY=yy+6;

            drawString(
                    matrixStack,
                    font,
                    numero,
                    catalogoX+36,
                    textoY,
                    TEXTO_SECUNDARIO
            );

            drawString(
                    matrixStack,
                    font,
                    pokemon.getNombre(),
                    catalogoX+82,
                    textoY,
                    seleccionado
                            ?TEXTO_ACENTO
                            :TEXTO
            );
        }

        if(catalogo.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    "Sin resultados",
                    catalogoX+8,
                    catalogoY+29,
                    TEXTO_SECUNDARIO
            );
        }

        dibujarScrollbar(
                matrixStack
        );
    }

    private void dibujarScrollbar(
            MatrixStack matrixStack
    ){
        int visibles=
                getFilasVisibles();

        if(catalogo.size()<=visibles){
            return;
        }

        int x=
                catalogoX+catalogoW-4;

        int y=
                catalogoY+26;

        int h=
                catalogoH-30;

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
                        18,
                        (int)(
                                h*
                                        (visibles/(float)catalogo.size())
                        )
                );

        int maxScroll=
                Math.max(
                        1,
                        catalogo.size()-visibles
                );

        int recorrido=
                h-thumbH;

        int thumbY=
                y+
                        (int)(
                                recorrido*
                                        (scrollCatalogo/(float)maxScroll)
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

    private void dibujarDetalle(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
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

        selectorFormaVisible=false;

        if(resumen==null){
            drawString(
                    matrixStack,
                    font,
                    "POKÉMON",
                    x+10,
                    y+9,
                    TEXTO_ACENTO
            );

            drawString(
                    matrixStack,
                    font,
                    "Selecciona un Pokémon",
                    x+10,
                    y+31,
                    TEXTO_SECUNDARIO
            );

            dibujarPestanas(
                    matrixStack,
                    x,
                    y+h-28,
                    w
            );

            return;
        }

        String numero=
                String.format(
                        "#%04d",
                        resumen.getNumeroDex()
                );

        drawString(
                matrixStack,
                font,
                numero,
                x+10,
                y+10,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                resumen.getPokemon()
                        .toUpperCase(Locale.ROOT),
                x+54,
                y+10,
                TEXTO
        );

        dibujarSpriteSeleccionado(
                matrixStack,
                x,
                y,
                w
        );

        int yy=y+29;

        drawString(
                matrixStack,
                font,
                PokemonTranslator.tipos(
                        resumen.getTipos()
                ),
                x+10,
                yy,
                TEXTO_ACENTO
        );

        yy+=18;

        if(formas.size()>1){
            dibujarSelectorForma(
                    matrixStack,
                    x+10,
                    yy,
                    Math.min(170,w-20),
                    mouseX,
                    mouseY
            );

            yy+=29;
        }else{
            String formaTexto=
                    PokemonTranslator.forma(
                            resumen.getForma()
                    );

            if(!resumen.isFormaBase()||
                    resumen.tieneForma()){

                drawString(
                        matrixStack,
                        font,
                        "Forma: "+formaTexto,
                        x+10,
                        yy,
                        TEXTO_SECUNDARIO
                );

                yy+=15;
            }
        }

        drawString(
                matrixStack,
                font,
                "Gen "+resumen.getGeneracion(),
                x+10,
                yy,
                TEXTO_SECUNDARIO
        );

        if(!resumen.getCategoria().isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    resumen.getCategoria(),
                    x+58,
                    yy,
                    TEXTO_SECUNDARIO
            );
        }

        yy+=20;

        drawString(
                matrixStack,
                font,
                "Obtenidos: "+resumen.getCantidad(),
                x+10,
                yy,
                resumen.isObtenido()
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "Shiny: "+resumen.getCantidadShiny(),
                x+102,
                yy,
                resumen.getCantidadShiny()>0
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );

        yy+=18;

        dibujarAccionesDex(
                matrixStack,
                x+10,
                yy,
                mouseX,
                mouseY
        );

        yy+=24;

        int contenidoPestanaY=
                Math.max(
                        yy,
                        y+132
                );

        int contenidoPestanaH=
                Math.max(
                        40,
                        (y+h-31)-contenidoPestanaY
                );

        NairaDexState.Pestana pestana=
                controller.getState()
                        .getPestana();

        if(pestana==NairaDexState.Pestana.SPAWN){
            spawnTab.render(
                    matrixStack,
                    font,
                    spawns,
                    x+10,
                    contenidoPestanaY,
                    w-20,
                    contenidoPestanaH
            );
        }else if(pestana==NairaDexState.Pestana.MOVIMIENTOS){
            movesTab.render(
                    matrixStack,
                    font,
                    movimientos,
                    x+10,
                    contenidoPestanaY,
                    w-20,
                    contenidoPestanaH,
                    mouseX,
                    mouseY
            );
        }else if(pestana==NairaDexState.Pestana.EVOLUCION){
            evolutionTab.render(
                    matrixStack,
                    font,
                    resumen==null
                            ?""
                            :resumen.getPokemon(),
                    controller.getState()
                            .getFormaSeleccionada(),
                    evoluciones,
                    x+10,
                    contenidoPestanaY,
                    w-20,
                    contenidoPestanaH,
                    mouseX,
                    mouseY
            );
        }else if(pestana==NairaDexState.Pestana.CRIANZA){
            breedingTab.render(
                    matrixStack,
                    font,
                    resumen==null
                            ?""
                            :resumen.getPokemon(),
                    controller.getState()
                            .getFormaSeleccionada(),
                    obtenerCrianzaActual(),
                    movimientos,
                    x+10,
                    contenidoPestanaY,
                    w-20,
                    contenidoPestanaH
            );
        }else if(pestana==NairaDexState.Pestana.GENERAL){
            int statsW=
                    Math.max(
                            170,
                            (int)(w*0.48F)
                    );

            dibujarStatsGeneral(
                    matrixStack,
                    x+10,
                    contenidoPestanaY,
                    statsW-10
            );

            dibujarInfoGeneral(
                    matrixStack,
                    x+statsW+10,
                    contenidoPestanaY,
                    w-statsW-20
            );
        }else{
            dibujarPestanaPendiente(
                    matrixStack,
                    x+10,
                    contenidoPestanaY,
                    pestana
            );
        }

        dibujarPestanas(
                matrixStack,
                x,
                y+h-28,
                w
        );
    }

    private void dibujarAccionesDex(
            MatrixStack matrixStack,
            int x,
            int y,
            int mouseX,
            int mouseY
    ){
        accionesDexVisibles=resumen!=null;
        if(!accionesDexVisibles)return;

        String pokemon=resumen.getPokemon();
        String forma=controller.getState().getFormaSeleccionada();

        boolean guardado=objectivesService.estaGuardado(pokemon,forma);
        boolean objetivoActivo=objectivesService.esObjetivoActivo(pokemon,forma);
        boolean objetivoPrincipal=objectivesService.esObjetivoPrincipal(pokemon,forma);

        guardarX=x;
        guardarY=y;
        objetivoX=guardarX+guardarW+7;
        objetivoY=y;

        dibujarBotonAccionDex(
                matrixStack,
                guardarX,
                guardarY,
                guardarW,
                guardarH,
                guardado?"★ GUARDADO":"☆ GUARDAR",
                guardado,
                dentro(mouseX,mouseY,guardarX,guardarY,guardarW,guardarH)
        );

        String textoObjetivo;

        if(objetivoPrincipal){
            textoObjetivo="● PRINCIPAL";
        }else if(objetivoActivo){
            textoObjetivo="● OBJETIVO";
        }else if(!objectivesService.puedeAgregarObjetivo()){
            textoObjetivo="LÍMITE 3";
        }else{
            textoObjetivo="◎ AÑADIR OBJETIVO";
        }

        dibujarBotonAccionDex(
                matrixStack,
                objetivoX,
                objetivoY,
                objetivoW,
                objetivoH,
                textoObjetivo,
                objetivoActivo,
                dentro(mouseX,mouseY,objetivoX,objetivoY,objetivoW,objetivoH)
        );
    }

    private void dibujarBotonAccionDex(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            String texto,
            boolean activo,
            boolean hover
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                activo?FONDO_ACTIVO:(hover?FONDO_HOVER:FONDO_NAV)
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                activo||hover?ACENTO:BORDE
        );

        drawString(
                matrixStack,
                font,
                texto,
                x+(w-font.width(texto))/2,
                y+5,
                activo||hover?TEXTO_ACENTO:TEXTO_SECUNDARIO
        );
    }

    private void dibujarSpriteSeleccionado(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
    ){
        int caja=96;
        int margen=12;

        if(w<300){
            caja=76;
        }

        int spriteX=
                x+w-caja-margen;

        int spriteY=
                y+18;

        fill(
                matrixStack,
                spriteX,
                spriteY,
                spriteX+caja,
                spriteY+caja,
                FONDO_NAV
        );

        borde(
                matrixStack,
                spriteX,
                spriteY,
                caja,
                caja,
                BORDE
        );

        float tamano=
                caja-16.0F;

        float renderX=
                spriteX+(caja-tamano)/2.0F;

        float renderY=
                spriteY+(caja-tamano)/2.0F;

        boolean dibujado=
                NairaPokemonSpriteRenderer.dibujar(
                        matrixStack,
                        resumen.getPokemon(),
                        controller.getState()
                                .getFormaSeleccionada(),
                        renderX,
                        renderY,
                        tamano
                );

        if(!dibujado){
            String texto="?";

            drawString(
                    matrixStack,
                    font,
                    texto,
                    spriteX+(caja-font.width(texto))/2,
                    spriteY+(caja-8)/2,
                    TEXTO_SECUNDARIO
            );
        }
    }

    private static List<PokemonFormData> filtrarFormasPrincipales(
            List<PokemonFormData> origen
    ){
        if(origen==null||origen.isEmpty()){
            return Collections.emptyList();
        }

        List<PokemonFormData> resultado=
                new ArrayList<>();

        for(PokemonFormData forma:origen){
            if(forma==null)continue;

            if(!forma.isTemporal()){
                resultado.add(forma);
            }
        }

        if(resultado.isEmpty()){
            return origen;
        }

        return Collections.unmodifiableList(
                resultado
        );
    }

    private void dibujarSelectorForma(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int mouseX,
            int mouseY
    ){
        formaX=x;
        formaY=y;
        formaW=w;
        formaH=21;

        selectorFormaVisible=true;

        boolean hover=
                dentro(
                        mouseX,
                        mouseY,
                        formaX,
                        formaY,
                        formaW,
                        formaH
                );

        fill(
                matrixStack,
                formaX,
                formaY,
                formaX+formaW,
                formaY+formaH,
                hover
                        ?FONDO_HOVER
                        :FONDO_NAV
        );

        borde(
                matrixStack,
                formaX,
                formaY,
                formaW,
                formaH,
                hover
                        ?ACENTO
                        :BORDE
        );

        drawString(
                matrixStack,
                font,
                "Forma",
                formaX+7,
                formaY+7,
                TEXTO_SECUNDARIO
        );

        String nombre=
                PokemonTranslator.forma(
                        controller.getState()
                                .getFormaSeleccionada()
                );

        int nombreX=
                formaX+formaW-20-font.width(nombre);

        drawString(
                matrixStack,
                font,
                nombre,
                nombreX,
                formaY+7,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                ">",
                formaX+formaW-11,
                formaY+7,
                hover
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void dibujarStatsGeneral(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
    ){
        drawString(
                matrixStack,
                font,
                "STATS",
                x,
                y,
                TEXTO_ACENTO
        );

        int yy=y+15;

        statBarra(
                matrixStack,
                x,
                yy,
                w,
                "HP",
                resumen.getPS()
        );

        statBarra(
                matrixStack,
                x,
                yy+14,
                w,
                "ATK",
                resumen.getAtaque()
        );

        statBarra(
                matrixStack,
                x,
                yy+28,
                w,
                "DEF",
                resumen.getDefensa()
        );

        statBarra(
                matrixStack,
                x,
                yy+42,
                w,
                "SpA",
                resumen.getAtaqueEspecial()
        );

        statBarra(
                matrixStack,
                x,
                yy+56,
                w,
                "SpD",
                resumen.getDefensaEspecial()
        );

        statBarra(
                matrixStack,
                x,
                yy+70,
                w,
                "SPE",
                resumen.getVelocidad()
        );

        drawString(
                matrixStack,
                font,
                "BST "+resumen.getBST(),
                x,
                yy+89,
                TEXTO_ACENTO
        );
    }

    private void statBarra(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String nombre,
            int valor
    ){
        int valorX=x+27;
        int barraX=x+53;
        int barraW=Math.max(28,w-53);
        int barraH=5;

        drawString(
                matrixStack,
                font,
                nombre,
                x,
                y,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                String.valueOf(valor),
                valorX,
                y,
                TEXTO
        );

        int barraY=y+2;

        fill(
                matrixStack,
                barraX,
                barraY,
                barraX+barraW,
                barraY+barraH,
                FONDO_NAV
        );

        int relleno=
                Math.min(
                        barraW,
                        Math.max(
                                0,
                                Math.round(
                                        barraW*
                                                (Math.min(valor,180)/180.0F)
                                )
                        )
                );

        if(relleno>0){
            fill(
                    matrixStack,
                    barraX,
                    barraY,
                    barraX+relleno,
                    barraY+barraH,
                    ACENTO
            );
        }
    }

    private void dibujarInfoGeneral(
            MatrixStack matrixStack,
            int x,
            int y,
            int w
    ){
        if(w<90){
            return;
        }

        drawString(
                matrixStack,
                font,
                "DATOS",
                x,
                y,
                TEXTO_ACENTO
        );

        int yy=y+15;

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "Hab.",
                unirLista(
                        resumen.getHabilidades(),
                        false
                ),
                TEXTO
        );

        yy+=14;

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "HA",
                unirLista(
                        resumen.getHabilidadesOcultas(),
                        false
                ),
                resumen.tieneHA()
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );

        yy+=14;

        PokemonBreedingData crianza=
                obtenerCrianzaActual();

        if(crianza==null){
            lineaInfo(
                    matrixStack,
                    x,
                    yy,
                    w,
                    "Captura",
                    "-",
                    TEXTO_SECUNDARIO
            );

            return;
        }

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "Captura",
                String.valueOf(
                        crianza.getRatioCaptura()
                ),
                TEXTO
        );

        yy+=14;

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "Egg",
                unirLista(
                        crianza.getGruposHuevo(),
                        true
                ),
                TEXTO
        );

        yy+=14;

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "Ciclos",
                String.valueOf(
                        crianza.getCiclosHuevo()
                ),
                TEXTO
        );

        yy+=14;

        lineaInfo(
                matrixStack,
                x,
                yy,
                w,
                "EV Yield",
                crearEvYield(crianza),
                TEXTO
        );
    }

    private void lineaInfo(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String etiqueta,
            String valor,
            int colorValor
    ){
        drawString(
                matrixStack,
                font,
                etiqueta,
                x,
                y,
                TEXTO_SECUNDARIO
        );

        int valorX=
                x+Math.min(
                        50,
                        Math.max(
                                34,
                                font.width(etiqueta)+7
                        )
                );

        String visible=
                limitarTexto(
                        valor,
                        Math.max(
                                10,
                                w-(valorX-x)
                        )
                );

        drawString(
                matrixStack,
                font,
                visible,
                valorX,
                y,
                colorValor
        );
    }

    private PokemonBreedingData obtenerCrianzaActual(){
        try{
            Object datos=
                    controller.getCrianzaActual();

            if(datos instanceof PokemonBreedingData){
                return (PokemonBreedingData)datos;
            }

            if(datos instanceof Optional){
                Object valor=
                        ((Optional<?>)datos)
                                .orElse(null);

                if(valor instanceof PokemonBreedingData){
                    return (PokemonBreedingData)valor;
                }
            }
        }catch(Exception ignored){
        }

        return null;
    }

    private String crearEvYield(
            PokemonBreedingData datos
    ){
        if(datos==null||
                datos.getEvTotal()<=0){

            return "0";
        }

        List<String> partes=
                new ArrayList<>();

        agregarEv(
                partes,
                "HP",
                datos.getEvPS()
        );

        agregarEv(
                partes,
                "ATK",
                datos.getEvAtaque()
        );

        agregarEv(
                partes,
                "DEF",
                datos.getEvDefensa()
        );

        agregarEv(
                partes,
                "SpA",
                datos.getEvAtaqueEspecial()
        );

        agregarEv(
                partes,
                "SpD",
                datos.getEvDefensaEspecial()
        );

        agregarEv(
                partes,
                "SPE",
                datos.getEvVelocidad()
        );

        if(partes.isEmpty()){
            return String.valueOf(
                    datos.getEvTotal()
            );
        }

        return String.join(
                " / ",
                partes
        );
    }

    private static void agregarEv(
            List<String> partes,
            String nombre,
            int valor
    ){
        if(valor<=0){
            return;
        }

        partes.add(
                nombre+" +"+valor
        );
    }

    private String unirLista(
            List<String> valores,
            boolean gruposHuevo
    ){
        if(valores==null||
                valores.isEmpty()){

            return "-";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String valor:valores){
            if(valor==null||
                    valor.trim().isEmpty()){

                continue;
            }

            String texto=
                    gruposHuevo
                            ?PokemonTranslator.grupoHuevo(valor)
                            :PokemonTranslator.formatear(valor);

            visibles.add(texto);
        }

        if(visibles.isEmpty()){
            return "-";
        }

        return String.join(
                " / ",
                visibles
        );
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

    private void dibujarPestanaPendiente(
            MatrixStack matrixStack,
            int x,
            int y,
            NairaDexState.Pestana pestana
    ){
        String nombre;

        if(pestana==NairaDexState.Pestana.EVOLUCION){
            nombre="EVOLUCIÓN";
        }else if(pestana==NairaDexState.Pestana.CRIANZA){
            nombre="CRIANZA";
        }else{
            nombre="NAIRADEX";
        }

        drawString(
                matrixStack,
                font,
                nombre,
                x,
                y,
                TEXTO_ACENTO
        );

        drawString(
                matrixStack,
                font,
                "Contenido en la siguiente etapa.",
                x,
                y+18,
                TEXTO_SECUNDARIO
        );
    }

    private void dibujarPestanas(
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
                y+1,
                BORDE
        );

        String[] nombres={
                "General",
                "Spawn",
                "Moves",
                "Evolución",
                "Crianza"
        };

        NairaDexState.Pestana[] valores={
                NairaDexState.Pestana.GENERAL,
                NairaDexState.Pestana.SPAWN,
                NairaDexState.Pestana.MOVIMIENTOS,
                NairaDexState.Pestana.EVOLUCION,
                NairaDexState.Pestana.CRIANZA
        };

        pestanaY=y+2;

        int xx=x+9;

        for(int i=0;i<nombres.length;i++){
            String texto=
                    nombres[i];

            int ancho=
                    font.width(texto)+12;

            pestanaX[i]=xx-4;
            pestanaW[i]=ancho;

            boolean activa=
                    controller.getState()
                            .getPestana()==valores[i];

            if(activa){
                fill(
                        matrixStack,
                        xx-4,
                        y+3,
                        xx+font.width(texto)+4,
                        y+25,
                        FONDO_ACTIVO
                );
            }

            drawString(
                    matrixStack,
                    font,
                    texto,
                    xx,
                    y+10,
                    activa
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );

            xx+=
                    font.width(texto)+13;
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ){
        if(button==0){
            if(dentro(
                    mouseX,
                    mouseY,
                    ayudaX,
                    ayudaY,
                    ayudaW,
                    ayudaH
            )){
                minecraft.setScreen(
                        new NairaHelpScreen(
                                this
                        )
                );

                return true;
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    ajustesX,
                    ajustesY,
                    ajustesW,
                    ajustesH
            )){
                minecraft.setScreen(
                        new NairaControlScreen(
                                this
                        )
                );

                return true;
            }

            if(manejarClickNavegacion(
                    mouseX,
                    mouseY
            )){
                return true;
            }

            if(controller.getState()
                    .getSeccion()==
                    NairaDexState.Seccion.COLECCION){

                if(dentro(
                        mouseX,
                        mouseY,
                        vistaListaX,
                        vistaColeccionY,
                        vistaColeccionW,
                        vistaColeccionH
                )){
                    vistaColeccionAlbum=false;
                    collectionPanel.resetScroll();
                    return true;
                }

                if(dentro(
                        mouseX,
                        mouseY,
                        vistaAlbumX,
                        vistaColeccionY,
                        vistaColeccionW,
                        vistaColeccionH
                )){
                    vistaColeccionAlbum=true;
                    albumPanel.resetScroll();
                    return true;
                }

                if(vistaColeccionAlbum){
                    if(albumPanel.mouseClicked(
                            mouseX,
                            mouseY
                    )){
                        PokemonSpeciesData abrir=
                                albumPanel.consumirPokemonParaAbrir();

                        if(abrir!=null){
                            controller.seleccionarPokemon(
                                    abrir.getNombre()
                            );

                            controller.seleccionarPestana(
                                    NairaDexState.Pestana.GENERAL
                            );

                            controller.seleccionarSeccion(
                                    NairaDexState.Seccion.DEX
                            );

                            actualizarDatos();
                        }

                        return true;
                    }
                }else if(collectionPanel.mouseClicked(
                        mouseX,
                        mouseY
                )){
                    return true;
                }

                return super.mouseClicked(
                        mouseX,
                        mouseY,
                        button
                );
            }

            if(controller.getState()
                    .getSeccion()==
                    NairaDexState.Seccion.OBJETIVOS){

                if(objectivesPanel.mouseClicked(
                        mouseX,
                        mouseY
                )){
                    actualizarDatos();
                    return true;
                }

                return super.mouseClicked(
                        mouseX,
                        mouseY,
                        button
                );
            }

            if(manejarClickAccionesDex(
                    mouseX,
                    mouseY
            )){
                return true;
            }

            if(manejarClickPestanas(
                    mouseX,
                    mouseY
            )){
                return true;
            }

            if(controller.getState()
                    .getPestana()==
                    NairaDexState.Pestana.MOVIMIENTOS&&
                    movesTab.mouseClicked(
                            mouseX,
                            mouseY
                    )){
                return true;
            }

            if(selectorFormaVisible&&
                    dentro(
                            mouseX,
                            mouseY,
                            formaX,
                            formaY,
                            formaW,
                            formaH
                    )){

                seleccionarSiguienteForma();
                return true;
            }

            int contenidoX=
                    panelX+116+12;

            if(dentro(
                    mouseX,
                    mouseY,
                    contenidoX,
                    filtroY,
                    42,
                    18
            )){
                cambiarFiltro(
                        NairaDexState.FiltroColeccion.TODOS
                );

                return true;
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    contenidoX+48,
                    filtroY,
                    64,
                    18
            )){
                cambiarFiltro(
                        NairaDexState.FiltroColeccion.OBTENIDOS
                );

                return true;
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    contenidoX+118,
                    filtroY,
                    78,
                    18
            )){
                cambiarFiltro(
                        NairaDexState.FiltroColeccion.NO_OBTENIDOS
                );

                return true;
            }

            int inicioY=
                    catalogoY+27;

            if(dentro(
                    mouseX,
                    mouseY,
                    catalogoX,
                    inicioY-4,
                    catalogoW,
                    catalogoH-27
            )){
                int fila=
                        ((int)mouseY-(inicioY-4))
                                /ALTO_FILA;

                int indice=
                        scrollCatalogo+fila;

                if(indice>=0&&
                        indice<catalogo.size()){

                    PokemonSpeciesData pokemon=
                            catalogo.get(indice);

                    controller.seleccionarPokemon(
                            pokemon.getNombre()
                    );

                    spawnTab.resetScroll();
                    movesTab.resetScroll();
                    evolutionTab.resetScroll();
                    actualizarDatos();

                    return true;
                }
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private boolean manejarClickAccionesDex(
            double mouseX,
            double mouseY
    ){
        if(controller.getState().getSeccion()!=NairaDexState.Seccion.DEX||
                !accionesDexVisibles||
                resumen==null){

            return false;
        }

        String pokemon=resumen.getPokemon();
        String forma=controller.getState().getFormaSeleccionada();

        if(dentro(mouseX,mouseY,guardarX,guardarY,guardarW,guardarH)){
            objectivesService.alternarGuardado(pokemon,forma);
            return true;
        }

        if(dentro(mouseX,mouseY,objetivoX,objetivoY,objetivoW,objetivoH)){
            if(objectivesService.esObjetivoActivo(pokemon,forma)){
                objectivesService.hacerPrincipal(pokemon,forma);
            }else if(objectivesService.puedeAgregarObjetivo()){
                objectivesService.marcarObjetivo(pokemon,forma,spawns);
            }

            return true;
        }

        return false;
    }

    private boolean manejarClickNavegacion(
            double mouseX,
            double mouseY
    ){
        int navW=116;
        int headerH=30;

        int x=panelX;
        int y=panelY+headerH;

        int primeraY=y+12;
        int alto=21;

        if(dentro(
                mouseX,
                mouseY,
                x+6,
                primeraY,
                navW-13,
                alto
        )){
            controller.seleccionarSeccion(
                    NairaDexState.Seccion.DEX
            );

            actualizarDatos();
            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                x+6,
                primeraY+28,
                navW-13,
                alto
        )){
            controller.seleccionarSeccion(
                    NairaDexState.Seccion.COLECCION
            );

            collectionPanel.resetScroll();
            albumPanel.resetScroll();
            actualizarDatos();
            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                x+6,
                primeraY+56,
                navW-13,
                alto
        )){
            controller.seleccionarSeccion(
                    NairaDexState.Seccion.OBJETIVOS
            );

            objectivesPanel.resetScroll();
            actualizarDatos();
            return true;
        }

        return false;
    }

    private boolean manejarClickPestanas(
            double mouseX,
            double mouseY
    ){
        if(controller.getState()
                .getSeccion()!=
                NairaDexState.Seccion.DEX){

            return false;
        }

        if(mouseY<pestanaY||
                mouseY>=pestanaY+pestanaH){

            return false;
        }

        NairaDexState.Pestana[] valores={
                NairaDexState.Pestana.GENERAL,
                NairaDexState.Pestana.SPAWN,
                NairaDexState.Pestana.MOVIMIENTOS,
                NairaDexState.Pestana.EVOLUCION,
                NairaDexState.Pestana.CRIANZA
        };

        for(int i=0;i<pestanaX.length;i++){
            if(dentro(
                    mouseX,
                    mouseY,
                    pestanaX[i],
                    pestanaY,
                    pestanaW[i],
                    pestanaH
            )){
                controller.seleccionarPestana(
                        valores[i]
                );

                spawnTab.resetScroll();
                movesTab.resetScroll();
                evolutionTab.resetScroll();
                actualizarDatos();

                return true;
            }
        }

        return false;
    }

    private void seleccionarSiguienteForma(){
        if(formas==null||formas.size()<=1){
            return;
        }

        String actual=
                normalizarForma(
                        controller.getState()
                                .getFormaSeleccionada()
                );

        int indiceActual=-1;

        for(int i=0;i<formas.size();i++){
            PokemonFormData forma=
                    formas.get(i);

            if(forma==null)continue;

            String key=
                    normalizarForma(
                            forma.getForma()
                    );

            if(key.equals(actual)){
                indiceActual=i;
                break;
            }
        }

        int siguiente=
                indiceActual+1;

        if(siguiente<0||
                siguiente>=formas.size()){

            siguiente=0;
        }

        PokemonFormData nueva=
                formas.get(siguiente);

        if(nueva==null){
            return;
        }

        controller.seleccionarForma(
                nueva.isFormaBase()
                        ?""
                        :nueva.getForma()
        );

        spawnTab.resetScroll();
        movesTab.resetScroll();
        evolutionTab.resetScroll();
        actualizarDatos();
    }

    private void cambiarFiltro(
            NairaDexState.FiltroColeccion filtro
    ){
        controller.setFiltroColeccion(
                filtro
        );

        scrollCatalogo=0;

        actualizarDatos();
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ){
        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.DEX&&
                controller.getState()
                        .getPestana()==
                        NairaDexState.Pestana.MOVIMIENTOS&&
                movesTab.keyPressed(
                        keyCode,
                        scanCode,
                        modifiers
                )){

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    public boolean charTyped(
            char codePoint,
            int modifiers
    ){
        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.DEX&&
                controller.getState()
                        .getPestana()==
                        NairaDexState.Pestana.MOVIMIENTOS&&
                movesTab.charTyped(
                        codePoint,
                        modifiers
                )){

            return true;
        }

        return super.charTyped(
                codePoint,
                modifiers
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ){
        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.COLECCION){

            if(vistaColeccionAlbum){
                if(albumPanel.mouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                )){
                    return true;
                }
            }else if(collectionPanel.mouseScrolled(
                    mouseX,
                    mouseY,
                    delta
            )){
                return true;
            }
        }

        if(controller.getState()
                .getSeccion()==
                NairaDexState.Seccion.OBJETIVOS&&
                objectivesPanel.mouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                )){

            return true;
        }

        if(controller.getState()
                .getSeccion()!=
                NairaDexState.Seccion.DEX){

            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    delta
            );
        }

        if(controller.getState()
                .getPestana()==
                NairaDexState.Pestana.MOVIMIENTOS&&
                movesTab.mouseScrolled(
                        mouseX,
                        mouseY,
                        delta,
                        movimientos
                )){

            return true;
        }

        if(controller.getState()
                .getPestana()==NairaDexState.Pestana.SPAWN&&
                spawnTab.mouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                )){

            return true;
        }

        if(controller.getState()
                .getPestana()==NairaDexState.Pestana.EVOLUCION&&
                evolutionTab.mouseScrolled(
                        mouseX,
                        mouseY,
                        delta,
                        evoluciones
                )){

            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                catalogoX,
                catalogoY,
                catalogoW,
                catalogoH
        )){
            if(delta>0){
                scrollCatalogo-=3;
            }else if(delta<0){
                scrollCatalogo+=3;
            }

            limitarScroll();

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                delta
        );
    }

    private int getFilasVisibles(){
        return Math.max(
                0,
                (catalogoH-34)/ALTO_FILA
        );
    }

    private void limitarScroll(){
        int maximo=
                Math.max(
                        0,
                        catalogo.size()-
                                getFilasVisibles()
                );

        if(scrollCatalogo<0){
            scrollCatalogo=0;
        }

        if(scrollCatalogo>maximo){
            scrollCatalogo=maximo;
        }
    }

    private boolean esSeleccionado(
            PokemonSpeciesData pokemon
    ){
        String actual=
                controller.getState()
                        .getPokemonSeleccionado();

        return actual!=null&&
                actual.equalsIgnoreCase(
                        pokemon.getNombre()
                );
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

    @Override
    public boolean isPauseScreen(){
        return false;
    }
}
