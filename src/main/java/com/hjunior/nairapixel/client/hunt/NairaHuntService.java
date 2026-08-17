package com.hjunior.nairapixel.client.hunt;

import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService.Objetivo;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator.Evaluacion;
import com.hjunior.nairapixel.client.dex.spawn.NairaDexSpawnEvaluator.ResultadoRegla;
import com.hjunior.nairapixel.client.hunt.NairaHuntCondition.Tipo;
import com.hjunior.nairapixel.client.legend.LegendaryEnvironmentReader;
import com.hjunior.nairapixel.core.pixelmon.spawn.PokemonSpawnRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class NairaHuntService {
    private static final NairaHuntService INSTANCE=
            new NairaHuntService();

    private final NairaDexObjectivesService objectives=
            NairaDexObjectivesService.get();

    private NairaHuntSnapshot snapshot=
            NairaHuntSnapshot.sinObjetivo();

    private List<NairaHuntSnapshot> snapshots=
            Collections.emptyList();

    private NairaHuntService(){}

    public static NairaHuntService get(){
        return INSTANCE;
    }

    public NairaHuntSnapshot getSnapshot(){
        return snapshot;
    }

    public List<NairaHuntSnapshot> getSnapshots(){
        return snapshots;
    }

    public NairaHuntSnapshot getSnapshot(
            String pokemon,
            String forma
    ){
        for(NairaHuntSnapshot item:
                snapshots){

            if(item==null){
                continue;
            }

            if(mismaClave(
                    item.getPokemon(),
                    item.getForma(),
                    pokemon,
                    forma
            )){
                return item;
            }
        }

        return null;
    }

    public void actualizar(){
        List<Objetivo> activos=
                objectives.getObjetivosActivos();

        if(activos.isEmpty()){
            snapshot=
                    NairaHuntSnapshot.sinObjetivo();

            snapshots=
                    Collections.emptyList();

            return;
        }

        List<NairaHuntSnapshot> nuevos=
                new ArrayList<>();

        for(Objetivo objetivo:
                activos){

            nuevos.add(
                    crearSnapshot(
                            objetivo
                    )
            );
        }

        snapshots=
                Collections.unmodifiableList(
                        nuevos
                );

        Objetivo principal=
                objectives.getObjetivoPrincipal();

        NairaHuntSnapshot principalSnapshot=
                principal==null
                        ?null
                        :getSnapshot(
                                principal.getPokemon(),
                                principal.getForma()
                        );

        snapshot=
                principalSnapshot==null
                        ?nuevos.get(0)
                        :principalSnapshot;
    }

    private NairaHuntSnapshot crearSnapshot(
            Objetivo objetivo
    ){
        if(objetivo==null){
            return NairaHuntSnapshot.sinObjetivo();
        }

        Evaluacion evaluacion=
                NairaDexSpawnEvaluator.evaluar(
                        objetivo.getSpawns()
                );

        if(evaluacion==null||
                !evaluacion.tieneReglas()){

            return NairaHuntSnapshot.crear(
                    objetivo.getPokemon(),
                    objetivo.getForma(),
                    NairaHuntSnapshot.Estado.SIN_REGLAS,
                    0,
                    0,
                    Collections.emptyList(),
                    "Sin reglas de spawn"
            );
        }

        ResultadoRegla mejor=
                mejorRegla(
                        evaluacion
                );

        List<NairaHuntCondition> condiciones=
                crearCondiciones(
                        evaluacion,
                        mejor
                );

        boolean compatible=
                evaluacion.isCompatibleAhora();

        return NairaHuntSnapshot.crear(
                objetivo.getPokemon(),
                objetivo.getForma(),
                compatible
                        ?NairaHuntSnapshot.Estado.ZONA_COMPATIBLE
                        :NairaHuntSnapshot.Estado.CONDICIONES_INCOMPLETAS,
                evaluacion.getCantidadCompatibles(),
                evaluacion.getTotalReglas(),
                condiciones,
                crearResumen(
                        compatible,
                        condiciones
                )
        );
    }

    private ResultadoRegla mejorRegla(
            Evaluacion evaluacion
    ){
        ResultadoRegla mejor=null;
        int menorFallos=Integer.MAX_VALUE;

        for(ResultadoRegla resultado:
                evaluacion.getResultados()){

            if(resultado==null){
                continue;
            }

            int fallos=
                    contarFallos(
                            resultado
                    );

            if(mejor==null||
                    fallos<menorFallos){

                mejor=resultado;
                menorFallos=fallos;
            }

            if(fallos==0){
                break;
            }
        }

        return mejor;
    }

    private int contarFallos(
            ResultadoRegla resultado
    ){
        int fallos=0;

        if(!resultado.isHorario())fallos++;
        if(!resultado.isBioma())fallos++;
        if(!resultado.isAltura())fallos++;
        if(!resultado.isClima())fallos++;
        if(!resultado.isLuna())fallos++;
        if(!resultado.isSuelo())fallos++;

        return fallos;
    }

    private List<NairaHuntCondition> crearCondiciones(
            Evaluacion evaluacion,
            ResultadoRegla resultado
    ){
        if(resultado==null||
                resultado.getRegla()==null){

            return Collections.emptyList();
        }

        PokemonSpawnRule regla=
                resultado.getRegla();

        LegendaryEnvironmentReader.Estado entorno=
                evaluacion.getEntorno();

        List<NairaHuntCondition> condiciones=
                new ArrayList<>();

        if(regla.tieneHorarios()){
            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.HORARIO,
                            "Horario",
                            valorHora(
                                    evaluacion.getHora()
                            ),
                            listaCompacta(
                                    regla.getHorarios(),
                                    Valor.HORARIO,
                                    2
                            ),
                            resultado.isHorario()
                    )
            );
        }

        if(regla.tieneBiomas()){
            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.BIOMA,
                            "Bioma",
                            entorno!=null&&
                                    entorno.tieneBioma()
                                    ?traducirId(
                                            entorno.getBioma()
                                    )
                                    :"Sin datos",
                            listaCompacta(
                                    regla.getBiomas(),
                                    Valor.ID,
                                    2
                            ),
                            resultado.isBioma()
                    )
            );
        }

        if(regla.tieneMinY()||
                regla.tieneMaxY()){

            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.ALTURA,
                            "Altura",
                            entorno==null
                                    ?"Sin datos"
                                    :"Y "+entorno.getY(),
                            rangoAltura(
                                    regla
                            ),
                            resultado.isAltura()
                    )
            );
        }

        if(regla.tieneClima()){
            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.CLIMA,
                            "Clima",
                            entorno!=null&&
                                    entorno.tieneClima()
                                    ?traducirClima(
                                            entorno.getClima()
                                    )
                                    :"Sin datos",
                            listaCompacta(
                                    regla.getClimas(),
                                    Valor.CLIMA,
                                    2
                            ),
                            resultado.isClima()
                    )
            );
        }

        if(regla.tieneFaseLunar()){
            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.LUNA,
                            "Luna",
                            entorno!=null&&
                                    entorno.tieneFaseLunar()
                                    ?faseLunar(
                                            entorno.getFaseLunar()
                                    )
                                    :"Sin datos",
                            regla.getFaseLunar()==null
                                    ?"Cualquiera"
                                    :faseLunar(
                                            regla.getFaseLunar()
                                    ),
                            resultado.isLuna()
                    )
            );
        }

        if(regla.tieneBloquesBase()){
            condiciones.add(
                    new NairaHuntCondition(
                            Tipo.SUELO,
                            "Suelo",
                            entorno!=null&&
                                    entorno.tieneBloqueBase()
                                    ?traducirId(
                                            entorno.getBloqueBase()
                                    )
                                    :"Sin datos",
                            listaCompacta(
                                    regla.getBloquesBase(),
                                    Valor.ID,
                                    2
                            ),
                            resultado.isSuelo()
                    )
            );
        }

        return condiciones;
    }

    private String crearResumen(
            boolean compatible,
            List<NairaHuntCondition> condiciones
    ){
        if(compatible){
            return "Zona compatible · esperando aparición";
        }

        List<String> faltantes=
                new ArrayList<>();

        for(NairaHuntCondition condicion:
                condiciones){

            if(!condicion.isCumple()){
                faltantes.add(
                        condicion.getEtiqueta()+
                                ": "+
                                condicion.getRequerido()
                );
            }
        }

        if(faltantes.isEmpty()){
            return "Condiciones incompletas";
        }

        int limite=
                Math.min(
                        2,
                        faltantes.size()
                );

        String resumen=
                String.join(
                        " · ",
                        faltantes.subList(
                                0,
                                limite
                        )
                );

        if(faltantes.size()>limite){
            resumen+=
                    " · +"+
                            (faltantes.size()-limite);
        }

        return resumen;
    }

    private enum Valor{
        HORARIO,
        CLIMA,
        ID
    }

    private String listaCompacta(
            List<String> valores,
            Valor tipo,
            int max
    ){
        if(valores==null||
                valores.isEmpty()){

            return "Cualquiera";
        }

        List<String> visibles=
                new ArrayList<>();

        for(String valor:
                valores){

            if(valor==null||
                    valor.trim().isEmpty()){

                continue;
            }

            String traducido;

            if(tipo==Valor.HORARIO){
                traducido=
                        traducirHorario(
                                valor
                        );
            }else if(tipo==Valor.CLIMA){
                traducido=
                        traducirClima(
                                valor
                        );
            }else{
                traducido=
                        traducirId(
                                valor
                        );
            }

            if(!traducido.isEmpty()&&
                    !visibles.contains(
                            traducido
                    )){

                visibles.add(
                        traducido
                );
            }
        }

        if(visibles.isEmpty()){
            return "Cualquiera";
        }

        int limite=
                Math.max(
                        1,
                        Math.min(
                                max,
                                visibles.size()
                        )
                );

        String base=
                String.join(
                        " / ",
                        visibles.subList(
                                0,
                                limite
                        )
                );

        int restantes=
                visibles.size()-
                        limite;

        return restantes>0
                ?base+" +"+restantes
                :base;
    }

    private String rangoAltura(
            PokemonSpawnRule regla
    ){
        Integer min=
                regla.getMinY();

        Integer max=
                regla.getMaxY();

        if(min==null&&
                max==null){

            return "Cualquiera";
        }

        if(min!=null&&
                max!=null){

            return "Y "+
                    min+
                    "-"+
                    max;
        }

        if(min!=null){
            return "Y >= "+
                    min;
        }

        return "Y <= "+
                max;
    }

    private String valorHora(
            String hora
    ){
        return hora==null||
                hora.trim().isEmpty()||
                hora.equals("--:--")
                ?"Sin datos"
                :hora;
    }

    private String traducirHorario(
            String horario
    ){
        String key=
                normalizar(
                        horario
                );

        if(key.equals("dawn"))return "Amanecer";
        if(key.equals("day")||key.equals("daytime"))return "Día";
        if(key.equals("dusk"))return "Atardecer";
        if(key.equals("night")||key.equals("nighttime"))return "Noche";
        if(key.equals("morning"))return "Mañana";
        if(key.equals("afternoon"))return "Tarde";

        return formatear(
                horario
        );
    }

    private String traducirClima(
            String clima
    ){
        String key=
                normalizar(
                        clima
                );

        if(key.equals("clear"))return "Despejado";
        if(key.equals("rain")||key.equals("raining"))return "Lluvia";
        if(key.equals("thunder")||
                key.equals("thunderstorm")||
                key.equals("storm")){

            return "Tormenta";
        }

        return formatear(
                clima
        );
    }

    private String traducirId(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "";
        }

        String limpio=
                valor.trim();

        int dosPuntos=
                limpio.indexOf(':');

        if(dosPuntos>=0&&
                dosPuntos<limpio.length()-1){

            limpio=
                    limpio.substring(
                            dosPuntos+1
                    );
        }

        return formatear(
                limpio.replace('_',' ')
                        .replace('-',' ')
        );
    }

    private String formatear(
            String valor
    ){
        if(valor==null||
                valor.trim().isEmpty()){

            return "";
        }

        String[] partes=
                valor.trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .split("\\s+");

        StringBuilder out=
                new StringBuilder();

        for(String parte:
                partes){

            if(parte.isEmpty()){
                continue;
            }

            if(out.length()>0){
                out.append(' ');
            }

            out.append(
                    Character.toUpperCase(
                            parte.charAt(0)
                    )
            );

            if(parte.length()>1){
                out.append(
                        parte.substring(1)
                );
            }
        }

        return out.toString();
    }

    private String normalizar(
            String valor
    ){
        return valor==null
                ?""
                :valor.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );
    }

    private String faseLunar(
            int fase
    ){
        switch(fase){
            case 0:
                return "Luna llena";
            case 1:
                return "Gibosa menguante";
            case 2:
                return "Cuarto menguante";
            case 3:
                return "Menguante";
            case 4:
                return "Luna nueva";
            case 5:
                return "Creciente";
            case 6:
                return "Cuarto creciente";
            case 7:
                return "Gibosa creciente";
            default:
                return "Fase "+fase;
        }
    }
    private boolean mismaClave(
            String pokemonA,
            String formaA,
            String pokemonB,
            String formaB
    ){
        return normalizarClave(
                pokemonA,
                formaA
        ).equals(
                normalizarClave(
                        pokemonB,
                        formaB
                )
        );
    }

    private String normalizarClave(
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

        String f=
                forma==null||
                        forma.trim().isEmpty()||
                        forma.equalsIgnoreCase(
                                "base"
                        )
                        ?""
                        :forma.trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        return p+"|"+f;
    }

}
