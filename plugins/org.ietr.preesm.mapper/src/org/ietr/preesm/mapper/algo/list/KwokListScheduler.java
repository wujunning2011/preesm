/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2008 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012)
 * Matthieu Wipliez <matthieu.wipliez@insa-rennes.fr> (2008)
 * Maxime Pelcat <maxime.pelcat@insa-rennes.fr> (2008 - 2014)
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
package org.ietr.preesm.mapper.algo.list;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ietr.dftools.architecture.slam.ComponentInstance;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.mapper.abc.IAbc;
import org.ietr.preesm.mapper.model.MapperDAG;
import org.ietr.preesm.mapper.model.MapperDAGVertex;

// TODO: Auto-generated Javadoc
/**
 * List scheduler from Yu Kwong Kwok PhD thesis.
 *
 * @author pmenuet
 * @author mpelcat
 */
public class KwokListScheduler {

  /**
   * constructor.
   */
  public KwokListScheduler() {
    super();
  }

  /**
   * operatorvertexstarttime: Return the date when the operator is ready to process the vertex.
   *
   * @param dag
   *          the dag
   * @param vertex
   *          the vertex
   * @param operator
   *          the operator
   * @param simu
   *          the simu
   * @param minimizeVStartorOpEnd
   *          If true, we minimize the starting date of the vertex; if false, we minimize the current scheduling length on the considered operator
   * @return the long
   * @throws WorkflowException
   *           the workflow exception
   */
  public long listImplementationCost(final MapperDAG dag, MapperDAGVertex vertex, final ComponentInstance operator, final IAbc simu,
      final boolean minimizeVStartorOpEnd) throws WorkflowException {

    // check the vertex is into the DAG
    vertex = dag.getMapperDAGVertex(vertex.getName());

    // maps the vertex on the operator
    simu.map(vertex, operator, true, false);
    simu.updateFinalCosts();

    // check if the vertex is a source vertex with no predecessors
    if (minimizeVStartorOpEnd) {
      return simu.getFinalCost(vertex);
    } else {
      return simu.getFinalCost(operator);
    }
  }

  /**
   * schedule: Do a mapping with the help of the lists (CPN-Dominant list, Blocking node list and the FCP list) and the architecture. It can take one vertex
   * already mapped with a particular operator chosen by the user and only one.
   *
   * @param dag
   *          the dag
   * @param orderlist
   *          the orderlist
   * @param archisimu
   *          the archisimu
   * @param operatorfcp
   *          the operatorfcp
   * @param fcpvertex
   *          the fcpvertex
   * @return : Implemented MapperDAG
   * @throws WorkflowException
   *           the workflow exception
   */

  public MapperDAG schedule(final MapperDAG dag, final List<MapperDAGVertex> orderlist, final IAbc archisimu, final ComponentInstance operatorfcp,
      final MapperDAGVertex fcpvertex) throws WorkflowException {

    final boolean minimizeVStartorOpEnd = false;

    // Variables
    ComponentInstance chosenoperator = null;
    final Logger logger = WorkflowLogger.getLogger();

    // Maps the fastest one to be ready among the operators in the vertex
    // check the vertex by priority in the CPN-Dominant list
    logger.log(Level.FINEST, " entering schedule ");
    for (final MapperDAGVertex currentvertex : orderlist) {

      // Mapping forced by the user or the Fast algorithm
      if (currentvertex.equals(fcpvertex)) {
        if (archisimu.isMapable(fcpvertex, operatorfcp, true)) {
          archisimu.map(currentvertex, operatorfcp, true, false);
        } else {
          // If the group mapping enters in conflict with the fcp mapping, find a solution
          final List<ComponentInstance> groupOperators = archisimu.getCandidateOperators(currentvertex, true);
          if (!groupOperators.isEmpty()) {
            archisimu.map(currentvertex, groupOperators.get(0), true, false);
          } else {
            logger.log(Level.SEVERE, "Found no operator for: " + currentvertex + ". Certainly a relative constraint problem.");
          }
        }
      } else {

        long time = Long.MAX_VALUE;
        // Choose the operator

        final List<ComponentInstance> opList = archisimu.getCandidateOperators(currentvertex, true);
        if (opList.size() == 1) {
          chosenoperator = (ComponentInstance) opList.toArray()[0];
        } else {
          for (final ComponentInstance currentoperator : opList) {

            final long test = listImplementationCost(dag, currentvertex, currentoperator, archisimu, minimizeVStartorOpEnd);

            // To debug list scheduling
            /*
             * IEditorInput input = new StatEditorInput(archisimu, archisimu.getScenario(), null);
             *
             * // Run statistic editor PlatformUI.getWorkbench().getDisplay() .asyncExec(new EditorRunnable(input));
             */

            // test the earliest ready operator
            if (test < time) {
              chosenoperator = currentoperator;
              time = test;
            }

          }
        }

        // ------------------25/06/2012 - Ugly temp fix, remove it soon ----------
        // -----------------This fix will ensure that broadcast (and roundbuffers) are mapped
        // ------------------on the same component as their immediate predecessor (and successor)
        // 04/12/2012 - commented out until semantics of special vertices is precise

        // If the currentVertex is a broadcast
        /*
         * if (currentvertex.getKind().equals("dag_broadcast_vertex") && !(currentvertex.getCorrespondingSDFVertex() instanceof SDFRoundBufferVertex)) { if
         * (currentvertex.incomingEdges().size() > 1) { WorkflowLogger .getLogger() .log(Level.SEVERE,
         * "Broadcast with several inputs: activate \"SuppressImplodeExplode\" in HSDF to solve this issue." ); } else { // Get the unique incoming edge of
         * broadcast DAGEdge inEdge = currentvertex.incomingEdges().iterator() .next(); chosenoperator = archisimu .getEffectiveComponent((MapperDAGVertex)
         * inEdge .getSource());
         *
         * } }
         *
         * // If current vertex has a (or several) roundbuffer(s) as predecessor(s), // map the round buffer on the same component for(DAGEdge inEdge:
         * currentvertex.incomingEdges()){ if(inEdge.getSource().getCorrespondingSDFVertex() instanceof SDFRoundBufferVertex){
         * archisimu.map((MapperDAGVertex)inEdge.getSource(), chosenoperator, true); } }
         *
         * // do not map Roundbuffers yet. They will be mapped with their successors if (!(currentvertex.getCorrespondingSDFVertex() instanceof
         * SDFRoundBufferVertex)) {
         */
        // -----------------End of the temp fix first half-----------------------------------

        // Map on the chosen operator
        archisimu.map(currentvertex, chosenoperator, true, false);

        final int currentVertexRank = orderlist.indexOf(currentvertex);
        if (((currentVertexRank % 100) == 0) && (fcpvertex == null) && (currentVertexRank != 0)) {
          logger.log(Level.INFO, "list scheduling: " + currentVertexRank + " vertices mapped ");
        }

        chosenoperator = null;

        // ------------------Second half of temp fix----------

        /*
         * }else{ // Curent vertex is a RoundBuffer // Do not map round buffer until their immediate sucessor is mapped
         * if(currentvertex.outgoingEdges().size()>1){ WorkflowLogger .getLogger() .log(Level.SEVERE,
         * "RoundBuffer with several outputs: activate \"SuppressImplodeExplode\" in HSDF to solve this issue \n or it will be mapped with only one of its
         * sucessors" ); } }
         */
        // ------------------end of Second half of temp fix----------

      }
    }

    /*
     * List<IScheduleElement> alreadyRescheduled = new ArrayList<IScheduleElement>(); for (int i = 0; i < 20; i++) { archisimu.reschedule(alreadyRescheduled); }
     */
    // archisimu.rescheduleTransfers(orderlist);
    // archisimu.retrieveTotalOrder();

    return dag;
  }
}
