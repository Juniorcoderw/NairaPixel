package com.hjunior.nairapixel.client.legend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LegendaryForecastService {
    private LegendaryForecastService(){}

    public static List<Candidate> predecir(
            long objetivoTicks,
            boolean sincronizado
    ){
        List<LegendarySpawnData> datos=
                PixelmonLegendaryProvider.getLegendarios();

        if(datos==null||datos.isEmpty()){
            return Collections.emptyList();
        }

        long margen=
                sincronizado
                        ?100L
                        :600L;

        long inicio=
                Math.floorMod(
                        objetivoTicks-margen,
                        24000L
                );

        long fin=
                Math.floorMod(
                        objetivoTicks+margen,
                        24000L
                );

        Map<String,List<LegendarySpawnData>> grupos=
                new LinkedHashMap<>();

        for(LegendarySpawnData regla:datos){
            if(regla==null||
                    !esOverworld(regla)||
                    !compatibleVentana(
                            regla,
                            inicio,
                            fin
                    )){
                continue;
            }

            String nombre=
                    regla.getPokemon();

            if(nombre==null||
                    nombre.trim().isEmpty()){
                continue;
            }

            String key=
                    nombre.trim()
                            .toLowerCase(Locale.ROOT);

            List<LegendarySpawnData> reglas=
                    grupos.get(key);

            if(reglas==null){
                reglas=new ArrayList<>();
                grupos.put(key,reglas);
            }

            reglas.add(regla);
        }

        List<Candidate> resultado=
                new ArrayList<>();

        for(List<LegendarySpawnData> reglas:
                grupos.values()){

            Candidate candidato=
                    crearCandidato(
                            reglas,
                            inicio,
                            fin
                    );

            if(candidato!=null){
                resultado.add(candidato);
            }
        }

        Collections.sort(
                resultado,
                new Comparator<Candidate>(){
                    @Override
                    public int compare(
                            Candidate a,
                            Candidate b
                    ){
                        int cobertura=
                                Double.compare(
                                        b.getCobertura(),
                                        a.getCobertura()
                                );

                        if(cobertura!=0){
                            return cobertura;
                        }

                        return a.getNombre()
                                .compareToIgnoreCase(
                                        b.getNombre()
                                );
                    }
                }
        );

        return Collections.unmodifiableList(
                resultado
        );
    }

    private static Candidate crearCandidato(
            List<LegendarySpawnData> reglas,
            long inicio,
            long fin
    ){
        if(reglas==null||
                reglas.isEmpty()){
            return null;
        }

        double cobertura=
                calcularCobertura(
                        reglas,
                        inicio,
                        fin
                );

        if(cobertura<=0){
            return null;
        }

        Set<String> biomas=
                new LinkedHashSet<>();

        Set<String> condiciones=
                new LinkedHashSet<>();

        for(LegendarySpawnData regla:reglas){
            if(!compatibleVentana(
                    regla,
                    inicio,
                    fin
            )){
                continue;
            }

            String bioma=
                    regla.getDisplayBiome();

            if(bioma!=null&&
                    !bioma.trim().isEmpty()){
                biomas.add(
                        bioma.trim()
                );
            }

            String condicion=
                    formatearCondicion(regla);

            if(!condicion.isEmpty()){
                condiciones.add(condicion);
            }
        }

        return new Candidate(
                reglas.get(0).getPokemon(),
                unir(
                        new ArrayList<>(biomas),
                        " / "
                ),
                unir(
                        new ArrayList<>(condiciones),
                        " / "
                ),
                cobertura
        );
    }

    private static boolean esOverworld(
            LegendarySpawnData regla
    ){
        List<String> biomas=
                regla.getBiomes();

        if(biomas==null||
                biomas.isEmpty()){
            return true;
        }

        boolean encontro=false;

        for(String bioma:biomas){
            if(bioma==null||
                    bioma.trim().isEmpty()){
                continue;
            }

            encontro=true;

            if(!esDimensionEspecial(bioma)){
                return true;
            }
        }

        return !encontro;
    }

    private static boolean esDimensionEspecial(
            String bioma
    ){
        String id=
                normalizar(bioma);

        return id.contains("ultra_space")||
                id.contains("ultraspace")||
                id.startsWith("ultra")||
                id.contains("nether_wastes")||
                id.contains("crimson_forest")||
                id.contains("warped_forest")||
                id.contains("soul_sand_valley")||
                id.contains("basalt_deltas")||
                id.contains("minecraft:nether")||
                id.contains("minecraft:the_end")||
                id.contains("end_highlands")||
                id.contains("end_midlands")||
                id.contains("small_end_islands")||
                id.contains("end_barrens");
    }

    private static boolean compatibleVentana(
            LegendarySpawnData regla,
            long inicio,
            long fin
    ){
        if(!regla.tieneHorarios()){
            return true;
        }

        for(String key:regla.getTimes()){
            PixelmonTimePeriod periodo=
                    PixelmonTimePeriod.fromKey(key);

            if(periodo!=null&&
                    periodo.intersecta(
                            inicio,
                            fin
                    )){
                return true;
            }
        }

        return false;
    }

    private static boolean compatibleTick(
            LegendarySpawnData regla,
            long tick
    ){
        if(!regla.tieneHorarios()){
            return true;
        }

        for(String key:regla.getTimes()){
            PixelmonTimePeriod periodo=
                    PixelmonTimePeriod.fromKey(key);

            if(periodo!=null&&
                    periodo.estaActivo(tick)){
                return true;
            }
        }

        return false;
    }

    private static double calcularCobertura(
            List<LegendarySpawnData> reglas,
            long inicio,
            long fin
    ){
        long distancia=
                Math.floorMod(
                        fin-inicio,
                        24000L
                );

        int total=
                (int)distancia+1;

        int validos=0;

        for(long i=0;i<=distancia;i++){
            long tick=
                    Math.floorMod(
                            inicio+i,
                            24000L
                    );

            for(LegendarySpawnData regla:reglas){
                if(compatibleTick(
                        regla,
                        tick
                )){
                    validos++;
                    break;
                }
            }
        }

        if(total<=0){
            return 0.0;
        }

        return validos/
                (double)total;
    }

    private static String formatearCondicion(
            LegendarySpawnData regla
    ){
        List<String> partes=
                new ArrayList<>();

        if(regla.tieneClima()){
            List<String> climas=
                    new ArrayList<>();

            for(String clima:
                    regla.getWeathers()){

                String valor=
                        clima==null
                                ?""
                                :clima.trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );

                if("CLEAR".equals(valor)){
                    agregarUnico(
                            climas,
                            "Despejado"
                    );
                }else if("RAIN".equals(valor)||
                        "RAINING".equals(valor)){
                    agregarUnico(
                            climas,
                            "Lluvia"
                    );
                }else if("STORM".equals(valor)||
                        "THUNDER".equals(valor)||
                        "THUNDERING".equals(valor)){
                    agregarUnico(
                            climas,
                            "Tormenta"
                    );
                }
            }

            if(!climas.isEmpty()){
                partes.add(
                        unir(
                                climas,
                                " / "
                        )
                );
            }
        }

        if(regla.tieneAlturaMinima()){
            partes.add(
                    "Y>="+
                            regla.getMinY()
            );
        }

        if(regla.tieneAlturaMaxima()){
            partes.add(
                    "Y<="+
                            regla.getMaxY()
            );
        }

        if(regla.tieneFaseLunar()){
            partes.add(
                    formatearLuna(
                            regla.getMoonPhase()
                    )
            );
        }

        if(regla.tieneBloquesBase()){
            List<String> bloques=
                    new ArrayList<>();

            for(String bloque:
                    regla.getBaseBlocks()){

                agregarUnico(
                        bloques,
                        formatearBloque(
                                bloque
                        )
                );
            }

            if(!bloques.isEmpty()){
                partes.add(
                        "Sobre "+
                                unir(
                                        bloques,
                                        ", "
                                )
                );
            }
        }

        return unir(
                partes,
                " · "
        );
    }

    private static String formatearLuna(
            Integer fase
    ){
        if(fase==null){
            return "";
        }

        switch(fase){
            case 0:
                return "Luna llena";
            case 1:
                return "Gibosa menguante";
            case 2:
                return "Cuarto menguante";
            case 3:
                return "Luna menguante";
            case 4:
                return "Luna nueva";
            case 5:
                return "Luna creciente";
            case 6:
                return "Cuarto creciente";
            case 7:
                return "Gibosa creciente";
            default:
                return "Luna "+fase;
        }
    }

    private static String formatearBloque(
            String bloque
    ){
        if(bloque==null){
            return "";
        }

        String texto=
                normalizar(bloque);

        int separador=
                texto.indexOf(':');

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
                texto.split(" ");

        StringBuilder resultado=
                new StringBuilder();

        for(String palabra:palabras){
            if(palabra.isEmpty()){
                continue;
            }

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

    private static void agregarUnico(
            List<String> lista,
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){
            return;
        }

        if(!lista.contains(valor)){
            lista.add(valor);
        }
    }

    private static String unir(
            List<String> valores,
            String separador
    ){
        StringBuilder texto=
                new StringBuilder();

        for(String valor:valores){
            if(valor==null||
                    valor.trim().isEmpty()){
                continue;
            }

            if(texto.length()>0){
                texto.append(separador);
            }

            texto.append(valor.trim());
        }

        return texto.toString();
    }

    private static String normalizar(
            String texto
    ){
        if(texto==null){
            return "";
        }

        return texto.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ","_")
                .replace("-","_");
    }

    public static final class Candidate {
        private final String nombre;
        private final String bioma;
        private final String condicion;
        private final double cobertura;

        private Candidate(
                String nombre,
                String bioma,
                String condicion,
                double cobertura
        ){
            this.nombre=
                    nombre==null?"":nombre;

            this.bioma=
                    bioma==null?"":bioma;

            this.condicion=
                    condicion==null?"":condicion;

            this.cobertura=
                    cobertura;
        }

        public String getNombre(){
            return nombre;
        }

        public String getBioma(){
            return bioma;
        }

        public String getCondicion(){
            return condicion;
        }

        public double getCobertura(){
            return cobertura;
        }
    }
}