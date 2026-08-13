package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public final class LegendaryEnvironmentReader {
    private LegendaryEnvironmentReader(){}

    public static Estado leer(){
        Minecraft mc=Minecraft.getInstance();

        if(mc.level==null||mc.player==null){
            return Estado.vacio();
        }

        BlockPos pos=mc.player.blockPosition();

        return new Estado(
                leerClima(mc),
                leerFaseLunar(mc),
                leerBioma(mc,pos),
                pos.getY(),
                leerBloqueBase(mc,pos)
        );
    }

    private static String leerClima(Minecraft mc){
        if(mc.level==null){
            return "";
        }

        if(mc.level.isThundering()){
            return "STORM";
        }

        if(mc.level.isRaining()){
            return "RAIN";
        }

        return "CLEAR";
    }

    private static int leerFaseLunar(Minecraft mc){
        if(mc.level==null){
            return -1;
        }

        try{
            return mc.level.getMoonPhase();
        }catch(Exception e){
            return -1;
        }
    }

    private static String leerBioma(
            Minecraft mc,
            BlockPos pos
    ){
        if(mc.level==null||pos==null){
            return "";
        }

        try{
            Biome biome=
                    mc.level.getBiome(pos);

            Registry<Biome> registry=
                    mc.level
                            .registryAccess()
                            .registryOrThrow(
                                    Registry.BIOME_REGISTRY
                            );

            ResourceLocation id=
                    registry.getKey(biome);

            if(id==null){
                return "";
            }

            return id.toString();

        }catch(Exception e){
            return "";
        }
    }

    private static String leerBloqueBase(
            Minecraft mc,
            BlockPos pos
    ){
        if(mc.level==null||pos==null){
            return "";
        }

        try{
            ResourceLocation id=
                    mc.level
                            .getBlockState(pos.below())
                            .getBlock()
                            .getRegistryName();

            if(id==null){
                return "";
            }

            return id.toString();

        }catch(Exception e){
            return "";
        }
    }

    public static final class Estado {
        private final String clima;
        private final int faseLunar;
        private final String bioma;
        private final int y;
        private final String bloqueBase;

        private Estado(
                String clima,
                int faseLunar,
                String bioma,
                int y,
                String bloqueBase
        ){
            this.clima=limpiar(clima);
            this.faseLunar=faseLunar;
            this.bioma=limpiar(bioma);
            this.y=y;
            this.bloqueBase=limpiar(bloqueBase);
        }

        private static Estado vacio(){
            return new Estado(
                    "",
                    -1,
                    "",
                    0,
                    ""
            );
        }

        public String getClima(){
            return clima;
        }

        public int getFaseLunar(){
            return faseLunar;
        }

        public String getBioma(){
            return bioma;
        }

        public int getY(){
            return y;
        }

        public String getBloqueBase(){
            return bloqueBase;
        }

        public boolean tieneClima(){
            return !clima.isEmpty();
        }

        public boolean tieneFaseLunar(){
            return faseLunar>=0;
        }

        public boolean tieneBioma(){
            return !bioma.isEmpty();
        }

        public boolean tieneBloqueBase(){
            return !bloqueBase.isEmpty();
        }

        private static String limpiar(
                String texto
        ){
            return texto==null
                    ?""
                    :texto.trim();
        }
    }
}