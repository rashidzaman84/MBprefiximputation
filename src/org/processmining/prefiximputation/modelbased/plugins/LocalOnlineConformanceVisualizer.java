package org.processmining.prefiximputation.modelbased.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.operationalsupport.xml.OSXMLConverter;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalConformanceStatus;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalConformanceTracker;
import org.processmining.prefiximputation.modelbased.completeforgetting.OnlineConformanceScore;
import org.processmining.streamconformance.local.plugin.gui.LocalConformanceListEntry;
import org.processmining.streamconformance.local.plugin.gui.LocalConformanceListEntryRenderer;
import org.processmining.streamconformance.regions.gui.widgets.ChartVisualizer;
import org.processmining.streamconformance.regions.utils.GUICustomUtils;
//import org.processmining.prefiximputation.inventory.GUICustomUtils;
import org.processmining.streamconformance.regions.utils.UIColors;

import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class LocalOnlineConformanceVisualizer extends JPanel {

	private static final long serialVersionUID = -4781114837188265638L;

	protected LocalOnlineConformanceConfiguration configuration;
	protected UIPluginContext context;
	protected OSXMLConverter converter = new OSXMLConverter();
	protected Runtime runtime = Runtime.getRuntime();

	private int port;
	private InetAddress address;
	private JLabel status;
	private long chartsUpdateInteval = 5000;
	private long listUpdateInteval = 10000;
	private ChartVisualizer tracesObservedChart;
	private ChartVisualizer memoryUsedChart;
	private ChartVisualizer eventsPerSecondChart;
	private ChartVisualizer errorsObservedChart;

	private Date startTime;
	private long eventsReceived = 0;
	private long errorsObserved = 0;

	private SlickerTabbedPane tabs = SlickerFactory.instance().createTabbedPane("Online conformance", UIColors.lightGray,
			Color.white,
			Color.lightGray);
	private JButton start;
	private JButton stop;
	private DefaultListModel<LocalConformanceListEntry> dlm;
	private JList<LocalConformanceListEntry> list;
	private JComboBox<String> sort;

	private LocalConformanceTracker tracker;
	private Map<String, Comparator<String>> sortersAvailable;

	@Plugin(
			name = "Local Online Conformance Checker",
			parameterLabels = { "" },
			returnLabels = { "" },
			returnTypes = { JComponent.class },
			userAccessible = true)
	@UITopiaVariant(author = "Andrea Burattin", email = "", affiliation = "DTU")
	@Visualizer(name = "Online Conformance Checker")
	public JComponent visualize(UIPluginContext context, LocalOnlineConformanceConfiguration configuration) {

		this.configuration = configuration;
		this.context = context;
		this.port = configuration.getPort();
		this.address = configuration.getAddress();
		//this.tracker = new LocalConformanceTracker(configuration.getLocalModelStructure(), configuration.getNoMaxParallelInstances());
		this.tracker = new LocalConformanceTracker(configuration.getLocalModelStructure(), configuration.getNoMaxParallelInstances());                     //CC w.r.t. algo choice
		this.dlm = new DefaultListModel<LocalConformanceListEntry>();
		this.list = new JList<LocalConformanceListEntry>(dlm);
		this.list.setCellRenderer(new LocalConformanceListEntryRenderer<>());
		this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.list.setBackground(UIColors.lightLightGray);

		this.sortersAvailable = new HashMap<String, Comparator<String>>();
		this.sortersAvailable.put("lower conformance first", new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LocalConformanceStatus cs1 = tracker.get(o1);
				LocalConformanceStatus cs2 = tracker.get(o2);
				if (cs1 == null || cs2 == null) {
					return 0;
				}
				return cs1.getCurrentScore().getConformance().compareTo(cs2.getCurrentScore().getConformance());
			}
		});
		this.sortersAvailable.put("lower completeness first", new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LocalConformanceStatus cs1 = tracker.get(o1);
				LocalConformanceStatus cs2 = tracker.get(o2);
				if (cs1 == null || cs2 == null) {
					return 0;
				}
				return cs1.getCurrentScore().getCompleteness().compareTo(cs2.getCurrentScore().getCompleteness());
			}
		});
		this.sortersAvailable.put("lower conformance+completeness first", new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LocalConformanceStatus cs1 = tracker.get(o1);
				LocalConformanceStatus cs2 = tracker.get(o2);
				if (cs1 == null || cs2 == null) {
					return 0;
				}
				Double cs1sum = cs1.getCurrentScore().getConformance() + cs1.getCurrentScore().getCompleteness();
				Double cs2sum = cs2.getCurrentScore().getConformance() + cs2.getCurrentScore().getCompleteness();
				return cs1sum.compareTo(cs2sum);
			}
		});
		this.sortersAvailable.put("recently updated", new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				LocalConformanceStatus cs1 = tracker.get(o1);
				LocalConformanceStatus cs2 = tracker.get(o2);
				if (cs1 == null || cs2 == null) {
					return 0;
				}
				return cs1.getLastUpdate().compareTo(cs2.getLastUpdate());
			}
		});
		System.out.println("sdsadsad");
		initComponents();

		return this;
	}

	/*
	 * Graphical components initializer
	 */
	@SuppressWarnings("unchecked")
	private void initComponents() {

		setBackground(new Color(40, 40, 40));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// main area
		// ---------------------------------------------------------------------

		// list of ongoing cases
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBackground(UIColors.lightLightGray);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		GUICustomUtils.customizeScrollBard(scrollPane);

		JPanel tracesArea = SlickerFactory.instance().createRoundedPanel(15, UIColors.lightLightGray);
		tracesArea.setLayout(new BorderLayout(0, 10));
		tracesArea.add(scrollPane, BorderLayout.CENTER);

		// statistics panel
		tracesObservedChart = new ChartVisualizer(Color.WHITE, "Traces in memory over time", false, 25);
		memoryUsedChart = new ChartVisualizer(Color.WHITE, "Memory usage over time (MB)", false, 25);
		eventsPerSecondChart = new ChartVisualizer(Color.WHITE, "Events / second over time", false, 50);
		errorsObservedChart = new ChartVisualizer(Color.RED, "Errors observed at each time fragment (" + (chartsUpdateInteval / 1000)
				+ " secs)", false, 50);

		JPanel memStatsArea = new JPanel();
		memStatsArea.setOpaque(false);
		memStatsArea.setLayout(new BoxLayout(memStatsArea, BoxLayout.X_AXIS));
		memStatsArea.add(tracesObservedChart);
		memStatsArea.add(memoryUsedChart);

		JPanel statsArea = SlickerFactory.instance().createRoundedPanel(15, UIColors.lightLightGray);
		statsArea.setLayout(new BoxLayout(statsArea, BoxLayout.Y_AXIS));
		statsArea.add(errorsObservedChart);
		statsArea.add(eventsPerSecondChart);
		statsArea.add(memStatsArea);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tracesArea, statsArea);
		split.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		split.setOpaque(false);
		split.setDividerSize(10);
		split.setDividerLocation(400);

		split.setUI(new BasicSplitPaneUI() {
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {
					private static final long serialVersionUID = 9030573699042574015L;

					@Override
					public void paint(Graphics g) {
						g.setColor(new Color(0, 0, 0, 0));
						g.fillRect(0, 0, getSize().width, getSize().height);
						super.paint(g);
					}

					@Override
					public void setBorder(Border b) {
					}
				};
			}
		});

		// sort / player controls
		// ---------------------------------------------------------------------
		start = SlickerFactory.instance().createButton("Start conformance checker");
		stop = SlickerFactory.instance().createButton("Stop");
		stop.setEnabled(false);
		start.addActionListener(new StartListener());
		stop.addActionListener(new StopListener());

		status = new JLabel("");

		sort = SlickerFactory.instance().createComboBox(sortersAvailable.keySet().toArray());
		JPanel sortContainer = new JPanel();
		sortContainer.setOpaque(false);
		sortContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));
		sortContainer.add(GUICustomUtils.prepareLabel("Sort traces by:"));
		sortContainer.add(sort);

		JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonContainer.setOpaque(false);
		buttonContainer.add(start);
		buttonContainer.add(stop);
		buttonContainer.add(Box.createHorizontalStrut(5));
		buttonContainer.add(status);

		JPanel sideContainer = new JPanel();
		sideContainer.setOpaque(false);
		sideContainer.setLayout(new BorderLayout());
		sideContainer.add(buttonContainer, BorderLayout.WEST);
		sideContainer.add(sortContainer, BorderLayout.EAST);

		// add everything to the main panel
		// ---------------------------------------------------------------------
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BorderLayout(0, 5));
		mainPanel.add(sideContainer, BorderLayout.SOUTH);
		mainPanel.add(split, BorderLayout.CENTER);

		// add the tab and the tab list
		tabs.addTab("Overall Information", mainPanel);

		setLayout(new BorderLayout(0, 5));
		add(tabs, BorderLayout.CENTER);
	}

	public void updateCharts() {
		double secondsSinceStart = (System.currentTimeMillis() - startTime.getTime()) / 1000d;

		double eventsPerSecond = eventsReceived / secondsSinceStart;
		double tracesObserved = tracker.getHandledCases().size();
		double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

		// add observations to the charts
		eventsPerSecondChart.addObservation(eventsPerSecond);
		tracesObservedChart.addObservation(tracesObserved);
		errorsObservedChart.addObservation(errorsObserved);
		memoryUsedChart.addObservation(memoryUsed);

		/*System.out.println(new Date().toString() + "\t" + eventsPerSecond + "\t" + tracesObserved + "\t" + errorsObserved + "\t"
				+ memoryUsed);*/

		// reset of the errors counter for this time fragment
		errorsObserved = 0;
	}

	public void updateTraces() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				list.clearSelection();
				dlm.clear();
				List<String> handled = new LinkedList<String>(tracker.getHandledCases());
				Collections.sort(handled, sortersAvailable.get(sort.getSelectedItem()));
				for (String caseId : handled) {
					LocalConformanceStatus cs = tracker.get(caseId);
					if (cs != null) {
						; //replaced the following statement with this ;
						//dlm.addElement(new LocalConformanceListEntry(caseId, cs.getLastUpdate(), cs.getCurrentScore()));
						//System.out.println("For" + caseId + " the getlastupdate is: " + cs.getLastUpdate() + " and cgetcurrentscore is: " + cs.getCurrentScore());
					}
				}
			}

		});
	}

	/*
	 * Listener for the start button
	 */
	private class StartListener implements ActionListener {

		private boolean execute = true;
		private Thread listener;
		//private Thread listener2;
		private Timer timerCharts;
		private Timer timerList;

		@Override
		public void actionPerformed(ActionEvent e) {
			stop.setEnabled(true);
			start.setEnabled(false);
			/*System.out.println("Memory before gc: " + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
			runtime.gc();
			System.out.println("Memory before gc: " + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));*/
			resetPlayerThread();
			
			execute = true;
			startTime = new Date();
			//listener2 = new listener();
			listener.start();
			
			//listener2.start();
			
		}

		public void resetPlayerThread() {
			listener = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Socket s = new Socket(address, port);

						status.setText("Stream started");
						status.setIcon(UIColors.loadingIcon);

						BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
						String str = "";
						XTrace t;
						errorsObserved = 0;
						while (execute && (str = br.readLine()) != null) {
							status.setText((eventsReceived++) + " events observed");
							
							// extract the observed components
							t = (XTrace) converter.fromXML(str);
							
							String caseId = XConceptExtension.instance().extractName(t);
							String newEventName = XConceptExtension.instance().extractName(t.get(0));
					//>>		System.out.println("\n-------------------New event observed on the Stream------------");
							//System.out.println("the string read from the port: " + str.toString());
							//System.out.println("in trace form: " + t.toString());
					//>>		System.out.println("For Case: "+ caseId + " the event observed is: " + newEventName);
							//System.out.println("End------------");
							//Check if the caseId exists and if the event received is the first event or one of the first events
							//if both conditions are false, then IMPUTE the trace by selecting the value in the shortestPrefixes
							//through the above event as key. OR in other case, can be checked inside the following tracker
							//method. In the tracker, we also need to replace LinkedList phiposophy to remove farthest updated
							//cases with a new philosophy.
							

							// replay the extracted event
							//OnlineConformanceScore returned = tracker.replayEvent(caseId, newEventName, t);
							OnlineConformanceScore returned = tracker.replayEvent(caseId, newEventName);
							// check if the replay was an error
							if (returned.isLastObservedViolation()) {
								errorsObserved++;
							}
						}
						br.close();
						s.close();

					} catch (IOException e) {
						JOptionPane.showMessageDialog(
								LocalOnlineConformanceVisualizer.this,
								e.getLocalizedMessage(),
								"Network Exception",
								JOptionPane.ERROR_MESSAGE);
					}

					stop.setEnabled(false);
					start.setEnabled(true);
					status.setText("Stream completed");
					status.setIcon(null);
				}
			});

			timerCharts = new Timer();
			timerList = new Timer();

			timerCharts.schedule(new TimerTask() {
				@Override
				public void run() {
					if (execute) {
						updateCharts();
					} else {
						timerCharts.cancel();
						timerCharts.purge();
					}
				}
			}, 1000, chartsUpdateInteval);

			timerList.schedule(new TimerTask() {
				@Override
				public void run() {
					if (execute) {
						updateTraces();
					} else {
						timerList.cancel();
						timerList.purge();
					}
				}
			}, 1000, listUpdateInteval);
		}

		public void stop() {
			execute = false;

			try {
				listener.join();
			} catch (InterruptedException e) {
			}

			status.setText("Analyzer stopped");
			status.setIcon(null);
		}
	}

	/*
	 * Listener for the stop button
	 */
	private class StopListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			((StartListener) start.getActionListeners()[0]).stop();

			stop.setEnabled(false);
			start.setEnabled(true);
		}
	}
}
