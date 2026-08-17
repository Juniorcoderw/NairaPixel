package com.hjunior.nairapixel.client.dex.spawn;

import com.hjunior.nairapixel.client.legend.LegendaryEnvironmentReader;
import com.hjunior.nairapixel.client.legend.MinecraftTimeReader;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class NairaDexSpawnEvaluator {
    private NairaDexSpawnEvaluator(){}

    public static Evaluacion evaluar(
            List<PokemonSpawnRule> reglas
    ){
        List<PokemonSpawnRule> origen=
                reglas==null
                        ?Collections.emptyList()
                        :reglas;

        LegendaryEnvironmentReader.Estado entorno=
                LegendaryEnvironmentReader.leer();

        List<ResultadoRegla> resultados=
                new ArrayList<>();

        boolean algunaCompatible=false;

        for(PokemonSpawnRule regla:origen){
            if(regla==null)continue;

            ResultadoRegla resultado=
                    evaluarRegla(
                            regla,
                            entorno
                    );

            resultados.add(resultado);

            if(resultado.isCompatible()){
                algunaCompatible=true;
            }
        }

        return new Evaluacion(
                entorno,
                MinecraftTimeReader.getHoraActual(),
                resultados,
                algunaCompatible
        );
    }

    private static ResultadoRegla evaluarRegla(
            PokemonSpawnRule regla,
            LegendaryEnvironmentReader.Estado entorno
    ){
        boolean horario=
                !regla.tieneHorarios()||
                        horarioCompatible(
                                regla.getHorarios()
                        );

        boolean bioma=
                !regla.tieneBiomas()||
                        biomaCompatible(
                                regla.getBiomas(),
                                entorno.getBioma()
                        );

        boolean clima=
                !regla.tieneClima()||
                        climaCompatible(
                                regla.getClimas(),
                                entorno.getClima()
                        );

        boolean altura=
                alturaCompatible(
                        regla,
                        entorno.getY()
                );

        boolean luna=
                !regla.tieneFaseLunar()||
                        entorno.tieneFaseLunar()&&
                                regla.getFaseLunar()!=null&&
                                regla.getFaseLunar().intValue()
                                        ==entorno.getFaseLunar();

        boolean suelo=
                !regla.tieneBloquesBase()||
                        bloqueCompatible(
                                regla.getBloquesBase(),
                                entorno.getBloqueBase()
                        );

        return new ResultadoRegla(
                regla,
                horario,
                bioma,
                clima,
                altura,
                luna,
                suelo
        );
    }

    private static boolean horarioCompatible(
            List<String> horarios
    ){
        if(horarios==null||
                horarios.isEmpty()){

            return true;
        }

        for(String horario:horarios){
            if(MinecraftTimeReader.esPeriodoActivo(
                    horario
            )){
                return true;
            }
        }

        return false;
    }

    private static boolean biomaCompatible(
            List<String> biomas,
            String actual
    ){
        if(biomas==null||
                biomas.isEmpty()){

            return true;
        }

        String actualKey=
                normalizarId(actual);

        if(actualKey.isEmpty()){
            return false;
        }

        for(String bioma:biomas){
            if(normalizarId(bioma)
                    .equals(actualKey)){

                return true;
            }
        }

        return false;
    }

    private static boolean climaCompatible(
            List<String> climas,
            String actual
    ){
        if(climas==null||
                climas.isEmpty()){

            return true;
        }

        String actualKey=
                normalizarClima(actual);

        if(actualKey.isEmpty()){
            return false;
        }

        for(String clima:climas){
            if(normalizarClima(clima)
                    .equals(actualKey)){

                return true;
            }
        }

        return false;
    }

    private static boolean alturaCompatible(
            PokemonSpawnRule regla,
            int y
    ){
        if(regla.tieneMinY()&&
                regla.getMinY()!=null&&
                y<regla.getMinY()){

            return false;
        }

        if(regla.tieneMaxY()&&
                regla.getMaxY()!=null&&
                y>regla.getMaxY()){

            return false;
        }

        return true;
    }

    private static boolean bloqueCompatible(
            List<String> bloques,
            String actual
    ){
        if(bloques==null||
                bloques.isEmpty()){

            return true;
        }

        String actualKey=
                normalizarId(actual);

        if(actualKey.isEmpty()){
            return false;
        }

        for(String bloque:bloques){
            if(normalizarId(bloque)
                    .equals(actualKey)){

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

        if(valor.contains("THUNDER")||
                valor.contains("STORM")){

            return "STORM";
        }

        if(valor.contains("RAIN")){
            return "RAIN";
        }

        if(valor.contains("CLEAR")){
            return "CLEAR";
        }

        return valor;
    }

    private static String normalizarId(
            String valor
    ){
        if(valor==null)return "";

        return valor.trim()
                .toLowerCase(Locale.ROOT);
    }

    public static final class Evaluacion {
        private final LegendaryEnvironmentReader.Estado entorno;
        private final String hora;
        private final List<ResultadoRegla> resultados;
        private final boolean compatibleAhora;

        private Evaluacion(
                LegendaryEnvironmentReader.Estado entorno,
                String hora,
                List<ResultadoRegla> resultados,
                boolean compatibleAhora
        ){
            this.entorno=entorno;
            this.hora=hora==null?"--:--":hora;
            this.resultados=
                    Collections.unmodifiableList(
                            new ArrayList<>(
                                    resultados
                            )
                    );
            this.compatibleAhora=compatibleAhora;
        }

        public LegendaryEnvironmentReader.Estado getEntorno(){
            return entorno;
        }

        public String getHora(){
            return hora;
        }

        public List<ResultadoRegla> getResultados(){
            return resultados;
        }

        public ResultadoRegla getResultado(
                int indice
        ){
            if(indice<0||
                    indice>=resultados.size()){

                return null;
            }

            return resultados.get(indice);
        }

        public boolean isCompatibleAhora(){
            return compatibleAhora;
        }

        public int getCantidadCompatibles(){
            int total=0;

            for(ResultadoRegla resultado:resultados){
                if(resultado!=null&&
                        resultado.isCompatible()){

                    total++;
                }
            }

            return total;
        }

        public int getPrimeraReglaCompatible(){
            for(int i=0;i<resultados.size();i++){
                ResultadoRegla resultado=
                        resultados.get(i);

                if(resultado!=null&&
                        resultado.isCompatible()){

                    return i+1;
                }
            }

            return -1;
        }

        public int getTotalReglas(){
            return resultados.size();
        }

        public boolean tieneReglas(){
            return !resultados.isEmpty();
        }
    }

    public static final class ResultadoRegla {
        private final PokemonSpawnRule regla;
        private final boolean horario;
        private final boolean bioma;
        private final boolean clima;
        private final boolean altura;
        private final boolean luna;
        private final boolean suelo;

        private ResultadoRegla(
                PokemonSpawnRule regla,
                boolean horario,
                boolean bioma,
                boolean clima,
                boolean altura,
                boolean luna,
                boolean suelo
        ){
            this.regla=regla;
            this.horario=horario;
            this.bioma=bioma;
            this.clima=clima;
            this.altura=altura;
            this.luna=luna;
            this.suelo=suelo;
        }

        public PokemonSpawnRule getRegla(){
            return regla;
        }

        public boolean isHorario(){
            return horario;
        }

        public boolean isBioma(){
            return bioma;
        }

        public boolean isClima(){
            return clima;
        }

        public boolean isAltura(){
            return altura;
        }

        public boolean isLuna(){
            return luna;
        }

        public boolean isSuelo(){
            return suelo;
        }

        public boolean isCompatible(){
            return horario&&
                    bioma&&
                    clima&&
                    altura&&
                    luna&&
                    suelo;
        }
    }
}