package com.blazemeter.bamboo.plugin.configuration;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

// TODO: to api-client 1.3
public class BambooCiBuild extends CiBuild {
    public BambooCiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        super(utils, testId, properties, notes, ciPostProcess);
    }
    @Override
    protected Master startTest(AbstractTest test) throws IOException {
        Master master = super.startTest(test);
        if (!StringUtils.isBlank(notes)) {
            notifier.notifyInfo("Sent notes: " + notes);
        }
        if (!StringUtils.isBlank(properties)) {
            notifier.notifyInfo("Sent properties: " + properties);
        }
        return master;
    }

}