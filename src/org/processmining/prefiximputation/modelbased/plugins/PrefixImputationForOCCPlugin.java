package org.processmining.prefiximputation.modelbased.plugins;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.streamconformance.regions.gui.panels.NetworkConfiguration;
import org.processmining.prefiximputation.inventory.NetworkConfiguration;
//import org.processmining.streamconformance.local.model.LocalModelStructure;
import org.processmining.prefiximputation.modelbased.models.LocalModelStructure;
import org.processmining.processtree.ProcessTree;

@Plugin(
	name = "Online Conformance - With Model-Based Prefix Imputation",
	returnLabels = { "Online conformance checking - with Model-based Prefix Imputation" },
	returnTypes = { LocalOnlineConformanceConfiguration.class },
	parameterLabels = {
			"Model", "Marking"/*,"Process Tree"*/
	},
	categories = PluginCategory.Analytics,
	help = "This plugin computes the conformance of a given model with respect to an event streams.",
	userAccessible = true)
public class PrefixImputationForOCCPlugin {

	@PluginVariant(requiredParameterLabels = { 0, 1/*, 2*/ })
	@UITopiaVariant(
		author = "R. Zaman",
		email = "r.zaman@tue.nl",
		affiliation = "TUe")
	public LocalOnlineConformanceConfiguration plugin(UIPluginContext context, Petrinet net, Marking initMarking, ProcessTree tree) throws Exception {
		// prepare the configuration panels
		//System.out.println("Here");
		NetworkConfiguration configNetwork = new NetworkConfiguration();
		//add the NDTransitionCOnfiguration here
		
		List<Pair<String, JPanel>> configurations = new LinkedList<Pair<String, JPanel>>();
		configurations.add(new Pair<String, JPanel>("Network Setup", configNetwork));
		//we need to add the screen for collecting non-deterministic regions
		//configurations.add(new Pair<String, JPanel>("ND Setup", configNetwork));
		
		// ask the user for the configuration parameters
		InteractionResult result = InteractionResult.NEXT;
		int currentStep = 0;
		int nofSteps = configurations.size();
		boolean configurationOngoing = true;
		while (configurationOngoing && currentStep < nofSteps) {
			Pair<String, JPanel> config = configurations.get(currentStep);
			result = context.showWizard(
					config.getFirst(),
					currentStep == 0,
					currentStep == nofSteps - 1,
					config.getSecond());

			switch (result) {
			case NEXT:
				currentStep++;
				break;
			case PREV:
				currentStep--;
				break;
			case FINISHED:
				configurationOngoing = false;
				break;
			case CANCEL:
				return null;
			default:
				configurationOngoing = false;
				break;
			}
		}
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		
		// wizard is over now, collect the configuration
		
		// create the local structure
		LocalOnlineConformanceConfiguration locc = new LocalOnlineConformanceConfiguration();
		locc.setAddress(configNetwork.getAddress());
		locc.setPort(configNetwork.getPort());
		locc.setCCAlgoChoice(configNetwork.getCCAlgoChoice());
		locc.setImputationRevisitWindowSize(Integer.parseInt(configNetwork.getImputationRevisitWindowSize()));
		//System.out.println("mid");
		locc.setLocalModelStructure(new LocalModelStructure(context, net, initMarking, /*tree,*/ locc.getCCAlgoChoice(), locc.getImputationRevisitWindowSize())); //the local model structure
		//will now contain (i)the list of non-deterministic and deterministic transitions, (ii) the mechanism to find
		//the shortest-path through Dijkstra Algorithm
		//System.out.println("now");
		locc.setNoMaxParallelInstances(10); // TODO: hardcoded for the moment
		
		return locc;
	}
}
