/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.ucchyocean.misc.AccountHandler;
import com.github.ucchyocean.misc.Resources;

/**
 * @author ucchy
 *
 */
public class HitAndBlowPlugin extends JavaPlugin {

    public static final String NAME = "HitAndBlow";

    private static final int MIN_LEVEL = 2;
    private static final int MAX_LEVEL = 7;

    public static final String KEY_CONF_ANNOUNCE = "announce";
    public static final String KEY_CONF_LANG = "lang";
    public static final String KEY_CONF_SINGLE_REWARDS = "single.rewards";
    public static final String KEY_CONF_SINGLE_LEVEL = "single.level";
    public static final String KEY_CONF_SINGLE_TIMES = "single.dailyTimes";
    public static final String KEY_CONF_VERSUS_STAKE = "versus.stake";
    public static final String KEY_CONF_VERSUS_LEVEL = "versus.level";
    public static final String KEY_CONF_VERSUS_REWARD = "versus.reward";

    private static final String GameLogFolderName = "gamelog";
    private static final String UserFolderName = "user";
    private static final String LangFolderName = "lang";

    public static String GameLogFolder;
    public static String UserFolder;
    public static Logger logger;
    public static AccountHandler accountHandler;
    public static Configuration config;

    private HitAndBlowCommandExecutor executor;

    protected static HitAndBlowPlugin instance;

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        instance = this;
        logger = this.getLogger();

        try {
            accountHandler = new AccountHandler();
        } catch (Exception e) {
            logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            copyFileFromJar(configFile, "config.yml");
        }
        config = getConfig();

        initResource(getLang());

        executor = new HitAndBlowCommandExecutor();
        getCommand("hb").setExecutor(executor);

        PlayerLogoutListener listener = new PlayerLogoutListener();
        getServer().getPluginManager().registerEvents(listener, this);

        GameLogFolder = this.getDataFolder() + File.separator + GameLogFolderName;
        UserFolder = this.getDataFolder() + File.separator + UserFolderName;

        super.onEnable();
    }

    private void initResource(String lang) {

        // Copy resource files to lang folder.

        File jaFile = new File(getDataFolder() + File.separator +
                HitAndBlowPlugin.LangFolderName + File.separator + "ja.txt");
        if ( !jaFile.exists() ) {
            copyFileFromJar(jaFile, "ja.txt");
        }

        File enFile = new File(getDataFolder() + File.separator +
                HitAndBlowPlugin.LangFolderName + File.separator + "en.txt");
        if ( !enFile.exists() ) {
            copyFileFromJar(enFile, "en.txt");
        }

        // Load default resrouce from jar file.
        Resources.loadFromInputStream(getInputStreamFromJar("en.txt"));

        // Load (overwrite) resource from lang folder file.
        Resources.loadFromFile(getDataFolder() + File.separator +
                HitAndBlowPlugin.LangFolderName + File.separator + lang + ".txt");
    }

    public static List<Double> getSingleRewards() {

        return config.getDoubleList(KEY_CONF_SINGLE_REWARDS);
    }

    public static int getSingleLevel() {

        int level = config.getInt(KEY_CONF_SINGLE_LEVEL);
        if ( level < MIN_LEVEL ) {
            level = MIN_LEVEL;
        } else if ( level > MAX_LEVEL ) {
            level = MAX_LEVEL;
        }
        return level;
    }

    public static Double getVersusStake() {

        return config.getDouble(KEY_CONF_VERSUS_STAKE);
    }

    public static int getVersusLevel() {

        int level = config.getInt(KEY_CONF_VERSUS_LEVEL);
        if ( level < MIN_LEVEL ) {
            level = MIN_LEVEL;
        } else if ( level > MAX_LEVEL ) {
            level = MAX_LEVEL;
        }
        return level;
    }

    public static Double getVersusReward() {

        return config.getDouble(KEY_CONF_VERSUS_REWARD);
    }

    public static int getSingleDialyTimes() {

        return config.getInt(KEY_CONF_SINGLE_TIMES);
    }

    public static boolean getAnnounce() {

        return config.getBoolean(KEY_CONF_ANNOUNCE);
    }

    public static String getLang() {

        return config.getString(KEY_CONF_LANG);
    }

    private void copyFileFromJar(File outputFile, String inputFileName) {

        InputStream is;
        FileOutputStream fos;
        File parent = outputFile.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        try {
            JarFile jarFile = new JarFile(getFile());
            ZipEntry zipEntry = jarFile.getEntry(inputFileName);
            is = jarFile.getInputStream(zipEntry);

            fos = new FileOutputStream(outputFile);

            byte[] buf = new byte[8192];
            int len;
            while ( (len = is.read(buf)) != -1 ) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getInputStreamFromJar(String inputFileName) {

        try {
            JarFile jarFile = new JarFile(getFile());
            ZipEntry zipEntry = jarFile.getEntry(inputFileName);
            return jarFile.getInputStream(zipEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}