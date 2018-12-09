/*
 * $Id: PickAction.java,v 1.9 2004/11/15 03:39:39 eed3si9n Exp $
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
import java.awt.geom.*;
import java.awt.event.*;

import org.doubletype.ossa.module.*;

/**
 * @author e.e
 */
public class PickAction extends GlyphAction {
	private Point m_firstPoint = null;
	private Rectangle2D m_rectPick = null;
	private Cursor m_moveCursor;
	private Cursor m_pickCursor;
	
	public PickAction() {
		super("pick", k_arrow);
		
		m_pickCursor = m_cursor;
		m_moveCursor = createCursor(k_arrowMove);
	}
	
	public PickAction(String a_key) {
	    super(a_key, k_arrow);
	}
	
	public Rectangle2D getRectPick() {
		return m_rectPick;
	}
	
	public void mousePressed(MouseEvent a_event, GlyphFile a_file) {
	    super.mousePressed(a_event, a_file);
	    
		Point2D logical = m_display.toLogical(a_event.getPoint());
		
		// first point needed for drawing select rect
		m_firstPoint = a_event.getPoint();
		m_lastDragged = a_event.getPoint();
		m_rectPick = null;
		
		if (a_file.isHittingSelected(logical)
			|| a_file.hit(logical, isControlPressed(a_event), true)) {
		    a_file.beginMove();
		    m_cursor = m_moveCursor;
		} // if
	}
	
	public void mouseDragged(MouseEvent a_event, GlyphFile a_file) {
		if (m_lastDragged == null || m_firstPoint == null) {
			m_lastDragged = a_event.getPoint();
			m_firstPoint = a_event.getPoint();
			return;
		} // if
		
		if (a_file.isMoving()) {
		    move(a_event, a_file);
		} else {
		    buildRectAndPick(a_event, a_file);
		} // if-else
	}
	
	private void buildRectAndPick(MouseEvent a_event, GlyphFile a_file) {
		Point2D endPoint = m_display.toLogical(a_event.getPoint());
		Point2D startPoint = m_display.toLogical(m_firstPoint);

		double x, y, width, height;
		x = Math.min(startPoint.getX(), endPoint.getX());
		y = Math.min(startPoint.getY(), endPoint.getY());
		width = Math.abs(startPoint.getX() - endPoint.getX());
		height = Math.abs(startPoint.getY()- endPoint.getY());
		
		m_rectPick = new Rectangle2D.Double(x, y, width, height);
		
		a_file.hit(m_rectPick, isControlPressed(a_event), false);
	}
	
	private void move(MouseEvent a_event, GlyphFile a_file) {		
		Point2D endPoint = m_display.toLogical(a_event.getPoint());
		Point2D startPoint = m_display.toLogical(m_lastDragged);
		m_lastDragged = a_event.getPoint();

		if (endPoint.distance(startPoint) > 1000.0)
			return;

		double x = endPoint.getX() - startPoint.getX();
		double y = endPoint.getY() - startPoint.getY();

		Point2D delta = new Point2D.Double(x, y);
		a_file.move(delta);
	}
	
	public void mouseReleased(MouseEvent a_event, GlyphFile a_file) {
		super.mouseReleased(a_event, a_file);
	    
	    m_lastDragged = null;
		m_firstPoint = null;
		m_rectPick = null;
		
	    if (0 != (a_event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)) {
	        a_file.fitMove();
	    } // if
		
		a_file.buildPointHost();
		
		if (a_file.isMoving()) {
		    a_file.endMove();
		    m_cursor = m_pickCursor;
		} // if
	}
}
