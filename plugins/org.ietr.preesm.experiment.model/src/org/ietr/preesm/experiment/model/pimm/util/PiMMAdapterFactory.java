/*******************************************************************************
 * Copyright or © or Copr. %%LOWERDATE%% - %%UPPERDATE%% IETR/INSA:
 *
 * %%AUTHORS%%
 *
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use
 * modify and/ or redistribute the software under the terms of the CeCILL-C
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
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
/**
 */
package org.ietr.preesm.experiment.model.pimm.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.ietr.preesm.experiment.model.pimm.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage
 * @generated
 */
public class PiMMAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static PiMMPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PiMMAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = PiMMPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PiMMSwitch<Adapter> modelSwitch =
		new PiMMSwitch<Adapter>() {
			@Override
			public Adapter caseParameterizable(Parameterizable object) {
				return createParameterizableAdapter();
			}
			@Override
			public Adapter caseAbstractVertex(AbstractVertex object) {
				return createAbstractVertexAdapter();
			}
			@Override
			public Adapter caseAbstractActor(AbstractActor object) {
				return createAbstractActorAdapter();
			}
			@Override
			public Adapter casePiGraph(PiGraph object) {
				return createPiGraphAdapter();
			}
			@Override
			public Adapter caseActor(Actor object) {
				return createActorAdapter();
			}
			@Override
			public Adapter casePort(Port object) {
				return createPortAdapter();
			}
			@Override
			public Adapter caseDataInputPort(DataInputPort object) {
				return createDataInputPortAdapter();
			}
			@Override
			public Adapter caseDataOutputPort(DataOutputPort object) {
				return createDataOutputPortAdapter();
			}
			@Override
			public Adapter caseConfigInputPort(ConfigInputPort object) {
				return createConfigInputPortAdapter();
			}
			@Override
			public Adapter caseConfigOutputPort(ConfigOutputPort object) {
				return createConfigOutputPortAdapter();
			}
			@Override
			public Adapter caseFifo(Fifo object) {
				return createFifoAdapter();
			}
			@Override
			public Adapter caseInterfaceActor(InterfaceActor object) {
				return createInterfaceActorAdapter();
			}
			@Override
			public Adapter caseDataInputInterface(DataInputInterface object) {
				return createDataInputInterfaceAdapter();
			}
			@Override
			public Adapter caseDataOutputInterface(DataOutputInterface object) {
				return createDataOutputInterfaceAdapter();
			}
			@Override
			public Adapter caseConfigInputInterface(ConfigInputInterface object) {
				return createConfigInputInterfaceAdapter();
			}
			@Override
			public Adapter caseConfigOutputInterface(ConfigOutputInterface object) {
				return createConfigOutputInterfaceAdapter();
			}
			@Override
			public Adapter caseRefinement(Refinement object) {
				return createRefinementAdapter();
			}
			@Override
			public Adapter caseParameter(Parameter object) {
				return createParameterAdapter();
			}
			@Override
			public Adapter caseDependency(Dependency object) {
				return createDependencyAdapter();
			}
			@Override
			public Adapter caseISetter(ISetter object) {
				return createISetterAdapter();
			}
			@Override
			public Adapter caseDelay(Delay object) {
				return createDelayAdapter();
			}
			@Override
			public Adapter caseExpression(Expression object) {
				return createExpressionAdapter();
			}
			@Override
			public Adapter caseHRefinement(HRefinement object) {
				return createHRefinementAdapter();
			}
			@Override
			public Adapter caseFunctionPrototype(FunctionPrototype object) {
				return createFunctionPrototypeAdapter();
			}
			@Override
			public Adapter caseFunctionParameter(FunctionParameter object) {
				return createFunctionParameterAdapter();
			}
			@Override
			public Adapter caseDataPort(DataPort object) {
				return createDataPortAdapter();
			}
			@Override
			public Adapter caseBroadcastActor(BroadcastActor object) {
				return createBroadcastActorAdapter();
			}
			@Override
			public Adapter caseJoinActor(JoinActor object) {
				return createJoinActorAdapter();
			}
			@Override
			public Adapter caseForkActor(ForkActor object) {
				return createForkActorAdapter();
			}
			@Override
			public Adapter caseRoundBufferActor(RoundBufferActor object) {
				return createRoundBufferActorAdapter();
			}
			@Override
			public Adapter caseExecutableActor(ExecutableActor object) {
				return createExecutableActorAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Parameterizable <em>Parameterizable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Parameterizable
	 * @generated
	 */
	public Adapter createParameterizableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.AbstractVertex <em>Abstract Vertex</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.AbstractVertex
	 * @generated
	 */
	public Adapter createAbstractVertexAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.AbstractActor <em>Abstract Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.AbstractActor
	 * @generated
	 */
	public Adapter createAbstractActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.PiGraph <em>Pi Graph</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.PiGraph
	 * @generated
	 */
	public Adapter createPiGraphAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Actor <em>Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Actor
	 * @generated
	 */
	public Adapter createActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Port <em>Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Port
	 * @generated
	 */
	public Adapter createPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.DataInputPort <em>Data Input Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.DataInputPort
	 * @generated
	 */
	public Adapter createDataInputPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.DataOutputPort <em>Data Output Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.DataOutputPort
	 * @generated
	 */
	public Adapter createDataOutputPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ConfigInputPort <em>Config Input Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ConfigInputPort
	 * @generated
	 */
	public Adapter createConfigInputPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ConfigOutputPort <em>Config Output Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ConfigOutputPort
	 * @generated
	 */
	public Adapter createConfigOutputPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Fifo <em>Fifo</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Fifo
	 * @generated
	 */
	public Adapter createFifoAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.InterfaceActor <em>Interface Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.InterfaceActor
	 * @generated
	 */
	public Adapter createInterfaceActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.DataInputInterface <em>Data Input Interface</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.DataInputInterface
	 * @generated
	 */
	public Adapter createDataInputInterfaceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.DataOutputInterface <em>Data Output Interface</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.DataOutputInterface
	 * @generated
	 */
	public Adapter createDataOutputInterfaceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface <em>Config Output Interface</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface
	 * @generated
	 */
	public Adapter createConfigOutputInterfaceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Refinement <em>Refinement</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Refinement
	 * @generated
	 */
	public Adapter createRefinementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Parameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Parameter
	 * @generated
	 */
	public Adapter createParameterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Dependency <em>Dependency</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Dependency
	 * @generated
	 */
	public Adapter createDependencyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ISetter <em>ISetter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ISetter
	 * @generated
	 */
	public Adapter createISetterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Delay <em>Delay</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Delay
	 * @generated
	 */
	public Adapter createDelayAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.Expression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.Expression
	 * @generated
	 */
	public Adapter createExpressionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.HRefinement <em>HRefinement</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.HRefinement
	 * @generated
	 */
	public Adapter createHRefinementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.FunctionPrototype <em>Function Prototype</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.FunctionPrototype
	 * @generated
	 */
	public Adapter createFunctionPrototypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.FunctionParameter <em>Function Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.FunctionParameter
	 * @generated
	 */
	public Adapter createFunctionParameterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.DataPort <em>Data Port</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.DataPort
	 * @generated
	 */
	public Adapter createDataPortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.BroadcastActor <em>Broadcast Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.BroadcastActor
	 * @generated
	 */
	public Adapter createBroadcastActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.JoinActor <em>Join Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.JoinActor
	 * @generated
	 */
	public Adapter createJoinActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ForkActor <em>Fork Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ForkActor
	 * @generated
	 */
	public Adapter createForkActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.RoundBufferActor <em>Round Buffer Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.RoundBufferActor
	 * @generated
	 */
	public Adapter createRoundBufferActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ExecutableActor <em>Executable Actor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ExecutableActor
	 * @generated
	 */
	public Adapter createExecutableActorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.ietr.preesm.experiment.model.pimm.ConfigInputInterface <em>Config Input Interface</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.ietr.preesm.experiment.model.pimm.ConfigInputInterface
	 * @generated
	 */
	public Adapter createConfigInputInterfaceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //PiMMAdapterFactory
