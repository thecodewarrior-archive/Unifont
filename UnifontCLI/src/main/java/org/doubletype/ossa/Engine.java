/*
 * $Id: Engine.java,v 1.84 2004/12/27 04:56:03 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2003, e.e d3si9n $
 * $License: 
 * This source code is part of DoubleType.
 * DoubleType is a graphical typeface designer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, e.e d3si9n gives permission to
 * link the code of this program with any Java Platform that is available
 * to public with free of charge, including but not limited to
 * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
 * and distribute linked combinations including the two.
 * You must obey the GNU General Public License in all respects for all 
 * of the code used other than Java Platform. If you modify this file, 
 * you may extend this exception to your version of the file, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * $
 */

package org.doubletype.ossa;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.doubletype.ossa.action.*;
import org.doubletype.ossa.adapter.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.truetype.*;
import org.doubletype.ossa.xml.GlyphFactory;

/**
 * @author e.e
 */
public class Engine {
	// --------------------------------------------------------------	

	// used by findFile
	public static final int USER_CANCELLED = -1;
	public static final int FILE_NOT_FOUND = 0;
	public static final int FILE_FOUND = 1;
	
	// used by Find
	public static final int SEARCH_BY_EXAMPLE = 0;
	public static final int SEARCH_UNICODE = 1;
	public static final int SEARCH_JIS_CODE = 2;
	
	// public static final double k_fontSizes [] = {9, 10, 11, 12, 14, 18, 24, 36, 72};
	public static final int k_defaultPixelSize = 12;
	public static final int k_resolutions [] = {96, 72, 75, 100};
	public static final int k_defaultResolution = 96;
	public static final int k_zooms [] = {25, 50, 100};
	public static final int k_defaultZoom = 50;
	
	// --------------------------------------------------------------

	private static int s_em = 1024;
	private static Engine s_singleton = null;

	private static ResourceBundle s_bundle =
		ResourceBundle.getBundle(
			org.doubletype.ossa.GuiResource.class.getName());

	public static Engine getSingletonInstance() {
		if (s_singleton == null)
			s_singleton = new Engine();
		return s_singleton;
	}

	public static int getEm() {
		return TTPixelSize.getEm();
	}

	// --------------------------------------------------------------

	private GlyphDisplay m_display;
	private UiBridge m_ui;
	
	private TypefaceFile m_typeface;
	private GlyphFile m_root;
	private ActiveList m_actives;
	private ArrayList<ActionListener> m_listeners = new ArrayList<ActionListener>();
	
	private String m_foundFileName;
	private Clipboard m_clipboard;
	private JFileChooser m_gifChooser;
	private JFileChooser m_chooser;
	
	private Action m_deleteAction;
	private Action m_addPointAction;
	private Action m_toggleAction;
	private Action m_hintAction;
	private Action m_contourAction;
	private Action m_moduleAction;
	private Action m_includeAction;
	private Action m_selectNextAction;
	private Action m_roundAction;
	private Action m_propertyAction;
	private Action m_convertControlPointAction;
	private Action m_convertContourAction;

	private String m_msgAlreadyExists = " already exists!";
	private String m_msgNoTypeface = "no typeface";
	private String m_msgEmptyGlyphTitle = "glyph title is empty";
	private String m_msgGlyphName = "glyph name?";
	private String m_msgCircular = "circular include!";
	private String m_msgNoJis = "Charset ISO-2022-JP is not supported.\n"
		+ "Please include charsets.jar in classpath.";

	// --------------------------------------------------------------

	private Engine() {
		GlyphFactory.setFactory(new EGlyphFactory());
		
		m_display = new GlyphDisplay(this);

		m_typeface = null;
		m_root = null;

		m_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		m_actives = ActiveList.getSingletonInstance();
		
		initActions();
	}
	
	private void initActions() {
	    m_deleteAction = new AbstractAction() {
	        public void actionPerformed(ActionEvent a_event) {
	            delete();
	        }
	        
	        public String toString() {
	            return "deleteObject";
	        }
	    };
	    m_deleteAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("remove.gif")));
	    
	    m_addPointAction = new AbstractAction() {
			public void actionPerformed(ActionEvent a_event) {
				m_root.addPoint();
				fireAction();
			}
		        
			public String toString() {
				return "addPoint";
			}
		};
		m_addPointAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("point.gif")));
		
		m_toggleAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
				m_root.toggleOnOff();
				fireAction();
		    }
		    
		    public String toString() {
		        return "togglePoint";
		    }
		};
		
		m_hintAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
				m_root.addHint(m_display.getPpem());
				fireAction();
		    }
		    
		    public String toString() {
		        return "addHint";
		    }
		};
		m_hintAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("hint.gif")));

		m_moduleAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        m_root.addModule(EModuleInvoke.create());
				fireAction();
		    }
		    
		    public String toString() {
		        return "createModule";
		    }
		};
		m_moduleAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("module.gif")));
		
		m_contourAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        m_root.addContour(EContour.createAt(new Point2D.Double()));
				fireAction();
		    }
		    
		    public String toString() {
		        return "createContour";
		    }
		};
		m_contourAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("contour.gif")));
		
		
		m_includeAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        addInclude();
		    }
		    
		    public String toString() {
		        return "createInclude";
		    }
		};
		m_includeAction.putValue(Action.SMALL_ICON,
	            new ImageIcon(getClass().getResource("include.gif")));
		
		m_selectNextAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        selectNext();
		    }
		    
		    public String toString() {
		        return "selectNext";
		    }
		};
		
		m_roundAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        m_root.toggleRounded();
		        fireAction();
		    }
		    
		    public String toString() {
		        return "toggleGridfit";
		    }
		};
		
		m_propertyAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        showPropertyDialog();
		    }
		    
		    public String toString() {
		        return "properties";
		    }
		};
		
		m_convertControlPointAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        m_root.convertControlPoint();
		        fireAction();
		    }
		    
		    public String toString() {
		        return "convertControlPoint";
		    } 
		};
		
		m_convertContourAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent a_event) {
		        m_root.convertContour();
		        fireAction();
		    }
		    
		    public String toString() {
		        return "convertContour";
		    }
		};
	}

	public Action [] buildCommands() {
		ArrayList actions = buildCommandsArrayList();
		Action [] retval = new Action[actions.size()];
		int i;
		for (i = 0; i < actions.size(); i++) {
			retval[i] = (Action) actions.get(i);
		} // for i
	    
		return retval;
	}
	
	private ArrayList buildCommandsArrayList() {
		ArrayList retval = new ArrayList();
	    
		if (m_root == null) {
			return retval;	
		} // if
		
		if (m_actives.hasActiveContour()) {
		    retval.add(m_convertContourAction);
		}
	    
		if (m_actives.hasActiveControlPoint()) {
		    EControlPoint controlPoint = m_actives.getActiveControlPoint();
		    retval.add(m_convertControlPointAction);
		}
		
		if (m_actives.hasActivePoint()) {
		    EContourPoint point = m_actives.getActivePoint();
		    
		    retval.add(m_toggleAction);
		    
		    if (!point.isRounded()) {
		        retval.add(m_hintAction);
		    } // if
		    
			if (!point.hasHintForCurrentPpem()) {
			    retval.add(m_roundAction);
			} // if
		} // if
		
		if (m_actives.size() > 0) {
		    // retval.add(m_propertyAction);
		    retval.add(m_deleteAction);
		} // if
		
		if (m_actives.hasActivePoint()) {
		    retval.add(m_addPointAction);
		} // if
		
		if (!GlyphAction.isPointVisible()) {
			retval.add(m_moduleAction);
			retval.add(m_contourAction);
			retval.add(m_includeAction);
		} // if
		
		return retval;
	}
	
	public void localize(ResourceBundle a_bundle) {
		m_msgAlreadyExists = a_bundle.getString("msgAlreadyExists");
		m_msgNoTypeface = a_bundle.getString("msgNoTypeface");
		m_msgEmptyGlyphTitle = a_bundle.getString("msgEmptyGlyphTitle");
		m_msgGlyphName = a_bundle.getString("msgGlyphName");
		m_msgCircular = a_bundle.getString("msgCircular");
	}
	
	private void showPropertyDialog() {
	    if (m_actives.size() != 1) {
	        return;
	    } // if
	    
	    m_ui.showPropertyDialog(m_actives.get(0));
	    fireAction();
	}
	
	public void setUi(UiBridge a_ui) {
	    m_ui = a_ui;
	}
	
	public GlyphDisplay getDisplay() {
		return m_display;
	}
	
	public void selectNext() {
	    if (m_root == null) {
	        return;
	    } // if
	    
	    m_root.selectNext();
	    fireAction();
	}
	
	public void delete() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if
		
		m_root.remove();
			
		fireAction();
	}

	public void cutToClipboard() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if
		
		copyToClipboard();
		delete();
		
		fireAction();
	}

	public void copyToClipboard() {
		if (m_root == null)
			return;
		if (!m_actives.hasSelected()) {
			return;
		} // if

		String s = m_actives.getSelectedAsString();
		if (s.equals("")) {
			return;
		} // if

		StringSelection selection = new StringSelection(s);

		try {
			m_clipboard.setContents(selection, selection);
		} catch (Exception e) {
			e.printStackTrace();
		} // try-catch

		fireAction();
	}

	public void pasteFromClipboard() {
		if (m_root == null)
			return;
		Transferable content = null;
		String s = "";

		try {
			content = m_clipboard.getContents(this);
			if (content == null)
				return;
			s = (String) content.getTransferData(DataFlavor.stringFlavor);
			if (s.equals("")) {
				return;
			} // if
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} // try-catch

		try {
			m_root.addObjectFromClipboard(s);
		} catch (GlyphFile.CircularIncludeException e) {
			JOptionPane.showMessageDialog(null, m_msgCircular);
		}

		fireAction();
	}

	public void undo() {
		if (m_root == null)
			return;

		m_root.undo();
		fireAction();
	}

	public void redo() {
		if (m_root == null)
			return;

		m_root.redo();
		fireAction();
	}

	public void setAdvanceWidth(int a_value) {
		if (m_root == null)
			return;

		m_root.setAdvanceWidth(a_value);
		fireAction();
	}
	
	public void moveLeft() {
	    move(new Point2D.Double(-1, 0));
	}
	
	public void moveUp() {
	   move(new Point2D.Double(0, 1)); 
	}
	
	public void moveDown() {
	    move(new Point2D.Double(0, -1));
	}
	
	public void moveRight() {
	    move(new Point2D.Double(1, 0));
	}
	
	private void move(Point2D a_delta) {
	    if (m_root == null) {
	        return;
	    } // if
	    
	    m_root.move(a_delta);
	}

	public void buildNewTypeface(String a_name, File a_dir) {
		if (a_name == null || a_name.equals("")) {
			return;
		} // if
		
		TypefaceFile typeface = new TypefaceFile(a_name, a_dir);
		typeface.setAuthor("no body");
		DateFormat format = new SimpleDateFormat("yyyy");
		typeface.setCopyrightYear(format.format(new Date()));
		typeface.setFontFamilyName(a_name);
		typeface.setSubFamily("Regular");
		typeface.addCodePage(TTCodePage.US_ASCII.toString());
		typeface.addCodePage(TTCodePage.Latin_1.toString());

		setTypeface(typeface);
	}

	public void addDefaultGlyphs() {
		m_typeface.addRequiredGlyphs();
		m_typeface.addBasicLatinGlyphs();

		fireAction();
	}

	public void openTypeface() {
	    if (m_chooser == null) {
	        m_chooser = new JFileChooser(new File(AppSettings.getLastTypefaceDir()));
	    } // if
	    
	    m_chooser.setFileFilter(new TypefaceFileFilter());
		int returnVal = m_chooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		} // if
		
		AppSettings.setLastTypefaceDir(m_chooser.getSelectedFile().toString());
		openTypeface(m_chooser.getSelectedFile());
	}
	
	private void openTypeface(File a_file) {
	    setTypeface(new TypefaceFile(a_file));
	    
	    if (m_typeface.addRequiredGlyphs()) {
	    	fireAction();
	    } // if
	}
	
	public void setTypeface(TypefaceFile a_typeface) {
		m_typeface = a_typeface;
		fireAction();
	}

	public void changeUnicode(long a_unicode) {
		if (m_typeface == null || m_root == null) {
			return;
		} // if

		m_typeface.setGlyphUnicode(m_root, a_unicode);
		fireAction();
	}
	
	public int findFile(long a_unicode) {
		if (m_typeface == null)
			return USER_CANCELLED;
		
		m_foundFileName = m_typeface.unicodeToFileName(a_unicode);
		if (m_foundFileName != null) {
			return FILE_FOUND;
		} // if

		return FILE_NOT_FOUND;
	}
	
	public String getFoundFileName() {
		return m_foundFileName;
	}

	public GlyphFile createNewGlyph() {
		if (m_typeface == null) {
			JOptionPane.showMessageDialog(null, m_msgNoTypeface);
			return null;
		} // if

		String name = JOptionPane.showInputDialog(m_msgGlyphName);
		if (name == null || name.equals("")) {
			return null;
		} // if

		File file = GlyphFile.createFileName(m_typeface.getGlyphPath(), name);
		if (file.exists()) {
			JOptionPane.showMessageDialog(
				null,
				file.toString() + m_msgAlreadyExists);
			return null;
		} // if

		GlyphFile glyphFile = new GlyphFile(m_typeface.getGlyphPath(), name);
		addGlyphToTypeface(glyphFile);
		return glyphFile;
	}

	public GlyphFile addExistingGlyph() {
		if (m_typeface == null) {
			JOptionPane.showMessageDialog(null, m_msgNoTypeface);
			return null;
		} // if

		JFileChooser chooser = new JFileChooser(getGlyphPath());
		chooser.setFileFilter(new GlyphFileFilter());

		int returnVal = chooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		} // if

		String fileName = chooser.getSelectedFile().getName().toString();

		ModuleManager manager = ModuleManager.getSingletonInstance();
		GlyphFile glyphFile = manager.getReloadedGlyphFile(fileName);
		addGlyphToTypeface(glyphFile);
		return glyphFile;
	}

	/**
	 * Create glyph out of given unicode, and add it to the typeface.
	 * @param a_unicode
	 */
	public GlyphFile addNewGlyph(long a_unicode) {
		GlyphFile retval;

		retval = m_typeface.createGlyph(a_unicode);
		addGlyphToTypeface(retval);

		return retval;
	}

	public void checkUnicodeBlock(long a_unicode) {
		TTUnicodeRange range = TTUnicodeRange.of(a_unicode);
		if (range == null)
			return;

		if (m_typeface.containsUnicodeRange(range.toString()))
			return;

		// int result = JOptionPane.showConfirmDialog(null,
		// 	"Add " + block.toString() + " block?",
		//	"Unicode block",
		//	JOptionPane.YES_NO_OPTION);

		m_typeface.addUnicodeRange(range.toString());
	}

	private void addGlyphToTypeface(GlyphFile a_file) {
		m_typeface.addGlyph(a_file);
		m_typeface.saveGlyphFile();

		setRoot(a_file);
	}

	public GlyphFile openGlyphFile(String a_fileName) {
		ModuleManager manager = ModuleManager.getSingletonInstance();
		GlyphFile retval = manager.getReloadedGlyphFile(a_fileName);
		setRoot(retval);

		return retval;
	}

	public void removeGlyphFromTypeface(String a_fileName) {
		if (m_typeface == null)
			return;

		m_typeface.removeGlyph(a_fileName);
		fireAction();
	}

	public Font buildTrueType(boolean a_isDebug) {
		Font retval = null;

		if (m_typeface == null)
			return retval;

		String msgSaveChange = getBundle().getString("msgSaveChange");

		if (DrawFrame.hasUnsavedChange()) {
			int result =
				JOptionPane.showConfirmDialog(
					null,
					msgSaveChange,
					"Unsaved change",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				DrawFrame.saveAll();
			} // if
		} // if

		try {
			m_typeface.buildTTF(a_isDebug);
			retval = m_typeface.getFont();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString());
		} // try-catch

		return retval;
	}

	public void saveGlyph() {
		if (m_root == null)
			return;

		if (m_root.getGlyphTitle().equals("")) {
			JOptionPane.showMessageDialog(null, m_msgEmptyGlyphTitle);
			return;
		} // if

		m_root.saveGlyphFile();
		fireAction();
	}

	public TypefaceFile getTypeface() {
		return m_typeface;
	}

	public File getGlyphPath() {
		return m_typeface.getGlyphPath();
	}
	
	/**
	 * top level display method
	 * @param g graphic context
	 * @param a_file glyph to diplay
	 */
	public void display(Graphics2D g, GlyphFile a_file) {
		m_display.display(g, a_file, m_typeface);
	}

	public GlyphFile getRoot() {
		return m_root;
	}

	public void setRoot(GlyphFile a_file) {
		m_root = a_file;
		fireAction();
	}

	public void addActionListener(ActionListener a_listener) {
		m_listeners.add(a_listener);
	}

	public void fireAction() {
		ActionEvent e = new ActionEvent(this, Event.ACTION_EVENT, "foo");
		
		for (ActionListener listener: m_listeners) {
			listener.actionPerformed(e);
		} // for listener	
	}

	public ResourceBundle getBundle() {
		return s_bundle;
	}

	public void setBundle(ResourceBundle a_bundle) {
		s_bundle = a_bundle;
	}

	public String includeFileName() {
		String retval = "";

		JFileChooser chooser = new JFileChooser(getGlyphPath());
		chooser.setFileFilter(new GlyphFileFilter());

		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			retval = chooser.getSelectedFile().getName().toString();
		} // if

		return retval;
	}

	public void addCodePage(String a_codePage) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.addCodePage(a_codePage);
		fireAction();
	}

	public void removeCodePage(String a_codePage) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.removeCodePage(a_codePage);
		fireAction();
	}

	public void setAuthor(String a_value) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setAuthor(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setCopyrightYear(String a_value) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setCopyrightYear(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setFontFamilyName(String a_value) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setFontFamilyName(a_value);
		fireAction();
	}

	public void setTypefaceLicense(String a_value) {
		if (m_typeface == null) {
			return;
		} // if

		m_typeface.setLicense(a_value);
		m_typeface.saveGlyphFile();
		fireAction();
	}

	public void setBaseline(double a_value) {
		if (m_typeface == null) {
			return;
		} // if
		
		double min = m_typeface.getBottomSideBearing();
		double max = m_typeface.getMeanline();
				
		if (a_value < min) {
			a_value = min;
		} // if
		
		if (a_value > max) {
			a_value = max;
		} // if
		
		try {
			m_typeface.setDescender(a_value - min);
			m_typeface.setAscender(m_typeface.getEm()
				- m_typeface.getTopSideBearing() - a_value);
			m_typeface.setXHeight(max - a_value);
		}
		catch (OutOfRangeException e) {
			e.printStackTrace();	
		} // try-catch
		
		fireAction();
	}
	
	public void setMeanline(double a_value) {
		if (m_typeface == null) {
			return;
		} // if
		
		double min = m_typeface.getBaseline();
		double max = m_typeface.getEm()
			- m_typeface.getTopSideBearing();
				
		if (a_value < min) {
			a_value = min;
		} // if
		
		if (a_value > max) {
			a_value = max;
		} // if
		
		try {
			m_typeface.setXHeight(a_value - min);
		}
		catch (OutOfRangeException e) {
			e.printStackTrace();	
		} // try-catch
		
		fireAction();
	}

	public void mousePressed(MouseEvent a_event) {
		GlyphAction action = GlyphAction.getAction();
		action.setDisplay(m_display);
		
		Point2D logical = m_display.toLogical(a_event.getPoint());
		boolean isControl = ((a_event.getModifiersEx()
			& KeyEvent.CTRL_DOWN_MASK) != 0);
		
		if (a_event.getButton() == MouseEvent.BUTTON3) {
			if (!GlyphAction.isPointVisible()) {
			    return;
			} // if

			m_root.hit(logical, isControl, true);			
			m_toggleAction.actionPerformed(null);
			
			return;
		} else if (a_event.getButton() != MouseEvent.BUTTON1) {
			return;
		} // if
		
		if (a_event.getClickCount() > 1) {
			fireAction();
			return;
		} // if
		
		action.mousePressed(a_event, m_root);

		fireAction();
	}
	
	private void addInclude() {
		String fileName = includeFileName();
		if (fileName.equals("")) {
			return;
		} // if
			
		try {
			m_root.addInclude(fileName);
		} catch (
			GlyphFile
				.CircularIncludeException e) {
			JOptionPane.showMessageDialog(null, m_msgCircular);
		} // try-catch
		fireAction();
	}

	public void mouseDragged(MouseEvent a_event) {
		GlyphAction action = GlyphAction.getAction();
		action.setDisplay(m_display);
		action.mouseDragged(a_event, m_root);
		fireAction();
	}

	public void mouseReleased(MouseEvent a_event) {
		GlyphAction action = GlyphAction.getAction();
		action.setDisplay(m_display);
		action.mouseReleased(a_event, m_root);
		fireAction();
	}
	
	public void setAction(String a_key) {		
		GlyphAction.setAction(GlyphAction.forKey(a_key));		
		m_actives.unselectAll();
		fireAction();
	}

	public void keyPressed(KeyEvent a_event) {
		if (a_event.getModifiers() == 0) {
		    if (a_event.getKeyCode() == KeyEvent.VK_TAB) {
		    
		        m_selectNextAction.actionPerformed(null);
		    } // if
		} else if (a_event.getModifiers() == KeyEvent.SHIFT_MASK) {
		    
		} // if

		fireAction();
	}
	
	private JFileChooser createImageChooser() {
	    JFileChooser retval;
	    
	    retval = new JFileChooser(new File(AppSettings.getLastTypefaceDir()));
	    retval.setFileFilter(new javax.swing.filechooser.FileFilter() {	
			public boolean accept(File a_file) {
				if (a_file.isDirectory())
					return true;
				
				String s = a_file.toString().toLowerCase();
				if (s.endsWith(".gif")
				        || s.endsWith(".jpg")
				        || s.endsWith(".jpeg")
				        || s.endsWith(".png"))
					return true;
		        
				return false;
			}
		
			//The description of this filter
			public String getDescription() {
				return "Image Files";
			}
		});
	    
	    return retval; 
	}
	
	public void openBackgroundImage() {
		if (m_typeface == null) {
			return;
		} // if
		
		if (m_gifChooser == null) {
		    m_gifChooser = createImageChooser();
		} // if
		
		int returnVal = m_gifChooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		} // if

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		try {
			Image image = toolkit.createImage(m_gifChooser.getSelectedFile().toURL());		
			ImageSizer sizer = new ImageSizer(image);
			Dimension size = sizer.getImageSize();
			setBackgroundImage(image, (int) size.getHeight());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setBackgroundImage(Image a_image, int a_height) {
		m_display.setBackground(a_image, a_height, m_typeface);
		fireAction();	
	}
	
	public void setShowBackground(boolean a_value) {
		m_display.setShowBackground(a_value);
		fireAction();
	}
	
	public ArrayList<TTPixelSize> getPixelSizes() {
		return TTPixelSize.getList();
	}
	
	class MyTreeListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent a_event) {
			TreePath path = a_event.getPath();
			Object obj = path.getLastPathComponent();
			String s = obj.toString();

			TreePath parent = path.getParentPath();
			if (parent != null) {
				obj = parent.getLastPathComponent();
				s = obj.toString() + "->" + s;
			} // if

			// JOptionPane.showMessageDialog(null, s);
		}
	}
	
}
