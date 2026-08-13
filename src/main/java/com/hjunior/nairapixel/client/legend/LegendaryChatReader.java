package com.hjunior.nairapixel.client.legend;

import com.hjunior.nairapixel.NairaPixel;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(
        modid=NairaPixel.MOD_ID,
        value=Dist.CLIENT
)
public class LegendaryChatReader {
    private static final Pattern SPAWN_PATTERN=
            Pattern.compile(
                    "(?i)\\bUn\\s+(.+?)\\s+apareci[oó]\\s+en\\s+(.+?)\\s+cerca\\s+de\\s+(.+?)\\s*[.!]?$"
            );

    @SubscribeEvent
    public static void onChat(
            ClientChatReceivedEvent event
    ){
        if(event.getMessage()==null)return;

        LegendaryEvent resultado=
                analizarMensaje(
                        event.getMessage().getString()
                );

        if(resultado!=null){
            LegendaryWatcher.registrarSpawn(
                    resultado
            );
        }
    }

    public static LegendaryEvent analizarMensaje(
            String texto
    ){
        if(texto==null||texto.trim().isEmpty()){
            return null;
        }

        String limpio=limpiarTexto(texto);
        String normalizado=normalizar(limpio);

        if(!esMensajeDeOak(normalizado)){
            return null;
        }

        Matcher matcher=
                SPAWN_PATTERN.matcher(limpio);

        if(!matcher.find()){
            return null;
        }

        String pokemon=
                limpiarCampo(matcher.group(1));

        String bioma=
                limpiarCampo(matcher.group(2));

        String jugador=
                limpiarCampo(matcher.group(3));

        if(pokemon.isEmpty()||
                bioma.isEmpty()||
                jugador.isEmpty()){
            return null;
        }

        if(!esLegendario(pokemon)){
            return null;
        }

        return new LegendaryEvent(
                pokemon,
                bioma,
                jugador
        );
    }

    private static boolean esMensajeDeOak(
            String texto
    ){
        return texto.contains("prof. oak")||
                texto.contains("prof oak");
    }

    private static boolean esLegendario(
            String nombre
    ){
        if(nombre==null||
                Pokedex.actualPokedex==null){
            return false;
        }

        String buscado=
                normalizarPokemon(nombre);

        for(Species especie:Pokedex.actualPokedex){
            if(especie==null)continue;

            String nombreEspecie=
                    normalizarPokemon(
                            especie.getName()
                    );

            if(!nombreEspecie.equals(buscado)){
                continue;
            }

            return especie.isLegendary()||
                    especie.isMythical();
        }

        return false;
    }

    private static String limpiarTexto(
            String texto
    ){
        if(texto==null)return "";

        return texto
                .replaceAll(
                        "(?i)§[0-9A-FK-OR]",
                        ""
                )
                .replace('\u00A0',' ')
                .replace("\u200B","")
                .replace("\u200C","")
                .replace("\u200D","")
                .trim();
    }

    private static String limpiarCampo(
            String texto
    ){
        if(texto==null)return "";

        return texto
                .trim()
                .replaceAll(
                        "^[»>:]+\\s*",
                        ""
                )
                .replaceAll(
                        "\\s*[.!]+$",
                        ""
                )
                .trim();
    }

    private static String normalizarPokemon(
            String texto
    ){
        return normalizar(texto)
                .replace(" ","")
                .replace("-","")
                .replace("'","");
    }

    private static String normalizar(
            String texto
    ){
        if(texto==null)return "";

        return Normalizer
                .normalize(
                        texto,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}","")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}