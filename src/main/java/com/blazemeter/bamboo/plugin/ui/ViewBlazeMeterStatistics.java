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
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;

public class ViewBlazeMeterStatistics extends ViewBuild {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(ViewBlazeMeterStatistics.class);

    @Override
	public String doExecute() throws Exception {
		return SUCCESS;
	}
}
