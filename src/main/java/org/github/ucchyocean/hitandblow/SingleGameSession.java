/**
 *
 */
package org.github.ucchyocean.hitandblow;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.github.ucchyocean.misc.Resources;

/**
 * @author ucchy
 *
 */
public class SingleGameSession extends GameSession {

	public SingleGameSession(Player player, int level) {

		super (player, level);

		phase = GamePhase.SINGLE_CALL;
		makeAnswer();

		if ( HitAndBlowPlugin.getAnnounce() ) {
			Bukkit.broadcastMessage(ChatColor.GRAY +
					String.format("[" + HitAndBlowPlugin.NAME + "] " + Resources.get("announceSingleStart"),
							player.getName()));
		}

		printP1(ChatColor.GOLD + Resources.get("singleStarting"));

		runCallPhase();
	}

	protected void runCallPhase() {

		phase = GamePhase.SINGLE_CALL;

		printP1(ChatColor.RED + String.format(
				Resources.get("singleTurnStart1"),
				p1codeHistory.size() + 1));
		printP1(ChatColor.RED + String.format(
				Resources.get("singleTurnStart2"),
				level));
	}

	@Override
	protected void callNumber(Player player, String number) {

		String message = String.format(Resources.get("turnCalled1"), player.getName(), number);
		printP1(message);
		printToListeners(message);

		int[] call = parseS2I(number);
		int[] score = checkEatBite(p2answer, call);

		p1codeHistory.add(number);
		p1scoreHistory.add(score);

		if ( score[0] < level ) {
			message = String.format(Resources.get("turnCalled2"), number, score[0], score[1]);
			printP1(message);
			printToListeners(message);
			runCallPhase();
		} else {
			message = String.format(Resources.get("turnCalled3"), number, score[0]);
			printP1(message);
			printToListeners(message);

			printP1(ChatColor.GOLD + Resources.get("singleWon"));
			payReward(player1);

			if ( HitAndBlowPlugin.getAnnounce() ) {
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + HitAndBlowPlugin.NAME + "] " +
						String.format(Resources.get("announceSingleEnd"),
								player.getName(), p1codeHistory.size() ));
			}

			runEndPhase();
		}
	}

	@Override
	protected void cancelGame() {

		printP1(ChatColor.RED + Resources.get("canceled"));
		printToListeners( String.format(Resources.get("listenerCanceled"), name) );
		super.cancelGame();
	}

	private void payReward(Player player) {

		List<Double> rewards = HitAndBlowPlugin.getSingleRewards();
		int times = p1codeHistory.size();
		String unit = HitAndBlowPlugin.accountHandler.getUnitsPlural();

		printP1(String.format( Resources.get("singleWonPay1"), times ));

		if ( times <= rewards.size() ) {
			Double reward = rewards.get(times-1);
			printP1(String.format( Resources.get("singleWonPay2"), reward, unit ));
			HitAndBlowPlugin.accountHandler.addMoney(player.getName(), reward);

			UserConfiguration.addScore(player.getName(), reward);
		}
	}

	@Override
	protected Vector<String> getHistory(CommandSender sender) {

		Vector<String> history = new Vector<String>();

		history.add(String.format("Status: %s", getPhaseForPrint(phase)));
		history.add(String.format("%-10s", player1.getName()));
		history.add("----------------------");

		for ( int i=0; i<p1codeHistory.size(); i++ ) {

			int[] p1score = p1scoreHistory.elementAt(i);
			history.add(String.format("%s %dH%dB",
				p1codeHistory.elementAt(i), p1score[0], p1score[1] ));
		}

		if ( p2answer != null && sender == null ) {
			history.add("------- answer -------");
			history.add(String.format("%s", parseI2S(p2answer) ));
		}

		return history;
	}


	private String getPhaseForPrint(GamePhase phase) {

		if ( phase.equals(GamePhase.SINGLE_CALL) ) {
			return Resources.get("phaseSingle");
		} else if ( phase.equals(GamePhase.ENDED) ) {
			return Resources.get("phaseEnded");
		} else if ( phase.equals(GamePhase.CANCELED) ) {
			return Resources.get("phaseCanced");
		}

		return "unknown game phase";
	}

	/**
	 * @see org.github.ucchyocean.GameSession#isPlayerForSet(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForSet(Player player) {
		return false;
	}

	/**
	 * @see org.github.ucchyocean.GameSession#isPlayerForCall(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForCall(Player player) {
		return phase.equals(GamePhase.SINGLE_CALL);
	}

	/**
	 * @see org.github.ucchyocean.GameSession#isPlayerForCancel(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForCancel(Player player) {
		return phase.equals(GamePhase.SINGLE_CALL);
	}

	private void makeAnswer() {

		p2answer = new int[level];
		Random rnd = new Random();

		for ( int i=0; i<level; i++ ) {

			int number;
			do {
				number = rnd.nextInt(10);
			} while (!isAlreadyExists(i, number));
			p2answer[i] = number;
		}
	}

	private boolean isAlreadyExists(int index, int number) {

		for ( int i=0; i<index; i++ ) {
			if ( p2answer[i] == number ) {
				return false;
			}
		}
		return true;
	}

	public String toString() {

		return name + " - " + getPhaseForPrint(phase);
	}
}
