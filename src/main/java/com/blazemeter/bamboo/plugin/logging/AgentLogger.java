/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.bamboo.plugin.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class AgentLogger implements com.blazemeter.api.logging.Logger {

    private java.util.logging.Logger logger;
    private FileHandler fileHandler;

    public AgentLogger(String logFilePath) {
        File f = new File(logFilePath);
        logger = java.util.logging.Logger.getLogger(f.getName());
        try {
            fileHandler = new FileHandler(logFilePath);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create file handler for log file", ex);
        }
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
    }

    public void close() {
        fileHandler.close();
    }

    @Override
    public void debug(String message) {
        logger.log(Level.FINE, message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.log(Level.FINE, message, throwable);
    }

    @Override
    public void info(String message) {
        logger.log(Level.INFO, message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.log(Level.INFO, message, throwable);
    }

    @Override
    public void warn(String message) {
        logger.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.log(Level.WARNING, message, throwable);
    }

    @Override
    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

}
