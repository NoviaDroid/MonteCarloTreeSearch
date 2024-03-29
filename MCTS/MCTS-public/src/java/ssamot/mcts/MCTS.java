/*
 *
 * *** BEGIN LICENSE
 *  Copyright (C) 2012 Spyridon Samothrakis spyridon.samothrakis@gmail.com
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 3, as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranties of
 *  MERCHANTABILITY, SATISFACTORY QUALITY, or FITNESS FOR A PARTICULAR
 *  PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * *** END LICENSE
 *
 */

package ssamot.mcts;

import java.util.ArrayList;
import java.util.List;

import ssamot.mcts.MCTSNode.NodeType;
import ssamot.mcts.backpropagators.Backpropagator;
import ssamot.mcts.selectors.Selector;
import ssamot.utilities.ElapsedCpuTimer;
import ssamot.visualitation.MCTSCell;
import ssamot.visualitation.MCTSVisualisationHandler;
import ssamot.visualitation.Visualiser;

/**
 * @author spyros
 * 
 */
public class MCTS<N extends MCTSNode> {

	public static boolean DEBUG = true;

	private N rootNode;

	private int treePolicyMaxTreeDepth = 30000;
	private boolean enableDefaultPolicy = false;

	private Selector<N> chanceNodeSelector;
	private Selector<N> deterministicNodeSelector;
	private Selector<N> adversarialNodeSelector;
	private Selector<N> actionSelector;
	private Backpropagator<N> backpropagator;

	private boolean visualisation;

	private Visualiser vis;

	private MCTSVisualisationHandler<N> visualisationHandler;

	public MCTSVisualisationHandler<N> getVisualisationHandler() {
		return visualisationHandler;
	}

	public void setVisualisationHandler(
			MCTSVisualisationHandler<N> visualisationHandler) {
		this.visualisationHandler = visualisationHandler;
	}

	public boolean getVisualisation() {
		return visualisation;
	}

	public void setVisualisation(boolean visualisation) {
		this.visualisation = visualisation;
	}

	public Selector<N> getChanceNodeSelector() {
		return chanceNodeSelector;
	}

	public Selector<N> getDeterministicNodeSelector() {
		return deterministicNodeSelector;
	}

	public Selector<N> getActionSelector() {
		return actionSelector;
	}

	public Backpropagator<N> getBackpropagator() {
		return backpropagator;
	}

	public void setChanceNodeSelector(Selector<N> chanceNodeSelector) {
		this.chanceNodeSelector = chanceNodeSelector;
	}

	public void setDeterministicNodeSelector(
			Selector<N> deterministicNodeSelector) {
		this.deterministicNodeSelector = deterministicNodeSelector;
	}

	public void setActionSelector(Selector<N> actionSelector) {
		this.actionSelector = actionSelector;
	}

	public Selector<N> getAdversarialNodeSelector() {
		return adversarialNodeSelector;
	}

	public void setAdversarialNodeSelector(Selector<N> adversarialNodeSelector) {
		this.adversarialNodeSelector = adversarialNodeSelector;
	}

	public void setBackpropagator(Backpropagator<N> backpropagator) {
		this.backpropagator = backpropagator;
	}

	public boolean shouldEnableDefaultPolicy() {
		return enableDefaultPolicy;
	}

	public void enableDefaultPolicy(boolean enableDefaultPolicy) {
		this.enableDefaultPolicy = enableDefaultPolicy;
	}

	public N getRootNode() {
		return rootNode;
	}

	public void setRootNode(N rootNode) {
		this.rootNode = rootNode;
	}

	public MCTS(N rootNode) {
		super();
		this.rootNode = rootNode;
	}

	public MCTS() {
		super();
	}

	// public double getRootNodeValue() {
	// return this.rootNode.value;
	// }

	public void runForMs(long time) {

		int totalSimulations = 0;

		// reset the timer
		ElapsedCpuTimer cput = new ElapsedCpuTimer(
				ElapsedCpuTimer.TimerType.WALL_TIME);

		for (;;) {
			// Do a simulation
			totalSimulations += 1;
			rootNode.init();
			playOneSequence(rootNode);
			// System.out.println(cput.elapsedMillis());
			if (time <= cput.elapsedMillis()) {

				break;
			}

		}

		if (DEBUG) {
			System.err.println("Total Time Spent is: "
					+ (double) (cput.elapsedMillis()) + "ms");
			System.err.println("Total Simulations: " + totalSimulations);
		}

	}

	public void runForSim(int simulations) {

		ElapsedCpuTimer cput = new ElapsedCpuTimer(
				ElapsedCpuTimer.TimerType.WALL_TIME);

		int totalSimulations = 0;

		for (; totalSimulations < simulations; totalSimulations++) {
			//System.err.println(totalSimulations);
			rootNode.init();
			playOneSequence(rootNode);
		}

		if (DEBUG) {
			System.err.println("Total Time Spent is: "
					+ (double) (cput.elapsedMillis()) + "ms");
			System.err.println("Total Simulations: " + totalSimulations);
		}

	}

	public N getHighestScoringChild(N node) {
		return actionSelector.selectChild(node);
	}

	public N getHighestScoringChild() {
		return getHighestScoringChild(rootNode);
	}

	private boolean playOneSequence(N rootNode) {
		// System.err.println("playing one sequence");
		List<N> nodes = new ArrayList<N>();
		nodes.add(rootNode);

		N node = rootNode;
		
		if (getVisualisation()) {
		
			

			

			
			if (vis == null) {
				vis = new Visualiser();
				vis.init();
				 MCTSCell child = new MCTSCell((StatisticsNode) rootNode);
				// top level node
				System.out.println("Initialising Visualitation Graph");
				try {
					vis.addNode(null, child, "edge");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// System.err.println("Starging Loop");
		// just for two people games, pick a random move for the opponent
		for (int depth = 0;; depth++) {
			// System.err.println(depth + "inside UCT");

			node = descend(nodes.get(depth));
			if (node == null) {
				// We have found a null node, this should never happen
				throw new RuntimeException("Found a null node at depth "
						+ depth);
			}

			nodes.add(node);

			if (getVisualisation()) {
			
				visualisationHandler.handle(nodes.get(nodes.size() - 2), node,vis);

			}

			// These can be put in the for loop, but it's easier to see what's
			// going on here
			// System.out.println(node);
			if (node.isLeaf()) {
				break;
			}

			if (!node.canBeEvaluated() && enableDefaultPolicy
					&& node.isFirstTime()) {
				// System.out.println(node.canBeEvaluated() + " " +
				// enableDefaultPolicy + " " + node.isFirstTime() + depth);
				break;

			}

			if (depth >= treePolicyMaxTreeDepth && node.canBeEvaluated()) {

				break;
			}
		}
		List<Double> reward = null;
		if (!node.canBeEvaluated() && node.isFirstTime() && enableDefaultPolicy) {

			reward = node.evaluateDefaultPolicy();
			node.setFirstTime(false);

		} else {
			reward = node.evaluate();
		}

		backpropagator.backpropagate(nodes, reward);

		return false;

	}

	@SuppressWarnings("unused")
	private N descend(N node) {
		// System.out.println("????");
		N preferedChild = null;
		// System.out.println(node.getChildren());
		if (((MCTSNode) node.getChildren().get(0)).getType() == NodeType.STOCHASTIC) {
			preferedChild = chanceNodeSelector.selectChild(node);
		} else if (((MCTSNode) node.getChildren().get(0)).getType() == NodeType.ADVERSARIAL) {
			preferedChild = adversarialNodeSelector.selectChild(node);
		} else {
			preferedChild = deterministicNodeSelector.selectChild(node);
		}

		// System.out.println(node);
		return preferedChild;

	}

}
