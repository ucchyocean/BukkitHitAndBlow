/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow.session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.ucchyocean.hitandblow.HitAndBlowException;
import com.github.ucchyocean.hitandblow.HitAndBlowPlugin;

/**
 * @author ucchy
 *
 */
public abstract class GameSession {

    enum GamePhase {
        SINGLE_CALL, VERSUS_PREPARE, VERSUS_SETNUMBER, VERSUS_P1CALL, VERSUS_P2CALL, ENDED, CANCELED };

    public String name;
    public Player player1;
    public Vector<CommandSender> listeners;

    protected String startDate;
    protected GamePhase phase;
    protected int level;
    protected int[] p2answer;
    protected Vector<int[]> p1scoreHistory;
    protected Vector<String> p1codeHistory;

    public GameSession(Player player1, int level) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
        this.startDate = format.format(new Date());
        this.name = player1.getName();

        this.player1 = player1;
        this.level = level;
        this.listeners = new Vector<CommandSender>();

        this.p1scoreHistory = new Vector<int[]>();
        this.p1codeHistory = new Vector<String>();

        GameSessionManager.addSession(this);
    }

    protected void runEndPhase() {

        phase = GamePhase.ENDED;
        GameSessionManager.removeSession(this);
        saveGameLog();
    }

    protected void cancelGame() {

        phase = GamePhase.CANCELED;
        GameSessionManager.removeSession(this);
        saveGameLog();
    }

    protected void printP1(String message) {

        printTo(player1, message);
    }

    protected void printToListeners(String message) {

        for ( CommandSender p : listeners ) {
            printTo(p, message);
        }
    }

    protected void printTo(CommandSender player, String message) {

        player.sendMessage(String.format("[%s] %s", HitAndBlowPlugin.NAME, message));
    }

    protected int[] checkEatBite(int[] answer, int[] call) {

        int eat = 0;
        int bite = 0;
        boolean[] checked = new boolean[level];

        for ( int i=0; i<level; i++ ) {
            if ( answer[i] == call[i] ) {
                eat++;
                checked[i] = true;
            }
        }

        for ( int i=0; i<level; i++ ) {
            if ( !checked[i] ) {
                for ( int j=0; j<level; j++ ) {
                    if ( answer[i] == call[j] ) {
                        bite++;
                        break;
                    }
                }
            }
        }

        int[] result = new int[2];

        result[0] = eat;
        result[1] = bite;

        return result;
    }

    protected void saveGameLog() {

        List<String> contents = getHistory(null);

        File logFileFolder = new File(HitAndBlowPlugin.GameLogFolder);

        if ( !logFileFolder.exists() ) {
            logFileFolder.mkdirs();
        }

        String filename = String.format("%s-%s.log", name, startDate);
        File logFile = new File(HitAndBlowPlugin.GameLogFolder + File.separator + filename);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile.getAbsolutePath()));
            for ( String s : contents ) {
                writer.write(s);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            HitAndBlowPlugin.logger.severe("Could not write game log file.");
            HitAndBlowPlugin.logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public int getLevel() {

        return level;
    }

    protected int[] parseS2I(String str) {

        int[] result = new int[level];

        for ( int i=0; i<level; i++ ) {
            result[i] = str.charAt(i) - 48;
        }

        return result;
    }

    protected String parseI2S(int[] integers) {

        if ( integers == null ) {
            return "";
        }

        StringBuilder str = new StringBuilder();

        for ( int i : integers ) {
            str.append(i);
        }

        return str.toString();
    }

    protected void printHistory(CommandSender sender) {

        List<String> messages = getHistory(sender);

        for ( String m : messages ) {
            sender.sendMessage(ChatColor.GRAY + m);
        }
    }

    public void addListener(CommandSender player) {

        listeners.add(player);
    }

    public void removeListener(CommandSender player) {

        listeners.remove(player);
    }

    protected abstract List<String> getHistory(CommandSender sender);
    protected abstract void callNumber(Player player, String number) throws HitAndBlowException;
    protected abstract boolean isPlayerForSet(Player player);
    protected abstract boolean isPlayerForCall(Player player);
    protected abstract boolean isPlayerForCancel(Player player);
    public abstract String toString();
}
