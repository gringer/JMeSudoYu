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
 *
 * A global variable class that does nothing except store an integer. The integer can be named with
 * a label if desired.
 */
public class GlobalVar {
  private final String varLabel;
  private int value = 0;

  public GlobalVar() {
    varLabel = "";
  }

  public GlobalVar(String tLabel){
    varLabel = tLabel;
  }

  /**
   * Retrieves the current label for this global variable.
   * @return The label associated with this global variable class
   */
  public String getLabel() { return varLabel; }

  /**
   * Retrieves the current representation of this global variable.
   * @return The integer stored in the global variable class
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets the internal integer to a specific value
   * @param tVal New value that the global variable should be set to
   */
  public void setValue(int tVal) {
    value = tVal;
  }

  /**
   * Compares the internal value with another integer.
   * @param tVal Value to compare this global variable with
   * @return True if the the two values are the same
   */
  public boolean equals(int tVal) {
    return value == tVal;
  }

  /**
   * Compares the internal value with another integer.
   * @param tVal Value to compare this global variable with
   * @return True if the the two values are <em>different</em>
   */
  public boolean notEquals(int tVal) {
    return value != tVal;
  }
}
