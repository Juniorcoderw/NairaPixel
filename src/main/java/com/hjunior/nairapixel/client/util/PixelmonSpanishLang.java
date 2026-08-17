package com.hjunior.nairapixel.client.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PixelmonSpanishLang {
    private static Map<String,String> traducciones=
            Collections.emptyMap();

    private static boolean cargado;

    private PixelmonSpanishLang(){}

    public static synchronized void cargar(){
        if(cargado){
            return;
        }

        cargado=true;

        InputStream stream=null;

        try{
            ClassLoader loader=
                    Thread.currentThread()
                            .getContextClassLoader();

            if(loader!=null){
                stream=
                        loader.getResourceAsStream(
                                "assets/pixelmon/lang/es_es.json"
                        );
            }

            if(stream==null){
                stream=
                        PixelmonSpanishLang.class
                                .getClassLoader()
                                .getResourceAsStream(
                                        "assets/pixelmon/lang/es_es.json"
                                );
            }

            if(stream==null){
                return;
            }

            JsonObject json=
                    new JsonParser()
                            .parse(
                                    new InputStreamReader(
                                            stream,
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .getAsJsonObject();

            Map<String,String> datos=
                    new HashMap<>();

            for(Map.Entry<String,JsonElement> entry:
                    json.entrySet()){

                JsonElement valor=
                        entry.getValue();

                if(valor!=null&&
                        valor.isJsonPrimitive()){

                    datos.put(
                            entry.getKey(),
                            valor.getAsString()
                    );
                }
            }

            traducciones=
                    Collections.unmodifiableMap(
                            datos
                    );

        }catch(Exception ignored){
            traducciones=
                    Collections.emptyMap();

        }finally{
            if(stream!=null){
                try{
                    stream.close();
                }catch(Exception ignored){}
            }
        }
    }

    public static String traducir(
            String key,
            String fallback
    ){
        if(!cargado){
            cargar();
        }

        if(key==null||
                key.trim().isEmpty()){

            return fallback==null
                    ?""
                    :fallback;
        }

        String valor=
                traducciones.get(
                        key
                );

        if(valor==null||
                valor.trim().isEmpty()){

            return fallback==null
                    ?""
                    :fallback;
        }

        return valor;
    }

    public static boolean contiene(
            String key
    ){
        if(!cargado){
            cargar();
        }

        return key!=null&&
                traducciones.containsKey(
                        key
                );
    }

    public static int getCantidad(){
        if(!cargado){
            cargar();
        }

        return traducciones.size();
    }
}
