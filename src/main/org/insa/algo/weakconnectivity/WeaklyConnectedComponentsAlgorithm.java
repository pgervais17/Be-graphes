package org.insa.algo.weakconnectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;

import org.insa.algo.AbstractAlgorithm;
import org.insa.algo.AbstractSolution.Status;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.Node;

public class WeaklyConnectedComponentsAlgorithm extends AbstractAlgorithm<WeaklyConnectedComponentObserver>{

	/**
	 * 
	 * @param instance
	 * @param logOutput
	 */
	public WeaklyConnectedComponentsAlgorithm(WeaklyConnectedComponentsInstance instance) {
		super(instance);
	}
	
	@Override
	public WeaklyConnectedComponentsSolution run() {
		return (WeaklyConnectedComponentsSolution)super.run();
	}
	
	@Override
	public WeaklyConnectedComponentsInstance getInstance() {
		return (WeaklyConnectedComponentsInstance)super.getInstance();
	}
	
	/**
	 * Notify all observers that the algorithm is entering a new component.
	 * 
	 * @param curNode Starting node for the component.
	 */
	protected void notifyStartComponent(Node curNode) {
		for (WeaklyConnectedComponentObserver obs: getObservers()) {
			obs.notifyStartComponent(curNode);
		}
	}
	
	/**
	 * Notify all observers that a new node has been found for the current component.
	 * 
	 * @param node New node found for the current component.
	 */
	protected void notifyNewNodeInComponent(Node node) {
		for (WeaklyConnectedComponentObserver obs: getObservers()) {
			obs.notifyNewNodeInComponent(node);
		}
	}
	
	/**
	 * Notify all observers that the algorithm has computed a new component.
	 * 
	 * @param nodes List of nodes in the component.
	 */
	protected void notifyEndComponent(ArrayList<Node> nodes) {
		for (WeaklyConnectedComponentObserver obs: getObservers()) {
			obs.notifyEndComponent(nodes);
		}
	}
	
	/**
	 * @return An adjacency list for the undirected graph equivalent to the stored graph.
	 */
	protected ArrayList<HashSet<Integer>> createUndirectedGraph() {
		int nNodes = getInstance().getGraph().getNodes().size();
		ArrayList<HashSet<Integer>> res = new ArrayList<HashSet<Integer>>(nNodes);
		for (int i = 0; i < nNodes; ++i) {
			res.add(new HashSet<Integer>());
		}		
		
		for (Node node: getInstance().getGraph().getNodes()) {
			for (Arc arc: node.getSuccessors()) {
				res.get(node.getId()).add(arc.getDestination().getId());
				if (arc.getInfo().isOneWay()) {
					res.get(arc.getDestination().getId()).add(node.getId());
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Apply a breadth first search algorithm on the given undirected graph (adjacency list),
	 * starting at node cur, and marking nodes in marked.
	 * 
	 * @param marked
	 * @param cur
	 * 
	 * @return
	 */
	protected ArrayList<Node> bfs(ArrayList<HashSet<Integer>> ugraph, boolean[] marked, int cur) {
		ArrayList<Node> nodes = getInstance().getGraph().getNodes();
		ArrayList<Node> component = new ArrayList<Node>();
		
		// Using a queue because we are doing a BFS
		Queue<Integer> queue = new LinkedList<Integer>();

		// Notify observers about the current component.
		notifyStartComponent(nodes.get(cur));
		
		// Add original node and loop until the queue is empty.
		queue.add(cur);
		marked[cur] = true;
		while (!queue.isEmpty()) {
			Node node = nodes.get(queue.remove());
			component.add(node);
			
			// Notify observers
			notifyNewNodeInComponent(node);
			
			for (Integer destId: ugraph.get(node.getId())) {
				Node dest = nodes.get(destId);
				if (!marked[dest.getId()]) {
					queue.add(destId);
					marked[destId] = true;
				}
			}
		}
		
		notifyEndComponent(component);
		
		return component;
	}

	@Override
	protected WeaklyConnectedComponentsSolution doRun() {
		
		Graph graph = getInstance().getGraph();
		ArrayList<HashSet<Integer>> ugraph = createUndirectedGraph();
		boolean[] marked = new boolean[graph.getNodes().size()];
		Arrays.fill(marked, false);
		
		ArrayList<ArrayList<Node>> components = new ArrayList<ArrayList<Node>>();
		
		// perform algorithm
		int cur = 0;
		while (cur < marked.length) {
			// Apply BFS
			components.add(this.bfs(ugraph, marked, cur));
			
			// Find next non-marked
			for (; cur < marked.length && marked[cur]; ++cur);
		}
				
		return new WeaklyConnectedComponentsSolution(getInstance(), Status.OPTIMAL, components);
	}

}
