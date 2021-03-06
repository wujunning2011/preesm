/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2014 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2015)
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
package org.ietr.preesm.algorithm.transforms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.dftools.algorithm.model.sdf.transformations.IbsdfFlattener;
import org.ietr.dftools.algorithm.model.sdf.visitors.ConsistencyChecker;
import org.ietr.dftools.algorithm.model.visitors.SDF4JException;
import org.ietr.dftools.algorithm.model.visitors.VisitorOutput;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation;
import org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation;
import org.ietr.dftools.workflow.tools.WorkflowLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiHierarchyFlattening.
 */
public class MultiHierarchyFlattening extends AbstractTaskImplementation {

  /** The Constant DEPTH_KEY. */
  private static final String DEPTH_KEY = "depth";

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#execute(java.util.Map, java.util.Map, org.eclipse.core.runtime.IProgressMonitor,
   * java.lang.String, org.ietr.dftools.workflow.elements.Workflow)
   */
  @Override
  public Map<String, Object> execute(final Map<String, Object> inputs, final Map<String, String> parameters, final IProgressMonitor monitor,
      final String nodeName, final Workflow workflow) throws WorkflowException {

    final Set<SDFGraph> result = new LinkedHashSet<>();
    @SuppressWarnings("unchecked")
    final Set<SDFGraph> algorithms = (Set<SDFGraph>) inputs.get(AbstractWorkflowNodeImplementation.KEY_SDF_GRAPHS_SET);
    final String depthS = parameters.get(MultiHierarchyFlattening.DEPTH_KEY);

    int depth;
    if (depthS != null) {
      depth = Integer.decode(depthS);
    } else {
      depth = 1;
    }

    final Logger logger = WorkflowLogger.getLogger();

    if (depth == 0) {
      for (final SDFGraph algorithm : algorithms) {
        result.add(algorithm.clone());
      }
      logger.log(Level.INFO, "flattening depth = 0: no flattening");
    } else {
      for (final SDFGraph algorithm : algorithms) {

        logger.setLevel(Level.FINEST);
        VisitorOutput.setLogger(logger);
        final ConsistencyChecker checkConsistent = new ConsistencyChecker();
        if (checkConsistent.verifyGraph(algorithm)) {
          logger.log(Level.FINER, "flattening application " + algorithm.getName() + " at level " + depth);
          final IbsdfFlattener flattener = new IbsdfFlattener(algorithm, depth);
          VisitorOutput.setLogger(logger);
          try {
            if (algorithm.validateModel(WorkflowLogger.getLogger())) {
              try {
                flattener.flattenGraph();
              } catch (final SDF4JException e) {
                throw (new WorkflowException(e.getMessage()));
              }
              logger.log(Level.FINER, "flattening complete");
              final SDFGraph resultGraph = flattener.getFlattenedGraph();
              result.add(resultGraph);
            } else {
              throw (new WorkflowException("Graph not valid, not schedulable"));
            }
          } catch (final SDF4JException e) {
            throw (new WorkflowException(e.getMessage()));
          }
        } else {
          logger.log(Level.SEVERE, "Inconsistent Hierarchy, graph can't be flattened");
          result.add(algorithm.clone());
          throw (new WorkflowException("Inconsistent Hierarchy, graph can't be flattened"));
        }
      }
    }

    final Map<String, Object> outputs = new LinkedHashMap<>();
    outputs.put(AbstractWorkflowNodeImplementation.KEY_SDF_GRAPHS_SET, result);
    return outputs;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractTaskImplementation#getDefaultParameters()
   */
  @Override
  public Map<String, String> getDefaultParameters() {
    final Map<String, String> parameters = new LinkedHashMap<>();

    parameters.put(MultiHierarchyFlattening.DEPTH_KEY, "1");
    return parameters;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.ietr.dftools.workflow.implement.AbstractWorkflowNodeImplementation#monitorMessage()
   */
  @Override
  public String monitorMessage() {
    return "Flattening algorithms hierarchies.";
  }

}
