/*
 * $Id: PointTableModel.java,v 1.7 2004/09/04 21:54:19 eed3si9n Exp $
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

import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.adapter.*;

class PointTableModel extends MyTableModel {	
	private XContourPoint [] m_points = null;
	
	public PointTableModel(Engine a_engine) {
		super(a_engine);
	}
	
	public int getRowCount() {
		reset();
		
		if (m_points == null)
			return 0;
		
		return m_points.length;
	}	
	
	public int getColumnCount() {
		return 4;
	}
	
	public void reset() {
		m_points = null;
		m_file = m_engine.getRoot();
		ActiveList actives = ActiveList.getSingletonInstance();
		
		
		if (m_file == null)
			return;
		if (!actives.hasActiveContour()) {
			return;	
		} // if
		
		m_points = actives.getActiveContour().getContourPoint();
	}
	
	public Object getValueAt(int a_row, int a_column) {			
		reset();
		
		if (m_points == null)
			return null; 
		
		Object retval = null;
		
		switch (a_column) {
			case 0: {
				retval = new Integer(a_row);
			} break;
			
			case 1: {
				retval 
					= new Double(m_points[a_row].getPoint2d().getX());
			} break;
			
			case 2: {
				retval
					= new Double(m_points[a_row].getPoint2d().getY());		
			} break;
			
			case 3: {
				EContourPoint point = (EContourPoint) m_points[a_row];
				
				retval = new Boolean(point.isOn());
			}
		} // switch
		
		return retval;
	} 
	
	public Class getColumnClass(int a_column) {
		Class retval = Double.class;
		
		if (a_column == 3) {
			retval = Boolean.class;
		} // if
		
		return retval;	
	}
	
	public String getColumnName(int a_column) {
		String retval = "";
		
		switch (a_column) {
			case 0: {
				retval = "id";
			} break;
			
			case 1: {
				retval = "x";
			} break;
			
			case 2: {
				retval = "y";		
			} break;
			
			case 3: {
				retval = "on curve";
			} break;
		} // switch
		
		return retval;
	}
	
	public boolean isCellEditable(int a_row, int a_column) {
		if (a_column == 0)
			return false;
		
		return true;	
	}
	
	public void setValueAt(Object a_value, int a_row, int a_column) {
		reset();
		EContourPoint point = (EContourPoint) m_points[a_row];
		
		switch (a_column) {
			case 1: {
				Double d = (Double) a_value;
				point.getPoint2d().setX(d.doubleValue());
			} break;
			
			case 2: {
				Double d = (Double) a_value;
				point.getPoint2d().setY(d.doubleValue());	
			} break;
			
			case 3: {
				Boolean b = (Boolean) a_value;
				point.setOn(b.booleanValue());
			} break;
		} // switch	
		
		m_engine.fireAction();	
	}
}
