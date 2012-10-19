/**
 *
 */
package org.github.ucchyocean.hitandblow;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

		if ( HitAndBlow.getAnnounce() ) {
			Bukkit.broadcastMessage(ChatColor.GRAY +
					String.format("[" + HitAndBlow.NAME + "] " + Resources.get("announceSingleStart"),
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

		printP1( String.format(Resources.get("turnCalled1"), player.getName(), number) );

		int[] call = parseS2I(number);
		int[] score = checkEatBite(p2answer, call);

		p1codeHistory.add(number);
		p1scoreHistory.add(score);

		if ( score[0] < level ) {
			printP1(String.format(
					Resources.get("turnCalled2"),
					number, score[0], score[1]) );
			runCallPhase();
		} else {
			printP1(String.format(
					Resources.get("turnCalled3"),
					number, score[0]) );

			printP1(ChatColor.GOLD + Resources.get("singleWon"));
			payReward(player1);

			if ( HitAndBlow.getAnnounce() ) {
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + HitAndBlow.NAME + "] " +
						String.format(Resources.get("announceSingleEnd"),
								player.getName(), p1codeHistory.size() ));
			}

			runEndPhase();
		}
	}

	@Override
	protected void cancelGame() {

		printP1(ChatColor.RED + Resources.get("canceled"));
		super.cancelGame();
	}

	private void payReward(Player player) {

		List<Double> rewards = HitAndBlow.getSingleRewards();
		int times = p1codeHistory.size();
		String unit = HitAndBlow.accountHandler.getUnitsPlural();

		printP1(String.format( Resources.get("singleWonPay1"), times ));

		if ( times <= rewards.size() ) {
			Double reward = rewards.get(times-1);
			printP1(String.format( Resources.get("singleWonPay2"), reward, unit ));
			HitAndBlow.accountHandler.addMoney(player.getName(), reward);

			UserConfiguration.addScore(player.getName(), reward);
		}
	}

	protected Vector<String> getHistory(boolean withAnswer) {

		Vector<String> history = new Vector<String>();

		history.add(String.format("Status: %s", phase));
		history.add(String.format("%-10s", player1.getName()));
		history.add("----------------------");

		for ( int i=0; i<p1codeHistory.size(); i++ ) {

			int[] p1score = p1scoreHistory.elementAt(i);
			history.add(String.format("%s %dH%dB",
				p1codeHistory.elementAt(i), p1score[0], p1score[1] ));
		}

		if ( p2answer != null && withAnswer ) {
			history.add("------- answer -------");
			history.add(String.format("%s", parseI2S(p2answer) ));
		}

		return history;
	}

	/* (非 Javadoc)
	 * @see org.github.ucchyocean.GameSession#isPlayerForSet(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForSet(Player player) {
		return false;
	}

	/* (非 Javadoc)
	 * @see org.github.ucchyocean.GameSession#isPlayerForCall(org.bukkit.entity.Player)
	 */
	@Override
	protected boolean isPlayerForCall(Player player) {
		return phase.equals(GamePhase.SINGLE_CALL);
	}

	/* (非 Javadoc)
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
}
