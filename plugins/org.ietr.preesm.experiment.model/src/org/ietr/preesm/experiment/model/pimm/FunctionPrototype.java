/**
 */
package org.ietr.preesm.experiment.model.pimm;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.ietr.preesm.experiment.model.pimm.util.PiMMVisitable;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Function Prototype</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.FunctionPrototype#getName <em>Name</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.FunctionPrototype#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getFunctionPrototype()
 * @model
 * @generated
 */
public interface FunctionPrototype extends EObject, PiMMVisitable {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getFunctionPrototype_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.ietr.preesm.experiment.model.pimm.FunctionPrototype#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' reference list.
	 * The list contents are of type {@link org.ietr.preesm.experiment.model.pimm.FunctionParameter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameters</em>' reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getFunctionPrototype_Parameters()
	 * @model
	 * @generated
	 */
	EList<FunctionParameter> getParameters();
	
	/**
	 * 
	 * @return a String formatted as follows: name (parameters types)
	 */
	String format();

} // FunctionPrototype