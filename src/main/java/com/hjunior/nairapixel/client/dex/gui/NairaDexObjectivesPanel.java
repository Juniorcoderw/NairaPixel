package com.hjunior.nairapixel.client.dex.gui;

import com.hjunior.nairapixel.client.dex.controller.NairaDexController;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService.Guardado;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService.Objetivo;
import com.hjunior.nairapixel.client.dex.render.NairaPokemonSpriteRenderer;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator.Evaluacion;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator.ResultadoRegla;
import com.hjunior.nairapixel.client.dex.state.NairaDexState;
import com.hjunior.nairapixel.client.legend.LegendaryEnvironmentReader;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class NairaDexObjectivesPanel extends AbstractGui {
    private static final int FONDO_CARD=0xE319202A;
    private static final int FONDO_NAV=0xF20A0E14;
    private static final int FONDO_HOVER=0xE326303C;
    private static final int BORDE=0xFF384653;
    private static final int SEPARADOR=0xFF2B3742;
    private static final int ACENTO=0xFF4FD7DF;

    private static final int TEXTO=0xFFF1F3F5;
    private static final int TEXTO_SECUNDARIO=0xFF9BA6B0;
    private static final int TEXTO_ACENTO=0xFF61DCE4;
    private static final int TEXTO_VERDE=0xFF73D39A;
    private static final int TEXTO_ROJO=0xFFE57373;
    private static final int TEXTO_AMARILLO=0xFFE0C26C;

    private static final int FILA=30;

    private final NairaDexObjectivesService service=
            NairaDexObjectivesService.get();

    private final NairaDexController controller=
            NairaDexController.get();

    private FontRenderer font;
    private List<Guardado> guardados=
            Collections.emptyList();

    private int activoVerX;
    private int activoVerY;
    private int activoVerW;
    private int activoVerH=18;

    private int activoQuitarX;
    private int activoQuitarY;
    private int activoQuitarW;
    private int activoQuitarH=18;

    private final int[] objetivoTabX=new int[3];
    private final int[] objetivoTabY=new int[3];
    private final int[] objetivoTabW=new int[3];
    private final int[] objetivoTabH=new int[3];

    private int hudToggleX;
    private int hudToggleY;
    private int hudToggleW=72;
    private int hudToggleH=18;

    private int sightToggleX;
    private int sightToggleY;
    private int sightToggleW=84;
    private int sightToggleH=18;

    private int listaX;
    private int listaY;
    private int listaW;
    private int listaH;
    private int scroll;

    public void render(
            MatrixStack matrixStack,
            FontRenderer font,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ){
        this.font=font;

        guardados=
                service.getGuardados();

        drawString(
                matrixStack,
                font,
                "OBJETIVOS",
                x,
                y,
                TEXTO_ACENTO
        );

        int cantidadActivos=
                service.getCantidadObjetivosActivos();

        String resumen=
                cantidadActivos>0
                        ?cantidadActivos+
                                (cantidadActivos==1
                                        ?" ACTIVO · "
                                        :" ACTIVOS · ")+
                                guardados.size()+
                                " GUARDADOS"
                        :guardados.size()+
                                " GUARDADOS";

        drawString(
                matrixStack,
                font,
                resumen,
                x+w-font.width(resumen),
                y,
                TEXTO_SECUNDARIO
        );

        int activoY=y+17;
        int activoH=
                service.tieneObjetivoActivo()
                        ?249
                        :92;

        dibujarObjetivoActivo(
                matrixStack,
                x,
                activoY,
                w,
                activoH,
                mouseX,
                mouseY
        );

        int tituloY=
                activoY+
                        activoH+
                        13;

        drawString(
                matrixStack,
                font,
                "GUARDADOS",
                x,
                tituloY,
                TEXTO_ACENTO
        );

        String cantidad=
                String.valueOf(
                        guardados.size()
                );

        drawString(
                matrixStack,
                font,
                cantidad,
                x+w-font.width(cantidad),
                tituloY,
                TEXTO_SECUNDARIO
        );

        listaX=x;
        listaY=tituloY+16;
        listaW=w;
        listaH=
                Math.max(
                        35,
                        y+h-listaY
                );

        dibujarGuardados(
                matrixStack,
                mouseX,
                mouseY
        );
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY
    ){
        Objetivo activo=
                service.getObjetivoActivo();

        if(activo!=null){
            List<Objetivo> activos=
                    service.getObjetivosActivos();

            for(int i=0;i<activos.size()&&i<3;i++){
                if(dentro(
                        mouseX,
                        mouseY,
                        objetivoTabX[i],
                        objetivoTabY[i],
                        objetivoTabW[i],
                        objetivoTabH[i]
                )){
                    Objetivo elegido=
                            activos.get(i);

                    service.hacerPrincipal(
                            elegido.getPokemon(),
                            elegido.getForma()
                    );

                    return true;
                }
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    activoVerX,
                    activoVerY,
                    activoVerW,
                    activoVerH
            )){
                abrirSpawn(
                        activo.getPokemon(),
                        activo.getForma()
                );

                return true;
            }

            if(dentro(
                    mouseX,
                    mouseY,
                    activoQuitarX,
                    activoQuitarY,
                    activoQuitarW,
                    activoQuitarH
            )){
                service.quitarObjetivo();
                return true;
            }
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

        int fila=
                ((int)mouseY-listaY)/
                        FILA;

        int indice=
                scroll+
                        fila;

        if(indice<0||
                indice>=guardados.size()){

            return true;
        }

        Guardado guardado=
                guardados.get(indice);

        int quitarW=48;
        int verW=58;
        int objetivoW=72;
        int gap=5;

        int quitarX=
                listaX+
                        listaW-
                        quitarW-
                        8;

        int verX=
                quitarX-
                        gap-
                        verW;

        int objetivoX=
                verX-
                        gap-
                        objetivoW;

        int botonY=
                listaY+
                        (fila*FILA)+
                        5;

        int botonH=20;

        if(dentro(
                mouseX,
                mouseY,
                quitarX,
                botonY,
                quitarW,
                botonH
        )){
            service.quitarGuardado(
                    guardado.getPokemon(),
                    guardado.getForma()
            );

            guardados=
                    service.getGuardados();

            limitarScroll();
            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                verX,
                botonY,
                verW,
                botonH
        )){
            abrirEnDex(
                    guardado.getPokemon(),
                    guardado.getForma()
            );

            return true;
        }

        if(dentro(
                mouseX,
                mouseY,
                objetivoX,
                botonY,
                objetivoW,
                botonH
        )){
            marcarGuardadoComoObjetivo(
                    guardado
            );

            return true;
        }

        abrirEnDex(
                guardado.getPokemon(),
                guardado.getForma()
        );

        return true;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ){
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

        if(delta>0){
            scroll-=3;
        }else if(delta<0){
            scroll+=3;
        }

        limitarScroll();

        return true;
    }

    public void resetScroll(){
        scroll=0;
    }

    private void dibujarObjetivoActivo(
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

        fill(
                matrixStack,
                x,
                y,
                x+2,
                y+h,
                ACENTO
        );

        drawString(
                matrixStack,
                font,
                "OBJETIVOS ACTIVOS "+
                        service.getCantidadObjetivosActivos()+
                        "/"+
                        NairaDexObjectivesService.MAX_OBJETIVOS,
                x+11,
                y+9,
                TEXTO_ACENTO
        );

        Objetivo objetivo=
                service.getObjetivoActivo();

        if(objetivo==null){
            drawString(
                    matrixStack,
                    font,
                    "Sin objetivo activo.",
                    x+11,
                    y+36,
                    TEXTO
            );

            drawString(
                    matrixStack,
                    font,
                    "Selecciona un Pokémon en el Dex y usa Marcar objetivo.",
                    x+11,
                    y+53,
                    TEXTO_SECUNDARIO
            );

            return;
        }

        int colorPrincipal=
                service.getColorObjetivo(
                        objetivo
                );

        fill(
                matrixStack,
                x,
                y,
                x+2,
                y+h,
                colorPrincipal
        );

        List<Objetivo> activos=
                service.getObjetivosActivos();

        int tabsX=x+11;
        int tabsY=y+27;
        int tabsGap=6;
        int tabsDisponibles=w-22;
        int tabW=
                Math.max(
                        72,
                        (tabsDisponibles-
                                tabsGap*2)/3
                );

        for(int i=0;i<3;i++){
            objetivoTabX[i]=0;
            objetivoTabY[i]=0;
            objetivoTabW[i]=0;
            objetivoTabH[i]=0;
        }

        for(int i=0;i<activos.size()&&i<3;i++){
            Objetivo item=
                    activos.get(i);

            int tx=
                    tabsX+
                            i*(tabW+tabsGap);

            int tw=
                    i==2
                            ?Math.max(
                                    50,
                                    x+w-11-tx
                            )
                            :tabW;

            int th=20;

            objetivoTabX[i]=tx;
            objetivoTabY[i]=tabsY;
            objetivoTabW[i]=tw;
            objetivoTabH[i]=th;

            int color=
                    service.getColorObjetivo(
                            item
                    );

            boolean principal=
                    service.esObjetivoPrincipal(
                            item.getPokemon(),
                            item.getForma()
                    );

            fill(
                    matrixStack,
                    tx,
                    tabsY,
                    tx+tw,
                    tabsY+th,
                    principal
                            ?0xE326303C
                            :FONDO_NAV
            );

            borde(
                    matrixStack,
                    tx,
                    tabsY,
                    tw,
                    th,
                    principal
                            ?color
                            :BORDE
            );

            fill(
                    matrixStack,
                    tx,
                    tabsY,
                    tx+2,
                    tabsY+th,
                    color
            );

            String etiqueta=
                    (principal
                            ?"● "
                            :"")+
                            item.getPokemon();

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            etiqueta,
                            tw-10
                    ),
                    tx+6,
                    tabsY+6,
                    principal
                            ?color
                            :TEXTO
            );
        }

        Evaluacion evaluacion=
                NairaDexSpawnEvaluator.evaluar(
                        objetivo.getSpawns()
                );

        ResultadoRegla mejor=
                mejorRegla(
                        evaluacion
                );

        int sprite=68;
        int spriteX=x+12;
        int spriteY=y+55;

        fill(
                matrixStack,
                spriteX,
                spriteY,
                spriteX+sprite,
                spriteY+sprite,
                FONDO_NAV
        );

        borde(
                matrixStack,
                spriteX,
                spriteY,
                sprite,
                sprite,
                BORDE
        );

        NairaPokemonSpriteRenderer.dibujar(
                matrixStack,
                objetivo.getPokemon(),
                objetivo.getForma(),
                spriteX+7,
                spriteY+7,
                sprite-14
        );

        int infoX=
                spriteX+
                        sprite+
                        14;

        drawString(
                matrixStack,
                font,
                objetivo.getPokemon()
                        .toUpperCase(
                                Locale.ROOT
                        ),
                infoX,
                y+58,
                TEXTO
        );

        String forma=
                objetivo.tieneForma()
                        ?PokemonTranslator.forma(
                                objetivo.getForma()
                        )
                        :"Base";

        drawString(
                matrixStack,
                font,
                "Forma: "+forma,
                infoX,
                y+74,
                TEXTO_SECUNDARIO
        );

        String estado;
        int colorEstado;

        if(!evaluacion.tieneReglas()){
            estado="SIN REGLAS DE SPAWN";
            colorEstado=TEXTO_SECUNDARIO;
        }else if(evaluacion.isCompatibleAhora()){
            estado="ZONA COMPATIBLE AHORA";
            colorEstado=TEXTO_VERDE;
        }else{
            estado="CONDICIONES INCOMPLETAS";
            colorEstado=TEXTO_ROJO;
        }

        drawString(
                matrixStack,
                font,
                estado,
                infoX,
                y+93,
                colorEstado
        );

        String reglas=
                evaluacion.getCantidadCompatibles()+
                        "/"+
                        evaluacion.getTotalReglas()+
                        " reglas compatibles";

        drawString(
                matrixStack,
                font,
                reglas,
                infoX,
                y+108,
                TEXTO_SECUNDARIO
        );

        int entornoX=
                Math.max(
                        infoX+165,
                        x+w-345
                );

        int entornoW=
                x+w-entornoX-11;

        if(entornoW>120){
            drawString(
                    matrixStack,
                    font,
                    "ENTORNO ACTUAL",
                    entornoX,
                    y+58,
                    TEXTO_ACENTO
            );

            drawString(
                    matrixStack,
                    font,
                    limitarTexto(
                            entornoActual(
                                    evaluacion
                            ),
                            entornoW
                    ),
                    entornoX,
                    y+74,
                    TEXTO
            );

            drawString(
                    matrixStack,
                    font,
                    "Datos del lugar donde estás ahora.",
                    entornoX,
                    y+91,
                    TEXTO_SECUNDARIO
            );
        }

        int separadorY=y+135;

        fill(
                matrixStack,
                x+11,
                separadorY,
                x+w-11,
                separadorY+1,
                SEPARADOR
        );

        if(mejor!=null){
            dibujarDiagnostico(
                    matrixStack,
                    mejor,
                    evaluacion,
                    x+11,
                    separadorY+11,
                    w-22
            );
        }else{
            drawString(
                    matrixStack,
                    font,
                    "No hay una regla de spawn disponible para analizar.",
                    x+11,
                    separadorY+18,
                    TEXTO_SECUNDARIO
            );
        }

        activoQuitarW=52;
        activoVerW=72;

        activoQuitarX=x+w-activoQuitarW-10;
        activoVerX=
                activoQuitarX-
                        activoVerW-
                        6;

        activoVerY=y+h-27;
        activoQuitarY=y+h-27;

        dibujarBoton(
                matrixStack,
                activoVerX,
                activoVerY,
                activoVerW,
                activoVerH,
                "VER SPAWN",
                dentro(
                        mouseX,
                        mouseY,
                        activoVerX,
                        activoVerY,
                        activoVerW,
                        activoVerH
                ),
                false
        );

        dibujarBoton(
                matrixStack,
                activoQuitarX,
                activoQuitarY,
                activoQuitarW,
                activoQuitarH,
                "QUITAR",
                dentro(
                        mouseX,
                        mouseY,
                        activoQuitarX,
                        activoQuitarY,
                        activoQuitarW,
                        activoQuitarH
                ),
                true
        );
    }

    private void dibujarDiagnostico(
            MatrixStack matrixStack,
            ResultadoRegla resultado,
            Evaluacion evaluacion,
            int x,
            int y,
            int w
    ){
        PokemonSpawnRule regla=
                resultado.getRegla();

        LegendaryEnvironmentReader.Estado entorno=
                evaluacion==null
                        ?null
                        :evaluacion.getEntorno();

        int indice=
                evaluacion==null
                        ?1
                        :evaluacion.getResultados()
                                .indexOf(
                                        resultado
                                )+
                                1;

        String origen=
                traducirOrigen(
                        regla.getOrigen()
                );

        drawString(
                matrixStack,
                font,
                "MEJOR REGLA",
                x,
                y,
                TEXTO_ACENTO
        );

        String reglaTexto=
                "Regla "+
                        Math.max(
                                1,
                                indice
                        )+
                        (origen.isEmpty()
                                ?""
                                :" · "+origen);

        drawString(
                matrixStack,
                font,
                reglaTexto,
                x+86,
                y,
                TEXTO_SECUNDARIO
        );

        int tablaY=y+18;
        int estadoW=12;
        int condicionW=76;
        int actualW=
                Math.max(
                        105,
                        (w-condicionW-estadoW-18)/2
                );

        int requiereX=
                x+
                        estadoW+
                        condicionW+
                        actualW+
                        12;

        drawString(
                matrixStack,
                font,
                "CONDICIÓN",
                x+estadoW,
                tablaY,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "ACTUAL",
                x+
                        estadoW+
                        condicionW,
                tablaY,
                TEXTO_SECUNDARIO
        );

        drawString(
                matrixStack,
                font,
                "REQUIERE",
                requiereX,
                tablaY,
                TEXTO_SECUNDARIO
        );

        int filaY=tablaY+14;
        int filas=0;

        if(regla.tieneHorarios()){
            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Horario",
                    evaluacion==null
                            ?"Sin datos"
                            :valorHoraActual(
                                    evaluacion.getHora()
                            ),
                    listaCompacta(
                            regla.getHorarios(),
                            TipoValor.HORARIO,
                            2
                    ),
                    resultado.isHorario()
            );
        }

        if(regla.tieneBiomas()){
            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Bioma",
                    entorno!=null&&
                            entorno.tieneBioma()
                            ?traducirId(
                                    entorno.getBioma()
                            )
                            :"Sin datos",
                    listaCompacta(
                            regla.getBiomas(),
                            TipoValor.ID,
                            2
                    ),
                    resultado.isBioma()
            );
        }

        if(regla.tieneMinY()||
                regla.tieneMaxY()){

            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Altura",
                    entorno==null
                            ?"Sin datos"
                            :"Y "+entorno.getY(),
                    rangoAltura(
                            regla
                    ),
                    resultado.isAltura()
            );
        }

        if(regla.tieneClima()){
            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Clima",
                    entorno!=null&&
                            entorno.tieneClima()
                            ?traducirClima(
                                    entorno.getClima()
                            )
                            :"Sin datos",
                    listaCompacta(
                            regla.getClimas(),
                            TipoValor.CLIMA,
                            2
                    ),
                    resultado.isClima()
            );
        }

        if(regla.tieneFaseLunar()){
            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Luna",
                    entorno!=null&&
                            entorno.tieneFaseLunar()
                            ?faseLunarNombre(
                                    entorno.getFaseLunar()
                            )
                            :"Sin datos",
                    faseLunar(
                            regla
                    ),
                    resultado.isLuna()
            );
        }

        if(regla.tieneBloquesBase()){
            filaCondicion(
                    matrixStack,
                    x,
                    filaY+(filas++*14),
                    w,
                    condicionW,
                    actualW,
                    "Suelo",
                    entorno!=null&&
                            entorno.tieneBloqueBase()
                            ?traducirId(
                                    entorno.getBloqueBase()
                            )
                            :"Sin datos",
                    listaCompacta(
                            regla.getBloquesBase(),
                            TipoValor.ID,
                            2
                    ),
                    resultado.isSuelo()
            );
        }

        if(filas==0){
            drawString(
                    matrixStack,
                    font,
                    "Esta regla no exige condiciones especiales.",
                    x,
                    filaY,
                    TEXTO_SECUNDARIO
            );
        }

        int faltaY=
                filaY+
                        Math.max(
                                1,
                                filas
                        )*
                                14+
                        7;

        fill(
                matrixStack,
                x,
                faltaY-5,
                x+w,
                faltaY-4,
                SEPARADOR
        );

        if(resultado.isCompatible()){
            drawString(
                    matrixStack,
                    font,
                    "LISTO",
                    x,
                    faltaY+4,
                    TEXTO_VERDE
            );

            drawString(
                    matrixStack,
                    font,
                    "Todas las condiciones coinciden. Esperando aparición.",
                    x+61,
                    faltaY+4,
                    TEXTO_VERDE
            );
        }else{
            drawString(
                    matrixStack,
                    font,
                    "TE FALTA",
                    x,
                    faltaY+4,
                    TEXTO_AMARILLO
            );

            List<String> faltantes=
                    faltantesCompactos(
                            resultado
                    );

            if(faltantes.isEmpty()){
                drawString(
                        matrixStack,
                        font,
                        "Revisa la regla de spawn.",
                        x+61,
                        faltaY+4,
                        TEXTO
                );
            }else{
                int maxLineas=
                        Math.min(
                                2,
                                faltantes.size()
                        );

                for(int i=0;
                        i<maxLineas;
                        i++){

                    String linea=
                            "• "+
                                    faltantes.get(i);

                    drawString(
                            matrixStack,
                            font,
                            limitarTexto(
                                    linea,
                                    Math.max(
                                            80,
                                            w-65
                                    )
                            ),
                            x+61,
                            faltaY+4+(i*13),
                            TEXTO
                    );
                }

                if(faltantes.size()>2){
                    String extra=
                            "+"+
                                    (faltantes.size()-2)+
                                    " condición"+
                                    (faltantes.size()-2==1
                                            ?""
                                            :"es")+
                                    " más · Ver Spawn";

                    drawString(
                            matrixStack,
                            font,
                            extra,
                            x+61,
                            faltaY+30,
                            TEXTO_SECUNDARIO
                    );
                }
            }
        }
    }

    private void filaCondicion(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int condicionW,
            int actualW,
            String condicion,
            String actual,
            String requiere,
            boolean cumple
    ){
        int estadoW=12;

        drawString(
                matrixStack,
                font,
                cumple
                        ?"✓"
                        :"✕",
                x,
                y,
                cumple
                        ?TEXTO_VERDE
                        :TEXTO_ROJO
        );

        drawString(
                matrixStack,
                font,
                condicion,
                x+estadoW,
                y,
                TEXTO_SECUNDARIO
        );

        int actualX=
                x+
                        estadoW+
                        condicionW;

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        actual,
                        actualW-8
                ),
                actualX,
                y,
                cumple
                        ?TEXTO
                        :TEXTO_ROJO
        );

        int requiereX=
                actualX+
                        actualW+
                        12;

        int restante=
                Math.max(
                        30,
                        x+w-requiereX
                );

        drawString(
                matrixStack,
                font,
                limitarTexto(
                        requiere,
                        restante
                ),
                requiereX,
                y,
                TEXTO
        );
    }

    private String valorHoraActual(
            String hora
    ){
        if(hora==null||
                hora.trim().isEmpty()||
                hora.equals("--:--")){

            return "Sin datos";
        }

        return hora;
    }

    private ResultadoRegla mejorRegla(
            Evaluacion evaluacion
    ){
        if(evaluacion==null||
                evaluacion.getResultados()==null||
                evaluacion.getResultados().isEmpty()){

            return null;
        }

        ResultadoRegla mejor=null;
        int menorFallos=Integer.MAX_VALUE;

        for(ResultadoRegla resultado:
                evaluacion.getResultados()){

            if(resultado==null){
                continue;
            }

            int fallos=
                    contarFallos(
                            resultado
                    );

            if(mejor==null||
                    fallos<menorFallos){

                mejor=resultado;
                menorFallos=fallos;
            }

            if(fallos==0){
                break;
            }
        }

        return mejor;
    }

    private int contarFallos(
            ResultadoRegla resultado
    ){
        if(resultado==null){
            return Integer.MAX_VALUE;
        }

        int fallos=0;

        if(!resultado.isHorario())fallos++;
        if(!resultado.isBioma())fallos++;
        if(!resultado.isClima())fallos++;
        if(!resultado.isAltura())fallos++;
        if(!resultado.isLuna())fallos++;
        if(!resultado.isSuelo())fallos++;

        return fallos;
    }

    private List<String> faltantesCompactos(
            ResultadoRegla resultado
    ){
        List<String> faltan=
                new ArrayList<>();

        if(resultado==null||
                resultado.isCompatible()){

            return faltan;
        }

        PokemonSpawnRule regla=
                resultado.getRegla();

        if(!resultado.isHorario()){
            faltan.add(
                    "Horario: "+
                            listaCompacta(
                                    regla.getHorarios(),
                                    TipoValor.HORARIO,
                                    2
                            )
            );
        }

        if(!resultado.isBioma()){
            faltan.add(
                    "Bioma: "+
                            listaCompacta(
                                    regla.getBiomas(),
                                    TipoValor.ID,
                                    2
                            )
            );
        }

        if(!resultado.isClima()){
            faltan.add(
                    "Clima: "+
                            listaCompacta(
                                    regla.getClimas(),
                                    TipoValor.CLIMA,
                                    2
                            )
            );
        }

        if(!resultado.isAltura()){
            faltan.add(
                    "Altura: "+
                            rangoAltura(
                                    regla
                            )
            );
        }

        if(!resultado.isLuna()){
            faltan.add(
                    "Luna: "+
                            faseLunar(
                                    regla
                            )
            );
        }

        if(!resultado.isSuelo()){
            faltan.add(
                    "Suelo: "+
                            listaCompacta(
                                    regla.getBloquesBase(),
                                    TipoValor.ID,
                                    2
                            )
            );
        }

        return faltan;
    }

    private String entornoActual(
            Evaluacion evaluacion
    ){
        if(evaluacion==null||
                evaluacion.getEntorno()==null){

            return "Sin datos";
        }

        LegendaryEnvironmentReader.Estado entorno=
                evaluacion.getEntorno();

        List<String> partes=
                new ArrayList<>();

        if(entorno.tieneBioma()){
            partes.add(
                    traducirId(
                            entorno.getBioma()
                    )
            );
        }

        partes.add(
                "Y "+
                        entorno.getY()
        );

        if(entorno.tieneClima()){
            partes.add(
                    traducirClima(
                            entorno.getClima()
                    )
            );
        }

        if(evaluacion.getHora()!=null&&
                !evaluacion.getHora()
                        .equals("--:--")){

            partes.add(
                    evaluacion.getHora()
            );
        }

        if(entorno.tieneFaseLunar()){
            partes.add(
                    faseLunarNombre(
                            entorno.getFaseLunar()
                    )
            );
        }

        if(entorno.tieneBloqueBase()){
            partes.add(
                    "Suelo "+
                            traducirId(
                                    entorno.getBloqueBase()
                            )
            );
        }

        return String.join(
                " · ",
                partes
        );
    }

    private enum TipoValor{
        HORARIO,
        CLIMA,
        ID
    }

    private String lista(
            List<String> valores,
            TipoValor tipo
    ){
        if(valores==null||
                valores.isEmpty()){

            return "Cualquiera";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String valor:
                valores){

            if(valor==null||
                    valor.trim().isEmpty()){

                continue;
            }

            String traducido;

            if(tipo==TipoValor.HORARIO){
                traducido=
                        traducirHorario(
                                valor
                        );
            }else if(tipo==TipoValor.CLIMA){
                traducido=
                        traducirClima(
                                valor
                        );
            }else{
                traducido=
                        traducirId(
                                valor
                        );
            }

            if(!traducido.isEmpty()){
                visibles.add(
                        traducido
                );
            }
        }

        return visibles.isEmpty()
                ?"Cualquiera"
                :String.join(
                        " / ",
                        visibles
                );
    }

    private String listaCompacta(
            List<String> valores,
            TipoValor tipo,
            int maxElementos
    ){
        if(valores==null||
                valores.isEmpty()){

            return "Cualquiera";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String valor:
                valores){

            if(valor==null||
                    valor.trim().isEmpty()){

                continue;
            }

            String traducido;

            if(tipo==TipoValor.HORARIO){
                traducido=
                        traducirHorario(
                                valor
                        );
            }else if(tipo==TipoValor.CLIMA){
                traducido=
                        traducirClima(
                                valor
                        );
            }else{
                traducido=
                        traducirId(
                                valor
                        );
            }

            if(!traducido.isEmpty()&&
                    !visibles.contains(
                            traducido
                    )){

                visibles.add(
                        traducido
                );
            }
        }

        if(visibles.isEmpty()){
            return "Cualquiera";
        }

        int limite=
                Math.max(
                        1,
                        Math.min(
                                maxElementos,
                                visibles.size()
                        )
                );

        String base=
                String.join(
                        " / ",
                        visibles.subList(
                                0,
                                limite
                        )
                );

        int restantes=
                visibles.size()-
                        limite;

        return restantes>0
                ?base+" +"+restantes
                :base;
    }

    private String rangoAltura(
            PokemonSpawnRule regla
    ){
        Integer min=
                regla.getMinY();

        Integer max=
                regla.getMaxY();

        if(min==null&&
                max==null){

            return "Cualquiera";
        }

        if(min!=null&&
                max!=null){

            return "Y "+
                    min+
                    "-"+
                    max;
        }

        if(min!=null){
            return "Y >= "+
                    min;
        }

        return "Y <= "+
                max;
    }

    private String faseLunar(
            PokemonSpawnRule regla
    ){
        Integer fase=
                regla.getFaseLunar();

        return fase==null
                ?"Cualquiera"
                :faseLunarNombre(
                        fase
                );
    }

    private String faseLunarNombre(
            int fase
    ){
        switch(fase){
            case 0:
                return "Luna llena";
            case 1:
                return "Gibosa menguante";
            case 2:
                return "Cuarto menguante";
            case 3:
                return "Menguante";
            case 4:
                return "Luna nueva";
            case 5:
                return "Creciente";
            case 6:
                return "Cuarto creciente";
            case 7:
                return "Gibosa creciente";
            default:
                return "Fase "+fase;
        }
    }

    private String traducirOrigen(
            String origen
    ){
        String key=
                normalizar(
                        origen
                );

        if(key.contains("standard")){
            return "Estándar";
        }

        if(key.contains("legendary")||
                key.contains("legendaries")){

            return "Legendario";
        }

        if(origen==null||
                origen.trim().isEmpty()){

            return "";
        }

        return PokemonTranslator.formatear(
                origen
        );
    }

    private String traducirHorario(
            String horario
    ){
        String key=
                normalizar(
                        horario
                );

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

        return PokemonTranslator.formatear(
                horario
        );
    }

    private String traducirClima(
            String clima
    ){
        String key=
                normalizar(
                        clima
                );

        if(key.equals("clear")){
            return "Despejado";
        }

        if(key.equals("rain")||
                key.equals("raining")){

            return "Lluvia";
        }

        if(key.equals("thunder")||
                key.equals("thunderstorm")||
                key.equals("storm")){

            return "Tormenta";
        }

        if(clima==null||
                clima.trim().isEmpty()){

            return "Cualquiera";
        }

        return PokemonTranslator.formatear(
                clima
        );
    }

    private String traducirId(
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

        if(limpio.isEmpty()){
            return "";
        }

        String[] partes=
                limpio.toLowerCase(
                        Locale.ROOT
                ).split("\\s+");

        StringBuilder out=
                new StringBuilder();

        for(String parte:
                partes){

            if(parte.isEmpty()){
                continue;
            }

            if(out.length()>0){
                out.append(' ');
            }

            out.append(
                    Character.toUpperCase(
                            parte.charAt(0)
                    )
            );

            if(parte.length()>1){
                out.append(
                        parte.substring(1)
                );
            }
        }

        return out.toString();
    }

    private String normalizar(
            String valor
    ){
        if(valor==null){
            return "";
        }

        return valor.trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private void dibujarGuardados(
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

        if(guardados.isEmpty()){
            drawString(
                    matrixStack,
                    font,
                    "Aún no guardaste Pokémon.",
                    listaX+10,
                    listaY+12,
                    TEXTO_SECUNDARIO
            );

            return;
        }

        limitarScroll();

        int visibles=
                getVisibles();

        for(int fila=0;
                fila<visibles;
                fila++){

            int indice=
                    scroll+
                            fila;

            if(indice>=guardados.size()){
                break;
            }

            Guardado guardado=
                    guardados.get(indice);

            int yy=
                    listaY+
                            (fila*FILA);

            boolean hover=
                    dentro(
                            mouseX,
                            mouseY,
                            listaX+4,
                            yy+2,
                            listaW-8,
                            FILA-4
                    );

            if(hover){
                fill(
                        matrixStack,
                        listaX+4,
                        yy+2,
                        listaX+listaW-4,
                        yy+FILA-2,
                        FONDO_HOVER
                );
            }

            NairaPokemonSpriteRenderer.dibujar(
                    matrixStack,
                    guardado.getPokemon(),
                    guardado.getForma(),
                    listaX+9,
                    yy+5,
                    20
            );

            drawString(
                    matrixStack,
                    font,
                    guardado.getPokemon(),
                    listaX+38,
                    yy+7,
                    TEXTO
            );

            if(guardado.tieneForma()){
                String forma=
                        PokemonTranslator.forma(
                                guardado.getForma()
                        );

                drawString(
                        matrixStack,
                        font,
                        limitarTexto(
                                forma,
                                Math.max(
                                        60,
                                        listaW-205
                                )
                        ),
                        listaX+125,
                        yy+7,
                        TEXTO_SECUNDARIO
                );
            }

            boolean esActivo=
                    service.esObjetivoActivo(
                            guardado.getPokemon(),
                            guardado.getForma()
                    );

            int quitarW=48;
            int verW=58;
            int objetivoW=72;
            int gap=5;

            int quitarX=
                    listaX+
                            listaW-
                            quitarW-
                            8;

            int verX=
                    quitarX-
                            gap-
                            verW;

            int objetivoX=
                    verX-
                            gap-
                            objetivoW;

            int botonY=yy+5;
            int botonH=20;

            boolean esPrincipal=
                    service.esObjetivoPrincipal(
                            guardado.getPokemon(),
                            guardado.getForma()
                    );

            String textoObjetivo=
                    esPrincipal
                            ?"PRINCIPAL"
                            :(esActivo
                                    ?"ACTIVO"
                                    :(service.puedeAgregarObjetivo()
                                            ?"AÑADIR"
                                            :"LÍMITE 3"));

            dibujarBotonGuardado(
                    matrixStack,
                    objetivoX,
                    botonY,
                    objetivoW,
                    botonH,
                    textoObjetivo,
                    dentro(
                            mouseX,
                            mouseY,
                            objetivoX,
                            botonY,
                            objetivoW,
                            botonH
                    ),
                    esActivo,
                    false
            );

            dibujarBotonGuardado(
                    matrixStack,
                    verX,
                    botonY,
                    verW,
                    botonH,
                    "VER DEX",
                    dentro(
                            mouseX,
                            mouseY,
                            verX,
                            botonY,
                            verW,
                            botonH
                    ),
                    false,
                    false
            );

            dibujarBotonGuardado(
                    matrixStack,
                    quitarX,
                    botonY,
                    quitarW,
                    botonH,
                    "QUITAR",
                    dentro(
                            mouseX,
                            mouseY,
                            quitarX,
                            botonY,
                            quitarW,
                            botonH
                    ),
                    false,
                    true
            );
        }

        dibujarScrollbar(
                matrixStack,
                listaX+listaW-3,
                listaY+4,
                listaH-8,
                guardados.size(),
                visibles,
                scroll
        );
    }

    private void marcarGuardadoComoObjetivo(
            Guardado guardado
    ){
        if(guardado==null){
            return;
        }

        if(service.esObjetivoActivo(
                guardado.getPokemon(),
                guardado.getForma()
        )){
            service.hacerPrincipal(
                    guardado.getPokemon(),
                    guardado.getForma()
            );

            return;
        }

        if(!service.puedeAgregarObjetivo()){
            return;
        }

        controller.seleccionarPokemon(
                guardado.getPokemon(),
                guardado.getForma()
        );

        service.marcarObjetivo(
                guardado.getPokemon(),
                guardado.getForma(),
                controller.getSpawnsActuales()
        );

        controller.seleccionarSeccion(
                NairaDexState.Seccion.OBJETIVOS
        );
    }

    private void dibujarBotonGuardado(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            String texto,
            boolean hover,
            boolean activo,
            boolean peligro
    ){
        int fondo=
                activo
                        ?0xE324382D
                        :(hover
                                ?FONDO_HOVER
                                :FONDO_NAV);

        int bordeColor=
                peligro
                        ?TEXTO_ROJO
                        :(activo
                                ?TEXTO_VERDE
                                :(hover
                                        ?ACENTO
                                        :BORDE));

        int textoColor=
                peligro
                        ?TEXTO_ROJO
                        :(activo
                                ?TEXTO_VERDE
                                :(hover
                                        ?TEXTO_ACENTO
                                        :TEXTO_SECUNDARIO));

        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                fondo
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                bordeColor
        );

        drawString(
                matrixStack,
                font,
                texto,
                x+
                        (w-font.width(texto))/2,
                y+6,
                textoColor
        );
    }

    private void abrirEnDex(
            String pokemon,
            String forma
    ){
        controller.seleccionarPokemon(
                pokemon,
                forma
        );

        controller.seleccionarPestana(
                NairaDexState.Pestana.GENERAL
        );

        controller.seleccionarSeccion(
                NairaDexState.Seccion.DEX
        );
    }

    private void abrirSpawn(
            String pokemon,
            String forma
    ){
        controller.seleccionarPokemon(
                pokemon,
                forma
        );

        controller.seleccionarPestana(
                NairaDexState.Pestana.SPAWN
        );

        controller.seleccionarSeccion(
                NairaDexState.Seccion.DEX
        );
    }

    private void dibujarBoton(
            MatrixStack matrixStack,
            int x,
            int y,
            int w,
            int h,
            String texto,
            boolean hover,
            boolean peligro
    ){
        fill(
                matrixStack,
                x,
                y,
                x+w,
                y+h,
                hover
                        ?FONDO_HOVER
                        :FONDO_NAV
        );

        borde(
                matrixStack,
                x,
                y,
                w,
                h,
                hover
                        ?(peligro
                                ?TEXTO_ROJO
                                :ACENTO)
                        :BORDE
        );

        int color=
                peligro
                        ?TEXTO_ROJO
                        :(hover
                                ?TEXTO_ACENTO
                                :TEXTO_SECUNDARIO);

        drawString(
                matrixStack,
                font,
                texto,
                x+
                        (w-font.width(texto))/2,
                y+5,
                color
        );
    }

    private int getVisibles(){
        return Math.max(
                1,
                listaH/FILA
        );
    }

    private void limitarScroll(){
        int max=
                Math.max(
                        0,
                        guardados.size()-
                                getVisibles()
                );

        if(scroll<0){
            scroll=0;
        }

        if(scroll>max){
            scroll=max;
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
