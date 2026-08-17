package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

final class NairaDexAlbumPanel extends AbstractGui {
    private static final int FONDO_CARD=0xE319202A;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int FONDO_ACTIVO=0xE32B3440;
    private static final int FONDO_NO_OBTENIDO=0xB51A1F26;

    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_NO_OBTENIDO=0xFF6F7882;
    private static final int TEXTO_DORADO=0xFFE0B84F;

    private static final int MAX_GENERACION=9;
    private static final int GAP=5;
    private static final int CARD_H=68;
    private static final int SPRITE=38;

    private final NairaDexObjectivesService objectivesService=
            NairaDexObjectivesService.get();

    private FontRenderer font;

    private NairaCollectionSnapshot snapshot=
            NairaCollectionSnapshot.vacio();

    private List<PokemonSpeciesData> catalogo=
            Collections.emptyList();

    private List<PokemonSpeciesData> visibles=
            Collections.emptyList();

    private final List<CardHit> hits=
            new ArrayList<>();

    private PokemonSpeciesData pokemonParaAbrir;

    private PokemonSpeciesData hoverPokemon;
    private boolean hoverObtenido;
    private ObjetivoVisual hoverObjetivo=ObjetivoVisual.NINGUNO;
    private int hoverMouseX;
    private int hoverMouseY;

    private int generacion;
    private int scrollFila;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private int gridX;
    private int gridY;
    private int gridW;
    private int gridH;
    private int columnas=8;
    private int cardW=66;

    private int filtrosY;
    private int filtrosH=18;
    private final int[] filtroX=new int[MAX_GENERACION+1];
    private final int[] filtroW=new int[MAX_GENERACION+1];

    private int filtrosExtraY;
    private int estadoX;
    private int tipoX;
    private int categoriaX;
    private int limpiarX;

    private int estadoW=118;
    private int tipoW=118;
    private int categoriaW=142;
    private int limpiarW=72;

    private EstadoFiltro estadoFiltro=EstadoFiltro.TODOS;
    private TipoFiltro tipoFiltro=TipoFiltro.TODOS;
    private CategoriaFiltro categoriaFiltro=CategoriaFiltro.TODOS;

    private MenuFiltro menuAbierto=MenuFiltro.NINGUNO;
    private final List<MenuHit> menuHits=
            new ArrayList<>();

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            NairaCollectionSnapshot snapshot,
            List<PokemonSpeciesData> catalogoCompleto,
            String busqueda,
            int totalDex,
            boolean sincronizada,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        this.font=font;
        this.snapshot=
                snapshot==null
                        ?NairaCollectionSnapshot.vacio()
                        :snapshot;

        this.catalogo=
                catalogoCompleto==null
                        ?Collections.emptyList()
                        :catalogoCompleto;

        panelX=x;
        panelY=y;
        panelW=w;
        panelH=h;

        actualizarVisibles(
                busqueda
        );

        hits.clear();

        drawString(
                matrixStack,
                font,
                "ÁLBUM POKÉMON",
                x,
                y,
                TEXTO_ACENTO
        );

        String estado=
                sincronizada
                        ?"SINCRONIZADA"
                        :"ABRE /PC PARA ACTUALIZAR";

        drawString(
                matrixStack,
                font,
                estado,
                x+w-font.width(estado),
                y,
                sincronizada
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );

        int obtenidas=
                contarObtenidas(
                        generacion
                );

        int total=
                contarTotal(
                        generacion
                );

        String resumen=
                obtenidas+
                        " / "+
                        total+
                        " especies obtenidas";

        drawString(
                matrixStack,
                font,
                resumen,
                x,
                y+15,
                TEXTO
        );

        String porcentaje=
                total<=0
                        ?"0%"
                        :String.format(
                                Locale.ROOT,
                                "%.1f%%",
                                (obtenidas*100.0D)/total
                        );

        drawString(
                matrixStack,
                font,
                porcentaje,
                x+w-font.width(porcentaje),
                y+15,
                TEXTO_SECUNDARIO
        );

        dibujarProgreso(
                matrixStack,
                x,
                y+29,
                w,
                obtenidas,
                total
        );

        filtrosY=y+43;

        dibujarFiltros(
                matrixStack,
                mouseX,
                mouseY
        );

        filtrosExtraY=
                filtrosY+24;

        dibujarFiltrosExtra(
                matrixStack,
                mouseX,
                mouseY
        );

        gridX=x;
        gridY=filtrosExtraY+27;
        gridW=w;
        gridH=
                Math.max(
                        70,
                        h-(gridY-y)
                );

        fill(
                matrixStack,
                gridX,
                gridY,
                gridX+gridW,
                gridY+gridH,
                0x60111820
        );

        borde(
                matrixStack,
                gridX,
                gridY,
                gridW,
                gridH,
                BORDE
        );

        dibujarGrid(
                matrixStack,
                mouseX,
                mouseY
        );

        dibujarMenuAbierto(
                matrixStack,
                mouseX,
                mouseY
        );
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY
    ){
        if(menuAbierto!=MenuFiltro.NINGUNO){
            for(MenuHit hit:
                    menuHits){

                if(dentro(
                        mouseX,
                        mouseY,
                        hit.x,
                        hit.y,
                        hit.w,
                        hit.h
                )){
                    aplicarOpcionMenu(
                            hit.valor
                    );

                    menuAbierto=
                            MenuFiltro.NINGUNO;

                    scrollFila=0;
                    return true;
                }
            }

            if(!clickDentroBotonMenu(
                    mouseX,
                    mouseY
            )){
                menuAbierto=
                        MenuFiltro.NINGUNO;
            }
        }

        for(int i=0;i<=MAX_GENERACION;i++){
            if(filtroW[i]>0&&
                    dentro(
                            mouseX,
                            mouseY,
                            filtroX[i],
                            filtrosY,
                            filtroW[i],
                            filtrosH
                    )){

                generacion=i;
                menuAbierto=
                        MenuFiltro.NINGUNO;
                scrollFila=0;
                return true;
            }
        }

        if(dentro(
                mouseX,
                mouseY,
                estadoX,
                filtrosExtraY,
                estadoW,
                filtrosH
        )){
            alternarMenu(
                    MenuFiltro.ESTADO
            );
            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                tipoX,
                filtrosExtraY,
                tipoW,
                filtrosH
        )){
            alternarMenu(
                    MenuFiltro.TIPO
            );
            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                categoriaX,
                filtrosExtraY,
                categoriaW,
                filtrosH
        )){
            alternarMenu(
                    MenuFiltro.CATEGORIA
            );
            return true;
        }

        if(filtrosSecundariosActivos()&&
                dentro(
                        mouseX,
                        mouseY,
                        limpiarX,
                        filtrosExtraY,
                        limpiarW,
                        filtrosH
                )){
            generacion=0;
            estadoFiltro=
                    EstadoFiltro.TODOS;
            tipoFiltro=
                    TipoFiltro.TODOS;
            categoriaFiltro=
                    CategoriaFiltro.TODOS;
            menuAbierto=
                    MenuFiltro.NINGUNO;
            scrollFila=0;
            return true;
        }

        for(CardHit hit:hits){
            if(dentro(
                    mouseX,
                    mouseY,
                    hit.x,
                    hit.y,
                    hit.w,
                    hit.h
            )){
                pokemonParaAbrir=
                        hit.pokemon;

                menuAbierto=
                        MenuFiltro.NINGUNO;
                return true;
            }
        }

        return dentro(
                mouseX,
                mouseY,
                panelX,
                panelY,
                panelW,
                panelH
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
                gridX,
                gridY,
                gridW,
                gridH
        )){
            return false;
        }

        if(delta>0){
            scrollFila-=1;
        }else if(delta<0){
            scrollFila+=1;
        }

        limitarScroll();

        return true;
    }

    public PokemonSpeciesData consumirPokemonParaAbrir(){
        PokemonSpeciesData pokemon=
                pokemonParaAbrir;

        pokemonParaAbrir=null;

        return pokemon;
    }

    public void resetScroll(){
        scrollFila=0;
        pokemonParaAbrir=null;
    }

    private void actualizarVisibles(
            String busqueda
    ){
        String filtro=
                normalizar(
                        busqueda
                );

        List<PokemonSpeciesData> nueva=
                new ArrayList<>();

        for(PokemonSpeciesData pokemon:
                catalogo){

            if(pokemon==null||
                    pokemon.getNombre()==null){

                continue;
            }

            int gen=
                    pokemon.getGeneracion();

            if(generacion>0&&
                    gen!=generacion){

                continue;
            }

            if(!cumpleEstado(
                    pokemon
            )){
                continue;
            }

            if(!cumpleTipo(
                    pokemon
            )){
                continue;
            }

            if(!cumpleCategoria(
                    pokemon
            )){
                continue;
            }

            if(!filtro.isEmpty()){
                String nombre=
                        normalizar(
                                pokemon.getNombre()
                        );

                String numero=
                        String.valueOf(
                                pokemon.getNumeroDex()
                        );

                if(!nombre.contains(filtro)&&
                        !numero.contains(filtro)){

                    continue;
                }
            }

            nueva.add(
                    pokemon
            );
        }

        nueva.sort(
                Comparator.comparingInt(
                        PokemonSpeciesData::getNumeroDex
                )
        );

        visibles=
                Collections.unmodifiableList(
                        nueva
                );

        limitarScroll();
    }

    private void dibujarFiltros(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        int x=panelX;

        int todosW=48;

        filtroX[0]=x;
        filtroW[0]=todosW;

        dibujarFiltro(
                matrixStack,
                x,
                filtrosY,
                todosW,
                "TODOS",
                generacion==0,
                dentro(
                        mouseX,
                        mouseY,
                        x,
                        filtrosY,
                        todosW,
                        filtrosH
                )
        );

        x+=todosW+4;

        int disponible=
                panelW-
                        todosW-
                        4-
                        (4*MAX_GENERACION);

        int genW=
                Math.max(
                        24,
                        Math.min(
                                34,
                                disponible/
                                        MAX_GENERACION
                        )
                );

        for(int gen=1;
                gen<=MAX_GENERACION;
                gen++){

            filtroX[gen]=x;
            filtroW[gen]=genW;

            String texto=
                    romano(
                            gen
                    );

            dibujarFiltro(
                    matrixStack,
                    x,
                    filtrosY,
                    genW,
                    texto,
                    generacion==gen,
                    dentro(
                            mouseX,
                            mouseY,
                            x,
                            filtrosY,
                            genW,
                            filtrosH
                    )
            );

            x+=genW+4;
        }

    }

    private void dibujarFiltrosExtra(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        estadoX=panelX;
        tipoX=estadoX+estadoW+5;
        categoriaX=tipoX+tipoW+5;
        limpiarX=categoriaX+categoriaW+5;

        dibujarSelector(
                matrixStack,
                estadoX,
                filtrosExtraY,
                estadoW,
                "ESTADO: "+
                        estadoFiltro.etiqueta,
                menuAbierto==
                        MenuFiltro.ESTADO,
                dentro(
                        mouseX,
                        mouseY,
                        estadoX,
                        filtrosExtraY,
                        estadoW,
                        filtrosH
                )
        );

        dibujarSelector(
                matrixStack,
                tipoX,
                filtrosExtraY,
                tipoW,
                "TIPO: "+
                        tipoFiltro.etiqueta,
                menuAbierto==
                        MenuFiltro.TIPO,
                dentro(
                        mouseX,
                        mouseY,
                        tipoX,
                        filtrosExtraY,
                        tipoW,
                        filtrosH
                )
        );

        dibujarSelector(
                matrixStack,
                categoriaX,
                filtrosExtraY,
                categoriaW,
                "CATEGORÍA: "+
                        categoriaFiltro.etiqueta,
                menuAbierto==
                        MenuFiltro.CATEGORIA,
                dentro(
                        mouseX,
                        mouseY,
                        categoriaX,
                        filtrosExtraY,
                        categoriaW,
                        filtrosH
                )
        );

        int xTexto=
                categoriaX+
                        categoriaW+
                        6;

        if(filtrosSecundariosActivos()&&
                limpiarX+
                        limpiarW<=
                        panelX+
                                panelW-
                                80){

            dibujarSelector(
                    matrixStack,
                    limpiarX,
                    filtrosExtraY,
                    limpiarW,
                    "LIMPIAR",
                    false,
                    dentro(
                            mouseX,
                            mouseY,
                            limpiarX,
                            filtrosExtraY,
                            limpiarW,
                            filtrosH
                    )
            );

            xTexto=
                    limpiarX+
                            limpiarW+
                            6;
        }

        String mostrando=
                visibles.size()+
                        " visibles";

        int textoX=
                Math.max(
                        xTexto,
                        panelX+
                                panelW-
                                font.width(
                                        mostrando
                                )
                );

        drawString(
                matrixStack,
                font,
                mostrando,
                textoX,
                filtrosExtraY+5,
                TEXTO_SECUNDARIO
        );
    }

    private void dibujarSelector(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String texto,
            boolean abierto,
            boolean hover
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+filtrosH,
                abierto
                        ?FONDO_ACTIVO
                        :hover
                                ?FONDO_HOVER
                                :FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                filtrosH,
                abierto
                        ?ACENTO
                        :BORDE
        );

        String visible=
                limitarTexto(
                        texto,
                        w-18
                );

        drawString(
                matrixStack,
                font,
                visible,
                x+6,
                y+5,
                abierto
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "▼",
                x+w-12,
                y+5,
                abierto
                        ?TEXTO_ACENTO
                        :TEXTO_SECUNDARIO
        );
    }

    private void dibujarMenuAbierto(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        menuHits.clear();

        if(menuAbierto==
                MenuFiltro.NINGUNO){

            return;
        }

        if(menuAbierto==
                MenuFiltro.ESTADO){

            dibujarMenuSimple(
                    matrixStack,
                    estadoX,
                    filtrosExtraY+
                            filtrosH+
                            2,
                    estadoW,
                    EstadoFiltro.values(),
                    mouseX,
                    mouseY
            );

            return;
        }

        if(menuAbierto==
                MenuFiltro.CATEGORIA){

            dibujarMenuSimple(
                    matrixStack,
                    categoriaX,
                    filtrosExtraY+
                            filtrosH+
                            2,
                    categoriaW,
                    CategoriaFiltro.values(),
                    mouseX,
                    mouseY
            );

            return;
        }

        dibujarMenuTipos(
                matrixStack,
                tipoX,
                filtrosExtraY+
                        filtrosH+
                        2,
                mouseX,
                mouseY
        );
    }

    private void dibujarMenuSimple(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            Object[] valores,
            int mouseX,
            int mouseY
    ){
        int itemH=18;
        int alto=
                valores.length*
                        itemH+
                        2;

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+alto,
                0xFA0A0E14
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                alto,
                ACENTO
        );

        for(int i=0;
                i<valores.length;
                i++){

            Object valor=
                    valores[i];

            String etiqueta=
                    etiquetaDe(
                            valor
                    );

            int yy=
                    y+1+
                            i*
                                    itemH;

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            x+1,
                            yy,
                            w-2,
                            itemH
                    );

            boolean activo=
                    opcionActiva(
                            valor
                    );

            if(hover||
                    activo){

                fill(
                        matrixStack,
                        x+1,
                        yy,
                        x+w-1,
                        yy+itemH,
                        activo
                                ?FONDO_ACTIVO
                                :FONDO_HOVER
                );
            }

            drawString(
                    matrixStack,
                    font,
                    etiqueta,
                    x+7,
                    yy+5,
                    activo
                            ?TEXTO_ACENTO
                            :TEXTO
            );

            menuHits.add(
                    new MenuHit(
                            x+1,
                            yy,
                            w-2,
                            itemH,
                            valor
                    )
            );
        }
    }

    private void dibujarMenuTipos(
            MatrixStack matrixStack,
            int x,
            int y,
            int mouseX,
            int mouseY
    ){
        TipoFiltro[] valores=
                TipoFiltro.values();

        int columnasMenu=2;
        int filas=
                (valores.length+
                        columnasMenu-
                        1)/
                        columnasMenu;

        int itemH=18;
        int colW=104;
        int ancho=
                columnasMenu*
                        colW+
                        2;
        int alto=
                filas*
                        itemH+
                        2;

        int menuX=
                Math.min(
                        x,
                        panelX+
                                panelW-
                                ancho
                );

        fill(
                matrixStack,
                menuX,
                y,
                menuX+ancho,
                y+alto,
                0xFA0A0E14
        );

        borde(
                matrixStack,
                menuX,
                y,
                ancho,
                alto,
                ACENTO
        );

        for(int i=0;
                i<valores.length;
                i++){

            int col=
                    i/
                            filas;

            int fila=
                    i%
                            filas;

            int xx=
                    menuX+
                            1+
                            col*
                                    colW;

            int yy=
                    y+
                            1+
                            fila*
                                    itemH;

            TipoFiltro valor=
                    valores[i];

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            xx,
                            yy,
                            colW,
                            itemH
                    );

            boolean activo=
                    valor==
                            tipoFiltro;

            if(hover||
                    activo){

                fill(
                        matrixStack,
                        xx,
                        yy,
                        xx+colW,
                        yy+itemH,
                        activo
                                ?FONDO_ACTIVO
                                :FONDO_HOVER
                );
            }

            drawString(
                    matrixStack,
                    font,
                    valor.etiqueta,
                    xx+6,
                    yy+5,
                    activo
                            ?TEXTO_ACENTO
                            :TEXTO
            );

            menuHits.add(
                    new MenuHit(
                            xx,
                            yy,
                            colW,
                            itemH,
                            valor
                    )
            );
        }
    }

    private void alternarMenu(
            MenuFiltro menu
    ){
        menuAbierto=
                menuAbierto==menu
                        ?MenuFiltro.NINGUNO
                        :menu;
    }

    private boolean clickDentroBotonMenu(
            double mouseX,
            double mouseY
    ){
        if(menuAbierto==
                MenuFiltro.ESTADO){

            return dentro(
                    mouseX,
                    mouseY,
                    estadoX,
                    filtrosExtraY,
                    estadoW,
                    filtrosH
            );
        }

        if(menuAbierto==
                MenuFiltro.TIPO){

            return dentro(
                    mouseX,
                    mouseY,
                    tipoX,
                    filtrosExtraY,
                    tipoW,
                    filtrosH
            );
        }

        if(menuAbierto==
                MenuFiltro.CATEGORIA){

            return dentro(
                    mouseX,
                    mouseY,
                    categoriaX,
                    filtrosExtraY,
                    categoriaW,
                    filtrosH
            );
        }

        return false;
    }

    private void aplicarOpcionMenu(
            Object valor
    ){
        if(valor instanceof EstadoFiltro){
            estadoFiltro=
                    (EstadoFiltro)valor;
        }else if(valor instanceof TipoFiltro){
            tipoFiltro=
                    (TipoFiltro)valor;
        }else if(valor instanceof CategoriaFiltro){
            categoriaFiltro=
                    (CategoriaFiltro)valor;
        }
    }

    private boolean opcionActiva(
            Object valor
    ){
        if(valor instanceof EstadoFiltro){
            return valor==
                    estadoFiltro;
        }

        if(valor instanceof TipoFiltro){
            return valor==
                    tipoFiltro;
        }

        if(valor instanceof CategoriaFiltro){
            return valor==
                    categoriaFiltro;
        }

        return false;
    }

    private String etiquetaDe(
            Object valor
    ){
        if(valor instanceof EstadoFiltro){
            return ((EstadoFiltro)valor).etiqueta;
        }

        if(valor instanceof CategoriaFiltro){
            return ((CategoriaFiltro)valor).etiqueta;
        }

        if(valor instanceof TipoFiltro){
            return ((TipoFiltro)valor).etiqueta;
        }

        return String.valueOf(
                valor
        );
    }

    private boolean filtrosSecundariosActivos(){
        return generacion!=0||
                estadoFiltro!=
                        EstadoFiltro.TODOS||
                tipoFiltro!=
                        TipoFiltro.TODOS||
                categoriaFiltro!=
                        CategoriaFiltro.TODOS;
    }

    private boolean cumpleEstado(
            PokemonSpeciesData pokemon
    ){
        if(estadoFiltro==
                EstadoFiltro.TODOS){

            return true;
        }

        boolean obtenido=
                snapshot.tiene(
                        pokemon.getNombre()
                );

        if(estadoFiltro==
                EstadoFiltro.OBTENIDOS){

            return obtenido;
        }

        if(estadoFiltro==
                EstadoFiltro.NO_OBTENIDOS){

            return !obtenido;
        }

        return esObjetivo(
                pokemon.getNombre()
        );
    }

    private boolean cumpleTipo(
            PokemonSpeciesData pokemon
    ){
        if(tipoFiltro==
                TipoFiltro.TODOS){

            return true;
        }

        List<String> tipos=
                pokemon.getTipos();

        if(tipos==null||
                tipos.isEmpty()){

            return false;
        }

        for(String tipo:
                tipos){

            if(tipoFiltro.coincide(
                    tipo
            )){

                return true;
            }
        }

        return false;
    }

    private boolean cumpleCategoria(
            PokemonSpeciesData pokemon
    ){
        if(categoriaFiltro==
                CategoriaFiltro.TODOS){

            return true;
        }

        if(categoriaFiltro==
                CategoriaFiltro.LEGENDARIOS){

            return pokemon.isLegendario();
        }

        if(categoriaFiltro==
                CategoriaFiltro.MITICOS){

            return pokemon.isMitico();
        }

        if(categoriaFiltro==
                CategoriaFiltro.ULTRAENTES){

            return pokemon.isUltraente();
        }

        return !pokemon.isLegendario()&&
                !pokemon.isMitico()&&
                !pokemon.isUltraente();
    }

    private boolean esObjetivo(
            String pokemon
    ){
        for(NairaDexObjectivesService.Objetivo objetivo:
                objectivesService.getObjetivosActivos()){

            if(objetivo!=null&&
                    objetivo.getPokemon()!=null&&
                    objetivo.getPokemon()
                            .equalsIgnoreCase(
                                    pokemon
                            )){

                return true;
            }
        }

        return false;
    }

    private void dibujarFiltro(
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
                y+filtrosH,
                activo
                        ?FONDO_ACTIVO
                        :hover
                                ?FONDO_HOVER
                                :FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                filtrosH,
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

    private void dibujarProgreso(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int obtenidas,
            int total
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+6,
                FONDO_CARD
        );

        int progreso=
                total<=0
                        ?0
                        :(int)Math.round(
                                (w*obtenidas)/
                                        (double)total
                        );

        if(progreso>0){
            fill(
                    matrixStack,
                    x,
                    y,
                    x+Math.min(w,progreso),
                    y+6,
                    TEXTO_VERDE
            );
        }

        borde(
                matrixStack,
                x,
                y,
                w,
                6,
                BORDE
        );
    }

    private void dibujarGrid(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        hoverPokemon=null;
        hoverObjetivo=ObjetivoVisual.NINGUNO;

        int anchoUtil=
                Math.max(
                        1,
                        gridW-10
                );

        columnas=
                Math.max(
                        5,
                        Math.min(
                                10,
                                (anchoUtil+GAP)/
                                        (64+GAP)
                        )
                );

        cardW=
                Math.max(
                        54,
                        (anchoUtil-
                                (GAP*(columnas-1)))/
                                columnas
                );

        int filasVisibles=
                Math.max(
                        1,
                        (gridH+GAP)/
                                (CARD_H+GAP)+1
                );

        limitarScroll();

        int filaInicio=
                scrollFila;

        int filaFin=
                filaInicio+
                        filasVisibles;

        for(int fila=filaInicio;
                fila<filaFin;
                fila++){

            int baseIndice=
                    fila*columnas;

            if(baseIndice>=visibles.size()){
                break;
            }

            for(int col=0;
                    col<columnas;
                    col++){

                int indice=
                        baseIndice+col;

                if(indice>=visibles.size()){
                    break;
                }

                PokemonSpeciesData pokemon=
                        visibles.get(indice);

                int xx=
                        gridX+
                                5+
                                col*
                                        (cardW+GAP);

                int yy=
                        gridY+
                                5+
                                (fila-filaInicio)*
                                        (CARD_H+GAP);

                if(yy>=gridY+gridH-4){
                    continue;
                }

                int alto=
                        Math.min(
                                CARD_H,
                                gridY+gridH-5-yy
                        );

                if(alto<28){
                    continue;
                }

                dibujarCarta(
                        matrixStack,
                        pokemon,
                        xx,
                        yy,
                        cardW,
                        alto,
                        mouseX,
                        mouseY
                );
            }
        }

        if(visibles.isEmpty()){
            String vacio=
                    "No hay Pokémon para este filtro.";

            drawString(
                    matrixStack,
                    font,
                    vacio,
                    gridX+
                            (gridW-font.width(vacio))/2,
                    gridY+24,
                    TEXTO_SECUNDARIO
            );
        }

        if(hoverPokemon!=null){
            dibujarTooltip(
                    matrixStack,
                    hoverPokemon,
                    hoverObtenido,
                    hoverObjetivo,
                    hoverMouseX,
                    hoverMouseY
            );
        }
    }

    private void dibujarCarta(
            MatrixStack matrixStack,
            PokemonSpeciesData pokemon,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        boolean obtenido=
                snapshot.tiene(
                        pokemon.getNombre()
                );

        ObjetivoVisual objetivo=
                getObjetivoVisual(
                        pokemon.getNombre()
                );

        boolean hover=
                dentro(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        w,
                        h
                );

        int fondo=
                hover
                        ?FONDO_HOVER
                        :obtenido
                                ?FONDO_CARD
                                :FONDO_NO_OBTENIDO;

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                fondo
        );

        int bordeColor=
                objetivo.activo
                        ?objetivo.color
                        :hover
                                ?ACENTO
                                :BORDE;

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                bordeColor
        );

        if(objetivo.principal&&
                w>8&&
                h>8){

            borde(
                    matrixStack,
                    x+2,
                    y+2,
                    w-4,
                    h-4,
                    objetivo.color
            );
        }

        int spriteX=
                x+
                        (w-SPRITE)/2;

        int spriteY=
                y+5;

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                pokemon.getNombre(),
                "",
                spriteX,
                spriteY,
                SPRITE
        );

        if(!obtenido){
            fill(
                    matrixStack,
                    spriteX,
                    spriteY,
                    spriteX+SPRITE,
                    spriteY+SPRITE,
                    0x992B3037
            );
        }

        String numero=
                String.format(
                        Locale.ROOT,
                        "#%03d",
                        pokemon.getNumeroDex()
                );

        drawString(
                matrixStack,
                font,
                numero,
                x+4,
                y+45,
                obtenido
                        ?TEXTO_SECUNDARIO
                        :TEXTO_NO_OBTENIDO
        );

        String nombre=
                limitarTexto(
                        pokemon.getNombre(),
                        w-8
                );

        drawString(
                matrixStack,
                font,
                nombre,
                x+
                        (w-font.width(nombre))/2,
                y+56,
                obtenido
                        ?TEXTO
                        :TEXTO_NO_OBTENIDO
        );

        if(obtenido){
            drawString(
                    matrixStack,
                    font,
                    "✓",
                    x+w-10,
                    y+4,
                    TEXTO_VERDE
            );
        }

        if(objetivo.activo){
            drawString(
                    matrixStack,
                    font,
                    String.valueOf(
                            objetivo.slot+1
                    ),
                    x+4,
                    y+4,
                    objetivo.color
            );
        }

        hits.add(
                new CardHit(
                        x,
                        y,
                        w,
                        h,
                        pokemon
                )
        );

        if(hover){
            hoverPokemon=pokemon;
            hoverObtenido=obtenido;
            hoverObjetivo=objetivo;
            hoverMouseX=mouseX;
            hoverMouseY=mouseY;
        }
    }

    private void dibujarTooltip(
            MatrixStack matrixStack,
            PokemonSpeciesData pokemon,
            boolean obtenido,
            ObjetivoVisual objetivo,
            int mouseX,
            int mouseY
    ){
        String linea1=
                String.format(
                        Locale.ROOT,
                        "#%04d %s",
                        pokemon.getNumeroDex(),
                        pokemon.getNombre()
                );

        String linea2=
                obtenido
                        ?"Obtenido · x"+
                                snapshot.getCantidad(
                                        pokemon.getNombre()
                                )
                        :"No obtenido";

        String linea3="";

        if(objetivo.activo){
            linea3=
                    "Objetivo "+
                            (objetivo.slot+1)+
                            (objetivo.principal
                                    ?" · PRINCIPAL"
                                    :"");
        }

        int ancho=
                Math.max(
                        font.width(linea1),
                        font.width(linea2)
                );

        if(!linea3.isEmpty()){
            ancho=
                    Math.max(
                            ancho,
                            font.width(linea3)
                    );
        }

        ancho+=12;

        int alto=
                linea3.isEmpty()
                        ?31
                        :43;

        int tx=
                mouseX+10;

        int ty=
                mouseY+9;

        if(tx+ancho>panelX+panelW){
            tx=
                    mouseX-ancho-8;
        }

        if(ty+alto>panelY+panelH){
            ty=
                    mouseY-alto-8;
        }

        fill(
                matrixStack,
                tx,
                ty,
                tx+ancho,
                ty+alto,
                0xFA0A0E14
        );

        borde(
                matrixStack,
                tx,
                ty,
                ancho,
                alto,
                objetivo.activo
                        ?objetivo.color
                        :BORDE
        );

        drawString(
                matrixStack,
                font,
                linea1,
                tx+6,
                ty+6,
                TEXTO
        );

        drawString(
                matrixStack,
                font,
                linea2,
                tx+6,
                ty+18,
                obtenido
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );

        if(!linea3.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    linea3,
                    tx+6,
                    ty+30,
                    objetivo.color
            );
        }
    }

    private ObjetivoVisual getObjetivoVisual(
            String pokemon
    ){
        for(NairaDexObjectivesService.Objetivo objetivo:
                objectivesService.getObjetivosActivos()){

            if(objetivo!=null&&
                    objetivo.getPokemon()!=null&&
                    objetivo.getPokemon()
                            .equalsIgnoreCase(
                                    pokemon
                            )){

                return new ObjetivoVisual(
                        true,
                        objetivo.getSlot(),
                        objectivesService.getColorObjetivo(
                                objetivo
                        ),
                        objectivesService.esObjetivoPrincipal(
                                objetivo.getPokemon(),
                                objetivo.getForma()
                        )
                );
            }
        }

        return ObjetivoVisual.NINGUNO;
    }

    private int contarTotal(
            int gen
    ){
        int total=0;

        for(PokemonSpeciesData pokemon:
                catalogo){

            if(pokemon==null){
                continue;
            }

            if(gen==0||
                    pokemon.getGeneracion()==gen){

                total++;
            }
        }

        return total;
    }

    private int contarObtenidas(
            int gen
    ){
        int total=0;

        for(PokemonSpeciesData pokemon:
                catalogo){

            if(pokemon==null||
                    pokemon.getNombre()==null){

                continue;
            }

            if((gen==0||
                    pokemon.getGeneracion()==gen)&&
                    snapshot.tiene(
                            pokemon.getNombre()
                    )){

                total++;
            }
        }

        return total;
    }

    private void limitarScroll(){
        if(scrollFila<0){
            scrollFila=0;
        }

        int cols=
                Math.max(
                        5,
                        columnas
                );

        int totalFilas=
                visibles.isEmpty()
                        ?0
                        :(visibles.size()+cols-1)/
                                cols;

        int filasVisibles=
                gridH<=0
                        ?1
                        :Math.max(
                                1,
                                gridH/
                                        (CARD_H+GAP)
                        );

        int max=
                Math.max(
                        0,
                        totalFilas-
                                filasVisibles
                );

        if(scrollFila>max){
            scrollFila=max;
        }
    }

    private String limitarTexto(
            String texto,
            int maxAncho
    ){
        if(texto==null){
            return "";
        }

        if(font.width(texto)<=maxAncho){
            return texto;
        }

        String base=texto;

        while(base.length()>1&&
                font.width(
                        base+"…"
                )>maxAncho){

            base=
                    base.substring(
                            0,
                            base.length()-1
                    );
        }

        return base+"…";
    }

    private static String romano(
            int numero
    ){
        switch(numero){
            case 1:return "I";
            case 2:return "II";
            case 3:return "III";
            case 4:return "IV";
            case 5:return "V";
            case 6:return "VI";
            case 7:return "VII";
            case 8:return "VIII";
            case 9:return "IX";
            default:return String.valueOf(numero);
        }
    }

    private static String normalizar(
            String texto
    ){
        if(texto==null){
            return "";
        }

        String valor=
                Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                )
                        .replaceAll(
                                "\\p{M}+",
                                ""
                        )
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return valor.replace(
                "_",
                ""
        )
                .replace(
                        "-",
                        ""
                )
                .replace(
                        " ",
                        ""
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
        fill(matrixStack,x,y,x+w,y+1,color);
        fill(matrixStack,x,y+h-1,x+w,y+h,color);
        fill(matrixStack,x,y,x+1,y+h,color);
        fill(matrixStack,x+w-1,y,x+w,y+h,color);
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

    private enum MenuFiltro {
        NINGUNO,
        ESTADO,
        TIPO,
        CATEGORIA
    }

    private enum EstadoFiltro {
        TODOS("Todos"),
        OBTENIDOS("Obtenidos"),
        NO_OBTENIDOS("No obtenidos"),
        OBJETIVOS("Objetivos");

        private final String etiqueta;

        EstadoFiltro(
                String etiqueta
        ){
            this.etiqueta=etiqueta;
        }
    }

    private enum CategoriaFiltro {
        TODOS("Todos"),
        COMUNES("Comunes"),
        LEGENDARIOS("Legendarios"),
        MITICOS("Míticos"),
        ULTRAENTES("Ultraentes");

        private final String etiqueta;

        CategoriaFiltro(
                String etiqueta
        ){
            this.etiqueta=etiqueta;
        }
    }

    private enum TipoFiltro {
        TODOS("Todos",""),
        NORMAL("Normal","normal"),
        FUEGO("Fuego","fire"),
        AGUA("Agua","water"),
        PLANTA("Planta","grass"),
        ELECTRICO("Eléctrico","electric"),
        HIELO("Hielo","ice"),
        LUCHA("Lucha","fighting"),
        VENENO("Veneno","poison"),
        TIERRA("Tierra","ground"),
        VOLADOR("Volador","flying"),
        PSIQUICO("Psíquico","psychic"),
        BICHO("Bicho","bug"),
        ROCA("Roca","rock"),
        FANTASMA("Fantasma","ghost"),
        DRAGON("Dragón","dragon"),
        SINIESTRO("Siniestro","dark"),
        ACERO("Acero","steel"),
        HADA("Hada","fairy");

        private final String etiqueta;
        private final String clave;

        TipoFiltro(
                String etiqueta,
                String clave
        ){
            this.etiqueta=etiqueta;
            this.clave=clave;
        }

        private boolean coincide(
                String tipo
        ){
            String valor=
                    normalizar(
                            tipo
                    );

            if(valor.equals(
                    clave
            )){
                return true;
            }

            if(this==
                    PLANTA){
                return valor.equals(
                        "planta"
                );
            }

            if(this==
                    FUEGO){
                return valor.equals(
                        "fuego"
                );
            }

            if(this==
                    AGUA){
                return valor.equals(
                        "agua"
                );
            }

            if(this==
                    ELECTRICO){
                return valor.equals(
                        "electrico"
                );
            }

            if(this==
                    HIELO){
                return valor.equals(
                        "hielo"
                );
            }

            if(this==
                    LUCHA){
                return valor.equals(
                        "lucha"
                );
            }

            if(this==
                    VENENO){
                return valor.equals(
                        "veneno"
                );
            }

            if(this==
                    TIERRA){
                return valor.equals(
                        "tierra"
                );
            }

            if(this==
                    VOLADOR){
                return valor.equals(
                        "volador"
                );
            }

            if(this==
                    PSIQUICO){
                return valor.equals(
                        "psiquico"
                );
            }

            if(this==
                    BICHO){
                return valor.equals(
                        "bicho"
                );
            }

            if(this==
                    ROCA){
                return valor.equals(
                        "roca"
                );
            }

            if(this==
                    FANTASMA){
                return valor.equals(
                        "fantasma"
                );
            }

            if(this==
                    DRAGON){
                return valor.equals(
                        "dragon"
                );
            }

            if(this==
                    SINIESTRO){
                return valor.equals(
                        "siniestro"
                );
            }

            if(this==
                    ACERO){
                return valor.equals(
                        "acero"
                );
            }

            if(this==
                    HADA){
                return valor.equals(
                        "hada"
                );
            }

            return this==
                    NORMAL&&
                    valor.equals(
                            "normal"
                    );
        }
    }

    private static final class MenuHit {
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final Object valor;

        private MenuHit(
                int x,
                int y,
                int w,
                int h,
                Object valor
        ){
            this.x=x;
            this.y=y;
            this.w=w;
            this.h=h;
            this.valor=valor;
        }
    }

    private static final class CardHit {
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final PokemonSpeciesData pokemon;

        private CardHit(
                int x,
                int y,
                int w,
                int h,
                PokemonSpeciesData pokemon
        ){
            this.x=x;
            this.y=y;
            this.w=w;
            this.h=h;
            this.pokemon=pokemon;
        }
    }

    private static final class ObjetivoVisual {
        private static final ObjetivoVisual NINGUNO=
                new ObjetivoVisual(
                        false,
                        -1,
                        BORDE,
                        false
                );

        private final boolean activo;
        private final int slot;
        private final int color;
        private final boolean principal;

        private ObjetivoVisual(
                boolean activo,
                int slot,
                int color,
                boolean principal
        ){
            this.activo=activo;
            this.slot=slot;
            this.color=color;
            this.principal=principal;
        }
    }
}
