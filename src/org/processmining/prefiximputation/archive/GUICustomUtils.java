package org.processmining.prefiximputation.archive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

/**
 * A collection of user interface utilities
 *
 * @author Andrea Burattin
 */
public class GUICustomUtils {

	public static void customizeScrollBard(JScrollPane scroll) {
		SlickerScrollBarUI vertical = new SlickerScrollBarUI(scroll.getVerticalScrollBar(), scroll.getBackground(), scroll.getBackground()
				.brighter(), scroll.getBackground().darker(), 3, 12);
		scroll.getVerticalScrollBar().setUI(vertical);

		SlickerScrollBarUI horizontal = new SlickerScrollBarUI(scroll.getHorizontalScrollBar(), scroll.getBackground(), scroll
				.getBackground().brighter(), scroll.getBackground().darker(), 3, 12);
		scroll.getHorizontalScrollBar().setUI(horizontal);
	}

	/**
	 * Method to convert a value in the interval [0, 1] into a black and white color, where 0 is white and 1 is black
	 *
	 * @param value
	 *            the value
	 * @return the color corresponding to the value
	 */
	public static Color fromWeightToBWColor(Double value) {
		value = (value > 1) ? 1.0 : value;
		int factor = (int) (255 * (1 - value));
		return new Color(factor, factor, factor);
	}

	/**
	 * Method that convert a give color (as "color-base") into another one that is obtained considering the value as a "weight" (values in
	 * [0, 1]). If the value is 0, the final color will be transparent; if the value is 1, the final color will be the "color-base"
	 *
	 * @param baseColor
	 *            the starting color
	 * @param value
	 *            the value
	 * @return the new color (with alpha)
	 */
	public static Color fromWeightToColor(Color baseColor, Double value) {
		value = (value > 1) ? 1.0 : (value < 0) ? 0.0 : value;
		Integer alpha = (int) (200 * value) + 54;
		return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
	}

	/**
	 * @param value
	 *            the value
	 * @return the new color (with alpha)
	 */
	public static Color fromWeightToColor(Double value) {
		value = (value > 1) ? 1.0 : (value < 0) ? 0.0 : value;
		value *= 100;
		int R = (int) ((255d * value) / 100d);
		int G = (int) ((255d * (100d - value)) / 100d);
		int B = 0;
		return new Color(R, G, B);
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted in order to be a frame title. The text will be inserted into a gray box.
	 *
	 * @param s
	 *            the string to be inserted into the label
	 * @return a component with the required widget
	 */
	public static JPanel prepareBorderedTitle(String s) {
		SlickerFactory factory = SlickerFactory.instance();
		JPanel title = factory.createRoundedPanel(15, new Color(40, 40, 40));
		title.setBounds(0, 0, 780, 40);
		JLabel labelTitle = factory.createLabel(s);
		labelTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		labelTitle.setForeground(Color.gray);
		labelTitle.setBounds(10, 5, 760, 30);
		title.setLayout(null);
		title.add(labelTitle);
		return title;
	}

	/**
	 * This method creates a new {@link JFormattedTextField} for receiving a double
	 *
	 * @param initialContent
	 *            the initial content of the text field
	 * @param minimum
	 *            the minimum value
	 * @param maximum
	 *            the maximum value
	 * @return the formatted text field
	 */
	public static JTextField prepareDoubleField(Double initialContent, Double minimum, Double maximum) {
		DecimalFormat df = new DecimalFormat("####.####");
		NumberFormatter nf = new NumberFormatter(df) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object stringToValue(String text) throws ParseException {
				if ("".equals(text)) {
					return null;
				}
				return super.stringToValue(text);
			}

			@Override
			public String valueToString(Object iv) throws ParseException {
				if ((iv == null) || (((Double) iv).doubleValue() == -1)) {
					return "";
				} else {
					return super.valueToString(iv);
				}
			}
		};
		nf.setMinimum(minimum);
		nf.setMaximum(maximum);
		nf.setValueClass(Double.class);

		JTextField tmp = new JFormattedTextField(nf);
		tmp.setColumns(8);
		tmp.setText(new Double(initialContent).toString());
		tmp.setBackground(Color.DARK_GRAY);
		tmp.setForeground(Color.LIGHT_GRAY);
		tmp.setCaretColor(Color.GRAY);
		tmp.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		return tmp;
	}

	/**
	 * This method creates a new {@link JFormattedTextField} for receiving integer (up to 5 digits) as input
	 *
	 * @param initialContent
	 *            the initial content of the text field
	 * @return the formatted text field
	 */
	public static JTextField prepareIntegerField(int initialContent) {
		DecimalFormat df = new DecimalFormat("#####");
		NumberFormatter nf = new NumberFormatter(df) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object stringToValue(String text) throws ParseException {
				if ("".equals(text)) {
					return null;
				}
				return super.stringToValue(text);
			}

			@Override
			public String valueToString(Object iv) throws ParseException {
				if ((iv == null) || (((Integer) iv).intValue() == -1)) {
					return "";
				} else {
					return super.valueToString(iv);
				}
			}
		};
		nf.setMinimum(0);
		nf.setMaximum(65534);
		nf.setValueClass(Integer.class);

		JTextField tmp = new JFormattedTextField(nf);
		tmp.setColumns(8);
		tmp.setText(new Integer(initialContent).toString());
		tmp.setBackground(Color.DARK_GRAY);
		tmp.setForeground(Color.LIGHT_GRAY);
		tmp.setCaretColor(Color.GRAY);
		tmp.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		return tmp;
	}

	/**
	 * This method creates a new text field for receiving integer (up to 5 digits) as input
	 *
	 * @param initialContent
	 *            the initial content of the text field
	 * @return the formatted text field
	 */
	public static JTextField prepareIPField(String initialContent) {
		RegexFormatter ipmask = new RegexFormatter("\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}");
		ipmask.setOverwriteMode(false);

		JTextField tmp = new JFormattedTextField(ipmask);
		tmp.setColumns(8);
		tmp.setText(initialContent);
		tmp.setBackground(Color.DARK_GRAY);
		tmp.setForeground(Color.LIGHT_GRAY);
		tmp.setCaretColor(Color.GRAY);
		tmp.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		return tmp;
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted
	 *
	 * @param i
	 *            the string to be inserted into the label
	 * @return a component with the required widget
	 */
	public static JLabel prepareLabel(int i) {
		return prepareLabel(i, SwingConstants.LEFT, UIColors.darkGray);
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted
	 *
	 * @param i
	 *            the string to be inserted into the label
	 * @param alignment
	 *            the alignment
	 * @param foreground
	 *            the foreground color
	 * @return a component with the required widget
	 */
	public static JLabel prepareLabel(int i, int alignment, Color foreground) {
		Integer value = i;
		JLabel l = SlickerFactory.instance().createLabel(value.toString());
		l.setHorizontalAlignment(alignment);
		l.setForeground(new Color(40, 40, 40));
		return l;
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted
	 *
	 * @param s
	 *            the string to be inserted into the label
	 * @return a component with the required widget
	 */
	public static JLabel prepareLabel(String s) {
		return prepareLabel(s, SwingConstants.LEFT, UIColors.darkGray);
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted
	 *
	 * @param s
	 *            the string to be inserted into the label
	 * @param alignment
	 *            the alignment
	 * @param foreground
	 *            the foreground color
	 * @return a component with the required widget
	 */
	public static JLabel prepareLabel(String s, int alignment, Color foreground) {
		JLabel l = SlickerFactory.instance().createLabel(s);
		l.setHorizontalAlignment(alignment);
		l.setForeground(foreground);
		return l;
	}

	/**
	 * This method creates a new text area
	 *
	 * @param initialContent
	 *            the initial content of the text field
	 * @return the text field
	 */
	public static JTextArea prepareTextArea(String initialContent) {
		JTextArea tmp = new JTextArea(initialContent);
		tmp.setColumns(8);
		tmp.setRows(4);
		tmp.setText(initialContent);
		tmp.setBackground(Color.DARK_GRAY);
		tmp.setForeground(Color.LIGHT_GRAY);
		tmp.setCaretColor(Color.GRAY);
		tmp.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		return tmp;
	}

	/**
	 * This method creates a new text field
	 *
	 * @param initialContent
	 *            the initial content of the text field
	 * @return the text field
	 */
	public static JTextField prepareTextField(String initialContent) {
		JTextField tmp = new JTextField(initialContent);
		tmp.setColumns(8);
		tmp.setText(initialContent);
		tmp.setBackground(Color.DARK_GRAY);
		tmp.setForeground(Color.LIGHT_GRAY);
		tmp.setCaretColor(Color.GRAY);
		tmp.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		return tmp;
	}

	/**
	 * This method creates a new {@link JLabel} correctly formatted in order to be a frame title
	 *
	 * @param s
	 *            the string to be inserted into the label
	 * @return a component with the required widget
	 */
	public static JLabel prepareTitle(String s) {
		JLabel labelTitle = SlickerFactory.instance().createLabel(s);
		labelTitle.setFont(labelTitle.getFont().deriveFont(16f));
		// labelTitle.setForeground(Color.white);
		labelTitle.setBorder(new EmptyBorder(5, 10, 10, 10));
		return labelTitle;
	}

	/**
	 * This method to wrap a given component into a rounded panel. Example usage:
	 *
	 * <pre>
	 * JTextField a = GUIUtils.prepareIntegerField(100);
	 * mainComponent.add(GUIUtils.wrapInRoundedPanel(a), BorderLayout.CENTER);
	 * </pre>
	 *
	 * @param c
	 *            the component to wrap
	 * @return a {@link JPanel} containing the given component
	 */
	public static JPanel wrapInRoundedPanel(JComponent c) {
		JPanel wrapper = new RoundedPanel(15);
		wrapper.setBackground(c.getBackground());
		wrapper.setLayout(new BorderLayout());
		wrapper.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		wrapper.add(c, BorderLayout.CENTER);
		return wrapper;
	}
}