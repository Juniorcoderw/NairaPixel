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

public class LegendaryPredictor {
    public static List<Candidate> predecir(
            MinecraftTimeReader.VentanaTiempo ventana
    ){
        if(ventana==null)return Collections.emptyList();

        List<LegendarySpawnData> datos=
                PixelmonLegendaryProvider.getLegendarios();

        if(datos==null||datos.isEmpty()){
            return Collections.emptyList();
        }

        LegendaryEnvironmentReader.Estado entorno=
                LegendaryEnvironmentReader.leer();

        List<LegendarySpawnData> overworld=
                filtrarOverworld(datos);

        List<LegendarySpawnData> horario=
                filtrarHorario(
                        overworld,
                        ventana.getMinimo(),
                        ventana.getMaximo()
                );

        List<LegendarySpawnData> clima=
                filtrarClima(
                        horario,
                        entorno
                );

        List<LegendarySpawnData> luna=
                filtrarLuna(
                        clima,
                        entorno
                );

        Map<String,List<LegendarySpawnData>> grupos=
                agruparPorPokemon(luna);

        List<Candidate> resultado=
                new ArrayList<>();

        for(List<LegendarySpawnData> reglas:
                grupos.values()){

            Candidate candidato=
                    crearCandidato(
                            reglas,
                            ventana.getMinimo(),
                            ventana.getMaximo(),
                            entorno
                    );

            if(candidato!=null&&
                    candidato.getCobertura()>0){

                resultado.add(candidato);
            }
        }

        resultado.sort(
                new Comparator<Candidate>(){
                    @Override
                    public int compare(
                            Candidate a,
                            Candidate b
                    ){
                        int ranking=
                                Double.compare(
                                        b.puntajeInterno,
                                        a.puntajeInterno
                                );

                        if(ranking!=0)return ranking;

                        int cobertura=
                                Double.compare(
                                        b.getCobertura(),
                                        a.getCobertura()
                                );

                        if(cobertura!=0)return cobertura;

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

    private static List<LegendarySpawnData> filtrarOverworld(
            List<LegendarySpawnData> datos
    ){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        for(LegendarySpawnData regla:datos){
            if(regla==null)continue;

            if(esReglaOverworld(regla)){
                resultado.add(regla);
            }
        }

        return resultado;
    }

    private static boolean esReglaOverworld(
            LegendarySpawnData regla
    ){
        if(regla.getBiomes().isEmpty()){
            return true;
        }

        boolean encontro=false;

        for(String biome:regla.getBiomes()){
            if(biome==null||
                    biome.trim().isEmpty()){
                continue;
            }

            encontro=true;

            if(!esBiomaDimensionEspecial(biome)){
                return true;
            }
        }

        return !encontro;
    }

    private static boolean esBiomaDimensionEspecial(
            String biome
    ){
        String id=normalizar(biome);

        return id.contains("ultra_space")||
                id.contains("ultraspace")||
                id.startsWith("ultra")||
                id.contains("minecraft:nether")||
                id.contains("nether_wastes")||
                id.contains("crimson_forest")||
                id.contains("warped_forest")||
                id.contains("soul_sand_valley")||
                id.contains("basalt_deltas")||
                id.contains("minecraft:the_end")||
                id.contains("end_highlands")||
                id.contains("end_midlands")||
                id.contains("small_end_islands")||
                id.contains("end_barrens");
    }

    private static List<LegendarySpawnData> filtrarHorario(
            List<LegendarySpawnData> reglas,
            long inicio,
            long fin
    ){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        for(LegendarySpawnData regla:reglas){
            if(regla==null)continue;

            if(reglaCompatibleConVentana(
                    regla,
                    inicio,
                    fin
            )){
                resultado.add(regla);
            }
        }

        return resultado;
    }

    private static List<LegendarySpawnData> filtrarClima(
            List<LegendarySpawnData> reglas,
            LegendaryEnvironmentReader.Estado entorno
    ){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        for(LegendarySpawnData regla:reglas){
            if(regla==null)continue;

            if(climaCompatible(
                    regla,
                    entorno
            )){
                resultado.add(regla);
            }
        }

        return resultado;
    }

    private static List<LegendarySpawnData> filtrarLuna(
            List<LegendarySpawnData> reglas,
            LegendaryEnvironmentReader.Estado entorno
    ){
        List<LegendarySpawnData> resultado=
                new ArrayList<>();

        for(LegendarySpawnData regla:reglas){
            if(regla==null)continue;

            if(lunaCompatible(
                    regla,
                    entorno
            )){
                resultado.add(regla);
            }
        }

        return resultado;
    }

    private static boolean climaCompatible(
            LegendarySpawnData regla,
            LegendaryEnvironmentReader.Estado entorno
    ){
        if(!regla.tieneClima()){
            return true;
        }

        if(entorno==null||
                !entorno.tieneClima()){
            return true;
        }

        String actual=
                normalizarClima(
                        entorno.getClima()
                );

        for(String clima:regla.getWeathers()){
            if(clima==null)continue;

            if(normalizarClima(clima)
                    .equals(actual)){

                return true;
            }
        }

        return false;
    }

    private static boolean lunaCompatible(
            LegendarySpawnData regla,
            LegendaryEnvironmentReader.Estado entorno
    ){
        if(!regla.tieneFaseLunar()){
            return true;
        }

        if(entorno==null||
                !entorno.tieneFaseLunar()){
            return true;
        }

        return regla.getMoonPhase()!=null&&
                regla.getMoonPhase().intValue()
                        ==entorno.getFaseLunar();
    }

    private static Map<String,List<LegendarySpawnData>> agruparPorPokemon(
            List<LegendarySpawnData> datos
    ){
        Map<String,List<LegendarySpawnData>> grupos=
                new LinkedHashMap<>();

        for(LegendarySpawnData data:datos){
            if(data==null||
                    data.getPokemon()==null||
                    data.getPokemon().trim().isEmpty()){
                continue;
            }

            String key=
                    data.getPokemon()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            List<LegendarySpawnData> reglas=
                    grupos.get(key);

            if(reglas==null){
                reglas=new ArrayList<>();
                grupos.put(key,reglas);
            }

            reglas.add(data);
        }

        return grupos;
    }

    private static Candidate crearCandidato(
            List<LegendarySpawnData> reglas,
            long inicio,
            long fin,
            LegendaryEnvironmentReader.Estado entorno
    ){
        if(reglas==null||reglas.isEmpty()){
            return null;
        }

        double cobertura=
                calcularCobertura(
                        reglas,
                        inicio,
                        fin
                );

        if(cobertura<=0)return null;

        String nombre=
                reglas.get(0).getPokemon();

        Set<String> biomasVisuales=
                new LinkedHashSet<>();

        Set<String> biomasExactos=
                new LinkedHashSet<>();

        Set<String> condiciones=
                new LinkedHashSet<>();

        RankingRegla mejorRanking=null;

        for(LegendarySpawnData regla:reglas){
            if(!reglaCompatibleConVentana(
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

                biomasVisuales.add(
                        bioma.trim()
                );
            }

            for(String id:regla.getBiomes()){
                if(id!=null&&
                        !id.trim().isEmpty()){

                    biomasExactos.add(
                            id.trim()
                    );
                }
            }

            String condicion=
                    formatearCondicion(regla);

            if(!condicion.isEmpty()){
                condiciones.add(condicion);
            }

            RankingRegla ranking=
                    evaluarRegla(
                            regla,
                            cobertura,
                            entorno
                    );

            if(mejorRanking==null||
                    ranking.puntaje>
                            mejorRanking.puntaje){

                mejorRanking=ranking;
            }
        }

        if(mejorRanking==null){
            mejorRanking=
                    new RankingRegla(
                            cobertura*100.0,
                            false,
                            false,
                            true,
                            true
                    );
        }

        return new Candidate(
                nombre,
                new ArrayList<>(biomasVisuales),
                new ArrayList<>(biomasExactos),
                new ArrayList<>(condiciones),
                cobertura,
                mejorRanking.puntaje,
                mejorRanking.coincidenciaLocal,
                mejorRanking.bioma,
                mejorRanking.altura,
                mejorRanking.bloque
        );
    }

    private static RankingRegla evaluarRegla(
            LegendarySpawnData regla,
            double cobertura,
            LegendaryEnvironmentReader.Estado entorno
    ){
        double puntaje=
                cobertura*100.0;

        boolean tieneBioma=
                regla.tieneBiomas();

        boolean coincideBioma=
                false;

        boolean alturaOk=
                !regla.tieneAlturaMinima()&&
                        !regla.tieneAlturaMaxima();

        boolean bloqueOk=
                !regla.tieneBloquesBase();

        if(entorno==null){
            return new RankingRegla(
                    puntaje,
                    false,
                    false,
                    alturaOk,
                    bloqueOk
            );
        }

        /*
         * Primero importa el bioma.
         * Si la regla exige un bioma y no coincide,
         * Y y bloque no pueden mejorar el ranking.
         */
        if(tieneBioma&&
                entorno.tieneBioma()){

            coincideBioma=
                    biomaCompatible(
                            regla,
                            entorno.getBioma()
                    );

            if(coincideBioma){
                puntaje+=45.0;
            }
        }

        boolean evaluarCondicionesLocales=
                !tieneBioma||
                        coincideBioma;

        if(evaluarCondicionesLocales){
            if(regla.tieneAlturaMinima()||
                    regla.tieneAlturaMaxima()){

                alturaOk=
                        alturaCompatible(
                                regla,
                                entorno.getY()
                        );

                if(alturaOk){
                    puntaje+=20.0;
                }else{
                    puntaje-=10.0;
                }
            }

            if(regla.tieneBloquesBase()){
                if(entorno.tieneBloqueBase()){
                    bloqueOk=
                            bloqueCompatible(
                                    regla,
                                    entorno.getBloqueBase()
                            );
                }else{
                    bloqueOk=false;
                }

                if(bloqueOk){
                    puntaje+=25.0;
                }else{
                    puntaje-=10.0;
                }
            }
        }else{
            if(regla.tieneAlturaMinima()||
                    regla.tieneAlturaMaxima()){

                alturaOk=false;
            }

            if(regla.tieneBloquesBase()){
                bloqueOk=false;
            }
        }

        /*
         * Clima y luna ya pasaron los filtros fuertes,
         * pero una regla específica recibe un pequeño
         * bonus para desempatar.
         */
        if(regla.tieneClima()&&
                climaCompatible(
                        regla,
                        entorno
                )){

            puntaje+=15.0;
        }

        if(regla.tieneFaseLunar()&&
                lunaCompatible(
                        regla,
                        entorno
                )){

            puntaje+=15.0;
        }

        boolean coincidenciaLocal=
                tieneBioma&&
                        coincideBioma&&
                        alturaOk&&
                        bloqueOk;

        return new RankingRegla(
                puntaje,
                coincidenciaLocal,
                coincideBioma,
                alturaOk,
                bloqueOk
        );
    }

    private static boolean biomaCompatible(
            LegendarySpawnData regla,
            String biomaActual
    ){
        if(regla==null||
                !regla.tieneBiomas()||
                biomaActual==null||
                biomaActual.trim().isEmpty()){

            return false;
        }

        for(String requerido:
                regla.getBiomes()){

            if(biomaCompatible(
                    requerido,
                    biomaActual
            )){
                return true;
            }
        }

        return false;
    }

    private static boolean biomaCompatible(
            String requerido,
            String actual
    ){
        if(requerido==null||
                actual==null){
            return false;
        }

        String r=normalizar(requerido);
        String a=normalizar(actual);

        if(r.isEmpty()||a.isEmpty()){
            return false;
        }

        if(r.equals(a)){
            return true;
        }

        if(r.contains(":")){
            return false;
        }

        String pathActual=
                quitarNamespace(a);

        if(r.contains("mountain")){
            return pathActual.contains("mountain")||
                    pathActual.contains("gravelly");
        }

        if(r.contains("freezing")||
                r.contains("frozen")){

            return pathActual.contains("snow")||
                    pathActual.contains("frozen")||
                    pathActual.contains("ice");
        }

        if(r.contains("mesa")||
                r.contains("badland")){

            return pathActual.contains("mesa")||
                    pathActual.contains("badland");
        }

        if(r.contains("savanna")){
            return pathActual.contains("savanna");
        }

        if(r.equals("arid")||
                r.contains("arid")){

            return pathActual.contains("desert")||
                    pathActual.contains("badland")||
                    pathActual.contains("mesa")||
                    pathActual.contains("savanna");
        }

        if(r.contains("roofed")){
            return pathActual.contains("dark_forest")||
                    pathActual.contains("roofed");
        }

        if(r.contains("birch")){
            return pathActual.contains("birch");
        }

        if(r.contains("forest")){
            return pathActual.contains("forest");
        }

        if(r.contains("plains")){
            return pathActual.contains("plains");
        }

        if(r.contains("swamp")){
            return pathActual.contains("swamp");
        }

        if(r.contains("jungle")){
            return pathActual.contains("jungle");
        }

        if(r.contains("taiga")){
            return pathActual.contains("taiga");
        }

        if(r.contains("beach")){
            return pathActual.contains("beach");
        }

        if(r.contains("deep_ocean")){
            return pathActual.contains("deep")&&
                    pathActual.contains("ocean");
        }

        if(r.contains("warm_ocean")){
            return pathActual.contains("warm_ocean")&&
                    !pathActual.contains("lukewarm");
        }

        if(r.contains("lukewarm_ocean")){
            return pathActual.contains(
                    "lukewarm_ocean"
            );
        }

        if(r.contains("ocean")){
            return pathActual.contains("ocean");
        }

        return pathActual.equals(r);
    }

    private static boolean alturaCompatible(
            LegendarySpawnData regla,
            int y
    ){
        if(regla==null)return true;

        if(regla.tieneAlturaMinima()&&
                y<regla.getMinY()){
            return false;
        }

        if(regla.tieneAlturaMaxima()&&
                y>regla.getMaxY()){
            return false;
        }

        return true;
    }

    private static boolean bloqueCompatible(
            LegendarySpawnData regla,
            String bloqueActual
    ){
        if(regla==null||
                !regla.tieneBloquesBase()){
            return true;
        }

        if(bloqueActual==null||
                bloqueActual.trim().isEmpty()){
            return false;
        }

        String actual=
                normalizar(bloqueActual);

        for(String requerido:
                regla.getBaseBlocks()){

            if(requerido==null)continue;

            if(normalizar(requerido)
                    .equals(actual)){

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

        int total=(int)distancia+1;
        int validos=0;

        for(long i=0;i<=distancia;i++){
            long tick=
                    Math.floorMod(
                            inicio+i,
                            24000L
                    );

            if(algunSpawnCompatible(
                    reglas,
                    tick
            )){
                validos++;
            }
        }

        if(total<=0)return 0;

        return validos/(double)total;
    }

    private static boolean algunSpawnCompatible(
            List<LegendarySpawnData> reglas,
            long tick
    ){
        for(LegendarySpawnData regla:reglas){
            if(esCompatibleEnTick(
                    regla,
                    tick
            )){
                return true;
            }
        }

        return false;
    }

    private static boolean reglaCompatibleConVentana(
            LegendarySpawnData regla,
            long inicio,
            long fin
    ){
        if(regla==null)return false;

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

    private static boolean esCompatibleEnTick(
            LegendarySpawnData regla,
            long tick
    ){
        if(regla==null)return false;

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
                        normalizarClima(clima);

                if("CLEAR".equals(valor)){
                    agregarUnico(
                            climas,
                            "Despejado"
                    );
                }else if("RAIN".equals(valor)){
                    agregarUnico(
                            climas,
                            "Lluvia"
                    );
                }else if("STORM".equals(valor)){
                    agregarUnico(
                            climas,
                            "Tormenta"
                    );
                }else{
                    agregarUnico(
                            climas,
                            valor
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
                    "Y >= "+
                            regla.getMinY()
            );
        }

        if(regla.tieneAlturaMaxima()){
            partes.add(
                    "Y <= "+
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
        if(fase==null)return "";

        switch(fase){
            case 0:
                return "Luna llena";

            case 1:
                return "Luna gibosa menguante";

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
                return "Luna gibosa creciente";

            default:
                return "Luna "+fase;
        }
    }

    private static String formatearBloque(
            String bloque
    ){
        if(bloque==null)return "";

        String texto=
                quitarNamespace(
                        normalizar(bloque)
                );

        texto=texto.replace("_"," ");

        String[] palabras=
                texto.split(" ");

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

    private static String normalizar(
            String texto
    ){
        if(texto==null)return "";

        return texto.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ","_")
                .replace("-","_");
    }

    private static String quitarNamespace(
            String texto
    ){
        if(texto==null)return "";

        int separador=
                texto.indexOf(':');

        if(separador>=0&&
                separador+1<texto.length()){

            return texto.substring(
                    separador+1
            );
        }

        return texto;
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

    private static String unir(
            List<String> valores,
            String separador
    ){
        StringBuilder texto=
                new StringBuilder();

        for(String valor:valores){
            if(valor==null||
                    valor.isEmpty()){
                continue;
            }

            if(texto.length()>0){
                texto.append(separador);
            }

            texto.append(valor);
        }

        return texto.toString();
    }

    public static class Candidate {
        private final String nombre;
        private final List<String> biomasVisuales;
        private final List<String> biomasExactos;
        private final List<String> condiciones;
        private final double cobertura;

        private final double puntajeInterno;
        private final boolean coincidenciaLocal;
        private final boolean coincideBioma;
        private final boolean coincideAltura;
        private final boolean coincideBloque;

        private Candidate(
                String nombre,
                List<String> biomasVisuales,
                List<String> biomasExactos,
                List<String> condiciones,
                double cobertura,
                double puntajeInterno,
                boolean coincidenciaLocal,
                boolean coincideBioma,
                boolean coincideAltura,
                boolean coincideBloque
        ){
            this.nombre=
                    nombre==null
                            ?""
                            :nombre;

            this.biomasVisuales=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    biomasVisuales
                            )
                    );

            this.biomasExactos=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    biomasExactos
                            )
                    );

            this.condiciones=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    condiciones
                            )
                    );

            this.cobertura=cobertura;
            this.puntajeInterno=puntajeInterno;
            this.coincidenciaLocal=coincidenciaLocal;
            this.coincideBioma=coincideBioma;
            this.coincideAltura=coincideAltura;
            this.coincideBloque=coincideBloque;
        }

        public String getNombre(){
            return nombre;
        }

        public String getBioma(){
            if(biomasVisuales.isEmpty()){
                return "Anywhere";
            }

            return unir(
                    biomasVisuales,
                    " / "
            );
        }

        public List<String> getBiomasVisuales(){
            return biomasVisuales;
        }

        public List<String> getBiomas(){
            return biomasExactos;
        }

        public String getCondicion(){
            if(condiciones.isEmpty()){
                return "";
            }

            return unir(
                    condiciones,
                    " / "
            );
        }

        public List<String> getCondiciones(){
            return condiciones;
        }

        public double getCobertura(){
            return cobertura;
        }

        public boolean isCoincidenciaLocal(){
            return coincidenciaLocal;
        }

        public boolean isCoincideBioma(){
            return coincideBioma;
        }

        public boolean isCoincideAltura(){
            return coincideAltura;
        }

        public boolean isCoincideBloque(){
            return coincideBloque;
        }
    }

    private static class RankingRegla {
        private final double puntaje;
        private final boolean coincidenciaLocal;
        private final boolean bioma;
        private final boolean altura;
        private final boolean bloque;

        private RankingRegla(
                double puntaje,
                boolean coincidenciaLocal,
                boolean bioma,
                boolean altura,
                boolean bloque
        ){
            this.puntaje=puntaje;
            this.coincidenciaLocal=coincidenciaLocal;
            this.bioma=bioma;
            this.altura=altura;
            this.bloque=bloque;
        }
    }

}