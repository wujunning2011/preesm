/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Julien Hascoet <jhascoet@kalray.eu> (2017)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2017)
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
package org.ietr.preesm.mapper.checker;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.ietr.dftools.algorithm.iterators.DAGIterator;
import org.ietr.dftools.algorithm.model.dag.DAGEdge;
import org.ietr.dftools.algorithm.model.dag.DAGVertex;
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.preesm.core.types.ImplementationPropertyNames;

/**
 * The purpose of the {@link CommunicationOrderChecker} is to verify the order of communications resulting from a scheduling. In particular, the checker
 * verifies if the Send and Receive communication primitives for each pair of core is always scheduled with the exact same order on both sides. For example:<br>
 * <ul>
 * <li>Correct schedule:
 * <ul>
 * <li>Core0 schedule : SendA, ..., SendB</li>
 * <li>Core1 schedule : RecvA, ..., RecvB</li>
 * </ul>
 * </li>
 * <li>Invalid schedule:
 * <ul>
 * <li>Core0 schedule : SendA, ..., SendB</li>
 * <li>Core1 schedule : RecvB, ..., RecvA</li>
 * </ul>
 * </li>
 * </ul>
 *
 *
 * @author kdesnos
 *
 */
public class CommunicationOrderChecker {

  /**
   * Function responsible for checking the validity of the schedule as specified in {@link CommunicationOrderChecker}.
   *
   * @param dag
   *          The {@link DirectedAcyclicGraph} whose schedule is verified.
   *
   * @throws WorkflowException
   *           if the schedule is incorrect.
   */
  public static void checkCommunicationOrder(final DirectedAcyclicGraph dag) {
    // Check communication ordering
    final DAGIterator iterDAGVertices = new DAGIterator(dag); // Iterator on DAG vertices

    // Create an array list of the Send/Receive DAGVertices, in scheduling order.
    final SortedMap<Integer, DAGVertex> sendVerticesMap = new TreeMap<>();
    final SortedMap<Integer, DAGVertex> recvVerticesMap = new TreeMap<>();

    // Store all used processing elements
    final Set<ComponentInstance> sendComponents = new LinkedHashSet<>();
    final Set<ComponentInstance> recvComponents = new LinkedHashSet<>();

    while (iterDAGVertices.hasNext()) {
      final DAGVertex currentVertex = iterDAGVertices.next();

      final String vertexType = currentVertex.getPropertyBean().getValue(ImplementationPropertyNames.Vertex_vertexType).toString();
      final boolean isSend = vertexType.equals("send");
      final boolean isReceive = vertexType.equals("receive");

      // get component
      final ComponentInstance comp = (ComponentInstance) currentVertex.getPropertyBean().getValue("Operator");

      // Get scheduling order
      final int schedulingOrder = (Integer) currentVertex.getPropertyBean().getValue(ImplementationPropertyNames.Vertex_schedulingOrder);
      if (isSend) {
        sendVerticesMap.put(schedulingOrder, currentVertex);
        sendComponents.add(comp);
      }
      if (isReceive) {
        recvVerticesMap.put(schedulingOrder, currentVertex);
        recvComponents.add(comp);
      }
    }

    // Check the order is identical on send and receive sides
    for (final ComponentInstance sendComponent : sendComponents) {
      for (final ComponentInstance recvComponent : recvComponents) {
        // For each pair of sender/receiver

        // Collect sender and receivers DAGVertices for this pair (in scheduling order)
        final List<DAGVertex> senders = new ArrayList<>(sendVerticesMap.values());
        senders.removeIf(vertex -> !((ComponentInstance) vertex.getPropertyBean().getValue(ImplementationPropertyNames.Vertex_Operator)).equals(sendComponent));

        final List<DAGVertex> receivers = new ArrayList<>(recvVerticesMap.values());
        receivers
            .removeIf(vertex -> !((ComponentInstance) vertex.getPropertyBean().getValue(ImplementationPropertyNames.Vertex_Operator)).equals(recvComponent));

        // Get corresponding edges (in scheduling order)
        final List<DAGEdge> senderDagEdges = new ArrayList<>(senders.size());
        senders
            .forEach(sender -> senderDagEdges.add((DAGEdge) sender.getPropertyBean().getValue(ImplementationPropertyNames.SendReceive_correspondingDagEdge)));
        final List<DAGEdge> receiverDagEdges = new ArrayList<>(receivers.size());
        receivers.forEach(
            receiver -> receiverDagEdges.add((DAGEdge) receiver.getPropertyBean().getValue(ImplementationPropertyNames.SendReceive_correspondingDagEdge)));

        // Keep only the DAGEdges in common (they are the one corresponding to communications between the selected sender and receiver
        receiverDagEdges.retainAll(senderDagEdges);
        senderDagEdges.retainAll(receiverDagEdges);

        // Throws an exception if the schedule is incorrect.
        if (!senderDagEdges.equals(receiverDagEdges)) {
          throw new WorkflowException("Order of communication primitives (Send/Receive) is not preserved between components " + sendComponent + " and "
              + recvComponent + ". Contact Preesm developers for more information.");
        }
      }
    }
  }

}
