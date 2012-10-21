/**
 *
 */
package org.github.ucchyocean.hitandblow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author ucchy
 *
 */
public class UserConfiguration extends HashMap<String, Object> {

	private static final long serialVersionUID = 3440047816098145171L;

	private static final String KEY_USER_LAST_PLAY_DATE = "lastPalyDate";
	private static final String KEY_USER_DAILY_PLAY_TIMES = "dailyPlayTimes";
	private static final String KEY_USER_SCORE = "score";

	private UserConfiguration() {

		this.put(KEY_USER_SCORE, 0.0);
		this.put(KEY_USER_DAILY_PLAY_TIMES, 0);

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		this.put(KEY_USER_LAST_PLAY_DATE, format.format(new Date()));
	}

	private static UserConfiguration getUserConfiguration(String name) {

	    File folder = new File(HitAndBlowPlugin.UserFolder);
	    if ( !folder.exists() ) {
	    	folder.mkdirs();
	    }

	    File file = new File(HitAndBlowPlugin.UserFolder + File.separator + name + ".yml");
	    if ( !file.exists() ) {
	    	UserConfiguration conf = new UserConfiguration();
	    	conf.save(file);
	    }

	    return UserConfiguration.load(file);
	}

	private void save(String name) {

		File file = new File(HitAndBlowPlugin.UserFolder + File.separator + name + ".yml");
		save(file);
	}

	private void save(File file) {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(KEY_USER_LAST_PLAY_DATE + ": " + this.get(KEY_USER_LAST_PLAY_DATE));
			writer.newLine();
			writer.write(KEY_USER_DAILY_PLAY_TIMES + ": " + this.get(KEY_USER_DAILY_PLAY_TIMES));
			writer.newLine();
			writer.write(KEY_USER_SCORE + ": " + this.get(KEY_USER_SCORE));
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if ( writer != null ) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// do nothing.
				}
			}
		}
	}

	private static UserConfiguration load(File file) {

		UserConfiguration conf = new UserConfiguration();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] l = line.split(":");
				if (l[0].contains(KEY_USER_LAST_PLAY_DATE)) {
					conf.put(KEY_USER_LAST_PLAY_DATE, l[1].trim());
				} else if (l[0].contains(KEY_USER_DAILY_PLAY_TIMES)) {
					conf.put(KEY_USER_DAILY_PLAY_TIMES, Integer.parseInt(l[1].trim()));
				} else if (l[0].contains(KEY_USER_SCORE)) {
					conf.put(KEY_USER_SCORE, Double.parseDouble(l[1].trim()));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					// do nothing.
				}
			}
		}

		return conf;
	}

    public static int getUserDailyPlayTimes(String name) {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(new Date());
		int times = 0;

    	UserConfiguration conf = getUserConfiguration(name);
    	if ( today.equals(conf.get(KEY_USER_LAST_PLAY_DATE)) ) {
    		times = (Integer)conf.get(KEY_USER_DAILY_PLAY_TIMES);
    	}

    	return times;
    }

    public static void addUserDailyPlayTimes(String name) {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(new Date());

    	UserConfiguration conf = getUserConfiguration(name);
    	if ( today.equals(conf.get(KEY_USER_LAST_PLAY_DATE)) ) {
    		int times = (Integer)conf.get(KEY_USER_DAILY_PLAY_TIMES);
    		conf.put(KEY_USER_DAILY_PLAY_TIMES, times+1);
    	} else {
    		conf.put(KEY_USER_DAILY_PLAY_TIMES, 1);
    		conf.put(KEY_USER_LAST_PLAY_DATE, today);
    	}

    	conf.save(name);
    }

    public static void addScore(String name, Double value) {

    	UserConfiguration conf = getUserConfiguration(name);
    	Double score = (Double)conf.get(KEY_USER_SCORE);
    	conf.put(KEY_USER_SCORE, score + value);

    	conf.save(name);
    }

    public static Vector<ScoreData> getRanking() {

    	Vector<ScoreData> arr = new Vector<ScoreData>();

	    File folder = new File(HitAndBlowPlugin.UserFolder);
	    if ( !folder.exists() ) {
	    	folder.mkdirs();
	    }

	    String[] filelist = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if ( name.endsWith(".yml") )
					return true;
				return false;
			}
	    });

	    for ( String f : filelist ) {
	    	String userName = f.substring(0, f.indexOf(".") );
	    	UserConfiguration conf = getUserConfiguration(userName);
	    	arr.add( new ScoreData( userName, (Double)conf.get(KEY_USER_SCORE) ) );
	    }

		Collections.sort(arr, new Comparator<ScoreData>() {
			public int compare(ScoreData o1, ScoreData o2) {
	    		if ( o1.score < o2.score ) {
	    			return 1;
	    		} else if ( o1.score > o2.score ) {
	    			return -1;
	    		} else {
	    			return 0;
	    		}
			}
		});

		return arr;
    }
}
