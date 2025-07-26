package org.processmining.prefiximputation.modelbased.completeforgetting;

/**
 * This class models the score of local online conformance
 * 
 * @author Andrea Burattin
 */
public class OnlineConformanceScore {

	private Double traceCost = null;
	
	private Double conformance = null;
	private Double completeness = null;
	private Double confidence = null;
	private boolean isLastObservedViolation = false;
	
	public Double getTraceCost() {
		return traceCost;
	}

	public void setTraceCost(Double traceCost) {
		this.traceCost = traceCost;
	}

	public Double getConformance() {
		return conformance;
	}

	public void setConformance(Double conformance) {
		this.conformance = conformance;
	}

	public Double getCompleteness() {
		return completeness;
	}

	public void setCompleteness(Double completeness) {
		this.completeness = completeness;
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	
	public boolean isLastObservedViolation() {
		return isLastObservedViolation;
	}
	
	public void isLastObservedViolation(boolean isLastObservedVilation) {
		this.isLastObservedViolation = isLastObservedVilation;
	}

	@Override
	public String toString() {
		return "conformance = " + getConformance() + "; completeness = " + getCompleteness() + "; confidence = " + getConfidence() + "; cost = " + getTraceCost();
	}
}
