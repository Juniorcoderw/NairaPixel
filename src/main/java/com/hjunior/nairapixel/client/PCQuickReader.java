package com.hjunior.nairapixel.client;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.data.PokemonSnapshot;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.egg.EggGroup;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.client.gui.pc.PCScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public class PCQuickReader {
    private static PokemonSnapshot snapshotActual;
    private static UUID ultimoUUID;

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event){
        if(!(event.getGui() instanceof PCScreen)){
            limpiar();
            return;
        }

        PCScreen screen=(PCScreen)event.getGui();
        Pokemon pokemon=screen.getSlotAt(event.getMouseX(),event.getMouseY());

        if(pokemon==null){
            limpiar();
            return;
        }

        UUID uuid=pokemon.getUUID();

        if(uuid!=null&&uuid.equals(ultimoUUID)&&snapshotActual!=null)return;

        ultimoUUID=uuid;
        snapshotActual=crearSnapshot(pokemon);
    }

    private static PokemonSnapshot crearSnapshot(Pokemon pokemon){
        PokemonSnapshot data=new PokemonSnapshot();

        data.uuid=pokemon.getUUID();
        data.nombre=pokemon.getSpecies().getLocalizedName();
        data.nivel=pokemon.getPokemonLevel();

        for(Element tipo:pokemon.getForm().getTypes()){
            data.tipos.add(PokemonTranslator.tipo(tipo));
        }

        data.genero=PokemonTranslator.genero(pokemon.getGender());
        data.tamano=PokemonTranslator.crecimiento(pokemon.getGrowth().toString());
        data.categoria=obtenerCategoria(pokemon);
        data.shiny=pokemon.isShiny();

        cargarFormaPaleta(data,pokemon);
        cargarDatosPokemon(data,pokemon);
        cargarCrianza(data,pokemon);
        cargarIVs(data,pokemon.getIVs());
        cargarEVs(data,pokemon.getEVs());
        cargarMovimientos(data,pokemon);

        return data;
    }

    private static void cargarFormaPaleta(PokemonSnapshot data,Pokemon pokemon){
        if(!pokemon.isDefaultForm()){
            String forma=pokemon.getForm().getLocalizedName();

            if(forma==null||forma.trim().isEmpty()){
                forma=pokemon.getForm().getName();
            }

            if(forma!=null&&!forma.trim().isEmpty()){
                data.forma=PokemonTranslator.formatear(forma);
            }
        }

        if(!pokemon.isDefaultPalette()){
            String paleta=pokemon.getPalette().getName();

            if(paleta!=null&&
                    !paleta.equalsIgnoreCase("none")&&
                    !paleta.equalsIgnoreCase("shiny")&&
                    !paleta.trim().isEmpty()){

                data.paleta=PokemonTranslator.formatear(paleta);
            }
        }
    }

    private static void cargarDatosPokemon(PokemonSnapshot data,Pokemon pokemon){
        if(pokemon.getNature()!=null){
            data.naturaleza=PokemonTranslator.naturaleza(pokemon.getNature());
        }

        String habilidad=pokemon.getAbilityName();

        if(habilidad!=null&&!habilidad.isEmpty()){
            data.habilidad=PokemonTranslator.habilidadIngles(habilidad);
        }

        data.habilidadOculta=pokemon.hasHiddenAbility();
        data.amistad=pokemon.getFriendship();

        String ot=pokemon.getOriginalTrainer();

        if(ot!=null&&!ot.trim().isEmpty()){
            data.ot=ot;
        }

        if(pokemon.getBall()!=null){
            data.pokeball=pokemon.getBall().getLocalizedName();
        }

        if(pokemon.getHeldItem().isEmpty()){
            data.objeto="Ninguno";
        }else{
            data.objeto=pokemon.getHeldItem().getHoverName().getString();
        }
    }

    private static void cargarCrianza(PokemonSnapshot data,Pokemon pokemon){
        boolean criable=true;

        for(EggGroup grupo:pokemon.getForm().getEggGroups()){
            String key=grupo.getKey();

            if(esUndiscovered(key)){
                criable=false;
            }

            data.gruposHuevo.add(
                    PokemonTranslator.grupoHuevo(key)
            );
        }

        if(data.gruposHuevo.isEmpty()){
            data.criable=null;
        }else{
            data.criable=criable;
        }
    }

    private static void cargarIVs(PokemonSnapshot data,IVStore ivs){
        if(ivs==null)return;

        data.ivPS=ivs.getStat(BattleStatsType.HP);
        data.ivATQ=ivs.getStat(BattleStatsType.ATTACK);
        data.ivDEF=ivs.getStat(BattleStatsType.DEFENSE);
        data.ivATQESP=ivs.getStat(BattleStatsType.SPECIAL_ATTACK);
        data.ivDEFESP=ivs.getStat(BattleStatsType.SPECIAL_DEFENSE);
        data.ivVEL=ivs.getStat(BattleStatsType.SPEED);

        if(data.tieneIVs()){
            int total=
                    data.ivPS+
                            data.ivATQ+
                            data.ivDEF+
                            data.ivATQESP+
                            data.ivDEFESP+
                            data.ivVEL;

            data.ivTotal=Math.round((total/186.0)*1000.0)/10.0;
        }
    }

    private static void cargarEVs(PokemonSnapshot data,EVStore evs){
        if(evs==null)return;

        data.evPS=evs.getStat(BattleStatsType.HP);
        data.evATQ=evs.getStat(BattleStatsType.ATTACK);
        data.evDEF=evs.getStat(BattleStatsType.DEFENSE);
        data.evATQESP=evs.getStat(BattleStatsType.SPECIAL_ATTACK);
        data.evDEFESP=evs.getStat(BattleStatsType.SPECIAL_DEFENSE);
        data.evVEL=evs.getStat(BattleStatsType.SPEED);
    }

    private static void cargarMovimientos(PokemonSnapshot data,Pokemon pokemon){
        for(Attack ataque:pokemon.getMoveset()){
            if(ataque==null)continue;

            String nombre=ataque.getMove().getLocalizedName();

            if(nombre!=null&&!nombre.trim().isEmpty()){
                data.movimientos.add(nombre);
            }
        }
    }

    private static String obtenerCategoria(Pokemon pokemon){
        if(pokemon.getSpecies().isMythical())return "Mítico";
        if(pokemon.getSpecies().isUltraBeast())return "Ultraente";
        if(pokemon.getSpecies().isLegendary())return "Legendario";
        if(esPseudo(pokemon))return "Pseudo";

        return "";
    }

    private static boolean esPseudo(Pokemon pokemon){
        String nombre=pokemon.getSpecies().getName();
        if(nombre==null)return false;

        switch(nombre.toLowerCase(Locale.ROOT)){
            case "dragonite":
            case "tyranitar":
            case "salamence":
            case "metagross":
            case "garchomp":
            case "hydreigon":
            case "goodra":
            case "kommo-o":
            case "kommoo":
            case "dragapult":
            case "baxcalibur":
                return true;
            default:
                return false;
        }
    }

    private static boolean esUndiscovered(String key){
        if(key==null)return false;

        String valor=key
                .toLowerCase(Locale.ROOT)
                .replace("_","")
                .replace("-","")
                .replace(" ","");

        return valor.equals("undiscovered");
    }

    private static void limpiar(){
        snapshotActual=null;
        ultimoUUID=null;
    }

    public static PokemonSnapshot getSnapshotActual(){
        return snapshotActual;
    }

    public static boolean tienePokemon(){
        return snapshotActual!=null;
    }
}