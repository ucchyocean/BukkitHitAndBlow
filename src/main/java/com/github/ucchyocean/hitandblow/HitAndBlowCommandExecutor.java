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

    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        String prefix = String.format("[%s] ", HitAndBlowPlugin.NAME);
        String preErr = ChatColor.DARK_RED + prefix;

        if ( args.length == 0 ) {
            printUsage(sender, command.getName());
            return true;
        }

        if ( !(sender instanceof Player) ) {

            if ( args.length >= 1 ) {

                if ( args[0].equalsIgnoreCase("hist") || args[0].equalsIgnoreCase("history") ) {

                    if ( !GameSessionManager.isCommandSenderForHistory(sender) ) {
                        sender.sendMessage(preErr + Resources.get("notPlayerInGame"));
                        return true;
                    }

                    return GameSessionManager.printHistoryByPlayer(sender);

                } if ( args[0].equalsIgnoreCase("rank") ) {

                    printRanking(sender);
                    return true;

                } else if ( args[0].equalsIgnoreCase("list") ) {

                    printList(sender);
                    return true;

                } else if ( args[0].equalsIgnoreCase("listen") ) {

                    if ( args.length < 2 ) {
                        sender.sendMessage(preErr + Resources.get("listenerSetName"));
                        return true;
                    }

                    String gamename = args[1];
                    GameSession session = GameSessionManager.getSessionByName(gamename);

                    if ( session == null ) {
                        sender.sendMessage(preErr + Resources.get("listenerNotFound"));
                        return true;
                    }

                    GameSession already = GameSessionManager.getSessionByListener(sender);
                    if ( already != null ) {
                        sender.sendMessage(preErr +
                                String.format(Resources.get("listenerAlreadyAdded"), already.name) );
                        return true;
                    }

                    session.addListener(sender);
                    sender.sendMessage( String.format(Resources.get("listenerAdded"), gamename) );
                    return true;

                } else if ( args[0].equalsIgnoreCase("exitlisten") ) {

                    GameSession session = GameSessionManager.getSessionByListener(sender);

                    if ( session == null ) {
                        sender.sendMessage(preErr + Resources.get("listenerNotAdded"));
                        return true;
                    }

                    session.removeListener(sender);
                    sender.sendMessage( String.format(Resources.get("listenerExited"), session.name) );
                    return true;

                }
            }

            sender.sendMessage(preErr + Resources.get("cannotRunOnConsole"));
            return true;
        }

        Player player = (Player)sender;

        if ( args[0].equalsIgnoreCase("newgame") ) {

            if ( GameSessionManager.isPlayerInGame(player) ) {
                sender.sendMessage(preErr + Resources.get("cannotRunInGame"));
                return true;
            }

            if ( args.length < 2 ) {
                // Single game mode.

                if ( HitAndBlowPlugin.getSingleDialyTimes() != 0 ) {
                    int times = UserConfiguration.getUserDailyPlayTimes(player.getName());
                    if ( HitAndBlowPlugin.getSingleDialyTimes() <= times ) {
                        sender.sendMessage(preErr + Resources.get("singleExpire"));
                        return true;
                    }
                    sender.sendMessage(prefix + String.format(Resources.get("singleTimes"), times+1));
                    sender.sendMessage(prefix + String.format(Resources.get("singleLeast"),
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
                    sender.sendMessage(preErr + Resources.get("versusOffline"));
                    return true;
                } else if ( GameSessionManager.isPlayerInGame(other) ) {
                    sender.sendMessage(preErr + Resources.get("versusPartnerInGame"));
                    return true;
                } else if ( player.getName().equals(other.getName()) ) {
                    sender.sendMessage(preErr + Resources.get("versusSelf"));
                    return true;
                }

                if ( !HitAndBlowPlugin.accountHandler.hasFunds(player.getName(), stake) ) {
                    sender.sendMessage(preErr + String.format(Resources.get("versusDontHaveFunds"), stake, unit));
                    return true;
                }

                if ( args.length >= 3 ) {
                    if ( args[2].matches("[2-7]") ) {
                        level = Integer.parseInt(args[2]);
                    } else {
                        sender.sendMessage(preErr + Resources.get("versusLevelWasnotInt"));
                        return true;
                    }
                }

                VersusGameSession session = new VersusGameSession(player, other, level);

                session.runPreparePhase();

                return true;
            }

        } else if ( args[0].equalsIgnoreCase("accept") ) {

            if ( !GameSessionManager.isPlayerForAccept(player) ) {
                sender.sendMessage(preErr + Resources.get("versusCannotAccept"));
                return true;
            }

            Double stake = HitAndBlowPlugin.getVersusStake();
            String unit = HitAndBlowPlugin.accountHandler.getUnitsPlural();

            if ( !HitAndBlowPlugin.accountHandler.hasFunds(player.getName(), stake) ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusDontHaveFunds"), stake, unit));
                return true;
            }

            return GameSessionManager.runSetNumberPhaseByPlayer(player);

        } else if ( args[0].equalsIgnoreCase("set") ) {

            if ( !GameSessionManager.isPlayerForSet(player) ) {
                sender.sendMessage(preErr + Resources.get("versusNotPlayerInGame"));
                return true;
            }

            if ( args.length < 2 ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusSetNumber"), command.getName()));
                return true;
            }

            int level = GameSessionManager.getSessionByPlayer(player).getLevel();
            String answer = args[1];

            if ( answer.length() != level ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberFigures"), level));
                return true;
            }

            if ( !answer.matches("[0-9]+") ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberFigures"), level));
                return true;
            }

            for ( int i=0; i<(level-1); i++ ) {
                for ( int j=i+1; j<level; j++ ) {
                    if ( answer.charAt(i) == answer.charAt(j) ) {
                        sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberSame"), level));
                        return true;
                    }
                }
            }

            return GameSessionManager.setNumberByPlayer(player, args[1]);

        } else if ( args[0].equalsIgnoreCase("call") ) {

            if ( !GameSessionManager.isPlayerForCall(player) ) {
                sender.sendMessage(preErr + Resources.get("notPlayerForCall"));
                return true;
            }

            if ( args.length < 2 ) {
                sender.sendMessage(preErr + String.format(Resources.get("pleaseCall"), command.getName()));
                return true;
            }

            int level = GameSessionManager.getSessionByPlayer(player).getLevel();
            String answer = args[1];

            if ( answer.length() != level ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberFigures"), level));
                return true;
            }

            if ( !answer.matches("[0-9]+") ) {
                sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberFigures"), level));
                return true;
            }

            for ( int i=0; i<(level-1); i++ ) {
                for ( int j=i+1; j<level; j++ ) {
                    if ( answer.charAt(i) == answer.charAt(j) ) {
                        sender.sendMessage(preErr + String.format(Resources.get("versusSetNumberSame"), level));
                        return true;
                    }
                }
            }

            return GameSessionManager.callNumberByPlayer(player, args[1]);

        } else if ( args[0].equalsIgnoreCase("cancel") ) {

            if ( !GameSessionManager.isPlayerForCancel(player) ) {
                sender.sendMessage(preErr + Resources.get("notPlayerForCancel"));
                return true;
            }

            return GameSessionManager.cancelGameByPlayer(player);

        } else if ( args[0].equalsIgnoreCase("hist") || args[0].equalsIgnoreCase("history") ) {

            if ( !GameSessionManager.isCommandSenderForHistory(player) ) {
                sender.sendMessage(preErr + Resources.get("notPlayerInGame"));
                return true;
            }

            return GameSessionManager.printHistoryByPlayer(player);

        } else if ( args[0].equalsIgnoreCase("rank") ) {

            printRanking(sender);
            return true;

        } else if ( args[0].equalsIgnoreCase("list") ) {

            printList(sender);
            return true;

        } else if ( args[0].equalsIgnoreCase("listen") ) {

            if ( args.length < 2 ) {
                sender.sendMessage(preErr + Resources.get("listenerSetName"));
                return true;
            }

            String gamename = args[1];
            GameSession session = GameSessionManager.getSessionByName(gamename);

            if ( session == null ) {
                sender.sendMessage(preErr +
                        String.format(Resources.get("listenerNotFound"), gamename) );
                return true;
            }

            if ( session.player1.equals(sender) ) {
                sender.sendMessage(preErr +
                        String.format(Resources.get("listenerPlayer"), gamename) );
                return true;
            } else if ( session instanceof VersusGameSession ) {
                if ( ((VersusGameSession)session).player2.equals(sender) ) {
                    sender.sendMessage(preErr +
                            String.format(Resources.get("listenerPlayer"), gamename) );
                    return true;
                }
            }

            GameSession already = GameSessionManager.getSessionByListener(sender);
            if ( already != null ) {
                sender.sendMessage(preErr +
                        String.format(Resources.get("listenerAlreadyAdded"), already.name) );
                return true;
            }

            session.addListener(sender);
            sender.sendMessage( String.format(Resources.get("listenerAdded"), gamename) );
            return true;

        } else if ( args[0].equalsIgnoreCase("exitlisten") ) {

            GameSession session = GameSessionManager.getSessionByListener(sender);

            if ( session == null ) {
                sender.sendMessage(preErr + Resources.get("listenerNotAdded"));
                return true;
            }

            session.removeListener(sender);
            sender.sendMessage( String.format(Resources.get("listenerExited"), session.name) );
            return true;

        }

        return false;
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
