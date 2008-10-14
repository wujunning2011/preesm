package org.ietr.preesm.plugin.mapper.commcontenlistsched.plotter;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.CommunicationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComputationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.LinkDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.OperationDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.OperatorDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.scheduler.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

/**
 * @author mpelcat
 * 
 *         Gantt plotter of a mapperdagvertex using JFreeChart
 */
public class GanttPlotter extends ApplicationFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            a dataset.
	 * 
	 * @return A chart.
	 */
	private static JFreeChart createChart(IntervalCategoryDataset dataset) {

		JFreeChart chart = ChartFactory.createGanttChart("Solution Gantt", // title
				"Operators", // x-axis label
				"Time", // y-axis label
				null, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		Point2D start = new Point2D.Float(0, 0);
		Point2D end = new Point2D.Float(500, 500);
		float[] dist = { 0.0f, 0.8f };
		Color[] colors = { Color.BLUE.brighter().brighter(), Color.WHITE };
		LinearGradientPaint p = new LinearGradientPaint(start, end, dist,
				colors);

		chart.setBackgroundPaint(p);

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.black);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setOrientation(PlotOrientation.HORIZONTAL);

		DateAxis xaxis = (DateAxis) plot.getRangeAxis();
		xaxis.setDateFormatOverride(new VertexDateFormat());
		xaxis.setPositiveArrowVisible(true);

		DefaultDrawingSupplier d = new DefaultDrawingSupplier();

		plot.setDrawingSupplier(d);
		GanttRenderer ren = new MyGanttRenderer();
		ren.setBaseFillPaint(p);
		// ren.setSeriesPaint(0, p);
		ren.setSeriesItemLabelsVisible(0, false);
		ren.setSeriesVisibleInLegend(0, false);
		ren.setSeriesItemLabelGenerator(0,
				new IntervalCategoryItemLabelGenerator());
		ren.setSeriesToolTipGenerator(0, new MapperGanttToolTipGenerator());

		ren.setAutoPopulateSeriesShape(false);

		plot.setRenderer(ren);

		plot.setDataset(dataset);

		return chart;

	}

	/**
	 * Creates a dataset from a MapperDAGVertex.
	 * 
	 * @return The dataset.
	 */
	private static IntervalCategoryDataset createDataset(
			AbstractScheduler scheduler) {

		TaskSeries series = new TaskSeries("Scheduled");
		Task currenttask;

		// Creating the Operator lines
		Set<LinkDescriptor> links = new HashSet<LinkDescriptor>();
		for (OperatorDescriptor indexOperator : scheduler.getArchitecture()
				.getAllOperators().values()) {
			currenttask = new Task(indexOperator.getId(), new SimpleTimePeriod(
					0, indexOperator.getFinishTime()));
			series.add(currenttask);
			for (LinkDescriptor indexLink : indexOperator.getOutputLinks()) {
				if (!links.contains(indexLink)) {
					links.add(indexLink);
					// currenttask = new Task(
					// indexLink.getId(),
					// new SimpleTimePeriod(
					// 0,
					// indexLink
					// .getOccupiedTimeInterval(
					// indexLink
					// .getCommunication(
					// indexLink
					// .getCommunications()
					// .size() - 2)
					// .getName())
					// .getFinishTime()));
					currenttask = new Task(indexLink.getId(),
							new SimpleTimePeriod(0, indexLink.getCommunication(
									indexLink.getCommunications().size() - 2)
									.getFinishTimeOnLink()));
					series.add(currenttask);
					for (CommunicationDescriptor indexCommunication : indexLink
							.getCommunications()) {
						// long start = indexLink.getOccupiedTimeInterval(
						// indexCommunication.getName()).getStartTime();
						// long end = indexLink.getOccupiedTimeInterval(
						// indexCommunication.getName()).getFinishTime();
						long start = indexCommunication.getStartTimeOnLink();
						long end = indexCommunication.getFinishTimeOnLink();
						if (indexCommunication.getSendLink() == indexLink) {
							Task t = new Task("send_"
									+ indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						} else if (indexCommunication.getReceiveLink() == indexLink) {

							Task t = new Task("receive_"
									+ indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						} else {
							Task t = new Task(indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						}
					}
				}
			}
			for (LinkDescriptor indexLink : indexOperator.getInputLinks()) {
				if (!links.contains(indexLink)) {
					links.add(indexLink);
					// currenttask = new Task(
					// indexLink.getId(),
					// new SimpleTimePeriod(
					// 0,
					// indexLink
					// .getOccupiedTimeInterval(
					// indexLink
					// .getCommunication(
					// indexLink
					// .getCommunications()
					// .size() - 2)
					// .getName())
					// .getFinishTime()));
					currenttask = new Task(indexLink.getId(),
							new SimpleTimePeriod(0, indexLink.getCommunication(
									indexLink.getCommunications().size() - 2)
									.getFinishTimeOnLink()));
					series.add(currenttask);
					for (CommunicationDescriptor indexCommunication : indexLink
							.getCommunications()) {
						// long start = indexLink.getOccupiedTimeInterval(
						// indexCommunication.getName()).getStartTime();
						// long end = indexLink.getOccupiedTimeInterval(
						// indexCommunication.getName()).getFinishTime();
						long start = indexCommunication.getStartTimeOnLink();
						long end = indexCommunication.getFinishTimeOnLink();
						if (indexCommunication.getSendLink() == indexLink) {
							Task t = new Task("send_"
									+ indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						} else if (indexCommunication.getReceiveLink() == indexLink) {

							Task t = new Task("receive_"
									+ indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						} else {
							Task t = new Task(indexCommunication.getName(),
									new SimpleTimePeriod(start, end));
							series.get(indexLink.getId()).addSubtask(t);
						}
					}
				}
			}
			// for (OperationDescriptor indexOperation : indexOperator
			// .getOperations()) {
			// long start = indexOperator.getOccupiedTimeInterval(
			// indexOperation.getName()).getStartTime();
			// long end = indexOperator.getOccupiedTimeInterval(
			// indexOperation.getName()).getFinishTime();
			// Task t = new Task(indexOperation.getName(),
			// new SimpleTimePeriod(start, end));
			// series.get(indexOperator.getId()).addSubtask(t);
			// }
			for (ComputationDescriptor indexComputation : indexOperator
					.getComputations()) {
				long start = indexComputation.getStartTime();
				long end = indexComputation.getFinishTime();
				Task t = new Task(indexComputation.getName(),
						new SimpleTimePeriod(start, end));
				series.get(indexOperator.getId()).addSubtask(t);
			}
			for (CommunicationDescriptor indexCommunication : indexOperator
					.getSendCommunications()) {
				long start = indexCommunication.getStartTimeOnSendOperator();
				long end = indexCommunication.getFinishTimeOnSendOperator();
				Task t = new Task(indexCommunication.getName(),
						new SimpleTimePeriod(start, end));
				series.get(indexOperator.getId()).addSubtask(t);
			}
			for (CommunicationDescriptor indexCommunication : indexOperator
					.getReceiveCommunications()) {
				long start = indexCommunication.getStartTimeOnReceiveOperator();
				long end = indexCommunication.getFinishTimeOnReceiveOperator();
				Task t = new Task(indexCommunication.getName(),
						new SimpleTimePeriod(start, end));
				series.get(indexOperator.getId()).addSubtask(t);
			}
		}

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(series);

		return collection;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void plot(AbstractScheduler scheduler) {

		GanttPlotter plot = new GanttPlotter("Solution gantt, latency: "
				+ scheduler.getScheduleLength(), scheduler);
		plot.pack();
		RefineryUtilities.centerFrameOnScreen(plot);
		plot.setVisible(true);

	}

	/**
	 * A demonstration application showing how to create a simple time series
	 * chart. This example uses monthly data.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public GanttPlotter(String title, AbstractScheduler scheduler) {
		super(title);
		JFreeChart chart = createChart(createDataset(scheduler));
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
	}

	public void windowClosing(WindowEvent event){
		if(event.equals(WindowEvent.WINDOW_CLOSING)){
			
		}
	}
}