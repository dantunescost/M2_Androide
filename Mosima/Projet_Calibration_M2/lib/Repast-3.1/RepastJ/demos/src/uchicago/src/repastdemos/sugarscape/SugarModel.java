/*$$
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Chicago nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/
package uchicago.src.repastdemos.sugarscape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import uchicago.src.reflector.BooleanPropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.OpenStats;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.SimUtilities;
import cern.jet.random.Uniform;

/**
 * An partial implementation of the Sugar Scape simulation from _Growing
 * Artificial Societies_ by Epstein, Joshua M and Axtell, Robert. This
 * implements rules G, R, and M from chapter II.
 * 
 * The source code is heavily annotated as example of a simulation built using
 * the Repast toolkit. See the html API documentation for the details of the
 * framework objects.
 * 
 * @author Nick Collier adapted from Nelson Minar's Swarm code
 * @version $Revision: 1.1 $ $Date: 2005/08/12 20:04:53 $
 */

// Simulation models should extend SimModelImpl which does some basic setup
// and allows a controller easy access to a simulations initial parameters.
public class SugarModel extends SimModelImpl {

	// Every model must have a schedule
	public Schedule schedule;

	// A list of all the agents. Convenient for any method that
	// iterates through a list of agents. A least one list like this is common
	// to most simulations. See below for examples of its use.
	public ArrayList agentList = new ArrayList();

	// A list of SugarAgents who have been "birthed" and should be introduced
	// to the simulation in the next turn
	public Vector birthList = new Vector();

	// The Sugar Space - the agent's environment consisting of variable
	// amounts of sugar arranged on a grid
	public SugarSpace space;

	// The 2 dimensional torusr on which the agents act.
	public Object2DTorus agentGrid;

	// A queue that tracks dead agents.
	public Vector reaperQueue = new Vector();

	// A DataRecorder allows data generated by a simulation to be written
	// to a file
	//public DataRecorder recorder;

	// initial starting parameters of the model
	public int numAgents = 400;

	public int maxMetabolism = 4;

	public int maxVision = 6;

	public int maxInitialSugar = 25;

	public int minInitialSugar = 5;

	public int minDeathAge = 60;

	public int maxDeathAge = 100;

	public boolean replace = true;

	public boolean write = false;

	/**
	 * @return Returns the write.
	 */
	public boolean isWrite() {
		return write;
	}

	/**
	 * @param write
	 *            The write to set.
	 */
	public void setWrite(boolean write) {
		this.write = write;
	}

	// The surface on which the agents and their environment are displayed
	public DisplaySurface dsurf;

	// A graph that plots sequence
	public OpenSequenceGraph graph;

	// A histogram
	public OpenHistogram bar;

	// These inner classes are used by all the data collection methods - i.e.
	// DataRecorder, SequenceGraph and the Histogram.

	// Creates a DataSource and Sequnce object that returns the average vision
	// for
	// all the agents in the agent list. It gets the individual agents value
	// by calling the method getMetabolism on the agent.
	class AvgAgentVision implements DataSource, Sequence {
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			int size = agentList.size();
			int val = 0;
			for (int i = 0; i < agentList.size(); i++) {
				SugarAgent agent = (SugarAgent) agentList.get(i);
				val += agent.getVision();
			}

			return val / size;
		}
	};

	// like above but for Metabolism
	class AvgAgentMetabolism implements DataSource, Sequence {
		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			int size = agentList.size();
			int val = 0;
			for (int i = 0; i < agentList.size(); i++) {
				SugarAgent agent = (SugarAgent) agentList.get(i);
				val += agent.getMetabolism();
			}

			return val / size;
		}
	};

	// All simulations should have a no argument constructor - this is empty and
	// thus not strictly necessary
	public SugarModel() {
		BooleanPropertyDescriptor bd = new BooleanPropertyDescriptor(
				"Replacement", false);
		descriptors.put("Replacement", bd);

	}

	// The typical way to code a simulation is to divide up the creation of the
	// simulation into three methods - buildModel(), buildDisplay(),
	// buildSchedule(). This division is not strictly necessary, but it does
	// make the creation conceptually clearer.

	// The buildModel() method is responsible for creating those parts of the
	// simulation that represent what is being modeled. Consequently, the agents
	// and their environment are typically created here together with any
	// optional data collection objects. Of course, this method may call other
	// methods to help build the model.
	public void buildModel() {

		// creates the sugar space using the values in sugarspace.pgm as
		// the amount of sugar (see SugarSpace.java for more).

		space = new SugarSpace(
				"/uchicago/src/repastdemos/sugarscape/sugarspace.pgm");
		agentGrid = new Object2DTorus(space.getXSize(), space.getYSize());

		for (int i = 0; i < numAgents; i++) {
			addNewAgent();
		}

		// create a new DataRecorder and write the data to ./sugar_data.txt
		/*if (write) {
			recorder = new DataRecorder("./sugar_data.txt", this);

			// adds an ObjectDataSource to the recorder as a source of data.
			// See above for the AvgAgentMetabolism class
			recorder.addObjectDataSource("Avg. Metabolism",
					new AvgAgentMetabolism());

			// we could also create an AvgDataSource that would accomplish the
			// same thing, but much more slowly.
			//recorder.createAverageDataSource("Avg. Metabolism", agentList,
			// "getMetabolism");

			// Like the above but for agent's vision
			recorder.addObjectDataSource("Avg. Vision", new AvgAgentVision());

			// Adds a data source that counts the agents all the agents alive
			// in the simulation. It does this by getting the size of the
			// agentList.

			// first we make an inner class that implements the DataSource
			// interface
			class AgentCountClass implements DataSource {
				public Object execute() {
					int i = agentList.size();
					return new Integer(i);
				}
			}
			;

			recorder.addObjectDataSource("AgentCount", new AgentCountClass());
		}*/
		// we could also have done the above with an anonymous inner class
		// recorder.addObjectDataSource("Agent Count", new DataSource() {
		//  public Object execute() {
		//    return new Integer(agentList.size());
		//  }
		// });
	}

	// buildDisplay() builds those parts of the simulation that have to do with
	// displaying the simulation to a user.
	public void buildDisplay() {

		// Create a display to display the agentGrid on the screen.
		// This will display any Drawable contained within the Object2DTorus.
		// In a non-batch simulation the agents in a simulation will typically
		// implement the Drawable interface.
		Object2DDisplay agentDisplay = new Object2DDisplay(agentGrid);

		// Rather than have the agentDisplay iterating over each object in the
		// Object2DTorus, give the display a list of Drawables to display.
		agentDisplay.setObjectList(agentList);

		// Maps 4 shades of yellow to integers 1 - 4, and white to 0.
		ColorMap map = new ColorMap();
		map.mapColor(4, new Color(255, 255, 0));
		map.mapColor(3, new Color(255, 255, 255 / 3));
		map.mapColor(2, new Color(255, 255, 255 / 2));
		map.mapColor(1, new Color(255, 255, (int) (255 / 1.2)));
		map.mapColor(0, Color.white);

		// SugarSpace.getCurrentSugar returns an Object2DTorus containing an
		// Integer of value (0 - 4) at each x, y coordinate. Using the map
		// created
		// above this Value2DDisplay will display will display each of these
		// Integers as a shade of yellow (or white, if the Integer == 0).
		Value2DDisplay sugarDisplay = new Value2DDisplay(space
				.getCurrentSugar(), map);

		// DisplaySurface does the actual drawing of the various displays on the
		// user's screen. Displays are displayed in the order they are added.
		// Our dsurf object is created in the setup() method

		dsurf.addDisplayableProbeable(sugarDisplay, "Sugar Space");
		dsurf.addDisplayableProbeable(agentDisplay, "Agents");

		// Adds two Sequence to be displayed by the graph, these Squences will
		// return the agents' average metabolism, and average vision. For how
		// the
		// averages are computed see the two inner classes, AvgAgentMetabolism,
		// and AvgAgentVision above.

		graph.addSequence("Avg. Metabolism", new AvgAgentMetabolism());
		graph.addSequence("Avg. Vision", new AvgAgentVision());

		// we could also achieve the same effect by using the
		// createAverageSequence
		// method. However, this is much slower, esp. with a list of agents.

		//graph.createAverageSequence("Avg. Metabolism", agentList,
		// "getMetabolism");
		//graph.createAverageSequence("Avg. Vision", agentList, "getVision");

		graph.setAxisTitles("Time", "Avgs");
		graph.setXRange(0, 50);
		graph.setYRange(0, 10);
		graph.setSize(400, 250);

		// Histograms get their data from histogramItems. This creates an item
		// called wealth that gets its data by calling getSugar on each agent
		// in the agent list.
		bar.createHistogramItem("Wealth", agentList, "getSugar", -1, 2);
		bar.setYRange(0, 100.0);

		// This causes the display surface to update the display whenever the
		// simulation run is paused or ended. The DisplaySurface now listens
		// for SimEvents produced by the SugarModel
		addSimEventListener(dsurf);
	}

	// buildSchedule builds the schedule that changes the simulations state.
	// Under this scheme, a simulation is a state machine where all transitions
	// between states are the result of actions initiated by a schedule.
	public void buildSchedule() {

		// this is a static schedule (no need to add or replace actions) so
		// we can create an inner class that extends from basic action. The
		// execute method of the inner class will execute all the methods that
		// we wish to schedule on the agents and the environment.

		// this inner class could also be done anonymously with the
		// same result, but doing it this way is clearer for those
		// with less experience with Java.

		// we could also move everything in the execute method of the
		// SugarRunner
		// into a method of this SugarModel class, a step() method for example,
		// and then schedule that as follows:
		//
		// schedule.scheduleActionBeginning(0, this, "step");

		class SugarRunner extends BasicAction {
			public void execute() {
				// call the birthAgents methods on this model
				birthAgents();
				// call the shuffleAgents method on this model
				shuffleAgents();

				// call the step method on each SugarAgent in the agentList
				for (int i = 0; i < agentList.size(); i++) {
					SugarAgent agent = (SugarAgent) agentList.get(i);
					agent.step();
				}
				space.updateSugar();

				// should call update display after the things that it displays
				// have changed their state. Otherwise, displays won't be in
				// synch with what it displays at end or at pause.
				dsurf.updateDisplay();
				graph.step();
				bar.step();
				/*if (write)
					recorder.record();*/
				reapAgents();
			}
		}
		;

		// the schedule has been created in setup()
		schedule.scheduleActionBeginning(0, new SugarRunner());

		// On a pause in the simulation run, call the writeToFile method
		// on the recorder object. (Writes the data collected by the recorder
		// to a file.
		/*if (write)
			schedule.scheduleActionAtPause(recorder, "writeToFile");*/
		//schedule.scheduleActionAtPause(graph, "writeToFile");

		// When the simulation run ends, call the writeToFile method
		// on the recorder object. (Writes the data collected by the recorder
		// to a file.
		/*if (write)
			schedule.scheduleActionAtEnd(recorder, "writeToFile");*/
		//schedule.scheduleActionAtEnd(graph, "writeToFile");
	}

	// Randomize the order of the object (the SugarAgents) in the agentList
	public void shuffleAgents() {
		SimUtilities.shuffle(agentList);
	}

	// Add a new agent.
	public void addNewAgent() {
		SugarAgent agent = new SugarAgent(space, this);
		int x, y;

		do {
			x = Uniform.staticNextIntFromTo(0, space.getXSize() - 1);
			y = Uniform.staticNextIntFromTo(0, space.getYSize() - 1);
		} while (agentGrid.getObjectAt(x, y) != null);

		agentGrid.putObjectAt(x, y, agent);
		agent.setXY(x, y);

		// Use the initial simulation parameters (maxMetabolism etc.) to
		// construct
		//the agents
		agent.setMetabolism(Uniform.staticNextIntFromTo(1, maxMetabolism));
		agent.setVision(Uniform.staticNextIntFromTo(1, maxVision));
		agent.setSugar(Uniform.staticNextIntFromTo(minInitialSugar,
				maxInitialSugar));
		agent.setMaxAge(Uniform.staticNextIntFromTo(minDeathAge, maxDeathAge));
		agentBirth(agent);
	}

	public void agentBirth(SugarAgent agent) {
		if (this.getTickCount() == 0) {
			agentList.add(agent);
		} else {
			birthList.add(agent);
		}
	}

	public void birthAgents() {
		agentList.addAll(birthList);
		birthList.clear();
	}

	// When an agent "dies" it is added to the reaperQueue
	public void agentDeath(SugarAgent agent) {
		reaperQueue.add(agent);
		if (replace) {
			addNewAgent();
		}
	}

	public SugarAgent getAgentAt(int x, int y) {
		return (SugarAgent) agentGrid.getObjectAt(x, y);
	}

	public void reapAgents() {
		ListIterator li = reaperQueue.listIterator();

		while (li.hasNext()) {
			SugarAgent agent = (SugarAgent) li.next();
			agentList.remove(agent);
			agentGrid.putObjectAt(agent.getX(), agent.getY(), null);
		}

		reaperQueue.clear();
	}

	public void moveAgent(SugarAgent agent, int x, int y) {
		agentGrid.putObjectAt(agent.getX(), agent.getY(), null);
		agentGrid.putObjectAt(x, y, agent);
		agent.setXY(agentGrid.xnorm(x), agentGrid.xnorm(y));
	}

	// When a simulation is started through SimInit, some BaseController is
	// created to control the running of that model. This BaseController calls
	// getInitParam() on the model and receives a list of initial parameters
	// that can be displayed for modification to the user. In order to display
	// the value of these parameters the controller determines if the model
	// has implemented get and set methods for that parameter. If so, the
	// controller calls the get method and displays the result to the user. A
	// similar process occurs when a model's initial starting parameters
	// are written to a data file.
	//
	// What this means is that if a user wants to display some initial starting
	// parameter and have this parameter be modifiable, the name of the
	// parameter
	// must be returned by the getInitParam() method and the model must contain
	// the appropriate get and set methods. For example, to do the above for
	// a parameter called NumAgents, "NumAgents" must be present in the array
	// return by getInitParam and the model must have a getNumAgents method and
	// a
	// setNumAgents method. The parameter name must match the method names minus
	// the "get" and "set". So "numAgents" and getNumAgents won't work, but
	// "NumAgents" and getNumAgents will.
	//
	// The following methods are an example of this pattern.
	public int getNumAgents() {
		return numAgents;
	}

	public void setNumAgents(int num) {
		numAgents = num;
	}

	public int getMaxMetabolism() {
		return maxMetabolism;
	}

	public void setMaxMetabolism(int maxMeta) {
		maxMetabolism = maxMeta;
	}

	public int getMaxVision() {
		return maxVision;
	}

	public void setMaxVision(int maxVis) {
		maxVision = maxVis;
	}

	public int getMaxInitialSugar() {
		return maxInitialSugar;
	}

	public void setMaxInitialSugar(int maxInitSugar) {
		maxInitialSugar = maxInitSugar;
	}

	public int getMinDeathAge() {
		return minDeathAge;
	}

	public void setMinDeathAge(int age) {
		minDeathAge = age;
	}

	public int getMaxDeathAge() {
		return maxDeathAge;
	}

	public void setMaxDeathAge(int age) {
		maxDeathAge = age;
	}

	public boolean getReplacement() {
		return replace;
	}

	public void setReplacement(boolean isReplaced) {
		replace = isReplaced;
	}

	public int getMinInitialSugar() {
		return minInitialSugar;
	}

	public void setMinInitialSugar(int minInitSugar) {
		minInitialSugar = minInitSugar;
	}

	public String[] getInitParam() {
		String[] params = { "MaxMetabolism", "MaxVision", "MaxInitialSugar",
				"MinInitialSugar", "Replacement", "NumAgents", "MaxDeathAge",
				"MinDeathAge" };
		return params;
	}

	// Every model must have begin() and setup() methods (required for
	// implementing
	// the SimModel inteface).

	// begin() should intialize the model for the start of a run. Consequently,
	// the build* methods are called here, and any displays are displayed.
	// build()
	// is called whenever the start button (or the step button if the run has
	// not yet started) is clicked.
	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();

		dsurf.display();
		graph.display();
		bar.display();
	}

	// setup() prepares the model for another run. Called whenver the setup
	// button
	// is clicked. Setup should set any objects that are created over the course
	// of the run to null, and dispose of any DisplaySurfaces or graphs. While
	// not strictly necessary this should some prevent memory leaks and calling
	// System.gc() helps too. The initial parameters should be set to whatever
	// defaults the user wants to see initially.
	public void setup() {
		schedule = null;
		agentList = new ArrayList();
		birthList = new Vector();
		space = null;
		agentGrid = null;
		reaperQueue = new Vector();

		if (dsurf != null)
			dsurf.dispose();
		dsurf = null;
		if (graph != null)
			graph.dispose();
		graph = null;

		if (bar != null)
			bar.dispose();
		bar = null;

		System.gc();

		// create a schedule with an interval of one.
		schedule = new Schedule(1);
		dsurf = new DisplaySurface(this, "Sugar Scape");

		// By registering a DisplaySurface, you are allowing Repast to automate
		// some tasks on the displays surface, such as making movies or taking
		// snapshots.
		registerDisplaySurface("Sugar Scape", dsurf);

		// creates a dynamic histogram of the distribution of sugar over the
		// agents.
		bar = new OpenHistogram("Agent Wealth Distribution", 10, 0, this);

		// Create a sequence graph called Agent Attributes and allow the data
		// plotted by this graph to be written to the file ./graph_data.txt
		// in comma delimited format.
		graph = new OpenSequenceGraph("Agent Attributes", this,
				"./graph_data.txt", OpenStats.CSV);

		this.registerMediaProducer("Hist", bar);
		this.registerMediaProducer("Plot", graph);

		// agent properties
		numAgents = 400;
		maxMetabolism = 4;
		maxVision = 6;
		maxInitialSugar = 25;
		minInitialSugar = 5;
		minDeathAge = 60;
		maxDeathAge = 100;

		replace = true;
	}

	// a required method
	public Schedule getSchedule() {
		return schedule;
	}

	// a required method - displayed on the Controller toolbar.
	public String getName() {
		return "SugarScape";
	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		SugarModel model = new SugarModel();
		init.loadModel(model, "", false);
	}
}