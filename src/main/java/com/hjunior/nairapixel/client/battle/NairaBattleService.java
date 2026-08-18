package com.hjunior.nairapixel.client.battle;

import com.hjunior.nairapixel.client.battle.analysis.NairaBattleRecommendation;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;

import java.util.List;
import java.util.Locale;

public final class NairaBattleService{
    private static NairaBattleSnapshot snapshot;
    private static NairaBattleRecommendation recomendacion;

    private NairaBattleService(){}

    public static NairaBattleSnapshot getSnapshot(){
        return snapshot;
    }

    public static NairaBattleRecommendation getRecomendacion(){
        return recomendacion;
    }

    public static boolean tieneBatalla(){
        return snapshot!=null;
    }

    public static void actualizar(ClientBattleManager bm){
        if(bm==null||!bm.isBattling()){
            limpiar();
            return;
        }

        NairaBattleSnapshot nuevo=crearSnapshot(bm);
        snapshot=nuevo;

        NairaBattlePokemon propio=nuevo.propioActivo();
        NairaBattlePokemon rival=nuevo.rivalActivo();
        recomendacion=propio!=null&&rival!=null
                ?NairaBattleRecommendation.analizar(propio,rival)
                :null;
    }

    public static void refrescarDinamicos(ClientBattleManager bm){
        if(snapshot==null||bm==null||!bm.isBattling())return;
        refrescarLista(snapshot.propios,bm.displayedOurPokemon);
        refrescarLista(snapshot.aliados,bm.displayedAllyPokemon);
        refrescarLista(snapshot.rivales,bm.displayedEnemyPokemon);
    }

    public static void limpiar(){
        snapshot=null;
        recomendacion=null;
    }

    public static String firma(ClientBattleManager bm){
        if(bm==null||!bm.isBattling())return "";

        StringBuilder f=new StringBuilder(512);
        f.append(bm.battleControllerIndex).append('|')
                .append(bm.battleTurn).append('|')
                .append(bm.battleType).append('|')
                .append(bm.currentPokemon).append('|')
                .append(bm.weather).append('|')
                .append(bm.terrain).append('|')
                .append(bm.canSwitch).append('|')
                .append(bm.canFlee).append('|')
                .append(bm.waitingText).append('|')
                .append(bm.dynamaxing).append('|')
                .append(bm.dynamaxTurnsLeft).append('|')
                .append(bm.gigantamax).append('|')
                .append(bm.megaEvolving).append('|')
                .append(bm.showZMoves);

        agregarFirmaPokemon(f,bm.displayedOurPokemon,true);
        agregarFirmaPokemon(f,bm.displayedAllyPokemon,false);
        agregarFirmaPokemon(f,bm.displayedEnemyPokemon,false);
        return f.toString();
    }

    private static NairaBattleSnapshot crearSnapshot(ClientBattleManager bm){
        NairaBattleSnapshot s=new NairaBattleSnapshot();
        s.formato=bm.battleType==null?BattleType.SINGLE:bm.battleType;
        s.turno=bm.battleTurn;

        s.clima=valor(bm.weather);
        s.terreno=valor(bm.terrain);

        s.puedeCambiar=bm.canSwitch;
        s.puedeHuir=bm.canFlee;
        s.esperando=bm.waitingText;
        s.espectando=bm.isSpectating;

        s.dynamax=bm.dynamaxing;
        s.turnosDynamax=bm.dynamaxTurnsLeft;
        s.gigantamax=bm.gigantamax;
        s.mega=bm.megaEvolving;
        s.zMoves=bm.showZMoves;

        copiarPokemon(s.propios,bm.displayedOurPokemon,true);
        copiarPokemon(s.aliados,bm.displayedAllyPokemon,false);
        copiarPokemon(s.rivales,bm.displayedEnemyPokemon,false);

        s.contexto=detectarContexto(bm,s);
        return s;
    }

    private static void copiarPokemon(
            List<NairaBattlePokemon> destino,
            PixelmonClientData[] origen,
            boolean incluirMovimientos){

        if(origen==null)return;
        for(PixelmonClientData d:origen){
            if(d!=null)destino.add(mapearPokemon(d,incluirMovimientos));
        }
    }

    private static NairaBattlePokemon mapearPokemon(
            PixelmonClientData d,
            boolean incluirMovimientos){

        NairaBattlePokemon p=new NairaBattlePokemon();
        p.uuid=d.pokemonUUID;
        p.nombre=d.species==null?"":d.species.getName();
        p.forma=d.form==null?"":d.form;
        p.posicion=d.position;
        p.nivel=d.level;
        p.hp=d.health==null?-1D:d.health.get();
        p.hpMax=d.maxHealth;
        p.estado=d.status;

        if(d.bossTier!=null){
            p.boss=d.bossTier.isBoss();
            p.bossTier=d.bossTier.getName();
        }

        p.escudos=d.shields;
        p.escudosMax=d.maxShields;
        p.escudoPerdido=d.lostShield;

        try{
            Stats stats=d.getBaseStats();
            if(stats!=null&&stats.getTypes()!=null)p.tipos.addAll(stats.getTypes());
        }catch(Throwable ignored){}

        if(incluirMovimientos)copiarMovimientos(p,d.moveset);
        return p;
    }

    private static void refrescarLista(
            List<NairaBattlePokemon> destino,
            PixelmonClientData[] origen){

        if(destino==null||origen==null)return;
        int limite=Math.min(destino.size(),origen.length);

        for(int i=0;i<limite;i++){
            NairaBattlePokemon p=destino.get(i);
            PixelmonClientData d=origen[i];
            if(p==null||d==null)continue;

            if(p.uuid!=null&&d.pokemonUUID!=null&&!p.uuid.equals(d.pokemonUUID))continue;

            p.hp=d.health==null?-1D:d.health.get();
            p.hpMax=d.maxHealth;
            p.estado=d.status;
            p.escudos=d.shields;
            p.escudosMax=d.maxShields;
            p.escudoPerdido=d.lostShield;

            if(d.bossTier!=null){
                p.boss=d.bossTier.isBoss();
                p.bossTier=d.bossTier.getName();
            }else{
                p.boss=false;
                p.bossTier="";
            }
        }
    }

    private static void copiarMovimientos(NairaBattlePokemon pokemon,Moveset moveset){
        if(moveset==null||moveset.isEmpty())return;

        for(int i=0;i<moveset.size();i++){
            Attack a=moveset.get(i);
            if(a==null)continue;

            ImmutableAttack base=a.getMove();
            if(base==null)continue;

            NairaBattleMove m=new NairaBattleMove();
            m.nombre=texto(base.getLocalizedName(),base.getAttackName());
            m.nombreIngles=base.getAttackName();
            m.tipo=a.getType()!=null?a.getType():base.getAttackType();
            m.categoria=a.getAttackCategory()!=null
                    ?a.getAttackCategory()
                    :base.getAttackCategory();
            m.potencia=a.getOverridePower()>0
                    ?a.getOverridePower()
                    :base.getBasePower();
            m.precision=base.getAccuracy();
            m.pp=a.pp;
            m.ppMax=a.getMaxPP();
            m.nuncaFalla=a.cantMiss||base.getAccuracy()==Attack.NEVER_MISS;
            m.deshabilitado=a.getDisabled();
            pokemon.movimientos.add(m);
        }
    }

    private static NairaBattleContext detectarContexto(
            ClientBattleManager bm,
            NairaBattleSnapshot s){

        if(s.formato==BattleType.RAID)return NairaBattleContext.RAID;

        for(NairaBattlePokemon rival:s.rivales){
            if(rival.tieneEscudos())return NairaBattleContext.RAID;
        }

        for(NairaBattlePokemon rival:s.rivales){
            if(rival.esBoss())return NairaBattleContext.BOSS;
        }

        if(bm.isOpponentWildNotBoss())return NairaBattleContext.NORMAL;
        if(oponenteEsJugador(bm.battleSetup))return NairaBattleContext.PVP;
        return NairaBattleContext.NORMAL;
    }

    private static boolean oponenteEsJugador(Object[][] setup){
        if(setup==null||setup.length<2||setup[1]==null)return false;

        for(Object participante:setup[1]){
            if(participante==null)continue;
            String valor=String.valueOf(participante).toLowerCase(Locale.ROOT);
            if(valor.contains("player"))return true;
        }
        return false;
    }

    private static void agregarFirmaPokemon(
            StringBuilder f,
            PixelmonClientData[] datos,
            boolean incluirMovimientos){

        f.append('|').append(datos==null?-1:datos.length);
        if(datos==null)return;

        for(PixelmonClientData d:datos){
            if(d==null){
                f.append("|null");
                continue;
            }

            f.append('|').append(d.pokemonUUID)
                    .append(':').append(d.species==null?"":d.species.getName())
                    .append(':').append(d.form)
                    .append(':').append(d.position)
                    .append(':').append(d.level)
                    .append(':').append(d.maxHealth)
                    .append(':').append(d.status)
                    .append(':').append(d.bossTier==null?"":d.bossTier.getID())
                    .append(':').append(d.shields)
                    .append(':').append(d.maxShields)
                    .append(':').append(d.lostShield);

            if(incluirMovimientos)agregarFirmaMovimientos(f,d.moveset);
        }
    }

    private static void agregarFirmaMovimientos(StringBuilder f,Moveset moveset){
        f.append(':').append(moveset==null?-1:moveset.size());
        if(moveset==null)return;

        for(int i=0;i<moveset.size();i++){
            Attack a=moveset.get(i);
            if(a==null){
                f.append(":null");
                continue;
            }

            ImmutableAttack base=a.getMove();
            f.append(':')
                    .append(base==null?"":base.getAttackName())
                    .append(',').append(a.pp)
                    .append(',').append(a.getMaxPP())
                    .append(',').append(a.getDisabled());
        }
    }

    private static String texto(String preferido,String respaldo){
        return preferido==null||preferido.trim().isEmpty()
                ?(respaldo==null?"":respaldo)
                :preferido;
    }

    private static String valor(Object o){
        return o==null?"":String.valueOf(o);
    }
}
