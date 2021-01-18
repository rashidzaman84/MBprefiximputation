package org.processmining.prefiximputation.modelbased.plugins;

import java.net.InetAddress;

import org.processmining.framework.annotations.AuthoredType;
//import org.processmining.streamconformance.local.model.LocalModelStructure;
import org.processmining.prefiximputation.modelbased.models.LocalModelStructure;

@AuthoredType(
	typeName = "Local Online Conformance Configuration",
	author = "Andrea Burattin",
	email = "",
	affiliation = "TUe")
public class LocalOnlineConformanceConfiguration {

	protected LocalModelStructure localModelStructure;
	protected int port;
	protected InetAddress address;
	protected int noMaxParallelInstances;
	protected String ccAlgoChoice;
	protected int imputationRevisitWindowSize;
	//protected Boolean imputationRevisitSelected=false;

	public LocalModelStructure getLocalModelStructure() {
		return localModelStructure;
	}
	
	public void setLocalModelStructure(LocalModelStructure localModelStructure) {
		this.localModelStructure = localModelStructure;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public int getNoMaxParallelInstances() {
		return noMaxParallelInstances;
	}
	
	public void setNoMaxParallelInstances(int noMaxParallelInstances) {
		this.noMaxParallelInstances = noMaxParallelInstances;
	}
	
	public String getCCAlgoChoice() {
		return ccAlgoChoice;
	}
	
	public void setCCAlgoChoice(String ccAlgoChoice) {
		this.ccAlgoChoice = ccAlgoChoice;
	}
	
	public int getImputationRevisitWindowSize() {
		return imputationRevisitWindowSize;
	}
	
	/*public Boolean getImputationRevisitSelected() {
		return this.imputationRevisitSelected;
	}*/
	
	public void setImputationRevisitWindowSize(Integer imputationRevisitWindowSize) {
		this.imputationRevisitWindowSize = imputationRevisitWindowSize;
		//this.imputationRevisitSelected = imputationRevisitWindowSize==0?false:true;
	}
}
