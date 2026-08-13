package com.hjunior.nairapixel.client.legend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.conditions.SpawnCondition;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class PixelmonLegendaryProvider {
    private static final String RUTA_LEGENDARIOS=
            "data/pixelmon/spawning/legendaries/";

    private static final Logger LOGGER=
            LogManager.getLogger("NairaLegend");

    private static List<LegendarySpawnData> cache;

    public static List<LegendarySpawnData> getLegendarios(){
        if(cache==null)cargar();
        return cache;
    }

    private static void cargar(){
        List<LegendarySpawnData> resultado=
                cargarDesdeJar();

        if(resultado.isEmpty()){
            LOGGER.warn(
                    "Recursos JAR no disponibles; usando PixelmonSpawning"
            );

            resultado=cargarDesdePixelmon();
        }

        cache=Collections.unmodifiableList(
                new ArrayList<>(resultado)
        );

        if(cache.isEmpty()){
            LOGGER.warn(
                    "No se cargaron reglas legendarias"
            );
        }else{
            LOGGER.info(
                    "Reglas normales cargadas: {}",
                    cache.size()
            );
        }
    }

    private static List<LegendarySpawnData> cargarDesdeJar(){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        try{
            File archivo=encontrarJarPixelmon();

            if(archivo==null){
                return resultado;
            }

            LOGGER.info(
                    "Pixelmon encontrado: {}",
                    archivo.getName()
            );

            int archivosLeidos=0;

            try(JarFile jar=new JarFile(archivo)){
                Enumeration<JarEntry> entries=
                        jar.entries();

                while(entries.hasMoreElements()){
                    JarEntry entry=
                            entries.nextElement();

                    if(entry.isDirectory())continue;

                    String nombre=entry.getName();

                    if(!nombre.startsWith(RUTA_LEGENDARIOS)||
                            !nombre.endsWith(".set.json")){
                        continue;
                    }

                    try(
                            InputStream stream=
                                    jar.getInputStream(entry);

                            Reader reader=
                                    new InputStreamReader(
                                            stream,
                                            StandardCharsets.UTF_8
                                    )
                    ){
                        leerSetJson(
                                reader,
                                resultado
                        );

                        archivosLeidos++;
                    }catch(Exception e){
                        LOGGER.warn(
                                "No se pudo leer recurso legendario: {}",
                                nombre,
                                e
                        );
                    }
                }
            }

            LOGGER.info(
                    "Fuente: Pixelmon JAR | archivos: {}",
                    archivosLeidos
            );

        }catch(Exception e){
            LOGGER.error(
                    "Error leyendo Pixelmon JAR",
                    e
            );
        }

        return resultado;
    }

    private static File encontrarJarPixelmon(){
        Path modsDir;

        try{
            modsDir=FMLPaths.MODSDIR.get();
        }catch(Exception e){
            return null;
        }

        if(modsDir==null||
                !Files.isDirectory(modsDir)){
            return null;
        }

        try(Stream<Path> archivos=Files.list(modsDir)){
            Iterator<Path> iterator=
                    archivos.iterator();

            while(iterator.hasNext()){
                Path path=iterator.next();

                if(!Files.isRegularFile(path)){
                    continue;
                }

                String nombre=
                        path.getFileName()
                                .toString()
                                .toLowerCase(Locale.ROOT);

                if(!nombre.endsWith(".jar")){
                    continue;
                }

                File archivo=path.toFile();

                try(JarFile jar=new JarFile(archivo)){
                    if(jar.getJarEntry(
                            RUTA_LEGENDARIOS+
                                    "rayquaza.set.json"
                    )!=null){
                        return archivo;
                    }
                }catch(Exception ignored){
                }
            }

        }catch(IOException e){
            LOGGER.warn(
                    "Error revisando directorio de mods",
                    e
            );
        }

        return null;
    }

    private static void leerSetJson(
            Reader reader,
            List<LegendarySpawnData> resultado
    ){
        JsonElement raiz=
                new JsonParser().parse(reader);

        if(raiz==null||
                !raiz.isJsonObject()){
            return;
        }

        JsonArray spawnInfos=
                obtenerArray(
                        raiz.getAsJsonObject(),
                        "spawnInfos"
                );

        if(spawnInfos==null)return;

        for(JsonElement elemento:spawnInfos){
            if(elemento==null||
                    !elemento.isJsonObject()){
                continue;
            }

            JsonObject spawn=
                    elemento.getAsJsonObject();

            String spec=
                    obtenerTexto(
                            spawn,
                            "spec"
                    );

            if(spec.isEmpty()||
                    esSpawnEspecial(spec)){
                continue;
            }

            String pokemon=
                    extraerPokemon(spec);

            if(pokemon.isEmpty())continue;

            JsonObject condition=
                    obtenerObjeto(
                            spawn,
                            "condition"
                    );

            List<String> tiempos=
                    leerTiempos(condition);

            List<String> biomas=
                    leerLista(
                            condition,
                            "stringBiomes"
                    );

            List<String> climas=
                    leerClimas(condition);

            List<String> bloques=
                    leerLista(
                            condition,
                            "baseBlocks"
                    );

            Integer minY=
                    leerEntero(
                            condition,
                            "minY"
                    );

            Integer maxY=
                    leerEntero(
                            condition,
                            "maxY"
                    );

            Integer moonPhase=
                    leerEntero(
                            condition,
                            "moonPhase"
                    );

            resultado.add(
                    new LegendarySpawnData(
                            pokemon,
                            resumirBioma(biomas),
                            tiempos,
                            biomas,
                            climas,
                            minY,
                            maxY,
                            moonPhase,
                            bloques
                    )
            );
        }
    }

    private static List<LegendarySpawnData> cargarDesdePixelmon(){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        if(PixelmonSpawning.legendaries==null||
                PixelmonSpawning.legendaries.isEmpty()){
            return resultado;
        }

        for(SpawnSet set:PixelmonSpawning.legendaries){
            if(set==null)continue;

            for(SpawnInfo info:set){
                if(!(info instanceof SpawnInfoPokemon)){
                    continue;
                }

                SpawnInfoPokemon pokemon=
                        (SpawnInfoPokemon)info;

                if(pokemon.getSpecies()==null)continue;
                if(esSpawnEspecial(pokemon))continue;

                SpawnCondition condition=
                        info.condition;

                List<String> tiempos=
                        obtenerTiemposPixelmon(condition);

                List<String> biomas=
                        obtenerBiomasPixelmon(condition);

                List<String> climas=
                        obtenerClimasPixelmon(condition);

                resultado.add(
                        new LegendarySpawnData(
                                pokemon.getSpecies().getName(),
                                resumirBioma(biomas),
                                tiempos,
                                biomas,
                                climas,
                                null,
                                null,
                                null,
                                Collections.emptyList()
                        )
                );
            }
        }

        if(!resultado.isEmpty()){
            LOGGER.info(
                    "Fuente: PixelmonSpawning"
            );
        }

        return resultado;
    }

    private static List<String> obtenerTiemposPixelmon(
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

    private static List<String> obtenerBiomasPixelmon(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.biomes==null){
            return resultado;
        }

        for(ResourceLocation biome:
                condition.biomes){

            if(biome==null)continue;

            agregarUnico(
                    resultado,
                    biome.toString()
            );
        }

        return resultado;
    }

    private static List<String> obtenerClimasPixelmon(
            SpawnCondition condition
    ){
        List<String> resultado=
                new ArrayList<>();

        if(condition==null||
                condition.cachedWeathers==null){
            return resultado;
        }

        for(Object weather:
                condition.cachedWeathers){

            if(weather==null)continue;

            agregarUnico(
                    resultado,
                    normalizarClima(
                            String.valueOf(weather)
                    )
            );
        }

        return resultado;
    }

    private static List<String> leerTiempos(
            JsonObject condition
    ){
        List<String> valores=
                leerLista(
                        condition,
                        "times"
                );

        List<String> resultado=
                new ArrayList<>();

        for(String tiempo:valores){
            agregarUnico(
                    resultado,
                    normalizarTiempo(tiempo)
            );
        }

        return resultado;
    }

    private static List<String> leerClimas(
            JsonObject condition
    ){
        List<String> valores=
                leerLista(
                        condition,
                        "weathers"
                );

        List<String> resultado=
                new ArrayList<>();

        for(String clima:valores){
            agregarUnico(
                    resultado,
                    normalizarClima(clima)
            );
        }

        return resultado;
    }

    private static List<String> leerLista(
            JsonObject objeto,
            String campo
    ){
        List<String> resultado=
                new ArrayList<>();

        if(objeto==null||
                !objeto.has(campo)||
                objeto.get(campo).isJsonNull()||
                !objeto.get(campo).isJsonArray()){
            return resultado;
        }

        JsonArray array=
                objeto.getAsJsonArray(campo);

        for(JsonElement elemento:array){
            if(elemento==null||
                    elemento.isJsonNull()){
                continue;
            }

            try{
                agregarUnico(
                        resultado,
                        elemento.getAsString()
                );
            }catch(Exception ignored){
            }
        }

        return resultado;
    }

    private static Integer leerEntero(
            JsonObject objeto,
            String campo
    ){
        if(objeto==null||
                !objeto.has(campo)||
                objeto.get(campo).isJsonNull()){
            return null;
        }

        try{
            return objeto
                    .get(campo)
                    .getAsInt();

        }catch(Exception e){
            return null;
        }
    }

    private static JsonArray obtenerArray(
            JsonObject objeto,
            String campo
    ){
        if(objeto==null||
                !objeto.has(campo)||
                objeto.get(campo).isJsonNull()||
                !objeto.get(campo).isJsonArray()){
            return null;
        }

        return objeto.getAsJsonArray(campo);
    }

    private static JsonObject obtenerObjeto(
            JsonObject objeto,
            String campo
    ){
        if(objeto==null||
                !objeto.has(campo)||
                objeto.get(campo).isJsonNull()||
                !objeto.get(campo).isJsonObject()){
            return null;
        }

        return objeto.getAsJsonObject(campo);
    }

    private static String obtenerTexto(
            JsonObject objeto,
            String campo
    ){
        if(objeto==null||
                !objeto.has(campo)||
                objeto.get(campo).isJsonNull()){
            return "";
        }

        try{
            return objeto
                    .get(campo)
                    .getAsString();

        }catch(Exception e){
            return "";
        }
    }

    private static String extraerPokemon(
            String spec
    ){
        if(spec==null)return "";

        String[] partes=
                spec.trim().split("\\s+");

        for(String parte:partes){
            String lower=
                    parte.toLowerCase(Locale.ROOT);

            if(lower.startsWith("species:")){
                return parte.substring(
                        "species:".length()
                ).trim();
            }
        }

        return "";
    }

    private static boolean esSpawnEspecial(
            SpawnInfoPokemon pokemon
    ){
        if(pokemon.getPokemonSpec()==null){
            return false;
        }

        return esSpawnEspecial(
                pokemon.getPokemonSpec().toString()
        );
    }

    private static boolean esSpawnEspecial(
            String spec
    ){
        if(spec==null)return false;

        String texto=
                spec.toLowerCase(Locale.ROOT);

        return texto.contains("palette:")||
                texto.contains("form:")||
                texto.contains("shiny:true");
    }

    private static String normalizarTiempo(
            String tiempo
    ){
        if(tiempo==null)return "";

        switch(tiempo
                .trim()
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

    private static String normalizarClima(
            String clima
    ){
        if(clima==null)return "";

        String valor=
                clima.trim()
                        .toUpperCase(Locale.ROOT);

        switch(valor){
            case "RAINING":
                return "RAIN";

            case "THUNDER":
            case "THUNDERING":
                return "STORM";

            default:
                return valor;
        }
    }

    private static String resumirBioma(
            List<String> biomas
    ){
        if(biomas==null||
                biomas.isEmpty()){
            return "Anywhere";
        }

        for(String biome:biomas){
            if(biome==null)continue;

            String id=
                    biome.toLowerCase(Locale.ROOT);

            if(id.contains("ultra")){
                return "Ultra Space";
            }

            if(id.contains("mountain")){
                return "Mountains";
            }
        }

        for(String biome:biomas){
            if(biome==null)continue;

            String id=
                    biome.toLowerCase(Locale.ROOT);

            if(id.contains("birch")){
                return "Birch Forest";
            }

            if(id.contains("dark_forest")||
                    id.contains("roofed")){
                return "Dark Forest";
            }

            if(id.contains("deep_ocean")){
                return "Deep Ocean";
            }

            if(id.contains("desert")){
                return "Desert";
            }

            if(id.contains("savanna")){
                return "Savanna";
            }

            if(id.contains("mesa")){
                return "Mesa";
            }

            if(id.contains("swamp")){
                return "Swamp";
            }

            if(id.contains("jungle")){
                return "Jungle";
            }

            if(id.contains("taiga")){
                return "Taiga";
            }

            if(id.contains("beach")){
                return "Beach";
            }

            if(id.contains("forest")){
                return "Forest";
            }

            if(id.contains("ocean")){
                return "Ocean";
            }

            if(id.contains("plains")){
                return "Plains";
            }

            if(id.contains("arid")){
                return "Arid";
            }

            if(id.contains("freezing")){
                return "Freezing";
            }
        }

        String primero=biomas.get(0);

        if(primero==null||
                primero.isEmpty()){
            return "Anywhere";
        }

        int separador=
                primero.indexOf(':');

        if(separador>=0){
            primero=
                    primero.substring(
                            separador+1
                    );
        }

        return formatear(primero);
    }

    private static void agregarUnico(
            List<String> lista,
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){
            return;
        }

        String limpio=valor.trim();

        if(!lista.contains(limpio)){
            lista.add(limpio);
        }
    }

    private static String formatear(
            String texto
    ){
        if(texto==null||
                texto.isEmpty()){
            return "";
        }

        String limpio=
                texto.replace("_"," ")
                        .replace("-"," ")
                        .toLowerCase(Locale.ROOT);

        String[] palabras=
                limpio.split(" ");

        StringBuilder resultado=
                new StringBuilder();

        for(String palabra:palabras){
            if(palabra.isEmpty())continue;

            if(resultado.length()>0){
                resultado.append(" ");
            }

            resultado.append(
                    Character.toUpperCase(
                            palabra.charAt(0)
                    )
            );

            if(palabra.length()>1){
                resultado.append(
                        palabra.substring(1)
                );
            }
        }

        return resultado.toString();
    }

    public static void recargar(){
        cache=null;
        cargar();
    }
}
