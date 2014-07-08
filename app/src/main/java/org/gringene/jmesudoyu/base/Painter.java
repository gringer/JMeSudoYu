/*
 * Copyright 2005-2014 David Hall (gringer)
 *
 *  This file is part of JMeSudoYu, a Java sudoku Solver/Puzzle generator
 *  with an emphasis on easily portable code for java-capable cellphones
 *  (originally j2me, hence the name).
 *
 *  JMeSudoYu is free software: you can redistribute it and/or modify
 *  it under the terms of the ISC license.
 *
 *  JMeSudoYu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  ISC License for more details.
 *
 *  You should have received a copy of the ISC License
 *  along with JMeSudoYu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gringene.jmesudoyu.base;

/**
 * Manages operations involved with the visual representation of the game board.
 * Instances of this class will be platform-specific, so this provides a more
 * generic interface to those instances.
 * 
 * @author gringer
 * 
 */
public interface Painter {
   /**
    * Calculates font metrics, and returns the height of the drawing font
    * @return The approximate height of a single character
    */
   public abstract int getFontHeight();
   /**
    * Converts a horizontal position (such as that of a mouse pointer) into a 
    * horizontal cell reference / column number
    * @param tx horizontal position
    * @return horizontal cell reference
    */
   public abstract int pos2cellX(int tx);
   /**
    * Converts a vertical position (such as that of a mouse pointer) into a
    * vertical cell reference / row number
    * @param ty vertical position
    * @return vertical cell reference
    */
   public abstract int pos2cellY(int ty);
   /**
    * Sets the size / metrics of the game board
    * 
    * @param tsw
    *           Width of a "square" on the game board
    * @param tsh
    *           Height of a "square" on the game board
    * @param tpx
    *           Horizontal offset for drawing text within squares
    * @param tpy
    *           Vertical offset for drawing text within squares
    */
   public abstract void setSize(
      int tsw,
      int tsh,
      int tpx,
      int tpy);
   /**
    * Start updating gauges and global variables. This function would be
    * typically used at the start of puzzle generation.
    */
   public abstract void startUpdate();
   /**
    * Stop updating gauges and global variables. This function would be
    * typically used at the end of puzzle generation.
    */
   public abstract void stopUpdate();
   /**
    * Draw a number on the information pane, usually corresponding to the
    * currently selected number.
    * 
    * @param tVal
    *           Number to draw on the information pane
    */
   public abstract void drawChoice(int tVal);
   /**
    * Redraw the numbers on the entire game board board.
    * 
    * @param doCands
    *           True if number candidates ("pencil marks") should be drawn,
    *           either instead of the numbers alone, or as well as the numbers.
    */
   public abstract void drawBoard(boolean doCands);
   /**
    * Draw the board pointer at the specified location. The board pointer
    * indicates the current position of the pointer, particularly useful for
    * completely keyboard-driven interfaces.
    * 
    * @param tx
    *           Horizontal location / square to draw pointer at
    * @param ty
    *           Vertical location / square to draw pointer at
    */
   public abstract void drawSquare(int tx, int ty);
   /**
    * Clear pointer information, and redraw the number / candidates at the
    * specified location
    * 
    * @param tx
    *           Horizontal location / square to clear
    * @param ty
    *           Vertical location / square to clear
    * @param doCands
    *           True if number candidates ("pencil marks") should be drawn,
    *           either instead of the numbers alone, or as well as the numbers.
    */
   public abstract void clearPos(int tx, int ty, boolean doCands);
   /**
    * Draws pointer information and the number / candidates at the specified
    * location, as well as possibly overlaying the currently set number.
    * 
    * @param pVal
    *           Current number
    * @param tx
    *           Horizontal location / square to draw
    * @param ty
    *           Vertical location / square to draw
    * @param doNum
    *           True if the set number (pVal) should be drawn as well
    * @param doCands
    *           True if number candidates ("pencil marks") should be drawn,
    *           either instead of the numbers alone, or as well as the numbers.
    */
   public abstract void drawPos(int pVal, int tx, int ty, boolean doNum, boolean doCands);
   /**
    * draw the number / candidates at the specified location
    * 
    * @param tx
    *           Horizontal location / square to clear
    * @param ty
    *           Vertical location / square to clear
    * @param doCands
    *           True if number candidates ("pencil marks") should be drawn,
    *           either instead of the numbers alone, or as well as the numbers.
    */
   public abstract void drawPos(int tx, int ty, boolean doCands);
   /**
    * Draws an indicator for the specified box. This function would be typically
    * used in expert mode after a box has been selected. Boxes are numbered 0-8
    * left to right, then top to bottom.
    * 
    * @param boxNum
    *           box to draw an indicator for
    */
   public abstract void drawBox(int boxNum);
   /**
    * Removes an indicator for the specified box. This function would be
    * typically used in expert mode after the selection of a box no longer makes
    * sense (for example, when the selection changes from a box to a square
    * inside that box). Boxes are numbered 0-8 left to right, then top to
    * bottom.
    * 
    * @param boxNum
    *           box to remove indicator from
    */
   public abstract void clearBox(int boxNum);
   /**
    * Returns the current draw state of the number 'buttons' (whether they 
    * are vertically drawn or not [horizontally]). These buttons appear at the
    * right hand side (or bottom), and can be clicked on to select numbers.
    * @return The current vertical state of the number 'buttons'
    */
   public abstract boolean getVertical();
   /**
    * Sets the draw state of the number 'buttons'.
    * @param tVert vertical state that the number buttons should be set to.
    * @see base.Painter#getVertical()
    */
   public abstract void setVertical(boolean tVert);
}