/**
 * 
 */
package org.ietr.preesm.core.architecture;

import org.ietr.preesm.core.architecture.advancedmodel.Fifo;
import org.jgrapht.EdgeFactory;

/**
 * @author mpelcat
 *
 */
public class InterconnectionFactory implements EdgeFactory<ArchitectureComponent, Interconnection> {

	boolean isDirected = false;
	
	public void isDirected(boolean isDirected){
		this.isDirected = isDirected;
	}
	
	@Override
	public Interconnection createEdge(ArchitectureComponent cmp1,
			ArchitectureComponent cmp2) {
		return new Interconnection(cmp1, cmp2);
	}

}