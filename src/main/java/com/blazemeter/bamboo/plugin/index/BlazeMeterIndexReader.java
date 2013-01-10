package com.blazemeter.bamboo.plugin.index;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.atlassian.bamboo.index.CustomIndexReader;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.blazemeter.bamboo.plugin.BlazeMeterConstants;

public class BlazeMeterIndexReader implements CustomIndexReader{

	@Override
	public void extractFromDocument(Document doc, BuildResultsSummary result) {
		Map<String, String> results = result.getCustomBuildData();
		
		Field thrDataField = doc.getField(BlazeMeterConstants.REPORT_ERROR_THRESHOLD);
		if (thrDataField != null){
			results.put(BlazeMeterConstants.REPORT_ERROR_THRESHOLD, thrDataField.stringValue());
		}
		
		Field respTimeDataField = doc.getField(BlazeMeterConstants.REPORT_RESPONSE_TIME);
		if (respTimeDataField != null){
			results.put(BlazeMeterConstants.REPORT_RESPONSE_TIME, respTimeDataField.stringValue());
		}
	}

}
