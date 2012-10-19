/**
 *
 */
package org.github.ucchyocean.misc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author ucchy
 *
 */
public class Resources {

	private static Properties resource;

	public static void initialize(String langFilePath) {

		resource = new Properties();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(langFilePath), "UTF-8"));

			String line;
			while ((line = reader.readLine()) != null) {
				if ( line.contains("=") ) {
					resource.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=")+1));
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
	}

	public static String get(String key) {

		return resource.getProperty(key, "");
	}
}
