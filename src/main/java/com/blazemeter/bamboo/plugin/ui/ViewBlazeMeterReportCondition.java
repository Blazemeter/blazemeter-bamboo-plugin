package com.blazemeter.bamboo.plugin.ui;

import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.plan.*;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.BuildResultsSummaryImpl;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewBlazeMeterReportCondition implements Condition {

    private static final Logger log = Logger.getLogger(ViewBlazeMeterReportCondition.class.getName());

    private PlanManager planManager;
    private ResultsSummaryManager resultsSummaryManager;

    public ViewBlazeMeterReportCondition(PlanManager planManager, ResultsSummaryManager resultsSummaryManager) {
        this.planManager = planManager;
        this.resultsSummaryManager = resultsSummaryManager;
    }

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        String planKeyDescr = context.get("planKey") == null ? null
                : (String) context.get("planKey");
        String buildNumber = context.get("buildNumber") == null ? null
                : (String) context.get("buildNumber");

        if (planKeyDescr == null || buildNumber == null) {
            log.log(Level.FINE,"buildNumber or planKeyDescr is null. BlazeMeter report tab will not displaying");
            return false;
        }
        PlanKey planKey = PlanKeys.getPlanKey(planKeyDescr);

        Plan plan = planManager.getPlanByKey(planKey);
        if (plan == null) {
            log.log(Level.FINE,"Plan is null. BlazeMeter report tab will not displaying");
            return false;
        }

        PlanResultKey planResultKey = PlanKeys.getPlanResultKey(plan.getPlanKey(), Integer.parseInt(buildNumber));
        ResultsSummary summary = resultsSummaryManager.getResultsSummary(planResultKey);

        if (summary == null) {
            log.log(Level.FINE,"ResultsSummary is null. BlazeMeter report tab will not displaying");
            return false;
        }

        if (!summary.isInProgress()) { // isFinished() does not include interrupt case
            log.log(Level.FINE,"Check is build contains BlazeMeter Step");
            if (summary instanceof ChainResultsSummaryImpl) {
                ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) summary;
                List<ChainStageResult> resultList = chainResults.getStageResults();
                for (ChainStageResult chainResult : resultList) {
                    Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
                    for (BuildResultsSummary sum : resultSet) {
                        Map<String, String> customBuildData = sum.getCustomBuildData();
                        if (customBuildData.containsKey("isBlazeMeterStep")) {
                            log.log(Level.FINE,"Found BlazeMeter step in ChainResultsSummaryImpl");
                            return true;
                        }
                    }
                }
            } else if (summary instanceof BuildResultsSummaryImpl) {
                BuildResultsSummaryImpl buildResultsSummary = (BuildResultsSummaryImpl) summary;
                Map<String, String> customBuildData = buildResultsSummary.getCustomBuildData();
                if (customBuildData.containsKey("isBlazeMeterStep")) {
                    log.log(Level.FINE,"Found BlazeMeter step in BuildResultsSummaryImpl");
                    return true;
                }
            }

            log.log(Level.FINE,"BlazeMeter step not found in current build. BlazeMeter report tab will not displaying");
            return false;
        } else {
            log.log(Level.FINE,"Build does not finished yet. BlazeMeter report tab will not displaying");
            return false;
        }
    }
}