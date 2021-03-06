/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2009 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2009 - 2016)
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
package org.ietr.preesm.core.architecture.route;

import java.util.ArrayList;
import java.util.List;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.architecture.slam.component.ComNode;
import org.ietr.dftools.architecture.slam.component.Component;
import org.ietr.dftools.architecture.slam.component.impl.ComNodeImpl;

// TODO: Auto-generated Javadoc
/**
 * Represents a single step in a route between two operators separated by communication nodes.
 *
 * @author mpelcat
 */
public class MessageRouteStep extends AbstractRouteStep {

  /** Communication nodes separating the sender and the receiver. */
  protected List<ComponentInstance> nodes = null;

  /**
   * The route step type determines how the communication will be simulated.
   */
  public static final String type = "NodeRouteStep";

  /**
   * Instantiates a new message route step.
   *
   * @param sender
   *          the sender
   * @param inNodes
   *          the in nodes
   * @param receiver
   *          the receiver
   */
  public MessageRouteStep(final ComponentInstance sender, final List<ComponentInstance> inNodes, final ComponentInstance receiver) {
    super(sender, receiver);
    this.nodes = new ArrayList<>();

    for (final ComponentInstance node : inNodes) {
      this.nodes.add(node);
    }
  }

  /**
   * The route step type determines how the communication will be simulated.
   *
   * @return the type
   */
  @Override
  public String getType() {
    return MessageRouteStep.type;
  }

  /**
   * The id is given to code generation. It selects the communication functions to use
   *
   * @return the id
   */
  @Override
  public String getId() {
    String id = "";
    for (final ComponentInstance node : this.nodes) {
      id += node.getComponent().getVlnv().getName();
    }
    return id;
  }

  /**
   * The name of the step node is retrieved.
   *
   * @return the name
   */
  @Override
  public String getName() {
    String name = "";
    for (final ComponentInstance node : this.nodes) {
      name += node.getInstanceName();
    }
    return name;
  }

  /**
   * Gets the contention nodes.
   *
   * @return the contention nodes
   */
  public List<ComponentInstance> getContentionNodes() {
    final List<ComponentInstance> contentionNodes = new ArrayList<>();
    for (final ComponentInstance node : this.nodes) {
      if (node.getComponent() instanceof ComNodeImpl) {
        if (!((ComNode) node.getComponent()).isParallel()) {
          contentionNodes.add(node);
        }
      }
    }
    return contentionNodes;
  }

  /**
   * Gets the nodes.
   *
   * @return the nodes
   */
  public List<ComponentInstance> getNodes() {
    return this.nodes;
  }

  /**
   * Returns the longest time a node needs to transfer the data.
   *
   * @param transfersSize
   *          the transfers size
   * @return the worst transfer time
   */
  @Override
  public final long getWorstTransferTime(final long transfersSize) {
    long time = 0;

    for (final ComponentInstance node : this.nodes) {
      final Component def = node.getComponent();
      if (def instanceof ComNodeImpl) {
        time = Math.max(time, (long) (transfersSize / ((ComNode) def).getSpeed()));
      }
    }

    // No zero transfer time is alloweds
    if (time <= 0) {
      time = 1;
    }

    return time;
  }

  /**
   * Evaluates the cost of a data transfer with size transferSize. Can include overheads, additional cost...
   *
   * @param transfersSize
   *          the transfers size
   * @return the transfer cost
   */
  @Override
  public long getTransferCost(final long transfersSize) {
    return getWorstTransferTime(transfersSize);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String trace = "{" + getSender().toString() + " -> ";
    for (final ComponentInstance node : this.nodes) {
      trace += node.getInstanceName() + " ";
    }
    trace += "-> " + getReceiver().toString() + "}";
    return trace;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.preesm.core.architecture.route.AbstractRouteStep#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new MessageRouteStep(getSender(), this.nodes, getReceiver());
  }

}
