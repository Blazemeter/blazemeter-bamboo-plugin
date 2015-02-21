package com.blazemeter.bamboo.plugin;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by zmicer on 21.2.15.
 */
public class Utils {

    private Utils() {
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }
}
