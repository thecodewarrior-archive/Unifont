/*
 * $Id: FileVarTableModel.java,v 1.5 2004/04/19 14:14:19 eed3si9n Exp $
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
public class FileVarTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	public static final int GLYPH_VAR = 1;
	public static final int TYPEFACE_VAR = 2;
	
	private XParamListParam [] m_params;
	private int m_type;
	protected GlyphFile m_file = null;
	protected TypefaceFile m_typeface = null;
	protected Engine m_engine;
	
	public FileVarTableModel(int a_type, Engine a_engine) {
		super();
		
		m_type = a_type;
		m_engine = a_engine;
	}
	
	public void reset() {
		m_params = null;
		m_typeface = m_engine.getTypeface();
		m_file = m_engine.getRoot();
		
		if (m_type == GLYPH_VAR && m_file != null) { 
			m_params = m_file.getGlyph().getHead()
				.getHeadGlobal().getParamListParam();
		} else if (m_type == TYPEFACE_VAR && m_typeface != null) {
			m_params = m_typeface.getGlyph().getHead()
				.getHeadGlobal().getParamListParam();
		} // if
	}
	
	
	public int getRowCount() {
		reset();
		
		if (m_params == null)
			return 0;
					
		return m_params.length;	
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public Object getValueAt(int a_row, int a_column) {			
		reset();
		XParamListParam param = m_params[a_row];
		
		Object retval = null;
		
		switch (a_column) {
			case 0: {
				retval = param.getName();
			} break;
			
			case 1: {
				retval = new Double(param.getContent());		
			} break;
		} // switch
		
		return retval;
	}
	
	public String getColumnName(int a_column) {
		String retval = "";
		
		switch (a_column) {
			case 0: {
				retval = "name";
			} break;
			
			case 1: {
				retval = "value";		
			} break;
		} // switch
		
		return retval;
	}
	
	public Class getColumnClass(int a_column) {
		Class retval = String.class;
		
		if (a_column == 1) {
			retval = Double.class;
		} // if
		
		return retval;	
	}
	
	public boolean isCellEditable(int a_row, int a_column) {
		return true;	
	}
	
	public void setValueAt(Object a_value, int a_row, int a_column) {
		reset();
		XParamListParam param = m_params[a_row];
		
		switch (a_column) {
			case 0: {
				param.setName(a_value.toString());
			} break;
			
			case 1: {
				Double d = (Double) a_value;
				param.setContent(d.doubleValue());		
			} break;
		} // switch
		
		if (m_type == TYPEFACE_VAR && m_typeface != null) {
			m_typeface.saveGlyphFile();	
		} // if
	}
}
