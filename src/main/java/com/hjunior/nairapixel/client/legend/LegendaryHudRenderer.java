package com.hjunior.nairapixel.client.legend;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(
        modid="nairapixel",
        value=Dist.CLIENT
)
public final class LegendaryHudRenderer {
    private static final int ANCHO_COMPACTO=202;
    private static final int ALTO_COMPACTO=40;

    private static final int ANCHO_EXPANDIDO=220;
    private static final int ALTO_RESUMEN=142;
    private static final int ALTO_LISTA=178;
    private static final int FILAS_VISIBLES=8;

    private static final int ANCHO_RESULTADO=208;
    private static final int ALTO_VERIFICANDO=82;
    private static final int ALTO_SPAWN=98;
    private static final int ALTO_SIN_SPAWN=82;

    private static final int CYAN=0xFF72E8F6;
    private static final int CYAN_SUAVE=0xFF9AF1F8;
    private static final int CYAN_OSCURO=0xFF2D8794;
    private static final int TEXTO=0xFFE7EDF2;
    private static final int SECUNDARIO=0xFF8E9CAA;

    private static final int LOCAL=0xFF69F0B8;
    private static final int LOCAL_TEXTO=0xFFA8FFDA;

    private static final int DORADO=0xFFFFD45C;
    private static final int CORAL=0xFFFF706A;
    private static final int AZUL_RESULTADO=0xFF79D3F5;

    private static final int FONDO_SUPERIOR=0xD80B131B;
    private static final int FONDO_INFERIOR=0xD8101821;
    private static final int BORDE=0xA7369CAB;
    private static final int SEPARADOR=0x553B6872;

    private static int ultimoMinuto=-1;
    private static long totalCicloTicks=-1L;

    private static final List<LineaCandidato> cacheTodos=
            new ArrayList<>();

    private static final List<FilaLista> filasLista=
            new ArrayList<>();

    private static String cacheNota="";
    private static long cacheActualizadoMs=0L;
    private static long cacheObjetivo=-1L;
    private static boolean cacheContextual=false;

    private static boolean modoLista=false;
    private static int desplazamientoLista=0;

    private LegendaryHudRenderer(){}

    @SubscribeEvent
    public static void onRender(
            RenderGameOverlayEvent.Post event
    ){
        if(event.getType()!=
                RenderGameOverlayEvent.ElementType.ALL){
            return;
        }

        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null||
                mc.player==null||
                mc.options.hideGui){
            return;
        }

        int minutos=
                LegendaryScoreboardReader
                        .leerMinutos();

        if(minutos<0){
            reiniciar();
            return;
        }

        LegendarySpawnClock.actualizar(
                minutos
        );

        if(!LegendarySpawnClock
                .tieneObjetivo()){
            return;
        }

        long objetivo=
                LegendarySpawnClock
                        .getObjetivoTicks();

        LegendaryWatcher.Resultado resultado=
                LegendaryWatcher
                        .getResultado();

        if(resultado!=
                LegendaryWatcher.Resultado.NINGUNO){

            dibujarResultado(
                    event.getMatrixStack(),
                    mc,
                    minutos,
                    resultado
            );

            return;
        }

        boolean expandido=
                LegendaryKeyHandler
                        .isExpandido();

        if(!expandido){
            modoLista=false;
            desplazamientoLista=0;
        }else{
            actualizarCandidatos(
                    mc,
                    minutos,
                    objetivo
            );
        }

        dibujar(
                event.getMatrixStack(),
                mc,
                minutos,
                objetivo,
                calcularProgreso(
                        mc,
                        minutos,
                        objetivo
                ),
                expandido
        );
    }

    public static boolean procesarScrollLista(
            int direccion
    ){
        if(!LegendaryKeyHandler
                .isExpandido()||
                filasLista.isEmpty()){
            return false;
        }

        if(!modoLista){
            if(direccion>0){
                modoLista=true;
                desplazamientoLista=0;
                return true;
            }

            return false;
        }

        if(direccion<0){
            if(desplazamientoLista>0){
                desplazamientoLista--;
            }else{
                modoLista=false;
            }

            return true;
        }

        int maximo=
                Math.max(
                        0,
                        filasLista.size()-
                                FILAS_VISIBLES
                );

        if(desplazamientoLista<maximo){
            desplazamientoLista++;
        }

        return true;
    }

    private static void actualizarCandidatos(
            Minecraft mc,
            int minutos,
            long objetivo
    ){
        boolean contextual=
                minutos<=1;

        long ahora=
                System.currentTimeMillis();

        long intervalo=
                contextual
                        ?1000L
                        :4000L;

        boolean mismoObjetivo=
                cacheObjetivo>=0L&&
                        Math.abs(
                                objetivo-cacheObjetivo
                        )<=40L;

        if(cacheContextual==contextual&&
                mismoObjetivo&&
                ahora-cacheActualizadoMs<
                        intervalo){
            return;
        }

        cacheTodos.clear();

        if(contextual){
            cargarContextuales(
                    mc,
                    objetivo
            );
        }else{
            cargarTemporales(
                    objetivo
            );
        }

        reconstruirLista();

        cacheContextual=contextual;
        cacheObjetivo=objetivo;
        cacheActualizadoMs=ahora;

        int maximo=
                Math.max(
                        0,
                        filasLista.size()-
                                FILAS_VISIBLES
                );

        if(desplazamientoLista>maximo){
            desplazamientoLista=maximo;
        }
    }

    private static void cargarTemporales(
            long objetivo
    ){
        List<LegendaryForecastService.Candidate>
                candidatos=
                LegendaryForecastService.predecir(
                        objetivo,
                        LegendarySpawnClock
                                .isSincronizado()
                );

        for(LegendaryForecastService.Candidate
                candidato:candidatos){

            cacheTodos.add(
                    new LineaCandidato(
                            candidato.getNombre(),
                            limpiarBioma(
                                    candidato.getBioma()
                            ),
                            candidato.getCondicion(),
                            false
                    )
            );
        }

        cacheNota=
                LegendarySpawnClock
                        .isSincronizado()
                        ?"Horario sincronizado"
                        :"Horario estimado";
    }

    private static void cargarContextuales(
            Minecraft mc,
            long objetivo
    ){
        MinecraftTimeReader.VentanaTiempo ventana=
                crearVentanaObjetivo(
                        mc,
                        objetivo
                );

        List<LegendaryPredictor.Candidate>
                candidatos=
                LegendaryPredictor.predecir(
                        ventana
                );

        for(LegendaryPredictor.Candidate
                candidato:candidatos){

            cacheTodos.add(
                    new LineaCandidato(
                            candidato.getNombre(),
                            limpiarBioma(
                                    candidato.getBioma()
                            ),
                            candidato.getCondicion(),
                            candidato
                                    .isCoincidenciaLocal()
                    )
            );
        }

        LegendaryEnvironmentReader.Estado entorno=
                LegendaryEnvironmentReader
                        .leer();

        String bioma=
                formatearBioma(
                        entorno.getBioma()
                );

        if(bioma.isEmpty()){
            bioma="Desconocido";
        }

        String clima=
                formatearClima(
                        entorno.getClima()
                );

        cacheNota=
                bioma+
                        " · Y"+
                        entorno.getY()+
                        (clima.isEmpty()
                                ?""
                                :" · "+clima);
    }

    private static MinecraftTimeReader.VentanaTiempo
    crearVentanaObjetivo(
            Minecraft mc,
            long objetivo
    ){
        long ahora=
                mc.level.getDayTime();

        long delta=
                Math.max(
                        0L,
                        objetivo-ahora
                );

        int segundos=
                (int)Math.round(
                        delta/
                                20.0
                );

        int margen=
                LegendarySpawnClock
                        .isSincronizado()
                        ?5
                        :30;

        return MinecraftTimeReader
                .calcularVentana(
                        Math.max(
                                0,
                                segundos-margen
                        ),
                        segundos+margen
                );
    }

    private static void reconstruirLista(){
        filasLista.clear();

        Map<String,List<LineaCandidato>> grupos=
                new LinkedHashMap<>();

        for(LineaCandidato candidato:
                cacheTodos){

            List<String> biomas=
                    separarBiomas(
                            candidato.bioma
                    );

            for(String bioma:biomas){
                List<LineaCandidato> grupo=
                        grupos.get(bioma);

                if(grupo==null){
                    grupo=new ArrayList<>();

                    grupos.put(
                            bioma,
                            grupo
                    );
                }

                if(!grupo.contains(candidato)){
                    grupo.add(candidato);
                }
            }
        }

        for(Map.Entry<String,List<LineaCandidato>>
                entrada:grupos.entrySet()){

            String titulo=
                    entrada.getKey()
                            .toUpperCase(
                                    Locale.ROOT
                            )+
                            " · "+
                            entrada.getValue()
                                    .size();

            filasLista.add(
                    FilaLista.grupo(
                            titulo
                    )
            );

            for(LineaCandidato candidato:
                    entrada.getValue()){

                filasLista.add(
                        FilaLista.candidato(
                                candidato
                        )
                );
            }
        }
    }

    private static List<String> separarBiomas(
            String texto
    ){
        List<String> resultado=
                new ArrayList<>();

        if(texto==null||
                texto.trim().isEmpty()){

            resultado.add(
                    "Otros"
            );

            return resultado;
        }

        String[] partes=
                texto.split(
                        "\\s*/\\s*"
                );

        for(String parte:partes){
            String limpio=
                    parte==null
                            ?""
                            :parte.trim();

            if(!limpio.isEmpty()&&
                    !resultado.contains(limpio)){

                resultado.add(
                        limpio
                );
            }
        }

        if(resultado.isEmpty()){
            resultado.add(
                    "Otros"
            );
        }

        return resultado;
    }

    private static void dibujar(
            MatrixStack stack,
            Minecraft mc,
            int minutos,
            long objetivo,
            double progreso,
            boolean expandido
    ){
        FontRenderer font=
                mc.font;

        int ancho=
                expandido
                        ?ANCHO_EXPANDIDO
                        :ANCHO_COMPACTO;

        int alto;

        if(!expandido){
            alto=
                    ALTO_COMPACTO;
        }else if(modoLista){
            alto=
                    ALTO_LISTA;
        }else{
            alto=
                    ALTO_RESUMEN;
        }

        int pantalla=
                mc.getWindow()
                        .getGuiScaledWidth();

        int x=
                pantalla/2-
                        ancho/2;

        int y=7;

        dibujarFondo(
                stack,
                x,
                y,
                ancho,
                alto
        );

        dibujarCabecera(
                stack,
                font,
                x,
                y,
                ancho,
                minutos,
                objetivo
        );

        if(expandido){

            if(modoLista){

                dibujarLista(
                        stack,
                        font,
                        x,
                        y,
                        ancho
                );

            }else{

                dibujarResumen(
                        stack,
                        font,
                        x,
                        y,
                        ancho,
                        minutos
                );
            }
        }

        dibujarPie(
                stack,
                font,
                x,
                y,
                ancho,
                alto,
                progreso,
                minutos
        );
    }

    private static void dibujarResultado(
            MatrixStack stack,
            Minecraft mc,
            int minutos,
            LegendaryWatcher.Resultado resultado
    ){
        FontRenderer font=
                mc.font;

        int ancho=
                ANCHO_RESULTADO;

        int alto;

        if(resultado==
                LegendaryWatcher.Resultado.SPAWN){

            alto=
                    ALTO_SPAWN;

        }else if(resultado==
                LegendaryWatcher.Resultado.SIN_SPAWN){

            alto=
                    ALTO_SIN_SPAWN;

        }else{

            alto=
                    ALTO_VERIFICANDO;
        }

        int pantalla=
                mc.getWindow()
                        .getGuiScaledWidth();

        int x=
                pantalla/2-
                        ancho/2;

        int y=7;

        dibujarFondo(
                stack,
                x,
                y,
                ancho,
                alto
        );

        dibujarCabeceraResultado(
                stack,
                font,
                x,
                y,
                ancho,
                minutos,
                resultado
        );

        if(resultado==
                LegendaryWatcher.Resultado.SPAWN){

            dibujarSpawnConfirmado(
                    stack,
                    font,
                    x,
                    y,
                    ancho
            );

        }else if(resultado==
                LegendaryWatcher.Resultado.SIN_SPAWN){

            dibujarSinSpawn(
                    stack,
                    font,
                    x,
                    y,
                    ancho
            );

        }else{

            dibujarVerificando(
                    stack,
                    font,
                    x,
                    y,
                    ancho
            );
        }

        dibujarFirma(
                stack,
                font,
                x+
                        ancho-
                        8-
                        font.width(
                                "by HJunior"
                        ),
                y+
                        alto-
                        12
        );
    }

    private static void dibujarCabeceraResultado(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int minutos,
            LegendaryWatcher.Resultado resultado
    ){
        String titulo=
                TextFormatting.BOLD+
                        "\u2726 NAIRA LEGEND \u2726";

        font.drawShadow(
                stack,
                titulo,
                x+
                        ancho/2-
                        font.width(titulo)/2,
                y+4,
                CYAN
        );

        String estado=
                resultado==
                        LegendaryWatcher.Resultado.VERIFICANDO
                        ?"CHECK"
                        :minutos+
                        "m";

        int color;

        if(resultado==
                LegendaryWatcher.Resultado.VERIFICANDO){

            color=
                    DORADO;

        }else if(resultado==
                LegendaryWatcher.Resultado.SPAWN){

            color=
                    LOCAL;

        }else{

            color=
                    AZUL_RESULTADO;
        }

        String estadoNegrita=
                TextFormatting.BOLD+
                        estado;

        font.drawShadow(
                stack,
                estadoNegrita,
                x+
                        ancho-
                        8-
                        font.width(
                                estadoNegrita
                        ),
                y+4,
                color
        );

        AbstractGui.fill(
                stack,
                x+8,
                y+17,
                x+ancho-8,
                y+18,
                0x66358A97
        );
    }

    private static void dibujarVerificando(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        dibujarTextoCentrado(
                stack,
                font,
                TextFormatting.BOLD+
                        "VERIFICANDO RESULTADO",
                x,
                ancho,
                y+28,
                DORADO
        );

        int proximo=
                LegendaryWatcher
                        .getProximoContador();

        String linea1=
                proximo>=0
                        ?"Nuevo ciclo detectado · "+
                        proximo+
                        "m"
                        :"Nuevo ciclo detectado";

        dibujarTextoCentrado(
                stack,
                font,
                linea1,
                x,
                ancho,
                y+44,
                TEXTO
        );

        dibujarTextoCentrado(
                stack,
                font,
                "Esperando anuncio del Prof. Oak...",
                x,
                ancho,
                y+55,
                SECUNDARIO
        );
    }

    private static void dibujarSinSpawn(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        dibujarTextoCentrado(
                stack,
                font,
                TextFormatting.BOLD+
                        "SIN SPAWN",
                x,
                ancho,
                y+28,
                AZUL_RESULTADO
        );

        dibujarTextoCentrado(
                stack,
                font,
                "No se detecto aparicion legendaria",
                x,
                ancho,
                y+44,
                TEXTO
        );

        int proximo=
                LegendaryWatcher
                        .getProximoContador();

        String detalle=
                proximo>=0
                        ?"Proximo ciclo · "+
                        proximo+
                        "m"
                        :"Esperando siguiente ciclo";

        dibujarTextoCentrado(
                stack,
                font,
                detalle,
                x,
                ancho,
                y+55,
                SECUNDARIO
        );
    }

    private static void dibujarSpawnConfirmado(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        dibujarTextoCentrado(
                stack,
                font,
                TextFormatting.BOLD+
                        "LEGENDARIO DETECTADO",
                x,
                ancho,
                y+27,
                LOCAL
        );

        LegendaryEvent evento=
                LegendaryWatcher
                        .getUltimoSpawn();

        if(evento==null){

            dibujarTextoCentrado(
                    stack,
                    font,
                    "Spawn confirmado",
                    x,
                    ancho,
                    y+48,
                    TEXTO
            );

            return;
        }

        AbstractGui.fill(
                stack,
                x+14,
                y+45,
                x+16,
                y+76,
                LOCAL
        );

        font.drawShadow(
                stack,
                evento.getPokemon(),
                x+21,
                y+46,
                LOCAL_TEXTO
        );

        String bioma=
                evento.getBioma()==null
                        ?""
                        :evento.getBioma();

        if(!bioma.isEmpty()){

            font.draw(
                    stack,
                    recortar(
                            font,
                            bioma,
                            ancho-32
                    ),
                    x+21,
                    y+59,
                    TEXTO
            );
        }

        String jugador=
                evento.getJugador()==null
                        ?""
                        :evento.getJugador();

        if(!jugador.isEmpty()){

            font.draw(
                    stack,
                    recortar(
                            font,
                            "Cerca de "+
                                    jugador,
                            ancho-32
                    ),
                    x+21,
                    y+71,
                    SECUNDARIO
            );
        }
    }

    private static void dibujarTextoCentrado(
            MatrixStack stack,
            FontRenderer font,
            String texto,
            int x,
            int ancho,
            int y,
            int color
    ){
        font.drawShadow(
                stack,
                texto,
                x+
                        ancho/2-
                        font.width(texto)/2,
                y,
                color
        );
    }

    private static void dibujarFondo(
            MatrixStack stack,
            int x,
            int y,
            int ancho,
            int alto
    ){
        AbstractGui.fill(
                stack,
                x+2,
                y+3,
                x+ancho+2,
                y+alto+3,
                0x56000000
        );

        dibujarDegradadoVertical(
                stack,
                x+1,
                y+1,
                x+ancho-1,
                y+alto-1,
                FONDO_SUPERIOR,
                FONDO_INFERIOR,
                8
        );

        AbstractGui.fill(
                stack,
                x,
                y,
                x+ancho,
                y+1,
                BORDE
        );

        AbstractGui.fill(
                stack,
                x,
                y+alto-1,
                x+ancho,
                y+alto,
                0x77317B87
        );

        AbstractGui.fill(
                stack,
                x,
                y,
                x+1,
                y+alto,
                0x77317B87
        );

        AbstractGui.fill(
                stack,
                x+ancho-1,
                y,
                x+ancho,
                y+alto,
                0x77317B87
        );

        AbstractGui.fill(
                stack,
                x+1,
                y+1,
                x+ancho-1,
                y+17,
                0x241A2A35
        );

        AbstractGui.fill(
                stack,
                x+5,
                y,
                x+ancho-5,
                y+1,
                0xD05EE5F1
        );
    }

    private static void dibujarCabecera(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int minutos,
            long objetivo
    ){
        String titulo=
                TextFormatting.BOLD+
                        "\u2726 NAIRA LEGEND \u2726";

        int tituloX=
                x+
                        ancho/2-
                        font.width(titulo)/2;

        font.drawShadow(
                stack,
                titulo,
                tituloX,
                y+4,
                CYAN
        );

        dibujarBadge(
                stack,
                font,
                x,
                y,
                ancho,
                minutos
        );

        AbstractGui.fill(
                stack,
                x+8,
                y+17,
                x+ancho-8,
                y+18,
                0x66358A97
        );

        int lineaY=
                y+21;

        String prefijo=
                "Spawn ~";

        String hora=
                formatearHora(
                        objetivo
                )+
                        " MC";

        String horario=
                franjaPrincipal(
                        objetivo
                );

        int cursor=
                x+9;

        font.draw(
                stack,
                prefijo,
                cursor,
                lineaY,
                SECUNDARIO
        );

        cursor+=
                font.width(
                        prefijo
                );

        font.drawShadow(
                stack,
                hora,
                cursor,
                lineaY,
                TEXTO
        );

        cursor+=
                font.width(
                        hora
                );

        font.draw(
                stack,
                " · "+
                        horario,
                cursor,
                lineaY,
                SECUNDARIO
        );
    }

    private static void dibujarBadge(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int minutos
    ){
        String texto=
                TextFormatting.BOLD
                        .toString()+
                        minutos+
                        "m";

        int textoAncho=
                font.width(
                        texto
                );

        int bx=
                x+
                        ancho-
                        8-
                        textoAncho;

        int color=
                colorTextoEstado(
                        minutos
                );

        font.drawShadow(
                stack,
                texto,
                bx,
                y+4,
                color
        );

        AbstractGui.fill(
                stack,
                bx,
                y+14,
                x+ancho-8,
                y+15,
                colorEstado(
                        minutos
                )
        );
    }

    private static void dibujarResumen(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int minutos
    ){
        dibujarSeparador(
                stack,
                x,
                y+33,
                ancho
        );

        String titulo=
                minutos<=1
                        ?"MEJORES COINCIDENCIAS"
                        :"PREVISI\u00D3N TEMPORAL";

        font.drawShadow(
                stack,
                TextFormatting.BOLD+
                        titulo,
                x+10,
                y+40,
                CYAN_SUAVE
        );

        int filaY=
                y+55;

        int limite=
                Math.min(
                        4,
                        cacheTodos.size()
                );

        if(limite==0){

            font.draw(
                    stack,
                    "Sin candidatos",
                    x+10,
                    filaY,
                    SECUNDARIO
            );
        }

        for(int i=0;
            i<limite;
            i++){

            dibujarCandidatoResumen(
                    stack,
                    font,
                    x,
                    ancho,
                    filaY,
                    cacheTodos.get(i)
            );

            filaY+=12;
        }

        int restantes=
                Math.max(
                        0,
                        cacheTodos.size()-
                                limite
                );

        int footerY=
                y+104;

        if(restantes>0){

            font.drawShadow(
                    stack,
                    "+"+
                            restantes+
                            " candidatos",
                    x+10,
                    footerY,
                    CYAN
            );

            String ayuda=
                    "Shift+rueda";

            font.draw(
                    stack,
                    ayuda,
                    x+
                            ancho-
                            10-
                            font.width(
                                    ayuda
                            ),
                    footerY,
                    0xFF71808E
            );
        }

        font.draw(
                stack,
                recortar(
                        font,
                        cacheNota,
                        ancho-20
                ),
                x+10,
                y+117,
                SECUNDARIO
        );
    }

    private static void dibujarCandidatoResumen(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int ancho,
            int y,
            LineaCandidato candidato
    ){
        int nombreX=
                x+15;

        int detalleX=
                x+91;

        if(candidato.local){

            AbstractGui.fill(
                    stack,
                    x+9,
                    y,
                    x+11,
                    y+10,
                    LOCAL
            );
        }

        String nombre=
                recortar(
                        font,
                        candidato.nombre,
                        detalleX-
                                nombreX-
                                6
                );

        font.drawShadow(
                stack,
                nombre,
                nombreX,
                y,
                candidato.local
                        ?LOCAL_TEXTO
                        :TEXTO
        );

        String detalle=
                crearDetalle(
                        candidato.bioma,
                        candidato.condicion
                );

        font.draw(
                stack,
                recortar(
                        font,
                        detalle,
                        x+
                                ancho-
                                10-
                                detalleX
                ),
                detalleX,
                y,
                0xFF99A8B7
        );
    }

    private static void dibujarLista(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho
    ){
        dibujarSeparador(
                stack,
                x,
                y+32,
                ancho
        );

        String titulo=
                "CANDIDATOS · "+
                        cacheTodos.size();

        font.drawShadow(
                stack,
                TextFormatting.BOLD+
                        titulo,
                x+10,
                y+39,
                CYAN_SUAVE
        );

        int fin=
                Math.min(
                        filasLista.size(),
                        desplazamientoLista+
                                FILAS_VISIBLES
                );

        int filaY=
                y+54;

        for(int i=desplazamientoLista;
            i<fin;
            i++){

            FilaLista fila=
                    filasLista.get(i);

            if(fila.grupo){

                dibujarGrupo(
                        stack,
                        font,
                        x,
                        filaY,
                        fila.texto
                );

            }else{

                dibujarCandidatoLista(
                        stack,
                        font,
                        x,
                        ancho,
                        filaY,
                        fila.candidato
                );
            }

            filaY+=11;
        }

        font.draw(
                stack,
                "Shift+rueda",
                x+10,
                y+149,
                0xFF71808E
        );

        int indicadorX=
                x+ancho-10;

        if(fin<
                filasLista.size()){

            String abajo="\u25BE";

            font.draw(
                    stack,
                    abajo,
                    indicadorX-
                            font.width(
                                    abajo
                            ),
                    y+149,
                    CYAN
            );

            indicadorX-=12;
        }

        if(desplazamientoLista>0){

            String arriba="\u25B4";

            font.draw(
                    stack,
                    arriba,
                    indicadorX-
                            font.width(
                                    arriba
                            ),
                    y+149,
                    CYAN
            );
        }

        dibujarSeparadorSuave(
                stack,
                x,
                y+160,
                ancho
        );
    }

    private static void dibujarGrupo(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            String texto
    ){
        AbstractGui.fill(
                stack,
                x+10,
                y,
                x+11,
                y+9,
                CYAN_OSCURO
        );

        font.drawShadow(
                stack,
                texto,
                x+16,
                y,
                0xFF78DCE8
        );
    }

    private static void dibujarCandidatoLista(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int ancho,
            int y,
            LineaCandidato candidato
    ){
        int nombreX=
                x+20;

        if(candidato.local){

            AbstractGui.fill(
                    stack,
                    x+14,
                    y,
                    x+16,
                    y+10,
                    LOCAL
            );
        }

        String nombre=
                candidato.nombre;

        font.drawShadow(
                stack,
                nombre,
                nombreX,
                y,
                candidato.local
                        ?LOCAL_TEXTO
                        :TEXTO
        );

        if(candidato.condicion
                .isEmpty()){

            return;
        }

        int detalleX=
                nombreX+
                        font.width(
                                nombre
                        )+
                        5;

        int disponible=
                x+
                        ancho-
                        10-
                        detalleX;

        if(disponible<=10){
            return;
        }

        font.draw(
                stack,
                recortar(
                        font,
                        "· "+
                                candidato.condicion,
                        disponible
                ),
                detalleX,
                y,
                0xFF91A0AF
        );
    }

    private static void dibujarSeparador(
            MatrixStack stack,
            int x,
            int y,
            int ancho
    ){
        AbstractGui.fill(
                stack,
                x+8,
                y,
                x+ancho-8,
                y+1,
                SEPARADOR
        );
    }

    private static void dibujarSeparadorSuave(
            MatrixStack stack,
            int x,
            int y,
            int ancho
    ){
        AbstractGui.fill(
                stack,
                x+10,
                y,
                x+ancho-10,
                y+1,
                0x333B6872
        );
    }

    private static void dibujarPie(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y,
            int ancho,
            int alto,
            double progreso,
            int minutos
    ){
        String firma=
                "by HJunior";

        int anchoFirma=
                font.width(
                        firma
                );

        int firmaX=
                x+
                        ancho-
                        8-
                        anchoFirma;

        int firmaY;

        if(alto==
                ALTO_COMPACTO){

            firmaY=
                    y+26;

        }else{

            firmaY=
                    y+
                            alto-
                            14;
        }

        int barraX1=
                x+8;

        int barraX2=
                firmaX-7;

        int barraY=
                y+
                        alto-
                        5;

        int anchoBarra=
                Math.max(
                        0,
                        barraX2-
                                barraX1
                );

        AbstractGui.fill(
                stack,
                barraX1,
                barraY,
                barraX2,
                barraY+2,
                0x3D31434F
        );

        int relleno=
                (int)Math.round(
                        anchoBarra*
                                limitar01(
                                        progreso
                                )
                );

        if(relleno>0){

            AbstractGui.fill(
                    stack,
                    barraX1,
                    barraY,
                    barraX1+
                            relleno,
                    barraY+2,
                    colorEstado(
                            minutos
                    )
            );

            AbstractGui.fill(
                    stack,
                    barraX1+
                            relleno-
                            1,
                    barraY-1,
                    barraX1+
                            relleno,
                    barraY+3,
                    colorBrilloEstado(
                            minutos
                    )
            );
        }

        dibujarFirma(
                stack,
                font,
                firmaX,
                firmaY
        );
    }

    private static void dibujarFirma(
            MatrixStack stack,
            FontRenderer font,
            int x,
            int y
    ){
        String by=
                "by ";

        font.draw(
                stack,
                by,
                x,
                y,
                0xFF667684
        );

        String autor=
                "HJunior";

        int[] colores={
                0xFF73D8E7,
                0xFF6FDDEC,
                0xFF6AE2F1,
                0xFF66E6F4,
                0xFF70EAF5,
                0xFF7DECF4,
                0xFF8AEEF2
        };

        int cursor=
                x+
                        font.width(
                                by
                        );

        for(int i=0;
            i<autor.length();
            i++){

            String letra=
                    String.valueOf(
                            autor.charAt(i)
                    );

            font.draw(
                    stack,
                    letra,
                    cursor,
                    y,
                    colores[
                            Math.min(
                                    i,
                                    colores.length-1
                            )
                            ]
            );

            cursor+=
                    font.width(
                            letra
                    );
        }
    }

    private static double calcularProgreso(
            Minecraft mc,
            int minutos,
            long objetivo
    ){
        long ahora=
                mc.level.getDayTime();

        long restantes=
                Math.max(
                        0L,
                        objetivo-ahora
                );

        if(ultimoMinuto<0){

            ultimoMinuto=
                    minutos;

            totalCicloTicks=
                    Math.max(
                            1L,
                            restantes
                    );

        }else if(minutos>
                ultimoMinuto){

            totalCicloTicks=
                    Math.max(
                            1L,
                            restantes
                    );
        }

        ultimoMinuto=
                minutos;

        if(totalCicloTicks<=0L){
            totalCicloTicks=
                    1L;
        }

        return limitar01(
                restantes/
                        (double)
                                totalCicloTicks
        );
    }

    private static String crearDetalle(
            String bioma,
            String condicion
    ){
        String b=
                bioma==null
                        ?""
                        :bioma.trim();

        String c=
                condicion==null
                        ?""
                        :condicion.trim();

        if(b.isEmpty()){
            b=
                    "Otros";
        }

        if(c.isEmpty()){
            return b;
        }

        return b+
                " · "+
                c;
    }

    private static String limpiarBioma(
            String bioma
    ){
        if(bioma==null||
                bioma.trim().isEmpty()||
                "Anywhere"
                        .equalsIgnoreCase(
                                bioma.trim()
                        )){

            return "Otros";
        }

        return bioma.trim();
    }

    private static int colorEstado(
            int minutos
    ){
        if(minutos<=0){
            return CORAL;
        }

        if(minutos==1){
            return DORADO;
        }

        return CYAN;
    }

    private static int colorTextoEstado(
            int minutos
    ){
        if(minutos<=0){
            return 0xFFFF8A84;
        }

        if(minutos==1){
            return 0xFFFFDE78;
        }

        return 0xFFA3F2F8;
    }

    private static int colorBrilloEstado(
            int minutos
    ){
        if(minutos<=0){
            return 0xFFFFB8B2;
        }

        if(minutos==1){
            return 0xFFFFEBA8;
        }

        return 0xFFBDF8FC;
    }

    private static String franjaPrincipal(
            long ticks
    ){
        long t=
                Math.floorMod(
                        ticks,
                        24000L
                );

        if(t>=22500||
                t<300){

            return "Amanecer";
        }

        if(t<6000){
            return "Mañana";
        }

        if(t<12000){
            return "Tarde";
        }

        if(t<13800){
            return "Anochecer";
        }

        if(t>=17500&&
                t<18500){

            return "Medianoche";
        }

        return "Noche";
    }

    private static String formatearHora(
            long ticks
    ){
        long normal=
                Math.floorMod(
                        ticks,
                        24000L
                );

        long reloj=
                Math.floorMod(
                        normal+
                                6000L,
                        24000L
                );

        int minutosTotales=
                (int)(
                        reloj*
                                1440L/
                                24000L
                );

        int hora=
                minutosTotales/
                        60;

        int minuto=
                minutosTotales%
                        60;

        return dos(
                hora
        )+
                ":"+
                dos(
                        minuto
                );
    }

    private static String formatearBioma(
            String id
    ){
        if(id==null||
                id.trim().isEmpty()){

            return "";
        }

        String texto=
                id.trim();

        int separador=
                texto.indexOf(
                        ':'
                );

        if(separador>=0&&
                separador+1<
                        texto.length()){

            texto=
                    texto.substring(
                            separador+1
                    );
        }

        texto=
                texto.replace(
                        "_",
                        " "
                );

        String[] palabras=
                texto.split(
                        " "
                );

        StringBuilder resultado=
                new StringBuilder();

        for(String palabra:
                palabras){

            if(palabra.isEmpty()){
                continue;
            }

            if(resultado.length()>0){
                resultado.append(
                        " "
                );
            }

            resultado.append(
                    Character.toUpperCase(
                            palabra.charAt(
                                    0
                            )
                    )
            );

            if(palabra.length()>1){

                resultado.append(
                        palabra.substring(
                                1
                        )
                );
            }
        }

        return resultado
                .toString();
    }

    private static String formatearClima(
            String clima
    ){
        if(clima==null||
                clima.trim().isEmpty()){

            return "";
        }

        String valor=
                clima.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if("CLEAR"
                .equals(valor)){

            return "Despejado";
        }

        if("RAIN"
                .equals(valor)){

            return "Lluvia";
        }

        if("STORM"
                .equals(valor)){

            return "Tormenta";
        }

        return valor;
    }

    private static String recortar(
            FontRenderer font,
            String texto,
            int anchoMaximo
    ){
        if(texto==null){
            return "";
        }

        if(anchoMaximo<=0){
            return "";
        }

        if(font.width(texto)<=
                anchoMaximo){

            return texto;
        }

        String sufijo=
                "...";

        int limite=
                Math.max(
                        0,
                        anchoMaximo-
                                font.width(
                                        sufijo
                                )
                );

        String resultado=
                texto;

        while(!resultado.isEmpty()&&
                font.width(resultado)>
                        limite){

            resultado=
                    resultado.substring(
                            0,
                            resultado.length()-
                                    1
                    );
        }

        return resultado+
                sufijo;
    }

    private static void dibujarDegradadoVertical(
            MatrixStack stack,
            int x1,
            int y1,
            int x2,
            int y2,
            int colorSuperior,
            int colorInferior,
            int pasos
    ){
        int alto=
                y2-y1;

        if(alto<=0||
                x2<=x1){

            return;
        }

        int partes=
                Math.max(
                        1,
                        Math.min(
                                pasos,
                                alto
                        )
                );

        for(int i=0;
            i<partes;
            i++){

            int inicio=
                    y1+
                            alto*
                                    i/
                                    partes;

            int fin=
                    y1+
                            alto*
                                    (i+1)/
                                    partes;

            double t=
                    partes<=1
                            ?0.0
                            :i/
                            (double)(
                                    partes-
                                            1
                            );

            AbstractGui.fill(
                    stack,
                    x1,
                    inicio,
                    x2,
                    fin,
                    interpolarColor(
                            colorSuperior,
                            colorInferior,
                            t
                    )
            );
        }
    }

    private static int interpolarColor(
            int colorA,
            int colorB,
            double t
    ){
        t=
                limitar01(
                        t
                );

        int aA=
                colorA>>>24&
                        0xFF;

        int rA=
                colorA>>>16&
                        0xFF;

        int gA=
                colorA>>>8&
                        0xFF;

        int bA=
                colorA&
                        0xFF;

        int aB=
                colorB>>>24&
                        0xFF;

        int rB=
                colorB>>>16&
                        0xFF;

        int gB=
                colorB>>>8&
                        0xFF;

        int bB=
                colorB&
                        0xFF;

        int a=
                (int)Math.round(
                        aA+
                                (aB-aA)*
                                        t
                );

        int r=
                (int)Math.round(
                        rA+
                                (rB-rA)*
                                        t
                );

        int g=
                (int)Math.round(
                        gA+
                                (gB-gA)*
                                        t
                );

        int b=
                (int)Math.round(
                        bA+
                                (bB-bA)*
                                        t
                );

        return a<<24|
                r<<16|
                g<<8|
                b;
    }

    private static String dos(
            int valor
    ){
        return valor<10
                ?"0"+
                valor
                :String.valueOf(
                valor
        );
    }

    private static double limitar01(
            double valor
    ){
        if(valor<0.0){
            return 0.0;
        }

        if(valor>1.0){
            return 1.0;
        }

        return valor;
    }

    private static void reiniciar(){
        ultimoMinuto=-1;
        totalCicloTicks=-1L;

        cacheTodos.clear();
        filasLista.clear();

        cacheNota="";
        cacheActualizadoMs=0L;
        cacheObjetivo=-1L;
        cacheContextual=false;

        modoLista=false;
        desplazamientoLista=0;

        LegendarySpawnClock
                .reiniciar();
    }

    private static final class LineaCandidato {
        private final String nombre;
        private final String bioma;
        private final String condicion;
        private final boolean local;

        private LineaCandidato(
                String nombre,
                String bioma,
                String condicion,
                boolean local
        ){
            this.nombre=
                    nombre==null
                            ?""
                            :nombre;

            this.bioma=
                    bioma==null
                            ?"Otros"
                            :bioma;

            this.condicion=
                    condicion==null
                            ?""
                            :condicion;

            this.local=
                    local;
        }
    }

    private static final class FilaLista {
        private final boolean grupo;
        private final String texto;
        private final LineaCandidato candidato;

        private FilaLista(
                boolean grupo,
                String texto,
                LineaCandidato candidato
        ){
            this.grupo=
                    grupo;

            this.texto=
                    texto;

            this.candidato=
                    candidato;
        }

        private static FilaLista grupo(
                String texto
        ){
            return new FilaLista(
                    true,
                    texto,
                    null
            );
        }

        private static FilaLista candidato(
                LineaCandidato candidato
        ){
            return new FilaLista(
                    false,
                    "",
                    candidato
            );
        }
    }
}