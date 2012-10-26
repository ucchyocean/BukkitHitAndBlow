/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow.handler;

import java.util.List;

import org.bukkit.entity.Player;

import com.github.ucchyocean.hitandblow.HitAndBlowPlugin;
import com.github.ucchyocean.hitandblow.Resources;
import com.github.ucchyocean.hitandblow.UserConfiguration;

/**
 * @author ucchy
 *
 */
public class ChargeMediator {

    public enum Mode { ITEM, ACCOUNT };

    private Mode mode;
    private AccountHandler accountHandler;
    private ItemHandler itemHandler;

    public ChargeMediator(Mode mode) {
        this.mode = mode;

        if ( mode.equals(Mode.ACCOUNT) ) {
            try {
                accountHandler = new AccountHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ( mode.equals(Mode.ITEM) ) {
            itemHandler = new ItemHandler();
        }
    }

    public boolean isSinglePayTimes(int callTimes) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            return ( callTimes <= HitAndBlowPlugin.config.getSingleMoneyRewards().size() );
        } else if ( mode.equals(Mode.ITEM) ) {
            return ( callTimes <= HitAndBlowPlugin.config.getSingleItemRewards().size() );
        }
        return false;
    }

    public void paySingleReward(Player player, int callTimes) {

        if ( !isSinglePayTimes(callTimes) ) {
            return;
        }

        if ( mode.equals(Mode.ACCOUNT) ) {

            double reward = HitAndBlowPlugin.config.getSingleMoneyRewards().get(callTimes-1);
            accountHandler.addMoney(player.getName(), reward);

            UserConfiguration.addScore(player.getName(), reward);

        } else if ( mode.equals(Mode.ITEM) ) {

            List<Integer> rewards = HitAndBlowPlugin.config.getSingleItemRewards();
            int reward = rewards.get(callTimes-1);
            itemHandler.addItem(player, reward, 1);

            double score = (double)(rewards.size() - callTimes + 1);
            UserConfiguration.addScore(player.getName(), score);
        }
    }

    public boolean hasVersusStake(Player player) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            return accountHandler.hasFunds(
                    player.getName(), HitAndBlowPlugin.config.getVersusMoneyStake());
        } else if ( mode.equals(Mode.ITEM) ) {
            return itemHandler.hasItem(
                    player, HitAndBlowPlugin.config.getVersusItemStake(), 1);
        }
        return false;
    }

    public void chargeVersusStake(Player player) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            accountHandler.chargeMoney(
                    player.getName(), HitAndBlowPlugin.config.getVersusMoneyStake());
        } else if ( mode.equals(Mode.ITEM) ) {
            itemHandler.chargeItem(
                    player, HitAndBlowPlugin.config.getVersusItemStake(), 1);
        }
    }

    public void payVersusStake(Player player) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            accountHandler.addMoney(
                    player.getName(), HitAndBlowPlugin.config.getVersusMoneyStake());
        } else if ( mode.equals(Mode.ITEM) ) {
            itemHandler.addItem(
                    player, HitAndBlowPlugin.config.getVersusItemStake(), 1);
        }
    }

    public void payVersusReward(Player player) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            accountHandler.addMoney(
                    player.getName(), HitAndBlowPlugin.config.getVersusMoneyReward());
        } else if ( mode.equals(Mode.ITEM) ) {
            itemHandler.addItem(
                    player, HitAndBlowPlugin.config.getVersusItemStake(), 2);
        }
    }

    public String getDisplaySingleReward(int callTimes) {
        if ( mode.equals(Mode.ACCOUNT) ) {
            double reward = HitAndBlowPlugin.config.getSingleMoneyRewards().get(callTimes-1);
            String unit = accountHandler.getUnitsPlural();
            return String.format("%.2f%s", reward, unit);
        } else if ( mode.equals(Mode.ITEM) ) {
            int id = HitAndBlowPlugin.config.getSingleItemRewards().get(callTimes-1);
            return itemHandler.getDisplayName(id);
        }
        return "";
    }

    public String getDisplayVersusStake() {
        if ( mode.equals(Mode.ACCOUNT) ) {
            String unit = accountHandler.getUnitsPlural();
            return String.format("%.2f%s", HitAndBlowPlugin.config.getVersusMoneyStake(), unit);
        } else if ( mode.equals(Mode.ITEM) ) {
            return itemHandler.getDisplayName(HitAndBlowPlugin.config.getVersusItemStake());
        }
        return "";
    }

    public String getDisplayVersusReward() {
        if ( mode.equals(Mode.ACCOUNT) ) {
            String unit = accountHandler.getUnitsPlural();
            return String.format("%.2f%s", HitAndBlowPlugin.config.getVersusMoneyReward(), unit);
        } else if ( mode.equals(Mode.ITEM) ) {
            String item = itemHandler.getDisplayName(HitAndBlowPlugin.config.getVersusItemStake());
            return String.format(Resources.get("versusWonItemReward"), item);
        }
        return "";
    }
}
