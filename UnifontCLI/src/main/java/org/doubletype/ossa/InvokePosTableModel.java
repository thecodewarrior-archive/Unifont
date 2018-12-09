/*
 * $Id: InvokePosTableModel.java,v 1.5 2004/05/02 08:42:18 eed3si9n Exp $
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

import javax.swing.table.AbstractTableModel;
import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.module.*;

/**
 * @author e.e
 */
public class InvokePosTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	public static final int MODULE_POS = 1;
	public static final int INCLUDE_POS = 2;
	
	private XInvokePos m_pos;
	protected int m_type;
	protected GlyphFile m_file;
	
	public InvokePosTableModel(int a_type) {
		super();
			
		m_type = a_type;
	}
	
	protected void reset() {
		m_pos = null;
		
		Engine engine = Engine.getSingletonInstance();
		ActiveList actives = ActiveList.getSingletonInstance();
		m_file = engine.getRoot();
		
		if (m_file == null)
			return;
				
		if (m_type == MODULE_POS) {
			if (!actives.hasActiveModule())
				return;
			
			XModule module = actives.getActiveModule();
			m_pos = module.getInvoke().getInvokePos();	
		} else {
			if (!actives.hasActiveInclude())
				return;
			
			XInclude include = actives.getActiveInclude();
			m_pos = include.getInvoke().getInvokePos();
		} // if-else
	}
	
	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		reset();
		
		if (m_pos == null)
			return 0;
		
		return 1;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int a_row, int a_column) {			
		reset();
		Object retval = null;
		
		if (m_pos == null)
			return null;
		
		if (a_column == 0) {
			retval = new Double(m_pos.getPoint2d().getX());
		} else {
			retval = new Double(m_pos.getPoint2d().getY());
		} // if
		
		return retval;
	}
	
	public Class getColumnClass(int a_column) {
		return Double.class;
	}
	
	public boolean isCellEditable(int a_row, int a_column) {
			return true;	
	}

	public String getColumnName(int a_column) {
		String retval = "";
		
		if (a_column == 0) {
			retval = "x";		
		} else {
			retval = "y";
		} // if-else
		
		return retval;
	}
}
