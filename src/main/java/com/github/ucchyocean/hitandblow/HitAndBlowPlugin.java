/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.ucchyocean.hitandblow.handler.ChargeMediator;
import com.github.ucchyocean.hitandblow.session.PlayerLogoutListener;

/**
 * @author ucchy
 *
 */
public class HitAndBlowPlugin extends JavaPlugin {

    public static final String NAME = "HitAndBlow";

    private static final String GameLogFolderName = "gamelog";
    private static final String UserFolderName = "user";
    private static final String LangFolderName = "lang";

    public static String GameLogFolder;
    public static String UserFolder;
    public static Logger logger;
    public static HitAndBlowConfiguration config;
    public static ChargeMediator mediator;

    private HitAndBlowCommandExecutor executor;

    protected static HitAndBlowPlugin instance;

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        instance = this;
        logger = this.getLogger();

        File configFile = new File(getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            copyFileFromJar(configFile, "config.yml");
        }
        config = new HitAndBlowConfiguration(getConfig());

        initResource(config.getLang());

        mediator = new ChargeMediator(config.getMode());

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
