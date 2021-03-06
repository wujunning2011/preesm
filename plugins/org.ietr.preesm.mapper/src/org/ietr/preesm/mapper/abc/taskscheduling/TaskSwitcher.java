/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2009 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2009 - 2012)
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
package org.ietr.preesm.mapper.abc.taskscheduling;

import org.ietr.preesm.mapper.abc.edgescheduling.IntervalFinder;
import org.ietr.preesm.mapper.abc.order.OrderManager;
import org.ietr.preesm.mapper.model.MapperDAGVertex;

// TODO: Auto-generated Javadoc
/**
 * The task switcher adds a processing to the mapping algorithm. When a vertex is mapped, it looks for the best place to schedule it.
 *
 * @author mpelcat
 */
public class TaskSwitcher extends AbstractTaskSched {

  /** The interval finder. */
  private IntervalFinder intervalFinder;

  /**
   * Instantiates a new task switcher.
   */
  public TaskSwitcher() {
    super();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.mapper.abc.taskscheduling.AbstractTaskSched#setOrderManager(org.ietr.preesm. mapper.abc.order.OrderManager)
   */
  @Override
  public void setOrderManager(final OrderManager orderManager) {
    super.setOrderManager(orderManager);
    this.intervalFinder = new IntervalFinder(orderManager);
  }

  /**
   * Insert vertex before.
   *
   * @param successor
   *          the successor
   * @param vertex
   *          the vertex
   */
  public void insertVertexBefore(final MapperDAGVertex successor, final MapperDAGVertex vertex) {

    // Removing the vertex if necessary before inserting it
    if (this.orderManager.totalIndexOf(vertex) != -1) {
      this.orderManager.remove(vertex, true);
    }

    final int newIndex = this.intervalFinder.getBestIndex(vertex, 0);
    if (newIndex >= 0) {
      this.orderManager.insertAtIndex(newIndex, vertex);
    } else {
      this.orderManager.insertBefore(successor, vertex);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.mapper.abc.taskscheduling.AbstractTaskSched#insertVertex(org.ietr.preesm.mapper .model.MapperDAGVertex)
   */
  @Override
  public void insertVertex(final MapperDAGVertex vertex) {

    // Removing the vertex if necessary before inserting it
    if (this.orderManager.totalIndexOf(vertex) != -1) {
      this.orderManager.remove(vertex, true);
    }

    final int newIndex = this.intervalFinder.getBestIndex(vertex, 0);
    if (newIndex >= 0) {
      this.orderManager.insertAtIndex(newIndex, vertex);
    } else {
      this.orderManager.addLast(vertex);
    }
  }
}
