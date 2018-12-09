 /*
 * $Id: TypefacePropertyDialog.java,v 1.10 2004/02/27 06:24:37 eed3si9n Exp $
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

import javax.swing.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.truetype.TTCodePage;
import java.util.*;
import java.awt.event.*;

/**
 *
 * @author  e.e
 */
public class TypefacePropertyDialog extends javax.swing.JDialog {
	private Desktop m_parent;
	private Engine m_engine;
	private DefaultListModel m_codePagesModel;
	private boolean m_isRefreshing = false;
	private String m_msgAuthor = "author?";
	private String m_msgYear = "copyright year? (for example: 2003-2004)";
    private String m_msgFamily = "font family name?";
    
    /** Creates new form TypefacePropertyDialog */
    public TypefacePropertyDialog(Engine a_engine, Desktop a_parent) {
        super(a_parent, true);
        
		m_parent = a_parent;
		m_engine = a_engine;
		m_codePagesModel = new DefaultListModel();

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engineActionPerformed(e);		
			}
		};
		m_engine.addActionListener(listener);
		
        initComponents();
        
		String [] codePages = TTCodePage.getNames();
		int i;
		for (i = 0; i < codePages.length; i++) {
			m_cmbCodePages.addItem(codePages[i]);
		} // for i
        
		refresh();
    }
    
	public void localize(ResourceBundle a_bundle) {	
		m_tabbedPanel.setTitleAt(0, a_bundle.getString("fontInfo"));
		m_tabbedPanel.setTitleAt(1, a_bundle.getString("metrics"));
		
		String strAdd = a_bundle.getString("add");
		m_btnAddCodePage.setText(strAdd);
		
		String strRemove = a_bundle.getString("remove");
		m_btnRemoveCodePage.setText(strRemove);
		
		m_btnClose.setText(a_bundle.getString("close"));
		
		m_pnlFontInfo.setBorder(
			new javax.swing.border.TitledBorder(
			a_bundle.getString("fontInfo") + ":"));
		m_lblFamily.setText(a_bundle.getString("fontFamily") + ":");
			
		m_pnlCopyrightInfo.setBorder(
					new javax.swing.border.TitledBorder(
					a_bundle.getString("copyrightInfo") + ":"));
		m_lblAuthor.setText(a_bundle.getString("author") + ":");
		m_lblYear.setText(a_bundle.getString("year") + ":");				
		
		m_pnlCodePages.setBorder(
					new javax.swing.border.TitledBorder(
					a_bundle.getString("codePages") + ":"));
		m_pnlLicense.setBorder(
					new javax.swing.border.TitledBorder(
					a_bundle.getString("license") + ":"));
		m_pnlMetrics.setBorder(
			new javax.swing.border.TitledBorder(
			a_bundle.getString("metrics") + ":"));
		m_lblTopBearing.setText(a_bundle.getString("tsb") 
			+ ":");
		m_lblAscender.setText(a_bundle.getString("ascender") 
			+ ":");
		m_lblXHeight.setText(a_bundle.getString("xHeight")
			+ ":");
		m_lblDescender.setText(a_bundle.getString("descender")
			+ ":");
		m_lblBottomBearing.setText(a_bundle.getString("bsb")
			+ ":");
		m_lblBodyBottom.setText(a_bundle.getString("bodybottom")
			+ ":");
		m_lblBaseline.setText(a_bundle.getString("baseline")
			+ ":");
		m_lblMeanline.setText(a_bundle.getString("meanline")
			+ ":");
		m_lblBodyTop.setText(a_bundle.getString("bodytop")
			+ ":");
					
		m_msgAuthor = a_bundle.getString("msgAuthor");
		m_msgFamily = a_bundle.getString("msgFamily");
		m_msgYear = a_bundle.getString("msgYear");
		
	}
	
	public void engineActionPerformed(ActionEvent a_event) {
		refresh();
	}
    
	public void refresh() {
		int i;
		m_codePagesModel.clear();
		TypefaceFile typeface = m_engine.getTypeface();
		if (typeface == null) {
			return;
		} // if
		
		m_isRefreshing = true;
		
		m_txtAuthor.setText(typeface.getAuthor());
		m_txtYear.setText(typeface.getCopyrightYear());
		m_txtFamilyName.setText(typeface.getFontFamilyName());
		m_txtVersion.setText(typeface.getVersion());
		m_txtLicese.setText(typeface.getLicense());
		
		Object [] codePages = typeface.getCodePages();
		for (i = 0; i < codePages.length; i++) {
			m_codePagesModel.addElement(codePages[i]);
		} // for i
		m_lstCodePages.invalidate();
		
		m_txtTopBearing.setText(
			Double.toString(typeface.getTopSideBearing()));
		m_txtAscender.setText(
			Double.toString(typeface.getAscender()));
		m_txtXHeight.setText(
			Double.toString(typeface.getXHeight()));
		m_txtDescender.setText(
			Double.toString(typeface.getDescender()));
		m_txtBottomBearing.setText(
			Double.toString(typeface.getBottomSideBearing()));

		m_sldBaseline.setValue((int) typeface.getBaseline());
		m_sldMeanline.setValue((int) typeface.getMeanline());			
		m_sldBodyTop.setValue((int) typeface.getBodyTop());
		m_sldBodyBottom.setValue((int) typeface.getBodyBottom());
		

		
	
		
		
		m_isRefreshing = false;
	}
	
	public void show() {
		refresh();
		super.show();
	}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        m_pnlRoot = new javax.swing.JPanel();
        m_tabbedPanel = new javax.swing.JTabbedPane();
        m_pnlTab1 = new javax.swing.JPanel();
        m_pnlFontInfo = new javax.swing.JPanel();
        m_txtFamilyName = new javax.swing.JTextField();
        m_btnChangeFamilyName = new javax.swing.JButton();
        m_lblFamily = new javax.swing.JLabel();
        m_lblVersion = new javax.swing.JLabel();
        m_txtVersion = new javax.swing.JTextField();
        m_btnChangeVersion = new javax.swing.JButton();
        m_cmbSubFamily = new javax.swing.JComboBox();
        m_pnlCopyrightInfo = new javax.swing.JPanel();
        m_txtAuthor = new javax.swing.JTextField();
        m_btnChangeAuthor = new javax.swing.JButton();
        m_txtYear = new javax.swing.JTextField();
        m_btnChangeYear = new javax.swing.JButton();
        m_lblAuthor = new javax.swing.JLabel();
        m_lblYear = new javax.swing.JLabel();
        m_pnlLicense = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_txtLicese = new javax.swing.JTextArea();
        m_pnlCodePages = new javax.swing.JPanel();
        m_scrollPane = new javax.swing.JScrollPane();
        m_lstCodePages = new javax.swing.JList();
        m_btnRemoveCodePage = new javax.swing.JButton();
        m_cmbCodePages = new javax.swing.JComboBox();
        m_btnAddCodePage = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        m_pnlMetrics = new javax.swing.JPanel();
        m_lblAscender = new javax.swing.JLabel();
        m_lblXHeight = new javax.swing.JLabel();
        m_lblTopBearing = new javax.swing.JLabel();
        m_lblDescender = new javax.swing.JLabel();
        m_lblBottomBearing = new javax.swing.JLabel();
        m_txtAscender = new javax.swing.JTextField();
        m_txtDescender = new javax.swing.JTextField();
        m_txtXHeight = new javax.swing.JTextField();
        m_txtTopBearing = new javax.swing.JTextField();
        m_txtBottomBearing = new javax.swing.JTextField();
        m_sldBaseline = new javax.swing.JSlider();
        m_lblBaseline = new javax.swing.JLabel();
        m_sldMeanline = new javax.swing.JSlider();
        m_lblMeanline = new javax.swing.JLabel();
        m_sldBodyBottom = new javax.swing.JSlider();
        m_lblBodyBottom = new javax.swing.JLabel();
        m_sldBodyTop = new javax.swing.JSlider();
        m_lblBodyTop = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        m_btnClose = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        m_pnlRoot.setLayout(new java.awt.BorderLayout());

        m_pnlRoot.setPreferredSize(new java.awt.Dimension(350, 500));
        m_pnlTab1.setLayout(null);

        m_pnlFontInfo.setLayout(null);

        m_pnlFontInfo.setBorder(new javax.swing.border.TitledBorder("font info"));
        m_txtFamilyName.setEditable(false);
        m_txtFamilyName.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlFontInfo.add(m_txtFamilyName);
        m_txtFamilyName.setBounds(110, 20, 140, 20);

        m_btnChangeFamilyName.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnChangeFamilyName.setText("...");
        m_btnChangeFamilyName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeFamilyNameActionPerformed(evt);
            }
        });

        m_pnlFontInfo.add(m_btnChangeFamilyName);
        m_btnChangeFamilyName.setBounds(260, 20, 20, 20);

        m_lblFamily.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblFamily.setText("font family:");
        m_pnlFontInfo.add(m_lblFamily);
        m_lblFamily.setBounds(10, 20, 90, 15);

        m_lblVersion.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblVersion.setText("version:");
        m_pnlFontInfo.add(m_lblVersion);
        m_lblVersion.setBounds(10, 70, 90, 15);

        m_txtVersion.setEditable(false);
        m_txtVersion.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlFontInfo.add(m_txtVersion);
        m_txtVersion.setBounds(110, 70, 140, 20);

        m_btnChangeVersion.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnChangeVersion.setText("...");
        m_btnChangeVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeVersionActionPerformed(evt);
            }
        });

        m_pnlFontInfo.add(m_btnChangeVersion);
        m_btnChangeVersion.setBounds(260, 70, 20, 20);

        m_cmbSubFamily.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Regular" }));
        m_cmbSubFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cmbSubFamilyActionPerformed(evt);
            }
        });

        m_pnlFontInfo.add(m_cmbSubFamily);
        m_cmbSubFamily.setBounds(110, 40, 140, 25);

        m_pnlTab1.add(m_pnlFontInfo);
        m_pnlFontInfo.setBounds(10, 0, 310, 100);

        m_pnlCopyrightInfo.setLayout(null);

        m_pnlCopyrightInfo.setBorder(new javax.swing.border.TitledBorder("copyright info"));
        m_txtAuthor.setEditable(false);
        m_txtAuthor.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlCopyrightInfo.add(m_txtAuthor);
        m_txtAuthor.setBounds(110, 20, 140, 20);

        m_btnChangeAuthor.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnChangeAuthor.setText("...");
        m_btnChangeAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeAuthorActionPerformed(evt);
            }
        });

        m_pnlCopyrightInfo.add(m_btnChangeAuthor);
        m_btnChangeAuthor.setBounds(260, 20, 20, 20);

        m_txtYear.setEditable(false);
        m_txtYear.setPreferredSize(new java.awt.Dimension(50, 20));
        m_pnlCopyrightInfo.add(m_txtYear);
        m_txtYear.setBounds(110, 40, 140, 20);

        m_btnChangeYear.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnChangeYear.setText("...");
        m_btnChangeYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnChangeYearActionPerformed(evt);
            }
        });

        m_pnlCopyrightInfo.add(m_btnChangeYear);
        m_btnChangeYear.setBounds(260, 40, 20, 20);

        m_lblAuthor.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblAuthor.setText("author:");
        m_pnlCopyrightInfo.add(m_lblAuthor);
        m_lblAuthor.setBounds(10, 20, 60, 15);

        m_lblYear.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblYear.setText("year:");
        m_pnlCopyrightInfo.add(m_lblYear);
        m_lblYear.setBounds(10, 40, 60, 15);

        m_pnlTab1.add(m_pnlCopyrightInfo);
        m_pnlCopyrightInfo.setBounds(10, 100, 310, 70);

        m_pnlLicense.setLayout(null);

        m_pnlLicense.setBorder(new javax.swing.border.TitledBorder("license"));
        m_txtLicese.setLineWrap(true);
        m_txtLicese.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_txtLiceseFocusLost(evt);
            }
        });

        jScrollPane1.setViewportView(m_txtLicese);

        m_pnlLicense.add(jScrollPane1);
        jScrollPane1.setBounds(10, 20, 290, 50);

        m_pnlTab1.add(m_pnlLicense);
        m_pnlLicense.setBounds(10, 170, 310, 80);

        m_pnlCodePages.setLayout(null);

        m_pnlCodePages.setBorder(new javax.swing.border.TitledBorder("code pages"));
        m_lstCodePages.setFont(new java.awt.Font("Dialog", 0, 12));
        m_lstCodePages.setModel(m_codePagesModel);
        m_lstCodePages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        m_scrollPane.setViewportView(m_lstCodePages);

        m_pnlCodePages.add(m_scrollPane);
        m_scrollPane.setBounds(10, 20, 210, 50);

        m_btnRemoveCodePage.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnRemoveCodePage.setText("remove");
        m_btnRemoveCodePage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnRemoveCodePageActionPerformed(evt);
            }
        });

        m_pnlCodePages.add(m_btnRemoveCodePage);
        m_btnRemoveCodePage.setBounds(230, 20, 75, 26);

        m_cmbCodePages.setFont(new java.awt.Font("Dialog", 0, 11));
        m_pnlCodePages.add(m_cmbCodePages);
        m_cmbCodePages.setBounds(10, 80, 210, 20);

        m_btnAddCodePage.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnAddCodePage.setText("add");
        m_btnAddCodePage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnAddCodePageActionPerformed(evt);
            }
        });

        m_pnlCodePages.add(m_btnAddCodePage);
        m_btnAddCodePage.setBounds(230, 80, 80, 26);

        m_pnlTab1.add(m_pnlCodePages);
        m_pnlCodePages.setBounds(10, 250, 320, 120);

        m_tabbedPanel.addTab("font info", m_pnlTab1);

        jPanel1.setLayout(null);

        m_pnlMetrics.setLayout(null);

        m_pnlMetrics.setBorder(new javax.swing.border.TitledBorder("metrics"));
        m_lblAscender.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblAscender.setText("ascender height:");
        m_pnlMetrics.add(m_lblAscender);
        m_lblAscender.setBounds(10, 40, 100, 15);

        m_lblXHeight.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblXHeight.setText("x-height:");
        m_pnlMetrics.add(m_lblXHeight);
        m_lblXHeight.setBounds(10, 60, 100, 15);

        m_lblTopBearing.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblTopBearing.setText("top-side bearing:");
        m_pnlMetrics.add(m_lblTopBearing);
        m_lblTopBearing.setBounds(10, 20, 100, 15);

        m_lblDescender.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblDescender.setText("descender depth:");
        m_pnlMetrics.add(m_lblDescender);
        m_lblDescender.setBounds(10, 80, 100, 15);

        m_lblBottomBearing.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblBottomBearing.setText("bottom-side bearing:");
        m_pnlMetrics.add(m_lblBottomBearing);
        m_lblBottomBearing.setBounds(10, 100, 100, 15);

        m_txtAscender.setEditable(false);
        m_pnlMetrics.add(m_txtAscender);
        m_txtAscender.setBounds(120, 40, 50, 20);

        m_txtDescender.setEditable(false);
        m_pnlMetrics.add(m_txtDescender);
        m_txtDescender.setBounds(120, 80, 50, 20);

        m_txtXHeight.setEditable(false);
        m_pnlMetrics.add(m_txtXHeight);
        m_txtXHeight.setBounds(120, 60, 50, 20);

        m_txtTopBearing.setEditable(false);
        m_pnlMetrics.add(m_txtTopBearing);
        m_txtTopBearing.setBounds(120, 20, 50, 20);

        m_txtBottomBearing.setEditable(false);
        m_pnlMetrics.add(m_txtBottomBearing);
        m_txtBottomBearing.setBounds(120, 100, 50, 20);

        jPanel1.add(m_pnlMetrics);
        m_pnlMetrics.setBounds(10, 10, 320, 140);

        m_sldBaseline.setFont(new java.awt.Font("Dialog", 0, 10));
        m_sldBaseline.setMajorTickSpacing(85);
        m_sldBaseline.setMaximum(1024);
        m_sldBaseline.setOrientation(javax.swing.JSlider.VERTICAL);
        m_sldBaseline.setPaintTicks(true);
        m_sldBaseline.setValue(0);
        m_sldBaseline.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_sldBaselineMouseReleased(evt);
            }
        });

        jPanel1.add(m_sldBaseline);
        m_sldBaseline.setBounds(90, 180, 50, 230);

        m_lblBaseline.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblBaseline.setText("baseline:");
        jPanel1.add(m_lblBaseline);
        m_lblBaseline.setBounds(90, 160, 80, 15);

        m_sldMeanline.setFont(new java.awt.Font("Dialog", 0, 10));
        m_sldMeanline.setMajorTickSpacing(85);
        m_sldMeanline.setMaximum(1024);
        m_sldMeanline.setOrientation(javax.swing.JSlider.VERTICAL);
        m_sldMeanline.setPaintTicks(true);
        m_sldMeanline.setValue(0);
        m_sldMeanline.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_sldMeanlineMouseReleased(evt);
            }
        });

        jPanel1.add(m_sldMeanline);
        m_sldMeanline.setBounds(170, 180, 50, 230);

        m_lblMeanline.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblMeanline.setText("meanline:");
        jPanel1.add(m_lblMeanline);
        m_lblMeanline.setBounds(170, 160, 80, 15);

        m_sldBodyBottom.setFont(new java.awt.Font("Dialog", 0, 10));
        m_sldBodyBottom.setMajorTickSpacing(85);
        m_sldBodyBottom.setMaximum(1024);
        m_sldBodyBottom.setOrientation(javax.swing.JSlider.VERTICAL);
        m_sldBodyBottom.setPaintTicks(true);
        m_sldBodyBottom.setValue(0);
        m_sldBodyBottom.setEnabled(false);
        jPanel1.add(m_sldBodyBottom);
        m_sldBodyBottom.setBounds(10, 180, 50, 230);

        m_lblBodyBottom.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblBodyBottom.setText("body bottom:");
        jPanel1.add(m_lblBodyBottom);
        m_lblBodyBottom.setBounds(10, 160, 80, 15);

        m_sldBodyTop.setFont(new java.awt.Font("Dialog", 0, 10));
        m_sldBodyTop.setMajorTickSpacing(85);
        m_sldBodyTop.setMaximum(1024);
        m_sldBodyTop.setOrientation(javax.swing.JSlider.VERTICAL);
        m_sldBodyTop.setPaintTicks(true);
        m_sldBodyTop.setValue(0);
        m_sldBodyTop.setEnabled(false);
        jPanel1.add(m_sldBodyTop);
        m_sldBodyTop.setBounds(250, 180, 50, 230);

        m_lblBodyTop.setFont(new java.awt.Font("Dialog", 0, 11));
        m_lblBodyTop.setText("body top:");
        jPanel1.add(m_lblBodyTop);
        m_lblBodyTop.setBounds(250, 160, 80, 15);

        m_tabbedPanel.addTab("metrics", jPanel1);

        m_pnlRoot.add(m_tabbedPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(null);

        jPanel3.setPreferredSize(new java.awt.Dimension(0, 50));
        m_btnClose.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnClose.setText("close");
        m_btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnCloseActionPerformed(evt);
            }
        });

        jPanel3.add(m_btnClose);
        m_btnClose.setBounds(240, 10, 80, 26);

        m_pnlRoot.add(jPanel3, java.awt.BorderLayout.SOUTH);

        getContentPane().add(m_pnlRoot, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void m_sldMeanlineMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_sldMeanlineMouseReleased
        int value = m_sldMeanline.getValue();
        m_engine.setMeanline(value);
    }//GEN-LAST:event_m_sldMeanlineMouseReleased

    private void m_sldBaselineMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_sldBaselineMouseReleased
    	int value = m_sldBaseline.getValue();
    	m_engine.setBaseline(value);
    }//GEN-LAST:event_m_sldBaselineMouseReleased

    private void m_cmbSubFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cmbSubFamilyActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_m_cmbSubFamilyActionPerformed

    private void m_btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnCloseActionPerformed
       hide();
    }//GEN-LAST:event_m_btnCloseActionPerformed

    private void m_btnChangeVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeVersionActionPerformed
        
    }//GEN-LAST:event_m_btnChangeVersionActionPerformed

    private void m_btnChangeFamilyNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeFamilyNameActionPerformed
        String name = JOptionPane.showInputDialog(this, m_msgFamily);
        if (name == null)
        	return;
        
        m_engine.setFontFamilyName(name);
    }//GEN-LAST:event_m_btnChangeFamilyNameActionPerformed

    private void m_txtLiceseFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_txtLiceseFocusLost
    	m_engine.setTypefaceLicense(m_txtLicese.getText());	
    }//GEN-LAST:event_m_txtLiceseFocusLost

    private void m_btnChangeYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeYearActionPerformed
        String year = JOptionPane.showInputDialog(this, m_msgYear);
        if (year == null)
        	return;
        	
        m_engine.setCopyrightYear(year);
    }//GEN-LAST:event_m_btnChangeYearActionPerformed

    private void m_btnChangeAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnChangeAuthorActionPerformed
		String author = JOptionPane.showInputDialog(this, m_msgAuthor);        
        if (author == null)
        	return;
        
        m_engine.setAuthor(author);
    }//GEN-LAST:event_m_btnChangeAuthorActionPerformed

    private void m_btnRemoveCodePageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnRemoveCodePageActionPerformed
		if (m_lstCodePages.getSelectedIndex() < 0)
			return;
		
		String codePage = m_lstCodePages.getSelectedValue().toString();
		if (codePage == null)
			return;
		
		m_engine.removeCodePage(codePage);
    }//GEN-LAST:event_m_btnRemoveCodePageActionPerformed

    private void m_btnAddCodePageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnAddCodePageActionPerformed
		String codePage = m_cmbCodePages.getSelectedItem().toString();
		if (codePage == null)
			return;
					
		m_engine.addCodePage(codePage);
    }//GEN-LAST:event_m_btnAddCodePageActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_btnAddCodePage;
    private javax.swing.JButton m_btnChangeAuthor;
    private javax.swing.JButton m_btnChangeFamilyName;
    private javax.swing.JButton m_btnChangeVersion;
    private javax.swing.JButton m_btnChangeYear;
    private javax.swing.JButton m_btnClose;
    private javax.swing.JButton m_btnRemoveCodePage;
    private javax.swing.JComboBox m_cmbCodePages;
    private javax.swing.JComboBox m_cmbSubFamily;
    private javax.swing.JLabel m_lblAscender;
    private javax.swing.JLabel m_lblAuthor;
    private javax.swing.JLabel m_lblBaseline;
    private javax.swing.JLabel m_lblBodyBottom;
    private javax.swing.JLabel m_lblBodyTop;
    private javax.swing.JLabel m_lblBottomBearing;
    private javax.swing.JLabel m_lblDescender;
    private javax.swing.JLabel m_lblFamily;
    private javax.swing.JLabel m_lblMeanline;
    private javax.swing.JLabel m_lblTopBearing;
    private javax.swing.JLabel m_lblVersion;
    private javax.swing.JLabel m_lblXHeight;
    private javax.swing.JLabel m_lblYear;
    private javax.swing.JList m_lstCodePages;
    private javax.swing.JPanel m_pnlCodePages;
    private javax.swing.JPanel m_pnlCopyrightInfo;
    private javax.swing.JPanel m_pnlFontInfo;
    private javax.swing.JPanel m_pnlLicense;
    private javax.swing.JPanel m_pnlMetrics;
    private javax.swing.JPanel m_pnlRoot;
    private javax.swing.JPanel m_pnlTab1;
    private javax.swing.JScrollPane m_scrollPane;
    private javax.swing.JSlider m_sldBaseline;
    private javax.swing.JSlider m_sldBodyBottom;
    private javax.swing.JSlider m_sldBodyTop;
    private javax.swing.JSlider m_sldMeanline;
    private javax.swing.JTabbedPane m_tabbedPanel;
    private javax.swing.JTextField m_txtAscender;
    private javax.swing.JTextField m_txtAuthor;
    private javax.swing.JTextField m_txtBottomBearing;
    private javax.swing.JTextField m_txtDescender;
    private javax.swing.JTextField m_txtFamilyName;
    private javax.swing.JTextArea m_txtLicese;
    private javax.swing.JTextField m_txtTopBearing;
    private javax.swing.JTextField m_txtVersion;
    private javax.swing.JTextField m_txtXHeight;
    private javax.swing.JTextField m_txtYear;
    // End of variables declaration//GEN-END:variables
    
}
