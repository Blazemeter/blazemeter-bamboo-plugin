package com.blazemeter.bamboo.plugin.ui;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.BuildResultsSummaryImpl;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewBlazeMeterReport extends PlanResultsAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ViewBlazeMeterReport.class);

    private Map<String, String> reports = new HashMap<>();

    public String execute() throws Exception {
        String result = super.execute();

        ResultsSummary summary = this.getResultsSummary();
        if (summary instanceof ChainResultsSummaryImpl) {
            ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) summary;
            log.debug("Try to get report link for ChainResultsSummaryImpl");
            List<ChainStageResult> resultList = chainResults.getStageResults();
            for (ChainStageResult chainResult : resultList) {
                Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
                for (BuildResultsSummary sum : resultSet) {
                    Map<String, String> customBuildData = sum.getCustomBuildData();
                    for (String key : customBuildData.keySet()) {
                        if (key.startsWith("master_id_")) {
                            log.debug("Found report link for master =" + key);
                            reports.put(key.substring(10), customBuildData.get(key));
                        }
                    }
                }
            }
        } else if (summary instanceof BuildResultsSummaryImpl) {
            BuildResultsSummaryImpl buildResultsSummary = (BuildResultsSummaryImpl) summary;
            log.debug("Try to get report link for BuildResultsSummaryImpl");
            Map<String, String> customBuildData = buildResultsSummary.getCustomBuildData();
            for (String key : customBuildData.keySet()) {
                if (key.startsWith("master_id_")) {
                    log.debug("Found report link for master =" + key);
                    reports.put(key.substring(10), customBuildData.get(key));
                }
            }
        }

        return result;
    }


    public Map<String, String> getReports() {
        return reports;
    }

    public void setReports(Map<String, String> reports) {
        this.reports = reports;
    }
}
