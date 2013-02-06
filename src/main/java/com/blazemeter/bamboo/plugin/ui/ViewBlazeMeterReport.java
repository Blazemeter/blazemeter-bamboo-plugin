package com.blazemeter.bamboo.plugin.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class ViewBlazeMeterReport extends PlanResultsAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
    private boolean isJob;
    private String sessionId;
    
    public String doExecute() throws Exception {
        String result = super.doExecute();
        
        if ((this.buildNumber != null) && (this.buildNumber > 0)) {
            this.isJob = true;
            
            String sessionId = null;
            ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) this.getResultsSummary();

            
            List<ChainStageResult> resultList = chainResults.getStageResults();
            for (ChainStageResult chainResult:resultList){
            	Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
            	Iterator<BuildResultsSummary> iter = resultSet.iterator();
            	while (iter.hasNext()){
            		BuildResultsSummary brs = iter.next();
            		if (brs.getCustomBuildData().containsKey("session_id")){
            			sessionId = brs.getCustomBuildData().get("session_id");
            		}
            	}
            }
            
            if (sessionId != null){
            	setSessionId(sessionId);
            } else {
            	setSessionId("");
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

    public boolean getIsJob() {
        return this.isJob;
    }

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
