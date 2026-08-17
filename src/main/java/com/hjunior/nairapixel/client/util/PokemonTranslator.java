package com.hjunior.nairapixel.client.util;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;

import java.util.List;
import java.util.Locale;

public final class PokemonTranslator {
    private PokemonTranslator(){}

    public static String tipo(Element tipo){
        if(tipo==null)return "-";

        return tipo(tipo.getName());
    }

    public static String tipo(String tipo){
        if(tipo==null||tipo.trim().isEmpty())return "-";

        switch(normalizar(tipo)){
            case "normal": return "Normal";
            case "fire": return "Fuego";
            case "water": return "Agua";
            case "electric": return "Eléctrico";
            case "grass": return "Planta";
            case "ice": return "Hielo";
            case "fighting": return "Lucha";
            case "poison": return "Veneno";
            case "ground": return "Tierra";
            case "flying": return "Volador";
            case "psychic": return "Psíquico";
            case "bug": return "Bicho";
            case "rock": return "Roca";
            case "ghost": return "Fantasma";
            case "dragon": return "Dragón";
            case "dark": return "Siniestro";
            case "steel": return "Acero";
            case "fairy": return "Hada";
            default: return formatear(tipo);
        }
    }

    public static String tipos(List<String> tipos){
        if(tipos==null||tipos.isEmpty()){
            return "-";
        }

        StringBuilder resultado=
                new StringBuilder();

        for(String tipo:tipos){
            if(tipo==null||tipo.trim().isEmpty()){
                continue;
            }

            if(resultado.length()>0){
                resultado.append(" / ");
            }

            resultado.append(
                    tipo(tipo)
            );
        }

        return resultado.length()==0
                ?"-"
                :resultado.toString();
    }

    public static String forma(String forma){
        if(forma==null||forma.trim().isEmpty()){
            return "Base";
        }

        switch(normalizar(forma)){
            case "base":
                return "Base";

            case "alolan":
            case "alola":
                return "Alola";

            case "galarian":
            case "galar":
                return "Galar";

            case "hisuian":
            case "hisui":
                return "Hisui";

            case "paldean":
            case "paldea":
                return "Paldea";

            case "battlebond":
                return "Battle Bond";

            default:
                return formatear(forma);
        }
    }

    public static String region(String region){
        if(region==null||region.trim().isEmpty()){
            return "";
        }

        switch(normalizar(region)){
            case "alola":
            case "alolan":
                return "Alola";

            case "galar":
            case "galarian":
                return "Galar";

            case "hisui":
            case "hisuian":
                return "Hisui";

            case "paldea":
            case "paldean":
                return "Paldea";

            default:
                return formatear(region);
        }
    }

    public static String naturaleza(Nature naturaleza){
        if(naturaleza==null)return "-";

        String ingles=
                formatear(
                        naturaleza.name()
                );

        switch(naturaleza.name()
                .toUpperCase(Locale.ROOT)){

            case "HARDY":
                return "Fuerte ("+ingles+")";

            case "LONELY":
                return "Huraña ("+ingles+")";

            case "BRAVE":
                return "Audaz ("+ingles+")";

            case "ADAMANT":
                return "Firme ("+ingles+")";

            case "NAUGHTY":
                return "Pícara ("+ingles+")";

            case "BOLD":
                return "Osada ("+ingles+")";

            case "DOCILE":
                return "Dócil ("+ingles+")";

            case "RELAXED":
                return "Plácida ("+ingles+")";

            case "IMPISH":
                return "Agitada ("+ingles+")";

            case "LAX":
                return "Floja ("+ingles+")";

            case "TIMID":
                return "Miedosa ("+ingles+")";

            case "HASTY":
                return "Activa ("+ingles+")";

            case "SERIOUS":
                return "Seria ("+ingles+")";

            case "JOLLY":
                return "Alegre ("+ingles+")";

            case "NAIVE":
                return "Ingenua ("+ingles+")";

            case "MODEST":
                return "Modesta ("+ingles+")";

            case "MILD":
                return "Afable ("+ingles+")";

            case "QUIET":
                return "Mansa ("+ingles+")";

            case "BASHFUL":
                return "Tímida ("+ingles+")";

            case "RASH":
                return "Alocada ("+ingles+")";

            case "CALM":
                return "Serena ("+ingles+")";

            case "GENTLE":
                return "Amable ("+ingles+")";

            case "SASSY":
                return "Grosera ("+ingles+")";

            case "CAREFUL":
                return "Cauta ("+ingles+")";

            case "QUIRKY":
                return "Rara ("+ingles+")";

            default:
                return ingles;
        }
    }

    public static String genero(Gender genero){
        if(genero==Gender.MALE){
            return "Macho";
        }

        if(genero==Gender.FEMALE){
            return "Hembra";
        }

        return "Sin género";
    }

    public static String crecimiento(String growth){
        if(growth==null||growth.trim().isEmpty()){
            return "-";
        }

        switch(normalizar(growth)){
            case "microscopic":
                return "Microscópico";

            case "pygmy":
                return "Pigmeo";

            case "runt":
                return "Diminuto";

            case "small":
                return "Pequeño";

            case "ordinary":
                return "Ordinario";

            case "huge":
                return "Enorme";

            case "giant":
                return "Gigante";

            case "enormous":
                return "Colosal";

            case "ginormous":
                return "Gigantesco";

            default:
                return formatear(growth);
        }
    }

    public static String grupoHuevo(String key){
        if(key==null||key.trim().isEmpty()){
            return "-";
        }

        switch(normalizar(key)){
            case "monster":
                return "Monstruo";

            case "water1":
                return "Agua 1";

            case "bug":
                return "Bicho";

            case "flying":
                return "Volador";

            case "field":
                return "Campo";

            case "fairy":
                return "Hada";

            case "grass":
                return "Planta";

            case "humanlike":
            case "humanshape":
                return "Humanoide";

            case "water3":
                return "Agua 3";

            case "mineral":
                return "Mineral";

            case "amorphous":
            case "indeterminate":
                return "Amorfo";

            case "water2":
                return "Agua 2";

            case "ditto":
                return "Ditto";

            case "dragon":
                return "Dragón";

            case "undiscovered":
                return "No descubierto";

            default:
                return formatear(key);
        }
    }

    public static String boss(String valor){
        if(valor==null||valor.trim().isEmpty()){
            return "";
        }

        String key=
                normalizar(valor);

        if(key.contains("uncommon")){
            return "Poco común";
        }

        if(key.contains("common")){
            return "Común";
        }

        if(key.contains("rare")){
            return "Raro";
        }

        if(key.contains("epic")){
            return "Épico";
        }

        if(key.contains("legendary")){
            return "Legendario";
        }

        if(key.contains("ultimate")){
            return "Ultimate";
        }

        return formatear(valor);
    }

    public static String habilidadIngles(String nombre){
        if(nombre==null||nombre.trim().isEmpty()){
            return "-";
        }

        StringBuilder resultado=
                new StringBuilder();

        char[] letras=
                nombre.replace("_"," ")
                        .toCharArray();

        for(int i=0;i<letras.length;i++){
            char c=letras[i];

            if(i>0&&
                    Character.isUpperCase(c)&&
                    Character.isLowerCase(letras[i-1])){

                resultado.append(' ');
            }

            resultado.append(c);
        }

        return formatear(
                resultado.toString()
        );
    }

    public static String iv(int valor){
        if(valor<0)return "-";
        if(valor==31)return "31 ★";

        return String.valueOf(valor);
    }

    public static boolean datoDisponible(int valor){
        return valor>=0;
    }

    public static String formatear(String texto){
        if(texto==null||texto.trim().isEmpty()){
            return "";
        }

        String limpio=
                texto.replace("_"," ")
                        .replace("-"," ")
                        .trim();

        String[] palabras=
                limpio.toLowerCase(Locale.ROOT)
                        .split("\\s+");

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

    public static String normalizar(String texto){
        if(texto==null)return "";

        return texto
                .toLowerCase(Locale.ROOT)
                .replace("enum.trainerboss.","")
                .replace("_","")
                .replace("-","")
                .replace(" ","");
    }
}