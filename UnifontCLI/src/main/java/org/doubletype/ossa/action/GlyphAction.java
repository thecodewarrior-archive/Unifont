/*
 * $Id: GlyphAction.java,v 1.11 2004/09/03 07:35:09 eed3si9n Exp $
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
 
package org.doubletype.ossa.action;

import java.awt.*;
import java.awt.event.*;

import org.doubletype.ossa.*;
import org.doubletype.ossa.module.*;


/**
 * @author e.e
 */
public class GlyphAction {	
    public static final GlyphAction PICK_ACTION = new PickAction();
    public static final GlyphAction WALLPAPER_ACTION = new WallpaperAction();
    public static final GlyphAction PICK_POINT_ACTION = new PickPointAction();
    public static final GlyphAction PREVIEW_ACTION = new PreviewAction();
    
    protected static final String k_arrow = "arrow.gif";
    protected static final String k_arrowAdd = "arrow_add.gif";
    protected static final String k_arrowRemove = "arrow_remove.gif";
    protected static final String k_arrowMove = "arrow_move.gif";
	
	private static final GlyphAction s_actions[]
			= {PICK_ACTION, WALLPAPER_ACTION,
				PICK_POINT_ACTION, 
				PREVIEW_ACTION};
	
	private static GlyphAction s_action = PICK_ACTION;
	
	public static GlyphAction getAction() {
		return s_action;
	}
	
	/** Is points and hints visible for display or picking?
	 * @return true if they are visible; false, otherwise.
	 */
	public static boolean isPointVisible() {
	    return (s_action == PICK_POINT_ACTION);
	}
	
	public static void setAction(GlyphAction a_action) {
		s_action = a_action;
	}
	
	public static String getActionString() {
		return getAction().toString();	
	}
	
	public static GlyphAction forKey(String a_key) {
		GlyphAction retval = PICK_ACTION;
		
		int i;
		for (i = 0; i < s_actions.length; i++) {
			if (s_actions[i].toString().equals(a_key)) {
				return s_actions[i];
			} // if
		} // for i
		
		return retval;
	}
	
	protected String m_key;
	protected String m_cursorKey;
	protected Cursor m_cursor;
	protected GlyphDisplay m_display;
	protected Point m_lastDragged;
	
	protected GlyphAction(String a_key, String a_cursorKey) {
		m_key = a_key;
		m_cursorKey = a_cursorKey;
		m_cursor = createCursor(m_cursorKey);
	}
	
	protected Cursor createCursor(String a_key) {
	    Cursor retval = null;
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    
	    try {
			retval = toolkit.createCustomCursor(
				toolkit.createImage(Engine.class.getResource(a_key)),
				new Point(0, 0), a_key);
		}
		catch (Exception e) {
			e.printStackTrace();	
		} // try-catch
		
		return retval;
	}
	
	protected boolean isControlPressed(MouseEvent a_event) {
	    return ((a_event.getModifiersEx()
				& KeyEvent.CTRL_DOWN_MASK) != 0);
	}
	
	public void setDisplay(GlyphDisplay a_display) {
		m_display = a_display;
	}
	
	public Cursor getCursor() {
		return m_cursor;
	}
	
	public String getKey() {
	    return m_key;
	}
	
	public String toString() {
		return m_key;
	}
	
	public void mousePressed(MouseEvent a_event, GlyphFile a_file) {
	    // processPopUpMenu(a_event, a_file);
	}
	
	public void mouseDragged(MouseEvent a_event, GlyphFile a_file) {
	}
	
	public void mouseReleased(MouseEvent a_event, GlyphFile a_file) {
	    // processPopUpMenu(a_event, a_file);
	}
	
	/*
	protected void processPopUpMenu(MouseEvent a_event, GlyphFile a_file) {
	    if (!a_event.isPopupTrigger()) {
	        return;
	    } // if
	    
	    JPopupMenu menu = createPopUpMenu(a_event, a_file);
	    if (menu == null) {
	        return;
	    } // if
	    
	    DrawFrame frame = DrawFrame.getForm(a_file);
	    if (frame == null) {
	        return;
	    } // if
	    
	    menu.show(a_event.getComponent(), 
	            a_event.getX(), a_event.getY());
	}
	
	protected JPopupMenu createPopUpMenu(MouseEvent a_event, GlyphFile a_file) {
	    return null;
	}
	*/
}
