/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.plugin.abc.impl.latency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.swt.widgets.Composite;
import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.Operator;
import org.ietr.preesm.core.scenario.IScenario;
import org.ietr.preesm.core.tools.PreesmLogger;
import org.ietr.preesm.plugin.abc.AbcType;
import org.ietr.preesm.plugin.abc.AbstractAbc;
import org.ietr.preesm.plugin.abc.SpecialVertexManager;
import org.ietr.preesm.plugin.abc.edgescheduling.AbstractEdgeSched;
import org.ietr.preesm.plugin.abc.edgescheduling.EdgeSchedType;
import org.ietr.preesm.plugin.abc.edgescheduling.IEdgeSched;
import org.ietr.preesm.plugin.abc.edgescheduling.IntervalFinder;
import org.ietr.preesm.plugin.abc.impl.ImplementationCleaner;
import org.ietr.preesm.plugin.abc.order.IScheduleElement;
import org.ietr.preesm.plugin.abc.order.SynchronizedVertices;
import org.ietr.preesm.plugin.abc.order.VertexOrderList;
import org.ietr.preesm.plugin.abc.order.VertexOrderList.OrderProperty;
import org.ietr.preesm.plugin.abc.route.AbstractCommunicationRouter;
import org.ietr.preesm.plugin.abc.route.CommunicationRouter;
import org.ietr.preesm.plugin.mapper.model.MapperDAG;
import org.ietr.preesm.plugin.mapper.model.MapperDAGEdge;
import org.ietr.preesm.plugin.mapper.model.MapperDAGVertex;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdge;
import org.ietr.preesm.plugin.mapper.model.impl.PrecedenceEdgeAdder;
import org.ietr.preesm.plugin.mapper.params.AbcParameters;
import org.ietr.preesm.plugin.mapper.plot.GanttPlotter;
import org.ietr.preesm.plugin.mapper.timekeeper.NewTimeKeeper;
import org.ietr.preesm.plugin.mapper.tools.SchedulingOrderIterator;
import org.ietr.preesm.plugin.mapper.tools.TLevelIterator;
import org.ietr.preesm.plugin.mapper.tools.TopologicalDAGIterator;
import org.sdf4j.model.dag.DAGEdge;
import org.sdf4j.model.dag.DAGVertex;

/**
 * Abc that minimizes latency
 * 
 * @author mpelcat
 */
public abstract class LatencyAbc extends AbstractAbc {

	/**
	 * Current time keeper: called exclusively by simulator to update the useful
	 * time tags in DAG
	 */
	// protected GraphTimeKeeper timeKeeper;
	protected NewTimeKeeper nTimeKeeper;

	protected AbstractCommunicationRouter comRouter = null;

	/**
	 * Scheduling the transfer vertices on the media
	 */
	protected IEdgeSched edgeScheduler;

	/**
	 * Current abc parameters
	 */
	protected AbcParameters params;

	/**
	 * Constructor of the simulator from a "blank" implementation where every
	 * vertex has not been implanted yet.
	 */
	public LatencyAbc(AbcParameters params, MapperDAG dag,
			MultiCoreArchitecture archi, AbcType abcType, IScenario scenario) {
		super(dag, archi, abcType, scenario);

		this.params = params;

		nTimeKeeper = new NewTimeKeeper(implementation, orderManager);
		nTimeKeeper.resetTimings();

		// this.timeKeeper = new GraphTimeKeeper(implementation, nTimeKeeper);
		// timeKeeper.resetTimings();

		// The media simulator calculates the edges costs
		edgeScheduler = AbstractEdgeSched.getInstance(
				params.getEdgeSchedType(), orderManager);
		comRouter = new CommunicationRouter(archi, scenario, implementation,
				edgeScheduler, orderManager);
	}

	/**
	 * Sets the DAG as current DAG and retrieves all implementation to calculate
	 * timings
	 */
	@Override
	public void setDAG(MapperDAG dag) {

		this.dag = dag;
		this.implementation = dag.clone();

		orderManager.reconstructTotalOrderFromDAG(implementation);

		nTimeKeeper = new NewTimeKeeper(implementation, orderManager);
		nTimeKeeper.resetTimings();

		// this.timeKeeper = new GraphTimeKeeper(implementation, nTimeKeeper);
		// timeKeeper.resetTimings();

		// Forces the unmapping process before the new mapping process
		HashMap<MapperDAGVertex, Operator> operators = new HashMap<MapperDAGVertex, Operator>();

		for (DAGVertex v : dag.vertexSet()) {
			MapperDAGVertex mdv = (MapperDAGVertex) v;
			operators.put(mdv, mdv.getImplementationVertexProperty()
					.getEffectiveOperator());
			mdv.getImplementationVertexProperty().setEffectiveComponent(
					Operator.NO_COMPONENT);
			implementation.getMapperDAGVertex(mdv.getName())
					.getImplementationVertexProperty().setEffectiveComponent(
							Operator.NO_COMPONENT);
			;
		}

		edgeScheduler = AbstractEdgeSched.getInstance(edgeScheduler
				.getEdgeSchedType(), orderManager);
		comRouter.setManagers(implementation, edgeScheduler, orderManager);

		SchedulingOrderIterator iterator = new SchedulingOrderIterator(
				this.dag, this, true);

		while (iterator.hasNext()) {
			MapperDAGVertex vertex = iterator.next();
			Operator operator = operators.get(vertex);

			implant(vertex, operator, false);
		}
	}

	@Override
	protected void fireNewMappedVertex(MapperDAGVertex vertex,
			boolean updateRank) {

		Operator effectiveOp = vertex.getImplementationVertexProperty()
				.getEffectiveOperator();

		if (effectiveOp == Operator.NO_COMPONENT) {
			PreesmLogger.getLogger().severe(
					"implementation of " + vertex.getName() + " failed");
		} else {

			long vertextime = vertex.getInitialVertexProperty().getTime(
					effectiveOp);

			// Set costs
			vertex.getTimingVertexProperty().setCost(vertextime);

			setEdgesCosts(vertex.incomingEdges());
			setEdgesCosts(vertex.outgoingEdges());

			if (updateRank) {
				updateTimings();
				taskScheduler.insertVertex(vertex);
			} else {
				orderManager.insertGivenTotalOrder(vertex);
			}

		}
	}

	@Override
	protected void fireNewUnmappedVertex(MapperDAGVertex vertex) {

		// unimplanting a vertex resets the cost of the current vertex
		// and its edges

		ImplementationCleaner cleaner = new ImplementationCleaner(orderManager,
				implementation);
		cleaner.removeAllOverheads(vertex);
		cleaner.removeAllInvolvements(vertex);
		cleaner.removeAllTransfers(vertex);
		cleaner.unscheduleVertex(vertex);

		// Keeps the total order
		orderManager.remove(vertex, false);

		vertex.getTimingVertexProperty().reset();
		resetCost(vertex.incomingEdges());
		resetCost(vertex.outgoingEdges());

	}

	@Override
	public void implant(MapperDAGVertex dagvertex, Operator operator,
			boolean updateRank) {
		super.implant(dagvertex, operator, updateRank);
		// timeKeeper.setAsDirty(dagvertex);
	}

	@Override
	public void unimplant(MapperDAGVertex dagvertex) {
		super.unimplant(dagvertex);
		// timeKeeper.setAsDirty(dagvertex);
	}

	/**
	 * Asks the time keeper to update timings. Crucial and costly operation.
	 * Depending on the king of timings we want, calls the necessary updates.
	 */
	public void updateTimings() {

		// timeKeeper.updateTLevels();
		nTimeKeeper.updateTLevels();
	}

	/**
	 * Setting edge costs for special types
	 */
	@Override
	protected void setEdgeCost(MapperDAGEdge edge) {

	}

	public abstract EdgeSchedType getEdgeSchedType();

	/**
	 * *********Timing accesses**********
	 */

	/**
	 * The cost of a vertex is the end time of its execution (latency
	 * minimization)
	 */
	@Override
	public final long getFinalCost(MapperDAGVertex vertex) {
		vertex = translateInImplementationVertex(vertex);

		long finalTime = nTimeKeeper.getFinalTime(vertex);

		if (finalTime < 0) {
			PreesmLogger.getLogger().log(Level.SEVERE,
					"negative vertex final time");
		}

		return finalTime;

	}

	/**
	 * The cost of a component is the end time of its last vertex (latency
	 * minimization)
	 */
	@Override
	public final long getFinalCost(ArchitectureComponent component) {

		long finalTime = nTimeKeeper.getFinalTime(component);

		return finalTime;
	}

	/**
	 * The cost of an implementation is calculated from its latency and loads
	 */
	@Override
	public final long getFinalCost() {

		long cost = getFinalLatency();

		if (params.isBalanceLoads()) {
			long loadBalancing = evaluateLoadBalancing();
			cost += loadBalancing;
		}
		return cost;
	}

	/**
	 * The cost of an implementation is calculated from its latency and loads
	 */
	public final long getFinalLatency() {

		long finalTime = nTimeKeeper.getFinalTime();

		if (finalTime < 0) {
			PreesmLogger.getLogger().log(Level.SEVERE,
					"negative implementation final latency");
		}

		return finalTime;
	}

	public final long getTLevel(MapperDAGVertex vertex, boolean update) {
		vertex = translateInImplementationVertex(vertex);

		if (update)
			updateTimings();
		return vertex.getTimingVertexProperty().getNewtLevel();
	}

	public final long getBLevel(MapperDAGVertex vertex, boolean update) {
		vertex = translateInImplementationVertex(vertex);

		if (update)
			updateTimings();
		return vertex.getTimingVertexProperty().getNewbLevel();
	}

	/**
	 * Plots the current implementation. If delegatedisplay=false, the gantt is
	 * displayed in a shell. Otherwise, it is displayed in Eclipse.
	 */
	public final void plotImplementation(Composite delegateDisplay) {

		GanttPlotter.plotDeployment(implementation, this.getArchitecture(),
				delegateDisplay);
	}

	public AbstractCommunicationRouter getComRouter() {
		return comRouter;
	}

	/**
	 * Gives an index evaluating the load balancing. This index is actually the
	 * standard deviation of the loads considered as values of a random variable
	 */
	public long evaluateLoadBalancing() {

		List<Long> taskSums = new ArrayList<Long>();
		long totalTaskSum = 0l;

		for (ArchitectureComponent o : orderManager.getArchitectureComponents()) {
			long load = getLoad(o);

			if (load > 0) {
				taskSums.add(load);
				totalTaskSum += load;
			}
		}

		if (taskSums.size() > 0) {
			Collections.sort(taskSums, new Comparator<Long>() {
				@Override
				public int compare(Long arg0, Long arg1) {
					return (int) (arg0 - arg1);
				}
			});

			long mean = totalTaskSum / taskSums.size();
			long variance = 0;
			// Calculating the load sum of half the components with the lowest
			// loads
			for (long taskDuration : taskSums) {
				variance += ((taskDuration - mean) * (taskDuration - mean))
						/ taskSums.size();
			}

			return (long) Math.sqrt(variance);
		}

		return 0;
	}

	/**
	 * Returns the sum of execution times on the given component
	 */
	public final long getLoad(ArchitectureComponent component) {

		long load2 = orderManager.getBusyTime(component);

		/*
		 * long load = 0; if (implementation != null) { for (DAGVertex v :
		 * implementation.vertexSet()) { MapperDAGVertex mv = (MapperDAGVertex)
		 * v; if (mv.getImplementationVertexProperty()
		 * .getEffectiveComponent().equals(component)) { load += getCost(mv); }
		 * } }
		 * 
		 * if(load2 != load){ int i=0; i++; }
		 */

		return load2;
	}

	/**
	 * Reschedule all the transfers generated during mapping
	 */
	/*
	 * public void rescheduleTransfers(List<MapperDAGVertex> cpnDominantList) {
	 * 
	 * if (this.orderManager != null) { ImplementationCleaner cleaner = new
	 * ImplementationCleaner( orderManager, implementation);
	 * 
	 * for (ArchitectureComponent cmp : archi
	 * .getComponents(ArchitectureComponentType.contentionNode)) { for
	 * (MapperDAGVertex v : this.orderManager.getSchedule(cmp) .getList()) {
	 * cleaner.unscheduleVertex(v); } }
	 * 
	 * updateTimings();
	 * 
	 * for (ArchitectureComponent cmp : archi
	 * .getComponents(ArchitectureComponentType.contentionNode)) {
	 * ConcurrentSkipListSet<MapperDAGVertex> list = new
	 * ConcurrentSkipListSet<MapperDAGVertex>( new Comparator<MapperDAGVertex>()
	 * {
	 * 
	 * @Override public int compare(MapperDAGVertex arg0, MapperDAGVertex arg1)
	 * { long TLevelDifference = (getTLevel(arg0, false) - getTLevel( arg1,
	 * false)); if (TLevelDifference == 0) TLevelDifference = (arg0.getName()
	 * .compareTo(arg1.getName())); return (int) TLevelDifference; } });
	 * list.addAll(this.orderManager.getSchedule(cmp).getList());
	 * 
	 * for (MapperDAGVertex v : list) { TransferVertex tv = (TransferVertex) v;
	 * orderManager.insertVertexBefore(tv, tv.getTarget()); } } }
	 */

	/*
	 * Schedule totalOrder = this.getTotalOrder(); List<String> orderedNames =
	 * new ArrayList<String>();
	 * 
	 * for (MapperDAGVertex v : totalOrder) { if (v instanceof TransferVertex) {
	 * // addVertexAfterSourceLastTransfer(v, orderedNames); } else if (v
	 * instanceof OverheadVertex) { addVertexAfterSourceLastOverhead(v,
	 * orderedNames); } else { orderedNames.add(v.getName()); } }
	 * 
	 * for(int index = cpnDominantList.size()-1;index >= 0 ; index--){
	 * MapperDAGVertex v = cpnDominantList.get(index); for (DAGVertex t :
	 * ImplementationCleaner
	 * .getFollowingTransfers(this.translateInImplementationVertex(v))) { if
	 * (!orderedNames.contains(t.getName())) {
	 * addVertexAfterSourceLastTransfer((MapperDAGVertex)t, orderedNames); } } }
	 */
	/*
	 * for (MapperDAGVertex v : cpnDominantList) { for (DAGVertex t :
	 * ImplementationCleaner
	 * .getPrecedingTransfers(this.translateInImplementationVertex(v))) { if
	 * (!orderedNames.contains(t.getName())) {
	 * addVertexBeforeTarget((MapperDAGVertex)t, orderedNames); } } }
	 */

	/*
	 * MapperDAGVertex v = totalOrder.getLast();
	 * 
	 * while(v!=null){ if (v instanceof TransferVertex) {
	 * addVertexBeforeTargetFirstTransfer(v, orderedNames); } else if (v
	 * instanceof OverheadVertex) { //addVertexAfterSourceLastOverhead(v,
	 * orderedNames); } else { //orderedNames.add(v.getName()); } v =
	 * totalOrder.getPreviousVertex(v); }
	 */
	// reorder(orderedNames);}
	@Override
	public void updateFinalCosts() {
		updateTimings();
	}

	/**
	 * Reorders the implementation using the given total order
	 */
	@Override
	public void reschedule(VertexOrderList totalOrder) {

		if (implementation != null && dag != null) {

			// Sets the order in the implementation
			for (VertexOrderList.OrderProperty vP : totalOrder.elements()) {
				MapperDAGVertex ImplVertex = (MapperDAGVertex) implementation
						.getVertex(vP.getName());
				if (ImplVertex != null)
					ImplVertex.getImplementationVertexProperty()
							.setSchedTotalOrder(vP.getOrder());

				MapperDAGVertex dagVertex = (MapperDAGVertex) dag.getVertex(vP
						.getName());
				if (dagVertex != null)
					dagVertex.getImplementationVertexProperty()
							.setSchedTotalOrder(vP.getOrder());

			}

			// Retrieves the new order in order manager
			orderManager.reconstructTotalOrderFromDAG(implementation);

			PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder(orderManager,
					implementation);
			adder.removePrecedenceEdges();
			adder.addPrecedenceEdges();

		}
	}

	/**
	 * Reorders the implementation
	 */
	public void reschedule2() {

		if (implementation != null && dag != null) {
			PreesmLogger.getLogger().log(Level.INFO, "Reordering");
			PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder(orderManager,
					implementation);
			adder.removePrecedenceEdges();

			nTimeKeeper.update(null, implementation.vertexSet());
			updateTimings();
			this.plotImplementation(null);

			int index = 0;
			TLevelIterator iterator = new TLevelIterator(implementation, true);

			while (iterator.hasNext()) {
				MapperDAGVertex implVertex = iterator.next();
				if (implVertex != null)
					implVertex.getImplementationVertexProperty()
							.setSchedTotalOrder(index);

				MapperDAGVertex dagVertex = (MapperDAGVertex) dag
						.getVertex(implVertex.getName());
				if (dagVertex != null)
					dagVertex.getImplementationVertexProperty()
							.setSchedTotalOrder(index);

				index++;
			}

			// Retrieves the new order in order manager
			orderManager.reconstructTotalOrderFromDAG(implementation);

			adder.addPrecedenceEdges();

		}
	}

	private class IshedTLevelComp implements Comparator<IScheduleElement> {

		@Override
		public int compare(IScheduleElement arg0, IScheduleElement arg1) {
			MapperDAGVertex v0 = null;
			MapperDAGVertex v1 = null;
			if (arg0 instanceof MapperDAGVertex) {
				v0 = (MapperDAGVertex) arg0;
			} else if (arg0 instanceof SynchronizedVertices) {
				v0 = ((SynchronizedVertices) arg0).vertices().get(0);
			}

			if (arg1 instanceof MapperDAGVertex) {
				v1 = (MapperDAGVertex) arg1;
			} else if (arg1 instanceof SynchronizedVertices) {
				v1 = ((SynchronizedVertices) arg1).vertices().get(0);
			}

			return (int) (v0.getTimingVertexProperty().getNewtLevel() - v1
					.getTimingVertexProperty().getNewtLevel());
		}

	}

	/**
	 * Reorders the implementation
	 */
	@Override
	public void reschedule(List<IScheduleElement> alreadyRescheduled) {

		if (implementation != null && dag != null) {

			PrecedenceEdgeAdder adder = new PrecedenceEdgeAdder(orderManager,
					implementation);
			adder.removePrecedenceEdges();

			nTimeKeeper.update(null, implementation.vertexSet());
			updateTimings();
			// this.plotImplementation(null);

			PreesmLogger.getLogger().log(Level.INFO, "Reordering");
			List<IScheduleElement> vList = new ArrayList<IScheduleElement>(
					orderManager.getTotalOrder().getList());

			Collections.sort(vList, new IshedTLevelComp());

			Map<IScheduleElement, IScheduleElement> refMap = new HashMap<IScheduleElement, IScheduleElement>();
			List<IScheduleElement> eltList = new ArrayList<IScheduleElement>();

			IntervalFinder finder = new IntervalFinder(orderManager);
			for (IScheduleElement elt : vList) {
				MapperDAGVertex v = null;
				if (elt instanceof MapperDAGVertex) {
					v = (MapperDAGVertex) elt;
				} else if (elt instanceof SynchronizedVertices) {
					v = ((SynchronizedVertices) elt).vertices().get(0);
				}

				int index = -1;
				if (SpecialVertexManager.isSpecial(v)) {
					index = finder.getIndexOfFirstBigEnoughHole(v, 0);
				} else {
					index = finder.getIndexOfFirstBigEnoughHole(v, v
							.getTimingVertexProperty().getCost());
				}

				if (index > -1
						&& index < v.getImplementationVertexProperty()
								.getSchedTotalOrder()) {
					IScheduleElement reference = orderManager.get(index);
					refMap.put(elt, reference);
					eltList.add(elt);
				}
			}

			for (int i = eltList.size() - 1; i >= 0; i--) {
				IScheduleElement elt = eltList.get(i);

				if (!alreadyRescheduled.contains(elt)) {
					IScheduleElement ref = refMap.get(elt);
					int newIndex = vList.indexOf(ref);
					vList.remove(elt);
					vList.add(newIndex, elt);
					alreadyRescheduled.add(elt);
				}
			}

			VertexOrderList orderList = new VertexOrderList();

			for (IScheduleElement elt : vList) {
				if (elt instanceof MapperDAGVertex) {
					MapperDAGVertex v = (MapperDAGVertex) elt;
					VertexOrderList.OrderProperty op = orderList.new OrderProperty(
							v.getName(), vList.indexOf(v));
					orderList.addLast(op);
				} else if (elt instanceof SynchronizedVertices) {
					for (MapperDAGVertex v : ((SynchronizedVertices) elt)
							.vertices()) {
						VertexOrderList.OrderProperty op = orderList.new OrderProperty(
								v.getName(), vList.indexOf(v));
						orderList.addLast(op);
					}
				}
			}

			reschedule(orderList);

		}
	}
}
