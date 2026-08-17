package com.hjunior.nairapixel.client.dex.objectives;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class NairaDexObjectivesService {
    public static final int MAX_OBJETIVOS=3;

    public static final int COLOR_OBJETIVO_1=0xFF4FD7DF;
    public static final int COLOR_OBJETIVO_2=0xFFE0B84F;
    public static final int COLOR_OBJETIVO_3=0xFFD86BFF;

    private static final int[] COLORES={
            COLOR_OBJETIVO_1,
            COLOR_OBJETIVO_2,
            COLOR_OBJETIVO_3
    };

    private static final Gson GSON=
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path DIRECTORIO=
            FMLPaths.CONFIGDIR
                    .get()
                    .resolve(
                            "nairapixel"
                    );

    private static final Path ARCHIVO=
            DIRECTORIO.resolve(
                    "objectives.json"
            );

    private static final NairaDexObjectivesService INSTANCE=
            new NairaDexObjectivesService();

    private final Map<String,Guardado> guardados=
            new LinkedHashMap<>();

    private final Map<String,Objetivo> objetivos=
            new LinkedHashMap<>();

    private String principalClave="";

    private boolean scannerActivo=true;
    private boolean hudNairaHunt=true;
    private boolean nairaSightActivo=true;
    private boolean avisosNairaSight=true;

    private NairaDexObjectivesService(){
        cargar();
    }

    public static NairaDexObjectivesService get(){
        return INSTANCE;
    }

    public Objetivo getObjetivoActivo(){
        return getObjetivoPrincipal();
    }

    public Objetivo getObjetivoPrincipal(){
        if(principalClave!=null&&
                !principalClave.isEmpty()){

            Objetivo principal=
                    objetivos.get(
                            principalClave
                    );

            if(principal!=null){
                return principal;
            }
        }

        for(Objetivo objetivo:
                objetivos.values()){

            return objetivo;
        }

        return null;
    }

    public List<Objetivo> getObjetivosActivos(){
        List<Objetivo> lista=
                new ArrayList<>(
                        objetivos.values()
                );

        Collections.sort(
                lista,
                (a,b)->
                        Integer.compare(
                                a.getSlot(),
                                b.getSlot()
                        )
        );

        return Collections.unmodifiableList(
                lista
        );
    }

    public int getCantidadObjetivosActivos(){
        return objetivos.size();
    }

    public boolean tieneObjetivoActivo(){
        return !objetivos.isEmpty();
    }

    public boolean puedeAgregarObjetivo(){
        return objetivos.size()<MAX_OBJETIVOS;
    }

    public boolean esObjetivoActivo(
            String pokemon,
            String forma
    ){
        return objetivos.containsKey(
                clave(
                        pokemon,
                        forma
                )
        );
    }

    public boolean esObjetivoPrincipal(
            String pokemon,
            String forma
    ){
        String key=
                clave(
                        pokemon,
                        forma
                );

        return !key.isEmpty()&&
                key.equals(
                        principalClave
                )&&
                objetivos.containsKey(key);
    }

    public int getIndiceObjetivo(
            String pokemon,
            String forma
    ){
        Objetivo objetivo=
                objetivos.get(
                        clave(
                                pokemon,
                                forma
                        )
                );

        return objetivo==null
                ?-1
                :objetivo.getSlot();
    }

    public int getColorObjetivo(
            String pokemon,
            String forma
    ){
        int slot=
                getIndiceObjetivo(
                        pokemon,
                        forma
                );

        return getColorSlot(
                slot
        );
    }

    public int getColorObjetivo(
            Objetivo objetivo
    ){
        return objetivo==null
                ?COLOR_OBJETIVO_1
                :getColorSlot(
                        objetivo.getSlot()
                );
    }

    public int getColorSlot(
            int slot
    ){
        if(slot<0||
                slot>=COLORES.length){

            return COLOR_OBJETIVO_1;
        }

        return COLORES[slot];
    }

    public boolean isScannerActivo(){
        return scannerActivo;
    }

    public void setScannerActivo(
            boolean activo
    ){
        if(scannerActivo==activo){
            return;
        }

        scannerActivo=activo;
        guardar();
    }

    public boolean alternarScanner(){
        scannerActivo=!scannerActivo;
        guardar();
        return scannerActivo;
    }

    public boolean isHudNairaHuntActivo(){
        return hudNairaHunt;
    }

    public void setHudNairaHuntActivo(
            boolean activo
    ){
        if(hudNairaHunt==activo){
            return;
        }

        hudNairaHunt=activo;
        guardar();
    }

    public boolean alternarHudNairaHunt(){
        hudNairaHunt=!hudNairaHunt;
        guardar();
        return hudNairaHunt;
    }

    public boolean isNairaSightActivo(){
        return nairaSightActivo;
    }

    public void setNairaSightActivo(
            boolean activo
    ){
        if(nairaSightActivo==activo){
            return;
        }

        nairaSightActivo=activo;
        guardar();
    }

    public boolean alternarNairaSight(){
        nairaSightActivo=!nairaSightActivo;
        guardar();
        return nairaSightActivo;
    }

    public boolean isAvisosNairaSightActivos(){
        return avisosNairaSight;
    }

    public void setAvisosNairaSightActivos(
            boolean activo
    ){
        if(avisosNairaSight==activo){
            return;
        }

        avisosNairaSight=activo;
        guardar();
    }

    public boolean alternarAvisosNairaSight(){
        avisosNairaSight=!avisosNairaSight;
        guardar();
        return avisosNairaSight;
    }

    public void marcarObjetivo(
            String pokemon,
            String forma,
            List<PokemonSpawnRule> spawns
    ){
        agregarObjetivo(
                pokemon,
                forma,
                spawns,
                true
        );
    }

    public boolean agregarObjetivo(
            String pokemon,
            String forma,
            List<PokemonSpawnRule> spawns
    ){
        return agregarObjetivo(
                pokemon,
                forma,
                spawns,
                true
        );
    }

    private boolean agregarObjetivo(
            String pokemon,
            String forma,
            List<PokemonSpawnRule> spawns,
            boolean hacerPrincipal
    ){
        if(pokemon==null||
                pokemon.trim().isEmpty()){

            return false;
        }

        String limpioPokemon=
                pokemon.trim();

        String limpiaForma=
                normalizarForma(
                        forma
                );

        String key=
                clave(
                        limpioPokemon,
                        limpiaForma
                );

        Objetivo existente=
                objetivos.get(key);

        if(existente!=null){
            Objetivo actualizado=
                    new Objetivo(
                            limpioPokemon,
                            limpiaForma,
                            spawns,
                            existente.getSlot()
                    );

            objetivos.put(
                    key,
                    actualizado
            );

            if(hacerPrincipal){
                principalClave=key;
            }

            guardar();
            return true;
        }

        if(objetivos.size()>=MAX_OBJETIVOS){
            return false;
        }

        int slot=
                primerSlotLibre();

        Objetivo nuevo=
                new Objetivo(
                        limpioPokemon,
                        limpiaForma,
                        spawns,
                        slot
                );

        objetivos.put(
                key,
                nuevo
        );

        if(hacerPrincipal||
                principalClave.isEmpty()){

            principalClave=key;
        }

        guardar();
        return true;
    }

    public boolean hacerPrincipal(
            String pokemon,
            String forma
    ){
        String key=
                clave(
                        pokemon,
                        forma
                );

        if(!objetivos.containsKey(key)){
            return false;
        }

        if(key.equals(principalClave)){
            return true;
        }

        principalClave=key;
        guardar();
        return true;
    }

    public void quitarObjetivo(){
        Objetivo principal=
                getObjetivoPrincipal();

        if(principal==null){
            return;
        }

        quitarObjetivo(
                principal.getPokemon(),
                principal.getForma()
        );
    }

    public boolean quitarObjetivo(
            String pokemon,
            String forma
    ){
        String key=
                clave(
                        pokemon,
                        forma
                );

        Objetivo eliminado=
                objetivos.remove(key);

        if(eliminado==null){
            return false;
        }

        if(key.equals(principalClave)){
            principalClave="";

            Objetivo siguiente=
                    objetivoMenorSlot();

            if(siguiente!=null){
                principalClave=
                        siguiente.getClave();
            }
        }

        guardar();
        return true;
    }

    public List<Guardado> getGuardados(){
        return Collections.unmodifiableList(
                new ArrayList<>(
                        guardados.values()
                )
        );
    }

    public int getCantidadGuardados(){
        return guardados.size();
    }

    public boolean estaGuardado(
            String pokemon,
            String forma
    ){
        return guardados.containsKey(
                clave(
                        pokemon,
                        forma
                )
        );
    }

    public boolean alternarGuardado(
            String pokemon,
            String forma
    ){
        if(pokemon==null||
                pokemon.trim().isEmpty()){

            return false;
        }

        String key=
                clave(
                        pokemon,
                        forma
                );

        if(guardados.containsKey(key)){
            guardados.remove(key);
            guardar();
            return false;
        }

        guardados.put(
                key,
                new Guardado(
                        pokemon.trim(),
                        normalizarForma(
                                forma
                        )
                )
        );

        guardar();
        return true;
    }

    public void quitarGuardado(
            String pokemon,
            String forma
    ){
        Guardado eliminado=
                guardados.remove(
                        clave(
                                pokemon,
                                forma
                        )
                );

        if(eliminado!=null){
            guardar();
        }
    }

    public Path getArchivoPersistencia(){
        return ARCHIVO;
    }

    private int primerSlotLibre(){
        boolean[] usados=
                new boolean[MAX_OBJETIVOS];

        for(Objetivo objetivo:
                objetivos.values()){

            if(objetivo.getSlot()>=0&&
                    objetivo.getSlot()<MAX_OBJETIVOS){

                usados[objetivo.getSlot()]=true;
            }
        }

        for(int i=0;i<MAX_OBJETIVOS;i++){
            if(!usados[i]){
                return i;
            }
        }

        return 0;
    }

    private Objetivo objetivoMenorSlot(){
        Objetivo mejor=null;

        for(Objetivo objetivo:
                objetivos.values()){

            if(mejor==null||
                    objetivo.getSlot()<
                            mejor.getSlot()){

                mejor=objetivo;
            }
        }

        return mejor;
    }

    private void cargar(){
        guardados.clear();
        objetivos.clear();
        principalClave="";

        if(!Files.isRegularFile(
                ARCHIVO
        )){
            return;
        }

        try(BufferedReader reader=
                    Files.newBufferedReader(
                            ARCHIVO,
                            StandardCharsets.UTF_8
                    )){

            DatosPersistidos datos=
                    GSON.fromJson(
                            reader,
                            DatosPersistidos.class
                    );

            if(datos==null){
                return;
            }

            scannerActivo=
                    datos.scannerActivo==null
                            ?true
                            :datos.scannerActivo;

            hudNairaHunt=
                    datos.hudNairaHunt==null
                            ?true
                            :datos.hudNairaHunt;

            nairaSightActivo=
                    datos.nairaSightActivo==null
                            ?true
                            :datos.nairaSightActivo;

            avisosNairaSight=
                    datos.avisosNairaSight==null
                            ?true
                            :datos.avisosNairaSight;

            cargarGuardados(
                    datos.guardados
            );

            if(datos.objetivos!=null&&
                    !datos.objetivos.isEmpty()){

                cargarObjetivos(
                        datos.objetivos
                );
            }else if(pokemonValido(
                    datos.objetivo
            )){
                List<PokemonPersistido> legado=
                        new ArrayList<>();

                legado.add(
                        datos.objetivo
                );

                cargarObjetivos(
                        legado
                );
            }

            if(datos.principalClave!=null&&
                    objetivos.containsKey(
                            datos.principalClave
                    )){

                principalClave=
                        datos.principalClave;
            }

            if(principalClave.isEmpty()){
                Objetivo principal=
                        objetivoMenorSlot();

                if(principal!=null){
                    principalClave=
                            principal.getClave();
                }
            }
        }catch(Exception ignored){
            guardados.clear();
            objetivos.clear();
            principalClave="";
        }
    }

    private void cargarGuardados(
            List<PokemonPersistido> datos
    ){
        if(datos==null){
            return;
        }

        for(PokemonPersistido pokemon:
                datos){

            if(!pokemonValido(
                    pokemon
            )){
                continue;
            }

            Guardado guardado=
                    new Guardado(
                            pokemon.pokemon.trim(),
                            normalizarForma(
                                    pokemon.forma
                            )
                    );

            guardados.put(
                    guardado.getClave(),
                    guardado
            );
        }
    }

    private void cargarObjetivos(
            List<PokemonPersistido> datos
    ){
        if(datos==null){
            return;
        }

        Set<Integer> slotsUsados=
                new LinkedHashSet<>();

        for(PokemonPersistido pokemon:
                datos){

            if(objetivos.size()>=MAX_OBJETIVOS){
                break;
            }

            if(!pokemonValido(
                    pokemon
            )){
                continue;
            }

            int slot=
                    pokemon.slot==null
                            ?primerSlotDisponible(
                                    slotsUsados
                            )
                            :pokemon.slot;

            if(slot<0||
                    slot>=MAX_OBJETIVOS||
                    slotsUsados.contains(slot)){

                slot=
                        primerSlotDisponible(
                                slotsUsados
                        );
            }

            slotsUsados.add(slot);

            Objetivo objetivo=
                    new Objetivo(
                            pokemon.pokemon.trim(),
                            normalizarForma(
                                    pokemon.forma
                            ),
                            restaurarSpawns(
                                    pokemon.spawns
                            ),
                            slot
                    );

            objetivos.put(
                    objetivo.getClave(),
                    objetivo
            );
        }
    }

    private int primerSlotDisponible(
            Set<Integer> usados
    ){
        for(int i=0;i<MAX_OBJETIVOS;i++){
            if(!usados.contains(i)){
                return i;
            }
        }

        return 0;
    }

    private void guardar(){
        DatosPersistidos datos=
                new DatosPersistidos();

        datos.scannerActivo=
                scannerActivo;

        datos.hudNairaHunt=
                hudNairaHunt;

        datos.nairaSightActivo=
                nairaSightActivo;

        datos.avisosNairaSight=
                avisosNairaSight;

        datos.principalClave=
                principalClave;

        datos.objetivos=
                new ArrayList<>();

        for(Objetivo objetivo:
                getObjetivosActivos()){

            datos.objetivos.add(
                    PokemonPersistido.desdeObjetivo(
                            objetivo
                    )
            );
        }

        Objetivo principal=
                getObjetivoPrincipal();

        if(principal!=null){
            datos.objetivo=
                    PokemonPersistido.desdeObjetivo(
                            principal
                    );
        }

        datos.guardados=
                new ArrayList<>();

        for(Guardado guardado:
                guardados.values()){

            datos.guardados.add(
                    PokemonPersistido.desdeGuardado(
                            guardado
                    )
            );
        }

        Path temporal=
                DIRECTORIO.resolve(
                        "objectives.json.tmp"
                );

        try{
            Files.createDirectories(
                    DIRECTORIO
            );

            try(BufferedWriter writer=
                        Files.newBufferedWriter(
                                temporal,
                                StandardCharsets.UTF_8
                        )){

                GSON.toJson(
                        datos,
                        writer
                );
            }

            try{
                Files.move(
                        temporal,
                        ARCHIVO,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            }catch(IOException atomicNoDisponible){
                Files.move(
                        temporal,
                        ARCHIVO,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }catch(Exception ignored){
            try{
                Files.deleteIfExists(
                        temporal
                );
            }catch(IOException ignoredDelete){
            }
        }
    }

    private static boolean pokemonValido(
            PokemonPersistido pokemon
    ){
        return pokemon!=null&&
                pokemon.pokemon!=null&&
                !pokemon.pokemon.trim()
                        .isEmpty();
    }

    private static List<PokemonSpawnRule> restaurarSpawns(
            List<SpawnPersistido> datos
    ){
        if(datos==null||
                datos.isEmpty()){

            return Collections.emptyList();
        }

        List<PokemonSpawnRule> resultado=
                new ArrayList<>();

        for(SpawnPersistido spawn:
                datos){

            if(spawn==null){
                continue;
            }

            resultado.add(
                    spawn.toRule()
            );
        }

        return resultado;
    }

    private static String clave(
            String pokemon,
            String forma
    ){
        String p=
                pokemon==null
                        ?""
                        :pokemon.trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        return p+
                "|"+
                normalizarForma(
                        forma
                ).toLowerCase(
                        Locale.ROOT
                );
    }

    private static String normalizarForma(
            String forma
    ){
        if(forma==null||
                forma.trim().isEmpty()||
                forma.equalsIgnoreCase(
                        "base"
                )){

            return "";
        }

        return forma.trim();
    }

    public static class Guardado {
        private final String pokemon;
        private final String forma;
        private final String clave;

        private Guardado(
                String pokemon,
                String forma
        ){
            this.pokemon=pokemon;
            this.forma=forma;
            this.clave=
                    NairaDexObjectivesService.clave(
                            pokemon,
                            forma
                    );
        }

        public String getPokemon(){
            return pokemon;
        }

        public String getForma(){
            return forma;
        }

        public String getClave(){
            return clave;
        }

        public boolean tieneForma(){
            return forma!=null&&
                    !forma.isEmpty();
        }
    }

    public static final class Objetivo
            extends Guardado{

        private final List<PokemonSpawnRule> spawns;
        private final int slot;

        private Objetivo(
                String pokemon,
                String forma,
                List<PokemonSpawnRule> spawns,
                int slot
        ){
            super(
                    pokemon,
                    forma
            );

            this.spawns=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    spawns==null
                                            ?Collections.emptyList()
                                            :spawns
                            )
                    );

            this.slot=slot;
        }

        public List<PokemonSpawnRule> getSpawns(){
            return spawns;
        }

        public int getSlot(){
            return slot;
        }
    }

    private static final class DatosPersistidos {
        private int version=4;

        private Boolean scannerActivo=Boolean.TRUE;
        private Boolean hudNairaHunt=Boolean.TRUE;
        private Boolean nairaSightActivo=Boolean.TRUE;
        private Boolean avisosNairaSight=Boolean.TRUE;

        private String principalClave="";

        private PokemonPersistido objetivo;

        private List<PokemonPersistido> objetivos=
                new ArrayList<>();

        private List<PokemonPersistido> guardados=
                new ArrayList<>();
    }

    private static final class PokemonPersistido {
        private String pokemon;
        private String forma="";
        private Integer slot;
        private List<SpawnPersistido> spawns=
                new ArrayList<>();

        private static PokemonPersistido desdeGuardado(
                Guardado guardado
        ){
            PokemonPersistido dato=
                    new PokemonPersistido();

            dato.pokemon=
                    guardado.getPokemon();

            dato.forma=
                    guardado.getForma();

            return dato;
        }

        private static PokemonPersistido desdeObjetivo(
                Objetivo objetivo
        ){
            PokemonPersistido dato=
                    desdeGuardado(
                            objetivo
                    );

            dato.slot=
                    objetivo.getSlot();

            for(PokemonSpawnRule regla:
                    objetivo.getSpawns()){

                if(regla!=null){
                    dato.spawns.add(
                            SpawnPersistido.desde(
                                    regla
                            )
                    );
                }
            }

            return dato;
        }
    }

    private static final class SpawnPersistido {
        private String pokemon="";
        private String forma="";
        private String spec="";
        private List<String> horarios=
                new ArrayList<>();
        private List<String> biomas=
                new ArrayList<>();
        private List<String> climas=
                new ArrayList<>();
        private Integer minY;
        private Integer maxY;
        private Integer faseLunar;
        private List<String> bloquesBase=
                new ArrayList<>();
        private String origen="";

        private static SpawnPersistido desde(
                PokemonSpawnRule regla
        ){
            SpawnPersistido dato=
                    new SpawnPersistido();

            dato.pokemon=
                    nvl(
                            regla.getPokemon()
                    );

            dato.forma=
                    nvl(
                            regla.getForma()
                    );

            dato.spec=
                    nvl(
                            regla.getSpec()
                    );

            dato.horarios=
                    copia(
                            regla.getHorarios()
                    );

            dato.biomas=
                    copia(
                            regla.getBiomas()
                    );

            dato.climas=
                    copia(
                            regla.getClimas()
                    );

            dato.minY=
                    regla.getMinY();

            dato.maxY=
                    regla.getMaxY();

            dato.faseLunar=
                    regla.getFaseLunar();

            dato.bloquesBase=
                    copia(
                            regla.getBloquesBase()
                    );

            dato.origen=
                    nvl(
                            regla.getOrigen()
                    );

            return dato;
        }

        private PokemonSpawnRule toRule(){
            return new PokemonSpawnRule(
                    nvl(
                            pokemon
                    ),
                    nvl(
                            forma
                    ),
                    nvl(
                            spec
                    ),
                    copia(
                            horarios
                    ),
                    copia(
                            biomas
                    ),
                    copia(
                            climas
                    ),
                    minY,
                    maxY,
                    faseLunar,
                    copia(
                            bloquesBase
                    ),
                    nvl(
                            origen
                    )
            );
        }
    }

    private static List<String> copia(
            List<String> origen
    ){
        return origen==null
                ?new ArrayList<>()
                :new ArrayList<>(
                        origen
                );
    }

    private static String nvl(
            String valor
    ){
        return valor==null
                ?""
                :valor;
    }
}
