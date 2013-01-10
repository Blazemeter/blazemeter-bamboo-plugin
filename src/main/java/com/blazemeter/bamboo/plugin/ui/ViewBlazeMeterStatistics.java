package com.blazemeter.bamboo.plugin.ui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.atlassian.bamboo.build.ViewBuild;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.core.util.RandomGenerator;
import com.blazemeter.bamboo.plugin.BlazeMeterConstants;

public class ViewBlazeMeterStatistics extends ViewBuild {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(ViewBlazeMeterStatistics.class);
	private Map<String, Object> chart;
	
    @Override
	public String doExecute() throws Exception {
		List<ResultsSummary> summaries = new ArrayList<ResultsSummary>(getResultsList());

		// Convert map into an XYSeriesCollection
		XYSeriesCollection dataset = convertMapToXySeries(summaries);

		// Create the chart from the dataset
		createChart(dataset);

		return SUCCESS;
	}

	/**
     * Convert the data map into a JFreeChart xy series
     *
     * @param series The series to convert
     * @param totalSeries The total series
     * @return The JFreeChart series collection
     */
    private XYSeriesCollection convertMapToXySeries(List<ResultsSummary> summaries)
    {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("Error Threshold" + " - " + "Error Threshold");
        XYSeries s2 = new XYSeries("Response Time" + " - " + "Response Time");

        for (ResultsSummary summary : summaries) {
            String errThr = summary.getCustomBuildData().get(BlazeMeterConstants.REPORT_ERROR_THRESHOLD);
            if (errThr != null){
            	s1.add(summary.getBuildNumber(), new Double(errThr));
            } else {
            	s1.add(summary.getBuildNumber(), 0);
            }
            
            String respTime = summary.getCustomBuildData().get(BlazeMeterConstants.REPORT_RESPONSE_TIME);
            if (respTime != null){
            	s2.add(summary.getBuildNumber(), new Double(respTime));
            } else {
            	s2.add(summary.getBuildNumber(), 0);
            }
        }
        dataset.addSeries(s1);
        dataset.addSeries(s2);
        return dataset;
    }
    
    /**
     * Create the chart
     *
     * @param dataset The dataset to create the chart from
     */
    private void createChart(XYSeriesCollection dataset)
    {
    	//TODO - remove first series from the first axis 
        chart = new HashMap<String, Object>();
        ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
        XYSeriesCollection dataset1 = new XYSeriesCollection(dataset.getSeries(1));
        JFreeChart jchart = ChartFactory.createXYLineChart("", "Build Number", "Values", dataset1,
            PlotOrientation.VERTICAL, true, true, false);

        // Set the tick units of the domain (x) axis so they are always integers, because you can't have
        // half a build.
        XYPlot plot = (XYPlot) jchart.getPlot();
        
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        XYSeriesCollection dataset2 = new XYSeriesCollection(dataset.getSeries(0));
        final NumberAxis axis2 = new NumberAxis("Secondary");
        axis2.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, dataset2);
        
        plot.mapDatasetToRangeAxis(1, 1);
        
        XYItemRenderer renderer1 = new StandardXYItemRenderer();
        renderer1.setSeriesPaint(0, Color.GREEN);
        renderer1.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator("ResponseTime: {0} {1} {2}"));
        
        XYItemRenderer renderer2 = new StandardXYItemRenderer();
        renderer2.setSeriesPaint(0, Color.BLUE);
        renderer1.setLegendItemLabelGenerator(new StandardXYSeriesLabelGenerator("Error threshold: {0} {1} {2}"));
        plot.setRenderer(1, renderer1);
        plot.setRenderer(2, renderer2);
        
        try
        {
            String location = getSavedChartLocation(chartRenderingInfo, jchart);

            chart.put("location", location);
            chart.put("width", 700);
            chart.put("height", 500);

            String mapName = generateRandomString() + "_map";

            chart.put("imageMap", getImageMap(chartRenderingInfo, mapName));
            chart.put("imageMapName", mapName);

        }
        catch (IOException e)
        {
            log.error(e, e);
        }
    }
 
    protected String getSavedChartLocation(
            ChartRenderingInfo chartRenderingInfo, JFreeChart jchart)
            throws IOException
    {
        return ServletUtilities.saveChartAsPNG(jchart, 600, 500, chartRenderingInfo, null);
    }
    
    protected String generateRandomString()
    {
        return RandomGenerator.randomString(5);
    }
    
    protected String getImageMap(ChartRenderingInfo chartRenderingInfo,
            String mapName)
    {
        return ChartUtilities.getImageMap(mapName, chartRenderingInfo);
    }
    
    public Map<String, Object> getChart()
    {
        return chart;
    }
}
