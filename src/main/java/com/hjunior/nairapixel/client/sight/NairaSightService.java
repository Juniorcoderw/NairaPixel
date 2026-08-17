package com.hjunior.nairapixel.client.sight;

import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService.Objetivo;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class NairaSightService {
    private static final NairaSightService INSTANCE=
            new NairaSightService();

    private static final Logger LOGGER=
            LogManager.getLogger(
                    "NairaSight"
            );

    private static final int INTERVALO_ESCANEO=5;
    private static final int TICKS_ALERTA=60;
    private static final int TICKS_PERDIDO=30;

    private final NairaDexObjectivesService objectives=
            NairaDexObjectivesService.get();

    private Map<String,NairaSightDetection> detecciones=
            Collections.emptyMap();

    private NairaSightDetection alertaActual;

    private int ticksEscaneo;
    private int ticksAlerta;
    private int ticksPerdido;

    private String pokemonPerdido="";
    private int colorPerdido=
            NairaDexObjectivesService.COLOR_OBJETIVO_1;

    private NairaSightService(){}

    public static NairaSightService get(){
        return INSTANCE;
    }

    public NairaSightDetection getActual(){
        Objetivo principal=
                objectives.getObjetivoPrincipal();

        if(principal!=null){
            NairaSightDetection actual=
                    detecciones.get(
                            principal.getClave()
                    );

            if(actual!=null){
                return actual;
            }
        }

        for(NairaSightDetection deteccion:
                detecciones.values()){

            return deteccion;
        }

        return null;
    }

    public List<NairaSightDetection> getDetecciones(){
        return Collections.unmodifiableList(
                new ArrayList<>(
                        detecciones.values()
                )
        );
    }

    public NairaSightDetection getDeteccion(
            String pokemon,
            String forma
    ){
        for(NairaSightDetection deteccion:
                detecciones.values()){

            if(mismaClave(
                    deteccion.getPokemon(),
                    deteccion.getForma(),
                    pokemon,
                    forma
            )){
                return deteccion;
            }
        }

        return null;
    }

    public boolean hayDeteccion(){
        return !detecciones.isEmpty();
    }

    public UUID getEntidadDetectada(){
        NairaSightDetection actual=
                getActual();

        return actual==null
                ?null
                :actual.getEntityUuid();
    }

    public boolean isAlertaActiva(){
        return ticksAlerta>0&&
                alertaActual!=null;
    }

    public NairaSightDetection getAlertaActual(){
        return alertaActual;
    }

    public boolean isPerdidoActivo(){
        return ticksPerdido>0&&
                !pokemonPerdido.isEmpty();
    }

    public int getTicksAlerta(){
        return ticksAlerta;
    }

    public int getTicksPerdido(){
        return ticksPerdido;
    }

    public String getPokemonPerdido(){
        return pokemonPerdido;
    }

    public int getColorPerdido(){
        return colorPerdido;
    }

    public boolean tienePrioridadVisual(){
        return hayDeteccion()||
                isPerdidoActivo();
    }

    public void tick(){
        Minecraft mc=
                Minecraft.getInstance();

        if(ticksAlerta>0){
            ticksAlerta--;

            if(ticksAlerta==0){
                alertaActual=null;
            }
        }

        if(ticksPerdido>0){
            ticksPerdido--;

            if(ticksPerdido==0){
                pokemonPerdido="";
            }
        }

        if(mc.player==null||
                mc.level==null||
                !objectives.isNairaSightActivo()||
                objectives.getObjetivosActivos()
                        .isEmpty()){

            limpiarSinAviso();
            return;
        }

        actualizarPosiciones(
                mc.player
        );

        ticksEscaneo++;

        if(ticksEscaneo<INTERVALO_ESCANEO){
            return;
        }

        ticksEscaneo=0;
        actualizar();
    }

    public void actualizar(){
        Minecraft mc=
                Minecraft.getInstance();

        if(mc.player==null||
                mc.level==null||
                !objectives.isNairaSightActivo()){

            limpiarSinAviso();
            return;
        }

        List<Objetivo> activos=
                objectives.getObjetivosActivos();

        if(activos.isEmpty()){
            limpiarSinAviso();
            return;
        }

        Map<String,Objetivo> porClave=
                new LinkedHashMap<>();

        Map<String,PixelmonEntity> bloqueadas=
                new LinkedHashMap<>();

        Map<String,PixelmonEntity> mejores=
                new LinkedHashMap<>();

        Map<String,Double> distancias=
                new LinkedHashMap<>();

        for(Objetivo objetivo:
                activos){

            porClave.put(
                    objetivo.getClave(),
                    objetivo
            );

            NairaSightDetection anterior=
                    detecciones.get(
                            objetivo.getClave()
                    );

            if(anterior!=null&&
                    anterior.getEntityUuid()!=null){

                bloqueadas.put(
                        objetivo.getClave(),
                        null
                );
            }

            distancias.put(
                    objetivo.getClave(),
                    Double.MAX_VALUE
            );
        }

        for(Entity entity:
                mc.level.entitiesForRendering()){

            if(!(entity instanceof PixelmonEntity)){
                continue;
            }

            PixelmonEntity pixelmon=
                    (PixelmonEntity)entity;

            if(!pixelmon.isAlive()||
                    pixelmon.hasOwner()){

                continue;
            }

            Species species=
                    pixelmon.getSpecies();

            if(species==null){
                continue;
            }

            String pokemonEntidad=
                    species.getName();

            String formaEntidad=
                    formaEntidad(
                            species,
                            pixelmon.getForm()
                    );

            for(Objetivo objetivo:
                    activos){

                if(!coincide(
                        objetivo,
                        pokemonEntidad,
                        formaEntidad
                )){
                    continue;
                }

                String key=
                        objetivo.getClave();

                NairaSightDetection anterior=
                        detecciones.get(key);

                if(anterior!=null&&
                        anterior.getEntityUuid()!=null&&
                        anterior.getEntityUuid()
                                .equals(
                                        pixelmon.getUUID()
                                )){

                    bloqueadas.put(
                            key,
                            pixelmon
                    );

                    continue;
                }

                double distanciaSq=
                        distanciaCuadrada(
                                mc.player,
                                pixelmon
                        );

                Double mejor=
                        distancias.get(key);

                if(mejor==null||
                        distanciaSq<mejor){

                    distancias.put(
                            key,
                            distanciaSq
                    );

                    mejores.put(
                            key,
                            pixelmon
                    );
                }
            }
        }

        Map<String,NairaSightDetection> nuevas=
                new LinkedHashMap<>();

        List<NairaSightDetection> detectadasAhora=
                new ArrayList<>();

        List<NairaSightDetection> perdidasAhora=
                new ArrayList<>();

        for(Objetivo objetivo:
                activos){

            String key=
                    objetivo.getClave();

            PixelmonEntity elegida=
                    bloqueadas.get(key);

            if(elegida==null){
                elegida=
                        mejores.get(key);
            }

            NairaSightDetection anterior=
                    detecciones.get(key);

            if(elegida!=null){
                NairaSightDetection nueva=
                        crearDeteccion(
                                mc.player,
                                elegida,
                                objetivo
                        );

                nuevas.put(
                        key,
                        nueva
                );

                if(anterior==null||
                        anterior.getEntityUuid()==null||
                        !anterior.getEntityUuid()
                                .equals(
                                        nueva.getEntityUuid()
                                )){

                    detectadasAhora.add(
                            nueva
                    );
                }
            }else if(anterior!=null){
                perdidasAhora.add(
                        anterior
                );
            }
        }

        detecciones=
                Collections.unmodifiableMap(
                        nuevas
                );

        if(!detectadasAhora.isEmpty()){
            NairaSightDetection elegida=
                    elegirAlerta(
                            detectadasAhora
                    );

            alertaActual=elegida;
            ticksAlerta=TICKS_ALERTA;

            ticksPerdido=0;
            pokemonPerdido="";

            LOGGER.info(
                    "[NairaSight] Objetivo detectado: {} | {} bloques | {}",
                    elegida.getPokemon(),
                    elegida.getDistanciaRedondeada(),
                    elegida.getDireccion()
            );
        }else if(!perdidasAhora.isEmpty()){
            NairaSightDetection perdida=
                    elegirPerdida(
                            perdidasAhora
                    );

            pokemonPerdido=
                    perdida.getPokemon();

            colorPerdido=
                    perdida.getColor();

            ticksPerdido=
                    TICKS_PERDIDO;

            LOGGER.info(
                    "[NairaSight] Objetivo perdido: {}",
                    perdida.getPokemon()
            );
        }
    }

    public void limpiar(){
        limpiarSinAviso();
    }

    private void actualizarPosiciones(
            PlayerEntity player
    ){
        if(detecciones.isEmpty()){
            return;
        }

        Map<String,NairaSightDetection> actualizadas=
                new LinkedHashMap<>();

        for(Map.Entry<String,NairaSightDetection> entry:
                detecciones.entrySet()){

            NairaSightDetection anterior=
                    entry.getValue();

            if(anterior==null||
                    anterior.getEntity()==null||
                    !anterior.getEntity().isAlive()){

                continue;
            }

            Objetivo objetivo=
                    buscarObjetivo(
                            entry.getKey()
                    );

            if(objetivo==null){
                continue;
            }

            actualizadas.put(
                    entry.getKey(),
                    crearDeteccion(
                            player,
                            anterior.getEntity(),
                            objetivo
                    )
            );
        }

        detecciones=
                Collections.unmodifiableMap(
                        actualizadas
                );
    }

    private Objetivo buscarObjetivo(
            String clave
    ){
        for(Objetivo objetivo:
                objectives.getObjetivosActivos()){

            if(objetivo.getClave()
                    .equals(clave)){

                return objetivo;
            }
        }

        return null;
    }

    private NairaSightDetection elegirAlerta(
            List<NairaSightDetection> lista
    ){
        for(NairaSightDetection deteccion:
                lista){

            if(deteccion.isPrincipal()){
                return deteccion;
            }
        }

        NairaSightDetection mejor=null;

        for(NairaSightDetection deteccion:
                lista){

            if(mejor==null||
                    deteccion.getDistancia()<
                            mejor.getDistancia()){

                mejor=deteccion;
            }
        }

        return mejor;
    }

    private NairaSightDetection elegirPerdida(
            List<NairaSightDetection> lista
    ){
        for(NairaSightDetection deteccion:
                lista){

            if(deteccion.isPrincipal()){
                return deteccion;
            }
        }

        return lista.get(0);
    }

    private NairaSightDetection crearDeteccion(
            PlayerEntity player,
            PixelmonEntity entity,
            Objetivo objetivo
    ){
        double dx=
                entity.getX()-
                        player.getX();

        double dz=
                entity.getZ()-
                        player.getZ();

        double centroY=
                entity.getY()+
                        entity.getBbHeight()*0.5D;

        double dyVista=
                centroY-
                        player.getEyeY();

        double diferenciaY=
                entity.getY()-
                        player.getY();

        double horizontal=
                Math.sqrt(
                        (dx*dx)+
                                (dz*dz)
                );

        double distancia=
                Math.sqrt(
                        (dx*dx)+
                                (diferenciaY*diferenciaY)+
                                (dz*dz)
                );

        float yawObjetivo=
                (float)Math.toDegrees(
                        Math.atan2(
                                -dx,
                                dz
                        )
                );

        float pitchObjetivo=
                (float)-Math.toDegrees(
                        Math.atan2(
                                dyVista,
                                Math.max(
                                        horizontal,
                                        0.0001D
                                )
                        )
                );

        float anguloHorizontal=
                normalizarAngulo(
                        yawObjetivo-
                                player.yRot
                );

        float anguloVertical=
                normalizarAngulo(
                        pitchObjetivo-
                                player.xRot
                );

        Species species=
                entity.getSpecies();

        String pokemon=
                species==null
                        ?objetivo.getPokemon()
                        :species.getName();

        String forma=
                species==null
                        ?objetivo.getForma()
                        :formaEntidad(
                                species,
                                entity.getForm()
                        );

        return new NairaSightDetection(
                objetivo.getClave(),
                objectives.getColorObjetivo(
                        objetivo
                ),
                objectives.esObjetivoPrincipal(
                        objetivo.getPokemon(),
                        objetivo.getForma()
                ),
                entity.getUUID(),
                entity,
                pokemon,
                forma,
                distancia,
                diferenciaY,
                anguloHorizontal,
                anguloVertical,
                direccionCardinal(
                        dx,
                        dz
                )
        );
    }

    private boolean coincide(
            Objetivo objetivo,
            String pokemonEntidad,
            String formaEntidad
    ){
        if(objetivo==null){
            return false;
        }

        if(!normalizar(
                objetivo.getPokemon()
        ).equals(
                normalizar(
                        pokemonEntidad
                )
        )){
            return false;
        }

        return normalizarForma(
                objetivo.getForma()
        ).equals(
                normalizarForma(
                        formaEntidad
                )
        );
    }

    private String formaEntidad(
            Species species,
            Stats form
    ){
        if(species==null||
                form==null||
                species.isDefaultForm(
                        form
                )){

            return "";
        }

        String nombre=
                form.getName();

        return nombre==null
                ?""
                :nombre;
    }

    private double distanciaCuadrada(
            Entity a,
            Entity b
    ){
        double dx=
                b.getX()-
                        a.getX();

        double dy=
                b.getY()-
                        a.getY();

        double dz=
                b.getZ()-
                        a.getZ();

        return (dx*dx)+
                (dy*dy)+
                (dz*dz);
    }

    private String direccionCardinal(
            double dx,
            double dz
    ){
        double grados=
                Math.toDegrees(
                        Math.atan2(
                                dx,
                                -dz
                        )
                );

        if(grados<0){
            grados+=360.0D;
        }

        String[] direcciones={
                "N",
                "NE",
                "E",
                "SE",
                "S",
                "SO",
                "O",
                "NO"
        };

        int indice=
                (int)Math.round(
                        grados/45.0D
                )%8;

        return direcciones[indice];
    }

    private float normalizarAngulo(
            float angulo
    ){
        while(angulo<=-180.0F){
            angulo+=360.0F;
        }

        while(angulo>180.0F){
            angulo-=360.0F;
        }

        return angulo;
    }

    private boolean mismaClave(
            String pokemonA,
            String formaA,
            String pokemonB,
            String formaB
    ){
        return clave(
                pokemonA,
                formaA
        ).equals(
                clave(
                        pokemonB,
                        formaB
                )
        );
    }

    private String clave(
            String pokemon,
            String forma
    ){
        return normalizar(
                pokemon
        )+"|"+
                normalizarForma(
                        forma
                );
    }

    private String normalizarForma(
            String forma
    ){
        if(forma==null||
                forma.trim().isEmpty()||
                forma.equalsIgnoreCase(
                        "base"
                )){

            return "";
        }

        return normalizar(
                forma
        );
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
                )
                .replace("_","")
                .replace("-","")
                .replace(" ","");
    }

    private void limpiarSinAviso(){
        detecciones=
                Collections.emptyMap();

        alertaActual=null;

        ticksEscaneo=0;
        ticksAlerta=0;
        ticksPerdido=0;

        pokemonPerdido="";
    }
}
