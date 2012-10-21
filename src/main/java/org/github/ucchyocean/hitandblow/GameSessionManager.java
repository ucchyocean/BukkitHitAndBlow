package org.github.ucchyocean.hitandblow;

import java.util.Vector;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameSessionManager {

	private static Vector<GameSession> sessions;

	static {
		sessions = new Vector<GameSession>();
	}

	public static void addSession(GameSession session) {

		cleanSessions();
		sessions.add(session);
	}

	public static void removeSession(GameSession session) {

		cleanSessions();
		sessions.remove(session);
	}

	public static Vector<GameSession> getSessions() {

		return sessions;
	}

	public static boolean isPlayerForAccept(Player player) {

		VersusGameSession session = getVersusSessionByClientPlayer(player);
		return (session != null && session.isVersusPreparePhase());
	}

	public static boolean isPlayerForSet(Player player) {

		GameSession session = getSessionByPlayer(player);
		return (session != null && session.isPlayerForSet(player));
	}

	public static boolean isPlayerForCall(Player player) {

		GameSession session = getSessionByPlayer(player);
		return (session != null && session.isPlayerForCall(player));
	}

	public static boolean isPlayerForCancel(Player player) {

		GameSession session = getSessionByPlayer(player);
		return (session != null && session.isPlayerForCancel(player));
	}

	public static boolean isCommandSenderForHistory(CommandSender sender) {

		GameSession session = getSessionByPlayerAndListener(sender);
		return (session != null);
	}

	public static boolean runSetNumberPhaseByPlayer(Player player) {

		VersusGameSession session = (VersusGameSession)getSessionByPlayer(player);
		if ( session != null ) {
			session.runSetNumberPhase();
			return true;
		}

		return false;
	}

	public static boolean setNumberByPlayer(Player player, String number) {

		VersusGameSession session = (VersusGameSession)getSessionByPlayer(player);
		if ( session != null ) {
			return session.setAnswer(player, number);
		}

		return false;
	}

	public static boolean callNumberByPlayer(Player player, String number) {

		GameSession session = getSessionByPlayer(player);
		if ( session != null ) {
			try {
				session.callNumber(player, number);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		return false;
	}

	public static boolean cancelGameByPlayer(Player player) {

		GameSession session = getSessionByPlayer(player);
		if ( session != null ) {
			session.cancelGame();
			return true;
		}

		return false;
	}

	public static boolean printHistoryByPlayer(CommandSender sender) {

		GameSession session = getSessionByPlayerAndListener(sender);
		if ( session != null ) {
			session.printHistory(sender);
			return true;
		}

		return false;
	}

	public static boolean isPlayerInGame(Player player) {

		return (getSessionByPlayer(player) != null);
	}

	public static GameSession getSessionByPlayer(Player player) {

		for ( GameSession s : sessions ) {
			if ( s.player1.equals(player) ) {
				return s;
			} else if ( s instanceof VersusGameSession ) {
				VersusGameSession vs = (VersusGameSession)s;
				if ( vs.player2.equals(player) ) {
					return vs;
				}
			}
		}

		return null;
	}

	public static GameSession getSessionByPlayerAndListener(CommandSender sender) {

		for ( GameSession s : sessions ) {
			if ( s.player1.equals(sender) || s.listeners.contains(sender) ) {
				return s;
			} else if ( s instanceof VersusGameSession ) {
				VersusGameSession vs = (VersusGameSession)s;
				if ( vs.player2.equals(sender) ) {
					return vs;
				}
			}
		}

		return null;
	}

	public static GameSession getSessionByListener(CommandSender sender) {

		for ( GameSession s : sessions ) {
			if ( s.listeners.contains(sender) ) {
				return s;
			}
		}

		return null;
	}

	public static VersusGameSession getVersusSessionByOwnerPlayer(Player player) {

		for ( GameSession s : sessions ) {
			if ( s instanceof VersusGameSession && s.player1.equals(player) ) {
				return (VersusGameSession)s;
			}
		}

		return null;
	}

	public static VersusGameSession getVersusSessionByClientPlayer(Player player) {

		for ( GameSession s : sessions ) {
			if ( s instanceof VersusGameSession ) {
				VersusGameSession vs = (VersusGameSession)s;
				if ( vs.player2.equals(player) ) {
					return vs;
				}
			}
		}

		return null;
	}

	public static GameSession getSessionByName(String name) {

		for ( GameSession s : sessions ) {
			if ( s.name.equalsIgnoreCase(name) ) {
				return s;
			}
		}

		return null;
	}

	private static void cleanSessions() {

		for ( GameSession s : sessions ) {
			if ( !isValidPlayer(s.player1) ) {
				s.cancelGame();
				//System.out.println(String.format("[%s] Game %s was canceled.", HitAndBlowPlugin.NAME, s.name));
			}
			if ( s instanceof VersusGameSession ) {
				VersusGameSession vs = (VersusGameSession)s;
				if ( !isValidPlayer(vs.player2) ) {
					s.cancelGame();
					//System.out.println(String.format("[%s] Game %s was canceled.", HitAndBlowPlugin.NAME, s.name));
				}
			}
		}
	}

	private static boolean isValidPlayer(Player player) {

		if ( player == null )
			return false;
		if ( !player.isOnline() )
			return false;
		if ( !player.equals( HitAndBlowPlugin.instance.getPlayer(player.getName()) ) )
			return false;
		return true;
	}
}
