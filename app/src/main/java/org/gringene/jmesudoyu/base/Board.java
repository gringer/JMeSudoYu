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

import java.util.Random;

/**
 * Main logic processing class for the program. This class integrates the
 * various logic functions in the Point and Line classes, and is the interface
 * between the underlying board logic and the higher level user interface
 * classes in the program. This class should be platform independent, which
 * essentially means no imports beyond those in the union of all platforms that
 * this is likely to run on (ideally, no imports at all).
 *
 * @author gringer
 * @see Point
 * @see Line
 * @see Box
 *
 */
public class Board {
  public static int LOGIC_LIMIT = 5;
  int LOGIC_MAX = 4;

  Line boxes[];

  Line rows[];

  Line columns[];

  Point board[];

  Random rGen;

  boolean doCreate;

  boolean hasStopped;

  /**
   * Creates a blank board. All points on the board are set up, and the points
   * are linked into their associated rows, columns and boxes.
   *
   * @see Point
   * @see Line
   * @see Box
   */
  public Board() {
    board = new Point[81];
    rows = new Line[9];
    columns = new Line[9];
    boxes = new Line[9];
    rGen = new Random();
    doCreate = true;
    for (int i = 0; i < 9; i++) {
      rows[i] = new Line();
      columns[i] = new Line();
      boxes[i] = new Box();
    }
    for (int r = 0; r < 9; r++) {
      for (int c = 0; c < 9; c++) {
        Point pTemp = new Point();
        board[r * 9 + c] = pTemp;
        pTemp.setRow(rows[r], c);
        pTemp.setColumn(columns[c], r);
        pTemp.setBox(boxes[r / 3 * 3 + c / 3], (r % 3) * 3 + c % 3);
      }
    }
  }

  /**
   * Indicates the completion state of the board. Possible return values are as
   * follows:
   * <UL>
   * <LI>Point.BLANK where board contains a contradiction (a point that cannot
   * have any numbers in it)</LI>
   * <LI>Point.SINGLE where board is complete (every point has only one
   * candidate number)</LI>
   * <LI>Point.MULTIPLE where board is incomplete (some points have more than
   * one candidate number)</LI>
   * </UL>
   *
   * @return The signature of this board
   * @see Point#signature()
   */
  public int signature() {
    int sVal = Point.SINGLE;
    for (int i = 0; (sVal > 0) && (i < 81); i++) {
      if (board[i].signature() == Point.BLANK)
        sVal = Point.BLANK;
      else if (board[i].signature() == Point.MULTIPLE)
        sVal = Point.MULTIPLE;
    }
    return sVal;
  }

  /**
   * <p>
   * Attempts to generate a completed Sudoku grid. The method will work from
   * the current layout of the game board, and add a minimal number of random
   * numbers (selected from all possible candidates on the board) to derive a
   * completed grid. The fullest possible logic is applied after each placement
   * of a new number, which should reduce the chances of failed attempts at
   * generating solutions. If an attempt <em>does</em> create a solution that
   * involves contradictions, the method will finish, rather than trying again.
   * During the solution creation process, a monitor variable is updated
   * (indicating the number of board positions that are completed), so that
   * feedback on the process can be provided to the end user.
   * </p>
   * <p>
   * If this method is stopped, then the board will remain in the state it was
   * at that point in the solution creation, leaving the "cleanup" job to other
   * methods.
   * </p>
   *
   * @param ta
   *           Monitor variable for the number of completed board placements
   *
   * @see #stopCreate()
   * @see GlobalVar
   */
  public void makeSolution(GlobalVar ta) {
    doCreate = true;
    applyLogic(LOGIC_MAX);
    int[] gbCands = new int[81];
    int[] bCands = new int[9];
    int numCands, numBits;
    Point tPoint;
    ta.setValue(0);
    while ((signature() > 1) && (doCreate)) {
      numCands = getCandidates(gbCands, Point.MULTIPLE);
      tPoint = board[gbCands[(rGen.nextInt() >>> 1) % numCands]];
      numBits = tPoint.getBits(bCands);
      this.clearChanged();
      tPoint.setExact(bCands[(rGen.nextInt() >>> 1) % numBits]);
      applyLogic(LOGIC_MAX);
      ta.setValue(countComplete());
    }
    if (!doCreate) {
      System.out.println("Told to stop solution creation");
    }
    this.setChanged();
  }

  /**
   * Sends a signal to the creator methods (makeProblem, makeSolution) to stop
   * what they are doing. The method will wait up to two seconds before
   * returning, so that the creator method will have a bit of time to finish
   * its current iteration before further operations are carried out on the
   * board. If two seconds has passed and the creation has not been stopped
   * then the method will return, spitting out a message, "Unable to stop
   * creation", to the console. The signal remains until the creator methods
   * have stopped, so the methods <em>should</em> stop the next time they
   * check the signal state.
   *
   * @see #makeProblem(int, boolean, GlobalVar, GlobalVar)
   * @see #makeSolution(GlobalVar)
   */
  public void stopCreate() {
    hasStopped = false;
    long inTime = System.currentTimeMillis();
    doCreate = false;
    while ((!hasStopped) && ((System.currentTimeMillis() - inTime) < 2000)) {
      try {
        Thread.sleep((long) 200);
      } catch (Exception e) {
        System.err.println("Interrupted sleep. How annoying!");
      }
    }
    if (!hasStopped) {
      System.out.println("Unable to stop creation");
    }
  }

  /**
   * <p>
   * Attempts to generate a Sudoku puzzle. The method will work from the
   * current layout of the game board, and remove a maximal number of random
   * numbers (selected from all visible numbers on the board) to derive a
   * Sudoku puzzle with a unique solution. During the puzzle creation process,
   * two monitor variables are updated (indicating the number of attempted
   * number removals, and the time taken), so that feedback on the process can
   * be provided to the end user.
   * </p>
   * <p>
   * The current method of generating a puzzle is to remove numbers, then
   * check to see if the puzzle is still solvable with the logic available to
   * this program. If a removal reduces the puzzle to something that has a
   * potentially ambiguous solution, then the board is restored to the state
   * it was before that removal, and another removal is attempted. This
   * process continues until all possible removals have been attempted. There
   * is no additional backtracking in the current method, so puzzles with more
   * removed numbers may be possible, given a different sequence of removed
   * numbers.
   * </p>
   * <p>
   * If this method is stopped, then the board will remain in the state it was
   * at that point in the puzzle creation, leaving the "cleanup" job to other
   * methods.
   * </p>
   *
   * @param maxTime
   *            Maximum allowed time for the problem generation step
   * @param useSymmetry
   *            Whether or not the generator should create a symmetrical
   *            puzzle
   * @param ta
   *            Monitor variable for the number of attempted board removals
   * @param tt
   *            Monitor variable for the total time taken
   *
   * @see #stopCreate()
   * @see GlobalVar
   */
  public void makeProblem(int maxTime, boolean useSymmetry, GlobalVar ta,
                          GlobalVar tt) {
    long inTime = System.currentTimeMillis();
    int ia;
    int[] gbCands = new int[81];
    int numCands;
    int pos;
    int[] newGame = new int[81];
    int[] oldGame = new int[81];
    boolean[] pCands = new boolean[81];
    doCreate = true;
    if (signature() == 0) {
      System.out.println("Contradiction found");
      return;
    }
    this.staticSave(newGame);
    staticLoadClear(newGame);
    this.applyLogic(LOGIC_MAX);
    if (signature() != 1) {
      System.out.println("Not solvable");
      hasStopped = true;
      return;
    }
    ia = 0;
    getCandidates(pCands);
    while (doCreate && (ia < 81)
        && ((System.currentTimeMillis() - inTime) < maxTime)) {
      do {
        staticLoadClear(newGame);
        System.arraycopy(oldGame,0,newGame,0,81);
        numCands = getCandidates(gbCands, pCands, Point.SINGLE);
        if(numCands != 0){
          pos = gbCands[(rGen.nextInt() >>> 1) % numCands];
          this.clearChanged();
          board[pos].clearValue();
          pCands[pos] = false;
          ia++;
          if (useSymmetry && (pos != 40)) {
            board[80 - pos].clearValue();
            pCands[80 - pos] = false;
            ia++;
          }
          ta.setValue(ia);
          tt.setValue((int) (System.currentTimeMillis() - inTime));
          this.staticSave(newGame);
          // newGame == something that is being tested
          applyLogic(LOGIC_MAX, false);
        }
      } while (doCreate && (signature() == Point.SINGLE));
      staticLoadClear(oldGame);
      this.staticSave(newGame);
    }
    System.out.println("Made " + ia + " attempts in "
        + (System.currentTimeMillis() - inTime) + "ms");
    if (!doCreate) {
      System.out.println("Told to stop");
    }
    hasStopped = true;
  }

  /**
   * Gets the Point at a specific location in the board. The current
   * implementation retrieves the point indirectly by calling the getPoint
   * method on a column.
   *
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   * @return Point at (tx, ty)
   */
  public Point getPoint(int tx, int ty) {
    return columns[tx].getPoint(ty);
  }

  /**
   * Clears all logic from a specific board location. This process resets the
   * candidate numbers at that location, so that all numbers are considered
   * candidates. If the location is locked, then the attempt to clear the logic
   * will fail silently.
   *
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   *
   * @see Point#getLocked()
   */
  public void clear(int tx, int ty) {
    if (!columns[tx].getPoint(ty).getLocked())
      columns[tx].getPoint(ty).clearValue();
  }

  /**
   * Runs the error flagging procedure at a specific board location. If a
   * change in error state is found, then the function returns true.
   *
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   *
   * @return Whether or not there have been any changes in error state
   */
  public boolean flagErrors(int tx, int ty) {
    return columns[tx].getPoint(ty).flagErrors();
  }

  /**
   * Places a single number at a specific board location. This is equivalent to
   * clearing the logic, and then removing all candidates except the given
   * number from the location. If the location is locked, then the attempt to
   * set the Point value will fail silently.
   *
   * @param tVal
   *           Number to set at the given location
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   *
   * @see Point#getLocked()
   */
  public void set(int tVal, int tx, int ty) {
    if (!columns[tx].getPoint(ty).getLocked())
      columns[tx].getPoint(ty).setValue(tVal);
  }

  /**
   * Removes a single number from the candidates at a specific board location.
   * If the number is already absent from candidates, then the function will do
   * nothing. If the location is locked, then the attempt to set the remove the
   * number from potential candidates will fail silently.
   *
   * @param tVal
   *           Number to remove from the given location
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   *
   * @see Point#getLocked()
   */
  public void remove(int tVal, int tx, int ty) {
    if (!columns[tx].getPoint(ty).getLocked())
      columns[tx].getPoint(ty).remValue(tVal);
  }

  /**
   * Inverts the candidate status of the given number at a specific board
   * location. If the number is already absent from candidates, then the
   * function will add the number to the candidates. If the location is locked,
   * then the attempt to set the remove the number from potential candidates
   * will fail silently.
   *
   * @param tVal
   *           Number to remove from the given location
   * @param tx
   *           Location along the x-axis (column)
   * @param ty
   *           Location along the y-axis (row)
   *
   * @see Point#getLocked()
   */

  public void flip(int tVal, int tx, int ty) {
    Point p = columns[tx].getPoint(ty);
    if (!p.getLocked()){
      if(p.countBits() == 9){
        p.setValue(tVal);
      }
      else{
        p.flipValue(tVal);
      }
      if(p.countBits() == 0){
        p.clearValue();
      }
    }
  }

  /**
   * Attempts to solve the current puzzle, using a series of logical steps up
   * to a certain level. The sweep will continue to try different techniques
   * until no part of the board changes from one iteration to the next. If
   * Points are specified (i.e. non-null), then the logic application will
   * stop if one of the Points has a Point.SINGLE signature.
   *
   * @param logLevel
   *            Maximum logic level to use for logical deductions
   * @param analyse
   *            produce an analysis string?
   * @return Analysis string, indicating the difficulty of the puzzle
   */
  public String applyLogic(int logLevel, boolean analyse) {
    String retVal = "";
    int[] opCounts = new int[Board.LOGIC_LIMIT];
    boolean changed;
    boolean[] singlePoints = new boolean[81];
    for (int i = 0; i < logLevel; i++) {
      opCounts[i] = 0;
    }
    do {
      changed = false;
      if (logLevel >= 1) {
        this.getCandidates(singlePoints);
        for (int i = 0; i < 81; i++) {
          if (singlePoints[i] && board[i].clearOthers()) {
            opCounts[0]++; // logLevel - 1
            changed = true;
          }
        }
        if(changed){
          continue;
        }
      }
      for (int i = 0; i < 9; i++) {
        boolean tRowChanged = rows[i].getChanged();
        boolean tColChanged = columns[i].getChanged();
        boolean tBoxChanged = boxes[i].getChanged();
        if (logLevel >= 2) {
          // System.out.print("2.");System.out.flush();
          if (tRowChanged && rows[i].setUnique()) {
            opCounts[1]++;
            changed = true;
          }
          if (tColChanged && columns[i].setUnique()) {
            opCounts[1]++;
            changed = true;
          }
          if (tBoxChanged && boxes[i].setUnique()) {
            opCounts[1]++;
            changed = true;
          }
          if(changed){
            break;
          }
        }
        if (logLevel >= 3) {
          if (tRowChanged && rows[i].remDisjoint()) {
            opCounts[2]++;
            changed = true;
          }
          if (tColChanged && columns[i].remDisjoint()) {
            opCounts[2]++;
            changed = true;
          }
          if (tBoxChanged && boxes[i].remDisjoint()) {
            opCounts[2]++;
            changed = true;
          }
          if(changed){
            break;
          }
        }
        if (logLevel >= 4) {
          if(tBoxChanged || columns[(i%3)*3].getChanged() ||
              columns[(i%3)*3+1].getChanged() ||
              columns[(i%3)*3+2].getChanged()){
            if( boxes[i].complement(columns[(i%3)*3]) ||
                boxes[i].complement(columns[(i%3)*3+1]) ||
                boxes[i].complement(columns[(i%3)*3+2])){
              opCounts[3]++;
              changed = true;
            }
          }
          if(tBoxChanged || rows[(i/3)*3].getChanged() ||
              rows[(i/3)*3+1].getChanged() ||
              rows[(i/3)*3+2].getChanged()){
            if( boxes[i].complement(rows[(i/3)*3]) ||
                boxes[i].complement(rows[(i/3)*3+1]) ||
                boxes[i].complement(rows[(i/3)*3+2])){
              opCounts[3]++;
              changed = true;
            }
          }
          if(changed){
            break;
          }
        }
        if (logLevel >= 5) {
          // System.out.print("5.");System.out.flush();
          if (rows[i].getChanged() && rows[i].remSubsets()) {
            opCounts[4]++;
            changed = true;
          }
          if (columns[i].getChanged() && columns[i].remSubsets()) {
            opCounts[4]++;
            changed = true;
          }
          if (boxes[i].getChanged() && boxes[i].remSubsets()) {
            opCounts[4]++;
            changed = true;
          }
          if(changed){
            break;
          }
        }
      }
    } while (changed);
    if(analyse){
      for (int i = 0; i < logLevel; i++) {
        retVal = retVal + "("+(i+1)+","+opCounts[i]+")";
      }
      retVal = retVal + "\n\nDifficulty: ";
      if(opCounts[4] != 0){
        retVal = retVal + "tough";
      }
      else if(opCounts[3] != 0){
        retVal = retVal + "hard";
      }
      else if((opCounts[2] != 0) || (opCounts[1] != 0)){
        retVal = retVal + "moderate";
      }
      else {
        retVal = retVal + "gentle";
      }
    }
    return retVal;
  }

  /**
   * Attempts to solve the current puzzle, using a series of logical steps up
   * to a certain level. The sweep will continue to try different techniques
   * until no part of the board changes from one iteration to the next.
   *
   * @param logLevel
   *            Maximum logic level to use for logical deductions
   * @return Analysis string, indicating the difficulty of the puzzle
   */
  public String applyLogic(int logLevel) {
    return applyLogic(logLevel, true);
  }

  /**
   * Attempts to solve the puzzle up to the maximum permissible logic level.
   * Due to limitations in processing speed, this may not be the same as the
   * highest logic level understood by the application.
   */
  public String applyLogic() {
    return applyLogic(LOGIC_MAX);
  }

  /**
   * Clears the current game board, allowing a new attempt at a puzzle. This
   * either clears the entire board, or only those locations where the number
   * has not been locked in.
   *
   * @param noLocked
   *           True if locked locations should not be cleared
   */
  public void reset(boolean noLocked) {
    for (int i = 0; i < 81; i++) {
      if (!(noLocked && board[i].getLocked()))
        board[i].clearValue();
    }
  }

  /**
   * Loads a board definition from an array of integers. Because the array will
   * not contain extra flags for the Points on the board, all Point flags will
   * be set to their default values. This should probably just be called
   * staticLoad(int[]).
   *
   * @param tBoard
   *           array containing board to be loaded
   */
  public void staticLoad(int[] tBoard) {
    if (tBoard.length != 81) {
      System.out.println("Loaded board is an incorrect size");
    } else {
      reset(false);
      System.out.println("Loading the board");
      for (int i = 0; i < 81; i++) {
        board[i].setExact(tBoard[i]);
      }
    }
  }

  /**
   * Loads a board definition from an array of Points. All Point flags will be
   * transferred, as well as the actual number values / candidates.
   *
   * @param tBoard array containing Points to load
   */
  public void staticLoad(Point[] tBoard) {
    if (tBoard.length != 81) {
      System.out.println("Loaded board is an incorrect size");
    } else {
      reset(false);
      for (int i = 0; i < 81; i++) {
        board[i].setBits(tBoard[i]);
      }
    }
  }

  /**
   * Loads a board flag definition from an array of bytes. This method
   * essentially complements staticLoadInt, accounting for the things that a
   * single integer array of board points doesn't include.
   *
   * @param tFlags
   *           array containing board to be loaded
   */
  public void loadFlags(byte[] tFlags) {
    if (tFlags.length != 81) {
      System.out.println("Incorrect number of flags loaded");
    }
    for (int i = 0; i < 81; i++) {
      board[i].setLocked((tFlags[i] & 1) == 0);
    }
  }

  /**
   * Saves a board flag definition to an array of bytes. This method
   * essentially complements staticSave(int[]), accounting for the things that
   * a single integer array of board points doesn't include.
   *
   * @param tFlags
   *           array that board will be saved to
   */
  public void saveFlags(byte[] tFlags) {
    if (tFlags.length != 81) {
      System.out.println("Result array is an incorrect size");
    }
    for (int i = 0; i < 81; i++) {
      tFlags[i] = (byte) (tFlags[i] | (board[i].getLocked() ? 0 : 1));
    }
  }

  /**
   * Saves a board definition to an array of integers. Because the array will
   * not contain extra flags for the Points on the board, all Point flags will
   * be lost (unless saveFlags is called as well).
   *
   * @param result
   *           array that board will be saved to
   */
  public void staticSave(int[] result) {
    for (int i = 0; i < 81; i++) {
      result[i] = this.board[i].getValue();
    }
  }

  /**
   * Saves a board definition to an array of bytes. The current arrangement is
   * that each Point is stored as three bytes, two for the candidates at the
   * Point, and one for the flags.
   *
   * @param result
   *            array that board will be saved to
   */
  public void staticSave(byte[] result) {
    for (int i = 0; i < 81; i++) {
      result[i*3  ] = (byte)((this.board[i].getValue() & 0xff00) >>> 8);
      result[i*3+1] = (byte)((this.board[i].getValue() & 0x00ff));
      result[i*3+2] = (byte)(result[i*3+2] | (board[i].getLocked() ? 0 : 1));
    }
  }

  /**
   * Loads a board definition from an array of bytes. This assumes that each
   * Point is stored as three bytes, two for the candidates at the Point, and
   * one for the flags.
   *
   * @param tBoard
   *            array containing board to be loaded
   */
  public void staticLoad(byte[] tBoard) {
    if (tBoard.length != 81 * 3) {
      System.out.println("Loaded board is an incorrect size");
    } else {
      reset(false);
      System.out.println("Loading the board");
      for (int i = 0; i < 81; i++) {
        board[i].setExact(((tBoard[i * 3    ] & 0xff) << 8)
            | ((tBoard[i * 3 + 1] & 0xff)));
        board[i].setLocked((tBoard[i * 3 + 2] & 1) == 0);
      }
    }
  }

  /**
   * Saves a board definition to an array of Points. All Point flags will be
   * transferred, as well as the actual number values / candidates.
   *
   * @param result
   *   array that the board will be saved to
   */
  public void staticSave(Point[] result) {
    for (int i = 0; i < 81; i++) {
      result[i].setBits(this.board[i]);
    }
  }

  /**
   * Locks all the decided points on the current board, preventing them from
   * being modified accidentally.
   */
  public void lockBoard() {
    for (int i = 0; i < 81; i++) {
      if (board[i].signature() == Point.SINGLE)
        board[i].setLocked(true);
    }
  }

  /**
   * Unlocks all the points on the current board, allowing them to be modified.
   * The main reason for doing this would probably be to correct a mistake made
   * in a puzzle.
   */
  public void unlockBoard() {
    for (int i = 0; i < 81; i++) {
      board[i].setLocked(false);
    }
  }
  /**
   * Checks to see if a given number appears the maximum number of times on the
   * board. This check excludes situations where more than one number is a
   * candidate in one cell.
   *
   * @param tNum
   *           Number to check
   * @return true if the given number appears 9 times, false otherwise
   */
  public boolean numberComplete(int tNum) {
    Point p = new Point();
    p.setValue(tNum);
    int numCount = 0;
    for (int i = 0; i < 81; i++) {
      if(board[i].equals(p)){
        numCount++;
      }
    }
    return (numCount == 9);
  }
  /**
   * Sets the maximum attempted logic level to a given value. This is
   * typically used for 'slow' devices (such as cellphones) to increase the
   * speed of puzzle generation.
   *
   * @param tLogic logic level
   */
  public void setMaxLogic(int tLogic) {
    this.LOGIC_MAX = Math.max(0, tLogic);
  }
  /**
   * Clears the changed flag on all lines on this board.
   */
  private void clearChanged(){
    for (int i = 0; i < 9; i++) {
      rows[i].setChanged(false);
      columns[i].setChanged(false);
      boxes[i].setChanged(false);
    }
  }
  /**
   * Sets the changed flag on all lines on this board.
   */
  private void setChanged(){
    for (int i = 0; i < 9; i++) {
      rows[i].setChanged(true);
      columns[i].setChanged(true);
      boxes[i].setChanged(true);
    }
  }
  /**
   * Loads a board definition from an array of integers, and removes any clues
   * that are not explicit. More simply, if a point only has one number as an
   * option, then it is set to that number. Otherwise, the point is made
   * completely blank.
   *
   * @param tBoard
   *           array containing board to be loaded
   */
  private void staticLoadClear(int[] tBoard) {
    if (tBoard.length != 81) {
      System.out.println("Loaded board is an incorrect size");
    } else {
      for (int i = 0; i < 81; i++) {
        if (Point.signature(tBoard[i]) == Point.SINGLE)
          board[i].setExact(tBoard[i]);
        else
          board[i].setExact(511);
      }
    }
  }

  /**
   * Retrieves the number of single Points on the board, as well as generating
   * a boolean array of those Point locations.
   *
   * @param result
   *           Array to store locations of single points in
   * @return the number of Points that only have one possible candidate number
   */
  private int getCandidates(boolean[] result) {
    if (result.length < 81) {
      System.out.println("Result array is the wrong size");
      return 0;
    }
    int numCands = 0;
    for (int i = 0; i < 81; i++) {
      result[i] = (board[i].signature() == Point.SINGLE);
      if (result[i])
        numCands++;
    }
    return numCands;
  }

  /**
   * Retrieves the number of single Points on the board, as well as generating
   * an integer array of the locations that match a specific signature.
   *
   * @param result
   *           Array to store locations and values of single Points in
   * @param tSig
   *           Signature to match when checking if the Point is a candidate
   * @return the number of Points that only have one possible candidate number
   */
  private int getCandidates(int[] result, int tSig) {
    if (result.length < 81) {
      System.out.println("Result array is the wrong size");
      return 0;
    }
    int numCands = 0;
    for (int i = 1; i < 81; i++) {
      if (board[i].signature() == tSig) {
        result[numCands++] = i;
      }
    }
    return numCands;
  }

  /**
   * Retrieves the number of single Points on the board, as well as generating
   * an integer array of the locations that match a specific signature. A
   * masking array is also used to select which Points should be considered.
   *
   * @param result
   *           Array to store locations and values of single Points in
   * @param mask
   *           Mask array &mdash; only these Points will be included in the
   *           result
   * @param tSig
   *           Signature to match when checking if the Point is a candidate
   * @return the number of Points that only have one possible candidate number
   */
  private int getCandidates(int[] result, boolean[] mask, int tSig) {
    if (result.length < 81) {
      System.out.println("Result array is the wrong size");
      return 0;
    }
    int numCands = 0;
    for (int i = 1; i < 81; i++) {
      if (mask[i] && (board[i].signature() == tSig)) {
        result[numCands++] = i;
      }
    }
    return numCands;
  }

  /**
   * An optimised version of getCandidates(boolean[]), which only counts the
   * number of single Points on the board.
   *
   * @return the number of Points that only have one possible candidate number
   */
  private int countComplete() {
    int cCount = 0;
    for (int i = 0; i < 81; i++) {
      cCount += (board[i].signature() == Point.SINGLE) ? 1 : 0;
    }
    return cCount;
  }
}
