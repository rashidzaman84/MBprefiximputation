package org.processmining.prefiximputation.inventory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.streamconformance.regions.utils.GUICustomUtils;


/**
 * Panel for the configuration of the network for the streaming. Can be one page of a wizard.
 *
 * @author Andrea Burattin
 */
public class NetworkConfiguration extends JPanel {

	private static final long serialVersionUID = -7669190410170225552L;
	private JTextField fieldPort;
	private JTextField fieldAddress;
	private JComboBox ccAlgoChoice;
	private JComboBox imputationRevisitWindowSize;
	//private JComboBox test;
	//private JTextField ccAlgoChoice;
	private String[] algoChoices = {"Prefix Alignment", "Behavioral Profiles" };
	private Integer[] imputationRevisitWindowSizeChoices = {0,3,4,5,6,7,8,9,10};
	/*enum ccAlgos {
		  PREFIXALIGNMENT,
		  BEHAVIORALPROFILES
		}*/

	/**
	 * Basic class constructor
	 */
	public NetworkConfiguration() {
		initComponents();
	}

	/**
	 * Method to get the address set by the user
	 *
	 * @return the address
	 * @throws UnknownHostException
	 */
	public InetAddress getAddress() throws UnknownHostException {
		return InetAddress.getByName(fieldAddress.getText());
	}
	
	public String getCCAlgoChoice() {
		return ccAlgoChoice.getSelectedItem().toString();
	}
	
	public String getImputationRevisitWindowSize() {
		return imputationRevisitWindowSize.getSelectedItem().toString();
	}

	/**
	 * Method to get the port set by the user
	 *
	 * @return the port
	 */
	public int getPort() {
		return Integer.parseInt(fieldPort.getText());
	}

	/*
	 * Graphical components initializer
	 */
	private void initComponents() {
		
		// swing factory
		//SlickerFactory factory = SlickerFactory.instance();
		//test = factory.createComboBox(imputationRevisitWindowSizeChoices);
		//test.setSelectedIndex(0);
		fieldPort = GUICustomUtils.prepareIntegerField(1234);
		fieldAddress = GUICustomUtils.prepareIPField("127.0.0.1");
		//ccAlgoChoice = GUICustomUtils.prepareTextField("BEHAVIORALPROFILES");
		ccAlgoChoice = new JComboBox(algoChoices);
		ccAlgoChoice.setSelectedIndex(0);
		imputationRevisitWindowSize = new JComboBox(imputationRevisitWindowSizeChoices);
		imputationRevisitWindowSize.setSelectedIndex(0);
		GridBagConstraints c = new GridBagConstraints();

		setOpaque(false);
		setLayout(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 15, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(GUICustomUtils.prepareLabel("Use this form for the configuration of " +
				"the stream data source and Conformance Checking technique."), c);

		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(GUICustomUtils.prepareLabel("Stream source port:"), c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 5, 5, 0);
		add(GUICustomUtils.wrapInRoundedPanel(fieldPort), c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 0, 5, 0);
		add(GUICustomUtils.prepareLabel("Stream source address:"), c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(0, 5, 5, 0);
		add(GUICustomUtils.wrapInRoundedPanel(fieldAddress), c);
		
		/*c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 0, 5, 0);
		add(GUICustomUtils.prepareLabel("CC Algorithm Choice:"), c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(0, 5, 5, 0);
		add(GUICustomUtils.wrapInRoundedPanel(ccAlgoChoice), c);*/
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 0, 5, 0);
		add(GUICustomUtils.prepareLabel("CC Algorithm Choice:"), c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(0, 5, 5, 0);
		add(GUICustomUtils.wrapInRoundedPanel(ccAlgoChoice), c);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(5, 0, 5, 0);
		add(GUICustomUtils.prepareLabel("IDR Window:"), c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(0, 5, 5, 0);
		add(GUICustomUtils.wrapInRoundedPanel(imputationRevisitWindowSize), c);
	}
	
	public static void main(String[] args) throws Exception {
		NetworkConfiguration nc= new NetworkConfiguration();
		
		System.out.println("??");
		
	}
}
