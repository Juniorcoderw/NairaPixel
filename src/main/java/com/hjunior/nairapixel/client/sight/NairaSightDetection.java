package com.hjunior.nairapixel.client.sight;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

import java.util.UUID;

public final class NairaSightDetection {
    private final String objetivoClave;
    private final int color;
    private final boolean principal;

    private final UUID entityUuid;
    private final PixelmonEntity entity;
    private final String pokemon;
    private final String forma;
    private final double distancia;
    private final double diferenciaY;
    private final float anguloHorizontal;
    private final float anguloVertical;
    private final String direccion;

    public NairaSightDetection(
            String objetivoClave,
            int color,
            boolean principal,
            UUID entityUuid,
            PixelmonEntity entity,
            String pokemon,
            String forma,
            double distancia,
            double diferenciaY,
            float anguloHorizontal,
            float anguloVertical,
            String direccion
    ){
        this.objetivoClave=objetivoClave==null?"":objetivoClave;
        this.color=color;
        this.principal=principal;
        this.entityUuid=entityUuid;
        this.entity=entity;
        this.pokemon=pokemon==null?"":pokemon;
        this.forma=forma==null?"":forma;
        this.distancia=distancia;
        this.diferenciaY=diferenciaY;
        this.anguloHorizontal=anguloHorizontal;
        this.anguloVertical=anguloVertical;
        this.direccion=direccion==null?"":direccion;
    }

    public String getObjetivoClave(){
        return objetivoClave;
    }

    public int getColor(){
        return color;
    }

    public boolean isPrincipal(){
        return principal;
    }

    public UUID getEntityUuid(){
        return entityUuid;
    }

    public PixelmonEntity getEntity(){
        return entity;
    }

    public String getPokemon(){
        return pokemon;
    }

    public String getForma(){
        return forma;
    }

    public double getDistancia(){
        return distancia;
    }

    public int getDistanciaRedondeada(){
        return (int)Math.round(
                distancia
        );
    }

    public double getDiferenciaY(){
        return diferenciaY;
    }

    public int getDiferenciaYRedondeada(){
        return (int)Math.round(
                diferenciaY
        );
    }

    public float getAnguloHorizontal(){
        return anguloHorizontal;
    }

    public float getAnguloVertical(){
        return anguloVertical;
    }

    public String getDireccion(){
        return direccion;
    }

    public boolean estaEnPantallaAproximada(){
        return Math.abs(
                anguloHorizontal
        )<=42.0F&&
                Math.abs(
                        anguloVertical
                )<=30.0F;
    }

    public boolean estaArriba(){
        return diferenciaY>3.0D;
    }

    public boolean estaAbajo(){
        return diferenciaY<-3.0D;
    }
}
