package com.hjunior.nairapixel.client;

import com.hjunior.nairapixel.NairaPixel;
import com.hjunior.nairapixel.client.data.PokemonSnapshot;
import com.hjunior.nairapixel.client.dex.objectives.NairaDexObjectivesService;
import com.hjunior.nairapixel.client.util.PokemonTranslator;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid=NairaPixel.MOD_ID,value=Dist.CLIENT)
public class PokemonTargetScanner {
    private static final Logger LOGGER=LogManager.getLogger("NairaScanner");

    private static PixelmonEntity objetivoActual;
    private static PokemonSnapshot snapshotActual;

    private static UUID ultimoErrorUuid;
    private static long ultimoErrorMs;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;

        Minecraft mc=Minecraft.getInstance();

        if(!NairaDexObjectivesService.get()
                .isScannerActivo()){

            limpiar();
            return;
        }

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

        try{
            PokemonSnapshot nuevo=crearSnapshot(entity,mc);

            if(nuevo==null){
                limpiar();
                return;
            }

            objetivoActual=entity;
            snapshotActual=nuevo;
        }catch(Exception e){
            registrarError(entity,e);
            limpiar();
        }
    }

    private static PokemonSnapshot crearSnapshot(PixelmonEntity entity,Minecraft mc){
        if(entity==null)return null;

        Pokemon pokemon=entity.getPokemon();
        if(pokemon==null)return null;

        Species species=pokemon.getSpecies();
        if(species==null)return null;

        PokemonSnapshot data=new PokemonSnapshot();

        data.uuid=pokemon.getUUID();

        String nombre=species.getLocalizedName();

        if(nombre==null||nombre.trim().isEmpty()){
            nombre=species.getName();
        }

        data.nombre=nombre==null?"Pokémon":nombre;
        data.nivel=pokemon.getPokemonLevel();

        cargarPropietario(data,entity,mc);
        cargarDatosPublicos(data,pokemon,entity);
        cargarDatosPrivados(data,pokemon,entity,mc);

        return data;
    }

    private static void cargarPropietario(PokemonSnapshot data,PixelmonEntity entity,Minecraft mc){
        if(data==null||entity==null)return;

        LivingEntity owner=entity.getOwner();

        if(owner==null){
            data.propietario="Salvaje";
            return;
        }

        if(owner.getName()!=null){
            data.propietario=owner.getName().getString();
        }else{
            data.propietario="Con entrenador";
        }
    }

    private static void cargarDatosPublicos(PokemonSnapshot data,Pokemon pokemon,PixelmonEntity entity){
        if(data==null||pokemon==null)return;

        Stats form=pokemon.getForm();

        if(form!=null&&form.getTypes()!=null){
            for(Element tipo:form.getTypes()){
                if(tipo!=null){
                    data.tipos.add(PokemonTranslator.tipo(tipo));
                }
            }
        }

        if(pokemon.getGender()!=null){
            data.genero=PokemonTranslator.genero(pokemon.getGender());
        }else{
            data.genero="No disponible";
        }

        if(pokemon.getGrowth()!=null){
            data.tamano=PokemonTranslator.crecimiento(
                    pokemon.getGrowth().toString()
            );
        }else{
            data.tamano="No disponible";
        }

        data.categoria=obtenerCategoria(pokemon);
        data.shiny=pokemon.isShiny();

        if(form!=null&&!pokemon.isDefaultForm()){
            String forma=form.getLocalizedName();

            if(forma==null||forma.trim().isEmpty()){
                forma=form.getName();
            }

            if(forma!=null&&!forma.trim().isEmpty()){
                data.forma=PokemonTranslator.formatear(forma);
            }
        }

        if(pokemon.getPalette()!=null&&!pokemon.isDefaultPalette()){
            String paleta=pokemon.getPalette().getName();

            if(paleta!=null&&
                    !paleta.equalsIgnoreCase("shiny")&&
                    !paleta.equalsIgnoreCase("none")&&
                    !paleta.trim().isEmpty()){

                data.paleta=PokemonTranslator.formatear(paleta);
            }
        }

        if(entity!=null&&entity.isBossPokemon()){
            if(entity.getBossTier()!=null){
                data.boss=PokemonTranslator.boss(
                        entity.getBossTier().getName()
                );
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
        if(data==null||pokemon==null||entity==null)return;

        LensData lens=obtenerLensInfo(entity);

        if(lens!=null){
            cargarLensInfo(data,pokemon,lens);
            return;
        }

        LivingEntity owner=entity.getOwner();

        if(owner==null||mc==null||mc.player==null)return;
        if(owner.getUUID()==null||!owner.getUUID().equals(mc.player.getUUID()))return;
        if(ClientStorageManager.party==null)return;

        UUID uuid=pokemon.getUUID();
        if(uuid==null)return;

        Pokemon propio=ClientStorageManager.party.get(uuid);
        if(propio==null)return;

        if(propio.getNature()!=null){
            data.naturaleza=PokemonTranslator.naturaleza(
                    propio.getNature()
            );
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
        if(data==null||pokemon==null||lens==null)return;

        if(lens.nature!=null){
            data.naturaleza=PokemonTranslator.naturaleza(lens.nature);
        }

        if(lens.abilityLangKey!=null&&!lens.abilityLangKey.isEmpty()){
            String nombre=I18n.get(lens.abilityLangKey);

            if(nombre!=null&&!nombre.trim().isEmpty()){
                data.habilidad=PokemonTranslator.habilidadIngles(nombre);
            }

            data.habilidadOculta=esHabilidadOculta(
                    pokemon,
                    lens.abilityLangKey
            );
        }

        cargarIVs(data,lens.ivs);
    }

    private static void cargarIVs(PokemonSnapshot data,IVStore ivs){
        if(data==null||ivs==null)return;

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
        if(pokemon==null||pokemon.getSpecies()==null)return "";

        Species species=pokemon.getSpecies();

        if(species.isMythical())return "Mítico";
        if(species.isUltraBeast())return "Ultraente";
        if(species.isLegendary())return "Legendario";
        if(esPseudo(pokemon))return "Pseudo";

        return "";
    }

    private static boolean esPseudo(Pokemon pokemon){
        if(pokemon==null||pokemon.getSpecies()==null)return false;

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
        if(entity==null)return null;

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
        if(pokemon==null||langKey==null)return false;
        if(pokemon.getForm()==null)return false;
        if(pokemon.getForm().getAbilities()==null)return false;

        String nombre=langKey;

        if(nombre.startsWith("ability.")){
            nombre=nombre.substring("ability.".length());
        }

        try{
            Ability ability=AbilityRegistry.getAbility(nombre).orElse(null);

            return ability!=null&&
                    pokemon.getForm()
                            .getAbilities()
                            .isHiddenAbility(ability);
        }catch(Exception e){
            return false;
        }
    }

    private static void registrarError(PixelmonEntity entity,Exception e){
        UUID uuid=entity==null?null:entity.getUUID();
        long ahora=System.currentTimeMillis();

        boolean mismo=
                uuid!=null&&
                        uuid.equals(ultimoErrorUuid);

        if(mismo&&ahora-ultimoErrorMs<3000L){
            return;
        }

        ultimoErrorUuid=uuid;
        ultimoErrorMs=ahora;

        LOGGER.warn(
                "[NairaScanner] No se pudo leer completamente el Pokémon apuntado. Se omitió el snapshot para evitar un crash.",
                e
        );
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
