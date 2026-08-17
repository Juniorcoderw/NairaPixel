package com.hjunior.nairapixel.core.pixelmon.moves;

import com.hjunior.nairapixel.core.pixelmon.forms.PixelmonFormResolver;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.moves.Moves;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.enums.technicalmoves.ITechnicalMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PixelmonMoveProvider {
    private static final Map<String,List<PokemonMoveData>> cache=
            new HashMap<>();

    private PixelmonMoveProvider(){}

    public static List<PokemonMoveData> getMovimientos(String pokemon){
        return getMovimientos(pokemon,"");
    }

    public static List<PokemonMoveData> getMovimientos(
            String pokemon,
            String forma
    ){
        String key=crearKey(pokemon,forma);

        if(key.isEmpty()){
            return Collections.emptyList();
        }

        synchronized(cache){
            List<PokemonMoveData> guardado=cache.get(key);

            if(guardado!=null){
                return guardado;
            }
        }

        List<PokemonMoveData> resultado=
                cargar(pokemon,forma);

        synchronized(cache){
            cache.put(key,resultado);
        }

        return resultado;
    }

    private static List<PokemonMoveData> cargar(
            String pokemon,
            String forma
    ){
        Optional<Species> encontrado=
                PixelmonSpecies.fromNameOrDex(pokemon);

        if(!encontrado.isPresent()){
            return Collections.emptyList();
        }

        Species species=encontrado.get();

        Optional<Stats> statsEncontrados=
                PixelmonFormResolver.resolver(
                        species,
                        forma
                );

        if(!statsEncontrados.isPresent()){
            return Collections.emptyList();
        }

        Stats stats=statsEncontrados.get();

        if(stats.getMoves()==null){
            return Collections.emptyList();
        }

        Moves moves=stats.getMoves();

        Map<String,ConstructorMovimiento> mapa=
                new LinkedHashMap<>();

        if(moves.getAllMoves()!=null){
            for(ImmutableAttack ataque:moves.getAllMoves()){
                registrar(mapa,ataque);
            }
        }

        cargarNiveles(mapa,moves);
        cargarTecnicos(mapa,moves);

        cargarGrupo(
                mapa,
                moves.getTutorMoves(),
                "TUTOR"
        );

        cargarGrupo(
                mapa,
                moves.getEggMoves(),
                "EGG"
        );

        cargarGrupo(
                mapa,
                moves.getTransferMoves(),
                "TRANSFER"
        );

        List<PokemonMoveData> resultado=
                new ArrayList<>();

        for(ConstructorMovimiento constructor:mapa.values()){
            resultado.add(
                    constructor.crear()
            );
        }

        resultado.sort(
                Comparator.comparing(
                        PokemonMoveData::getNombre,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return Collections.unmodifiableList(resultado);
    }

    private static void cargarNiveles(
            Map<String,ConstructorMovimiento> mapa,
            Moves moves
    ){
        Map<Integer,Set<ImmutableAttack>> niveles=
                moves.getPokemonLevelUpMoves();

        if(niveles==null)return;

        for(Map.Entry<Integer,Set<ImmutableAttack>> entry:
                niveles.entrySet()){

            if(entry.getValue()==null)continue;

            for(ImmutableAttack ataque:entry.getValue()){
                ConstructorMovimiento constructor=
                        registrar(mapa,ataque);

                if(constructor==null)continue;

                constructor.agregarFuente(
                        new MoveLearnSource(
                                "LEVEL",
                                entry.getKey(),
                                null,
                                null
                        )
                );
            }
        }
    }

    private static void cargarTecnicos(
            Map<String,ConstructorMovimiento> mapa,
            Moves moves
    ){
        for(int generacion=1;generacion<=9;generacion++){
            Set<ITechnicalMove> tecnicos=
                    moves.getGenerationMoves(generacion);

            if(tecnicos==null)continue;

            for(ITechnicalMove tecnico:tecnicos){
                if(tecnico==null)continue;

                String key=
                        normalizar(
                                tecnico.getAttackName()
                        );

                ConstructorMovimiento constructor=
                        mapa.get(key);

                if(constructor==null)continue;

                constructor.agregarFuente(
                        new MoveLearnSource(
                                tecnico.prefix()
                                        .toUpperCase(Locale.ROOT),
                                null,
                                tecnico.getGeneration(),
                                tecnico.getId()
                        )
                );
            }
        }
    }

    private static void cargarGrupo(
            Map<String,ConstructorMovimiento> mapa,
            Set<ImmutableAttack> ataques,
            String metodo
    ){
        if(ataques==null)return;

        for(ImmutableAttack ataque:ataques){
            ConstructorMovimiento constructor=
                    registrar(mapa,ataque);

            if(constructor==null)continue;

            constructor.agregarFuente(
                    new MoveLearnSource(
                            metodo,
                            null,
                            null,
                            null
                    )
            );
        }
    }

    private static ConstructorMovimiento registrar(
            Map<String,ConstructorMovimiento> mapa,
            ImmutableAttack ataque
    ){
        if(ataque==null)return null;

        String key=
                normalizar(
                        ataque.getAttackName()
                );

        if(key.isEmpty()){
            return null;
        }

        ConstructorMovimiento existente=
                mapa.get(key);

        if(existente!=null){
            return existente;
        }

        ConstructorMovimiento nuevo=
                new ConstructorMovimiento(
                        ataque
                );

        mapa.put(key,nuevo);

        return nuevo;
    }

    private static String crearKey(
            String pokemon,
            String forma
    ){
        String pokemonKey=
                normalizar(pokemon);

        if(pokemonKey.isEmpty()){
            return "";
        }

        String formaKey=
                normalizar(forma);

        if(formaKey.isEmpty()||
                formaKey.equals("base")){
            formaKey="base";
        }

        return pokemonKey+"|"+formaKey;
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

    private static final class ConstructorMovimiento {
        private final ImmutableAttack ataque;

        private final List<MoveLearnSource> fuentes=
                new ArrayList<>();

        private ConstructorMovimiento(
                ImmutableAttack ataque
        ){
            this.ataque=ataque;
        }

        private void agregarFuente(
                MoveLearnSource fuente
        ){
            if(fuente==null)return;

            for(MoveLearnSource existente:fuentes){
                if(iguales(existente,fuente)){
                    return;
                }
            }

            fuentes.add(fuente);
        }

        private PokemonMoveData crear(){
            return new PokemonMoveData(
                    ataque.getAttackName(),
                    ataque.getAttackType()==null
                            ?""
                            :ataque.getAttackType().getName(),
                    ataque.getAttackCategory()==null
                            ?""
                            :ataque.getAttackCategory().toString(),
                    ataque.getBasePower(),
                    ataque.getAccuracy(),
                    ataque.getPPBase(),
                    fuentes
            );
        }

        private static boolean iguales(
                MoveLearnSource a,
                MoveLearnSource b
        ){
            return a.getMetodo().equals(b.getMetodo())&&
                    igual(a.getNivel(),b.getNivel())&&
                    igual(a.getGeneracion(),b.getGeneracion())&&
                    igual(a.getNumero(),b.getNumero());
        }

        private static boolean igual(
                Integer a,
                Integer b
        ){
            return a==null
                    ?b==null
                    :a.equals(b);
        }
    }
}