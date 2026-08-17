package com.hjunior.nairapixel.core.pixelmon.spawn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.conditions.SpawnCondition;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class PixelmonSpawnProvider {
    private static final Logger LOGGER=
            LogManager.getLogger("NairaSpawn");

    private static final String RUTA_STANDARD=
            "data/pixelmon/spawning/standard/";

    private static final String RUTA_LEGENDARIOS=
            "data/pixelmon/spawning/legendaries/";

    private static final String RUTA_CONFIG=
            "data/pixelmon/config/betterspawnerconfig.json";

    private static Map<String,List<PokemonSpawnRule>> cache=
            Collections.emptyMap();

    private static Map<String,List<PokemonSpawnRule>> cachePorForma=
            Collections.emptyMap();

    private static boolean cargado;
    private static String fuente="sin_cargar";

    private PixelmonSpawnProvider(){}

    public static List<PokemonSpawnRule> getReglas(String pokemon){
        asegurarCarga();

        List<PokemonSpawnRule> reglas=
                cache.get(normalizar(pokemon));

        return reglas==null
                ?Collections.emptyList()
                :reglas;
    }

    public static List<PokemonSpawnRule> getReglas(
            String pokemon,
            String forma
    ){
        asegurarCarga();

        String pokemonKey=
                normalizar(pokemon);

        if(pokemonKey.isEmpty()){
            return Collections.emptyList();
        }

        String formaKey=
                normalizarForma(forma);

        List<PokemonSpawnRule> reglas=
                cachePorForma.get(
                        pokemonKey+"|"+formaKey
                );

        return reglas==null
                ?Collections.emptyList()
                :reglas;
    }

    public static int getCantidadReglas(){
        asegurarCarga();

        int total=0;

        for(List<PokemonSpawnRule> reglas:cache.values()){
            total+=reglas.size();
        }

        return total;
    }

    public static String getFuente(){
        asegurarCarga();
        return fuente;
    }

    public static synchronized void preparar(){
        asegurarCarga();
    }

    public static synchronized void reiniciar(){
        cache=Collections.emptyMap();
        cachePorForma=Collections.emptyMap();
        cargado=false;
        fuente="sin_cargar";
    }

    private static synchronized void asegurarCarga(){
        if(cargado)return;

        Map<String,List<PokemonSpawnRule>> resultado=
                new LinkedHashMap<>();

        Map<String,List<PokemonSpawnRule>> resultadoPorForma=
                new LinkedHashMap<>();

        boolean hayStandard=
                PixelmonSpawning.standard!=null&&
                        !PixelmonSpawning.standard.isEmpty();

        boolean hayLegendarios=
                PixelmonSpawning.legendaries!=null&&
                        !PixelmonSpawning.legendaries.isEmpty();

        if(hayStandard||hayLegendarios){
            cargarSetsRuntime(
                    PixelmonSpawning.standard,
                    "runtime:standard",
                    resultado,
                    resultadoPorForma
            );

            cargarSetsRuntime(
                    PixelmonSpawning.legendaries,
                    "runtime:legendaries",
                    resultado,
                    resultadoPorForma
            );

            fuente="runtime";
        }else{
            boolean ok=
                    cargarBaseDesdeJar(
                            resultado,
                            resultadoPorForma
                    );

            fuente=
                    ok
                            ?"pixelmon_base_json"
                            :"no_disponible";
        }

        cache=hacerInmutable(resultado);
        cachePorForma=hacerInmutable(resultadoPorForma);
        cargado=true;

        LOGGER.info(
                "[NairaSpawn] Fuente={} | Reglas={} | Pokemon={}",
                fuente,
                contarReglas(cache),
                cache.size()
        );
    }

    private static boolean cargarBaseDesdeJar(
            Map<String,List<PokemonSpawnRule>> resultado,
            Map<String,List<PokemonSpawnRule>> resultadoPorForma
    ){
        File archivo=
                localizarJarPixelmon();

        if(archivo==null){
            LOGGER.warn(
                    "[NairaSpawn] No se encontró el JAR de Pixelmon."
            );
            return false;
        }

        LOGGER.info(
                "[NairaSpawn] JAR Pixelmon localizado: {}",
                archivo.getAbsolutePath()
        );

        try(JarFile jar=
                    new JarFile(
                            archivo
                    )){

            Categorias categorias=
                    leerCategorias(
                            jar
                    );

            int standard=
                    leerDirectorioJson(
                            jar,
                            RUTA_STANDARD,
                            "base:standard",
                            categorias,
                            resultado,
                            resultadoPorForma
                    );

            int legendarios=
                    leerDirectorioJson(
                            jar,
                            RUTA_LEGENDARIOS,
                            "base:legendaries",
                            categorias,
                            resultado,
                            resultadoPorForma
                    );

            LOGGER.info(
                    "[NairaSpawn] JSON base leído sin inicializar Pokémon | standard={} reglas | legendaries={} reglas",
                    standard,
                    legendarios
            );

            return standard+legendarios>0;
        }catch(Exception e){
            LOGGER.error(
                    "[NairaSpawn] Error leyendo los JSON base de Pixelmon.",
                    e
            );
            return false;
        }
    }

    private static int leerDirectorioJson(
            JarFile jar,
            String ruta,
            String origen,
            Categorias categorias,
            Map<String,List<PokemonSpawnRule>> resultado,
            Map<String,List<PokemonSpawnRule>> resultadoPorForma
    ){
        int total=0;

        Enumeration<JarEntry> entries=
                jar.entries();

        while(entries.hasMoreElements()){
            JarEntry entry=
                    entries.nextElement();

            if(entry==null||
                    entry.isDirectory()){

                continue;
            }

            String nombre=
                    entry.getName();

            if(nombre==null||
                    !nombre.startsWith(ruta)||
                    !nombre.toLowerCase(Locale.ROOT)
                            .endsWith(".json")){

                continue;
            }

            try(Reader reader=
                        new InputStreamReader(
                                jar.getInputStream(entry),
                                StandardCharsets.UTF_8
                        )){

                JsonElement raiz=
                        new JsonParser()
                                .parse(reader);

                if(raiz==null||
                        !raiz.isJsonObject()){

                    continue;
                }

                total+=
                        cargarSpawnSetJson(
                                raiz.getAsJsonObject(),
                                origen,
                                categorias,
                                resultado,
                                resultadoPorForma
                        );
            }catch(Exception e){
                LOGGER.warn(
                        "[NairaSpawn] JSON omitido: {} | {}",
                        nombre,
                        e.getClass().getSimpleName()
                );
            }
        }

        return total;
    }

    private static int cargarSpawnSetJson(
            JsonObject root,
            String origen,
            Categorias categorias,
            Map<String,List<PokemonSpawnRule>> resultado,
            Map<String,List<PokemonSpawnRule>> resultadoPorForma
    ){
        JsonArray infos=
                obtenerArray(
                        root,
                        "spawnInfos"
                );

        if(infos==null){
            return 0;
        }

        int total=0;

        for(JsonElement elemento:infos){
            if(elemento==null||
                    !elemento.isJsonObject()){

                continue;
            }

            JsonObject info=
                    elemento.getAsJsonObject();

            String typeId=
                    obtenerString(
                            info,
                            "typeID"
                    );

            if(!typeId.isEmpty()&&
                    !typeId.equalsIgnoreCase(
                            "pokemon"
                    )){

                continue;
            }

            List<SpecBase> specs=
                    leerSpecs(
                            info
                    );

            if(specs.isEmpty()){
                continue;
            }

            JsonObject condition=
                    obtenerObjeto(
                            info,
                            "condition"
                    );

            List<String> horarios=
                    leerListaTexto(
                            condition,
                            "times"
                    );

            List<String> climas=
                    leerListaTexto(
                            condition,
                            "weathers"
                    );

            List<String> biomasRaw=
                    leerPrimeraLista(
                            condition,
                            "stringBiomes",
                            "biomes"
                    );

            List<String> bloquesRaw=
                    leerPrimeraLista(
                            condition,
                            "baseBlocks",
                            "stringBaseBlocks"
                    );

            List<String> biomas=
                    expandirCategorias(
                            biomasRaw,
                            categorias.biomas
                    );

            List<String> bloques=
                    expandirCategorias(
                            bloquesRaw,
                            categorias.bloques
                    );

            Integer minY=
                    obtenerEntero(
                            condition,
                            "minY"
                    );

            Integer maxY=
                    obtenerEntero(
                            condition,
                            "maxY"
                    );

            Integer luna=
                    obtenerEntero(
                            condition,
                            "moonPhase"
                    );

            for(SpecBase spec:specs){
                if(spec.pokemon.isEmpty()){
                    continue;
                }

                PokemonSpawnRule regla=
                        new PokemonSpawnRule(
                                spec.pokemon,
                                spec.forma,
                                spec.raw,
                                normalizarHorarios(horarios),
                                normalizarBiomas(biomas),
                                normalizarClimas(climas),
                                minY,
                                maxY,
                                luna,
                                normalizarBloques(bloques),
                                origen
                        );

                agregarRegla(
                        regla,
                        resultado,
                        resultadoPorForma
                );

                total++;
            }
        }

        return total;
    }

    private static List<SpecBase> leerSpecs(
            JsonObject info
    ){
        List<SpecBase> resultado=
                new ArrayList<>();

        if(info==null){
            return resultado;
        }

        JsonElement spec=
                info.get("spec");

        if(spec!=null&&
                !spec.isJsonNull()){

            SpecBase parsed=
                    parsearSpec(spec);

            if(parsed!=null){
                resultado.add(parsed);
            }

            return resultado;
        }

        JsonArray specs=
                obtenerArray(
                        info,
                        "specs"
                );

        if(specs==null){
            return resultado;
        }

        for(JsonElement element:specs){
            SpecBase parsed=
                    parsearSpec(element);

            if(parsed!=null){
                resultado.add(parsed);
            }
        }

        return resultado;
    }

    private static SpecBase parsearSpec(
            JsonElement element
    ){
        if(element==null||
                element.isJsonNull()){

            return null;
        }

        if(element.isJsonObject()){
            JsonObject obj=
                    element.getAsJsonObject();

            String pokemon=
                    primeroNoVacio(
                            obtenerString(
                                    obj,
                                    "name"
                            ),
                            obtenerString(
                                    obj,
                                    "species"
                            ),
                            obtenerString(
                                    obj,
                                    "pokemon"
                            )
                    );

            String forma=
                    primeroNoVacio(
                            obtenerString(
                                    obj,
                                    "form"
                            ),
                            obtenerString(
                                    obj,
                                    "forme"
                            )
                    );

            if(pokemon.isEmpty()){
                return null;
            }

            return new SpecBase(
                    pokemon,
                    forma,
                    element.toString()
            );
        }

        if(!element.isJsonPrimitive()){
            return null;
        }

        String raw;

        try{
            raw=
                    element.getAsString();
        }catch(Exception e){
            return null;
        }

        if(raw==null||
                raw.trim().isEmpty()){

            return null;
        }

        String pokemon="";
        String forma="";

        String[] partes=
                raw.trim()
                        .split("\\s+");

        for(String parte:partes){
            if(parte==null||
                    parte.trim().isEmpty()){

                continue;
            }

            String lower=
                    parte.toLowerCase(
                            Locale.ROOT
                    );

            if(lower.startsWith("species:")){
                pokemon=
                        valorDespuesDosPuntos(
                                parte
                        );
            }else if(lower.startsWith("name:")&&
                    pokemon.isEmpty()){

                pokemon=
                        valorDespuesDosPuntos(
                                parte
                        );
            }else if(lower.startsWith("form:")){
                forma=
                        valorDespuesDosPuntos(
                                parte
                        );
            }
        }

        if(pokemon.isEmpty()){
            String primero=
                    partes[0];

            if(primero!=null&&
                    !primero.contains(":")){

                pokemon=primero;
            }
        }

        if(pokemon.isEmpty()){
            return null;
        }

        return new SpecBase(
                pokemon,
                forma,
                raw
        );
    }

    private static Categorias leerCategorias(
            JarFile jar
    ){
        Categorias categorias=
                new Categorias();

        JarEntry entry=
                jar.getJarEntry(
                        RUTA_CONFIG
                );

        if(entry==null){
            return categorias;
        }

        try(Reader reader=
                    new InputStreamReader(
                            jar.getInputStream(entry),
                            StandardCharsets.UTF_8
                    )){

            JsonElement raiz=
                    new JsonParser()
                            .parse(reader);

            if(raiz==null||
                    !raiz.isJsonObject()){

                return categorias;
            }

            JsonObject root=
                    raiz.getAsJsonObject();

            categorias.biomas=
                    leerMapaCategorias(
                            obtenerObjeto(
                                    root,
                                    "biomeCategories"
                            )
                    );

            categorias.bloques=
                    leerMapaCategorias(
                            obtenerObjeto(
                                    root,
                                    "blockCategories"
                            )
                    );

            LOGGER.info(
                    "[NairaSpawn] Categorías base | biomas={} | bloques={}",
                    categorias.biomas.size(),
                    categorias.bloques.size()
            );
        }catch(Exception e){
            LOGGER.warn(
                    "[NairaSpawn] No se pudieron leer las categorías del Better Spawner.",
                    e
            );
        }

        return categorias;
    }

    private static Map<String,List<String>> leerMapaCategorias(
            JsonObject object
    ){
        Map<String,List<String>> resultado=
                new LinkedHashMap<>();

        if(object==null){
            return resultado;
        }

        for(Map.Entry<String,JsonElement> entry:
                object.entrySet()){

            if(entry.getKey()==null||
                    entry.getValue()==null||
                    !entry.getValue().isJsonArray()){

                continue;
            }

            List<String> valores=
                    leerArrayTexto(
                            entry.getValue()
                                    .getAsJsonArray()
                    );

            resultado.put(
                    normalizarCategoria(
                            entry.getKey()
                    ),
                    valores
            );
        }

        return resultado;
    }

    private static List<String> expandirCategorias(
            List<String> valores,
            Map<String,List<String>> categorias
    ){
        List<String> resultado=
                new ArrayList<>();

        if(valores==null){
            return resultado;
        }

        for(String valor:valores){
            expandirCategoria(
                    valor,
                    categorias,
                    resultado,
                    new HashSet<String>()
            );
        }

        return resultado;
    }

    private static void expandirCategoria(
            String valor,
            Map<String,List<String>> categorias,
            List<String> resultado,
            Set<String> visitados
    ){
        if(valor==null){
            return;
        }

        String limpio=
                valor.trim();

        if(limpio.isEmpty()){
            return;
        }

        String key=
                normalizarCategoria(
                        limpio
                );

        List<String> expansion=
                categorias.get(key);

        if(expansion==null){
            agregarUnico(
                    resultado,
                    limpio
            );
            return;
        }

        if(!visitados.add(key)){
            return;
        }

        for(String hijo:expansion){
            expandirCategoria(
                    hijo,
                    categorias,
                    resultado,
                    visitados
            );
        }

        visitados.remove(key);
    }

    private static void agregarRegla(
            PokemonSpawnRule regla,
            Map<String,List<PokemonSpawnRule>> resultado,
            Map<String,List<PokemonSpawnRule>> resultadoPorForma
    ){
        String pokemonKey=
                normalizar(
                        regla.getPokemon()
                );

        if(pokemonKey.isEmpty()){
            return;
        }

        resultado
                .computeIfAbsent(
                        pokemonKey,
                        k->new ArrayList<>()
                )
                .add(regla);

        String formaKey=
                normalizarForma(
                        regla.getForma()
                );

        resultadoPorForma
                .computeIfAbsent(
                        pokemonKey+"|"+formaKey,
                        k->new ArrayList<>()
                )
                .add(regla);
    }

    private static void cargarSetsRuntime(
            List<SpawnSet> sets,
            String origen,
            Map<String,List<PokemonSpawnRule>> resultado,
            Map<String,List<PokemonSpawnRule>> resultadoPorForma
    ){
        if(sets==null)return;

        for(SpawnSet set:sets){
            if(set==null)continue;

            for(SpawnInfo info:set){
                if(!(info instanceof SpawnInfoPokemon)){
                    continue;
                }

                SpawnInfoPokemon spawn=
                        (SpawnInfoPokemon)info;

                if(spawn.getSpecies()==null){
                    continue;
                }

                String pokemon=
                        spawn.getSpecies().getName();

                String spec=
                        spawn.getPokemonSpec()==null
                                ?"species:"+pokemon
                                :spawn.getPokemonSpec().toString();

                String forma=
                        extraerValorSpec(
                                spec,
                                "form"
                        );

                SpawnCondition condition=
                        info.condition;

                PokemonSpawnRule regla=
                        new PokemonSpawnRule(
                                pokemon,
                                forma,
                                spec,
                                leerHorariosRuntime(condition),
                                leerBiomasRuntime(condition),
                                leerClimasRuntime(condition),
                                condition==null
                                        ?null
                                        :condition.minY,
                                condition==null
                                        ?null
                                        :condition.maxY,
                                condition==null
                                        ?null
                                        :condition.moonPhase,
                                leerBloquesRuntime(condition),
                                origen
                        );

                agregarRegla(
                        regla,
                        resultado,
                        resultadoPorForma
                );
            }
        }
    }

    private static List<String> leerHorariosRuntime(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.times==null){

            return resultado;
        }

        for(Object tiempo:condition.times){
            if(tiempo==null)continue;

            agregarUnico(
                    resultado,
                    normalizarTiempo(
                            String.valueOf(tiempo)
                    )
            );
        }

        return resultado;
    }

    private static List<String> leerBiomasRuntime(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.biomes==null){

            return resultado;
        }

        for(ResourceLocation bioma:condition.biomes){
            if(bioma==null)continue;

            agregarUnico(
                    resultado,
                    bioma.toString()
            );
        }

        return resultado;
    }

    private static List<String> leerClimasRuntime(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.cachedWeathers==null){

            return resultado;
        }

        for(Object clima:condition.cachedWeathers){
            if(clima==null)continue;

            agregarUnico(
                    resultado,
                    normalizarClima(
                            String.valueOf(clima)
                    )
            );
        }

        return resultado;
    }

    private static List<String> leerBloquesRuntime(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.cachedBaseBlocks==null){

            return resultado;
        }

        for(Block bloque:condition.cachedBaseBlocks){
            if(bloque==null||
                    bloque.getRegistryName()==null){

                continue;
            }

            agregarUnico(
                    resultado,
                    bloque.getRegistryName().toString()
            );
        }

        return resultado;
    }

    private static File localizarJarPixelmon(){
        try{
            File modsDir=
                    FMLPaths.MODSDIR
                            .get()
                            .toFile();

            if(!modsDir.isDirectory()){
                return null;
            }

            File[] archivos=
                    modsDir.listFiles();

            if(archivos==null){
                return null;
            }

            for(File archivo:archivos){
                if(archivo==null||
                        !archivo.isFile()){

                    continue;
                }

                String nombre=
                        archivo.getName()
                                .toLowerCase(
                                        Locale.ROOT
                                );

                if(!nombre.endsWith(".jar")||
                        !nombre.contains("pixelmon")){

                    continue;
                }

                if(contieneSpawnBase(
                        archivo
                )){
                    return archivo;
                }
            }

            for(File archivo:archivos){
                if(archivo==null||
                        !archivo.isFile()||
                        !archivo.getName()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .endsWith(".jar")){

                    continue;
                }

                if(contieneSpawnBase(
                        archivo
                )){
                    return archivo;
                }
            }
        }catch(Exception e){
            LOGGER.error(
                    "[NairaSpawn] Error localizando el JAR de Pixelmon.",
                    e
            );
        }

        return null;
    }

    private static boolean contieneSpawnBase(
            File archivo
    ){
        try(JarFile jar=
                    new JarFile(
                            archivo
                    )){

            Enumeration<JarEntry> entries=
                    jar.entries();

            while(entries.hasMoreElements()){
                JarEntry entry=
                        entries.nextElement();

                if(entry==null||
                        entry.isDirectory()){

                    continue;
                }

                String nombre=
                        entry.getName();

                if(nombre!=null&&
                        (nombre.startsWith(
                                RUTA_STANDARD
                        )||
                                nombre.startsWith(
                                        RUTA_LEGENDARIOS
                                ))){

                    return true;
                }
            }
        }catch(Exception ignored){}

        return false;
    }

    private static Map<String,List<PokemonSpawnRule>> hacerInmutable(
            Map<String,List<PokemonSpawnRule>> origen
    ){
        Map<String,List<PokemonSpawnRule>> resultado=
                new LinkedHashMap<>();

        for(Map.Entry<String,List<PokemonSpawnRule>> entry:
                origen.entrySet()){

            resultado.put(
                    entry.getKey(),
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    entry.getValue()
                            )
                    )
            );
        }

        return Collections.unmodifiableMap(resultado);
    }

    private static int contarReglas(
            Map<String,List<PokemonSpawnRule>> origen
    ){
        int total=0;

        for(List<PokemonSpawnRule> reglas:origen.values()){
            total+=reglas.size();
        }

        return total;
    }

    private static List<String> leerPrimeraLista(
            JsonObject obj,
            String primero,
            String segundo
    ){
        List<String> a=
                leerListaTexto(
                        obj,
                        primero
                );

        if(!a.isEmpty()){
            return a;
        }

        return leerListaTexto(
                obj,
                segundo
        );
    }

    private static List<String> leerListaTexto(
            JsonObject obj,
            String campo
    ){
        if(obj==null||
                campo==null){

            return new ArrayList<>();
        }

        JsonArray array=
                obtenerArray(
                        obj,
                        campo
                );

        return leerArrayTexto(
                array
        );
    }

    private static List<String> leerArrayTexto(
            JsonArray array
    ){
        List<String> resultado=
                new ArrayList<>();

        if(array==null){
            return resultado;
        }

        for(JsonElement element:array){
            if(element==null||
                    element.isJsonNull()){

                continue;
            }

            try{
                agregarUnico(
                        resultado,
                        element.getAsString()
                );
            }catch(Exception ignored){}
        }

        return resultado;
    }

    private static JsonArray obtenerArray(
            JsonObject obj,
            String campo
    ){
        if(obj==null||
                campo==null){

            return null;
        }

        JsonElement element=
                obj.get(campo);

        return element!=null&&
                element.isJsonArray()
                ?element.getAsJsonArray()
                :null;
    }

    private static JsonObject obtenerObjeto(
            JsonObject obj,
            String campo
    ){
        if(obj==null||
                campo==null){

            return null;
        }

        JsonElement element=
                obj.get(campo);

        return element!=null&&
                element.isJsonObject()
                ?element.getAsJsonObject()
                :null;
    }

    private static String obtenerString(
            JsonObject obj,
            String campo
    ){
        if(obj==null||
                campo==null){

            return "";
        }

        JsonElement element=
                obj.get(campo);

        if(element==null||
                element.isJsonNull()||
                !element.isJsonPrimitive()){

            return "";
        }

        try{
            return element
                    .getAsString()
                    .trim();
        }catch(Exception e){
            return "";
        }
    }

    private static Integer obtenerEntero(
            JsonObject obj,
            String campo
    ){
        if(obj==null||
                campo==null){

            return null;
        }

        JsonElement element=
                obj.get(campo);

        if(element==null||
                element.isJsonNull()||
                !element.isJsonPrimitive()){

            return null;
        }

        try{
            return element.getAsInt();
        }catch(Exception e){
            try{
                return Integer.parseInt(
                        element.getAsString()
                );
            }catch(Exception ignored){
                return null;
            }
        }
    }

    private static List<String> normalizarHorarios(
            List<String> horarios
    ){
        List<String> resultado=
                new ArrayList<>();

        if(horarios==null){
            return resultado;
        }

        for(String horario:horarios){
            agregarUnico(
                    resultado,
                    normalizarTiempo(
                            horario
                    )
            );
        }

        return resultado;
    }

    private static List<String> normalizarClimas(
            List<String> climas
    ){
        List<String> resultado=
                new ArrayList<>();

        if(climas==null){
            return resultado;
        }

        for(String clima:climas){
            agregarUnico(
                    resultado,
                    normalizarClima(
                            clima
                    )
            );
        }

        return resultado;
    }

    private static List<String> normalizarBiomas(
            List<String> biomas
    ){
        List<String> resultado=
                new ArrayList<>();

        if(biomas==null){
            return resultado;
        }

        for(String bioma:biomas){
            if(bioma==null)continue;

            String limpio=
                    bioma.trim();

            if(limpio.startsWith("#")){
                limpio=
                        limpio.substring(1);
            }

            agregarUnico(
                    resultado,
                    limpio
            );
        }

        return resultado;
    }

    private static List<String> normalizarBloques(
            List<String> bloques
    ){
        List<String> resultado=
                new ArrayList<>();

        if(bloques==null){
            return resultado;
        }

        for(String bloque:bloques){
            if(bloque==null)continue;

            String limpio=
                    bloque.trim();

            if(limpio.startsWith("#")){
                limpio=
                        limpio.substring(1);
            }

            agregarUnico(
                    resultado,
                    limpio
            );
        }

        return resultado;
    }

    private static String extraerValorSpec(
            String spec,
            String campo
    ){
        if(spec==null||campo==null){
            return "";
        }

        String prefijo=
                campo.toLowerCase(Locale.ROOT)+":";

        for(String parte:spec.trim().split("\\s+")){
            if(parte.toLowerCase(Locale.ROOT)
                    .startsWith(prefijo)){

                return parte
                        .substring(prefijo.length())
                        .trim();
            }
        }

        return "";
    }

    private static String valorDespuesDosPuntos(
            String parte
    ){
        if(parte==null){
            return "";
        }

        int i=
                parte.indexOf(':');

        if(i<0||
                i>=parte.length()-1){

            return "";
        }

        return parte
                .substring(i+1)
                .trim();
    }

    private static String primeroNoVacio(
            String... valores
    ){
        if(valores==null){
            return "";
        }

        for(String valor:valores){
            if(valor!=null&&
                    !valor.trim().isEmpty()){

                return valor.trim();
            }
        }

        return "";
    }

    private static String normalizarCategoria(
            String valor
    ){
        if(valor==null){
            return "";
        }

        String limpio=
                valor.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if(limpio.startsWith("#")){
            limpio=
                    limpio.substring(1);
        }

        return limpio
                .replace("_","")
                .replace("-","")
                .replace(" ","");
    }

    private static String normalizarForma(String forma){
        String key=
                normalizar(forma);

        if(key.isEmpty()||
                key.equals("base")){

            return "base";
        }

        return key;
    }

    private static void agregarUnico(
            List<String> lista,
            String valor
    ){
        if(valor==null)return;

        String limpio=
                valor.trim();

        if(!limpio.isEmpty()&&
                !lista.contains(limpio)){

            lista.add(limpio);
        }
    }

    private static String normalizarTiempo(String tiempo){
        if(tiempo==null)return "";

        switch(tiempo.trim()
                .toUpperCase(Locale.ROOT)){

            case "DAWN":
                return "Dawn";

            case "MORNING":
                return "Morning";

            case "DAY":
                return "Day";

            case "MIDDAY":
                return "Midday";

            case "AFTERNOON":
                return "Afternoon";

            case "DUSK":
                return "Dusk";

            case "NIGHT":
                return "Night";

            case "MIDNIGHT":
                return "Midnight";

            default:
                return tiempo.trim();
        }
    }

    private static String normalizarClima(String clima){
        if(clima==null)return "";

        String valor=
                clima.trim()
                        .toUpperCase(Locale.ROOT);

        if(valor.equals("RAINING")){
            return "RAIN";
        }

        if(valor.equals("THUNDER")||
                valor.equals("THUNDERING")){

            return "STORM";
        }

        return valor;
    }

    private static String normalizar(String texto){
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

        return resultado.toString();
    }

    private static final class SpecBase {
        private final String pokemon;
        private final String forma;
        private final String raw;

        private SpecBase(
                String pokemon,
                String forma,
                String raw
        ){
            this.pokemon=
                    pokemon==null
                            ?""
                            :pokemon;

            this.forma=
                    forma==null
                            ?""
                            :forma;

            this.raw=
                    raw==null
                            ?""
                            :raw;
        }
    }

    private static final class Categorias {
        private Map<String,List<String>> biomas=
                new LinkedHashMap<>();

        private Map<String,List<String>> bloques=
                new LinkedHashMap<>();
    }
}
