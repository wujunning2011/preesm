/**
 * Copyright or © or Copr. IETR/INSA - Rennes (2012 - 2017) :
 *
 * Antoine Morvan <antoine.morvan@insa-rennes.fr> (2017)
 * Clément Guy <clement.guy@insa-rennes.fr> (2014 - 2015)
 * Julien Heulot <julien.heulot@insa-rennes.fr> (2013)
 * Karol Desnos <karol.desnos@insa-rennes.fr> (2012 - 2014)
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
package org.ietr.preesm.experiment.model.pimm.serialize;

import java.io.InputStream;
import java.util.logging.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.ietr.dftools.algorithm.importer.InvalidModelException;
import org.ietr.dftools.architecture.utils.DomUtil;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.AbstractVertex;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputInterface;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataInputInterface;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputInterface;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Delay;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.Fifo;
import org.ietr.preesm.experiment.model.pimm.FunctionParameter;
import org.ietr.preesm.experiment.model.pimm.FunctionPrototype;
import org.ietr.preesm.experiment.model.pimm.HRefinement;
import org.ietr.preesm.experiment.model.pimm.ISetter;
import org.ietr.preesm.experiment.model.pimm.InterfaceActor;
import org.ietr.preesm.experiment.model.pimm.Parameter;
import org.ietr.preesm.experiment.model.pimm.Parameterizable;
import org.ietr.preesm.experiment.model.pimm.PiGraph;
import org.ietr.preesm.experiment.model.pimm.PiMMFactory;
import org.ietr.preesm.experiment.model.pimm.Port;
import org.ietr.preesm.experiment.model.pimm.PortMemoryAnnotation;
import org.ietr.preesm.experiment.model.pimm.util.PiIdentifiers;
import org.ietr.preesm.experiment.model.pimm.util.SubgraphConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * Parser for the PiMM Model in the Pi format.
 *
 * @author kdesnos
 * @author jheulot
 */
public class PiParser {

  /**
   * Gets the pi graph.
   *
   * @param algorithmURL
   *          URL of the Algorithm.
   * @return the {@link PiGraph} algorithm.
   * @throws InvalidModelException
   *           the invalid model exception
   * @throws CoreException
   *           the core exception
   */
  public static PiGraph getPiGraph(final String algorithmURL) throws InvalidModelException, CoreException {
    PiGraph pigraph = null;
    final ResourceSet resourceSet = new ResourceSetImpl();

    final URI uri = URI.createPlatformResourceURI(algorithmURL, true);
    if ((uri.fileExtension() == null) || !uri.fileExtension().contentEquals("pi")) {
      return null;
    }
    Resource ressource;
    try {
      ressource = resourceSet.getResource(uri, true);
      pigraph = (PiGraph) (ressource.getContents().get(0));

      final SubgraphConnector connector = new SubgraphConnector();
      connector.connectSubgraphs(pigraph);
    } catch (final WrappedException e) {
      WorkflowLogger.getLogger().log(Level.SEVERE, "The algorithm file \"" + uri + "\" specified by the scenario does not exist any more.");
    }

    return pigraph;
  }

  /**
   * Retrieve the value of a property of the given {@link Element}. A property is a data element child of the given element.<br>
   * <br>
   *
   * <p>
   * This method will iterate over the properties of the element so it might not be a good idea to use it in a method that would successively retrieve all
   * properties of the element.
   * </p>
   *
   * @author Jonathan Piat
   * @param elt
   *          The element containing the property
   * @param propertyName
   *          The name of the property
   * @return The property value or null if the property was not found
   */
  protected static String getProperty(final Element elt, final String propertyName) {
    final NodeList childList = elt.getChildNodes();
    for (int i = 0; i < childList.getLength(); i++) {
      if (childList.item(i).getNodeName().equals("data") && ((Element) childList.item(i)).getAttribute("key").equals(propertyName)) {
        return childList.item(i).getTextContent();
      }
    }
    return null;
  }

  /** The URI of the parsed file. */
  private final URI documentURI;

  /**
   * Instantiates a new pi parser.
   *
   * @param uri
   *          the uri
   */
  public PiParser(final URI uri) {
    this.documentURI = uri;
  }

  /**
   * Parse the PiMM {@link PiGraph} from the given {@link InputStream} using the Pi format.
   *
   * @param inputStream
   *          The Parsed input stream
   * @return The parsed Graph or null is something went wrong
   */
  public PiGraph parse(final InputStream inputStream) {
    // Instantiate the graph that will be filled with parser informations
    final PiGraph graph = PiMMFactory.eINSTANCE.createPiGraph();

    // Parse the input stream
    final Document document = DomUtil.parseDocument(inputStream);

    // Retrieve the root element
    final Element rootElt = document.getDocumentElement();

    try {
      // Fill the graph with parsed information
      parsePi(rootElt, graph);
    } catch (final RuntimeException e) {
      e.printStackTrace();
      return null;
    }

    return graph;
  }

  /**
   * Parse a node {@link Element} with kind "actor".
   *
   * @param nodeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   * @return the created actor
   */
  protected Actor parseActor(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new actor
    final Actor actor = PiMMFactory.eINSTANCE.createActor();

    // Get the actor properties
    actor.setName(nodeElt.getAttribute(PiIdentifiers.ACTOR_NAME));

    // Add the actor to the parsed graph
    graph.getVertices().add(actor);

    parseRefinement(nodeElt, actor);

    final String memoryScript = PiParser.getProperty(nodeElt, PiIdentifiers.ACTOR_MEMORY_SCRIPT);
    if ((memoryScript != null) && !memoryScript.isEmpty()) {
      final IPath path = getWorkspaceRelativePathFrom(new Path(memoryScript));
      actor.setMemoryScriptPath(path);
    }

    return actor;
  }

  /**
   * Parses the refinement.
   *
   * @param nodeElt
   *          the node elt
   * @param actor
   *          the actor
   */
  private void parseRefinement(final Element nodeElt, final Actor actor) {
    final String refinement = PiParser.getProperty(nodeElt, PiIdentifiers.REFINEMENT);
    if ((refinement != null) && !refinement.isEmpty()) {
      final IPath path = getWorkspaceRelativePathFrom(new Path(refinement));

      // If the refinement is a .h file, then we need to create a
      // HRefinement
      if (path.getFileExtension().equals("h")) {
        final HRefinement hrefinement = PiMMFactory.eINSTANCE.createHRefinement();
        // The nodeElt should have a loop element, and may have an init
        // element
        final NodeList childList = nodeElt.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
          final Node elt = childList.item(i);
          final String eltName = elt.getNodeName();
          Element elmt;
          switch (eltName) {
            case PiIdentifiers.REFINEMENT_LOOP:
              elmt = (Element) elt;
              hrefinement.setLoopPrototype(parseFunctionPrototype(elmt, elmt.getAttribute(PiIdentifiers.REFINEMENT_FUNCTION_PROTOTYPE_NAME)));
              break;
            case PiIdentifiers.REFINEMENT_INIT:
              elmt = (Element) elt;
              hrefinement.setInitPrototype(parseFunctionPrototype(elmt, elmt.getAttribute(PiIdentifiers.REFINEMENT_FUNCTION_PROTOTYPE_NAME)));
              break;
            default:
              // ignore #text and other children
          }
        }
        actor.setRefinement(hrefinement);
      }

      actor.getRefinement().setFilePath(path);
    }
  }

  /**
   * Parses the function prototype.
   *
   * @param protoElt
   *          the proto elt
   * @param protoName
   *          the proto name
   * @return the function prototype
   */
  private FunctionPrototype parseFunctionPrototype(final Element protoElt, final String protoName) {
    final FunctionPrototype proto = PiMMFactory.eINSTANCE.createFunctionPrototype();

    proto.setName(protoName);
    final NodeList childList = protoElt.getChildNodes();
    for (int i = 0; i < childList.getLength(); i++) {
      final Node elt = childList.item(i);
      final String eltName = elt.getNodeName();
      switch (eltName) {
        case PiIdentifiers.REFINEMENT_PARAMETER:
          proto.getParameters().add(parseFunctionParameter((Element) elt));
          break;
        default:
          // ignore #text and other children
      }
    }
    return proto;
  }

  /**
   * Parses the function parameter.
   *
   * @param elt
   *          the elt
   * @return the function parameter
   */
  private FunctionParameter parseFunctionParameter(final Element elt) {
    final FunctionParameter param = PiMMFactory.eINSTANCE.createFunctionParameter();

    param.setName(elt.getAttribute(PiIdentifiers.REFINEMENT_PARAMETER_NAME));
    param.setType(elt.getAttribute(PiIdentifiers.REFINEMENT_PARAMETER_TYPE));
    param.setDirection(PiMMFactory.eINSTANCE.createDirection(elt.getAttribute(PiIdentifiers.REFINEMENT_PARAMETER_DIRECTION)));
    param.setIsConfigurationParameter(Boolean.valueOf(elt.getAttribute(PiIdentifiers.REFINEMENT_PARAMETER_IS_CONFIG)));

    return param;
  }

  /**
   * Parse a ConfigInputInterface (i.e. a {@link Parameter}) of the Pi File.
   *
   * @param nodeElt
   *          The node {@link Element} holding the {@link Parameter} properties.
   * @param graph
   *          the deserialized {@link PiGraph}.
   * @return the {@link AbstractVertex} of the {@link Parameter}.
   */
  protected AbstractVertex parseConfigInputInterface(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new Config Input Interface
    final Parameter param = PiMMFactory.eINSTANCE.createParameter();
    param.setConfigurationInterface(true);
    // param.setLocallyStatic(true);

    // Get the actor properties
    param.setName(nodeElt.getAttribute(PiIdentifiers.PARAMETER_NAME));

    // Add the actor to the parsed graph
    graph.getParameters().add(param);

    return param;
  }

  /**
   * Parse a node {@link Element} with kind "dependency".
   *
   * @param edgeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   */
  protected void parseDependencies(final Element edgeElt, final PiGraph graph) {
    // Instantiate the new Dependency
    final Dependency dependency = PiMMFactory.eINSTANCE.createDependency();

    // Find the source and target of the fifo
    final String setterName = edgeElt.getAttribute(PiIdentifiers.DEPENDENCY_SOURCE);
    final String getterName = edgeElt.getAttribute(PiIdentifiers.DEPENDENCY_TARGET);
    final AbstractVertex source = graph.getVertexNamed(setterName);
    Parameterizable target = graph.getVertexNamed(getterName);
    if (source == null) {
      throw new RuntimeException("Dependency source vertex " + setterName + " does not exist.");
    }
    if (target == null) {
      // The target can also be a Delay associated to a Fifo
      final Fifo targetFifo = graph.getFifoIded(getterName);

      if (targetFifo == null) {
        throw new RuntimeException("Dependency target " + getterName + " does not exist.");
      }

      if (targetFifo.getDelay() == null) {
        throw new RuntimeException("Dependency fifo target " + getterName + " has no delay to receive the dependency.");
      } else {
        target = targetFifo.getDelay();
      }
    }

    // Get the sourcePort and targetPort
    if (source instanceof ExecutableActor) {

      String sourcePortName = edgeElt.getAttribute(PiIdentifiers.DEPENDENCY_SOURCE_PORT);
      sourcePortName = (sourcePortName.isEmpty()) ? null : sourcePortName;
      final ConfigOutputPort oPort = (ConfigOutputPort) ((ExecutableActor) source).getPortNamed(sourcePortName);
      if (oPort == null) {
        throw new RuntimeException("Edge source port " + sourcePortName + " does not exist for vertex " + setterName);
      }
      dependency.setSetter(oPort);
    }
    if (source instanceof Parameter) {
      dependency.setSetter((ISetter) source);
    }

    if (target instanceof ExecutableActor) {
      String targetPortName = edgeElt.getAttribute(PiIdentifiers.DEPENDENCY_TARGET_PORT);
      targetPortName = (targetPortName.isEmpty()) ? null : targetPortName;
      final ConfigInputPort iPort = (ConfigInputPort) ((AbstractVertex) target).getPortNamed(targetPortName);
      if (iPort == null) {
        throw new RuntimeException("Dependency target port " + targetPortName + " does not exist for vertex " + getterName);
      }
      dependency.setGetter(iPort);
    }

    if ((target instanceof Parameter) || (target instanceof InterfaceActor) || (target instanceof Delay)) {
      final ConfigInputPort iCfgPort = PiMMFactory.eINSTANCE.createConfigInputPort();
      target.getConfigInputPorts().add(iCfgPort);
      dependency.setGetter(iCfgPort);
    }

    if ((dependency.getGetter() == null) || (dependency.getSetter() == null)) {
      throw new RuntimeException("There was a problem parsing the following dependency: " + setterName + "=>" + getterName);
    }

    // Add the new dependency to the graph
    graph.getDependencies().add(dependency);
  }

  /**
   * Parse an edge {@link Element} of the Pi description. An edge {@link Element} can be a parameter dependency or a FIFO of the parsed graph.
   *
   * @param edgeElt
   *          The edge {@link Element} to parse
   * @param graph
   *          The deserialized graph
   */
  protected void parseEdge(final Element edgeElt, final PiGraph graph) {
    // Identify if the node is an actor or a parameter
    final String edgeKind = edgeElt.getAttribute(PiIdentifiers.EDGE_KIND);

    switch (edgeKind) {
      case PiIdentifiers.FIFO:
        parseFifo(edgeElt, graph);
        break;
      case PiIdentifiers.DEPENDENCY:
        parseDependencies(edgeElt, graph);
        break;
      default:
        throw new RuntimeException("Parsed edge has an unknown kind: " + edgeKind);
    }
  }

  /**
   * Parse a node {@link Element} with kind "fifo".
   *
   * @param edgeElt
   *          the edge elt
   * @param graph
   *          the deserialized {@link PiGraph}
   */
  protected void parseFifo(final Element edgeElt, final PiGraph graph) {
    // Instantiate the new Fifo
    final Fifo fifo = PiMMFactory.eINSTANCE.createFifo();

    // Find the source and target of the fifo
    final String sourceName = edgeElt.getAttribute(PiIdentifiers.FIFO_SOURCE);
    final String targetName = edgeElt.getAttribute(PiIdentifiers.FIFO_TARGET);
    final AbstractActor source = (AbstractActor) graph.getVertexNamed(sourceName);
    final AbstractActor target = (AbstractActor) graph.getVertexNamed(targetName);
    if (source == null) {
      throw new RuntimeException("Edge source vertex " + sourceName + " does not exist.");
    }
    if (target == null) {
      throw new RuntimeException("Edge target vertex " + sourceName + " does not exist.");
    }
    // Get the type
    String type = edgeElt.getAttribute(PiIdentifiers.FIFO_TYPE);
    // If none is find, add the default type
    if ((type == null) || type.equals("")) {
      type = "void";
    }
    fifo.setType(type);
    // Get the sourcePort and targetPort
    String sourcePortName = edgeElt.getAttribute(PiIdentifiers.FIFO_SOURCE_PORT);
    sourcePortName = (sourcePortName.isEmpty()) ? null : sourcePortName;
    String targetPortName = edgeElt.getAttribute(PiIdentifiers.FIFO_TARGET_PORT);
    targetPortName = (targetPortName.isEmpty()) ? null : targetPortName;
    final DataOutputPort oPort = (DataOutputPort) source.getPortNamed(sourcePortName);
    final DataInputPort iPort = (DataInputPort) target.getPortNamed(targetPortName);

    if (iPort == null) {
      throw new RuntimeException("Edge target port " + targetPortName + " does not exist for vertex " + targetName);
    }
    if (oPort == null) {
      throw new RuntimeException("Edge source port " + sourcePortName + " does not exist for vertex " + sourceName);
    }

    fifo.setSourcePort(oPort);
    fifo.setTargetPort(iPort);

    // Check if the fifo has a delay
    if (PiParser.getProperty(edgeElt, PiIdentifiers.DELAY) != null) {
      // TODO replace with a parse Delay if delay have their own element
      // in the future
      final Delay delay = PiMMFactory.eINSTANCE.createDelay();
      delay.getExpression().setString(edgeElt.getAttribute(PiIdentifiers.DELAY_EXPRESSION));
      fifo.setDelay(delay);
    }

    // Add the new Fifo to the graph
    graph.getFifos().add(fifo);
  }

  /**
   * Retrieve and parse the graph element of the Pi description.
   *
   * @param rootElt
   *          The root element (that must have a graph child)
   * @param graph
   *          The deserialized {@link PiGraph}
   */
  protected void parseGraph(final Element rootElt, final PiGraph graph) {
    // Retrieve the Graph Element
    final NodeList graphElts = rootElt.getElementsByTagName(PiIdentifiers.GRAPH);
    if (graphElts.getLength() == 0) {
      throw new RuntimeException("No graph was found in the parsed document");
    }
    if (graphElts.getLength() > 1) {
      throw new RuntimeException("More than one graph was found in the parsed document");
    }
    // If this code is reached, a unique graph element was found in the
    // document
    final Element graphElt = (Element) graphElts.item(0);

    // TODO parseGraphProperties() of the graph

    // Parse the elements of the graph
    final NodeList childList = graphElt.getChildNodes();
    for (int i = 0; i < childList.getLength(); i++) {
      final Node elt = childList.item(i);

      final String eltName = elt.getNodeName();

      switch (eltName) {
        case PiIdentifiers.DATA:
          // Properties of the Graph.
          // TODO transfer this code in a separate function
          // parseGraphProperties()
          final String keyName = elt.getAttributes().getNamedItem(PiIdentifiers.DATA_KEY).getNodeValue();
          final String keyValue = elt.getTextContent();
          if (keyName.equals(PiIdentifiers.GRAPH_NAME)) {
            graph.setName(keyValue);
          }
          break;
        case PiIdentifiers.NODE:
          // Node elements
          parseNode((Element) elt, graph);
          break;
        case PiIdentifiers.EDGE:
          // Edge elements
          parseEdge((Element) elt, graph);
          break;
        default:

      }
    }
  }

  /**
   * Parse a node {@link Element} of the Pi description. A node {@link Element} can be a parameter or an vertex of the parsed graph.
   *
   * @param nodeElt
   *          The node {@link Element} to parse
   * @param graph
   *          The deserialized {@link PiGraph}
   */
  protected void parseNode(final Element nodeElt, final PiGraph graph) {
    // Identify if the node is an actor or a parameter
    final String nodeKind = nodeElt.getAttribute(PiIdentifiers.NODE_KIND);
    AbstractVertex vertex;

    switch (nodeKind) {
      case PiIdentifiers.ACTOR:
        vertex = parseActor(nodeElt, graph);
        break;
      case PiIdentifiers.BROADCAST:
      case PiIdentifiers.FORK:
      case PiIdentifiers.JOIN:
      case PiIdentifiers.ROUND_BUFFER:
        vertex = parseSpecialActor(nodeElt, graph);
        break;
      case PiIdentifiers.DATA_INPUT_INTERFACE:
        vertex = parseSourceInterface(nodeElt, graph);
        break;
      case PiIdentifiers.DATA_OUTPUT_INTERFACE:
        vertex = parseSinkInterface(nodeElt, graph);
        break;
      case PiIdentifiers.PARAMETER:
        vertex = parseParameter(nodeElt, graph);
        break;
      case PiIdentifiers.CONFIGURATION_INPUT_INTERFACE:
        vertex = parseConfigInputInterface(nodeElt, graph);
        break;
      case PiIdentifiers.CONFIGURATION_OUTPUT_INTERFACE:
        vertex = parseConfigOutputInterface(nodeElt, graph);
        break;
      // TODO Parse all types of nodes
      // case "implode":
      // break;
      // case "explode":
      // break;
      // case "parameter":
      // break;

      default:
        throw new RuntimeException("Parsed node " + nodeElt.getNodeName() + " has an unknown kind: " + nodeKind);
    }

    // Parse the elements of the node
    final NodeList childList = nodeElt.getChildNodes();
    for (int i = 0; i < childList.getLength(); i++) {
      final Node elt = childList.item(i);
      final String eltName = elt.getNodeName();

      switch (eltName) {
        case PiIdentifiers.PORT:
          parsePort((Element) elt, vertex);
          break;
        default:
          // ignore #text and unknown children
      }
    }
  }

  /**
   * Parse a {@link Parameter} of the Pi File.
   *
   * @param nodeElt
   *          The node {@link Element} holding the {@link Parameter} properties.
   * @param graph
   *          the deserialized {@link PiGraph}.
   * @return the {@link AbstractVertex} of the {@link Parameter}.
   */
  protected AbstractVertex parseParameter(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new Parameter
    final Parameter param = PiMMFactory.eINSTANCE.createParameter();
    param.getExpression().setString(nodeElt.getAttribute(PiIdentifiers.PARAMETER_EXPRESSION));
    param.setConfigurationInterface(false);
    // param.setLocallyStatic(true);
    param.setGraphPort(null); // No port of the graph corresponds to this
    // parameter

    // Get the actor properties
    param.setName(nodeElt.getAttribute(PiIdentifiers.PARAMETER_NAME));

    // Add the actor to the parsed graph
    graph.getParameters().add(param);

    return param;
  }

  /**
   * Parse the root element of the Pi description.
   *
   * @param rootElt
   *          the root elt
   * @param graph
   *          The deserialized {@link PiGraph}
   */
  protected void parsePi(final Element rootElt, final PiGraph graph) {
    // TODO parseKeys() (Not sure if it is really necessary to do that)

    // Parse the graph element
    parseGraph(rootElt, graph);

  }

  /**
   * Parse a {@link Port} of the Pi file.
   *
   * @param elt
   *          the {@link Element} holding the {@link Port} properties.
   * @param vertex
   *          the {@link AbstractVertex} owning this {@link Port}
   */
  protected void parsePort(final Element elt, final AbstractVertex vertex) {
    final String portName = elt.getAttribute(PiIdentifiers.PORT_NAME);
    final String portKind = elt.getAttribute(PiIdentifiers.PORT_KIND);

    switch (portKind) {
      case PiIdentifiers.DATA_INPUT_PORT:
        // Throw an error if the parsed vertex is not an actor
        if (!(vertex instanceof AbstractActor)) {
          throw new RuntimeException("Parsed data port " + portName + " cannot belong to the non-actor vertex " + vertex.getName());
        }

        DataInputPort iPort;

        // Do not create data ports for InterfaceActor since the unique port
        // is automatically created when the vertex is instantiated
        if (!(vertex instanceof InterfaceActor)) {
          iPort = PiMMFactory.eINSTANCE.createDataInputPort();
          ((AbstractActor) vertex).getDataInputPorts().add(iPort);
          iPort.setName(portName);
        } else {
          iPort = ((AbstractActor) vertex).getDataInputPorts().get(0);
        }
        iPort.getExpression().setString(elt.getAttribute(PiIdentifiers.PORT_EXPRESSION));
        iPort.setAnnotation(PortMemoryAnnotation.get(elt.getAttribute(PiIdentifiers.PORT_MEMORY_ANNOTATION)));
        break;
      case PiIdentifiers.DATA_OUTPUT_PORT:
        // Throw an error if the parsed vertex is not an actor
        if (!(vertex instanceof AbstractActor)) {
          throw new RuntimeException("Parsed data port " + portName + " cannot belong to the non-actor vertex " + vertex.getName());
        }

        DataOutputPort oPort;

        // Do not create data ports for InterfaceActor since the unique port
        // is automatically created when the vertex is instantiated
        if (!(vertex instanceof InterfaceActor)) {
          oPort = PiMMFactory.eINSTANCE.createDataOutputPort();
          ((AbstractActor) vertex).getDataOutputPorts().add(oPort);
          oPort.setName(portName);
        } else {
          oPort = ((AbstractActor) vertex).getDataOutputPorts().get(0);
        }
        oPort.getExpression().setString(elt.getAttribute(PiIdentifiers.PORT_EXPRESSION));
        oPort.setAnnotation(PortMemoryAnnotation.get(elt.getAttribute(PiIdentifiers.PORT_MEMORY_ANNOTATION)));
        break;
      case PiIdentifiers.CONFIGURATION_INPUT_PORT:
        final ConfigInputPort iCfgPort = PiMMFactory.eINSTANCE.createConfigInputPort();
        iCfgPort.setName(portName);
        vertex.getConfigInputPorts().add(iCfgPort);
        break;

      case PiIdentifiers.CONFIGURATION_OUPUT_PORT:
        // Throw an error if the parsed vertex is not an actor
        if (!(vertex instanceof AbstractActor)) {
          throw new RuntimeException("Parsed config. port " + portName + " cannot belong to the non-actor vertex " + vertex.getName());
        }
        final ConfigOutputPort oCfgPort = PiMMFactory.eINSTANCE.createConfigOutputPort();
        oCfgPort.setName(portName);
        ((AbstractActor) vertex).getConfigOutputPorts().add(oCfgPort);
        break;
      default:
        throw new RuntimeException("Parsed port " + portName + " has children of unknown kind: " + portKind);
    }
  }

  /**
   * Parse a node {@link Element} with kind "cfg_out_iface".
   *
   * @param nodeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   * @return the created {@link ConfigOutputInterface}
   */
  protected AbstractActor parseConfigOutputInterface(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new Interface and its corresponding port
    final ConfigOutputInterface cfgOutIf = PiMMFactory.eINSTANCE.createConfigOutputInterface();

    // Set the Interface properties
    cfgOutIf.setName(nodeElt.getAttribute(PiIdentifiers.CONFIGURATION_OUTPUT_INTERFACE_NAME));

    // Add the actor to the parsed graph
    graph.getVertices().add(cfgOutIf);

    return cfgOutIf;
  }

  /**
   * Parse a node {@link Element} with kind "snk".
   *
   * @param nodeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   * @return the created {@link DataOutputInterface}
   */
  protected AbstractActor parseSinkInterface(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new Interface and its corresponding port
    final DataOutputInterface snkInterface = PiMMFactory.eINSTANCE.createDataOutputInterface();

    // Set the sourceInterface properties
    snkInterface.setName(nodeElt.getAttribute(PiIdentifiers.DATA_OUTPUT_INTERFACE_NAME));

    // Add the actor to the parsed graph
    graph.getVertices().add(snkInterface);

    return snkInterface;
  }

  /**
   * Parse a node {@link Element} with kind "src".
   *
   * @param nodeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   * @return the created {@link DataInputInterface}
   */
  protected AbstractActor parseSourceInterface(final Element nodeElt, final PiGraph graph) {
    // Instantiate the new Interface and its corresponding port
    final DataInputInterface srcInterface = PiMMFactory.eINSTANCE.createDataInputInterface();

    // Set the sourceInterface properties
    srcInterface.setName(nodeElt.getAttribute(PiIdentifiers.DATA_INPUT_INTERFACE_NAME));

    // Add the actor to the parsed graph
    graph.getVertices().add(srcInterface);

    return srcInterface;
  }

  /**
   * Parse a node {@link Element} with kind "broadcast", "fork", "join", "roundbuffer".
   *
   * @param nodeElt
   *          the {@link Element} to parse
   * @param graph
   *          the deserialized {@link PiGraph}
   * @return the created actor
   */
  protected AbstractActor parseSpecialActor(final Element nodeElt, final PiGraph graph) {
    // Identify if the node is an actor or a parameter
    final String nodeKind = nodeElt.getAttribute(PiIdentifiers.NODE_KIND);
    AbstractActor actor = null;

    // Instantiate the actor.
    switch (nodeKind) {
      case PiIdentifiers.BROADCAST:
        actor = PiMMFactory.eINSTANCE.createBroadcastActor();
        break;
      case PiIdentifiers.FORK:
        actor = PiMMFactory.eINSTANCE.createForkActor();
        break;
      case PiIdentifiers.JOIN:
        actor = PiMMFactory.eINSTANCE.createJoinActor();
        break;
      case PiIdentifiers.ROUND_BUFFER:
        actor = PiMMFactory.eINSTANCE.createRoundBufferActor();
        break;
      default:
        throw new IllegalArgumentException("Given node element has an unknown kind");
    }

    // Get the actor properties
    actor.setName(nodeElt.getAttribute(PiIdentifiers.ACTOR_NAME));

    // Add the actor to the parsed graph
    graph.getVertices().add(actor);

    return actor;
  }

  /**
   * Transform a project relative path to workspace relative path.
   *
   * @param path
   *          the IPath to transform
   * @return the path to the file inside the project containing the parsed file if this file exists, path otherwise
   */
  private IPath getWorkspaceRelativePathFrom(final IPath path) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    // If the file pointed by path does not exist, we try to add the
    // name of the project containing the file we parse to it
    if (!root.getFile(path).exists()) {
      // Get the project
      final String platformString = this.documentURI.toPlatformString(true);
      final IFile documentFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString));
      final IProject documentProject = documentFile.getProject();
      // Create a new path using the project name
      final IPath newPath = new Path(documentProject.getName()).append(path);
      // Check there is a file where newPath points, if yes, use it
      // instead of path
      if (root.getFile(newPath).exists()) {
        return newPath;
      }
    }
    return path;
  }
}
