/*
 * $Id: InvokeTableModel.java,v 1.7 2004/05/02 08:42:18 eed3si9n Exp $
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
public class InvokeTableModel extends AbstractTableModel {
	public static final int MODULE_ARG = 1;
	public static final int MODULE_VARG = 2;
	public static final int INCLUDE_ARG = 3;
	public static final int INCLUDE_VARG = 4;
	public static final int FILES_MODEL = 5;
	
	private XInvokeArg [] m_args = null;
	private XInvokeVarg [] m_vargs = null;
	
	protected int m_type;
	protected GlyphFile m_file = null;
	protected Engine m_engine;
	
	public InvokeTableModel(int a_type, Engine a_engine) {
		super();
		
		m_type = a_type;
		m_engine = a_engine;
	}
	
	protected void reset() {
		m_args = null;
		m_vargs = null;
		m_file = m_engine.getRoot();
		ActiveList actives = ActiveList.getSingletonInstance();
		
		if (m_file == null)
			return;
		
		if (m_type == MODULE_ARG || m_type == MODULE_VARG) {
			if (!actives.hasActiveModule())
				return;
			
			XModule module = actives.getActiveModule();			
			m_args = module.getInvoke().getInvokeArg();
			m_vargs = module.getInvoke().getInvokeVarg();
		} else {
			if (!actives.hasActiveInclude())
				return;
			
			XInclude include = actives.getActiveInclude();
			m_args = include.getInvoke().getInvokeArg();
			m_vargs = include.getInvoke().getInvokeVarg();
		} // if-else
	}
	
	
	public int getRowCount() {
		reset();
		
		if (m_type == MODULE_ARG || m_type == INCLUDE_ARG) {
			if (m_args == null)
				return 0;	
			
			return m_args.length;
		} else {
			if (m_vargs == null)
				return 0;
			
			return m_vargs.length;
		} // if	
	}
	
	public int getColumnCount() {
		return 3;
	}
	
	public Object getValueAt(int a_row, int a_column) {			
		reset();
		Object retval = null;
		
		if (m_type == MODULE_ARG || m_type == INCLUDE_ARG) {
			XInvokeArg arg = m_args[a_row];
			
			switch (a_column) {
				case 0: {
					retval = arg.getName();
				} break;
				
				case 1: {
					retval = new Double(arg.getContent());
				} break;
			} // switch
		} else {
			XInvokeVarg varg = m_vargs[a_row];
			
			switch (a_column) {
				case 0: {
					retval = varg.getName();
				} break;
				
				case 1: {
					retval = varg.getSrc();
				} break;
			 	
			} // switch
		} // if	
		
		return retval;
	}
	
	public String getColumnName(int a_column) {
		String retval = "";
		
		if (m_type == MODULE_ARG || m_type == INCLUDE_ARG) {
			switch (a_column) {
				case 0: {
					 retval = "arg";
				} break;
				
				case 1: {
					retval = "value";
				} break;
			} // switch
		} else {				
			switch (a_column) {
				case 0: {
					 retval = "varg";
				} break;
				
				case 1: {
					retval = "src";
				} break;
			} // switch
		} // if	
		
		return retval;
	}
	
	public Class getColumnClass(int a_column) {
		Class retval = String.class;
		
		if (m_type == MODULE_ARG || m_type == INCLUDE_ARG) {				
			if (a_column == 0) {
				retval = String.class;
			} else {
				retval = Double.class;
			} // if
		} else {				
			retval = String.class;
		} // if
		
		return retval;	
	}
	
	public boolean isCellEditable(int a_row, int a_column) {
		return true;	
	}
	
	public void setValueAt(Object a_value, int a_row, int a_column) {
		reset();
		
		if (m_type == MODULE_ARG || m_type == INCLUDE_ARG) {
			XInvokeArg arg = m_args[a_row];
			
			switch (a_column) {
				case 0: {
					arg.setName(a_value.toString());
				} break;
				
				case 1: {
					Double d = (Double) a_value;
					arg.setContent(d.doubleValue());
				} break;
			} // switch
		} else {
			XInvokeVarg varg = m_vargs[a_row];
						
			switch (a_column) {
				case 0: {
					varg.setName(a_value.toString());
				} break;
				
				case 1: {
					varg.setSrc(a_value.toString());
				} break;
			} // switch
		} // if		
	}
}
