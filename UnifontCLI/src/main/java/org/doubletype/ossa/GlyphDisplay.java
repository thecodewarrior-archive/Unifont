/*
 * $Id: GlyphDisplay.java,v 1.39 2004/10/04 02:25:39 eed3si9n Exp $
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

import java.awt.*;
import java.awt.geom.*;
import org.doubletype.ossa.truetype.*;
import org.doubletype.ossa.xml.*;
import org.doubletype.ossa.module.*;
import org.doubletype.ossa.action.*;


/**
 * @author e.e
 */
public class GlyphDisplay {
	private Graphics2D m_graphics = null;
	private int m_xOrigin = 0;
	private int m_yOrigin = 0;
	
	/** pixels per em. body size (1 em) meatured in pixel. */
	private int m_ppem;
	
	/** zoom rate. 100% will display 1024 px. */
	private double m_displayRatio;
	
	/**
	 * Trans matrix to screen coordinate system from logical 
	 */
	private AffineTransform m_glyphTransform;
	private Font m_menuFont;
    private Font m_numberFont;
    private BasicStroke m_dottedStroke;
	
	private GlyphFile m_rootGlyphFile = null;
	private XInclude m_currentInclude = null;
	private Engine m_engine;
	private Image m_backgroundImage = null;
	private int m_imageHeight;
	private boolean m_isShowBackground = true;
	private Point m_posBackground = new Point();
	private VarStack m_stack;
	
	/**
	 * for selecting module
	 */
	private XModule m_currentModule = null;

	// --------------------------------------------------------------
	
	public GlyphDisplay(Engine a_engine) {
		m_engine = a_engine;
		m_menuFont = new Font("Monospaced", Font.PLAIN, 12);
		
		setPpem(Engine.k_defaultPixelSize);
		setZoom(Engine.k_defaultZoom);

		m_stack = VarStack.getSingletonInstance();
	}
	
	public void setBackground(Image a_image, int a_height, TypefaceFile a_typeface) {
		m_backgroundImage = a_image;
		m_imageHeight = a_height;
		
		double typographicHeight = a_typeface.getEm() - a_typeface.getBaseline();
		int height = 50 + (int) (typographicHeight / 4);
		int y = height - (m_imageHeight / 2);
		
		m_posBackground = new Point(50, y);
	}
	
	public void setShowBackground(boolean a_value) {
		m_isShowBackground = a_value;
	}
	
	
	// --------------------------------------------------------------
	
	public void moveWallpaper(Point a_delta) {
		m_posBackground.x += a_delta.x;
		m_posBackground.y += a_delta.y;	
	}
	
	// --------------------------------------------------------------
	
	/** display request from engine.
	 */
	public void display(Graphics2D g, GlyphFile a_file, TypefaceFile a_typeface) {
		m_rootGlyphFile = a_file;
		m_currentInclude = null;
		m_graphics = (Graphics2D) g;
		AffineTransform oldTransform = m_graphics.getTransform();
		m_stack.clear();
		m_stack.push(a_typeface);
		m_stack.push(a_file);	
				
		if (GlyphAction.getAction() == GlyphAction.PREVIEW_ACTION) {
		    String s = Character.toString((char) a_file.getUnicodeAsLong());
			previewFont(a_typeface, s);
		    
		    m_graphics.transform(m_glyphTransform);
			Shape shape = a_file.toShape(new AffineTransform());
			previewDisplay(shape);
		} else {
			displayWallpaper();
			m_graphics.transform(m_glyphTransform);
			displayBackground(a_file, a_typeface);
			a_file.display(m_graphics, new AffineTransform());
		} // if
		
		
		displayRectPick();
		
	    m_graphics.setTransform(oldTransform);
	    m_stack.pop();
	    m_stack.pop();	
	}
	
	// --------------------------------------------------------------
	
	private void previewFont(TypefaceFile a_typeface, String a_unicode) {
	    Font font = a_typeface.getFont();
	    if (font == null) {
	        return;
	    } // if
	    
	    int x = 10;
	    int y = 30;
	    
	    m_graphics.setColor(Color.BLACK);
	    for (TTPixelSize pixelSize: TTPixelSize.getList()) {
	        m_graphics.setFont(m_menuFont);
	        m_graphics.drawString(pixelSize.getPixel() + ": ", x, y);
	        m_graphics.setFont(font.deriveFont((float) pixelSize.getPixel()));
	        x += 20;
	        m_graphics.drawString(a_unicode, x, y);
	        
	        x += pixelSize.getPixel();
	        
	        if (x > 550) {
	            x = 10;
	            y += 96;
	        } // if
	    } // for
	}
	
	// --------------------------------------------------------------
	
	private void displayRectPick() {
		if (GlyphAction.getAction() != GlyphAction.PICK_ACTION
		        && GlyphAction.getAction() != GlyphAction.PICK_POINT_ACTION) {
			return;
		} // if
		
		PickAction action = (PickAction) GlyphAction.getAction();
		Rectangle2D rect = action.getRectPick();
		
		if (rect == null) {
			return;
		} // if
		
		Stroke oldStroke = m_graphics.getStroke();
		m_graphics.setStroke(m_dottedStroke);
		m_graphics.setColor(Color.DARK_GRAY);
		m_graphics.draw(rect);
		m_graphics.setStroke(oldStroke);
	}

	// --------------------------------------------------------------
	
	private void displayWallpaper() {
		if (m_backgroundImage == null) {
			return;
		} // if
		
		if (!m_isShowBackground) {
			return;
		} // if
		
		m_graphics.drawImage(m_backgroundImage,
						m_posBackground.x, m_posBackground.y, null);
	}

	// --------------------------------------------------------------
	
	private void previewDisplay(Shape a_shape) {
		int i, j;
		double gridWidth = ((double) Engine.getEm()) / m_ppem;
		double left, bottom, x, y;
		
		m_graphics.setColor(GlyphColor.GRAY);
		
		for (i = -m_ppem; i <= m_ppem; i++) {
			for (j = -m_ppem; j <= m_ppem; j++) {
				left = j * gridWidth;
				bottom = i * gridWidth;
				x = left + gridWidth / 2;
				y = bottom + gridWidth / 2;
				
				if (a_shape.contains(x, y)) {
					Rectangle2D rect
						= new Rectangle2D.Double(left, bottom, gridWidth, gridWidth);
					m_graphics.fill(rect);
				} // if
			} // for j
		} // for i
	}
	
	// --------------------------------------------------------------
	
	public Point2D toLogical(Point a_point) {
		Point2D retval = new Point2D.Double();
		Point2D.Double source = new Point2D.Double(a_point.x, a_point.y);
		
		try {
			m_glyphTransform.inverseTransform(source, retval);
		} catch (Exception e) {
			return null;
		} // try-catch
		
		return retval;
	}
	
    // ------------------------------------------------------------------
	
	public void displayBackground(GlyphFile a_file,
			TypefaceFile a_typeface) {    	
    	double em = Engine.getEm();
    	int i, j;
		Line2D line = new Line2D.Double();
    	
    	i = 3;
    	
		m_graphics.setColor(GlyphColor.CROSS);
		double ascender = a_typeface.getAscender();
		line.setLine(-em, ascender, i * em, ascender);
		m_graphics.draw(line);
    	
		double descender = a_typeface.getDescender();
		line.setLine(-em, -descender, i * em, -descender);
		m_graphics.draw(line);
		
		double xHeight = a_typeface.getXHeight();
		line.setLine(-em, xHeight, i * em, xHeight);
		m_graphics.draw(line);
		    	
    	// grid    	
    	m_graphics.setColor(GlyphColor.CROSS);
    	
    	double funitInGrid = em / m_ppem;
    	double topSideBearing = a_typeface.getTopSideBearing();
    	double bottomSideBearing = a_typeface.getBottomSideBearing();
		double width = a_file.getAdvanceWidth();
		
    	int upperPixels = 0;
    	int lowerPixels = 0;
    	int rightPixels = 0;
    	if (ascender > 0) {
			upperPixels = (int) Math.ceil(
				(topSideBearing + ascender) / funitInGrid);
    	} // if
    	
    	if (descender > 0) {
    		lowerPixels = (int) Math.floor(
    			(descender + bottomSideBearing) / funitInGrid);
    	} // if
    	
    	if (width > 0) {
    		rightPixels = (int) Math.ceil(
    			width / funitInGrid);
    	} // if
    	
    	// horizontal line
    	for (i = 0; i <= upperPixels; i++) {
			double yGrid = i * funitInGrid;
			double xGrid = rightPixels * funitInGrid;
			 
			line.setLine(0, yGrid, xGrid, yGrid);
			m_graphics.draw(line);
    	} // for i
    	
    	// horizontal line
    	for (i = 0; i <= lowerPixels; i++) {
    		double yGrid = -i * funitInGrid;
    		double xGrid = rightPixels * funitInGrid;
    		
			line.setLine(0, yGrid, xGrid, yGrid);
			m_graphics.draw(line);
    	} // for i
		
		// vertical line
		for (i = 0; i <= rightPixels; i++) {
			double xGrid = i * funitInGrid;
			double bottom = -lowerPixels * funitInGrid;
			double top = upperPixels * funitInGrid;
			line.setLine(xGrid, bottom, xGrid, top);
			m_graphics.draw(line);
		} // for i
		
		// dots
    	m_graphics.setColor(Color.DARK_GRAY);
    	for (i = -lowerPixels; i < upperPixels; i++) {
    		for (j = 0; j < rightPixels; j++) {
    			int x = (int) (j * funitInGrid + funitInGrid / 2); 
				int y = (int) (i * funitInGrid + funitInGrid / 2); 
    			m_graphics.drawLine(x, y, x, y);	
    		} // for j
    	}
    	

		line.setLine(0, -descender, 0, ascender);
		m_graphics.draw(line);
		line.setLine(width, -descender, width, ascender);
		m_graphics.draw(line);
		
		// base line
		m_graphics.setColor(Color.DARK_GRAY);
		line.setLine(-em, 0, i * em, 0);
		m_graphics.draw(line);
	}
	
	public void setPpem(int a_ppem) {
		m_ppem = a_ppem;
	}
	
	// --------------------------------------------------------------
	
	public int getPpem() {
		return m_ppem;
	}
	
	// --------------------------------------------------------------
	
	public void refreshZoom() {
	    setZoom(getZoom());
	}
	
    // --------------------------------------------------------------
	
	public int getZoom() {
	    return (int) (m_displayRatio * 100);
	}
	
	// --------------------------------------------------------------
	
	public double getDisplayRatio() {
		return m_displayRatio;	
	}	
	
	/**
	 * sets the display pixels of em.
	 * @param a_value
	 */
	public void setZoom(int a_value) {
		if (a_value == 0) {
			setDisplayRatio(((double) m_ppem) / Engine.getEm());
			return;
		} // if
		
		setDisplayRatio(((double) a_value) / 100.0);
	}

	// --------------------------------------------------------------
	
	private void setDisplayRatio(double a_value) {
	    m_displayRatio = a_value;
		buildGlyphTransform(m_displayRatio);
		rescaleFontAndStroke();
	}
	
	// --------------------------------------------------------------
	
	private void buildGlyphTransform(double a_ratio) {
		TypefaceFile typeface = m_engine.getTypeface();
		double height = 1024;
		
		if (typeface != null) {
			height = typeface.getEm() - typeface.getBaseline();
		} // if
		
		m_xOrigin = 50;
		m_yOrigin = 50 + (int) (a_ratio * height);
		
		m_glyphTransform = new AffineTransform();		
		m_glyphTransform.translate(m_xOrigin, m_yOrigin);
		m_glyphTransform.scale(a_ratio, -a_ratio);
		// m_glyphTransform.translate(-Engine.getEn(), -Engine.getEn());
	}
	
	// --------------------------------------------------------------
	
	private void rescaleFontAndStroke() {
		m_numberFont = new Font("Dialog", Font.PLAIN, 
			 	(int) (12 / m_displayRatio));
			
			float ten = (float) (10.0 / m_displayRatio);
			float five = (float) (5.0 / m_displayRatio);
			
			m_dottedStroke = new BasicStroke(1,
				BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER,
				ten, new float[] {five, five}, 0.0f);
	}
	
	public Font getNumberFont() {
		return m_numberFont;
	}
}
