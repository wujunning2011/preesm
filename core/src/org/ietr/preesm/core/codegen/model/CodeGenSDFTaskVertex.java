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
 
package org.ietr.preesm.core.codegen.model;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.codegen.ImplementationPropertyNames;
import org.ietr.preesm.core.codegen.VertexType;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFVertex;

public class CodeGenSDFTaskVertex extends SDFVertex implements ICodeGenSDFVertex{

	public static final String OPERATOR = ImplementationPropertyNames.Vertex_Operator; 
	public static final String NB_REPEAT ="nb_repeat";
	public static final String POS =ImplementationPropertyNames.Vertex_schedulingOrder;
	public static final String TYPE =ImplementationPropertyNames.Vertex_vertexType;
	
	public CodeGenSDFTaskVertex(){
		this.getPropertyBean().setValue(TYPE,  VertexType.task);
	}
	
	
	public ArchitectureComponent getOperator(){
		return (ArchitectureComponent) this.getPropertyBean().getValue(OPERATOR, ArchitectureComponent.class);
	}
	
	public void setOperator(ArchitectureComponent op){
		this.getPropertyBean().setValue(OPERATOR, getOperator(), op);
	}
	
	public int getPos(){
		if(this.getPropertyBean().getValue(POS) != null){
			return (Integer) this.getPropertyBean().getValue(POS, Integer.class);
		}
		return 0 ;
	}
	
	public void setPos(int pos){
		this.getPropertyBean().setValue(POS, getPos(), pos);
	}
	
	public int getNbRepeat(){
		return (Integer) this.getPropertyBean().getValue(NB_REPEAT, Integer.class);
	}
	
	public void setNbRepeat(int nb){
		this.getPropertyBean().setValue(NB_REPEAT, nb);
	}
	
	
	public String toString(){
		String code = new String();
		if(getNbRepeat() > 1){
			code +="for(ind = 0 ; ind <"+getNbRepeat()+" ; ind ++){\n";
			if(this.getGraphDescription() == null){
				code += this.getName()+"(";
				for(SDFEdge edge : this.getBase().incomingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				for(SDFEdge edge : this.getBase().outgoingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				code = code.substring(0,code.length()-1);
				code +=");\n";
			}else{
				code +=this.getGraphDescription().toString();
				code +=";\n";
			}
			code +="}\n";
		}else{
			if(this.getGraphDescription() == null && this.getRefinement() instanceof FunctionCall){
				code += ((FunctionCall) this.getRefinement()).getFunctionName()+"(";
				for(SDFEdge edge : this.getBase().incomingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				for(SDFEdge edge : this.getBase().outgoingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				code = code.substring(0,code.length()-1);
				code +=");\n";
			}else{
				code +=this.getGraphDescription().toString();
			}
		}
		
		return code ;
	}
}