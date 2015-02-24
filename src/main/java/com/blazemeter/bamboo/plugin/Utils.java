package com.blazemeter.bamboo.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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


    public static String getFileContents(String fn) {

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);

        try {

            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try {
                String line;    // not declared within while loop

                /*
                 *         readLine is a bit quirky : it returns the content of a line
                 *         MINUS the newline. it returns null only for the END of the
                 *         stream. it returns an empty String if two newlines appear in
                 *         a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }

        return contents.toString();
    }

    public static boolean checkNumber(String number, boolean isPercentage){
        try{
            if (number.equals("-0")){
                throw new NumberFormatException("Value cannot be -0!");
            }
            Integer val = Integer.valueOf(number);
            if (isPercentage){
                if (!((val >= 0) && (val <= 100))){
                    throw new NumberFormatException("Value is not between 0 and 100!");
                }
            } else {
                if (!(val >= 0)){
                    throw new NumberFormatException("Value must be greater than 0!");
                }
            }
        } catch (NumberFormatException nfe){
            return false;
        }

        return true;
    }


}
