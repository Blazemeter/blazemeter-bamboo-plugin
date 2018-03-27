package com.blazemeter.bamboo.plugin.ui;

import com.atlassian.bamboo.resultsummary.BuildResultsSummaryImpl;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import org.apache.log4j.Logger;
import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewBlazeMeterReport extends PlanResultsAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(ViewBlazeMeterReport.class);
    private boolean hasBzmReports;
    private String sessionId;
    private Map<String, String> reports = new HashMap<>();

    private AgentManager agentManager;

    public String execute() throws Exception {
        log.info("EXECUTE H@H@H@H@" + this);
        String result = super.execute();
        String buildKey = this.getBuildKey();
        hasBzmReports = true;
        reports.put("123","http://blazedemo.com");
        if ((this.buildNumber != null) && (this.buildNumber > 0)) {

            ResultsSummary summary = this.getResultsSummary();
            log.info("TYPE:-----" + summary);
            if (summary instanceof  ChainResultsSummaryImpl) {
                ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) summary;
                List<ChainStageResult> resultList = chainResults.getStageResults();
                for (ChainStageResult chainResult : resultList) {
                    Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
                    for (BuildResultsSummary sum : resultSet) {
                        Map<String, String> customBuildData = sum.getCustomBuildData();
                        for (String key : customBuildData.keySet()) {
                            if (key.startsWith("master_id_")) {
                                reports.put(key.substring(10), customBuildData.get(key));
                                this.hasBzmReports = true;
                            }
                        }
                    }
                }
            } else if (summary instanceof BuildResultsSummaryImpl) {
                BuildResultsSummaryImpl buildResultsSummary = (BuildResultsSummaryImpl) summary;
                this.hasBzmReports = true;
                log.info("aaaa" + buildResultsSummary);
            }
        } else {
            this.hasBzmReports = false;
        }
        return result;
    }

    public Map<String, String> getReports() {
        return reports;
    }

    public void setReports(Map<String, String> reports) {
        this.reports = reports;
    }

    public boolean isHasBzmReports() {
        return hasBzmReports;
    }

    public void setHasBzmReports(boolean hasBzmReports) {
        this.hasBzmReports = hasBzmReports;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }


}
