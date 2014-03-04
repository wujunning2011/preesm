package org.ietr.preesm.experiment.memory

import bsh.EvalError
import bsh.Interpreter
import bsh.ParseException
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import java.util.logging.Level
import java.util.logging.Logger
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Path
import org.eclipse.xtext.xbase.lib.Pair
import org.ietr.dftools.algorithm.model.dag.DAGEdge
import org.ietr.dftools.algorithm.model.dag.DAGVertex
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph
import org.ietr.dftools.algorithm.model.dag.edag.DAGBroadcastVertex
import org.ietr.dftools.algorithm.model.dag.edag.DAGForkVertex
import org.ietr.dftools.algorithm.model.dag.edag.DAGJoinVertex
import org.ietr.dftools.algorithm.model.parameters.Argument
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex
import org.ietr.dftools.algorithm.model.sdf.SDFEdge
import org.ietr.dftools.algorithm.model.sdf.SDFGraph
import org.ietr.dftools.algorithm.model.sdf.SDFVertex
import org.ietr.dftools.algorithm.model.sdf.esdf.SDFRoundBufferVertex
import org.ietr.dftools.workflow.tools.WorkflowLogger
import org.ietr.preesm.core.types.DataType

import static extension org.ietr.preesm.experiment.memory.Buffer.*
import static extension org.ietr.preesm.experiment.memory.Range.*

enum CheckPolicy {
	NONE,
	FAST,
	THOROUGH
}

class ScriptRunner {

	/**
	 * Helper method to get the incoming {@link SDFEdge}s of an {@link 
	 * SDFAbstractVertex}.
	 */
	def static Set<SDFEdge> incomingEdges(SDFAbstractVertex vertex) {
		vertex.base.incomingEdgesOf(vertex)
	}

	/**
	 * Helper method to get the outgoing {@link SDFEdge}s of an {@link 
	 * SDFAbstractVertex}.
	 */
	def static Set<SDFEdge> outgoingEdges(SDFAbstractVertex vertex) {
		vertex.base.outgoingEdgesOf(vertex)
	}

	@Property
	var checkPolicy = CheckPolicy::NONE

	/**
	 * A {@link Map} that associates each {@link String} representing a type name 
	 * with a corresponding {@link DataType}. 
	 */
	@Property
	var Map<String, DataType> dataTypes

	/**
	 * A {@link Map} that associates each {@link DAGVertex} from the
	 * {@link #scriptedVertices} map to the result of the successful 
	 * execution of its script. The result is stored as a {@link Pair}
	 * of {@link List} of {@link Buffer}. The first {@link List} contains
	 * the input {@link Buffer buffers} and the second contains output
     * {@link Buffer buffers}.
	 */
	val scriptResults = new HashMap<DAGVertex, Pair<List<Buffer>, List<Buffer>>>

	/** 
	 * A {@link Map} that associates each {@link DAGVertex} with a
	 * memory script to this memory script {@link File}.
	 */
	val scriptedVertices = new HashMap<DAGVertex, File>();

	static public final boolean printTodo = false

	/**
	 * Check the results obtained when running the {@link #run()} method.
	 * Checks are performed according to the current {@link #setCheckPolicy(CheckPolicy)}.
	 * The {@link #checkResult(File,Pair)} method is used to perform the checks.
	 * Vertices whose script results do not pass the checks are removed
	 * from the {@link #scriptResults} map.
	 */
	def check() {
		if (checkPolicy != CheckPolicy::NONE) {

			// Do the checks
			val invalidVertices = newArrayList
			scriptResults.forEach[vertex, result|
				if(!checkResult(scriptedVertices.get(vertex), result)) invalidVertices.add(vertex)]

			// Remove invalid results
			invalidVertices.forEach[scriptResults.remove(it)]
		}
	}

	/**
	 * This method perform several checks on the {@link Buffer buffers}
	 * resulting from the evaluation of a script. The checked points are:
	 * <ul>
	 * <li>If all {@link Match matches} are reciprocal. A {@link Match}
	 * belonging to the {@link Buffer#getMatchTable() matchTable} of a
	 * {@link Buffer} is reciprocal if the {@link Match#getBuffer() matched
	 * buffer} has a reciprocal {@link Match} in its
	 * {@link Buffer#getMatchTable() match table}.</li>
	 * <li>If there are inter-siblings matches. (i.e. inter-inputs or inter-
	 * outputs matches.)</li>
	 * <li>If ranges matched multiple times are not matched with other ranges
	 * that are {@link #getMultipleMatchRange(Buffer) matched multiple times}.
	 * For example, with a,b and c three {@link Buffer buffers}, if a[i] is
	 * matched with b[j], b[k], and c[l] then b[j] (or b[k] or c[l]) cannot be
	 * matched with a {@link Buffer} different than a[i].</li>
	 * </ul>
	 * If one of the checks is not valid, the method will return false and a
	 * warning will be displayed in the {@link Logger log}.
	 * 
	 * @param script
	 *            the script {@link File} from which the result {@link Buffer
	 *            buffers} result.
	 * @param result
	 *            a {@link Pair} of {@link List} of {@link Buffer buffers}. The
	 *            key {@link List} contains input {@link Buffer buffers} and the
	 *            value {@link List} contain output {@link Buffer buffers}.
	 * @return <code>true</code> if all checks were valid, <code>false</code>
	 *         otherwise.
	 */
	def checkResult(File script, Pair<List<Buffer>, List<Buffer>> result) {
		val allBuffers = new ArrayList<Buffer>
		allBuffers.addAll(result.key)
		allBuffers.addAll(result.value)

		// Check that all matches are reciprocal
		// For all buffers
		val res1 = allBuffers.forall [ localBuffer |
			// for all matcheSet  
			localBuffer.reciprocal
		]
		if (!res1) {
			val logger = WorkflowLogger.logger
			logger.log(Level.WARNING,
				"Error in " + script + ":\nOne or more match is not reciprocal." +
					" Please set matches only by using Buffer.matchWith() methods.")
		}
		
		// Find inter-inputs and inter-outputs matches
		var res2 = result.key.forall[ buffer |
			buffer.matchTable.values.flatten.forall[ match |
				result.value.contains(match.remoteBuffer)
			]	
		] && result.value.forall[ buffer |
			buffer.matchTable.values.flatten.forall[ match |
				result.key.contains(match.remoteBuffer)
			]	
		] 
		if (!res2) {
			val logger = WorkflowLogger.logger
			logger.log(Level.WARNING,
				"Error in " + script + ":\nOne or more match links an input (or an output) to another." +
					" Please set matches only between inputs and outputs.")
		}
		

		// Find ranges from input and output with multiple matches
		// Check: If a[i] is matched with b[j], b[k], and c[l] then b[j] (or 
		// b[k] or c[l]) cannot be matched with a buffer different than a[i].
		// forall inputs -> forall elements -> forall multiple matches
		// check that other side of the match has a unique match (implicitly: 
		// with the current multiple match).
		val multipleRanges = allBuffers.map[it -> it.multipleMatchRange]
		val res3 = multipleRanges.forall [ multipleRange |
			if (multipleRange.value.size != 0) {

				// If the current buffer has multiple ranges
				multipleRange.value.forall [ r |
					// Fill a list that contains Pair
					// Each pair is composed of a remote buffer and a remote range
					// This range is matched with a multiple matched range from
					// the current local buffer. Consequently, this remote 
					// range must not be part of a remote multiple matched range.
					val matchFromMultRange = newArrayList;
					multipleRange.key.matchTable.forEach [ localIdx, matches |
						matches.forEach [
							if (r.hasOverlap(it.localRange)) {
								val localInter = r.intersection(it.localRange)
								val remoteRangeStart = it.remoteIndex + localInter.start - localIdx
								val remoteRange = new Range(remoteRangeStart, remoteRangeStart + localInter.length)
								matchFromMultRange.add(it.remoteBuffer -> remoteRange)
							}
						]
					]
					matchFromMultRange.forall [
						val buffer = it.key
						val range = it.value
						val intersect = multipleRanges.get(allBuffers.indexOf(buffer)).value.intersection(range)
						intersect.size == 0
					]
				]
			} else
				true // No multiple range for this buffer
		]

		if (!res3) {
			val logger = WorkflowLogger.logger
			logger.log(Level.WARNING,
				"Error in " + script + ":\nA buffer element matched multiple times cannot" +
					" be matched with an element that is itself matched multiple times.")
		}
		res1 && res2 && res3
	}

	/**
	 * This method finds the memory scripts associated to the {@link DAGVertex
	 * vertices} of the input {@link DirectedAcyclicGraph}. When a script path
	 * is set in the property of the {@link SDFVertex} associated to a
	 * {@link DAGVertex} of the graph, scripts are either found in a path
	 * relative to the original {@link SDFGraph} file, or in the plugin project
	 * "scripts" directory. If an invalid script path is set, a warning message
	 * will be written in the log.
	 * 
	 * @param dag
	 *            the {@link DirectedAcyclicGraph} whose vertices memory scripts
	 *            are retrieved.
	 */
	def protected findScripts(DirectedAcyclicGraph dag) {

		// Retrieve the original sdf folder
		val workspace = ResourcesPlugin.getWorkspace
		val sdfPath = dag.propertyBean.getValue(DirectedAcyclicGraph.PATH, String) as String
		var sdfFile = workspace.root.getFileForLocation(new Path(sdfPath))

		// Logger is used to display messages in the console
		val logger = WorkflowLogger.getLogger

		// Identify all actors with a memory Script
		for (dagVertex : dag.vertexSet) {
			val sdfVertex = dagVertex.propertyBean.getValue(DAGVertex.SDF_VERTEX, SDFAbstractVertex) as SDFAbstractVertex
			if (dagVertex.kind != null) {
				switch (dagVertex.kind) {
					case DAGVertex.DAG_VERTEX: {
						val pathString = sdfVertex.propertyBean.getValue(SDFVertex.MEMORY_SCRIPT, String) as String
						if (pathString != null) {

							// Retrieve the script path as a relative path to the
							// graphml
							var scriptFile = sdfFile.parent.getFile(new Path(pathString)).rawLocation.makeAbsolute.
								toFile
							if (scriptFile.exists) {
								scriptedVertices.put(dagVertex, scriptFile)
							} else {

								// If this code is reached
								// Check if the script file exists in the source code
								// /scripts directory.
								val classpathString = "/../scripts/" + pathString
								var URI sourceStream
								try {
									sourceStream = class.getResource(classpathString).toURI
									scriptFile = new File(FileLocator.resolve(sourceStream.toURL).file)
									if (scriptFile.exists) {
										scriptedVertices.put(dagVertex, scriptFile)
									}
								} catch (Exception e) {
									// Nothing to do
								}
								if (scriptFile == null || !scriptFile.exists)
									logger.log(Level.WARNING,
										"Memory script of vertex " + sdfVertex.getName() + " is invalid: \"" +
											pathString + "\". Change it in the graphml editor.")
							}
						}
					}
					case DAGForkVertex.DAG_FORK_VERTEX: {
						val classpathString = "/../scripts/fork.bsh";
						try {
							val sourceStream = class.getResource(classpathString).toURI;
							val scriptFile = new File(FileLocator.resolve(sourceStream.toURL).file)
							if (scriptFile.exists) {
								scriptedVertices.put(dagVertex, scriptFile)
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE,
								"Memory script of fork vertices not found. Please contact Preesm developers.")
						}
					}
					case DAGJoinVertex.DAG_JOIN_VERTEX: {
						val classpathString = "/../scripts/join.bsh"
						try {
							val sourceStream = class.getResource(classpathString).toURI
							val scriptFile = new File(FileLocator.resolve(sourceStream.toURL).file)
							if (scriptFile.exists) {
								scriptedVertices.put(dagVertex, scriptFile)
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE,
								"Memory script of join vertices not found. Please contact Preesm developers.")
						}

					}
					case DAGBroadcastVertex.DAG_BROADCAST_VERTEX: {
						var classpathString = if (sdfVertex instanceof SDFRoundBufferVertex) {
								"/../scripts/roundbuffer.bsh"
							} else {
								"/../scripts/broadcast.bsh"
							}
						try {
							val sourceStream = class.getResource(classpathString).toURI

							val scriptFile = new File(FileLocator.resolve(sourceStream.toURL).file)
							if (scriptFile.exists) {
								scriptedVertices.put(dagVertex, scriptFile)
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE,
								"Memory script of broadcast/roundbuffer vertices not found. Please contact Preesm developers.")
						}
					}
				}
			}
		}

		scriptedVertices.size
	}

	/**
	 * This method process the {@link #scriptResults} in order to simplify 
	 * them with {@link #simplifyResult(List,List)}. Then, it extracts 
	 * mergeable buffers. 
	 * This method must be called after {@link #run()} and {@link #check()} 
	 * have been successfully called.
	 */
	def process() {

		// Simplify results
		scriptResults.forEach[vertex, result|simplifyResult(result.key, result.value)]

		// Identify divisible buffers
		scriptResults.forEach[vertex, result|identifyDivisibleBuffers(result)]

		// Identify inter-inputs and inter-outputs matches
		scriptResults.forEach[vertex, result|identifySiblingMatches(result)]

		// Identify match that may cause a inter-input / inter-output merge
		scriptResults.forEach[vertex, result|identifyConflictingMatchCandidates(result.key, result.value)]

		// Identify groups of chained buffers from the scripts and dag
		val groups = groupVertices()

		// Process the groups one by one
		groups.forEach [
			it.processGroup
		]
		var result = groups.fold(0, [res, gr|res + gr.size])
		println("Identified " + groups.size + " groups. " + result)
	}

	/**
	 * For each {@link Buffer} passed as a parameter, this method scan the 
	 * {@link Match} in the {@link Buffer#getMatchTable() matchTable} and set.
	 * their {@link Match#getType() type}. Matches whose {@link 
	 * Match#getLocalBuffer() localBuffer} and {@link Match#getRemoteBuffer() 
	 * remoteBuffer} belong to the same {@link List} of {@link Match} are 
	 * marked as <code>INTER_SIBLINGS</code>. Other {@link Match} are marked as
	 * <code>FORWARD</code> or <code>BACKWARD</code>.
	 * 
	 * @param result
	 * 	{@link Pair} of {@link List} of {@link Buffer}. The {@link Pair} key 
	 * and value respectively contain input and output {@link Buffer} of an 
	 * actor. 
	 */
	def identifySiblingMatches(Pair<List<Buffer>, List<Buffer>> result) {
		result.key.forEach [
			it.matchTable.values.flatten.forEach [
				if (result.key.contains(it.remoteBuffer)) {
					it.siblingMatch = true
					it.type = MatchType::INTER_SIBLINGS
				} else {
					it.type = MatchType::FORWARD
				}
			]
		]

		result.value.forEach [
			it.matchTable.values.flatten.forEach [
				if (result.value.contains(it.remoteBuffer)) {
					it.siblingMatch = true
					it.type = MatchType::INTER_SIBLINGS
				} else {
					it.type = MatchType::BACKWARD
				}
			]
		]
	}

	/** 
	 * Also fill the {@link Buffer#getDivisibilityRequiredMatches() 
	 * divisibilityRequiredMatches} {@link List}. 
	 */
	def identifyDivisibleBuffers(Pair<List<Buffer>, List<Buffer>> result) {
		val allBuffers = new ArrayList<Buffer>
		allBuffers.addAll(result.key)
		allBuffers.addAll(result.value)
		val divisibleCandidates = allBuffers.filter [ buffer |
			// A buffer is potentially divisible 	
			// If it has several matches (that were not merged by the
			//  simplifyResult). (Because if the buffer only has one
			// contiguous match, a divided buffer is not possible, cf
			// Buffer.simplifyMatches() comments.)
			buffer.matchTable.size > 1 &&
				// if it is totally matched, so that all parts of the divided 
				// buffer can still be accessed in an immediately logical way.
				// With the successive merges, unmatched ranges might become 
				// part of an indivisible range with a matched range. However
				// since this kind of behavior is not intuitive, we set not
				// completely matched buffers as indivisible from the start so 
				// that the developer knows where tokens are only by looking at
				// its actor script.
				buffer.completelyMatched
		// Note that at this point, virtual tokens are always matched
		// so this constraint ensure that future virtual tokens are 
		// always attached to real token by an overlapping 
		// indivisible range !
		]

		// All are divisible BUT it will not be possible to match divided
		// buffers together (checked later)
		divisibleCandidates.forEach [ buffer |
			val drMatches = newArrayList
			buffer.divisibilityRequiredMatches.add(drMatches)
			buffer.matchTable.values.flatten.forEach [
				val r = it.localRange
				buffer.indivisibleRanges.lazyUnion(r)
				drMatches.add(it)
			]
		]

		// All other buffers are not divisible
		allBuffers.removeAll(divisibleCandidates)
		allBuffers.forEach [
			it.indivisibleRanges.add(new Range(it.minIndex, it.maxIndex))
		]
	}

	/**
	 * This method fills the {@link Match#getConflictCandidates() 
	 * conflictCandidates} {@link Match} {@link List} of all the {@link Match 
	 * matches} contained in the {@link Buffer#getMatchTable() matchTable} of 
	 * the {@link Buffer} passed as parameter. Two {@link Match} are 
	 * potentially conflicting if:
	 * <ul><li>They link inputs with inputs (or outputs with outputs).<br>
	 * <b>OR</b></li>
	 * <li>They have the same {@link Match#getLocalBuffer()}</li></ul>
	 * 
	 * @param inputs
	 * 	{@link List} of input {@link Buffer} of an actor.
	 * @param outputs
	 * 	{@link List} of output {@link Buffer} of an actor.
	 */
	def identifyConflictingMatchCandidates(List<Buffer> inputs, List<Buffer> outputs) {

		// Identify potentially conflicting matches
		// For each Buffer
		for (buffer : #{inputs, outputs}.flatten) {

			// Sort matches in 2 groups: 
			val regularMatches = newArrayList
			val interSiblingMatches = newArrayList
			buffer.matchTable.values.flatten.forEach [ match |
				if (match.siblingMatch) {
					interSiblingMatches.add(match)
				} else {
					regularMatches.add(match)
				}
			]

			// Update the potential conflict list of all matches
			regularMatches.forEach [ match |
				match.conflictCandidates.addAll(regularMatches.filter[it != match])
			]
			interSiblingMatches.forEach [ match |
				match.conflictCandidates.addAll(regularMatches.filter[it != match])
			]
		}

		// Identify the already conflicting matches
		for (buffer : #{inputs, outputs}.flatten) {

			// for Each match
			val matchList = buffer.matchTable.values.flatten
			Buffer::updateConflictingMatches(matchList)
		}
	}

	/**
	 * Process the groups generated by the groupVertices method.
	 */
	def processGroup(ArrayList<DAGVertex> vertices) {

		// Get all the buffers
		val buffers = newArrayList
		vertices.forEach [
			var pair = scriptResults.get(it)
			// Buffer that were already merged are not processed
			buffers.addAll(pair.key.filter[it.appliedMatches.size == 0])
			buffers.addAll(pair.value.filter[it.appliedMatches.size == 0])
		]

		// Sort the buffers in alphabetical order to enforce similarities 
		// between successive run
		buffers.sortInplaceBy[name]

		//println(buffers.fold(0,[res, buf | res + buf.maxIndex - buf.minIndex]))
		// Iterate the merging algorithm until no buffers are merged
		var updated = false
		var step = 0
		do {
			switch (step) {
				// First step: Merge non-conflicting buffer with a unique match 
				case 0: {

					// Find all buffers with a unique match (if any)
					val candidates = buffers.filter [
						val entry = it.matchTable.entrySet.head
						// Returns true if:
						// There is a unique match
						it.matchTable.size == 1 && entry.value.size == 1 &&
							// that begins at index 0 (or less)
							entry.key <= 0 &&
							// and ends at the end of the buffer (or more)
							entry.key + entry.value.head.length >= it.nbTokens * it.tokenSize &&
						    // and is not involved in any conflicting range
							{
								val match = entry.value.head
								match.conflictingMatches.size == 0
							}
					].toList.immutableCopy

					// Copy the candidate list, otherwise it is updated when
					// the content of buffers are modified
					println('''0- («candidates.size») «candidates.map[it.matchTable.entrySet.head.value.head]»''')
					if (!candidates.empty) {

						// If there are candidates, merge them all and do step 0 again
						step = 0

						// Do the merge
						candidates.forEach [
							it.applyMatches(it.matchTable.entrySet.head.value)
						]
						buffers.removeAll(candidates)

					} else {

						// If there was no candidates, go to step 1
						step = 1
					}
				} // case 0
				// Second step: Merge divisible buffers with multiple matchs 
				// and no conflict 
				case 1: {

					// Find all divisible buffers with multiple match and no 
					// conflict that are not matched in another divisible buffer
					// (if any)
					val candidates = buffers.filter [
						// Has a non-empty matchTable 
						val t1 = it.matchTable.size != 0
						// is divisible
						val t2 = it.divisible
						val t3 = it.matchTable.values.flatten.forall [
							// Is not involved in any conflicting range
							it.conflictingMatches.size == 0
						]
						// Has no multiple match Range. 
						val t4 = it.multipleMatchRange.size == 0
						// No need to check the divisibilityRequiredMatches since
						// the only matches of the Buffer are the one
						// responsible for the division
						t1 && t2 && t3 && t4
					].toList.immutableCopy

					println('''1- («candidates.size») «candidates»''')
					if (!candidates.empty) {

						// If there are candidates, merge them all and do step 0 again
						step = 0

						// Do the matche
						candidates.forEach [
							applyDivisionMatch(it)
						]
						buffers.removeAll(candidates)

					} else {
						step = 2
					}
				}
				// Third step: Same as step 0, but test forward matches
				// of buffers only
				case 2: {
					val matchedBuffers = newArrayList
					while (step == 2) {
						var MatchType type

						// Find all buffers with a unique forward match (if any)
						var candidate = buffers.findFirst [
							val matches = it.matchTable.values.flatten.filter[it.type == MatchType::FORWARD]
							// Returns true if:
							// There is a unique forward match
							matches.size == 1 &&
								// that begins at index 0 (or less)
								matches.head.localIndex <= 0 &&
								// and ends at the end of the buffer (or more)
								matches.head.localIndex + matches.head.length >= it.nbTokens * it.tokenSize &&
								// and is not involved in any conflicting match
								matches.head.conflictingMatches.size == 0
						]
						if (candidate != null) {
							type = MatchType::FORWARD
						} else {
							candidate = buffers.findFirst [
								val matches = it.matchTable.values.flatten.filter[it.type == MatchType::BACKWARD]
								// Returns true if:
								// There is a unique forward match
								matches.size == 1 &&
									// that begins at index 0 (or less)
									matches.head.localIndex <= 0 &&
									// and ends at the end of the buffer (or more)
									matches.head.localIndex + matches.head.length >= it.nbTokens * it.tokenSize &&
									// and is not involved in any conflicting match
									matches.head.conflictingMatches.size == 0
							]
							if (candidate != null) {
								type = MatchType::BACKWARD
							}
						}

						if (candidate != null) {
							val valType = type // a val is needed to be usable in the filter lambda expression
							matchedBuffers.add(candidate.matchTable.values.flatten.filter[it.type == valType].head)

							// If there is a candidate, merge them all and do step 2 again
							candidate.applyMatches(
								#[candidate.matchTable.values.flatten.filter[it.type == valType].head])

							step = 2
							buffers.remove(candidate)

						} else {
							println('''2- («matchedBuffers.size») «matchedBuffers»''')
							step = 3
						}
					}
				}
				// Fourth step: Like case 1 but considering forward only
				// or backward only matches
				case 3: {
					val matchedBuffers = newArrayList
					while (step == 3) {
						var MatchType type

						// Find all buffers with a division with forward matches (if any)
						var candidate = buffers.findFirst [
							val matches = it.matchTable.values.flatten.filter[it.type == MatchType::FORWARD]
							// Returns true if:
							// Has a several forward matches 
							matches.size != 0 &&						
							// is divisible
							it.divisible && matches.forall [
								// Is not involved in any conflicting range
								it.conflictingMatches.size == 0
							] &&
								// Matches have no multiple match Range. 
								matches.overlappingRanges.size == 0 &&
								// Check divisibilityRequiredMatches
								it.doesCompleteRequiredMatches(matches)
						]
						if (candidate != null) {
							type = MatchType::FORWARD
						} else {
							candidate = buffers.findFirst [
								val matches = it.matchTable.values.flatten.filter[it.type == MatchType::BACKWARD]
								// Returns true if:
								// Has a several forward matches 
								matches.size != 0 &&						
								// is divisible
								it.divisible && matches.forall [
									// Is not involved in any conflicting range
									it.conflictingMatches.size == 0
								] &&
									// Matches have no multiple match Range. 
									matches.overlappingRanges.size == 0 &&
									// Check divisibilityRequiredMatches
									it.doesCompleteRequiredMatches(matches)
							]
							if (candidate != null) {
								type = MatchType::BACKWARD
							}
						}

						if (candidate != null) {
							matchedBuffers.add(candidate)
							val valType = type // a val is needed to be usable in the filter lambda expression

							// If there is a candidate, merge them all and do step 3 again
							candidate.applyMatches(
								#[candidate.matchTable.values.flatten.filter[it.type == valType].head])
							step = 3
							buffers.remove(candidate)

						} else {
							println('''3- («matchedBuffers.size») «matchedBuffers»''')
							step = 4
						}
					}
				}
				// Fifth step: Mergeable buffers with a unique match that have conflicts 
				case 4: {

					// Find all buffers with a unique match (if any)
					val candidates = buffers.filter [
						val entry = it.matchTable.entrySet.head
						// Returns true if:
						// There is a unique match
						it.matchTable.size == 1 && entry.value.size == 1 &&
							// that begins at index 0 (or less)
							entry.key <= 0 &&
							// and ends at the end of the buffer (or more)
							entry.key + entry.value.head.length >= it.nbTokens * it.tokenSize &&
						    // and is involved in conflicting matche(s)
							{
								val match = entry.value.head
								match.conflictingMatches.size > 0
							} &&
							// Buffer is mergeable
							{
								it.mergeableRanges.size == 1 &&
								it.mergeableRanges.head.start == it.minIndex &&
								it.mergeableRanges.head.end == it.maxIndex								
							} 
					].toList.immutableCopy

					// Copy the candidate list, otherwise it is updated when
					// the content of buffers are modified
					//println('''4- («candidates.size») «candidates.map[it.matchTable.entrySet.head.value.head]»''')
					if (!candidates.empty) {

						// If there are candidates, merge them all and do step 0 again
						step = 5

						// Do the merge
						//candidates.forEach [
						//	it.applyMatches(it.matchTable.entrySet.head.value)
						//]
						//buffers.removeAll(candidates)

					} else {

						// If there was no candidates, go to step 5
						step = 5
					}

				}
			}

			updated = step != 5
		} while (updated)

		//println(buffers.fold(0,[res, buf | res + buf.maxIndex - buf.minIndex]))
		println("---")
	}

	/**
	 * Called only for divisible buffers with multiple match and no 
	 * conflict that are not matched in another divisible buffer
	 */
	def applyDivisionMatch(Buffer buffer) {

		// In the current version, the buffer only contains
		// the matches necessary and sufficient for the division (i.e. no multiple matched ranges)
		// To process this special case in the future, some matches will have 
		// to be changes: e.g. siblings will become forward or things like that
		// . For a simpler version, simply remove those other matches.	
		// The match table will be modified by the applyMatch method, so we need a copy of it to iterate ! 
		val matches = new ArrayList(buffer.matchTable.values.flatten.toList)

		// Remove the matches from each other conflict candidates
		matches.forEach [
			it.conflictCandidates.removeAll(matches)
			it.reciprocate.conflictCandidates.removeAll(matches.map[it.reciprocate])
		]

		// apply the matches of the buffer one by one
		buffer.applyMatches(matches)
	}

	/**
	 * The purpose of this method is to create groups of {@link DAGVertex} which
	 * satisfy the following conditions:
	 * <ul>
	 * <li> {@link DAGVertex Vertices} are associated to a memory script</li>
	 * <li> {@link DAGVertex Vertices} of the same group are strongly connected
	 * via {@link DAGEdge FIFOs}</li>
	 * </ul>
	 * The {@link #scriptResults} attribute of the calling {@link ScriptRunner}
	 * are updated by this method. In particular, a
	 * {@link Buffe#matchWith(int,Buffer,int,int) match} is added between
	 * buffers of different actors that correspond to the same SDFEdges. This 
	 * method must be called after {@link
	 * ScriptRunner#identifyDivisibleBuffer()} as it set to indivisible the 
	 * buffers that are on the border of groups. 
	 * 
	 * @return a {@link List} of groups. Each group is itself a {@link List} of
	 *         {@link DAGVertex}.
	 */
	def groupVertices() {

		// Each dag vertex can be involved in at most one group
		val groups = newArrayList
		val dagVertices = new ArrayList(scriptResults.keySet)
		while (dagVertices.size != 0) {

			// Get the first dagVertex
			val dagSeedVertex = dagVertices.remove(0)

			// Create a new group
			val group = newArrayList(dagSeedVertex)

			// Identify other vertices that can be put into the group
			var List<DAGVertex> newVertices = newArrayList(dagSeedVertex)
			val List<Buffer> intraGroupBuffer = newArrayList
			while (newVertices.size != 0) {

				// Initialize the group size
				val groupSize = group.size

				// For all vertices from the newVertices list
				// check if a successors/predecessor can be added to the group
				for (dagVertex : newVertices) {
					val candidates = newArrayList
					dagVertex.incomingEdges.forEach[candidates.add(it.source)]
					dagVertex.outgoingEdges.forEach[candidates.add(it.target)]

					var addedVertices = group.subList(groupSize, group.size)
					for (candidate : candidates) {
						if (addedVertices.contains(candidate) || newVertices.contains(candidate) ||
							dagVertices.contains(candidate)) {

							// Match the buffers corresponding to the edge 
							// between vertices "dagVertex" and "candidate"
							// Get the sdfEdges
							var dagEdge = ( dagVertex.base.getEdge(dagVertex, candidate) ?:
								dagVertex.base.getEdge(candidate, dagVertex)) as DAGEdge

							// For edges between newVertices, only process if the dagVertex
							// is the source (to avoid matching the pair of buffer twice)
							var validBuffers = false
							val isBetweenNewVertices = newVertices.contains(candidate)
							if (!isBetweenNewVertices || dagEdge.source == dagVertex) {

								// Add match between the two buffers that
								// correspond to the sdf edge(s) between vertex
								// and it 
								val bufferCandidates = newArrayList
								for (v : #{dagVertex, candidate}) {
									var pair = scriptResults.get(v)
									bufferCandidates.addAll(pair.key)
									bufferCandidates.addAll(pair.value)
								}
								for (sdfEdge : dagEdge.aggregate) {

									// Find the 2 buffers corresponding to this sdfEdge
									var buffers = bufferCandidates.filter[it.sdfEdge == sdfEdge]
									if (buffers.size == 2) {
										validBuffers = true

										// Match them together
										val match = buffers.get(0).matchWith(0, buffers.get(1), 0,
											buffers.get(0).nbTokens)
										val forwardMatch = if (buffers.get(0).dagVertex == dagEdge.source) {
												match.type = MatchType::FORWARD
												match.reciprocate.type = MatchType::BACKWARD
												match
											} else {
												match.type = MatchType::BACKWARD
												match.reciprocate.type = MatchType::FORWARD
												match.reciprocate
											}

										// Apply the forward match immediately
										// (we always apply the forward match to enforce
										// reproducibility of the processing)
										forwardMatch.localBuffer.applyMatches(#[forwardMatch])

										// Save matched buffer
										intraGroupBuffer.add(match.localBuffer)
										intraGroupBuffer.add(match.remoteBuffer)

									}
								}
							}

							// Add the vertex to the group (if not already in 
							// it) and if there was valid buffers)
							if (!group.contains(candidate) && validBuffers) {
								group.add(candidate)
								dagVertices.remove(candidate)
								addedVertices = group.subList(groupSize, group.size)
							}
						}
					}
				}

				// Update the newVertices list (we do not use sublists here because it causes
				// a ConcurrentModificationException
				newVertices = new ArrayList<DAGVertex>(group.subList(groupSize, group.size))
			}

			// Set as indivisible all buffers that are on the edge of the group.
			group.forEach [
				val results = scriptResults.get(it)
				#{results.key, results.value}.flatten.filter[!intraGroupBuffer.contains(it)].forEach [
					it.indivisibleRanges.lazyUnion(new Range(it.minIndex, it.maxIndex))
				]
			]

			// The group is completed, save it
			groups.add(group)
		}
		groups
	}

	/**
	 * This method run the scripts that were found during the call to
	 * {@link #findScripts()}. As a result, the {@link #scriptResults} is
	 * filled.<br>
	 * <br>
	 * 
	 * If the execution of a script fails, the {@link Interpreter} error message
	 * will be printed in the {@link Logger log} as a warning.<br>
	 * <br>
	 * The {@link #check(List,List)} method is also used after each script
	 * execution to verify the validity of the script results. If the results
	 * are not valid, they will not be stored in the {@link #scriptResults}
	 * {@link Map}, and a warning will be printed in the {@link Logger log}.
	 */
	def run() {

		// For each vertex with a script
		for (e : scriptedVertices.entrySet) {
			val dagVertex = e.key
			val script = e.value
			val interpreter = new Interpreter();

			// Retrieve the corresponding sdf vertex
			val sdfVertex = dagVertex.getPropertyBean().getValue(DAGVertex.SDF_VERTEX, SDFAbstractVertex) as SDFAbstractVertex

			// Create the vertex parameter list
			val Map<String, Integer> parameters = newHashMap
			{
				val arguments = sdfVertex.propertyBean.getValue(SDFAbstractVertex.ARGUMENTS) as HashMap<String, Argument>
				if(arguments != null) arguments.entrySet.forEach[parameters.put(it.key, it.value.intValue)]
			}

			// Create the input/output lists
			val inputs = sdfVertex.incomingEdges.map[
				new Buffer(it, dagVertex, it.targetLabel, it.cons.intValue, dataTypes.get(it.dataType.toString).size,
					(it.sourcePortModifier ?: "").toString.contains(SDFEdge::MODIFIER_PURE_OUT))].toList
			val outputs = sdfVertex.outgoingEdges.map[
				new Buffer(it, dagVertex, it.sourceLabel, it.prod.intValue, dataTypes.get(it.dataType.toString).size,
					(it.targetPortModifier ?: "").toString.contains(SDFEdge::MODIFIER_PURE_IN) ||
						(it.targetPortModifier ?: "").toString.contains(SDFEdge::MODIFIER_UNUSED))].toList

			// Import the necessary libraries
			interpreter.eval("import " + Buffer.name + ";")
			interpreter.eval("import " + List.name + ";")

			// Feed the parameters/inputs/outputs to the interpreter
			parameters.forEach[name, value|interpreter.set(name, value)]
			inputs.forEach[interpreter.set("i_" + it.name, it)]
			outputs.forEach[interpreter.set("o_" + it.name, it)]
			if (interpreter.get("parameters") == null)
				interpreter.set("parameters", parameters)
			if (interpreter.get("inputs") == null)
				interpreter.set("inputs", inputs)
			if (interpreter.get("outputs") == null)
				interpreter.set("outputs", outputs)
			try {

				// Run the script
				interpreter.source(script.absolutePath);

				// Store the result if the execution was successful 
				scriptResults.put(dagVertex, inputs -> outputs)
			} catch (ParseException error) {

				// Logger is used to display messages in the console
				val logger = WorkflowLogger.getLogger
				var message = error.rawMessage + "\n" + error.cause
				logger.log(Level.WARNING, "Parse error in " + sdfVertex.name + " memory script:\n" + message)
			} catch (EvalError error) {

				// Logger is used to display messages in the console
				val logger = WorkflowLogger.getLogger
				var message = error.rawMessage + "\n" + error.cause
				logger.log(Level.WARNING,
					"Evaluation error in " + sdfVertex.name + " memory script:\n[Line " + error.errorLineNumber + "] " +
						message)
			} catch (IOException exception) {
				exception.printStackTrace
			}
		}
	}

	def runTest() {
		val interpreter = new Interpreter();

		// Import the necessary libraries
		interpreter.eval("import " + Buffer.name + ";")

		//interpreter.eval("import " + Map.name + ";")
		// Retrieve Parameters
		var parameters = newHashMap("NbSlice" -> 3, "Overlap" -> 1, "Height" -> 3, "Width" -> 2)
		parameters.forEach[name, value|interpreter.set(name, value)]

		// Retrieve buffers
		var inputs = newArrayList(
			new Buffer(null, null, "input", parameters.get("Height") * parameters.get("Width"), 1, true))
		inputs.forEach[interpreter.set("i_" + it.name, it)]

		var outputs = newArrayList(
			new Buffer(null, null, "output",
				parameters.get("Height") * parameters.get("Width") +
					parameters.get("NbSlice") * parameters.get("Overlap") * 2 * parameters.get("Width"), 1, true))
		outputs.forEach[interpreter.set("o_" + it.name, it)]

		try {
			val sourceStream = class.getResource("/../scripts/split.bsh").toURI
			val scriptFile = new File(FileLocator.resolve(sourceStream.toURL).file)
			if (scriptFile.exists) {
				interpreter.source(scriptFile.absolutePath);
			}

		} catch (IOException e) {
			e.printStackTrace

		}
	}

	/**
	 * This method calls {@link Buffer#simplifyMatches()} for each 
	 * {@link Buffer} of the {@link #scriptResults}. 
	 * If a {@link Buffer} has an empty {@link Buffer#getMatchTable() 
	 * matchTable} after the simplification process, it is removed from
	 * the  {@link #scriptResults}.
	 */
	def simplifyResult(List<Buffer> inputs, List<Buffer> outputs) {
		val allBuffers = new ArrayList<Buffer>
		allBuffers.addAll(inputs)
		allBuffers.addAll(outputs)

		// Matches whose reciprocate has been processed
		// no need to test them again
		val processedMatch = newArrayList

		// Iterate over all buffers
		allBuffers.forEach[it.simplifyMatches(processedMatch)]

		// If a buffer has an empty matchTable, remove it from its list
		val unmatchedBuffer = allBuffers.filter[it.matchTable.empty]
		inputs.removeAll(unmatchedBuffer)
		outputs.removeAll(unmatchedBuffer)
	}
}
