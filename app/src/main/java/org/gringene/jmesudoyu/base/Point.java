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
 * Class for individual cells in a sudoku game. Most functions alter the
 * bit-packed representation of the candidate numbers at a single org.gringene.jmesudoyu.base.Point.
 * 
 * @author gringer
 */
public class Point {
    public static int BLANK = 0;

    public static int SINGLE = 1;

    public static int MULTIPLE = 2;

    public static int MAX = 9; // maximum possible number of candidates

    public static byte[] bitCounts;

    private Line row, column, box;

    private int rpos, cpos, bpos;

    private int value;

    boolean locked;

    boolean error;

    /**
     * Sets all fields to their default values. All numbers are considered
     * candidates and the org.gringene.jmesudoyu.base.Point is unlocked.
     */
    public Point() {
        if (Point.bitCounts == null) {
            Point.bitCounts = new byte[512];
            /* see http://www.kerneltrap.org/node/60273 for an O(n) version */
            for (int i = 0; i < 512; i++) {
                byte tCount = 0;
                for (int tn = i; tn != 0; tn >>>= 1) {
                    tCount += (tn & 1);
                }
                Point.bitCounts[i] = tCount;
            }
        }
        value = 511;
        locked = false;
        error = false;
        row = null;
        column = null;
        box = null;
    }

    /**
     * Determines if the bit-packed representation of the point is the same as
     * another given point.
     * 
     * @param p
     *            org.gringene.jmesudoyu.base.Point to compare against
     * @return true if the bit-packed representations are equal
     */
    public boolean equals(Point p) {
        return (this.value == p.value);
    }

    /**
     * Determines if the bit-packed representation of the point is the same as
     * another given bit-packed representation.
     * 
     * @param tBits
     *            bit-packed number to compare against
     * @return true if the bit-packed representations are equal
     */
    public boolean equals(int tBits) {
        return (this.value == tBits);
    }

    /**
     * Counts the total number of candidates in this org.gringene.jmesudoyu.base.Point. This is one of the
     * most used function in the logic calculations, so should be as fast as
     * possible.
     * 
     * @return an integer that indicates how many candidate numbers there are in
     *         this org.gringene.jmesudoyu.base.Point
     */
    public int countBits() {
        if (value < 512) {
            return Point.bitCounts[value];
        } else {
            // old version
            int tCount = 0;
            for (int tn = value; tn != 0; tn >>>= 1) {
                tCount += (tn & 1);
            }
            return tCount;
        }
    }

    /**
     * Counts the total number of candidates in a point described by an integer.
     * This is a static equivalent to countBits(), for classes that have a need
     * to implement a similar function.
     * 
     * @param pVal
     *            integer that describes candidate numbers at a point
     * @return an integer that indicates how many candidates there are (set
     *         bits) in the given point
     */
    public static int countBits(int pVal) {
        if (pVal < 512) {
            return Point.bitCounts[pVal];
        } else {
            // old version
            int tCount = 0;
            for (int tn = pVal; tn != 0; tn >>>= 1) {
                tCount += (tn & 1);
            }
            return tCount;
        }
    }

    /**
     * Determines the numerical value of the highest set bit within this org.gringene.jmesudoyu.base.Point.
     * The most used case for this is when there is only one candidate number,
     * in which case this will return the numerical value of that number.
     * 
     * @return an integer that describes the numerical / unpacked value of the
     *         highest set bit
     */
    private int highBit() {
        int pCount = 0;
        for (int tn = value; tn != 0; tn >>>= 1, pCount++)
            ;
        return pCount - 1;
    }

    /**
     * Generates a descriptive signature for this org.gringene.jmesudoyu.base.Point that indicates how many
     * candidate values that there are within the point. Values are as follows:
     * 
     * <ul>
     * <li>org.gringene.jmesudoyu.base.Point.BLANK &mdash; no candidate values</li>
     * <li>org.gringene.jmesudoyu.base.Point.SINGLE &mdash; a single candidate number at this org.gringene.jmesudoyu.base.Point</li>
     * <li>org.gringene.jmesudoyu.base.Point.MULTIPLE &mdash; more than one candidate at this org.gringene.jmesudoyu.base.Point</li>
     * </ul>
     * 
     * @return the descriptive signature for this org.gringene.jmesudoyu.base.Point
     */
    public int signature() {
        return Math.min(this.countBits(),Point.MULTIPLE);
    }

    /**
     * Generates a descriptive signature for a point described by an integer.
     * This is a static equivalent to signature(), for classes that have a need
     * to implement a similar function. Values are as follows:
     * 
     * <ul>
     * <li>org.gringene.jmesudoyu.base.Point.BLANK &mdash; no candidate values at this org.gringene.jmesudoyu.base.Point</li>
     * <li>org.gringene.jmesudoyu.base.Point.SINGLE &mdash; a single candidate number</li>
     * <li>org.gringene.jmesudoyu.base.Point.MULTIPLE &mdash; more than one candidate number</li>
     * </ul>
     * 
     * @param pVal
     *            integer that describes candidate numbers at a point
     * @return the descriptive signature for this org.gringene.jmesudoyu.base.Point
     */
    public static int signature(int pVal) {
        return Math.min(Point.countBits(pVal),Point.MULTIPLE);
    }

    /**
     * Returns a boolean value to indicate whether there is a single candidate
     * number at this point.
     * 
     * @return True if there is only one candidate number at this org.gringene.jmesudoyu.base.Point
     */
    public boolean isSingle() {
        return (countBits() == 1);
    }

    /**
     * Retrieves the row that this org.gringene.jmesudoyu.base.Point is on.
     * 
     * @return A org.gringene.jmesudoyu.base.Line that points to the row coincident with this org.gringene.jmesudoyu.base.Point.
     */
    public Line getRow() {
        return row;
    }

    /**
     * Sets this org.gringene.jmesudoyu.base.Point at a specific position in a given row.
     * 
     * @param tRow
     *            row that the org.gringene.jmesudoyu.base.Point should be placed in
     * @param tpos
     *            position within row that the org.gringene.jmesudoyu.base.Point should be placed at
     */
    public void setRow(Line tRow, int tpos) {
        row = tRow;
        rpos = tpos;
        row.setPoint(rpos, this);
    }

    /**
     * Retrieves the column that this org.gringene.jmesudoyu.base.Point is on.
     * 
     * @return A org.gringene.jmesudoyu.base.Line that points to the column coincident with this org.gringene.jmesudoyu.base.Point.
     */
    public Line getColumn() {
        return column;
    }

    /**
     * Sets this org.gringene.jmesudoyu.base.Point at a specific position in a given column.
     * 
     * @param tCol
     *            column that the org.gringene.jmesudoyu.base.Point should be placed in
     * @param tpos
     *            position within column that the org.gringene.jmesudoyu.base.Point should be placed at
     */
    public void setColumn(Line tCol, int tpos) {
        column = tCol;
        cpos = tpos;
        column.setPoint(cpos, this);
    }

    /**
     * Retrieves the box that this org.gringene.jmesudoyu.base.Point is contained within.
     * 
     * @return A Box that points to the box coincident with this org.gringene.jmesudoyu.base.Point.
     */
    public Line getBox() {
        return box;
    }

    /**
     * Sets this org.gringene.jmesudoyu.base.Point at a specific position in a given box. Boxes are defined
     * with position 0 being on the same row as position 2, and on the same
     * column as position 6 (assuming zero-based positions).
     * 
     * @param tBox
     *            Box that the org.gringene.jmesudoyu.base.Point should be placed in
     * @param tpos
     *            position within box that the org.gringene.jmesudoyu.base.Point should be placed at
     */
    public void setBox(Line tBox, int tpos) {
        box = tBox;
        bpos = tpos;
        box.setPoint(bpos, this);
    }

    /**
     * Determines whether or not this org.gringene.jmesudoyu.base.Point is coincident with a specific row,
     * column, or box.
     * 
     * @param tLine
     *            org.gringene.jmesudoyu.base.Line to check for membership
     * @return True if the org.gringene.jmesudoyu.base.Point can be found on the given org.gringene.jmesudoyu.base.Line
     */
    public boolean inLine(Line tLine) {
        return ((tLine == row) || (tLine == column) || (tLine == box));
    }

    /**
     * Retrieves the current integer representation of this org.gringene.jmesudoyu.base.Point
     * 
     * @return a value representing the bit-packed candidate numbers at this
     *         org.gringene.jmesudoyu.base.Point
     */
    public int getValue() {
        return value;
    }

    /**
     * Retrieves the current locked state of this org.gringene.jmesudoyu.base.Point. Other classes would be
     * expected to use this method to work out if a org.gringene.jmesudoyu.base.Point should be modified.
     * 
     * @return True if the org.gringene.jmesudoyu.base.Point is flagged as locked against modification
     */
    public boolean getLocked() {
        return locked;
    }

    /**
     * Retrieves the current error state of this org.gringene.jmesudoyu.base.Point (whether it contradicts
     * some other point).
     * 
     * @return True if the org.gringene.jmesudoyu.base.Point is flagged as an error
     */
    public boolean getError() {
        return error;
    }

    /**
     * Sets the lock state of this org.gringene.jmesudoyu.base.Point. If a org.gringene.jmesudoyu.base.Point is locked, then no
     * modifications to the point should be allowed.
     * 
     * @param tLocked
     *            True if the org.gringene.jmesudoyu.base.Point should be locked
     */
    public void setLocked(boolean tLocked) {
        locked = tLocked;
    }

    /**
     * Sets the error state of this org.gringene.jmesudoyu.base.Point.
     * 
     * @param tError
     *            True if the org.gringene.jmesudoyu.base.Point should be flagged as an error
     */
    public void setError(boolean tError) {
        error = tError;
    }

    /**
     * Clears the logic state of this org.gringene.jmesudoyu.base.Point. This typically involves resetting
     * the candidate numbers (setting all numbers as potential candidates), and
     * clearing flags for this org.gringene.jmesudoyu.base.Point.
     */
    public void clearValue() {
        value = 511;
        locked = false;
        error = false;
        this.updateChanged();
    }

    /**
     * Logic method that removes numbers from the org.gringene.jmesudoyu.base.Line, Column, and Box that
     * this org.gringene.jmesudoyu.base.Point is a part of. If there is only one possible option for a
     * number at this point, then remove that number from the candidates in the
     * row, column and box which contain this point.
     */
    public boolean clearOthers() {
        boolean retVal = false;
        if (countBits() == 1) {
            int tHighBit = highBit();
            if(row.getChanged())
                retVal = row.remValue(tHighBit, rpos) || retVal;
            if(column.getChanged())
                retVal = column.remValue(tHighBit, cpos) || retVal;
            if(box.getChanged())
                retVal = box.remValue(tHighBit, bpos) || retVal;
        }
        return retVal;
    }

    /**
     * Determines if there is a simple logic error at this point, cascading to
     * other points.
     * 
     * @return true if there is a simple logic error at this point.
     */
    public boolean flagErrors() {
        boolean oldError = this.error;
        if (countBits() == 1) {
            this.error = ((row.flagErrors(highBit(), rpos))
                    || (column.flagErrors(highBit(), cpos)) || (box.flagErrors(
                    highBit(), bpos)));
        } else if (this.error) {
            // could potentially release a org.gringene.jmesudoyu.base.Point from error status prematurely
            // should this cascade?
            this.error = false;
            row.flagErrors(Point.MAX, rpos); // , cascade = true)
            column.flagErrors(Point.MAX, cpos);
            box.flagErrors(Point.MAX, bpos);
        } else {
            this.error = false;
        }
        return (oldError != this.error);
    }

    /**
     * Places packed versions of bits into an integer array, returning the
     * number of candidates in the current org.gringene.jmesudoyu.base.Point.
     * 
     * @param result
     *            integer array to store bits in
     * @return an integer indicating the number of candidates at this point
     */
    public int getBits(int[] result) {
        if (result.length < 9) {
            System.out.println("Result array is the wrong size");
            return 0;
        }
        int bCount = 0;
        for (int tVal = 256; tVal != 0; tVal >>>= 1)
            if ((value & tVal) != 0)
                result[bCount++] = tVal;
        return bCount;
    }

    /**
     * Copies the representation of another org.gringene.jmesudoyu.base.Point to this org.gringene.jmesudoyu.base.Point, transferring
     * all candidate values and status flags.
     * 
     * @param tPoint
     *            org.gringene.jmesudoyu.base.Point containing values to copy onto this org.gringene.jmesudoyu.base.Point
     */
    public void setBits(Point tPoint) {
        this.value = tPoint.value;
        this.locked = tPoint.locked;
        this.updateChanged();
    }

    /**
     * Sets the numerical representation of this point to match a given integer
     * exactly. This integer should be a bit-packed representation of candidate
     * numbers.
     * 
     * @param tVal
     *            bit-packed set of candidate numbers that the org.gringene.jmesudoyu.base.Point should
     *            represent
     */
    public void setExact(int tVal) {
        value = tVal;
        this.updateChanged();
    }

    /**
     * Sets the numerical representation of this point to match a single number.
     * This number should be only one of the possible candidate numbers
     * (unpacked).
     * 
     * @param tVal
     *            numerical representation of the number that this org.gringene.jmesudoyu.base.Point should
     *            represent
     */
    public void setValue(int tVal) {
        value = 1 << tVal;
        this.updateChanged();
    }

    /**
     * Set the candidate numbers of this org.gringene.jmesudoyu.base.Point to the intersection of the
     * current candidates, and those described by a given integer.
     * 
     * @param tVal
     *            bit-packed maximal set of candidate numbers that should be set
     *            in this org.gringene.jmesudoyu.base.Point
     */
    public void setBits(int tVal) {
        value = value & tVal;
        this.updateChanged();
    }

    /**
     * Removes a specific number from the candidate numbers for this org.gringene.jmesudoyu.base.Point.
     * 
     * @param tVal
     *            number to remove from candidates
     */
    public boolean remValue(int tVal) {
        boolean retVal = ((value & (1 << tVal)) != 0);
        if (retVal) {
            value = value & (511 - (1 << tVal));
            this.updateChanged();
        }
        return retVal;
    }

    /**
     * Flips the candidacy of a specific number within the candidate numbers for
     * this org.gringene.jmesudoyu.base.Point.
     * 
     * @param tVal
     *            number to flip candidate status for
     */
    public void flipValue(int tVal) {
        value = value ^ (1 << tVal);
        this.updateChanged();
    }

    /**
     * Removes a bit-packed set of numbers from candidate numbers for this
     * org.gringene.jmesudoyu.base.Point.
     * 
     * @param tVal
     *            numbers (as bit packed integer) to remove from candidates
     */
    public boolean remBits(int tVal) {
        boolean retVal = ((value & tVal) != 0);
        if (retVal) {
            value = value & (511 - tVal);
            this.updateChanged();
        }
        return retVal;
    }

    /**
     * Converts the point into a character that would be appropriate for output
     * to the screen. If there is only one candidate, then this returns that
     * number. If there is a contradiction (no candidates), then this returns a
     * symbol appropriate for expressing a contradiction. If there are multiple
     * candidates, then this returns a blank character.
     * 
     * @return A screen formatted character representation of thr current org.gringene.jmesudoyu.base.Point
     */
    public char toChar() {
        if (countBits() == 1)
            return (("" + (highBit() + 1)).charAt(0));
        else if (countBits() == 0)
            return ('X');
        else
            return ' ';
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (countBits() == 1)
            return ("" + (highBit() + 1));
        else if (countBits() == 0)
            return ("X");
        else
            return "";
    }

    /**
     * Converts the org.gringene.jmesudoyu.base.Point into a string that would be appropriate for output to
     * a file. If there is only one candidate, then this returns that number.
     * Otherwise, it outputs the representation of a "blank" value.
     * 
     * @return A file-formatted String representation of the current org.gringene.jmesudoyu.base.Point
     */
    public String toFileString() {
        if (countBits() == 1)
            return ("" + (highBit() + 1));
        else
            return "X";
    }

    /**
     * Converts the org.gringene.jmesudoyu.base.Point candidates into a string that would be appropriate for
     * output to a file. Candidates are listed in numerical order and are
     * surrounded by square brackets if locked. All numbers as candidates result
     * in an empty String output, while no candidates are represented by 'X'.
     * 
     * @return A file-formatted String representation of the current org.gringene.jmesudoyu.base.Point
     */
    public String toFileBitsString() {
        String outString = "";
        if (value != 511) {
            for (int i = 0; i < 9; i++) {
                if ((value & (1 << i)) != 0) {
                    outString = outString + Integer.toString(i + 1);
                }
            }
        }
        if (value == 0) {
            outString = "X";
        }
        if (this.locked) {
            outString = "[" + outString + "]";
        }
        return outString;
    }

    /**
     * Converts an input string into a org.gringene.jmesudoyu.base.Point, with candidates. This is the
     * complementary method to toFileBitsString().
     * 
     * @param tString
     *            Input string, formatted as in toFileBitsString()
     */
    public void setBits(String tString) {
        this.clearValue();
        if ((tString.charAt(0) == '[')
                && (tString.charAt(tString.length() - 1) == ']')) {
            this.locked = true;
            tString = tString.substring(1, tString.length() - 1);
        }
        if (!tString.equals("")) {
            this.value = 0;
            for (int i = 0; i < tString.length(); i++) {
                int candidate = Character.digit(tString.charAt(i), 10)
                        - Character.digit('0', 10) - 1;
                if ((candidate >= 0) && (candidate < 9)) {
                    this.flipValue(candidate);
                }
            }
            this.updateChanged();
        }
    }

    /**
     * Updates the changed status of the row/column/box associated with this
     * point.
     */
    private void updateChanged() {
        if (row != null) {
            row.setChanged(true);
            column.setChanged(true);
            box.setChanged(true);
        }
    }
}
