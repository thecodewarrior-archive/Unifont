/*
 * $Id: GuiResource.java,v 1.43 2004/11/29 07:53:19 eed3si9n Exp $
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

import java.util.ListResourceBundle;

/**
 * @author e.e
 */
public class GuiResource extends ListResourceBundle {
	static final Object[][] contents = {
	    {"task", "Task"},
        {"deleteObject", "Delete"},
	    {"addPoint", "Add a Point"},
	    {"togglePoint", "On/Off Curve Point"},
	    {"addHint", "Add a Hint"},
	    {"createContour", "Create Contour"},
	    {"createModule", "Create Module"},
	    {"createInclude", "Create Include"},
	    {"selectNext", "Select Next"},
	    {"toggleGridfit", "On/Off Grid"},
	    {"properties", "Properties"},
	    {"convertControlPoint", "Convert"},
	    {"convertContour", "Convert"},
	    
	        
		// -----------------------------------
		// file menu
		{"file", "File"},
		{"new", "New"},
		{"newTypeface", "Typeface..."},
		{"addNewGlyph", "Glyph..."},
		{"addEmptyGlyph", "Empty Glyph..."},
		{"open", "Open"},
		{"openTypeface", "Typeface..."},
		{"openBackground", "Background Image..."},
		{"addExistingGlyph", "Glyph..."},
		{"saveGlyph", "Save %0"},
		{"exit", "Exit"},
		
		// edit menu
		{"edit", "Edit"},
		{"undo", "Undo"},
		{"redo", "Redo"},
		{"cut", "Cut"},
		{"copy", "Copy"},
		{"paste", "Paste"},
		{"moveLeft", "Move Left"},
		{"moveUp", "Move Up"},
		{"moveDown", "Move Down"},
		{"moveRight", "Move Right"},
		{"delete", "Delete"},
		
		// view menu
		{"view", "View"},
		{"showBackground", "Backgorund Image"},
		{"zoom", "Zoom"},
		{"resolution", "Resolution"},
		
		// font size menu
		{"fontSize", "Size"},
		
		// typeface menu
		{"typeface", "Typeface"},
		{"build", "Build TrueType"},
		{"release", "Release Build"},
		{"typefaceSetting", "Properties"},
		
		// help menu
		{"help", "Help"},
		
		// -----------------------------------
		{"contour", "Contour"},
		{"module", "Module"},
		{"include", "Include"},
		{"tree", "Tree"},
		{"close", "Close"},
		{"search", "Search"},
		{"example", "By Example"},
		{"jis", "JIS Code"},
		{"unicode", "unicode"},
		{"add", "Add"},
		{"remove", "Remove"},
		
		{"glyph", "Glyph"},
		{"wallpaper", "Wallpaper"},
		{"hint", "Hint"},
		
		
		{"ascender", "ascender height"},
		{"descender", "descender depth"},
		{"xHeight", "x-height"},
		{"tsb", "top-side bearing"},
		{"bsb", "bottom-side bearing"},
		{"lsb", "left-side bearing"},
		{"rsb", "right-side bearing"},
		{"baseline", "baseline"},
		{"meanline", "meanline"},
		{"bodytop", "ascender line"},
		{"bodybottom", "descender line"},
		
		{"metrics", "metrics"},
		{"fontInfo", "typeface info"},
		{"fontFamily", "font family"},
		{"copyrightInfo", "copyright info"},
		{"author", "author"},
		{"year", "year"},
		{"license", "license"},
		{"codePages", "code pages"},
		{"glyphName", "glyph name"},
		{"width", "set width"},
		
		{"bodySize", "body size"},

		{"reference", "reference"},
		{"target", "target"},
		{"fontSample", "Font Sample"},
		{"glyphTable", "Glyph Table"},
		{"typefaceFileName",		"Typeface File Name"},
		{"typefaceDir",				"Typeface Folder"},
		{"fileName",				"File Name"},
		{"msgIsRequired",			"%s is required."},
		{"msgTypefaceExistsInDir",	"A typeface already exists in the folder."},
		
		{"pickMenu", "Pick"},
		{"moveMenu", "Move"},
		{"addMenu", "Add"},
		{"removeMenu", "Remove"},
		{"includeMenu", "Include"},
		{"contourMenu", "Contour"},
		{"moduleMenu", "Module"},
		{"pointMenu", "Point"},
		{"previewMenu", "Preview"},
		{"pickpoint", "Pick Point"},
		{"movepoint", "Move Point"},
		{"removepoint", "Remove Point"},
		
		{"msgSaveChange",
			"save the unsaved changes?"},
		{"msgAlreadyExists",
					" already exists!"},
		{"msgNoTypeface",
					"there's no typeface!"},
		{"msgEmptyGlyphTitle",
					"the glyph title is empty!"},
		{"msgUnicode",
		"enter a unicode:\n"
		+ "You may enter in hex, for example \"0061\"\n" 
		+ "or the actual character, for example \"a\""
		},
		
		{"msgJis",
		"enter a JIS hex code:\n"
		+ "You may enter in hex, for example \"306c\"\n" 
		+ "or the actual character, for example \"\""
		},
		{"msgNoPunct",
		"You cannot use punctuation marks!\n"
		+ "(except minus, underscore, and period)"},
		{"msgGlyphName",
		"enter glyph's name:"},
		{"msgCreateNewGlyph",
		"file not found.\n"
		+ "do you want to create a new glyph?"},
		{"msgAuthor",
		"enter author's name:"},
		{"msgYear", "enter copyright year (for example: 2003-2004):"},
		{"msgFamily", "enter font falimy name:"},
		{"msgCircular", "adding the include creates circular inclusion!"},
		{"msgHasUnicode", "Is new glyph a letter?"}
		
		// END OF MATERIAL TO LOCALIZE
    };
      
	/**
	 * @see java.util.ListResourceBundle#getContents()
	 */
	protected Object[][] getContents() {
		return contents;
	}
}
