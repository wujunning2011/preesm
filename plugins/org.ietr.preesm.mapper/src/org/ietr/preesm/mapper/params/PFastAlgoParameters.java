/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Jonathan Piat <jpiat@laas.fr> (2011)
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
package org.ietr.preesm.mapper.params;

import java.util.Map;
import java.util.logging.Level;
import org.ietr.dftools.workflow.tools.WorkflowLogger;

// TODO: Auto-generated Javadoc
/**
 * Parameters for task scheduling FAST algorithm multithread.
 *
 * @author pmenuet
 * @author mpelcat
 */
public class PFastAlgoParameters {

  /**
   * This variable is the one which fix the number of Nodes necessary for each processor to execute probabilistic jump locally(local refinement). The number of
   * vertices present in the DAG is determinant for this parameter. More, if this number is too low the PFastAlgo become no efficient.
   */
  private int nodesMin;

  /** Number of processors available. */
  private int procNumber;

  /** true if we need to display the intermediate solutions. */
  private boolean displaySolutions;

  /** Time in seconds to run one FAST. */
  private int fastTime = 200;

  /** Time in seconds spent in one FAST local neighborhood. */
  private int fastLocalSearchTime = 10;

  /** Number of fast iterations to execute before stopping PFast. */
  private int fastNumber = 100;

  /**
   * Constructors.
   *
   * @param textParameters
   *          the text parameters
   */

  public PFastAlgoParameters(final Map<String, String> textParameters) {

    this.nodesMin = Integer.valueOf(textParameters.get("nodesMin"));
    this.procNumber = Integer.valueOf(textParameters.get("procNumber"));
    this.displaySolutions = Boolean.valueOf(textParameters.get("displaySolutions"));
    if (Integer.valueOf(textParameters.get("fastTime")) > 0) {
      this.fastTime = Integer.valueOf(textParameters.get("fastTime"));
    }
    if (Integer.valueOf(textParameters.get("fastLocalSearchTime")) > 0) {
      this.fastLocalSearchTime = Integer.valueOf(textParameters.get("fastLocalSearchTime"));
    }
    if (Integer.valueOf(textParameters.get("fastNumber")) != 0) {
      this.fastNumber = Integer.valueOf(textParameters.get("fastNumber"));
    }

    WorkflowLogger.getLogger().log(Level.INFO,
        "The PFast algo parameters are: nodesMin; procNumber; displaySolutions=true/false; fastTime in seconds; fastLocalSearchTime in seconds; fastNumber");

  }

  /**
   * Instantiates a new p fast algo parameters.
   *
   * @param fastNumber
   *          the fast number
   * @param fastTime
   *          the fast time
   * @param fastLocalSearchTime
   *          the fast local search time
   * @param displaySolutions
   *          the display solutions
   * @param nodesmin
   *          the nodesmin
   * @param procNumber
   *          the proc number
   */
  public PFastAlgoParameters(final int fastNumber, final int fastTime, final int fastLocalSearchTime, final boolean displaySolutions, final int nodesmin,
      final int procNumber) {

    this.nodesMin = nodesmin;
    this.procNumber = procNumber;
    this.displaySolutions = displaySolutions;
    this.fastTime = fastTime;
    this.fastLocalSearchTime = fastLocalSearchTime;
    this.fastNumber = fastNumber;
  }

  /**
   * Getters and Setters.
   *
   * @return the proc number
   */

  public int getProcNumber() {
    return this.procNumber;
  }

  /**
   * Sets the proc number.
   *
   * @param procNumber
   *          the new proc number
   */
  public void setProcNumber(final int procNumber) {
    this.procNumber = procNumber;
  }

  /**
   * Checks if is display solutions.
   *
   * @return true, if is display solutions
   */
  public boolean isDisplaySolutions() {
    return this.displaySolutions;
  }

  /**
   * Sets the display solutions.
   *
   * @param displaySolutions
   *          the new display solutions
   */
  public void setDisplaySolutions(final boolean displaySolutions) {
    this.displaySolutions = displaySolutions;
  }

  /**
   * Gets the nodesmin.
   *
   * @return the nodesmin
   */
  public int getNodesmin() {
    return this.nodesMin;
  }

  /**
   * Sets the nodesmin.
   *
   * @param nodesmin
   *          the new nodesmin
   */
  public void setNodesmin(final int nodesmin) {
    this.nodesMin = nodesmin;
  }

  /**
   * Returns the Number of fast iterations to execute before stopping PFast.
   *
   * @return the fast number
   */
  public int getFastNumber() {
    return this.fastNumber;
  }

  /**
   * Returns the time in seconds for one whole FAST process.
   *
   * @return the fast time
   */
  public int getFastTime() {
    return this.fastTime;
  }

  /**
   * Returns the time in seconds spent in one FAST local neighborhood.
   *
   * @return the fast local search time
   */
  public int getFastLocalSearchTime() {
    return this.fastLocalSearchTime;
  }

}
