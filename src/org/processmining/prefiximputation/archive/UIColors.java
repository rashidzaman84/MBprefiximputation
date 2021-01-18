package org.processmining.prefiximputation.archive;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * This class contains the basic colors of the plugins
 * 
 * @author Andrea Burattin
 */
public class UIColors {

	public static final Color darkGray = new Color(28, 28, 28);
	public static final Color gray = new Color(82, 82, 82);
	public static final Color lightGray = new Color(119, 119, 119);
	public static final Color lightLightGray = new Color(194, 194, 194);
	
	public static final ImageIcon loadingIcon = new ImageIcon(UIColors.class.getResource("/loader.gif"));
}
