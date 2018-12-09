/*
 * $Id: FontFrame.java,v 1.16 2004/10/05 06:34:19 eed3si9n Exp $
 * 
 * $Copyright: copyright (c) 2004, e.e d3si9n $
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
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

import org.doubletype.ossa.truetype.TTPixelSize;

/**
 *
 * @author  e.e
 */
public class FontFrame extends javax.swing.JInternalFrame {
	private static final long serialVersionUID = 1L;
	private final int k_firstCharacter = 0x20;
    
	private Engine m_engine;
    private Desktop m_parent;
    private Canvas m_glyphTable;
    private Font m_referenceFont;
    private Font m_targetFont;
    private Font m_font;
    private Font m_monospaced;
    private Font m_serif;
    private Font m_small;
    private AbstractDocument m_doc;
    
    
	private int m_gridWidth = 50;
	private int m_gridHeight = 50;
	private int m_numOfColumns = 11;
	private int m_numOfRows = 5;
	private int m_currentScroll = 0;
	
    
    /** Creates new form FontFrame */
    public FontFrame(Desktop a_parent, Engine a_engine) {
    	m_parent = a_parent;
    	m_engine = a_engine;
        
        initComponents();
        setLocation(350, 0);
        setFrameIcon(null);
        init();
        
        m_pnlGlyphTable.add(m_glyphTable, BorderLayout.CENTER);
        
        System.out.print("Initializing Monospaced..");
		m_monospaced = new Font("Monospaced", Font.PLAIN, 16);
		System.out.print("done\n");
		
		m_small = m_monospaced.deriveFont(12.0f);
		
		m_referenceFont = m_monospaced;
		
		m_serif = new Font("Serif", Font.PLAIN, 16);
		m_targetFont = m_serif;
		
		m_font = m_referenceFont;
		m_txtSample.setFont(m_font);
		
		m_scrollBar.setMinimum(0);
		m_scrollBar.setMaximum((int) (0x10000 / 11.0));
		
		System.out.println("init FontFrame done.");
    }
    
    private void init() {
		m_glyphTable = new Canvas(new Renderer() {
			public boolean isRenderNeeded() {
				if (m_currentScroll == m_scrollBar.getValue()) {
					return false;
				} // if
				
				m_currentScroll = m_scrollBar.getValue();
				return true;
			}
			
			public void render(Graphics2D g) {
				drawGlyphTable(g);	 
			}
		});
		
		m_doc = (AbstractDocument) m_txtSample.getStyledDocument();
		initDocument();
    }
    
    private void initDocument() {
    	SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontSize(attr, 16);
		try {
			m_doc.remove(0, m_doc.getLength());
			m_doc.insertString(m_doc.getLength(), "abcdefghijklmnopqrstuvwxyz\n", attr);
			m_doc.insertString(m_doc.getLength(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n", attr);
			m_doc.insertString(m_doc.getLength(), "1234567890 .:,;!?@#$%^&*()-_+=`~'\"|\\/<>{}[]\n", attr);
			String theQuickBrown = "The quick brown fox jumps over the lazy dog. "
				+ "1234567890\n";
			for (TTPixelSize pixelSize: m_engine.getPixelSizes()) {
	    		StyleConstants.setFontSize(attr, pixelSize.getPixel());
				m_doc.insertString(m_doc.getLength(), theQuickBrown, attr);
			} // for
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try-catch
		
		m_txtSample.setCaretPosition(0);
    }
    
    private void rerender() {
    	m_glyphTable.renderBuffer();
    }
    
    public void localize(ResourceBundle a_bundle) {
		m_cmbFont.setModel(new DefaultComboBoxModel(
			new String[] { a_bundle.getString("reference"),
				a_bundle.getString("target") }));
		m_cmbFont.invalidate();
		
		int i = 0;
		m_tab.setTitleAt(i++, a_bundle.getString("fontSample"));
		m_tab.setTitleAt(i++, a_bundle.getString("glyphTable"));
    }
    
    public void unsetTargetFont() {
    	setTargetFont(m_serif);
    }
    
    public void setTargetFont(Font a_font) {
    	m_targetFont = a_font;
    	m_cmbFont.setSelectedIndex(1);
    	m_txtSample.setFont(a_font);
    	rerender();
    }
	
	private void drawGlyphBox(Graphics2D g, int a_x, int a_y, long a_unicode) {
		if (a_unicode > 0xFFFF) {
			return;
		} // if
		
		int left = a_x * m_gridWidth;
		int top = a_y * m_gridHeight;
		
		g.setColor(Color.BLACK);
		g.drawRect(left, top, m_gridWidth, m_gridHeight);
		
		g.setFont(m_small);
		String s =  "(" + toHexString(a_unicode, 4) + ")"
			+ charToString(a_unicode);
		g.drawString(s, 2 + left, 49 + top);
		
		g.setFont(m_font);
		s = charToString(a_unicode);
		g.drawString(s, 10 + left, 30 + top);		
	} // if
		
	public void drawGlyphTable(Graphics2D g) {	
		int i, j;
		for (i = 0; i < m_numOfColumns; i++) {
			for (j = 0; j < m_numOfRows; j++) {
				drawGlyphBox(g, i, j, buildUnicodeFromGrid(i, j));
			} // for j
		} // for i
	}
	
	private long buildUnicodeFromGrid(int a_column, int a_row) {
		return(m_currentScroll + a_row) * m_numOfColumns + a_column + k_firstCharacter;
	}
	
	private void onMouseUp(int a_x, int a_y) {
		int column = a_x / m_gridWidth;
		int row = a_y / m_gridWidth;
		if (column >= m_numOfColumns || row >= m_numOfRows) {
			return;
		} // if
		
		long unicode = buildUnicodeFromGrid(column, row);
		if (unicode > 0xffff) {
			return;
		} // if
		
		m_parent.openGlyphFile(unicode);
	}
	
	private String charToString(long a_value) {
		String retval = "";
		char c = (char) a_value;
		retval = Character.toString(c); 
		return retval;
	}
	
	private String toHexString(long a_value, int a_length) {
		String retval;
		
		retval = Long.toHexString(a_value);
		if (retval.length() < a_length) {
			int pad = a_length - retval.length();
			int i;
			
			for (i = 0; i < pad; i++) {
				retval = "0" + retval;
			} // for i	
		} // if
		
		return retval;
	}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        m_pnlRoot = new javax.swing.JPanel();
        m_tab = new javax.swing.JTabbedPane();
        m_pnlSample = new javax.swing.JPanel();
        m_sclSample = new javax.swing.JScrollPane();
        m_txtSample = new javax.swing.JTextPane();
        m_plnGlyphTable = new javax.swing.JPanel();
        m_pnlGlyphTable = new javax.swing.JPanel();
        m_scrollBar = new javax.swing.JScrollBar();
        jPanel3 = new javax.swing.JPanel();
        m_cmbFont = new javax.swing.JComboBox();
        m_cmbUnicodeRange = new javax.swing.JComboBox();
        m_btnQuickBrown = new javax.swing.JButton();

        setIconifiable(true);
        m_pnlRoot.setLayout(new java.awt.BorderLayout());

        m_pnlSample.setLayout(new java.awt.BorderLayout());

        m_pnlSample.setBackground(new java.awt.Color(255, 255, 255));
        m_pnlSample.setPreferredSize(new java.awt.Dimension(10, 100));
        m_sclSample.setViewportView(m_txtSample);

        m_pnlSample.add(m_sclSample, java.awt.BorderLayout.CENTER);

        m_tab.addTab("Font Sample", m_pnlSample);

        m_plnGlyphTable.setLayout(new java.awt.BorderLayout());

        m_pnlGlyphTable.setLayout(new java.awt.BorderLayout());

        m_pnlGlyphTable.setBackground(new java.awt.Color(255, 255, 255));
        m_pnlGlyphTable.setMinimumSize(new java.awt.Dimension(220, 220));
        m_pnlGlyphTable.setPreferredSize(new java.awt.Dimension(570, 270));
        m_pnlGlyphTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_pnlGlyphTableMouseReleased(evt);
            }
        });
        m_pnlGlyphTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                m_pnlGlyphTableMouseMoved(evt);
            }
        });

        m_scrollBar.setBlockIncrement(4);
        m_scrollBar.setMaximum(4095);
        m_scrollBar.setVisibleAmount(5);
        m_scrollBar.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                m_scrollBarAdjustmentValueChanged(evt);
            }
        });
        m_scrollBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_scrollBarMouseReleased(evt);
            }
        });

        m_pnlGlyphTable.add(m_scrollBar, java.awt.BorderLayout.EAST);

        m_plnGlyphTable.add(m_pnlGlyphTable, java.awt.BorderLayout.CENTER);

        m_tab.addTab("Glyph Table", m_plnGlyphTable);

        m_pnlRoot.add(m_tab, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(null);

        jPanel3.setPreferredSize(new java.awt.Dimension(0, 30));
        m_cmbFont.setFont(new java.awt.Font("Dialog", 0, 12));
        m_cmbFont.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "reference", "target" }));
        m_cmbFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cmbFontActionPerformed(evt);
            }
        });

        jPanel3.add(m_cmbFont);
        m_cmbFont.setBounds(0, 0, 130, 25);

        m_cmbUnicodeRange.setFont(new java.awt.Font("Dialog", 0, 12));
        m_cmbUnicodeRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cmbUnicodeRangeActionPerformed(evt);
            }
        });

        jPanel3.add(m_cmbUnicodeRange);
        m_cmbUnicodeRange.setBounds(150, 0, 130, 25);

        m_btnQuickBrown.setFont(new java.awt.Font("Dialog", 0, 12));
        m_btnQuickBrown.setText("Quick Brown");
        m_btnQuickBrown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnQuickBrownActionPerformed(evt);
            }
        });

        jPanel3.add(m_btnQuickBrown);
        m_btnQuickBrown.setBounds(350, 0, 120, 26);

        m_pnlRoot.add(jPanel3, java.awt.BorderLayout.NORTH);

        getContentPane().add(m_pnlRoot, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void m_btnQuickBrownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnQuickBrownActionPerformed
        initDocument();
    }//GEN-LAST:event_m_btnQuickBrownActionPerformed

    private void m_pnlGlyphTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_pnlGlyphTableMouseReleased
    	if (evt.getClickCount() < 2)
    		return;
    	
    	onMouseUp(evt.getX(), evt.getY());
    }//GEN-LAST:event_m_pnlGlyphTableMouseReleased

    private void m_pnlGlyphTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_pnlGlyphTableMouseMoved
        // Add your handling code here:
    }//GEN-LAST:event_m_pnlGlyphTableMouseMoved

    private void m_scrollBarAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_m_scrollBarAdjustmentValueChanged
		repaint();
    }//GEN-LAST:event_m_scrollBarAdjustmentValueChanged

    private void m_scrollBarMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_scrollBarMouseReleased
		repaint();
    }//GEN-LAST:event_m_scrollBarMouseReleased

    private void m_cmbUnicodeRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cmbUnicodeRangeActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_m_cmbUnicodeRangeActionPerformed

    private void m_cmbFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cmbFontActionPerformed
    	int selected = m_cmbFont.getSelectedIndex();
    	
    	if (selected < 0)
    		return;
    		
    	if (selected == 0) {
    		m_font = m_referenceFont;
    	} else {
    		m_font = m_targetFont;
    	} // if
    	  
    	m_txtSample.setFont(m_font);
		rerender();
    }//GEN-LAST:event_m_cmbFontActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton m_btnQuickBrown;
    private javax.swing.JComboBox m_cmbFont;
    private javax.swing.JComboBox m_cmbUnicodeRange;
    private javax.swing.JPanel m_plnGlyphTable;
    private javax.swing.JPanel m_pnlGlyphTable;
    private javax.swing.JPanel m_pnlRoot;
    private javax.swing.JPanel m_pnlSample;
    private javax.swing.JScrollPane m_sclSample;
    private javax.swing.JScrollBar m_scrollBar;
    private javax.swing.JTabbedPane m_tab;
    private javax.swing.JTextPane m_txtSample;
    // End of variables declaration//GEN-END:variables
}
