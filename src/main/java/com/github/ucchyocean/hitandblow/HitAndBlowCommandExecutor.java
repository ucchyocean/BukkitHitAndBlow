/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow;

import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.ucchyocean.misc.Resources;

/**
 * @author ucchy
 *
 */
public class HitAndBlowCommandExecutor implements CommandExecutor {

    private static final String PREFIX = String.format("[%s] ", HitAndBlowPlugin.NAME);
    private static final String PREERR = ChatColor.DARK_RED + PREFIX;

    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        String PREFIX = String.format("[%s] ", HitAndBlowPlugin.NAME);
        String PREERR = ChatColor.DARK_RED + PREFIX;

        if ( args.length == 0 ) {
            printUsage(sender, command.getName());
            return true;
        }

        if ( !(sender instanceof Player) ) {

            if ( args.length >= 1 ) {

                if ( args[0].equalsIgnoreCase("hist") || args[0].equalsIgnoreCase("history") ) {
                    return execHistory(sender);
                } if ( args[0].equalsIgnoreCase("rank") ) {
                    printRanking(sender);
                    return true;
                } else if ( args[0].equalsIgnoreCase("list") ) {
                    printList(sender);
                    return true;
                } else if ( args[0].equalsIgnoreCase("listen") ) {
                    return execListen(sender, args);
                } else if ( args[0].equalsIgnoreCase("exitlisten") ) {
                    return execExitListen(sender);
                }
            }

            sender.sendMessage(PREERR + Resources.get("cannotRunOnConsole"));
            return true;
        }

        Player player = (Player)sender;

        if ( args[0].equalsIgnoreCase("newgame") ) {
            return execNewGame(player, args);
        } else if ( args[0].equalsIgnoreCase("accept") ) {
            return execAccept(player);
        } else if ( args[0].equalsIgnoreCase("set") ) {
            return execSet(player, args);
        } else if ( args[0].equalsIgnoreCase("call") ) {
            return execCall(player, args);
        } else if ( args[0].equalsIgnoreCase("cancel") ) {
            return execCancel(player);
        } else if ( args[0].equalsIgnoreCase("hist") || args[0].equalsIgnoreCase("history") ) {
            return execHistory(sender);
        } else if ( args[0].equalsIgnoreCase("rank") ) {
            printRanking(sender);
            return true;
        } else if ( args[0].equalsIgnoreCase("list") ) {
            printList(sender);
            return true;
        } else if ( args[0].equalsIgnoreCase("listen") ) {
            return execListen(sender, args);
        } else if ( args[0].equalsIgnoreCase("exitlisten") ) {
            return execExitListen(sender);
        }

        return false;
    }

    private boolean execNewGame(Player player, String[] args) {

        if ( GameSessionManager.isPlayerInGame(player) ) {
            player.sendMessage(PREERR + Resources.get("cannotRunInGame"));
            return true;
        }

        if ( args.length < 2 ) {
            // Single game mode.

            if ( HitAndBlowPlugin.getSingleDialyTimes() != 0 ) {
                int times = UserConfiguration.getUserDailyPlayTimes(player.getName());
                if ( HitAndBlowPlugin.getSingleDialyTimes() <= times ) {
                    player.sendMessage(PREERR + Resources.get("singleExpire"));
                    return true;
                }
                player.sendMessage(PREERR + String.format(Resources.get("singleTimes"), times+1));
                player.sendMessage(PREERR + String.format(Resources.get("singleLeast"),
                        (HitAndBlowPlugin.getSingleDialyTimes() - times)));

                UserConfiguration.addUserDailyPlayTimes(player.getName());
            }

            new SingleGameSession(player, HitAndBlowPlugin.getSingleLevel());
            return true;

        } else {
            // Versus game mode.

            Double stake = HitAndBlowPlugin.getVersusStake();
            String unit = HitAndBlowPlugin.accountHandler.getUnitsPlural();
            int level = HitAndBlowPlugin.getVersusLevel();

            Player other = (Bukkit.getServer().getPlayer(args[1]));
            if ( other == null ) {
                player.sendMessage(PREERR + Resources.get("versusOffline"));
                return true;
            } else if ( GameSessionManager.isPlayerInGame(other) ) {
                player.sendMessage(PREERR + Resources.get("versusPartnerInGame"));
                return true;
            } else if ( player.getName().equals(other.getName()) ) {
                player.sendMessage(PREERR + Resources.get("versusSelf"));
                return true;
            }

            if ( !HitAndBlowPlugin.accountHandler.hasFunds(player.getName(), stake) ) {
                player.sendMessage(PREERR + String.format(Resources.get("versusDontHaveFunds"), stake, unit));
                return true;
            }

            if ( args.length >= 3 ) {
                if ( args[2].matches("[2-7]") ) {
                    level = Integer.parseInt(args[2]);
                } else {
                    player.sendMessage(PREERR + Resources.get("versusLevelWasnotInt"));
                    return true;
                }
            }

            VersusGameSession session = new VersusGameSession(player, other, level);

            session.runPreparePhase();

            return true;
        }
    }

    private boolean execAccept(Player player) {

        if ( !GameSessionManager.isPlayerForAccept(player) ) {
            player.sendMessage(PREERR + Resources.get("versusCannotAccept"));
            return true;
        }

        Double stake = HitAndBlowPlugin.getVersusStake();
        String unit = HitAndBlowPlugin.accountHandler.getUnitsPlural();

        if ( !HitAndBlowPlugin.accountHandler.hasFunds(player.getName(), stake) ) {
            player.sendMessage(PREERR + String.format(Resources.get("versusDontHaveFunds"), stake, unit));
            return true;
        }

        return GameSessionManager.runSetNumberPhaseByPlayer(player);
    }

    private boolean execSet(Player player, String[] args) {

        if ( !GameSessionManager.isPlayerForSet(player) ) {
            player.sendMessage(PREERR + Resources.get("versusNotPlayerInGame"));
            return true;
        }

        if ( args.length < 2 ) {
            player.sendMessage(PREERR + Resources.get("versusSetNumber"));
            return true;
        }

        int level = GameSessionManager.getSessionByPlayer(player).getLevel();
        String answer = args[1];

        if ( answer.length() != level ) {
            player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberFigures"), level));
            return true;
        }

        if ( !answer.matches("[0-9]+") ) {
            player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberFigures"), level));
            return true;
        }

        for ( int i=0; i<(level-1); i++ ) {
            for ( int j=i+1; j<level; j++ ) {
                if ( answer.charAt(i) == answer.charAt(j) ) {
                    player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberSame"), level));
                    return true;
                }
            }
        }

        return GameSessionManager.setNumberByPlayer(player, args[1]);
    }

    private boolean execCall(Player player, String[] args) {

        if ( !GameSessionManager.isPlayerForCall(player) ) {
            player.sendMessage(PREERR + Resources.get("notPlayerForCall"));
            return true;
        }

        if ( args.length < 2 ) {
            player.sendMessage(PREERR + Resources.get("pleaseCall"));
            return true;
        }

        int level = GameSessionManager.getSessionByPlayer(player).getLevel();
        String answer = args[1];

        if ( answer.length() != level ) {
            player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberFigures"), level));
            return true;
        }

        if ( !answer.matches("[0-9]+") ) {
            player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberFigures"), level));
            return true;
        }

        for ( int i=0; i<(level-1); i++ ) {
            for ( int j=i+1; j<level; j++ ) {
                if ( answer.charAt(i) == answer.charAt(j) ) {
                    player.sendMessage(PREERR + String.format(Resources.get("versusSetNumberSame"), level));
                    return true;
                }
            }
        }

        return GameSessionManager.callNumberByPlayer(player, args[1]);
    }

    private boolean execCancel(Player player) {

        if ( !GameSessionManager.isPlayerForCancel(player) ) {
            player.sendMessage(PREERR + Resources.get("notPlayerForCancel"));
            return true;
        }

        return GameSessionManager.cancelGameByPlayer(player);
    }

    private boolean execHistory(CommandSender sender) {

        if ( !GameSessionManager.isCommandSenderForHistory(sender) ) {
            sender.sendMessage(PREERR + Resources.get("notPlayerInGame"));
            return true;
        }

        return GameSessionManager.printHistoryByPlayer(sender);
    }

    private boolean execListen(CommandSender sender, String[] args) {

        if ( args.length < 2 ) {
            sender.sendMessage(PREERR + Resources.get("listenerSetName"));
            return true;
        }

        String gamename = args[1];
        GameSession session = GameSessionManager.getSessionByName(gamename);

        if ( session == null ) {
            sender.sendMessage(PREERR +
                    String.format(Resources.get("listenerNotFound"), gamename) );
            return true;
        }

        if ( session.player1.equals(sender) ) {
            sender.sendMessage(PREERR +
                    String.format(Resources.get("listenerPlayer"), gamename) );
            return true;
        } else if ( session instanceof VersusGameSession ) {
            if ( ((VersusGameSession)session).player2.equals(sender) ) {
                sender.sendMessage(PREERR +
                        String.format(Resources.get("listenerPlayer"), gamename) );
                return true;
            }
        }

        GameSession already = GameSessionManager.getSessionByListener(sender);
        if ( already != null ) {
            sender.sendMessage(PREERR +
                    String.format(Resources.get("listenerAlreadyAdded"), already.name) );
            return true;
        }

        session.addListener(sender);
        sender.sendMessage( String.format(Resources.get("listenerAdded"), gamename) );
        return true;
    }

    private boolean execExitListen(CommandSender sender) {

        GameSession session = GameSessionManager.getSessionByListener(sender);

        if ( session == null ) {
            sender.sendMessage(PREERR + Resources.get("listenerNotAdded"));
            return true;
        }

        session.removeListener(sender);
        sender.sendMessage( String.format(Resources.get("listenerExited"), session.name) );
        return true;
    }

    private void printUsage(CommandSender sender, String command) {

        String prefix = ChatColor.GRAY.toString() + "/" + command + " ";

        sender.sendMessage(prefix + Resources.get("usageNSingle"));
        sender.sendMessage(prefix + Resources.get("usageNVersus"));
        sender.sendMessage(prefix + Resources.get("usageAccept"));
        sender.sendMessage(prefix + Resources.get("usageSet"));
        sender.sendMessage(prefix + Resources.get("usageCall"));
        sender.sendMessage(prefix + Resources.get("usageCancel"));
        sender.sendMessage(prefix + Resources.get("usageHist"));
        sender.sendMessage(prefix + Resources.get("usageHistory"));
        sender.sendMessage(prefix + Resources.get("usageRanking"));
        sender.sendMessage(prefix + Resources.get("usageList"));
        sender.sendMessage(prefix + Resources.get("usageListen"));
    }

    private void printRanking(CommandSender sender) {

        String prefix = ChatColor.GRAY.toString() + "";

        sender.sendMessage(prefix + "Hit and Blow  Score Ranking");
        sender.sendMessage(prefix + "---------------------------");

        Vector<ScoreData> data = UserConfiguration.getRanking();

        int maxToDisplay = data.size();
        if ( data.size() > 10 ) {
            maxToDisplay = 10;
        }

        for ( int i=0; i<maxToDisplay; i++ ) {
            sender.sendMessage(prefix + data.elementAt(i).toString());
        }
    }

    private void printList(CommandSender sender) {

        String prefix = ChatColor.GRAY.toString() + "";
        Vector<GameSession> sessions = GameSessionManager.getSessions();

        sender.sendMessage(prefix + "Game Name - Current Status");
        sender.sendMessage(prefix + "---------------------------");
        for ( GameSession s : sessions )
            sender.sendMessage(prefix + s.toString());
    }
}
