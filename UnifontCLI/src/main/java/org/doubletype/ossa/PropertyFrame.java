/*
 * $Id: PropertyFrame.java,v 1.22 2004/09/11 10:09:06 eed3si9n Exp $
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

import org.doubletype.ossa.module.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author  Eugene
 */
public class PropertyFrame extends javax.swing.JInternalFrame {
	
//	-------------------------------------------------------------------
    
	private Engine m_engine; 
	private Desktop m_parent;
	
	private GlyphFile m_glyphFile;
	private TypefaceFile m_typeface;
	   
	private DefaultTreeModel m_treeModel;
	private DefaultMutableTreeNode m_rootNode;
    
	private FileVarTableModel m_glyphGlobalModel;
	private FileVarTableModel m_typefaceGlobalModel;
	private PointTableModel m_pointModel;
	
	private InvokeTableModel m_moduleVargs;
	private InvokeTableModel m_includeVargs;
		
	private InvokePosTableModel m_modulePos;
	private InvokePosTableModel m_includePos;
	
	private DefaultListModel m_filesModel;
	private boolean m_isRefreshing = false;
	
//	-------------------------------------------------------------------
    
    /** Creates new form PropertyFrame */
    public PropertyFrame(Engine a_engine, Desktop a_parent) {
    	m_engine = a_engine;
    	m_parent = a_parent;
    	
		m_engine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engineActionPerformed(e);		
			}
		});
        
		m_glyphGlobalModel
			= new FileVarTableModel(FileVarTableModel.GLYPH_VAR,
				m_engine);
		m_typefaceGlobalModel
			= new FileVarTableModel(FileVarTableModel.TYPEFACE_VAR,
				m_engine);
		
		m_pointModel = new PointTableModel(m_engine);
		m_moduleVargs = new InvokeTableModel(InvokeTableModel.MODULE_VARG,
			m_engine);
		m_includeVargs = new InvokeTableModel(InvokeTableModel.INCLUDE_VARG,
			m_engine);
		
		m_modulePos
			= new InvokePosTableModel(InvokePosTableModel.MODULE_POS);
		m_includePos
			= new InvokePosTableModel(InvokePosTableModel.INCLUDE_POS);
		m_filesModel = new DefaultListModel();
    	
		// m_tree = m_engine.getTree();
		// m_scrollPanel.setViewportView(m_tree);
    	
        initComponents();
		setLocation(0, 0);
		setFrameIcon(null);
        
        localize(m_engine.getBundle());
        
		engineActionPerformed(null);
		m_tabbedPanel.setSelectedIndex(0);
    }
    
	public void localize(ResourceBundle a_bundle) {
		int i = 0;
		m_tabbedPanel.setTitleAt(i++, a_bundle.getString("typeface"));
		m_tabbedPanel.setTitleAt(i++, a_bundle.getString("glyph"));
		m_tabbedPanel.setTitleAt(i++, a_bundle.getString("contour"));
		m_tabbedPanel.setTitleAt(i++, a_bundle.getString("module"));
		m_tabbedPanel.setTitleAt(i++, a_bundle.getString("include"));
		
		String strAdd = a_bundle.getString("add");
		m_btnAddGlyphGlobal.setText(strAdd);
		m_btnAddTypefaceGlobal.setText(strAdd);
		m_btnAddVargInclude.setText(strAdd);
		m_btnAddVargModule.setText(strAdd);
		
		String strRemove = a_bundle.getString("remove");
		m_btnRemoveGlyphGlobal.setText(strRemove);
		m_btnRemoveTypefaceGlobal.setText(strRemove);
		m_btnRemoveVargInclude.setText(strRemove);
		m_btnRemoveVargModule.setText(strRemove);
		m_btnRemoveGlyph.setText(strRemove);
		
		m_btnOpenGlyphFile.setText(a_bundle.getString("open"));
		m_lblGlyphName.setText(a_bundle.getString("glyphName") + ":");
		m_lblUnicode.setText(a_bundle.getString("unicode") + ":");
		m_lblWidth.setText(a_bundle.getString("width") + ":");
	}
    
	public void engineActionPerformed(ActionEvent a_event) {
    	m_isRefreshing = true;
		m_glyphFile = m_engine.getRoot();
		m_typeface = m_engine.getTypeface();
    	
		m_pointModel.reset();
		refresh(m_tblPoints);
		refresh(m_tblIncludeVargs);
		refresh(m_tblModuleVargs);
		refresh(m_tblModulePos);
		refresh(m_tblIncludePos);
		refresh(m_tblTypefaceGlobal);
		refresh(m_tblGlyphGlobal);
   		
		if (m_glyphFile != null) {
			m_txtTitle.setText(m_glyphFile.getGlyphTitle());
			m_txtUnicode.setText(m_glyphFile.getUnicode());
			m_txtIncludeName.setText(m_glyphFile.getIncludeName()); 
			m_txtModuleName.setText(m_glyphFile.getModuleName());
			m_spnAdvanceWidth.setValue(
				new Integer(m_glyphFile.getAdvanceWidth()));
			m_spnAdvanceWidth.invalidate();
		} else {
			m_txtTitle.setText("");
			m_txtUnicode.setText("");
			m_txtIncludeName.setText("");			
			m_txtModuleName.setText("");		
		} // if-else
   		
		m_filesModel.clear();
		if (m_typeface != null) {			
			for (String fileName: m_typeface.getChildFileNames()) {
				m_filesModel.addElement(fileName);
			} // for fileName
		} // if
		m_lstFiles.invalidate();
		reenableUI();
		m_isRefreshing = false;
	}
	
	private void reenableUI() {
	    boolean isTypefaceExists = (m_typeface != null);
	    
	    m_btnOpenGlyphFile.setEnabled(isTypefaceExists);
	    m_btnRemoveGlyph.setEnabled(isTypefaceExists);
	    m_btnAddTypefaceGlobal.setEnabled(isTypefaceExists);
	    m_btnRemoveTypefaceGlobal.setEnabled(isTypefaceExists);
	    m_tblTypefaceGlobal.setEnabled(isTypefaceExists);
	    
	    boolean isGlyphExists = (m_glyphFile != null);
	    m_btnChangeTitle.setEnabled(isGlyphExists);
	    m_btnChangeUnicode.setEnabled(isGlyphExists);
	    m_spnAdvanceWidth.setEnabled(isGlyphExists);
	    m_btnAddGlyphGlobal.setEnabled(isGlyphExists);
	    m_btnRemoveGlyphGlobal.setEnabled(isGlyphExists);
	    m_tblGlyphGlobal.setEnabled(isGlyphExists);
	    
	    m_tblPoints.setEnabled(isGlyphExists);
	    m_btnChangeInclude3.setEnabled(isGlyphExists);
	    m_tblModulePos.setEnabled(isGlyphExists);
	    m_btnAddVargModule.setEnabled(isGlyphExists);
	    m_btnRemoveVargModule.setEnabled(isGlyphExists);
	    
	    m_btnChangeInclude.setEnabled(isGlyphExists);
	    m_tblIncludePos.setEnabled(isGlyphExists);
	    m_tblIncludeVargs.setEnabled(isGlyphExists);
	    m_btnAddVargInclude.setEnabled(isGlyphExists);
	    m_btnRemoveVargInclude.setEnabled(isGlyphExists);
	}
	
	private void refresh(JTable a_table) {
		a_table.revalidate();
		a_table.repaint();
	}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        m_pnlRoot = new javax.swing.JPanel();
        m_tabbedPanel = new javax.swing.JTabbedPane();
        m_pnlTypefaceProperty = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        m_lstFiles = new javax.swing.JList();
        m_btnOpenGlyphFile = new javax.swing.JButton();
        m_btnRemoveGlyph = new javax.swing.JButton();
        m_pnlTypefaceVars = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        m_tblTypefaceGlobal = new javax.swing.JTable();
        m_btnAddTypefaceGlobal = new javax.swing.JButton();
        m_btnRemoveTypefaceGlobal = new javax.swing.JButton();
        m_pnlFileProperty = new javax.swing.JPanel();
        m_pnlGlyphVars = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_tblGlyphGlobal = new javax.swing.JTable();
        m_btnAddGlyphGlobal = new javax.swing.JButton();
        m_btnRemoveGlyphGlobal = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        m_lblGlyphName = new javax.swing.JLabel();
        m_txtTitle = new javax.swing.JTextField();
        m_btnChangeTitle = new javax.swing.JButton();
        m_lblUnicode = new javax.swing.JLabel();
        m_txtUnicode = new javax.swing.JTextField();
        m_btnChangeUnicode = new javax.swing.JButton();
        m_lblWidth = new javax.swing.JLabel();
        m_spnAdvanceWidth = new javax.swing.JSpinner();
        m_pnlContourPoint = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        m_tblPoints = new javax.swing.JTable();
        m_pnlModule = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        m_tblModuleVargs = new javax.swing.JTable();
        m_txtModuleName = new javax.swing.JTextField();
        m_btnAddVargModule = new javax.swing.JButton();
        m_btnRemoveVargModule = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        m_tblModulePos = new javax.swing.JTable();
        m_btnChangeInclude3 = new javax.swing.JButton();
        m_pnlInclude = new javax.swing.JPanel();
        m_txtIncludeName = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        m_tblIncludeVargs = new javax.swing.JTable();
        m_btnAddVargInclude = new javax.swing.JButton();
        m_btnRemoveVargInclude = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        m_tblIncludePos = new javax.swing.JTable();
        m_btnChangeInclude = new javax.swing.JButton();

        setIconifiable(true);
        m_pnlRoot.setLayout(new java.awt.BorderLayout());

        m_pnlRoot.setMinimumSize(new java.awt.Dimension(300, 76));
        m_tabbedPanel.setPreferredSize(new java.awt.Dimension(300, 450));
        m_pnlTypefaceProperty.setLayout(null);

        m_lstFiles.setModel(m_filesModel);
        m_lstFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        m_lstFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_lstFilesMouseClicked(evt);
            }
        });

        jScrollPane10.setViewportView(m_lstFiles);

        m_pnlTypefaceProperty.add(jScrollPane10);
        jScrollPane10.setBounds(0, 10, 290, 90);

        m_btnOpenGlyphFile.setText("open...");
        m_btnOpenGlyphFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnOpenGlyphFileActionPerformed(evt);
            }
        });

        m_pnlTypefaceProperty.add(m_btnOpenGlyphFile);
        m_btnOpenGlyphFile.setBounds(0, 100, 81, 25);

        m_btnRemoveGlyph.setText("remove");
        m_btnRemoveGlyph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveGlyphActionPerformed(evt);
            }
        });

        m_pnlTypefaceProperty.add(m_btnRemoveGlyph);
        m_btnRemoveGlyph.setBounds(100, 100, 81, 25);

        m_pnlTypefaceVars.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        m_pnlTypefaceVars.setBorder(new javax.swing.border.TitledBorder("typeface vars"));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 75));
        m_tblTypefaceGlobal.setModel(m_typefaceGlobalModel);
        jScrollPane2.setViewportView(m_tblTypefaceGlobal);

        m_pnlTypefaceVars.add(jScrollPane2);

        m_btnAddTypefaceGlobal.setText("add");
        m_btnAddTypefaceGlobal.setMaximumSize(new java.awt.Dimension(75, 26));
        m_btnAddTypefaceGlobal.setPreferredSize(new java.awt.Dimension(75, 26));
        m_btnAddTypefaceGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnAddTypefaceGlobalActionPerformed(evt);
            }
        });

        m_pnlTypefaceVars.add(m_btnAddTypefaceGlobal);

        m_btnRemoveTypefaceGlobal.setText("remove");
        m_btnRemoveTypefaceGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveTypefaceGlobalActionPerformed(evt);
            }
        });

        m_pnlTypefaceVars.add(m_btnRemoveTypefaceGlobal);

        m_pnlTypefaceProperty.add(m_pnlTypefaceVars);
        m_pnlTypefaceVars.setBounds(0, 160, 270, 150);

        m_tabbedPanel.addTab("typeface", m_pnlTypefaceProperty);

        m_pnlFileProperty.setLayout(null);

        m_pnlFileProperty.setName("");
        m_pnlFileProperty.setPreferredSize(new java.awt.Dimension(200, 500));
        m_pnlGlyphVars.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        m_pnlGlyphVars.setBorder(new javax.swing.border.TitledBorder("glyph vars"));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 75));
        m_tblGlyphGlobal.setModel(m_glyphGlobalModel);
        m_tblGlyphGlobal.setEnabled(false);
        jScrollPane1.setViewportView(m_tblGlyphGlobal);

        m_pnlGlyphVars.add(jScrollPane1);

        m_btnAddGlyphGlobal.setText("add");
        m_btnAddGlyphGlobal.setMaximumSize(new java.awt.Dimension(75, 26));
        m_btnAddGlyphGlobal.setPreferredSize(new java.awt.Dimension(75, 26));
        m_btnAddGlyphGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnAddGlyphGlobalActionPerformed(evt);
            }
        });

        m_pnlGlyphVars.add(m_btnAddGlyphGlobal);

        m_btnRemoveGlyphGlobal.setText("remove");
        m_btnRemoveGlyphGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveGlyphGlobalActionPerformed(evt);
            }
        });

        m_pnlGlyphVars.add(m_btnRemoveGlyphGlobal);

        m_pnlFileProperty.add(m_pnlGlyphVars);
        m_pnlGlyphVars.setBounds(0, 100, 270, 150);

        jPanel1.setLayout(null);

        jPanel1.setBorder(new javax.swing.border.TitledBorder(""));
        m_lblGlyphName.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblGlyphName.setText("glyph name:");
        jPanel1.add(m_lblGlyphName);
        m_lblGlyphName.setBounds(10, 10, 70, 15);

        m_txtTitle.setEditable(false);
        m_txtTitle.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel1.add(m_txtTitle);
        m_txtTitle.setBounds(90, 10, 140, 20);

        m_btnChangeTitle.setText("...");
        jPanel1.add(m_btnChangeTitle);
        m_btnChangeTitle.setBounds(240, 10, 20, 20);

        m_lblUnicode.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblUnicode.setText("unicode:");
        jPanel1.add(m_lblUnicode);
        m_lblUnicode.setBounds(10, 30, 70, 15);

        m_txtUnicode.setEditable(false);
        m_txtUnicode.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel1.add(m_txtUnicode);
        m_txtUnicode.setBounds(90, 30, 80, 20);

        m_btnChangeUnicode.setText("...");
        m_btnChangeUnicode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeUnicodeActionPerformed(evt);
            }
        });

        jPanel1.add(m_btnChangeUnicode);
        m_btnChangeUnicode.setBounds(180, 30, 20, 20);

        m_lblWidth.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblWidth.setText("advance width:");
        jPanel1.add(m_lblWidth);
        m_lblWidth.setBounds(10, 50, 80, 15);

        m_spnAdvanceWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_spnAdvanceWidthStateChanged(evt);
            }
        });

        jPanel1.add(m_spnAdvanceWidth);
        m_spnAdvanceWidth.setBounds(90, 50, 60, 20);

        m_pnlFileProperty.add(jPanel1);
        jPanel1.setBounds(0, 10, 270, 80);

        m_tabbedPanel.addTab("glyph", m_pnlFileProperty);

        m_pnlContourPoint.setLayout(null);

        m_tblPoints.setModel(m_pointModel);
        m_tblPoints.setEnabled(false);
        jScrollPane3.setViewportView(m_tblPoints);

        m_pnlContourPoint.add(jScrollPane3);
        jScrollPane3.setBounds(0, 30, 290, 130);

        m_tabbedPanel.addTab("contour", m_pnlContourPoint);

        m_pnlModule.setLayout(null);

        m_tblModuleVargs.setModel(m_moduleVargs);
        m_tblModuleVargs.setEnabled(false);
        jScrollPane4.setViewportView(m_tblModuleVargs);

        m_pnlModule.add(jScrollPane4);
        jScrollPane4.setBounds(0, 100, 290, 70);

        m_txtModuleName.setEditable(false);
        m_txtModuleName.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlModule.add(m_txtModuleName);
        m_txtModuleName.setBounds(10, 10, 130, 20);

        m_btnAddVargModule.setText("add");
        m_btnAddVargModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnAddVargModuleActionPerformed(evt);
            }
        });

        m_pnlModule.add(m_btnAddVargModule);
        m_btnAddVargModule.setBounds(0, 180, 81, 25);

        m_btnRemoveVargModule.setText("remove");
        m_btnRemoveVargModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveVargModuleActionPerformed(evt);
            }
        });

        m_pnlModule.add(m_btnRemoveVargModule);
        m_btnRemoveVargModule.setBounds(90, 180, 81, 25);

        m_tblModulePos.setModel(m_modulePos);
        m_tblModulePos.setEnabled(false);
        jScrollPane8.setViewportView(m_tblModulePos);

        m_pnlModule.add(jScrollPane8);
        jScrollPane8.setBounds(0, 40, 290, 40);

        m_btnChangeInclude3.setText("...");
        m_pnlModule.add(m_btnChangeInclude3);
        m_btnChangeInclude3.setBounds(150, 10, 20, 20);

        m_tabbedPanel.addTab("module", m_pnlModule);

        m_pnlInclude.setLayout(null);

        m_txtIncludeName.setEditable(false);
        m_txtIncludeName.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlInclude.add(m_txtIncludeName);
        m_txtIncludeName.setBounds(10, 10, 130, 20);

        m_tblIncludeVargs.setModel(m_includeVargs);
        jScrollPane7.setViewportView(m_tblIncludeVargs);

        m_pnlInclude.add(jScrollPane7);
        jScrollPane7.setBounds(0, 100, 290, 70);

        m_btnAddVargInclude.setText("add");
        m_btnAddVargInclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnAddVargIncludeActionPerformed(evt);
            }
        });

        m_pnlInclude.add(m_btnAddVargInclude);
        m_btnAddVargInclude.setBounds(0, 180, 81, 25);

        m_btnRemoveVargInclude.setText("remove");
        m_btnRemoveVargInclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveVargIncludeActionPerformed(evt);
            }
        });

        m_pnlInclude.add(m_btnRemoveVargInclude);
        m_btnRemoveVargInclude.setBounds(90, 180, 81, 25);

        m_tblIncludePos.setModel(m_includePos);
        jScrollPane9.setViewportView(m_tblIncludePos);

        m_pnlInclude.add(jScrollPane9);
        jScrollPane9.setBounds(0, 40, 290, 40);

        m_btnChangeInclude.setText("...");
        m_btnChangeInclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeIncludeActionPerformed(evt);
            }
        });

        m_pnlInclude.add(m_btnChangeInclude);
        m_btnChangeInclude.setBounds(150, 10, 20, 20);

        m_tabbedPanel.addTab("include", m_pnlInclude);

        m_pnlRoot.add(m_tabbedPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_pnlRoot, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void m_spnAdvanceWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_spnAdvanceWidthStateChanged
        if (m_isRefreshing)
        	return;
        
        Integer n = (Integer) m_spnAdvanceWidth.getValue();
        m_engine.setAdvanceWidth(n.intValue());
    }//GEN-LAST:event_m_spnAdvanceWidthStateChanged

    private void m_btnChangeUnicodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeUnicodeActionPerformed
		Long unicode = UnicodeBuilder.askUnicode();
		if (unicode == null) {
		    return;
		} // if
        
        m_engine.changeUnicode(unicode.longValue());
    }//GEN-LAST:event_m_btnChangeUnicodeActionPerformed

    private void m_btnChangeIncludeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeIncludeActionPerformed
		if (m_glyphFile == null)
			return;
		
		ActiveList actives = ActiveList.getSingletonInstance();
		if (!actives.hasActiveInclude())
			return;
		
		JFileChooser chooser = new JFileChooser(m_engine.getGlyphPath());
		chooser.setFileFilter(new GlyphFileFilter());
	    
		int returnVal = chooser.showOpenDialog(this);
	    
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			m_glyphFile.setIncludeName(chooser.getSelectedFile().getName().toString());
		} // if
    }//GEN-LAST:event_m_btnChangeIncludeActionPerformed

    private void m_btnRemoveVargIncludeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveVargIncludeActionPerformed
		removeInvokeArg(m_tblIncludeVargs,
			InvokeTableModel.INCLUDE_VARG);
    }//GEN-LAST:event_m_btnRemoveVargIncludeActionPerformed

    private void m_btnAddVargIncludeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnAddVargIncludeActionPerformed
		addInvokeArg(m_tblIncludeVargs,
			InvokeTableModel.INCLUDE_VARG);
    }//GEN-LAST:event_m_btnAddVargIncludeActionPerformed

    private void m_btnRemoveVargModuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveVargModuleActionPerformed
		removeInvokeArg(m_tblModuleVargs, InvokeTableModel.MODULE_VARG);
    }//GEN-LAST:event_m_btnRemoveVargModuleActionPerformed

    private void m_btnAddVargModuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnAddVargModuleActionPerformed
		addInvokeArg(m_tblModuleVargs,
			InvokeTableModel.MODULE_VARG);
    }//GEN-LAST:event_m_btnAddVargModuleActionPerformed
    
	private void addInvokeArg(JTable a_table, int a_type) {
		if (m_glyphFile == null)
			return;
		
		m_glyphFile.addInvokeArg(a_type);
		refresh(a_table);	
	}
	
	private void removeInvokeArg(JTable a_table, int a_type) {
		if (m_glyphFile == null)
			return;
		
		if (a_table.getRowCount() < 1)
			return;
    	
		if (a_table.getSelectedRow() < 0)
			return;
		m_glyphFile.removeInvokeArg(a_type, a_table.getSelectedRow());
		refresh(a_table);
	}

    private void m_btnRemoveGlyphGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveGlyphGlobalActionPerformed
		if (m_glyphFile == null)
			return;
		
		if (m_tblGlyphGlobal.getRowCount() < 1)
			return;
		if (m_tblGlyphGlobal.getSelectedRow() < 0)
			return;
		    		
		m_glyphFile.removeFileVar(m_tblGlyphGlobal.getSelectedRow());
		refresh(m_tblGlyphGlobal);
    }//GEN-LAST:event_m_btnRemoveGlyphGlobalActionPerformed

    private void m_btnAddGlyphGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnAddGlyphGlobalActionPerformed
		if (m_glyphFile == null)
			return;
		
		m_glyphFile.addFileVar();
		refresh(m_tblGlyphGlobal);
    }//GEN-LAST:event_m_btnAddGlyphGlobalActionPerformed
	
	private void m_btnRemoveTypefaceGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveTypefaceGlobalActionPerformed
		if (m_typeface == null)
			return;
		
		if (m_tblTypefaceGlobal.getRowCount() < 1)
			return;
		if (m_tblTypefaceGlobal.getSelectedRow() < 0)
			return;
			
		m_typeface.removeFileVar(m_tblTypefaceGlobal.getSelectedRow());
		m_typeface.saveGlyphFile();
		refresh(m_tblTypefaceGlobal);	
	}//GEN-LAST:event_m_btnRemoveTypefaceGlobalActionPerformed

	private void m_btnAddTypefaceGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnAddTypefaceGlobalActionPerformed
		if (m_typeface == null)
			return;
			
		m_typeface.addFileVar();
		m_typeface.saveGlyphFile();
		refresh(m_tblTypefaceGlobal);
	}//GEN-LAST:event_m_btnAddTypefaceGlobalActionPerformed

    private void m_btnRemoveGlyphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveGlyphActionPerformed
		if (m_lstFiles.getSelectedIndex() < 0)
			return;
				
		String fileName = m_lstFiles.getSelectedValue().toString();
		if (fileName == null)
			return;
		    		
		m_engine.removeGlyphFromTypeface(fileName);
    }//GEN-LAST:event_m_btnRemoveGlyphActionPerformed

    private void m_btnOpenGlyphFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnOpenGlyphFileActionPerformed
		openSelectedFile();
    }//GEN-LAST:event_m_btnOpenGlyphFileActionPerformed
	
    private void m_lstFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_lstFilesMouseClicked
		if (evt.getClickCount() != 2) {
			return;
		} // if
    	
		openSelectedFile();
    }//GEN-LAST:event_m_lstFilesMouseClicked
    
	private void openSelectedFile() {
		if (m_lstFiles.getSelectedIndex() < 0)
			return;
		
		String fileName = m_lstFiles.getSelectedValue().toString();
		if (fileName == null)
			return;
    		
		m_parent.openGlyphFile(fileName);
	}
	
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton m_btnAddGlyphGlobal;
    private javax.swing.JButton m_btnAddTypefaceGlobal;
    private javax.swing.JButton m_btnAddVargInclude;
    private javax.swing.JButton m_btnAddVargModule;
    private javax.swing.JButton m_btnChangeInclude;
    private javax.swing.JButton m_btnChangeInclude3;
    private javax.swing.JButton m_btnChangeTitle;
    private javax.swing.JButton m_btnChangeUnicode;
    private javax.swing.JButton m_btnOpenGlyphFile;
    private javax.swing.JButton m_btnRemoveGlyph;
    private javax.swing.JButton m_btnRemoveGlyphGlobal;
    private javax.swing.JButton m_btnRemoveTypefaceGlobal;
    private javax.swing.JButton m_btnRemoveVargInclude;
    private javax.swing.JButton m_btnRemoveVargModule;
    private javax.swing.JLabel m_lblGlyphName;
    private javax.swing.JLabel m_lblUnicode;
    private javax.swing.JLabel m_lblWidth;
    private javax.swing.JList m_lstFiles;
    private javax.swing.JPanel m_pnlContourPoint;
    private javax.swing.JPanel m_pnlFileProperty;
    private javax.swing.JPanel m_pnlGlyphVars;
    private javax.swing.JPanel m_pnlInclude;
    private javax.swing.JPanel m_pnlModule;
    private javax.swing.JPanel m_pnlRoot;
    private javax.swing.JPanel m_pnlTypefaceProperty;
    private javax.swing.JPanel m_pnlTypefaceVars;
    private javax.swing.JSpinner m_spnAdvanceWidth;
    private javax.swing.JTabbedPane m_tabbedPanel;
    private javax.swing.JTable m_tblGlyphGlobal;
    private javax.swing.JTable m_tblIncludePos;
    private javax.swing.JTable m_tblIncludeVargs;
    private javax.swing.JTable m_tblModulePos;
    private javax.swing.JTable m_tblModuleVargs;
    private javax.swing.JTable m_tblPoints;
    private javax.swing.JTable m_tblTypefaceGlobal;
    private javax.swing.JTextField m_txtIncludeName;
    private javax.swing.JTextField m_txtModuleName;
    private javax.swing.JTextField m_txtTitle;
    private javax.swing.JTextField m_txtUnicode;
    // End of variables declaration//GEN-END:variables
    
}
