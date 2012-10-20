/**
 *
 */
package org.github.ucchyocean.hitandblow;

import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.github.ucchyocean.misc.Resources;

/**
 * @author ucchy
 *
 */
public class VersusGameSession extends GameSession {

	protected Player player2;

	private int[] p1answer;
	private Vector<int[]> p2scoreHistry;
	private Vector<String> p2codeHistry;

	protected VersusGameSession(Player player1, Player player2, int level) {

		super(player1, level);

		this.player2 = player2;

		this.p2scoreHistry = new Vector<int[]>();
		this.p2codeHistry = new Vector<String>();

		phase = GamePhase.VERSUS_PREPARE;
	}

	protected void runPreparePhase() {

		String unit = HitAndBlow.accountHandler.getUnitsPlural();

		printP1( String.format(Resources.get("versusNewgameSend"),
				player2.getName()) );

		printP2( String.format(Resources.get("versusNewgameReceive1"),
				player2.getName()) );
		printP2(String.format(Resources.get("versusNewgameReceive2"),
				HitAndBlow.getVersusStake(), unit));
		printP2(Resources.get("versusNewgameReceive3"));
	}

	protected boolean runSetNumberPhase() {

		String unit = HitAndBlow.accountHandler.getUnitsPlural();

		if ( !HitAndBlow.accountHandler.hasFunds(player1.getName(), HitAndBlow.getVersusStake())) {
			printBoth(String.format(Resources.get("versusPartnerNotHaveFunds"),
					player1.getName(), HitAndBlow.getVersusStake(), unit));
			return false;
		}
		if ( !HitAndBlow.accountHandler.hasFunds(player2.getName(), HitAndBlow.getVersusStake())) {
			printBoth(String.format(Resources.get("versusPartnerNotHaveFunds"),
					player2.getName(), HitAndBlow.getVersusStake(), unit));
			return false;
		}

		HitAndBlow.accountHandler.chargeMoney(player1.getName(), HitAndBlow.getVersusStake());
		HitAndBlow.accountHandler.chargeMoney(player2.getName(), HitAndBlow.getVersusStake());

		phase = GamePhase.VERSUS_SETNUMBER;

		printBoth(String.format(Resources.get("versusStarting1"),
				HitAndBlow.getVersusStake(), unit));
		printBoth(String.format(Resources.get("versusStarting2"),
				level));

		return true;
	}

	protected boolean setAnswer(Player player, String answer) {

		if ( !phase.equals(GamePhase.VERSUS_SETNUMBER) ) {
			return false;
		}

		if ( player1.equals(player) ) {
			p1answer = parseS2I(answer);
		} else {
			p2answer = parseS2I(answer);
		}

		printBoth( String.format(Resources.get("versusSet"),
				player.getName()) );

		if (p1answer != null && p2answer != null) {
			runGamePhase();
		}

		return true;
	}

	protected void runGamePhase() {

		if ( HitAndBlow.getAnnounce() ) {
			Bukkit.broadcastMessage(ChatColor.GRAY + "[" + HitAndBlow.NAME + "] " +
					String.format(Resources.get("announceVersusStart"),
							player1.getName(), player2.getName() ));
		}

		printBoth(ChatColor.GOLD + Resources.get("singleStarting"));

		runCallPhase(player2);
	}

	protected void runCallPhase(Player player) {

		if ( player1.equals(player) ) {
			phase = GamePhase.VERSUS_P1CALL;
		} else {
			phase = GamePhase.VERSUS_P2CALL;
		}

		printBoth(String.format(Resources.get("versusTurnStart1"), player.getName()));
		printTo(player, ChatColor.RED + Resources.get("versusTurnStart2"));
		printTo(player, ChatColor.RED + String.format(Resources.get("singleTurnStart2"), level));
	}

	@Override
	protected void callNumber(Player player, String number) throws HitAndBlowException {

		List<String> codeHistory;
		List<int[]> scoreHistory;
		int[] answer;

		if ( phase == GamePhase.VERSUS_P1CALL && player1.equals(player) ) {
			codeHistory = p1codeHistory;
			scoreHistory = p1scoreHistory;
			answer = p2answer;
		} else if ( phase == GamePhase.VERSUS_P2CALL && player2.equals(player) ) {
			codeHistory = p2codeHistry;
			scoreHistory = p2scoreHistry;
			answer = p1answer;
		} else {
			throw new HitAndBlowException("Phase was broken!");
		}

		printBoth( String.format(Resources.get("turnCalled1"), player.getName(), number) );

		int[] call = parseS2I(number);
		int[] score = checkEatBite(answer, call);

		codeHistory.add(number);
		scoreHistory.add(score);

		if ( score[0] < level ) {
			printBoth(String.format(
					Resources.get("turnCalled2"),
					number, score[0], score[1]) );
			if ( phase == GamePhase.VERSUS_P1CALL ) {
				runCallPhase(player2);
			} else {
				runCallPhase(player1);
			}
		} else {
			printBoth(String.format(
					Resources.get("turnCalled3"),
					number, score[0]) );

			printTo(player, ChatColor.GOLD + Resources.get("versusWon"));
			payReward(player);

			printToOther(player, ChatColor.GOLD + Resources.get("versusLost"));

			if ( HitAndBlow.getAnnounce() ) {
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + HitAndBlow.NAME + "] " +
						String.format(Resources.get("announceVersusEnd"),
								player.getName(),
								getOtherPlayer(player).getName(),
								codeHistory.size() ));
			}

			runEndPhase();
		}
	}

	private void payReward(Player player) {

		String unit = HitAndBlow.accountHandler.getUnitsPlural();
		Double reward = HitAndBlow.getVersusReward();
		printTo(player, String.format( Resources.get("singleWonPay2"), reward, unit ));
		HitAndBlow.accountHandler.addMoney( player.getName(), reward );

		UserConfiguration.addScore(player.getName(), reward);
	}

	@Override
	protected void cancelGame() {

		String unit = HitAndBlow.accountHandler.getUnitsPlural();

		printBoth( ChatColor.RED + Resources.get("canceled"));

		if ( phase.equals(GamePhase.VERSUS_SETNUMBER)
				|| phase.equals(GamePhase.VERSUS_P1CALL)
				|| phase.equals(GamePhase.VERSUS_P2CALL) ) {

			// returning funds for online player.
			if ( player1.isOnline() ) {
				printP1( String.format( Resources.get("versusCancelReturn"),
						HitAndBlow.getVersusStake(), unit ) );
				HitAndBlow.accountHandler.addMoney(player1.getName(), HitAndBlow.getVersusStake());
			}
			if ( player2.isOnline() ) {
				printP2( String.format( Resources.get("versusCancelReturn"),
						HitAndBlow.getVersusStake(), unit ) );
				HitAndBlow.accountHandler.addMoney(player2.getName(), HitAndBlow.getVersusStake());
			}
		}

		super.cancelGame();
	}

	public boolean isVersusPreparePhase() {

		return phase.equals(GamePhase.VERSUS_PREPARE);
	}

	public boolean isVersusSetNumberPhase() {

		return phase.equals(GamePhase.VERSUS_SETNUMBER);
	}

	public boolean isVersusGamePhase() {

		return (isVersusP1CallPhase() || isVersusP2CallPhase());
	}

	public boolean isVersusP1CallPhase() {

		return phase.equals(GamePhase.VERSUS_P1CALL);
	}

	public boolean isVersusP2CallPhase() {

		return phase.equals(GamePhase.VERSUS_P2CALL);
	}

	/* (非 Javadoc)
	 * @see org.github.ucchyocean.GameSession#isPlayerForSet(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForSet(Player player) {

		return phase.equals(GamePhase.VERSUS_SETNUMBER);
	}

	/* (非 Javadoc)
	 * @see org.github.ucchyocean.GameSession#isPlayerForCall(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForCall(Player player) {

		if (player1.equals(player) && phase.equals(GamePhase.VERSUS_P1CALL)) {
			return true;
		} else if (player2.equals(player) && phase.equals(GamePhase.VERSUS_P2CALL)) {
			return true;
		}

		return false;
	}

	/* (非 Javadoc)
	 * @see org.github.ucchyocean.GameSession#isPlayerForCancel(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForCancel(Player player) {

		return !(phase.equals(GamePhase.CANCELED) || phase.equals(GamePhase.ENDED));
	}

	@Override
	protected Vector<String> getHistory(Player player) {

		Vector<String> history = new Vector<String>();

		history.add(String.format("Status: %s", getPhaseForPrint(phase, player)));
		history.add(String.format("%-10s %-10s",
				player1.getName(), player2.getName()));
		history.add("----------------------");

		for ( int i=0; i<p1codeHistory.size(); i++ ) {

			int[] p1score = p1scoreHistory.elementAt(i);
			int[] p2score = p2scoreHistry.elementAt(i);
			history.add(String.format("%s %dH%dB   %s %dH%dB",
				p1codeHistory.elementAt(i), p1score[0], p1score[1],
				p2codeHistry.elementAt(i), p2score[0], p2score[1] ));
		}

		if ( p1codeHistory.size() < p2codeHistry.size() ) {

			int[] p2score = p2scoreHistry.lastElement();
			history.add(String.format("           %s %dH%dB",
					p2codeHistry.lastElement(), p2score[0], p2score[1] ));
		}

		if ( p1answer != null && p2answer != null ) {
			history.add("------- answer -------");
			if ( player == null ) {
				history.add(String.format("%s        %s",
						parseI2S(p2answer), parseI2S(p1answer) ));
			} else if ( player.equals(player1) ) {
				history.add(String.format("???       %s",
						parseI2S(p1answer) ));
			} else if ( player.equals(player2) ) {
				history.add(String.format("%s        ???",
						parseI2S(p2answer) ));
			}
		}

		return history;
	}

	private String getPhaseForPrint(GamePhase phase, Player player) {

		if ( phase.equals(GamePhase.VERSUS_PREPARE) ) {
			return Resources.get("phaseVersusPrepare");
		} else if ( phase.equals(GamePhase.VERSUS_SETNUMBER) ) {
			return Resources.get("phaseVersusSetnumber");
		} else if ( phase.equals(GamePhase.VERSUS_P1CALL) ) {
			if ( player1.equals(player) ) {
				return ChatColor.RED + Resources.get("phaseVersusYourTurn");
			} else if ( player2.equals(player) ) {
				return Resources.get("phaseVersusOtherTurn");
			} else {
				return Resources.get("phaseVersusCallTurn");
			}
		} else if ( phase.equals(GamePhase.VERSUS_P2CALL) ) {
			if ( player1.equals(player) ) {
				return Resources.get("phaseVersusOtherTurn");
			} else if ( player2.equals(player) ) {
				return ChatColor.RED + Resources.get("phaseVersusYourTurn");
			} else {
				return Resources.get("phaseVersusCallTurn");
			}
		} else if ( phase.equals(GamePhase.ENDED) ) {
			return Resources.get("phaseEnded");
		} else if ( phase.equals(GamePhase.CANCELED) ) {
			return Resources.get("phaseCanced");
		}

		return "unknown game phase";
	}

	private void printBoth(String message) {

		printP1(message);
		printP2(message);
	}

	private void printP2(String message) {

		printTo(player2, message);
	}

	private void printToOther(Player player, String message) {

		printTo(getOtherPlayer(player), message);
	}

	private Player getOtherPlayer(Player player) {

		if ( player1.equals(player) )
			return player2;
		else
			return player1;
	}
}
