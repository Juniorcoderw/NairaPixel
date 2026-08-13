package com.hjunior.nairapixel.client.legend;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegendaryScoreboardReader {
    private static final Pattern LEGEND_PATTERN=
            Pattern.compile("(?i)legend\\D{0,32}?(\\d+)\\s*m");

    public static int leerMinutos(){
        String linea=buscarLineaLegend();
        if(linea==null)return -1;
        return extraerMinutos(linea);
    }

    public static String buscarLineaLegend(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null)return null;

        Scoreboard scoreboard=mc.level.getScoreboard();
        if(scoreboard==null)return null;

        ScoreObjective objetivo=scoreboard.getDisplayObjective(1);
        if(objetivo==null)return null;

        Collection<Score> scores=scoreboard.getPlayerScores(objetivo);
        if(scores==null||scores.isEmpty())return null;

        for(Score score:scores){
            if(score==null)continue;

            String owner=score.getOwner();
            if(owner==null)owner="";

            ScorePlayerTeam team=scoreboard.getPlayersTeam(owner);

            String prefijo="";
            String sufijo="";

            if(team!=null){
                if(team.getPlayerPrefix()!=null){
                    prefijo=team.getPlayerPrefix().getString();
                }

                if(team.getPlayerSuffix()!=null){
                    sufijo=team.getPlayerSuffix().getString();
                }
            }

            String[] candidatos={
                    prefijo+owner+sufijo,
                    prefijo+sufijo,
                    owner
            };

            for(String candidato:candidatos){
                String limpio=limpiar(candidato);

                if(extraerMinutos(limpio)>=0){
                    return limpio;
                }
            }
        }

        return null;
    }

    public static String getTituloSidebar(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null)return "";

        Scoreboard scoreboard=mc.level.getScoreboard();
        if(scoreboard==null)return "";

        ScoreObjective objetivo=scoreboard.getDisplayObjective(1);

        if(objetivo==null||objetivo.getDisplayName()==null){
            return "";
        }

        return limpiar(objetivo.getDisplayName().getString());
    }

    public static int extraerMinutos(String texto){
        if(texto==null||texto.trim().isEmpty())return -1;

        Matcher matcher=LEGEND_PATTERN.matcher(limpiar(texto));

        if(!matcher.find())return -1;

        try{
            return Integer.parseInt(matcher.group(1));
        }catch(NumberFormatException e){
            return -1;
        }
    }

    private static String limpiar(String texto){
        if(texto==null)return "";

        return texto
                .replaceAll("(?i)§[0-9A-FK-OR]","")
                .replace('\u00A0',' ')
                .replace("\u200B","")
                .replace("\u200C","")
                .replace("\u200D","")
                .trim();
    }
}