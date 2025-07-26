package org.processmining.prefiximputation.inventory;

import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.onlineconformance.models.PartialAlignment.State;

public class StatesCalculator {
	
	public static <A extends PartialAlignment, S, L, T> int getStartState(final A previousAlignment) {
		/*if (previousAlignment == null || previousAlignment.size() < getParameters().getLookBackWindow()) {
			return getInitialState();
		} else {*/
		//System.out.println(previousAlignment.size());
		if (previousAlignment != null) {
			State<S, L, T> state = previousAlignment.getState();
			int i = 1;
			while (state.getParentState() != null /*&& state.getParentState().getParentMove() !=null*/ ) {
				state = state.getParentState();
				i++;
			}
			return i;
		}
			return -1;
		//}
	}
}
