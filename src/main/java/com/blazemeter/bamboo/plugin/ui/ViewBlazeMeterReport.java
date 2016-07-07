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
package com.blazemeter.bamboo.plugin.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.buildqueue.manager.AgentManager;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.blazemeter.bamboo.plugin.configuration.StaticAccessor;

public class ViewBlazeMeterReport extends PlanResultsAction {
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
                    reportUrl= (String) StaticAccessor.getReportUrls().get(brs.getBuildResultKey());
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
