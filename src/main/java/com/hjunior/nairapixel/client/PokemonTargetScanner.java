package com.hjunior.nairapixel.client;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.data.PokemonSnapshot;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.client.storage.ClientStorageManager;
import com.pixelmonmod.pixelmon.comm.packetHandlers.LensInfoPacket;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public class PokemonTargetScanner {
    private static PixelmonEntity objetivoActual;
    private static PokemonSnapshot snapshotActual;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;

        Minecraft mc=Minecraft.getInstance();

        if(mc.player==null||mc.level==null||mc.hitResult==null){
            limpiar();
            return;
        }

        if(mc.hitResult.getType()!=RayTraceResult.Type.ENTITY){
            limpiar();
            return;
        }

        EntityRayTraceResult hit=(EntityRayTraceResult)mc.hitResult;

        if(!(hit.getEntity() instanceof PixelmonEntity)){
            limpiar();
            return;
        }

        PixelmonEntity entity=(PixelmonEntity)hit.getEntity();

        objetivoActual=entity;
        snapshotActual=crearSnapshot(entity,mc);
    }

    private static PokemonSnapshot crearSnapshot(PixelmonEntity entity,Minecraft mc){
        Pokemon pokemon=entity.getPokemon();
        if(pokemon==null)return null;

        PokemonSnapshot data=new PokemonSnapshot();

        data.uuid=pokemon.getUUID();
        data.nombre=pokemon.getSpecies().getLocalizedName();
        data.nivel=pokemon.getPokemonLevel();

        cargarPropietario(data,entity,mc);
        cargarDatosPublicos(data,pokemon,entity);
        cargarDatosPrivados(data,pokemon,entity,mc);

        return data;
    }

    private static void cargarPropietario(PokemonSnapshot data,PixelmonEntity entity,Minecraft mc){
        LivingEntity owner=entity.getOwner();

        if(owner==null){
            data.propietario="Salvaje";
            return;
        }

        data.propietario=owner.getName().getString();
    }

    private static void cargarDatosPublicos(PokemonSnapshot data,Pokemon pokemon,PixelmonEntity entity){
        for(Element tipo:pokemon.getForm().getTypes()){
            data.tipos.add(PokemonTranslator.tipo(tipo));
        }

        data.genero=PokemonTranslator.genero(pokemon.getGender());
        data.tamano=PokemonTranslator.crecimiento(pokemon.getGrowth().toString());
        data.categoria=obtenerCategoria(pokemon);
        data.shiny=pokemon.isShiny();

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
                    !paleta.equalsIgnoreCase("shiny")&&
                    !paleta.equalsIgnoreCase("none")&&
                    !paleta.trim().isEmpty()){
                data.paleta=PokemonTranslator.formatear(paleta);
            }
        }

        if(entity.isBossPokemon()){
            if(entity.getBossTier()!=null){
                data.boss=PokemonTranslator.boss(entity.getBossTier().getName());
            }else{
                data.boss="Boss";
            }
        }
    }

    private static void cargarDatosPrivados(
            PokemonSnapshot data,
            Pokemon pokemon,
            PixelmonEntity entity,
            Minecraft mc
    ){
        LensData lens=obtenerLensInfo(entity);

        if(lens!=null){
            cargarLensInfo(data,pokemon,lens);
            return;
        }

        LivingEntity owner=entity.getOwner();

        if(owner==null||mc.player==null)return;
        if(!owner.getUUID().equals(mc.player.getUUID()))return;
        if(ClientStorageManager.party==null)return;

        UUID uuid=pokemon.getUUID();
        if(uuid==null)return;

        Pokemon propio=ClientStorageManager.party.get(uuid);
        if(propio==null)return;

        if(propio.getNature()!=null){
            data.naturaleza=PokemonTranslator.naturaleza(propio.getNature());
        }

        String habilidad=propio.getAbilityName();

        if(habilidad!=null&&!habilidad.isEmpty()){
            data.habilidad=PokemonTranslator.habilidadIngles(habilidad);
        }

        data.habilidadOculta=propio.hasHiddenAbility();

        cargarIVs(data,propio.getIVs());
    }

    private static void cargarLensInfo(
            PokemonSnapshot data,
            Pokemon pokemon,
            LensData lens
    ){
        if(lens.nature!=null){
            data.naturaleza=PokemonTranslator.naturaleza(lens.nature);
        }

        if(lens.abilityLangKey!=null&&!lens.abilityLangKey.isEmpty()){
            String nombre=I18n.get(lens.abilityLangKey);
            data.habilidad=PokemonTranslator.habilidadIngles(nombre);
            data.habilidadOculta=esHabilidadOculta(pokemon,lens.abilityLangKey);
        }

        cargarIVs(data,lens.ivs);
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

    private static LensData obtenerLensInfo(PixelmonEntity entity){
        LensInfoPacket packet=entity.getClientOnlyInfo();
        if(packet==null)return null;

        try{
            LensData data=new LensData();

            Field ivsField=LensInfoPacket.class.getDeclaredField("ivs");
            Field abilityField=LensInfoPacket.class.getDeclaredField("abilityLangKey");
            Field natureField=LensInfoPacket.class.getDeclaredField("nature");

            ivsField.setAccessible(true);
            abilityField.setAccessible(true);
            natureField.setAccessible(true);

            data.ivs=(IVStore)ivsField.get(packet);
            data.abilityLangKey=(String)abilityField.get(packet);
            data.nature=(Nature)natureField.get(packet);

            return data;
        }catch(Exception e){
            return null;
        }
    }

    private static boolean esHabilidadOculta(Pokemon pokemon,String langKey){
        if(langKey==null)return false;

        String nombre=langKey;

        if(nombre.startsWith("ability.")){
            nombre=nombre.substring("ability.".length());
        }

        try{
            Ability ability=AbilityRegistry.getAbility(nombre).orElse(null);

            return ability!=null&&
                    pokemon.getForm().getAbilities().isHiddenAbility(ability);
        }catch(Exception e){
            return false;
        }
    }

    private static void limpiar(){
        objetivoActual=null;
        snapshotActual=null;
    }

    public static PixelmonEntity getObjetivoActual(){
        return objetivoActual;
    }

    public static PokemonSnapshot getSnapshotActual(){
        return snapshotActual;
    }

    public static boolean tieneObjetivo(){
        return objetivoActual!=null&&snapshotActual!=null;
    }

    private static class LensData{
        IVStore ivs;
        String abilityLangKey;
        Nature nature;
    }
}