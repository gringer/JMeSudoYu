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
 * @author gringer
 */
public class Line {
   Point points[];
   Board board;
   int[] tempVals = new int[9];
   boolean changed;
   public Line() {
      points = new Point[9];
      this.setChanged(true);
   }
   /**
     * Determines if something in the line has changed since the change flag was
     * last reset.
     * 
     * @return true if something in this org.gringene.jmesudoyu.base.Line has changed.
     */
    public boolean getChanged() {
        return this.changed;
    }

    /**
     * Sets the change flag to a given value. The change flag is used to record
     * whether or not a org.gringene.jmesudoyu.base.Point within the line has changed recently.
     * 
     * @param tChanged
     *            state to set changed flag to
     */
    public void setChanged(boolean tChanged) {
        this.changed = tChanged;

    }
    /**
     * Generates an array containing the Points in this line as an array of
     * bit-packed integers.
     * 
     * @param result
     *            Result array to store point value in
     */
    public void getPoints(int[] result) {
        for (int i = 0; i < 9; i++)
            result[i] = points[i].getValue();
    }
   /**
    * Generates a signature corresponding to the solve state of the line. This
    * iterates through all the Points in the line to generate a single signature
    * that summarises the signatures of those Points.
    * 
    * @return A signature reflecting the solve state of the line
    */
   public int signature() {
      int sVal = 1;
      for (int i = 0;(sVal > 0) && (i < 9); i++) {
         if (points[i].signature() == Point.BLANK)
            sVal = Point.BLANK;
         else if (points[i].signature() == Point.MULTIPLE)
            sVal = Point.MULTIPLE;
      }
      return sVal;
   }
   /**
     * Logic method that identifies unique values within a column / box. If a
     * number (or possibly set of numbers) is unique to a point within a row,
     * column or box, then set that point to the unique number(s).
     */
    public boolean setUnique() {
        boolean retVal = false;
        getPoints(tempVals);
        for (int pos = 0; pos < 9; pos++) {
            int pVal = tempVals[pos];
            if (Point.countBits(pVal) > 1) {
                int bitLogic = pVal;
                for (int i = 0; i < 9; i++) {
                    if (i != pos)
                        bitLogic = bitLogic & ~tempVals[i];
                }
                if ((Point.countBits(bitLogic) > 0) && (!points[pos].equals(bitLogic))) {
                    points[pos].setBits(bitLogic);
                    retVal = true;
                }
            }
        }
        return retVal;
    }
   /**
     * Logic method that removes explicit disjoint subsets from a row / column /
     * box. If a set of n numbers (and nothing else) only occurrs in n points in
     * a row, column or box, then remove those numbers from the possibilities at
     * all other points within that row, column or box.
     */
    public boolean remDisjoint() {
        boolean retVal = false;
        getPoints(tempVals);
        for (int pos = 0; pos < 9; pos++) {
            int pVal = tempVals[pos];
            if (Point.countBits(pVal) > 1) {
                int countEqual = 0;
                for (int i = 0; i < 9; i++) {
                    if (tempVals[i] == pVal)
                        countEqual++;
                }
                if ((countEqual > 1) && (countEqual < 9)
                        && (countEqual >= Point.countBits(pVal))) {
                    for (int i = 0; i < 9; i++) {
                        if (tempVals[i] != pVal){
                            retVal = points[i].remBits(pVal) || retVal;
                        }
                    }
                }
            }
        }
        return retVal;
    }
   /**
    * Retrieves a point at a specific location in the line.
    * 
    * @param pos
    *           Position to retrieve the point from
    * @return the org.gringene.jmesudoyu.base.Point that is at the specified location in the line
    */
   public Point getPoint(int pos) {
      return points[pos];
   }
   /**
    * Sets a point at a specific location in the line.
    * 
    * @param pos
    *           Position on the line that the point will be set at
    * @param p
    *           org.gringene.jmesudoyu.base.Point to place at the specified location in the line
    */
   public void setPoint(int pos, Point p) {
      points[pos] = p;
   }
   /**
     * Removes a candidate number from all Points in this org.gringene.jmesudoyu.base.Line except those that
     * correspond with another org.gringene.jmesudoyu.base.Line.
     * 
     * @param tVal
     *            Number to remove from potential candidates
     * @param tPos
     *            Corresponding org.gringene.jmesudoyu.base.Line to not remove number from
     */
    public boolean remValue(int tVal, Line tPos) {
        boolean retVal = false;
        for (int i = 0; i < 9; i++) {
            if (!points[i].inLine(tPos)){
                retVal = points[i].remValue(tVal) || retVal;
            }
        }
        return retVal;
    }
   /**
     * Removes a candidate number from all Points, skipping over a specific org.gringene.jmesudoyu.base.Point.
     * 
     * @param tVal
     *            Number to remove from potential candidates
     * @param pSkip
     *            Corresponding org.gringene.jmesudoyu.base.Point to not remove number from
     */
    public boolean remValue(int tVal, int pSkip) {
        boolean retVal = false;
        for (int i = 0; i < 9; i++) {
            if (i != pSkip)
                retVal = points[i].remValue(tVal) || retVal;
        }
        return retVal;
    }
   /**
    * Carries out a simple check for errors
    * 
    * @param tVal
    *           Number to remove from potential candidates
    * @param pSkip
    *           Corresponding org.gringene.jmesudoyu.base.Point to remove number from
    */
   public boolean flagErrors(int tVal, int pSkip) {
      boolean errorState = false;
      for (int i = 0; i < 9; i++) {
         if ((i != pSkip) && (points[i].signature() == Point.SINGLE) &&
             ((points[i].getValue() & (1<<tVal)) != 0 )){
            errorState = true;
            points[i].setError(true);
         }
         else{
            points[i].setError(false);
         }
      }
      return errorState;
   }
   /**
    * Removes a candidate number from all Points within this org.gringene.jmesudoyu.base.Line.
    * 
    * @param tVal
    *           Number to remove from potential candidates
    */
   public void remValue(int tVal) {
      for (int i = 0; i < 9; i++) {
         points[i].remValue(tVal);
      }
   }
   /**
     * Removes bit-packed candidate numbers from a set of Points within this
     * org.gringene.jmesudoyu.base.Line.
     * 
     * @param tVal
     *            Bit-packed integer of candidates to remove from potential
     *            candidates
     * @param tPos
     *            Bit-packed position integer for Points to remove candidates
     *            from
     * @param invert
     *            Invert locations (i.e. remove numbers from all except the
     *            given points)
     * @return True if bits have been removed from a org.gringene.jmesudoyu.base.Point
     */
    public boolean remBits(int tVal, int tPos, boolean invert) {
        boolean retVal = false;
        for (int i = 0; i < 9; i++) {
            if(invert ^ ((tPos & (1 << i)) != 0)){
                retVal = points[i].remBits(tVal) || retVal;
            }
         }
        return retVal;
    }
    /**
     * Removes a candidate number from all Points in this org.gringene.jmesudoyu.base.Line except those that
     * correspond with another org.gringene.jmesudoyu.base.Line.
     * 
     * @param tVal
     *            Candidates to remove from this org.gringene.jmesudoyu.base.Line
     * @param tLine
     *            Corresponding org.gringene.jmesudoyu.base.Line to not remove candidates from
     * @return true if candidate numbers were removed
     */
    public boolean remBits(int tVal, Line tLine){
        boolean retVal = false;
        if(tVal != 0){
            for (int i = 0; i < 9; i++) {
                if (!points[i].inLine(tLine)){
                    retVal = points[i].remBits(tVal) || retVal;
                }
            }
        }
        return retVal;
    }
   /**
     * Logic method that removes implicit / hidden subsets of numbers from
     * subsets of Points within the org.gringene.jmesudoyu.base.Line. When the cardinality of the union of n
     * Points is n, remove that union from all other Points. This is actually a
     * generalisation of most of the other logic methods, but is incredibly time
     * consuming, so is only done as a last resort.
     * 
     * @return true if values were removed
     */
    public boolean remSubsets() {
        boolean retVal = false;
        int posMask = 0;
        getPoints(tempVals);
        for(int i=0; i<9; i++){
            if(Point.signature(tempVals[i]) == Point.MULTIPLE){
                posMask |= 1 << i;
            }
        }
        for (int tPos = 1; tPos < 511; tPos++) {
            if((tPos & posMask) != 0){
            int lUnion = this.union(tPos, tempVals);
                if(lUnion != 511){
                    if (Point.countBits(lUnion) == Point.countBits(tPos)) {
                        retVal = this.remBits(lUnion, tPos, true) || retVal;
                    }
                }
            }
        }
        return retVal;
    }
   /**
     * Calculates the union of given members of this line, identified by a
     * bit-packed integer.
     * 
     * @param tPos
     *            Bit-packed location integer for Points to generate union for
     * @param tVals
     *            Integer array of point values to extract union from
     * @return Bit-packed union of the specified Points
     */
    public int union(int tPos, int[] tVals) {
        int retVal = 0;
        for (int curPoint = 0; curPoint < 9; curPoint++) {
            if(((1 << curPoint) & tPos) != 0){
                retVal |= tVals[curPoint];
            }
        }
        return retVal;
    }
    /**
     * Calculates the intersection of given members of this line, identified by
     * a bit-packed integer.
     * 
     * @param tPos
     *            Bit-packed location integer for Points to generate
     *            intersection for
     * @param tVals
     *            Integer array of point values to extract intersection from
     * @return Bit-packed intersection of the specified Points
     */
    public int intersection(int tPos, int[] tVals) {
        int retVal = 511;
        for (int curPoint = 0; curPoint < 9; curPoint++) {
            if(((1 << curPoint) & tPos) != 0){
                retVal &= tVals[curPoint];
            }
        }
        return retVal;
    }
    /**
     * Helper method which just calls complement(org.gringene.jmesudoyu.base.Line) twice, switching the
     * "current" and "intersect" line. This saves a bit of duplication when
     * calling these functions.
     * 
     * @param tLine
     *            intersecting line
     * @return True if candidate numbers have been removed
     */
    public boolean complementCheck(Line tLine){
        return this.complement(tLine) || tLine.complement(this);
    }
    /**
     * An advanced logic / solver that compares the Points in this org.gringene.jmesudoyu.base.Line which
     * don't intersect a given org.gringene.jmesudoyu.base.Line with those that do intersect that org.gringene.jmesudoyu.base.Line. If
     * any candidates appear in the intersecting Points, but not outside the
     * intersecting Points, then those candiates can be removed from the
     * non-intersecting Points in that org.gringene.jmesudoyu.base.Line.
     * 
     * @param tLine
     *            intersecting org.gringene.jmesudoyu.base.Line
     * @return True if candidate numbers have been removed
     */
    public boolean complement(Line tLine){
        int inVal = 0;
        int outVal = 0;
        for(int curPoint = 0; curPoint < 9; curPoint++){
            if(points[curPoint].inLine(tLine)){
                inVal |= points[curPoint].getValue();
            }
            else{
                outVal |= points[curPoint].getValue();
            }
        }
        return tLine.remBits(inVal & ~outVal, this);
    }
}
