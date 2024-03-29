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

import com.blazemeter.api.logging.Logger;

import java.util.logging.Level;

public class ServerLogger implements Logger {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServerLogger.class.getName());
    public ServerLogger() {
    }

    @Override
    public void debug(String message) {
        logger.log(Level.FINE,message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.log(Level.FINE,message,throwable);
    }

    @Override
    public void info(String message) {
        logger.log(Level.INFO,message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.log(Level.INFO,message,throwable);
    }

    @Override
    public void warn(String message) {
        logger.log(Level.WARNING,message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.log(Level.WARNING,message,throwable);
    }

    @Override
    public void error(String message) {
        logger.log(Level.SEVERE,message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE,message,throwable);
    }
}