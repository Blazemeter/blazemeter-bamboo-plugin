package com.blazemeter.bamboo.plugin.configuration;

import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.logging.AgentLogger;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;

public class BambooCiBuild extends CiBuild {
    public BambooCiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        super(utils, testId, properties, notes, ciPostProcess);
    }

    public void closeLogger() {
        if (logger != null && logger instanceof AgentLogger) {
            ((AgentLogger) logger).close();
        }
    }
}
