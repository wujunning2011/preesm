/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2015)
 * Jonathan Piat <jpiat@laas.fr> (2008)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2012)
 *
 * This software is a computer program whose purpose is to help prototyping
 * parallel applications using dataflow formalism.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package org.ietr.preesm.mapper.ui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.ietr.preesm.mapper.gantt.GanttComponent;
import org.ietr.preesm.mapper.gantt.GanttData;
import org.ietr.preesm.mapper.gantt.GanttTask;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

// TODO: Auto-generated Javadoc
/**
 * Gantt plotter of a mapperdagvertex using JFreeChart.
 *
 * @author mpelcat
 */
public class GanttPlotter extends ApplicationFrame {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The chart. */
  JFreeChart chart = null;

  /** The chart panel. */
  ChartPanel chartPanel = null;

  /**
   * Creates a chart.
   *
   * @param dataset
   *          a dataset.
   *
   * @return A chart.
   */
  private JFreeChart createChart(final IntervalCategoryDataset dataset) {

    final JFreeChart chart = ChartFactory.createGanttChart("Solution Gantt", // title
        "Operators", // x-axis label
        "Time", // y-axis label
        null, // data
        true, // create legend?
        true, // generate tooltips?
        false // generate URLs?
    );

    final CategoryPlot plot = (CategoryPlot) chart.getPlot();

    final Paint p = GanttPlotter.getBackgroundColorGradient();
    chart.setBackgroundPaint(p);

    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.black);
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    plot.setOrientation(PlotOrientation.HORIZONTAL);

    final DateAxis xaxis = (DateAxis) plot.getRangeAxis();
    xaxis.setDateFormatOverride(new VertexDateFormat());
    xaxis.setPositiveArrowVisible(true);

    final DefaultDrawingSupplier d = new DefaultDrawingSupplier();

    plot.setDrawingSupplier(d);
    final MyGanttRenderer ren = new MyGanttRenderer();
    // ren.setRepaintedListener(new RefreshRepaintedListener(this));

    ren.setSeriesItemLabelsVisible(0, false);
    ren.setSeriesVisibleInLegend(0, false);
    ren.setSeriesItemLabelGenerator(0, new IntervalCategoryItemLabelGenerator());
    ren.setSeriesToolTipGenerator(0, new MapperGanttToolTipGenerator());

    ren.setAutoPopulateSeriesShape(false);

    plot.setRenderer(ren);

    plot.setDataset(dataset);
    return chart;

  }

  /**
   * Creates a dataset from a MapperDAGVertex. This dataset is used to prepare display of a Gantt chart with one line per populated SLAM component.
   *
   * @param ganttData
   *          the gantt data
   * @return The dataset.
   */
  private static IntervalCategoryDataset createDataset(final GanttData ganttData) {

    final TaskSeries series = new TaskSeries("Scheduled");

    // Creating the component lines (operator or communicator)
    final List<GanttComponent> components = ganttData.getComponents();

    for (final GanttComponent cmp : components) {
      final Task currentJFreeCmp = new Task(cmp.getId(), new SimpleTimePeriod(0, 1));
      series.add(currentJFreeCmp);

      // Setting the series length to the maximum end time of a task
      final long finalCost = cmp.getEndTime();
      series.get(cmp.getId()).setDuration(new SimpleTimePeriod(0, finalCost));

      for (final GanttTask ganttTask : cmp.getTasks()) {
        final String taskName = ganttTask.getId();
        final long start = ganttTask.getStartTime();
        final long end = start + ganttTask.getDuration();
        final Task currentJFreeTask = new Task(taskName, new SimpleTimePeriod(start, end));

        currentJFreeCmp.addSubtask(currentJFreeTask);
      }
    }

    final TaskSeriesCollection collection = new TaskSeriesCollection();
    collection.add(series);

    return collection;

  }

  /**
   * Plot deployment.
   *
   * @param ganttData
   *          the gantt data
   * @param delegateDisplay
   *          the delegate display
   */
  public static void plotDeployment(final GanttData ganttData, final Composite delegateDisplay) {

    final GanttPlotter plotter = new GanttPlotter("Solution gantt", ganttData);

    if (delegateDisplay == null) {
      plotter.plot();
    } else {
      plotter.plotInComposite(delegateDisplay);
    }
  }

  /**
   * Starting point for the demonstration application.
   */
  public void plot() {

    pack();
    RefineryUtilities.centerFrameOnScreen(this);
    setVisible(true);

  }

  /**
   * Gantt chart plotting function in a given composite.
   *
   * @param parent
   *          the parent
   */
  public void plotInComposite(final Composite parent) {

    final Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.FILL);
    parent.setLayout(new FillLayout());
    final Frame frame = SWT_AWT.new_Frame(composite);
    frame.add(getContentPane());

    final MouseClickedListener listener = new MouseClickedListener(frame);
    this.chartPanel.addChartMouseListener(listener);
    this.chartPanel.addMouseMotionListener(listener);
    this.chartPanel.addMouseListener(listener);
  }

  /**
   * Plotting a Gantt chart.
   *
   * @param title
   *          the title
   * @param ganttData
   *          the gantt data
   */
  public GanttPlotter(final String title, final GanttData ganttData) {
    super(title);

    this.chart = createChart(GanttPlotter.createDataset(ganttData));
    this.chartPanel = new ChartPanel(this.chart);
    this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
    this.chartPanel.setMouseZoomable(true, true);
    setContentPane(this.chartPanel);

  }

  /*
   * (non-Javadoc)
   *
   * @see org.jfree.ui.ApplicationFrame#windowClosing(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosing(final WindowEvent event) {
  }

  /**
   * Gets the background color gradient.
   *
   * @return the background color gradient
   */
  public static LinearGradientPaint getBackgroundColorGradient() {
    final Point2D start = new Point2D.Float(0, 0);
    final Point2D end = new Point2D.Float(500, 500);
    final float[] dist = { 0.0f, 0.8f };
    final Color[] colors = { new Color(170, 160, 190), Color.WHITE };
    final LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
    return p;
  }
}
