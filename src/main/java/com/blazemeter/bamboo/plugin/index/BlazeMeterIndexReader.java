package com.blazemeter.bamboo.plugin.index;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.atlassian.bamboo.index.CustomIndexReader;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;

public class BlazeMeterIndexReader implements CustomIndexReader{

	@Override
	public void extractFromDocument(Document doc, BuildResultsSummary result) {
		Map<String, String> results = result.getCustomBuildData();
		
		Field thrDataField = doc.getField(Constants.REPORT_ERROR_THRESHOLD);
		if (thrDataField != null){
			results.put(Constants.REPORT_ERROR_THRESHOLD, thrDataField.stringValue());
		}
		
		Field respTimeDataField = doc.getField(Constants.REPORT_RESPONSE_TIME);
		if (respTimeDataField != null){
			results.put(Constants.REPORT_RESPONSE_TIME, respTimeDataField.stringValue());
		}
	}

}
