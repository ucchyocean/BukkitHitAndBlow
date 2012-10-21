/**
 *
 */
package org.github.ucchyocean.hitandblow;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author ucchy
 *
 */
public class PlayerLogoutListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		Player player = event.getPlayer();

		GameSession session = GameSessionManager.getSessionByPlayer(player);

		if ( session != null ) {
			session.cancelGame();
		}
	}
}
