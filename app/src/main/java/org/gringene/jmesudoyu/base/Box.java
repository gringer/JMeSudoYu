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
 * Box class, extends Line to add boxDeath function. This is no longer necessary
 * after creating the complement functions of the Line class.
 * 
 * @author gringer
 * 
 */
public class Box extends Line {
   /**
     * An advanced logic / solver that eliminates candidates from other boxes.
     * For each number, if all points within a box with that number as a
     * candidate lie along the same row or column, then that number can be
     * eliminated from all points in that row or column outside this box.
     */
    public boolean boxDeath() {
        boolean retVal = false;
        Line pRow, pCol;
        boolean found;
        int pBit;
        getPoints(tempVals);
        for (int i = 0; i < 9; i++) {
            pRow = null;
            pCol = null;
            found = false;
            pBit = 1 << i;
            for (int j = 0; j < 9; j++) {
                if ((tempVals[j] & pBit) != 0) {
                    if (!found) {
                        pRow = points[j].getRow();
                        pCol = points[j].getColumn();
                        found = true;
                    } else {
                        if (!points[j].inLine(pRow))
                            pRow = null;
                        if (!points[j].inLine(pCol))
                            pCol = null;
                    }
                }
            }
            if (found) {
                retVal = ((pRow != null) && (pRow.remValue(i, this))) || retVal;
                retVal = ((pCol != null) && (pCol.remValue(i, this))) || retVal;
            }
        }
        return retVal;
    }
}
