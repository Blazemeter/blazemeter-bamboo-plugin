/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blazemeter.bamboo.plugin;

import java.io.*;
import java.util.*;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Utils {

    private Utils(){
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

    public static File resolvePath(TaskContext context, String path, BuildLogger logger) throws Exception {
        File f = null;
        File root = new File("/");
        if (path.equals("/")) {
            f = root;
        }
        if (path.startsWith("/")) {
            f = new File(root, path);
        } else {
            f = new File(context.getWorkingDirectory().getAbsolutePath() + "/build # "
                + context.getBuildContext().getBuildNumber(), path);
        }
        if (!f.exists()) {
            boolean mkDir = false;
            try {
                mkDir = f.mkdirs();
            } catch (Exception e) {
                throw new Exception("Failed to find filepath = " + f.getName());
            } finally {
                if (!mkDir) {
                    logger.addBuildLogEntry("Failed to create " + f.getCanonicalPath() + " , workspace will be used.");
                    f = new File(context.getWorkingDirectory(), path);
                    f.mkdirs();
                    logger.addBuildLogEntry("Resolving path into " + f.getCanonicalPath());
                }
            }
        }
        if (!f.canWrite()) {
            f = new File(context.getWorkingDirectory(), path);
            f.mkdirs();
            logger.addBuildLogEntry("Resolving path into " + f.getCanonicalPath());
        }
        return f.getCanonicalFile();
    }

}
