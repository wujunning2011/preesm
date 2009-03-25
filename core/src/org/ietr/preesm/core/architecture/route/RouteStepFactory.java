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

package org.ietr.preesm.core.architecture.route;

import java.util.List;

import org.ietr.preesm.core.architecture.Interconnection;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.simplemodel.AbstractNode;
import org.ietr.preesm.core.architecture.simplemodel.Dma;
import org.ietr.preesm.core.architecture.simplemodel.Medium;
import org.ietr.preesm.core.architecture.simplemodel.Operator;

/**
 * Depending on the architecture nodes separating two operators, generates a
 * suited route step. The route steps represents one type of connection
 * between two connected operators
 * 
 * @author mpelcat
 */
public class RouteStepFactory {

	private MultiCoreArchitecture archi = null;

	public RouteStepFactory(MultiCoreArchitecture archi) {
		super();
		this.archi = archi;
	}

	/**
	 * Generates the suited route steps from intermediate nodes
	 */
	public AbstractRouteStep getRouteStep(Operator source,
			List<AbstractNode> nodes, Operator target) {
		AbstractRouteStep step = null;

		if (nodes.size() == 1 && nodes.get(0) instanceof Medium) {
			return new MediumRouteStep(source, (Medium) nodes.get(0), target);
		} else {
			Dma dma = getDma(nodes, source);
			if (dma != null) {
				step = new DmaRouteStep(source, nodes, target, dma);
			} else {
				step = new NodeRouteStep(source, nodes, target);
			}
		}

		return step;
	}

	/**
	 * Gets the dma corresponding to the step if any exists.
	 * The Dma must have a setup link with the source.
	 */
	private Dma getDma(List<AbstractNode> nodes, Operator dmaSetup) {
		Dma dma = null;
		for (AbstractNode node : nodes) {
			for (Interconnection i : archi.undirectedEdgesOf(node)) {
				if (i.getSource() instanceof Dma)
					dma = (Dma) i.getSource();
				if (i.getTarget() instanceof Dma)
					dma = (Dma) i.getTarget();

				if (dma != null) {
					if (existSetup(dma, dmaSetup)) {
						return dma;
					}
				}
			}
		}
		return null;
	}


	/**
	 * Checks if a setup link exists between dma and operator
	 */
	private boolean existSetup(Dma dma, Operator op) {

		for (Interconnection i : archi.incomingEdgesOf(dma)) {
			if (i.getSource().equals(op)) {
				return true;
			}
		}

		return false;
	}
}