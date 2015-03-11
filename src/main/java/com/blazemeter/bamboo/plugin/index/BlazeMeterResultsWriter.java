package com.blazemeter.bamboo.plugin.index;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.jetbrains.annotations.NotNull;

import com.atlassian.bamboo.index.CustomPostBuildIndexWriter;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;

public class BlazeMeterResultsWriter implements CustomPostBuildIndexWriter {

	@Override
	public void updateIndexDocument(@NotNull Document doc, @NotNull BuildResultsSummary summary) {
		Map<String, String> data = summary.getCustomBuildData();
		String thrData = data.get(Constants.REPORT_ERROR_THRESHOLD);
		if (thrData != null){
			String respTimeData = data.get(Constants.REPORT_RESPONSE_TIME);
			
			// Store the values unindexed.
            doc.add(new Field(Constants.REPORT_ERROR_THRESHOLD, thrData, Store.COMPRESS, Index.NO));
            doc.add(new Field(Constants.REPORT_RESPONSE_TIME, respTimeData, Store.COMPRESS,Index.NO));
		}
	}

}
