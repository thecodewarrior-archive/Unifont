/**
 * Fontastic
 * A font file writer to create TTF and WOFF (Webfonts).
 * http://code.andreaskoller.com/libraries/fontastic
 *
 * Copyright (C) 2013 Andreas Koller http://andreaskoller.com
 *
 * Uses:
 * doubletype http://sourceforge.net/projects/doubletype/ for TTF creation
 * sfntly http://code.google.com/p/sfntly/ for WOFF creation
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author      Andreas Koller http://andreaskoller.com
 * @modified    06/19/2013
 * @version     0.4 (4)
 */

package fontastic

import fontastic.FGlyph
import fontastic.FContour
import fontastic.FPoint

import org.doubletype.ossa.*
import org.doubletype.ossa.module.*
import org.doubletype.ossa.truetype.*
import org.doubletype.ossa.adapter.*

import com.google.typography.font.sfntly.Font
import com.google.typography.font.sfntly.FontFactory
import com.google.typography.font.sfntly.data.WritableFontData
import com.google.typography.font.tools.conversion.woff.WoffWriter

import java.io.*
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * Fontastic A font file writer to create TTF and WOFF (Webfonts). http://code.andreaskoller.com/libraries/fontastic
 *
 */
class Fontastic
/**
 * Constructor
 *
 * @example Fontastic f = new Fontastic(this, "MyFont");
 *
 * @param fontname
 * Font name
 */
(
        private val applet: PApplet,
        /**
         * Returns the font name.
         *
         * @return String
         */
        val fontname: String) {



    private val typeface: org.doubletype.ossa.module.TypefaceFile? = null
    /**
     * Engine getter
     *
     * @return The doubletype Engine used for font creation, so that you can
     * access all functions of doubletype in case you need them.
     */
    var engine: Engine? = null
        private set

    /**
     * Returns the .ttf file name
     *
     * @return The .ttf file name, which is being created when you call build()
     */
    var ttFfilename: String? = null
        private set
    /**
     * Returns the .woff file name
     *
     * @return The .woff file name, which is being created when you call build()
     */
    var wofFfilename: String? = null
        private set
    private var HTMLfilename: String? = null

    private val glyphs: MutableList<FGlyph>

    private var advanceWidth = 512
    private var debug = true // debug toggles println calls

    /**
     * Returns the TypefaceFile
     *
     * @return The doubletype TypefaceFile used for font creation, so that you
     * can access functions of doubletype in case you need them.
     */
    val typefaceFile: TypefaceFile
        get() = engine!!.typeface

    init {
        intitialiseFont()
        this.glyphs = ArrayList()
    }

    /**
     * Creates and initialises a new typeface. Font data is put into sketch
     * folder data/fontname.
     */
    private fun intitialiseFont() {
        val data_dir = File(myParent.dataPath(""))
        if (!data_dir.exists()) {
            data_dir.mkdir()
        }
        val a_dir = File(myParent.dataPath(fontName))
        if (!a_dir.exists()) {
            a_dir.mkdir()
        } else {
            deleteFolderContents(a_dir, false)
        }

        engine = Engine.getSingletonInstance()
        engine!!.buildNewTypeface(fontname, a_dir)

        this.setFontFamilyName(fontname)
        this.setVersion("CC BY-SA 3.0 http://creativecommons.org/licenses/by-sa/3.0/") // default
        // license

        val directoryName = a_dir.toString() + File.separator + "bin" + File.separator

        ttFfilename = "$directoryName$fontname.ttf"
        wofFfilename = "$directoryName$fontname.woff"
        HTMLfilename = directoryName + "template.html"
    }

    /**
     * Builds the font and writes the .ttf and the .woff file as well as a HTML template for previewing the WOFF.
     * If debug is set (default is true) then you'll see the .ttf and .woff file name in the console.
     */
    fun buildFont() {

        // Create TTF file with doubletype

        engine!!.addDefaultGlyphs()

        for (glyph in glyphs) {

            val glyphFile = engine!!.addNewGlyph(glyph.glyphChar.toLong())
            glyphFile.advanceWidth = glyph.advanceWidth

            for (contour in glyph.contours) {

                val econtour = EContour()
                econtour.type = EContour.k_cubic

                for (point in contour.points) {

                    val e = EContourPoint(point.x, point.y, true)

                    if (point.hasControlPoint1()) {
                        val cp1 = EControlPoint(true,
                                point.controlPoint1.x, point.controlPoint1.y)
                        e.controlPoint1 = cp1
                    }

                    if (point.hasControlPoint2()) {
                        val cp2 = EControlPoint(false,
                                point.controlPoint2.x, point.controlPoint2.y)
                        e.controlPoint2 = cp2
                    }

                    econtour.addContourPoint(e)
                }

                glyphFile.addContour(econtour)
            }
            glyphFile.saveGlyphFile()
        }

        engine!!.buildTrueType(false)
        if (debug)
            println("TTF file created successfully: " + ttFfilename!!)

        // End TTF creation

        // Create a WOFF file from this TTF file using sfntly

        val fontFactory = FontFactory.getInstance()

        val fontFile = File(ttFfilename!!)
        val outputFile = File(wofFfilename!!)

        var fontBytes = ByteArray(0)
        try {
            val fis = FileInputStream(fontFile)
            fontBytes = ByteArray(fontFile.length().toInt())
            fis.read(fontBytes)
        } catch (e: IOException) {
            println("Error while creating WOFF File. TTF file not found: " + ttFfilename!!)
            e.printStackTrace()
        }

        var fontArray: Array<Font>? = null
        try {
            fontArray = fontFactory.loadFonts(fontBytes)
        } catch (e: IOException) {
            println("Error while creating WOFF File. TTF file could not be read: " + ttFfilename!!)
            e.printStackTrace()
        }

        val font = fontArray!![0]

        try {
            val fos = FileOutputStream(outputFile)
            val w = WoffWriter()
            val woffData = w.convert(font)
            woffData.copyTo(fos)
            if (debug)
                println("WOFF File created successfully: " + wofFfilename!!)
        } catch (e: IOException) {
            println("Error while creating WOFF File. WOFF file could not be written.$outputFile")
            e.printStackTrace()
        }

        // End of WOFF creation

        // Create HTML Template for WOFF file
        val htmlTemplate = myParent.join(myParent.loadStrings("template.html"), "\n")
        val params = HashMap<String, String>()
        params["FONTNAME"] = fontname
        params["WOFFFILENAME"] = "$fontname.woff"
        val htmlContent = replaceAll(htmlTemplate, params)

        myParent.saveStrings(HTMLfilename, myParent.split(htmlContent, "\n"))
        // End HTML Template
    }

    /**
     * Deletes all the glyph files created by doubletype in your data/fontname
     * folder.
     */
    fun cleanup() {

        val a_dir = File(myParent.dataPath(fontname))
        val filesToExclude = arrayOfNulls<File>(3)
        filesToExclude[0] = File(ttFfilename!!)
        filesToExclude[1] = File(wofFfilename!!)
        filesToExclude[2] = File(HTMLfilename!!)

        deleteFolderContents(a_dir, true, filesToExclude)
        if (debug)
            println("Cleaned up and deleted all glyph files, except font files.")

    }

    /**
     * Sets the author of the font.
     */
    fun setAuthor(author: String) {
        engine!!.setAuthor(author)
    }

    /**
     * Sets the copyright year of the font.
     */
    fun setCopyrightYear(copyrightYear: String) {
        engine!!.setCopyrightYear(copyrightYear)
    }

    /**
     * Sets the version of the font (default is "0.1").
     */
    fun setVersion(version: String) {
        engine!!.typeface.glyph.head.version = version
    }

    /**
     * Sets the font family name of the font. Also called in the constructor. If
     * changed with setFontFamilyName() it won't affect folder the font is
     * stored in.
     */
    fun setFontFamilyName(fontFamilyName: String) {
        engine!!.setFontFamilyName(fontFamilyName)
    }

    /**
     * Sets the sub family of the font.
     */
    fun setSubFamily(subFamily: String) {
        engine!!.typeface.setSubFamily(subFamily)
    }

    /**
     * Sets the license of the font (default is
     * "CC BY-SA 3.0 http://creativecommons.org/licenses/by-sa/3.0/")
     */
    fun setTypefaceLicense(typefaceLicense: String) {
        engine!!.setTypefaceLicense(typefaceLicense)
    }

    /**
     * Sets the baseline of the font.
     */
    fun setBaseline(baseline: Float) {
        engine!!.setBaseline(baseline.toDouble())
    }

    /**
     * Sets the meanline of the font.
     */
    fun setMeanline(meanline: Float) {
        engine!!.setMeanline(meanline.toDouble())
    }

    /**
     * Sets the advanceWidth of the font. Can be changed for every glyph
     * individually. Won't affect already created glyphs.
     */
    fun setAdvanceWidth(advanceWidth: Int) {
        engine!!.setAdvanceWidth(advanceWidth)
        this.advanceWidth = advanceWidth
    }

    fun setTopSideBearing(topSideBearing: Float) {
        try {
            engine!!.typeface.topSideBearing = topSideBearing.toDouble()
        } catch (e: OutOfRangeException) {
            println("Error while setting aopSideBearing (must be within range " + engine!!.typeface.em)
            e.printStackTrace()
        }

    }

    fun setBottomSideBearing(bottomSideBearing: Float) {
        try {
            engine!!.typeface.bottomSideBearing = bottomSideBearing.toDouble()
        } catch (e: OutOfRangeException) {
            println("Error while setting bottomSideBearing (must be within range " + engine!!.typeface.em)
            e.printStackTrace()
        }

    }

    fun setAscender(ascender: Float) {
        try {
            engine!!.typeface.ascender = ascender.toDouble()
        } catch (e: OutOfRangeException) {
            println("Error while setting ascender (must be within range 0 to "
                    + engine!!.typeface.em + ")")
            e.printStackTrace()
        }

    }

    fun setDescender(descender: Float) {
        try {
            engine!!.typeface.descender = descender.toDouble()
        } catch (e: OutOfRangeException) {
            println("Error while setting descender (must be within range 0 to "
                    + engine!!.typeface.em + ")")
            e.printStackTrace()
        }

    }

    fun setXHeight(xHeight: Float) {
        try {
            engine!!.typeface.xHeight = xHeight.toDouble()
        } catch (e: OutOfRangeException) {
            println("Error while setting xHeight (must be within range 0 to "
                    + engine!!.typeface.em
                    + " as well as lower than the ascender "
                    + engine!!.typeface.ascender + ")")
            e.printStackTrace()
        }

    }

    /**
     * Sets the default metrics for the typeface: setTopSideBearing(170); // 2
     * px setAscender(683); // 8 px setXHeight(424); // 5 px setDescender(171);
     * // 2 px setBottomSideBearing(0); // 0px
     *
     */
    fun setDefaultMetrics() {
        engine!!.typeface.setDefaultMetrics()
    }

    /**
     * Sets the value of debug
     *
     * @param debug
     * true or false
     */
    @JvmOverloads
    fun setDebug(debug: Boolean = true) {
        this.debug = debug
    }

    /**
     * Add a glyph
     *
     * @param c
     * Character of the glyph.
     *
     * @return FGlyph that has been created.
     */
    fun addGlyph(c: Char): FGlyph {

        val glyph = FGlyph(c)
        glyph.advanceWidth = advanceWidth
        glyphs.add(glyph)
        if (debug)
            println("Glyph " + c + " added. Number of glyphs: "
                    + glyphs.size)
        return glyph

    }

    /**
     * Add a glyph and its one contour
     *
     * @param c
     * Character of the glyph.
     *
     * @param FContour
     * Shape of the glyph as FContour.
     *
     * @return The glyph FGlyph that has been created. You can use this to store
     * the glyph and add contours afterwards. Alternatively, you can
     * call getGlyph(char c) to retrieve it.
     */
    fun addGlyph(c: Char, contour: FContour): FGlyph {

        val glyph = FGlyph(c)
        glyphs.add(glyph)
        if (debug)
            println("Glyph " + c + " added. Number of glyphs: "
                    + glyphs.size)

        glyph.addContour(contour)

        glyph.advanceWidth = advanceWidth
        return glyph

    }

    /**
     * Add a glyph and its contours
     *
     * @param c
     * Character of the glyph.
     *
     * @param FContour
     * [] Shape of the glyph in an array of FContour.
     *
     * @return The FGlyph that has been created. You can use this to store the
     * glyph and add contours afterwards. Alternatively, you can call
     * getGlyph(char c) to retrieve it.
     */
    fun addGlyph(c: Char, contours: Array<FContour>): FGlyph {

        val glyph = FGlyph(c)
        glyphs.add(glyph)
        if (debug)
            println("Glyph " + c + " added. Number of glyphs: "
                    + glyphs.size)

        for (contour in contours) {
            // if (debug) System.out.println(p.x + " - " + p.y);
            glyph.addContour(contour)
        }
        glyph.advanceWidth = advanceWidth
        return glyph

    }

    /**
     * Get glyph by character
     *
     * @param c
     * The character of the glyph
     *
     * @return The glyph
     */

    fun getGlyph(c: Char): FGlyph? {

        var glyph: FGlyph? = null
        for (i in glyphs.indices) {
            if (glyphs[i].glyphChar == c) {
                glyph = glyphs[i]
                break
            }
        }
        return glyph

    }

    private fun sketchName(): String {
        var s = myParent.sketchPath("")
        s = s.substring(0, s.length - 1)
        val sketchName = s.substring(s.lastIndexOf(File.separator) + 1,
                s.length)
        return sketchName
    }

    companion object {

        val VERSION = "0.4"

        /** Uppercase alphabet 26 characters  */
        val alphabet = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
        /** Lowercase alphabet 26 characters  */
        val alphabetLc = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

        /**
         * Return the version of the library.
         *
         * @return String
         */
        fun version(): String {
            return VERSION
        }

        private fun deleteFolderContents(folder: File,
                                         deleteFolderItself: Boolean) {
            val files = folder.listFiles()
            if (files != null) { // some JVMs return null for empty dirs
                for (f in files) {
                    if (f.isDirectory) {
                        deleteFolderContents(f, true)
                        f.delete()
                    } else {
                        f.delete()
                    }
                }
            }
            if (deleteFolderItself)
                folder.delete()
        }

        private fun deleteFolderContents(folder: File,
                                         deleteFolderItself: Boolean, exceptions: Array<File>) {
            val files = folder.listFiles()
            if (files != null) { // some JVMs return null for empty dirs
                for (f in files) {
                    var deleteFile = true
                    for (exceptfile in exceptions) {
                        if (f == exceptfile)
                            deleteFile = false
                    }
                    if (deleteFile) {
                        if (f.isDirectory) {
                            deleteFolderContents(f, true, exceptions)
                            f.delete()
                        } else {
                            f.delete()
                        }
                    }
                }
            }
            if (deleteFolderItself)
                folder.delete()
        }

        // http://stackoverflow.com/questions/2368802/how-to-create-dynamic-template-string
        // Author: cletus http://stackoverflow.com/users/18393/cletus
        private fun replaceAll(text: String, params: Map<String, String>,
                               leading: Char = '%', trailing: Char = '%'): String {
            var pattern = ""
            if (leading.toInt() != 0) {
                pattern += leading
            }
            pattern += "(\\w+)"
            if (trailing.toInt() != 0) {
                pattern += trailing
            }
            val p = Pattern.compile(pattern)
            val m = p.matcher(text)
            var result = m.find()
            if (result) {
                val sb = StringBuffer()
                do {
                    var replacement: String? = params[m.group(1)]
                    if (replacement == null) {
                        replacement = m.group()
                    }
                    m.appendReplacement(sb, replacement!!)
                    result = m.find()
                } while (result)
                m.appendTail(sb)
                return sb.toString()
            }
            return text
        }
    }

}
/**
 * Sets debug to true (in debug mode, the library outputs what happens under
 * the hood).
 */// http://stackoverflow.com/questions/2368802/how-to-create-dynamic-template-string
// Author: cletus http://stackoverflow.com/users/18393/cletus
