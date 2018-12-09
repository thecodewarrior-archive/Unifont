/*
 * $Id: DrawFrame.java,v 1.27 2004/09/07 02:33:18 eed3si9n Exp $
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
import org.doubletype.ossa.truetype.TTUnicodeRange;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import org.doubletype.ossa.action.*;

/**
 *
 * @author  e.e
 */
public class DrawFrame extends javax.swing.JInternalFrame {
	private static final long serialVersionUID = 1L;
	static private ArrayList<DrawFrame> s_drawForms = new ArrayList<DrawFrame>();
	
	static public DrawFrame getForm(GlyphFile a_file) {		
		for (DrawFrame frame: s_drawForms) {
			if (frame.getFile() == a_file) {
				return frame;
			} // if
		} // for frame
		
		return null;
	}
	
	static public ArrayList getForms() {
		return s_drawForms;
	}
	
	static public boolean hasUnsavedChange() {		
		for (DrawFrame frame: s_drawForms) {
			if (frame.getFile().hasUnsavedChange()) {
				return true;
			} // if
		} // for frame
		
		return false;
	}
	
	static public void saveAll() {
		int i;
		for (i = 0; i < s_drawForms.size(); i++) {
			DrawFrame frame = (DrawFrame) s_drawForms.get(i);
			if (frame.getFile().hasUnsavedChange()) {
				frame.getFile().saveGlyphFile();
				frame.repaint();
			} // if
		} // for i
	}
	
	static public boolean closeAll() {
		int i;
		int numOfForms = s_drawForms.size();
		for (i = 0; i < numOfForms; i++) {
		    DrawFrame frame = (DrawFrame) s_drawForms.get(0);
		    frame.doDefaultCloseAction();
		    
		    if (frame.m_isCloseCancelled) {
		        return false;
		    } // if
		} // for
		
		return true;
	}

//	-------------------------------------------------------------------
	
    private GlyphFile m_file;
    private Engine m_engine;
	private Canvas m_canvas;
	private boolean m_isRefreshing = false;
	private boolean m_isCloseCancelled = false;

//	-------------------------------------------------------------------
    
    /** Creates new form DrawFrame */
    public DrawFrame(GlyphFile a_file, Engine a_engine) {
        m_engine = a_engine;
        m_file = a_file;
		s_drawForms.add(this);
		
		init();
        initComponents();
        setFrameIcon(null);
        pack();
		updateTitle();

		m_engine.addActionListener(
		        new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                engineActionPerformed(e);		
		            }
		        });
		refresh();
    }
    
    private void init() {    	
        m_engine.getDisplay().refreshZoom();
        
        m_canvas = new Canvas(new Renderer() {
			public boolean isRenderNeeded() {
				if (m_file == m_engine.getRoot()) {
					return true;
				} // if
				
				return false;
			}
			
			public void render(Graphics2D g) {
				m_engine.display(g, m_file);	 
			}
		});
    	
		m_canvas.setPreferredSize(new Dimension(360, 360));	
		m_canvas.setBackground(new Color(255, 255, 255));
		m_canvas.setOpaque(true);
		m_canvas.setRequestFocusEnabled(true);
    	    	
		m_canvas.addKeyListener(new KeyAdapter() {
    			
			/* (non-Javadoc)
			 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				m_engine.keyPressed(e);
				m_canvas.repaint();
			}
		});
    	
		m_canvas.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				m_engine.setRoot(m_file);
				m_engine.mousePressed(e);
				m_canvas.repaint();
				m_canvas.requestFocus();
			}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				m_engine.mouseReleased(e);
				m_canvas.repaint();
				m_canvas.requestFocus();
				updateTitle();
			}
		});
    	
		m_canvas.addMouseMotionListener(new MouseMotionAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
			 */
			public void mouseDragged(MouseEvent e) {
			    setCursor(GlyphAction.getAction().getCursor());
			    m_engine.mouseDragged(e);
				m_canvas.repaint();
			}
			
			public void mouseMoved(MouseEvent e) {
				setCursor(GlyphAction.getAction().getCursor());
			}

		});
    }
    
	public void engineActionPerformed(ActionEvent a_event) {
		refresh();
	}
	
	private void refresh() {
		m_isRefreshing = true;
		
		resizeCanvas();
		
		repaint();
		updateTitle();
		requestCanvasFocus();
		
		m_isRefreshing = false;
	}
	
	public boolean isRefreshing() {
		return m_isRefreshing;
	}
	
	public void resizeCanvas() {
	    double ratio = m_engine.getDisplay().getDisplayRatio();
	    int size = (int) (Engine.getEm() * ratio) + 100;
		if (m_canvas.getPreferredSize().height == size) {
			return;
		} // if
		
		m_canvas.setPreferredSize(new Dimension(size, size));
		m_scrollPane.revalidate();
	}
	
    public GlyphFile getFile() {
    	return m_file;
    }
    
    public void requestCanvasFocus() {
    	m_canvas.requestFocus();
    }
    
	public void updateTitle() {
		long unicode = m_file.getUnicodeAsLong();
	
		char c = ' ';
		if (unicode >= TTUnicodeRange.k_space) {
			c = (char) unicode;
		} // if
	
		String title = m_file.getGlyphTitle() + " (" + c + ")";
	
		if (m_file.hasUnsavedChange()) {
			title = "* " + title;
		} // if
	
		setTitle(title);
	}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        m_bgpMode = new javax.swing.ButtonGroup();
        m_bgpAction = new javax.swing.ButtonGroup();
        m_pnlRoot = new javax.swing.JPanel();
        m_scrollPane = new JScrollPane(m_canvas);

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setDefaultCloseOperation(0);
        setDoubleBuffered(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });

        m_pnlRoot.setLayout(new java.awt.BorderLayout());

        m_scrollPane.setBackground(new java.awt.Color(255, 255, 255));
        m_scrollPane.setPreferredSize(new java.awt.Dimension(500, 500));
        m_pnlRoot.add(m_scrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_pnlRoot, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        m_isCloseCancelled = false;
        
        if (m_file.hasUnsavedChange()) {
			int result = JOptionPane.showConfirmDialog(this,
			    m_engine.getBundle().getString("msgSaveChange"),
				getTitle(),
				JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				m_file.saveGlyphFile();
			} // if
			
			if (result == JOptionPane.CANCEL_OPTION) {
			    m_isCloseCancelled = true;
			    return;
			} // if
		} // if
		
		s_drawForms.remove(this);
		m_engine.setRoot(null);
		
		dispose();
		
		if (s_drawForms.size() == 0) {
			return;
		} // if
				
		DrawFrame drawFrame = (DrawFrame) s_drawForms.get(0);
		drawFrame.requestFocus();
    }//GEN-LAST:event_formInternalFrameClosing

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
    	repaint();
    }//GEN-LAST:event_formMouseEntered

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		m_canvas.requestFocus();
    }//GEN-LAST:event_formComponentResized

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
		m_canvas.requestFocus();
		
		m_engine.setRoot(m_file);
    }//GEN-LAST:event_formComponentMoved

    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
		repaint();
		m_canvas.requestFocus();
		
		m_engine.setRoot(m_file);
    }//GEN-LAST:event_formInternalFrameActivated

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
		repaint();
		m_canvas.requestFocus();
       
		m_engine.setRoot(m_file);
    }//GEN-LAST:event_formFocusGained
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup m_bgpAction;
    private javax.swing.ButtonGroup m_bgpMode;
    private javax.swing.JPanel m_pnlRoot;
    private javax.swing.JScrollPane m_scrollPane;
    // End of variables declaration//GEN-END:variables
    
}
