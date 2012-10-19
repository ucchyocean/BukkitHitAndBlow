package org.github.ucchyocean.hitandblow;

import java.util.Vector;

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

	public static boolean isPlayerForHistory(Player player) {

		GameSession session = getSessionByPlayer(player);
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
			} catch (HitAndBlowException e) {
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

	public static boolean printHistoryByPlayer(Player player) {

		GameSession session = getSessionByPlayer(player);
		if ( session != null ) {
			session.printHistory(player);
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

	private static void cleanSessions() {

		for ( GameSession s : sessions ) {
			if ( !s.player1.isOnline() ) {
				s.cancelGame();
				System.out.println(String.format("[%s] Game %s was canceled.", HitAndBlow.NAME, s.name));
			}
			if ( s instanceof VersusGameSession ) {
				VersusGameSession vs = (VersusGameSession)s;
				if ( !vs.player2.isOnline() ) {
					s.cancelGame();
					System.out.println(String.format("[%s] Game %s was canceled.", HitAndBlow.NAME, s.name));
				}
			}
		}
	}
}
