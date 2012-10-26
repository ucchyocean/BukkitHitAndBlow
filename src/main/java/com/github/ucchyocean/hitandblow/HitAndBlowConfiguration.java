/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.Configuration;

import com.github.ucchyocean.hitandblow.handler.ChargeMediator.Mode;

/**
 * @author ucchy
 *
 */
public class HitAndBlowConfiguration {

    private static final String KEY_CONF_ANNOUNCE = "announce";
    private static final String KEY_CONF_LANG = "lang";
    private static final String KEY_CONF_MODE = "mode";
    private static final String KEY_CONF_SINGLE_LEVEL = "single.level";
    private static final String KEY_CONF_SINGLE_ITEM_REWARDS = "single.itemRewards";
    private static final String KEY_CONF_SINGLE_MONEY_REWARDS = "single.moneyRewards";
    private static final String KEY_CONF_SINGLE_DAILY_TIMES = "single.dailyTimes";
    private static final String KEY_CONF_VERSUS_LEVEL = "versus.level";
    private static final String KEY_CONF_VERSUS_ITEM_STAKE = "versus.itemStake";
    private static final String KEY_CONF_VERSUS_MONEY_STAKE = "versus.moneyStake";
    private static final String KEY_CONF_VERSUS_MONEY_REWARD = "versus.moneyReward";

    private static final int MIN_LEVEL = 2;
    private static final int MAX_LEVEL = 7;

    private static final boolean DEFAULT_ANNOUNCE = true;
    private static final String DEFAULT_LANG = "en";
    private static final Mode DEFAULT_MODE = Mode.ITEM;

    private static final int DEFAULT_SINGLE_LEVEL = 3;
    private static final int[] DEFAULT_SINGLE_ITEM_REWARDS =
        {57, 41, 264, 42, 266, 265, 341, 364, 297, 17};
    private static final double[] DEFAULT_SINGLE_MONEY_REWARDS =
        {100000.0, 10000.0, 2500.0, 1000.0, 250.0, 100.0, 50.0, 25.0, 10.0, 5.0};
    private static final int DEFAULT_SINGLE_DAILY_TIMES = 3;

    private static final int DEFAULT_VERSUS_LEVEL = 3;
    private static final int DEFAULT_VERSUS_ITEM_STAKE = 264;
    private static final double DEFAULT_VERSUS_MONEY_STAKE = 100.0;
    private static final double DEFAULT_VERSUS_MONEY_REWARD = 200.0;

    private Configuration config;

    public HitAndBlowConfiguration(Configuration config) {
        this.config = config;
    }

    public boolean getAnnounce() {
        return config.getBoolean(KEY_CONF_ANNOUNCE, DEFAULT_ANNOUNCE);
    }

    public String getLang() {
        return config.getString(KEY_CONF_LANG, DEFAULT_LANG);
    }

    public Mode getMode() {
        String tempMode = config.getString(KEY_CONF_MODE, DEFAULT_MODE.toString());
        if ( tempMode.equalsIgnoreCase("money") ) {
            return Mode.ACCOUNT;
        } else {
            return Mode.ITEM;
        }
    }

    public int getSingleLevel() {
        int level = config.getInt(KEY_CONF_SINGLE_LEVEL, DEFAULT_SINGLE_LEVEL);
        if ( level < MIN_LEVEL ) {
            level = MIN_LEVEL;
        } else if ( level > MAX_LEVEL ) {
            level = MAX_LEVEL;
        }
        return level;
    }

    public List<Integer> getSingleItemRewards() {
        List<Integer> singleItemRewards = config.getIntegerList(KEY_CONF_SINGLE_ITEM_REWARDS);
        if ( singleItemRewards != null ) {
            return singleItemRewards;
        } else {
            List<Integer> a = new ArrayList<Integer>();
            for ( int i : DEFAULT_SINGLE_ITEM_REWARDS ) {
                a.add(i);
            }
            return a;
        }
    }

    public List<Double> getSingleMoneyRewards() {
        List<Double> singleMoneyRewards = config.getDoubleList(KEY_CONF_SINGLE_MONEY_REWARDS);
        if ( singleMoneyRewards != null ) {
            return singleMoneyRewards;
        } else {
            List<Double> a = new ArrayList<Double>();
            for ( double d : DEFAULT_SINGLE_MONEY_REWARDS ) {
                a.add(d);
            }
            return a;
        }
    }

    public int getSingleDailyTimes() {
        return config.getInt(KEY_CONF_SINGLE_DAILY_TIMES, DEFAULT_SINGLE_DAILY_TIMES);
    }

    public int getVersusLevel() {
        int level = config.getInt(KEY_CONF_VERSUS_LEVEL, DEFAULT_VERSUS_LEVEL);
        if ( level < MIN_LEVEL ) {
            level = MIN_LEVEL;
        } else if ( level > MAX_LEVEL ) {
            level = MAX_LEVEL;
        }
        return level;
    }

    public int getVersusItemStake() {
        return config.getInt(KEY_CONF_VERSUS_ITEM_STAKE, DEFAULT_VERSUS_ITEM_STAKE);
    }

    public double getVersusMoneyStake() {
        return config.getDouble(KEY_CONF_VERSUS_MONEY_STAKE, DEFAULT_VERSUS_MONEY_STAKE);
    }

    public double getVersusMoneyReward() {
        return config.getDouble(KEY_CONF_VERSUS_MONEY_REWARD, DEFAULT_VERSUS_MONEY_REWARD);
    }
}
