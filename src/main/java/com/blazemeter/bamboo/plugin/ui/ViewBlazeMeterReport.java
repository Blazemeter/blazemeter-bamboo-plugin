package com.blazemeter.bamboo.plugin.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;

public class ViewBlazeMeterReport extends PlanResultsAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
    private boolean isJob;
    private String reportUrl;
    
    public String doExecute() throws Exception {
        String result = super.doExecute();
        
        if ((this.buildNumber != null) && (this.buildNumber > 0)) {
            this.isJob = true;
            
            String reportUrl = null;
            ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) this.getResultsSummary();

            
            List<ChainStageResult> resultList = chainResults.getStageResults();
            for (ChainStageResult chainResult:resultList){
            	Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
            	Iterator<BuildResultsSummary> iter = resultSet.iterator();
            	while (iter.hasNext()){
            		BuildResultsSummary brs = iter.next();
            		if (brs.getCustomBuildData().containsKey(Constants.REPORT_URL)){
            			reportUrl = brs.getCustomBuildData().get(Constants.REPORT_URL);
            		}
            	}
            }
            
            if (reportUrl != null){
            	reportUrl(reportUrl);
            } else {
            	reportUrl("");
            }
        }
        else {
            this.isJob = false;
        }
        
        return result;
    }
    
    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public boolean isJob() {
        return this.isJob;
    }

	public String reportUrl() {
		return reportUrl;
	}

	public void reportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

}
