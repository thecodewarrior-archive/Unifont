/*
 * $Id: Desktop.java,v 1.53 2004/10/10 06:04:09 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2003-2004, e.e d3si9n $
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
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.*;
import org.doubletype.ossa.action.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.property.*;
import org.doubletype.ossa.truetype.TTPixelSize;
import org.doubletype.ossa.adapter.*;

/**
 *
 * @author  e.e
 */
public class Desktop extends javax.swing.JFrame implements UiBridge {
	private static final long serialVersionUID = 1L;
	
	static public final String k_icon = "dt_small.png";
	
	/**
	 * @param args the command line arguments
	 */
	static public void main(String args[]) {
	    // http://forum.java.sun.com/thread.jsp?thread=220083&forum=57&message=1962469
		// System.setProperty("swing.disableFileChooserSpeedFix", "true");
	    
		try {		    
		    UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
			Engine engine = Engine.getSingletonInstance();
			Desktop desktop = new Desktop(engine);
			desktop.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		} // try-catch
	}
	
	
//	-------------------------------------------------------------------

	Engine m_engine;
	PropertyFrame m_propertyFrame;
	TypefacePropertyDialog m_typefacePropertyDlg;
	FontFrame m_fontFrame;
	PropertyDialog m_propertyDialogs;
	
//	-------------------------------------------------------------------
	
    /** Creates new form Desktop */
    public Desktop(Engine a_engine) {
        m_engine = a_engine;
        m_engine.setUi(this);
        
		m_engine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engineActionPerformed(e);		
			}
		});
        
		System.out.println("initComponents.");
		initComponents();
		m_desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		System.out.println("setJMenuBar.");
		setJMenuBar(m_menuBar);
		buildFontSizeMenus();
		buildZoomMenus();
		buildCommandMenu();
		buildPropertyDialogs();
		enableTab();
		
		beforeShow();
    }
    
    public void beforeShow() {
		System.out.println("setIconImage.");
		setIconImage(new ImageIcon(
				getClass().getResource(Desktop.k_icon)).getImage());
		
		System.out.println("Property frame.");
		m_propertyFrame = new PropertyFrame(m_engine, this);
		addFrame(m_propertyFrame);
		
		System.out.println("Font frame.");
		m_fontFrame = new FontFrame(this, m_engine);
		
		System.out.println("Adding Font frame to Desktop.");
		addFrame(m_fontFrame);
		
		System.out.println("Typeface dlg.");
		m_typefacePropertyDlg = new TypefacePropertyDialog(m_engine, this);
		
		localize(m_engine.getBundle());
		engineActionPerformed(null);
		
		System.out.println("Maximizing the window.");
		setExtendedState(Frame.MAXIMIZED_BOTH); 
    }
    
    public void showPropertyDialog(GlyphObject a_object) {
        m_propertyDialogs.showDialog(a_object);
    }
    
    public void engineActionPerformed(ActionEvent a_event) {
        reenableUI();
        updateSaveMenu(m_engine.getBundle());
        buildCommandMenu();
    }
    
    private void enableTab() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {  
		    public boolean dispatchKeyEvent(KeyEvent e) {
		        if (e.getKeyCode() != KeyEvent.VK_TAB) {
		            return false;
		        } // if
		        
		        Component component = e.getComponent();
		        while (component != null) {
		           component = component.getParent();
		           if (component == null) {
		               return false;
		           } // if
		           
			       if (component instanceof DrawFrame) {
			            break;
			       } // if
		        } // while
		        
		        if (e.getID() == KeyEvent.KEY_PRESSED) {
		            m_engine.selectNext();
		            return true;
		        } // if
		        
		        return false;
		    }
		});
    }
    
    private void buildPropertyDialogs() {
        m_propertyDialogs = new PointPropertyDialog(this, null);
    }
    
    private void buildCommandMenu() {
    	int i;

		m_pnlCommands.removeAll();    	
		m_pnlCommands.revalidate();
    	
    	Action [] commands = m_engine.buildCommands();
    	for (i = 0; i < commands.length; i++) {
        	JButton btn = new JButton(commands[i]);
        	btn.setForeground(new java.awt.Color(0, 51, 204));
        	btn.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 1)));
        	btn.setBorderPainted(false);
        	btn.setContentAreaFilled(false);
        	btn.setDoubleBuffered(true);
        	
        	m_pnlCommands.add(btn);
    	} // for i
        
    	localizeCommands(m_engine.getBundle());
    	
    	m_pnlCommands.revalidate();
    	m_pnlCommands.repaint();	
    }
	
    private void localizeCommands(ResourceBundle a_bundle) {
        for (int i = 0; i < m_pnlCommands.getComponentCount(); i++) {
            Component component = m_pnlCommands.getComponent(i);
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                Action action = button.getAction();
                button.setText(a_bundle.getString(action.toString()));
            } // if
        } // for i
    }
    
    
    private void reenableUI() {
        boolean isTypefaceExists = (m_engine.getTypeface() != null);
        m_mnuView.setEnabled(isTypefaceExists);
        m_mnuFontSize.setEnabled(isTypefaceExists);
        m_mnuTypeface.setEnabled(isTypefaceExists);
        m_mniAddNewGlyph.setEnabled(isTypefaceExists);
        m_mniAddExistingGlyph.setEnabled(isTypefaceExists);
        
        m_btnSearch.setEnabled(isTypefaceExists);
        m_txtSearch.setEnabled(isTypefaceExists);
        m_cmbSearch.setEnabled(isTypefaceExists);
        
        
        boolean isGlyphExists = (m_engine.getRoot() != null);
        m_mnuEdit.setEnabled(isGlyphExists);
        m_mniSaveGlyph.setEnabled(isGlyphExists);
        m_mniOpenBackground.setEnabled(isGlyphExists);
    }
    
    private void buildFontSizeMenus() {
    	m_mnuFontSize.removeAll();
		ButtonGroup group = new ButtonGroup();		
		for (TTPixelSize pixelSize: m_engine.getPixelSizes()) {
			JMenuItem menuItem = new JRadioButtonMenuItem(
					new MyFontSizeAction(pixelSize));
			if (Engine.k_defaultPixelSize == pixelSize.getPixel()) {
				menuItem.setSelected(true);
			} // if
			
			group.add(menuItem);
			m_mnuFontSize.add(menuItem);
		} // for
    	
    }
    
    private void buildZoomMenus() {
        m_mnuZoom.removeAll();
		ButtonGroup group = new ButtonGroup();
    	
    	int i;
    	for (i = 0; i < Engine.k_zooms.length; i++) {
			int zoom = Engine.k_zooms[i];
			JMenuItem menuItem = new JRadioButtonMenuItem(
				new ZoomAction(zoom));
			
			if (zoom == Engine.k_defaultZoom) {
				menuItem.setSelected(true);
			} // if
			
			group.add(menuItem);
			m_mnuZoom.add(menuItem);
    	} // for i
    }
    
    private void updateTitle() {
    	if (m_engine.getTypeface() == null) {
    		setTitle("DoubleType");
    		return;
    	} // if
    	
    	String title = m_engine.getTypeface().getGlyphTitle();
    	setTitle(title + " - DoubleType");
    }
    
    public void addFrame(JInternalFrame a_frame) {
        System.out.println("setting visible");
		a_frame.setVisible(true);
		
		System.out.println("add to desktop");
		
		try {
		    Thread.sleep(100);
		} catch (InterruptedException  e) {
		    e.printStackTrace();
		}
		
		
		m_desktop.add(a_frame);
		try {
		    System.out.println("set selected");
		    
		    a_frame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
		    e.printStackTrace();
		} // try-catch
    }
    
    public void removeFrame(JInternalFrame a_frame) {
        m_desktop.remove(a_frame);
    }
    
    public void buildNewGlyphFile(long a_unicode) {    	
		GlyphFile file = m_engine.addNewGlyph(a_unicode);
		m_engine.checkUnicodeBlock(a_unicode);
		
		if (file != null) {
			openGlyphFile(file);
		} // if
    }
    
    // called by File->New->Glyph... menu
	private void addNewGlyph() {
		boolean isUnicodeKnown = (JOptionPane.showConfirmDialog(this,
				m_engine.getBundle().getString("msgHasUnicode"),
				"", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    	
		if (isUnicodeKnown) {
		    askUnicodeAndCreate();
		} else {
			GlyphFile file = m_engine.createNewGlyph();
		
			if (file != null) {
				openGlyphFile(file);	
			} // if
		} // if
	}
	
    public void askUnicodeAndCreate() {
        Long unicode = UnicodeBuilder.askUnicode();
        if (unicode == null) {
            return;
        } // if
        
        openGlyphFile(unicode.longValue());
    }
    
    public void openGlyphFile(long a_unicode) {
    	int result = m_engine.findFile(a_unicode);
    	
		if (result == Engine.FILE_FOUND) {
			openGlyphFile(m_engine.getFoundFileName());	
			return;
		} // if
		    	
		if (result == Engine.FILE_NOT_FOUND) {			
			buildNewGlyphFile(a_unicode);
		} // if	
    }
    
    /** Searches for the a_value provided and opens the glyph */
    public void searchAndOpen(String a_value, int a_option) {
        Long unicode = UnicodeBuilder.build(a_value, a_option);
        if (unicode == null) {
            return;
        } // if
        
        int result = m_engine.findFile(unicode.longValue());
		if (result == Engine.FILE_FOUND) {
			openGlyphFile(m_engine.getFoundFileName());	
			return;
		} // if
		
		if (result == Engine.FILE_NOT_FOUND) {
			result = JOptionPane.showConfirmDialog(this,
					m_engine.getBundle().getString("msgCreateNewGlyph"),
					"", JOptionPane.YES_NO_OPTION);	
			if (result != JOptionPane.YES_OPTION)
				return;
			
			buildNewGlyphFile(unicode.longValue());
		} // if	
    }
    
    public void openGlyphFile(GlyphFile a_glyphFile) {
    	openGlyphFile(a_glyphFile.getShortFileName());
    }
    
    public void openGlyphFile(String a_fileName) {
		ArrayList drawForms = DrawFrame.getForms();
		int i;
		for (i = 0; i < drawForms.size(); i++) {
			DrawFrame drawFrame = (DrawFrame) drawForms.get(i);
			if (drawFrame.getFile().getShortFileName().equals(
				a_fileName))
			{
				drawFrame.setVisible(true);
				try {
					drawFrame.setSelected(true);
				} catch (java.beans.PropertyVetoException e) {}

				return;
			} // if
		} // for i
    	
		GlyphFile glyphFile = m_engine.openGlyphFile(a_fileName);
		DrawFrame drawFrame = new DrawFrame(glyphFile, m_engine);
		addFrame(drawFrame);
    }
    
    private void updateSaveMenu(ResourceBundle a_bundle) {
        String strGlyph = a_bundle.getString("glyph");
        if (m_engine.getRoot() != null) {
            strGlyph = m_engine.getRoot().getShortFileName();
        } // if
        
        String s = a_bundle.getString("saveGlyph");
        m_mniSaveGlyph.setText(s.replaceAll("%0", strGlyph));
    }
    
	public void localize(ResourceBundle a_bundle) {
		// file menu
		m_mnuFile.setText(a_bundle.getString("file"));
		m_mnuNew.setText(a_bundle.getString("new"));
		m_mniNewTypeface.setText(a_bundle.getString("newTypeface"));
		m_mniAddNewGlyph.setText(a_bundle.getString("addNewGlyph"));
		m_mnuOpen.setText(a_bundle.getString("open"));
		m_mniOpenTypeface.setText(a_bundle.getString("openTypeface"));
		m_mniAddExistingGlyph.setText(a_bundle.getString("addExistingGlyph"));
		updateSaveMenu(a_bundle);
		m_mniExit.setText(a_bundle.getString("exit"));
		
		m_mnuEdit.setText(a_bundle.getString("edit"));
		m_mniUndo.setText(a_bundle.getString("undo"));
		m_mniRedo.setText(a_bundle.getString("redo"));
		m_mniCut.setText(a_bundle.getString("cut"));
		m_mniCopy.setText(a_bundle.getString("copy"));
		m_mniPaste.setText(a_bundle.getString("paste"));
		m_mniMoveLeft.setText(a_bundle.getString("moveLeft"));
		m_mniMoveUp.setText(a_bundle.getString("moveUp"));
		m_mniMoveDown.setText(a_bundle.getString("moveDown"));
		m_mniMoveRight.setText(a_bundle.getString("moveRight"));
		m_mniDelete.setText(a_bundle.getString("delete"));
		
		m_mnuView.setText(a_bundle.getString("view"));
		m_mnuZoom.setText(a_bundle.getString("zoom"));
		
		m_mnuFontSize.setText(a_bundle.getString("fontSize"));
		
		m_mnuHelp.setText(a_bundle.getString("help"));
		
		m_mnuTypeface.setText(a_bundle.getString("typeface"));
		m_mniBuild.setText(a_bundle.getString("build"));
		
		m_mniTypefaceSetting.setText(
			a_bundle.getString("typefaceSetting"));
		m_mniRelease.setText(a_bundle.getString("release"));
		m_mniOpenBackground.setText(a_bundle.getString("openBackground"));
		m_mniShowBackground.setText(a_bundle.getString("showBackground"));
		
		m_pnlCommands.setBorder(
				new javax.swing.border.TitledBorder(
				a_bundle.getString("task") + ":"));
		
		localizeToolbar(a_bundle);
		localizeCommands(a_bundle);
		localizeSearch(a_bundle);
		
		m_engine.localize(a_bundle);
		m_propertyFrame.localize(a_bundle);
		m_typefacePropertyDlg.localize(a_bundle);
		m_fontFrame.localize(a_bundle);
	}
	
	private void localizeSearch(ResourceBundle a_bundle) {
	    m_btnSearch.setText(a_bundle.getString("search"));
	    
	    if (UnicodeBuilder.isJisSupported()) {
		    m_cmbSearch.setModel(new javax.swing.DefaultComboBoxModel(
		            new String[] { 
		                    a_bundle.getString("example"), 
		                    a_bundle.getString("unicode"), 
		                    a_bundle.getString("jis") }));
	    } else {
		    m_cmbSearch.setModel(new javax.swing.DefaultComboBoxModel(
		            new String[] { 
		                    a_bundle.getString("example"), 
		                    a_bundle.getString("unicode")}));    
	    } // if-else
	}
	
	private void localizeToolbar(ResourceBundle a_bundle) {
		m_btnPreview.setText(a_bundle.getString("previewMenu"));
		m_btnPreview.setActionCommand(GlyphAction.PREVIEW_ACTION.getKey());
				
		m_btnPick.setText(a_bundle.getString("pickMenu"));
		m_btnPick.setActionCommand(GlyphAction.PICK_ACTION.getKey());
		
		m_btnWallpaper.setText(a_bundle.getString("wallpaper"));
		m_btnWallpaper.setActionCommand(GlyphAction.WALLPAPER_ACTION.getKey());
		
		m_btnPickPoint.setText(a_bundle.getString("pickpoint"));
		m_btnPickPoint.setActionCommand(GlyphAction.PICK_POINT_ACTION.getKey());
	}
	
//	-------------------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        m_menuBar = new javax.swing.JMenuBar();
        m_mnuFile = new javax.swing.JMenu();
        m_mnuNew = new javax.swing.JMenu();
        m_mniNewTypeface = new javax.swing.JMenuItem();
        m_mniAddNewGlyph = new javax.swing.JMenuItem();
        m_mnuOpen = new javax.swing.JMenu();
        m_mniOpenTypeface = new javax.swing.JMenuItem();
        m_mniAddExistingGlyph = new javax.swing.JMenuItem();
        m_mniOpenBackground = new javax.swing.JMenuItem();
        m_sprOpen = new javax.swing.JSeparator();
        m_mniSaveGlyph = new javax.swing.JMenuItem();
        m_sprSave = new javax.swing.JSeparator();
        m_mniExit = new javax.swing.JMenuItem();
        m_mnuEdit = new javax.swing.JMenu();
        m_mniUndo = new javax.swing.JMenuItem();
        m_mniRedo = new javax.swing.JMenuItem();
        m_sprRedo = new javax.swing.JSeparator();
        m_mniCut = new javax.swing.JMenuItem();
        m_mniCopy = new javax.swing.JMenuItem();
        m_mniPaste = new javax.swing.JMenuItem();
        m_mniDelete = new javax.swing.JMenuItem();
        m_sprPaste = new javax.swing.JSeparator();
        m_mniMoveLeft = new javax.swing.JMenuItem();
        m_mniMoveUp = new javax.swing.JMenuItem();
        m_mniMoveDown = new javax.swing.JMenuItem();
        m_mniMoveRight = new javax.swing.JMenuItem();
        m_mnuView = new javax.swing.JMenu();
        m_mniShowBackground = new javax.swing.JCheckBoxMenuItem();
        m_mnuZoom = new javax.swing.JMenu();
        m_mnuFontSize = new javax.swing.JMenu();
        m_mnuTypeface = new javax.swing.JMenu();
        m_mniTypefaceSetting = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        m_mniBuild = new javax.swing.JMenuItem();
        m_mniRelease = new javax.swing.JMenuItem();
        m_mnuLanguage = new javax.swing.JMenu();
        m_mniEnglish = new javax.swing.JMenuItem();
        m_mniJapanese = new javax.swing.JMenuItem();
        m_mnuHelp = new javax.swing.JMenu();
        m_mniAbout = new javax.swing.JMenuItem();
        m_bgpAction = new javax.swing.ButtonGroup();
        m_pnlRoot = new javax.swing.JPanel();
        m_pnlLeft = new javax.swing.JPanel();
        m_pnlToolBar = new javax.swing.JPanel();
        m_toolBar = new javax.swing.JToolBar();
        m_btnPick = new javax.swing.JToggleButton();
        m_btnPickPoint = new javax.swing.JToggleButton();
        m_btnPreview = new javax.swing.JToggleButton();
        m_btnWallpaper = new javax.swing.JToggleButton();
        m_pnlLeftBottom = new javax.swing.JPanel();
        m_pnlCommands = new javax.swing.JPanel();
        m_pnlRight = new javax.swing.JPanel();
        m_desktop = new javax.swing.JDesktopPane();
        m_pnlSearch = new javax.swing.JPanel();
        m_txtSearch = new javax.swing.JTextField();
        m_btnSearch = new javax.swing.JButton();
        m_cmbSearch = new javax.swing.JComboBox();

        m_mnuFile.setText("file");
        m_mnuNew.setText("New");
        m_mniNewTypeface.setText("Typeface...");
        m_mniNewTypeface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniNewTypefaceActionPerformed(evt);
            }
        });

        m_mnuNew.add(m_mniNewTypeface);

        m_mniAddNewGlyph.setText("add new glyph...");
        m_mniAddNewGlyph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniAddNewGlyphActionPerformed(evt);
            }
        });

        m_mnuNew.add(m_mniAddNewGlyph);

        m_mnuFile.add(m_mnuNew);

        m_mnuOpen.setText("Open");
        m_mniOpenTypeface.setText("open typeface...");
        m_mniOpenTypeface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniOpenTypefaceActionPerformed(evt);
            }
        });

        m_mnuOpen.add(m_mniOpenTypeface);

        m_mniAddExistingGlyph.setText("open glyph...");
        m_mniAddExistingGlyph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniAddExistingGlyphActionPerformed(evt);
            }
        });

        m_mnuOpen.add(m_mniAddExistingGlyph);

        m_mniOpenBackground.setText("open background image...");
        m_mniOpenBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniOpenBackgroundActionPerformed(evt);
            }
        });

        m_mnuOpen.add(m_mniOpenBackground);

        m_mnuFile.add(m_mnuOpen);

        m_mnuFile.add(m_sprOpen);

        m_mniSaveGlyph.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        m_mniSaveGlyph.setText("save glyph");
        m_mniSaveGlyph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniSaveGlyphActionPerformed(evt);
            }
        });

        m_mnuFile.add(m_mniSaveGlyph);

        m_mnuFile.add(m_sprSave);

        m_mniExit.setText("exit");
        m_mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniExitActionPerformed(evt);
            }
        });

        m_mnuFile.add(m_mniExit);

        m_menuBar.add(m_mnuFile);

        m_mnuEdit.setText("edit");
        m_mniUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        m_mniUndo.setText("undo");
        m_mniUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniUndoActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniUndo);

        m_mniRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        m_mniRedo.setText("redo");
        m_mniRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniRedoActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniRedo);

        m_mnuEdit.add(m_sprRedo);

        m_mniCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        m_mniCut.setText("cut");
        m_mniCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniCutActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniCut);

        m_mniCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        m_mniCopy.setText("copy");
        m_mniCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniCopyActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniCopy);

        m_mniPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        m_mniPaste.setText("paste");
        m_mniPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniPasteActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniPaste);

        m_mniDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        m_mniDelete.setText("delete");
        m_mniDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniDeleteActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniDelete);

        m_mnuEdit.add(m_sprPaste);

        m_mniMoveLeft.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.CTRL_MASK));
        m_mniMoveLeft.setText("Move Left");
        m_mniMoveLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniMoveLeftActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniMoveLeft);

        m_mniMoveUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_MASK));
        m_mniMoveUp.setText("Move Up");
        m_mniMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniMoveUpActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniMoveUp);

        m_mniMoveDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_MASK));
        m_mniMoveDown.setText("Move Down");
        m_mniMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniMoveDownActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniMoveDown);

        m_mniMoveRight.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.CTRL_MASK));
        m_mniMoveRight.setText("Move Right");
        m_mniMoveRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniMoveRightActionPerformed(evt);
            }
        });

        m_mnuEdit.add(m_mniMoveRight);

        m_menuBar.add(m_mnuEdit);

        m_mnuView.setText("view");
        m_mniShowBackground.setSelected(true);
        m_mniShowBackground.setText("show backgounrd");
        m_mniShowBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniShowBackgroundActionPerformed(evt);
            }
        });

        m_mnuView.add(m_mniShowBackground);

        m_mnuZoom.setText("Zoom");
        m_mnuView.add(m_mnuZoom);

        m_menuBar.add(m_mnuView);

        m_mnuFontSize.setText("Font Size");
        m_menuBar.add(m_mnuFontSize);

        m_mnuTypeface.setText("typeface");
        m_mniTypefaceSetting.setText("setting...");
        m_mniTypefaceSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniTypefaceSettingActionPerformed(evt);
            }
        });

        m_mnuTypeface.add(m_mniTypefaceSetting);

        m_mnuTypeface.add(jSeparator3);

        m_mniBuild.setText("Build TrueType file...");
        m_mniBuild.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniBuildActionPerformed(evt);
            }
        });

        m_mnuTypeface.add(m_mniBuild);

        m_mniRelease.setText("release...");
        m_mniRelease.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniReleaseActionPerformed(evt);
            }
        });

        m_mnuTypeface.add(m_mniRelease);

        m_menuBar.add(m_mnuTypeface);

        m_mnuLanguage.setText("Language");
        m_mniEnglish.setText("English");
        m_mniEnglish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniEnglishActionPerformed(evt);
            }
        });

        m_mnuLanguage.add(m_mniEnglish);

        m_mniJapanese.setText("Japanese");
        m_mniJapanese.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniJapaneseActionPerformed(evt);
            }
        });

        m_mnuLanguage.add(m_mniJapanese);

        m_menuBar.add(m_mnuLanguage);

        m_mnuHelp.setText("help");
        m_mniAbout.setText("About DoubleType...");
        m_mniAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_mniAboutActionPerformed(evt);
            }
        });

        m_mnuHelp.add(m_mniAbout);

        m_menuBar.add(m_mnuHelp);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("DoubleType");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        m_pnlRoot.setLayout(new java.awt.BorderLayout());

        m_pnlLeft.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        m_pnlLeft.setPreferredSize(new java.awt.Dimension(150, 0));
        m_pnlToolBar.setLayout(new java.awt.BorderLayout());

        m_pnlToolBar.setMinimumSize(new java.awt.Dimension(114, 100));
        m_pnlToolBar.setPreferredSize(new java.awt.Dimension(114, 100));
        m_toolBar.setFloatable(false);
        m_toolBar.setOrientation(1);
        m_toolBar.setRollover(true);
        m_toolBar.setMinimumSize(new java.awt.Dimension(114, 230));
        m_toolBar.setPreferredSize(new java.awt.Dimension(114, 230));
        m_btnPick.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/doubletype/ossa/pick.gif")));
        m_btnPick.setSelected(true);
        m_btnPick.setToolTipText("Pick");
        m_bgpAction.add(m_btnPick);
        m_btnPick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionActionPerformed(evt);
            }
        });

        m_toolBar.add(m_btnPick);

        m_btnPickPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/doubletype/ossa/pickpoint.gif")));
        m_bgpAction.add(m_btnPickPoint);
        m_btnPickPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionActionPerformed(evt);
            }
        });

        m_toolBar.add(m_btnPickPoint);

        m_btnPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/doubletype/ossa/preview.gif")));
        m_bgpAction.add(m_btnPreview);
        m_btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionActionPerformed(evt);
            }
        });

        m_toolBar.add(m_btnPreview);

        m_btnWallpaper.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/doubletype/ossa/wallpaper.gif")));
        m_bgpAction.add(m_btnWallpaper);
        m_btnWallpaper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionActionPerformed(evt);
            }
        });

        m_toolBar.add(m_btnWallpaper);

        m_pnlToolBar.add(m_toolBar, java.awt.BorderLayout.EAST);

        m_pnlLeft.add(m_pnlToolBar);

        m_pnlLeftBottom.setLayout(new java.awt.GridLayout(1, 0));

        m_pnlLeftBottom.setMinimumSize(new java.awt.Dimension(150, 20));
        m_pnlLeftBottom.setPreferredSize(new java.awt.Dimension(150, 200));
        m_pnlCommands.setLayout(new javax.swing.BoxLayout(m_pnlCommands, javax.swing.BoxLayout.Y_AXIS));

        m_pnlCommands.setBackground(new java.awt.Color(255, 255, 255));
        m_pnlCommands.setBorder(new javax.swing.border.TitledBorder("Task"));
        m_pnlLeftBottom.add(m_pnlCommands);

        m_pnlLeft.add(m_pnlLeftBottom);

        m_pnlRoot.add(m_pnlLeft, java.awt.BorderLayout.WEST);

        m_pnlRight.setLayout(new java.awt.BorderLayout());

        m_desktop.setPreferredSize(new java.awt.Dimension(750, 550));
        m_pnlRight.add(m_desktop, java.awt.BorderLayout.CENTER);

        m_pnlSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 1));

        m_txtSearch.setPreferredSize(new java.awt.Dimension(100, 21));
        m_txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnSearchActionPerformed(evt);
            }
        });

        m_pnlSearch.add(m_txtSearch);

        m_btnSearch.setText("Search");
        m_btnSearch.setPreferredSize(new java.awt.Dimension(77, 21));
        m_btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnSearchActionPerformed(evt);
            }
        });

        m_pnlSearch.add(m_btnSearch);

        m_cmbSearch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "By Example", "Unicode", "JIS Code" }));
        m_cmbSearch.setPreferredSize(new java.awt.Dimension(100, 19));
        m_pnlSearch.add(m_cmbSearch);

        m_pnlRight.add(m_pnlSearch, java.awt.BorderLayout.NORTH);

        m_pnlRoot.add(m_pnlRight, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_pnlRoot, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void m_btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnSearchActionPerformed
        searchAndOpen(m_txtSearch.getText(), m_cmbSearch.getSelectedIndex());
    }//GEN-LAST:event_m_btnSearchActionPerformed

    private void m_mniMoveRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniMoveRightActionPerformed
        m_engine.moveRight();
    }//GEN-LAST:event_m_mniMoveRightActionPerformed

    private void m_mniMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniMoveDownActionPerformed
        m_engine.moveDown();
    }//GEN-LAST:event_m_mniMoveDownActionPerformed

    private void m_mniMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniMoveUpActionPerformed
        m_engine.moveUp();
    }//GEN-LAST:event_m_mniMoveUpActionPerformed

    private void m_mniMoveLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniMoveLeftActionPerformed
        m_engine.moveLeft();
    }//GEN-LAST:event_m_mniMoveLeftActionPerformed

    private void actionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionActionPerformed
		m_engine.setAction(evt.getActionCommand());
    }//GEN-LAST:event_actionActionPerformed

    private void m_mniShowBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniShowBackgroundActionPerformed
    	m_engine.setShowBackground(m_mniShowBackground.isSelected());
    }//GEN-LAST:event_m_mniShowBackgroundActionPerformed

    private void m_mniOpenBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniOpenBackgroundActionPerformed
		m_engine.openBackgroundImage();
    }//GEN-LAST:event_m_mniOpenBackgroundActionPerformed

    private void m_mniDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniDeleteActionPerformed
        m_engine.delete();
    }//GEN-LAST:event_m_mniDeleteActionPerformed

    private void m_mniPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniPasteActionPerformed
        m_engine.pasteFromClipboard();
    }//GEN-LAST:event_m_mniPasteActionPerformed

    private void m_mniCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniCopyActionPerformed
        m_engine.copyToClipboard();
    }//GEN-LAST:event_m_mniCopyActionPerformed

    private void m_mniCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniCutActionPerformed
        m_engine.cutToClipboard();
    }//GEN-LAST:event_m_mniCutActionPerformed

    private void m_mniRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniRedoActionPerformed
        m_engine.redo();
    }//GEN-LAST:event_m_mniRedoActionPerformed

    private void m_mniUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniUndoActionPerformed
    	m_engine.undo();
    }//GEN-LAST:event_m_mniUndoActionPerformed

    private void m_mniReleaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniReleaseActionPerformed
		m_engine.buildTrueType(false);
    }//GEN-LAST:event_m_mniReleaseActionPerformed

    private void m_mniTypefaceSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniTypefaceSettingActionPerformed
		if (m_engine.getTypeface() == null)
			return;
		
		m_typefacePropertyDlg.show();   
    }//GEN-LAST:event_m_mniTypefaceSettingActionPerformed

    private void m_mniJapaneseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniJapaneseActionPerformed
		m_engine.setBundle(new GuiResource_ja_JP());
		localize(m_engine.getBundle());
    }//GEN-LAST:event_m_mniJapaneseActionPerformed

    private void m_mniEnglishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniEnglishActionPerformed
		// force english
		m_engine.setBundle(new GuiResource());
		localize(m_engine.getBundle());
    }//GEN-LAST:event_m_mniEnglishActionPerformed
    
    private void m_mniAddNewGlyphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniAddNewGlyphActionPerformed
		addNewGlyph();
    }//GEN-LAST:event_m_mniAddNewGlyphActionPerformed
    	
    private void m_mniAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniAboutActionPerformed
		AboutDialog dlg = new AboutDialog(this, true);
		dlg.setVisible(true);
    }//GEN-LAST:event_m_mniAboutActionPerformed

    private void m_mniBuildActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniBuildActionPerformed
		Font font = m_engine.buildTrueType(true);
    	if (font == null)
    		return;
    	
    	m_fontFrame.setTargetFont(font.deriveFont(16.0f));
		try {
			m_fontFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {}
		
		
    }//GEN-LAST:event_m_mniBuildActionPerformed

    private void m_mniAddExistingGlyphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniAddExistingGlyphActionPerformed
		GlyphFile file = m_engine.addExistingGlyph();
		
		if (file != null) {
			openGlyphFile(file);	
		} // if
    }//GEN-LAST:event_m_mniAddExistingGlyphActionPerformed

    private void m_mniSaveGlyphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniSaveGlyphActionPerformed
		m_engine.saveGlyph();
    }//GEN-LAST:event_m_mniSaveGlyphActionPerformed

    private void m_mniNewTypefaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniNewTypefaceActionPerformed
    	NewTypefaceDialog dialog = new NewTypefaceDialog(this, true);
    	dialog.setLocationRelativeTo(this);
    	dialog.setVisible(true);
    	if (dialog.getFileName() == null) {
    		return;
    	} // if
    	
    	m_engine.buildNewTypeface(dialog.getFileName(),
    			dialog.getDir());
		
		if (m_engine.getTypeface() == null) {
			return;
		} // if
		
		m_typefacePropertyDlg.show();
		m_engine.addDefaultGlyphs();
    }//GEN-LAST:event_m_mniNewTypefaceActionPerformed

	private void m_mniOpenTypefaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniOpenTypefaceActionPerformed
		m_engine.openTypeface();
		updateTitle();
	}//GEN-LAST:event_m_mniOpenTypefaceActionPerformed

	private void m_mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_mniExitActionPerformed
		exitForm(null);
	}//GEN-LAST:event_m_mniExitActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (!DrawFrame.closeAll()) {
            return;
        } // if
        
        dispose();
        System.exit(0);
    }//GEN-LAST:event_exitForm
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.ButtonGroup m_bgpAction;
    private javax.swing.JToggleButton m_btnPick;
    private javax.swing.JToggleButton m_btnPickPoint;
    private javax.swing.JToggleButton m_btnPreview;
    private javax.swing.JButton m_btnSearch;
    private javax.swing.JToggleButton m_btnWallpaper;
    private javax.swing.JComboBox m_cmbSearch;
    private javax.swing.JDesktopPane m_desktop;
    private javax.swing.JMenuBar m_menuBar;
    private javax.swing.JMenuItem m_mniAbout;
    private javax.swing.JMenuItem m_mniAddExistingGlyph;
    private javax.swing.JMenuItem m_mniAddNewGlyph;
    private javax.swing.JMenuItem m_mniBuild;
    private javax.swing.JMenuItem m_mniCopy;
    private javax.swing.JMenuItem m_mniCut;
    private javax.swing.JMenuItem m_mniDelete;
    private javax.swing.JMenuItem m_mniEnglish;
    private javax.swing.JMenuItem m_mniExit;
    private javax.swing.JMenuItem m_mniJapanese;
    private javax.swing.JMenuItem m_mniMoveDown;
    private javax.swing.JMenuItem m_mniMoveLeft;
    private javax.swing.JMenuItem m_mniMoveRight;
    private javax.swing.JMenuItem m_mniMoveUp;
    private javax.swing.JMenuItem m_mniNewTypeface;
    private javax.swing.JMenuItem m_mniOpenBackground;
    private javax.swing.JMenuItem m_mniOpenTypeface;
    private javax.swing.JMenuItem m_mniPaste;
    private javax.swing.JMenuItem m_mniRedo;
    private javax.swing.JMenuItem m_mniRelease;
    private javax.swing.JMenuItem m_mniSaveGlyph;
    private javax.swing.JCheckBoxMenuItem m_mniShowBackground;
    private javax.swing.JMenuItem m_mniTypefaceSetting;
    private javax.swing.JMenuItem m_mniUndo;
    private javax.swing.JMenu m_mnuEdit;
    private javax.swing.JMenu m_mnuFile;
    private javax.swing.JMenu m_mnuFontSize;
    private javax.swing.JMenu m_mnuHelp;
    private javax.swing.JMenu m_mnuLanguage;
    private javax.swing.JMenu m_mnuNew;
    private javax.swing.JMenu m_mnuOpen;
    private javax.swing.JMenu m_mnuTypeface;
    private javax.swing.JMenu m_mnuView;
    private javax.swing.JMenu m_mnuZoom;
    private javax.swing.JPanel m_pnlCommands;
    private javax.swing.JPanel m_pnlLeft;
    private javax.swing.JPanel m_pnlLeftBottom;
    private javax.swing.JPanel m_pnlRight;
    private javax.swing.JPanel m_pnlRoot;
    private javax.swing.JPanel m_pnlSearch;
    private javax.swing.JPanel m_pnlToolBar;
    private javax.swing.JSeparator m_sprOpen;
    private javax.swing.JSeparator m_sprPaste;
    private javax.swing.JSeparator m_sprRedo;
    private javax.swing.JSeparator m_sprSave;
    private javax.swing.JToolBar m_toolBar;
    private javax.swing.JTextField m_txtSearch;
    // End of variables declaration//GEN-END:variables
    
    class MyFontSizeAction extends StyledEditorKit.FontSizeAction {
    	private static final long serialVersionUID = 1L;
    	
    	TTPixelSize m_pixelSize;
    	
    	public MyFontSizeAction(TTPixelSize a_pixelSize) {
    		super(a_pixelSize.getDescription(),
    				a_pixelSize.getPixel());
    		
    		m_pixelSize = a_pixelSize;
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		super.actionPerformed(e);
    		m_engine.getDisplay().setPpem(m_pixelSize.getPixel());
    	}
    }
	
	class ZoomAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
	    private int m_zoom;
	    
	    public ZoomAction(int a_zoom) {
	        super(Integer.toString(a_zoom) + "%");
	        
	        m_zoom = a_zoom;
	    }
	    
		public void actionPerformed(ActionEvent e) {
		    m_engine.getDisplay().setZoom(m_zoom);	
		}
	}
}
