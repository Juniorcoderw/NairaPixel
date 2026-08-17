package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.collection.NairaCollectionSnapshot;
import com.hjunior.nairapixel.client.collection.OwnedPokemonData;
import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.species.PokemonSpeciesData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class NairaDexCollectionPanel extends AbstractGui {
    private static final int FONDO_CARD=0xE319202A;
    private static final int FONDO_NAV=0xF20A0E14;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int FONDO_ACTIVO=0xE32B3440;

    private static final int BORDE=0xFF384653;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;

    private static final int FILA_ESPECIE=28;
    private static final int FILA_EJEMPLAR=21;

    private FontRenderer font;

    private NairaCollectionSnapshot snapshot=
            NairaCollectionSnapshot.vacio();

    private List<PokemonSpeciesData> lista=
            Collections.emptyList();

    private List<OwnedPokemonData> ejemplares=
            Collections.emptyList();

    private NairaCollectionSnapshot ultimoSnapshot;
    private List<PokemonSpeciesData> ultimoCatalogo;
    private String ultimaBusqueda="";

    private PokemonSpeciesData seleccionada;

    private OwnedPokemonData ejemplarSeleccionado;

    private int irPcX;
    private int irPcY;
    private int irPcW=58;
    private int irPcH=17;
    private boolean irPcVisible;

    private String mensajePc="";

    private int listaX;
    private int listaY;
    private int listaW;
    private int listaH;

    private int ejemplaresX;
    private int ejemplaresY;
    private int ejemplaresW;
    private int ejemplaresH;

    private int scrollEspecies;
    private int scrollEjemplares;

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

        actualizarLista(
                catalogoCompleto,
                busqueda
        );

        int tituloY=y;

        drawString(
                matrixStack,
                font,
                "MI COLECCIÓN",
                x,
                tituloY,
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
                tituloY,
                sincronizada
                        ?TEXTO_VERDE
                        :TEXTO_SECUNDARIO
        );

        int metricasY=y+17;
        int gap=5;
        int tarjetas=6;
        int tarjetaW=
                Math.max(
                        70,
                        (w-(gap*(tarjetas-1)))/
                                tarjetas
                );

        dibujarMetrica(
                matrixStack,
                x,
                metricasY,
                tarjetaW,
                "TOTAL",
                String.valueOf(
                        this.snapshot.getTotalPokemon()
                )
        );

        dibujarMetrica(
                matrixStack,
                x+(tarjetaW+gap),
                metricasY,
                tarjetaW,
                "ESPECIES",
                String.valueOf(
                        this.snapshot.getEspeciesDistintas()
                )
        );

        dibujarMetrica(
                matrixStack,
                x+((tarjetaW+gap)*2),
                metricasY,
                tarjetaW,
                "SHINY",
                String.valueOf(
                        this.snapshot.getTotalShiny()
                )
        );

        dibujarMetrica(
                matrixStack,
                x+((tarjetaW+gap)*3),
                metricasY,
                tarjetaW,
                "PC",
                String.valueOf(
                        this.snapshot.getTotalPC()
                )
        );

        dibujarMetrica(
                matrixStack,
                x+((tarjetaW+gap)*4),
                metricasY,
                tarjetaW,
                "EQUIPO",
                String.valueOf(
                        this.snapshot.getTotalEquipo()
                )
        );

        String progreso=
                totalDex<=0
                        ?"0%"
                        :String.format(
                        Locale.ROOT,
                        "%.1f%%",
                        (this.snapshot.getEspeciesDistintas()*
                                100.0D)/
                                totalDex
                );

        dibujarMetrica(
                matrixStack,
                x+((tarjetaW+gap)*5),
                metricasY,
                tarjetaW,
                "DEX",
                progreso
        );

        int cuerpoY=metricasY+40;
        int cuerpoH=
                Math.max(
                        90,
                        h-(cuerpoY-y)
                );

        int izquierdaW=
                Math.max(
                        210,
                        (int)(w*0.37F)
                );

        listaX=x;
        listaY=cuerpoY;
        listaW=izquierdaW;
        listaH=cuerpoH;

        int detalleX=
                listaX+listaW+8;

        int detalleW=
                Math.max(
                        170,
                        w-listaW-8
                );

        dibujarLista(
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

    public boolean mouseClicked(
            double mouseX,
            double mouseY
    ){
        if(irPcVisible&&
                dentro(
                        mouseX,
                        mouseY,
                        irPcX,
                        irPcY,
                        irPcW,
                        irPcH
                )){

            return abrirEnPc(
                    ejemplarSeleccionado
            );
        }

        if(dentro(
                mouseX,
                mouseY,
                ejemplaresX,
                ejemplaresY,
                ejemplaresW,
                ejemplaresH
        )){
            int fila=
                    ((int)mouseY-
                            ejemplaresY)/
                            FILA_EJEMPLAR;

            int indice=
                    scrollEjemplares+
                            fila;

            if(indice>=0&&
                    indice<ejemplares.size()){

                ejemplarSeleccionado=
                        ejemplares.get(indice);

                mensajePc="";
            }

            return true;
        }

        if(!dentro(
                mouseX,
                mouseY,
                listaX,
                listaY,
                listaW,
                listaH
        )){
            return false;
        }

        int inicioY=listaY+27;

        if(mouseY<inicioY-3){
            return true;
        }

        int fila=
                ((int)mouseY-
                        (inicioY-3))/
                        FILA_ESPECIE;

        int indice=
                scrollEspecies+
                        fila;

        if(indice>=0&&
                indice<lista.size()){

            seleccionada=
                    lista.get(indice);

            scrollEjemplares=0;
            mensajePc="";

            actualizarEjemplares();
        }

        return true;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ){
        if(dentro(
                mouseX,
                mouseY,
                listaX,
                listaY,
                listaW,
                listaH
        )){
            if(delta>0){
                scrollEspecies-=3;
            }else if(delta<0){
                scrollEspecies+=3;
            }

            limitarScrollEspecies();

            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                ejemplaresX,
                ejemplaresY,
                ejemplaresW,
                ejemplaresH
        )){
            if(delta>0){
                scrollEjemplares-=3;
            }else if(delta<0){
                scrollEjemplares+=3;
            }

            limitarScrollEjemplares();

            return true;
        }

        return false;
    }

    public void resetScroll(){
        scrollEspecies=0;
        scrollEjemplares=0;
        ejemplarSeleccionado=null;
        irPcVisible=false;
        mensajePc="";
    }

    private void actualizarLista(
            List<PokemonSpeciesData> catalogoCompleto,
            String busqueda
    ){
        List<PokemonSpeciesData> catalogo=
                catalogoCompleto==null
                        ?Collections.emptyList()
                        :catalogoCompleto;

        String filtro=
                normalizar(
                        busqueda
                );

        if(snapshot==ultimoSnapshot&&
                catalogo==ultimoCatalogo&&
                filtro.equals(
                        ultimaBusqueda
                )){

            return;
        }

        ultimoSnapshot=snapshot;
        ultimoCatalogo=catalogo;
        ultimaBusqueda=filtro;

        List<PokemonSpeciesData> nueva=
                new ArrayList<>();

        for(PokemonSpeciesData especie:
                catalogo){

            if(especie==null||
                    especie.getNombre()==null||
                    !snapshot.tiene(
                            especie.getNombre()
                    )){

                continue;
            }

            if(!filtro.isEmpty()){
                String nombre=
                        normalizar(
                                especie.getNombre()
                        );

                String numero=
                        String.valueOf(
                                especie.getNumeroDex()
                        );

                if(!nombre.contains(filtro)&&
                        !numero.contains(filtro)){

                    continue;
                }
            }

            nueva.add(
                    especie
            );
        }

        lista=
                Collections.unmodifiableList(
                        nueva
                );

        boolean seleccionValida=false;

        if(seleccionada!=null){
            for(PokemonSpeciesData especie:
                    lista){

                if(especie.getNombre()
                        .equalsIgnoreCase(
                                seleccionada.getNombre()
                        )){

                    seleccionada=especie;
                    seleccionValida=true;
                    break;
                }
            }
        }

        if(!seleccionValida){
            seleccionada=
                    lista.isEmpty()
                            ?null
                            :lista.get(0);

            scrollEjemplares=0;
        }

        actualizarEjemplares();
        limitarScrollEspecies();
    }

    private void actualizarEjemplares(){
        if(seleccionada==null){
            ejemplares=
                    Collections.emptyList();

            ejemplarSeleccionado=null;
            return;
        }

        List<OwnedPokemonData> datos=
                snapshot.getEjemplares(
                        seleccionada.getNombre()
                );

        ejemplares=
                datos==null
                        ?Collections.emptyList()
                        :datos;

        OwnedPokemonData anterior=
                ejemplarSeleccionado;

        ejemplarSeleccionado=null;

        if(anterior!=null&&
                anterior.getUuid()!=null){

            for(OwnedPokemonData dato:
                    ejemplares){

                if(dato!=null&&
                        anterior.getUuid()
                                .equals(
                                        dato.getUuid()
                                )){

                    ejemplarSeleccionado=dato;
                    break;
                }
            }
        }

        if(ejemplarSeleccionado==null&&
                !ejemplares.isEmpty()){

            ejemplarSeleccionado=
                    ejemplares.get(0);
        }

        limitarScrollEjemplares();
    }

    private void dibujarMetrica(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            String etiqueta,
            String valor
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+34,
                FONDO_CARD
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                34,
                BORDE
        );

        drawString(
                matrixStack,
                font,
                etiqueta,
                x+7,
                y+6,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                valor,
                x+7,
                y+19,
                TEXTO
        );
    }

    private void dibujarLista(
            MatrixStack matrixStack,
            int mouseX,
            int mouseY
    ){
        fill(
                matrixStack,
                listaX,
                listaY,
                listaX+listaW,
                listaY+listaH,
                FONDO_CARD
        );

        borde(
                matrixStack,
                listaX,
                listaY,
                listaW,
                listaH,
                BORDE
        );

        drawString(
                matrixStack,
                font,
                "ESPECIES OBTENIDAS",
                listaX+8,
                listaY+8,
                TEXTO_ACENTO
        );

        String cantidad=
                String.valueOf(
                        lista.size()
                );

        drawString(
                matrixStack,
                font,
                cantidad,
                listaX+listaW-10-font.width(cantidad),
                listaY+8,
                TEXTO_SECUNDARIO
        );

        int inicioY=listaY+27;
        int visibles=getEspeciesVisibles();

        limitarScrollEspecies();

        for(int fila=0;
            fila<visibles;
            fila++){

            int indice=
                    scrollEspecies+
                            fila;

            if(indice>=lista.size()){
                break;
            }

            PokemonSpeciesData especie=
                    lista.get(indice);

            int yy=
                    inicioY+
                            (fila*FILA_ESPECIE);

            boolean activa=
                    seleccionada!=null&&
                            especie.getNombre()
                                    .equalsIgnoreCase(
                                            seleccionada.getNombre()
                                    );

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            listaX+4,
                            yy-3,
                            listaW-8,
                            FILA_ESPECIE
                    );

            if(activa||hover){
                fill(
                        matrixStack,
                        listaX+4,
                        yy-3,
                        listaX+listaW-4,
                        yy+FILA_ESPECIE-3,
                        activa
                                ?FONDO_ACTIVO
                                :FONDO_HOVER
                );
            }

            if(activa){
                fill(
                        matrixStack,
                        listaX+4,
                        yy-3,
                        listaX+6,
                        yy+FILA_ESPECIE-3,
                        ACENTO
                );
            }

            NairaPokemonSpriteRenderer.dibujar(
                    matrixStack,
                    especie.getNombre(),
                    "",
                    listaX+9,
                    yy,
                    21
            );

            String numero=
                    String.format(
                            Locale.ROOT,
                            "#%04d",
                            especie.getNumeroDex()
                    );

            drawString(
                    matrixStack,
                    font,
                    numero,
                    listaX+35,
                    yy+6,
                    TEXTO_SECUNDARIO
            );

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            especie.getNombre(),
                            Math.max(
                                    55,
                                    listaW-135
                            )
                    ),
                    listaX+80,
                    yy+6,
                    activa
                            ?TEXTO_ACENTO
                            :TEXTO
            );

            String total=
                    "x"+
                            snapshot.getCantidad(
                                    especie.getNombre()
                            );

            drawString(
                    matrixStack,
                    font,
                    total,
                    listaX+listaW-10-font.width(total),
                    yy+6,
                    snapshot.getCantidadShiny(
                            especie.getNombre()
                    )>0
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );
        }

        if(lista.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    snapshot.getTotalPokemon()==0
                            ?"Colección vacía"
                            :"Sin resultados",
                    listaX+8,
                    listaY+31,
                    TEXTO_SECUNDARIO
            );
        }

        dibujarScrollbar(
                matrixStack,
                listaX+listaW-4,
                listaY+26,
                listaH-30,
                lista.size(),
                visibles,
                scrollEspecies
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

        if(seleccionada==null){
            drawString(
                    matrixStack,
                    font,
                    "DETALLE",
                    x+10,
                    y+9,
                    TEXTO_ACENTO
            );

            drawString(
                    matrixStack,
                    font,
                    snapshot.getTotalPokemon()==0
                            ?"Abre /pc para sincronizar tu colección."
                            :"Selecciona una especie.",
                    x+10,
                    y+31,
                    TEXTO_SECUNDARIO
            );

            return;
        }

        String nombre=
                seleccionada.getNombre();

        String numero=
                String.format(
                        Locale.ROOT,
                        "#%04d",
                        seleccionada.getNumeroDex()
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
                nombre.toUpperCase(
                        Locale.ROOT
                ),
                x+55,
                y+10,
                TEXTO
        );

        int spriteCaja=66;
        int spriteX=
                x+w-spriteCaja-12;
        int spriteY=y+12;

        fill(
                matrixStack,
                spriteX,
                spriteY,
                spriteX+spriteCaja,
                spriteY+spriteCaja,
                FONDO_NAV
        );

        borde(
                matrixStack,
                spriteX,
                spriteY,
                spriteCaja,
                spriteCaja,
                BORDE
        );

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                nombre,
                "",
                spriteX+7,
                spriteY+7,
                spriteCaja-14
        );

        int yy=y+34;

        dibujarDato(
                matrixStack,
                x+10,
                yy,
                "Obtenidos",
                String.valueOf(
                        snapshot.getCantidad(
                                nombre
                        )
                )
        );

        dibujarDato(
                matrixStack,
                x+126,
                yy,
                "Shiny",
                String.valueOf(
                        snapshot.getCantidadShiny(
                                nombre
                        )
                )
        );

        yy+=17;

        dibujarDato(
                matrixStack,
                x+10,
                yy,
                "PC",
                String.valueOf(
                        snapshot.getCantidadPC(
                                nombre
                        )
                )
        );

        dibujarDato(
                matrixStack,
                x+126,
                yy,
                "Equipo",
                String.valueOf(
                        snapshot.getCantidadEquipo(
                                nombre
                        )
                )
        );

        yy+=17;

        dibujarDato(
                matrixStack,
                x+10,
                yy,
                "Nivel máx.",
                String.valueOf(
                        snapshot.getNivelMaximo(
                                nombre
                        )
                )
        );

        yy+=20;

        String formas=
                formasTexto(
                        snapshot.getFormas(
                                nombre
                        )
                );

        drawString(
                matrixStack,
                font,
                "Formas:",
                x+10,
                yy,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        formas,
                        Math.max(
                                80,
                                w-68
                        )
                ),
                x+53,
                yy,
                TEXTO
        );

        yy+=20;

        fill(
                matrixStack,
                x+10,
                yy,
                x+w-10,
                yy+1,
                BORDE
        );

        yy+=9;

        drawString(
                matrixStack,
                font,
                "EJEMPLARES",
                x+10,
                yy,
                TEXTO_ACENTO
        );

        String totalEjemplares=
                String.valueOf(
                        ejemplares.size()
                );

        drawString(
                matrixStack,
                font,
                totalEjemplares,
                x+72,
                yy,
                TEXTO_SECUNDARIO
        );

        irPcVisible=
                ejemplarSeleccionado!=null&&
                        ejemplarSeleccionado.estaEnPC();

        if(irPcVisible){
            irPcX=x+w-irPcW-10;
            irPcY=yy-4;

            boolean hoverIrPc=
                    dentro(
                            mouseX,
                            mouseY,
                            irPcX,
                            irPcY,
                            irPcW,
                            irPcH
                    );

            fill(
                    matrixStack,
                    irPcX,
                    irPcY,
                    irPcX+irPcW,
                    irPcY+irPcH,
                    hoverIrPc
                            ?FONDO_HOVER
                            :FONDO_NAV
            );

            borde(
                    matrixStack,
                    irPcX,
                    irPcY,
                    irPcW,
                    irPcH,
                    hoverIrPc
                            ?ACENTO
                            :BORDE
            );

            String boton="IR AL PC";

            drawString(
                    matrixStack,
                    font,
                    boton,
                    irPcX+
                            (irPcW-font.width(boton))/2,
                    irPcY+5,
                    hoverIrPc
                            ?TEXTO_ACENTO
                            :TEXTO_SECUNDARIO
            );
        }

        if(!mensajePc.isEmpty()){
            int mensajeW=
                    Math.max(
                            60,
                            w-
                                    102-
                                    (irPcVisible
                                            ?irPcW+14
                                            :0)
                    );

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            mensajePc,
                            mensajeW
                    ),
                    x+92,
                    yy,
                    TEXTO_SECUNDARIO
            );
        }

        yy+=17;

        ejemplaresX=x+8;
        ejemplaresY=yy;
        ejemplaresW=w-16;
        ejemplaresH=
                Math.max(
                        30,
                        y+h-yy-8
                );

        int visibles=
                getEjemplaresVisibles();

        limitarScrollEjemplares();

        for(int fila=0;
            fila<visibles;
            fila++){

            int indice=
                    scrollEjemplares+
                            fila;

            if(indice>=ejemplares.size()){
                break;
            }

            OwnedPokemonData dato=
                    ejemplares.get(indice);

            int filaY=
                    ejemplaresY+
                            (fila*FILA_EJEMPLAR);

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            ejemplaresX,
                            filaY,
                            ejemplaresW-5,
                            FILA_EJEMPLAR-1
                    );

            boolean seleccionado=
                    ejemplarSeleccionado!=null&&
                            dato.getUuid()!=null&&
                            dato.getUuid()
                                    .equals(
                                            ejemplarSeleccionado.getUuid()
                                    );

            if(hover||seleccionado){
                fill(
                        matrixStack,
                        ejemplaresX,
                        filaY,
                        ejemplaresX+ejemplaresW-5,
                        filaY+FILA_EJEMPLAR-1,
                        seleccionado
                                ?FONDO_ACTIVO
                                :FONDO_HOVER
                );
            }

            if(seleccionado){
                fill(
                        matrixStack,
                        ejemplaresX,
                        filaY,
                        ejemplaresX+2,
                        filaY+FILA_EJEMPLAR-1,
                        ACENTO
                );
            }

            String nivel=
                    "Nv. "+
                            dato.getNivel();

            drawString(
                    matrixStack,
                    font,
                    nivel,
                    ejemplaresX+4,
                    filaY+6,
                    TEXTO
            );

            String forma=
                    dato.tieneForma()
                            ?PokemonTranslator.forma(
                            dato.getForma()
                    )
                            :"Base";

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            forma,
                            78
                    ),
                    ejemplaresX+54,
                    filaY+6,
                    TEXTO_SECUNDARIO
            );

            String ubicacion=
                    ubicacionTexto(
                            dato
                    );

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            ubicacion,
                            Math.max(
                                    60,
                                    ejemplaresW-190
                            )
                    ),
                    ejemplaresX+125,
                    filaY+6,
                    TEXTO_SECUNDARIO
            );

            if(dato.isShiny()){
                String shiny="Shiny";

                drawString(
                        matrixStack,
                        font,
                        shiny,
                        ejemplaresX+ejemplaresW-10-font.width(shiny),
                        filaY+6,
                        TEXTO_ACENTO
                );
            }
        }

        dibujarScrollbar(
                matrixStack,
                ejemplaresX+ejemplaresW-3,
                ejemplaresY,
                ejemplaresH,
                ejemplares.size(),
                visibles,
                scrollEjemplares
        );
    }

    private boolean abrirEnPc(
            OwnedPokemonData dato
    ){
        if(dato==null||
                !dato.estaEnPC()){

            return false;
        }

        int cajaVisible=
                dato.getCaja();

        int slotVisible=
                dato.getSlot();

        if(cajaVisible<=0||
                slotVisible<=0||
                slotVisible>30){

            mensajePc=
                    "Ubicación PC no disponible.";

            return true;
        }

        int cajaPixelmon=
                cajaVisible-1;

        int slotPixelmon=
                slotVisible-1;

        try{
            Minecraft.getInstance()
                    .setScreen(
                            new NairaPCNavigatorScreen(
                                    cajaPixelmon,
                                    slotPixelmon
                            )
                    );

            return true;

        }catch(Exception e){
            mensajePc=
                    "No se pudo abrir esa posición del PC.";

            return true;
        }
    }

    private static final class NairaPCNavigatorScreen
            extends PCScreen{

        private static final int COLOR_OBJETIVO=
                0xFF8B1E1E;
        private final int cajaObjetivo;
        private final int slotObjetivo;

        private NairaPCNavigatorScreen(
                int caja,
                int slot
        ){
            super(
                    new StoragePosition(
                            caja,
                            slot
                    )
            );

            this.cajaObjetivo=caja;
            this.slotObjetivo=slot;

            /*
             * PCScreen(StoragePosition) usa la posición también como
             * "selected", que en Pixelmon representa un Pokémon tomado
             * para moverlo. Conservamos boxNumber, pero limpiamos esa
             * selección para que NairaPixel solo navegue/resalte.
             */
            updateSelected(
                    null
            );
        }

        @Override
        public void init(){
            super.init();

            /*
             * El constructor de Pixelmon ya ejecutó updateBox() con
             * esta caja. Solo reafirmamos boxNumber tras init.
             */
            this.boxNumber=
                    cajaObjetivo;

            updateSelected(
                    null
            );
        }

        @Override
        public void render(
                MatrixStack matrixStack,
                int mouseX,
                int mouseY,
                float partialTicks
        ){
            super.render(
                    matrixStack,
                    mouseX,
                    mouseY,
                    partialTicks
            );

            if(this.boxNumber!=cajaObjetivo||
                    slotObjetivo<0||
                    slotObjetivo>=30){

                return;
            }

            int columna=
                    slotObjetivo%6;

            int fila=
                    slotObjetivo/6;

            int x=
                    this.pcLeft+
                            (columna*30);

            int y=
                    this.pcTop+
                            (fila*28);

            dibujarMarcadorObjetivo(
                    matrixStack,
                    x+2,
                    y+6,
                    26,
                    26
            );
        }

        private void dibujarMarcadorObjetivo(
                MatrixStack matrixStack,
                int x,
                int y,
                int w,
                int h
        ){
            int largo=6;

            // Esquinas superiores
            fill(
                    matrixStack,
                    x,
                    y,
                    x+largo,
                    y+1,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x,
                    y,
                    x+1,
                    y+largo,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x+w-largo,
                    y,
                    x+w,
                    y+1,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x+w-1,
                    y,
                    x+w,
                    y+largo,
                    COLOR_OBJETIVO
            );

            // Esquinas inferiores
            fill(
                    matrixStack,
                    x,
                    y+h-1,
                    x+largo,
                    y+h,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x,
                    y+h-largo,
                    x+1,
                    y+h,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x+w-largo,
                    y+h-1,
                    x+w,
                    y+h,
                    COLOR_OBJETIVO
            );

            fill(
                    matrixStack,
                    x+w-1,
                    y+h-largo,
                    x+w,
                    y+h,
                    COLOR_OBJETIVO
            );
        }
    }

    private String ubicacionTexto(
            OwnedPokemonData dato
    ){
        if(dato==null){
            return "-";
        }

        if(dato.estaEnEquipo()){
            return "Equipo · Slot "+
                    numeroVisible(
                            dato.getSlot()
                    );
        }

        if(dato.estaEnPC()){
            return "PC · Caja "+
                    numeroVisible(
                            dato.getCaja()
                    )+
                    " · Slot "+
                    numeroVisible(
                            dato.getSlot()
                    );
        }

        return "Ubicación desconocida";
    }

    private int numeroVisible(
            int numero
    ){
        return numero;
    }

    private String formasTexto(
            List<String> formas
    ){
        if(formas==null||
                formas.isEmpty()){

            return "Base";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String forma:
                formas){

            String texto=
                    forma==null||
                            forma.trim().isEmpty()||
                            forma.equalsIgnoreCase(
                                    "base"
                            )
                            ?"Base"
                            :PokemonTranslator.forma(
                            forma
                    );

            if(!visibles.contains(
                    texto
            )){
                visibles.add(
                        texto
                );
            }
        }

        return visibles.isEmpty()
                ?"Base"
                :String.join(
                " · ",
                visibles
        );
    }

    private void dibujarDato(
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
                valor,
                x+font.width(
                        etiqueta+":"
                )+5,
                y,
                TEXTO
        );
    }

    private int getEspeciesVisibles(){
        return Math.max(
                0,
                (listaH-34)/
                        FILA_ESPECIE
        );
    }

    private int getEjemplaresVisibles(){
        return Math.max(
                1,
                ejemplaresH/
                        FILA_EJEMPLAR
        );
    }

    private void limitarScrollEspecies(){
        int max=
                Math.max(
                        0,
                        lista.size()-
                                getEspeciesVisibles()
                );

        if(scrollEspecies<0){
            scrollEspecies=0;
        }

        if(scrollEspecies>max){
            scrollEspecies=max;
        }
    }

    private void limitarScrollEjemplares(){
        int max=
                Math.max(
                        0,
                        ejemplares.size()-
                                getEjemplaresVisibles()
                );

        if(scrollEjemplares<0){
            scrollEjemplares=0;
        }

        if(scrollEjemplares>max){
            scrollEjemplares=max;
        }
    }

    private void dibujarScrollbar(
            MatrixStack matrixStack,
            int x,
            int y,
            int h,
            int total,
            int visibles,
            int scroll
    ){
        if(total<=visibles||
                h<=0){

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
                Math.max(
                        0,
                        h-thumbH
                );

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
                font.width(
                        actual+sufijo
                )>maxAncho){

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
        if(valor==null){
            return "";
        }

        String texto=
                valor.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        StringBuilder out=
                new StringBuilder();

        for(int i=0;i<texto.length();i++){
            char c=texto.charAt(i);

            if(Character.isLetterOrDigit(c)){
                out.append(c);
            }
        }

        return out.toString();
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
}
