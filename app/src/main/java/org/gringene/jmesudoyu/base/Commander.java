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
 * The Commander class manages commands, acting as an intermediary between the
 * command listener and the rest of the classes. This class should be platform
 * independent, which essentially means no imports beyond those in the union of
 * all platforms that this is likely to run on (ideally, no imports at all).
 * 
 * @author gringer
 * 
 */
public class Commander implements Runnable {
    public static int SETTINGSSIZE = 1;
   Controller gameController;
   Painter gamePainter;
   Board gameBoard;

   Point[] undoBoard = new Point[81];
   Point[] cutBoard = new Point[81];
   Point[] tempBoard = new Point[81];

   Thread puzzler; // used for puzzle generation

   boolean expertMode; // place numbers with fewer key presses (box, cell, number)?
   boolean numMode; // draw 'current number' on top of cell (and place that number when clicked)?
   boolean hasWon; // has the user completed the current puzzle?
   boolean flipMode; // are the number positions flipped from the usual cellphone locations?
   boolean candMode; // should candidates be placed/flipped (instead of single numbers?

   int posX, posY;
   int oldX, oldY;
   int expB;
   int w, h;
   int val;
   int expertLevel;
   int autoLevel = 0;

   /**
    * Sets up the game system; sets fields to default values, and
    * establishes the width and height of the game field.
    * 
    * @param tController Platform-specific command dispatcher
    * @param tBoard Board / logic controller
    * @param tPainter Platform-specific drawing manager
    */
   public Commander(Controller tController, Board tBoard, Painter tPainter) {
      super();
      gameBoard = tBoard;
      gameController = tController;
      gamePainter = tPainter;
      expertMode = false;
      numMode = true;
      hasWon = false;
      flipMode = false;
      candMode = true;
      posX = 4;
      posY = 4;
      val = 0;
      expB = 0;
      expertLevel = 0;
      oldX = posX;
      oldY = posY;
      w = Math.min(gameController.getWidth(), gameController.getHeight());
      h = Math.min(gameController.getWidth(), gameController.getHeight());
      for (int i = 0; i < 81; i++) {
         undoBoard[i] = new Point();
         cutBoard[i] = new Point();
         tempBoard[i] = new Point();
      }
      gameBoard.staticSave(undoBoard);
      gameBoard.staticSave(cutBoard);
   }

   /**
    * Carries out functions that would be expected to follow on from a press of
    * the primary button of the pointer (or a tap on the screen for
    * touch-sensitive screens). This essentially calculates what square the
    * press happened in, and runs appropriate functions depending on that square.
    * 
    * @param tx X location of pointer press/tap
    * @param ty Y location of pointer press/tap
    */
   public void doPointerPress(int tx, int ty) {
      expertMode = false;
      numMode = true;
      int tpx = gamePainter.pos2cellX(tx);
      int tpy = gamePainter.pos2cellY(ty);
      if ((tpx > 8) && gamePainter.getVertical()) {
         val = tpy;
         checkPos();
         numChange(val+1);
      } else if ((tpy > 8) && !gamePainter.getVertical()) {
         val = tpx;
         checkPos();
         numChange(val+1);
      } else {
         posX = tpx;
         posY = tpy;
         doButton(Controller.LEFT);
      }
      doUpdate(false);
   }

   /**
    * Carries out basic command parsing and platform-independent dispatches.
    * Currently understood commands are the following:
    * <ul>
    * <li> Quit &mdash; Quit / exit the application</li>
    * <li> Load &mdash; Load a game board from a file / resource</li>
    * <li> Save &mdash; Save a game board to a file / resource</li>
    * <li> Help &mdash; Display general help related to the program</li>
    * <li> Keys &mdash; Display more specific help relating to key use</li>
    * <li> Expert &mdash; Change into expert mode, which typically involves less key presses</li>
    * <li> Keyflip &mdash; Change between cellphone (1 at top-left) and keyboard (1 at bottom left) configurations</li>
    * <li> Solve &mdash; Attempt to solve the current puzzle</li>
    * <li> Check &mdash; Check the current board to see if it can be solved</li>
    * <li> Clear &mdash; Clear the current cell</li>
    * <li> Create &mdash; Attempt to create a new puzzle</li>
    * <li> Cancel &mdash; Attempt to cancel a running puzzle generation</li>
    * <li> Accept &mdash; Accept a puzzle generation as it is (don't wait for it
    * to get harder)</li>
    * <li> Lock &mdash; Lock all visible numbers so that they can't be changed</li>
    * <li> Unlock &mdash; Unlock the board, allowing all numbers to be changed</li>
    * <li> Reset &mdash; Clear the current board, ignoring locked Points</li>
    * <li> Undo &mdash; Undo the last operation</li>
    * </ul>
    * 
    * @param tCommand
    *           Command to attempt to parse and deal with
    */
   public void doCommand(String tCommand) {
      if (tCommand.equals("Quit")) {
         gameController.quit();
      } else if (tCommand.equals("Load")) {
         gameBoard.staticSave(undoBoard);
         gameController.loadBoard();
      } else if (tCommand.equals("Save")) {
         gameController.saveBoard();
      } else if (tCommand.equals("Solve")) {
         gameBoard.staticSave(undoBoard);
         gameBoard.applyLogic();
         doUpdate(true);
      } else if (tCommand.equals("MiniSolve")){
         gameBoard.staticSave(undoBoard);
         gameBoard.applyLogic(1);
         doUpdate(true);
      } else if (tCommand.equals("Cancel")) {
         gameBoard.stopCreate();
         doCommand("Undo");
         gameBoard.staticSave(undoBoard);
      } else if (tCommand.equals("Accept")) {
         gameBoard.stopCreate();
         gameBoard.staticSave(undoBoard);
      } else if (tCommand.equals("Create")) {
         gameBoard.staticSave(undoBoard);
         puzzler = new Thread(this);
         puzzler.start();
      } else if (tCommand.equals("Expert")) {
         setExpert(!expertMode);
      } else if (tCommand.equals("Lock")) {
         gameBoard.lockBoard();
         doUpdate(true);
      } else if (tCommand.equals("Clear")) {
         gameBoard.clear(posX, posY);
         doUpdate(false);
      } else if (tCommand.equals("Check")) {
         // NOTE: This hoses the cutBuffer
         gameBoard.staticSave(cutBoard);
         gameBoard.applyLogic();
         if (gameBoard.signature() == Point.SINGLE)
            gameController.infoMsg("Solvable", "Puzzle is solvable.\n\n"
                  + "[solvable by the logic known to this program]");
         else if (gameBoard.signature() == Point.MULTIPLE)
            gameController
                  .infoMsg(
                        "Unsure",
                        "This program is unable to determine "
                              + "whether or not the puzzle is solvable.\n\n"
                              + "[no contradictions "
                              + "reached, but not fully solvable by logic known to this "
                              + "program]");
         else if (gameBoard.signature() == Point.BLANK)
            gameController
                  .infoMsg(
                        "Incorrect",
                        "Puzzle / solution is invalid.\n\n"
                              + "[a contradiction "
                              + "is reached when applying logic known to this program]");
         gameBoard.staticLoad(cutBoard);
         doUpdate(true);
      } else if (tCommand.equals("Analyse") || tCommand.equals("Analyze")) {
          // NOTE: This hoses the cutBuffer
          gameBoard.staticSave(cutBoard);
          String anStr = gameBoard.applyLogic();
          int gameSig = gameBoard.signature();
          if (gameSig == Point.BLANK)
             gameController.infoMsg("Incorrect", "Puzzle/solution is invalid.\n\n" +
                     "(Further analysis would not make sense)");
          else if(gameSig == Point.MULTIPLE){
              gameController.infoMsg("Unsure", "Puzzle is beyond the logic " +
                    "of this program.\n\n" + anStr);
          }
          else if(gameSig == Point.SINGLE){
              gameController.infoMsg("Solvable", "Logic levels required for " +
                    "completion follow.\n\n" + anStr);
          }
          gameBoard.staticLoad(cutBoard);
          doUpdate(true);
      } else if (tCommand.equals("Unlock")) {
         gameBoard.unlockBoard();
         doUpdate(true);
      } else if (tCommand.equals("Keyflip")) {
         flipMode = !flipMode;
      } else if (tCommand.equals("Reset")) {
         gameBoard.staticSave(undoBoard);
         gameBoard.reset(true);
         hasWon = false;
         doUpdate(true);
      } else if (tCommand.equals("Help")) {
         gameController.helpMessage();
      } else if (tCommand.equals("Keys")) {
         gameController.keysMessage();
      } else if (tCommand.equals("Undo")) {
         gameBoard.staticSave(tempBoard);
         gameBoard.unlockBoard();
         gameBoard.staticLoad(undoBoard);
         for (int i = 0; i < 81; i++) {
            undoBoard[i].setBits(tempBoard[i]);
         }
         doUpdate(true);
      }
   }

   /**
    * Carries out functions that would be expected to follow on from a press of
    * a specific button of the pointer.
    * 
    * @param button Indicates the button that has been pressed
    */
   public void doButton(int button) {
      gameBoard.staticSave(undoBoard);
      if (button == Controller.LEFT) {
         if (expertMode) {
            if (expertLevel == 2) {
               setVal();
            }
         } else if (!numMode) {
            setVal();
         } else {
            doUpdate(false);
         }
      } else if (button == Controller.MIDDLE) {
         if (expertLevel != 1) 
            gameBoard.remove(val, posX, posY);
         expertLevel = 0;
         doUpdate(false);
      } else if (button == Controller.RIGHT) {
         if (expertLevel != 1) 
            gameBoard.clear(posX, posY);
         expertLevel = 0;
         doUpdate(false);
      }
      checkWon();
   }

   /**
     * Carries out functions that would be expected to follow on from a change,
     * setting, or modification of the current number. This varies depending on
     * the different modes in the program (number mode, expert mode, drag mode).
     * 
     * @param tNum
     *            Number that has been "pressed"
     * @return True if the operation was successful
     */
    public boolean numChange(int tNum) {
        if ((tNum >= 0) && (tNum <= 9)) {
            if (tNum == 0) {
                if (expertMode && (expertLevel != 0)) {
                    expertLevel = 0;
                } else {
                    setExpert(!expertMode);
                    // numMode = !numMode;
                }
            } else if (expertMode) {
                if (flipMode) {
                    if ((tNum < 4) || (tNum > 6)) {
                        tNum = (tNum + 6) % 12;
                    }
                }
                if (expertLevel == 0) {
                    expB = tNum - 1;
                    posX = (expB % 3) * 3;
                    posY = (expB / 3) * 3;
                    expertLevel = 1;
                } else if (expertLevel == 1) {
                    posY = (expB / 3) * 3 + ((tNum - 1) / 3);
                    posX = (expB % 3) * 3 + ((tNum - 1) % 3);
                    expertLevel = 2;
                } else if (expertLevel == 2) {
                    if (flipMode) {
                        if ((tNum < 4) || (tNum > 6)) {
                            tNum = (tNum + 6) % 12;
                        }
                    }
                    val = tNum - 1;
                    doButton(Controller.LEFT);
                }
            } else if (numMode) {
                if ((tNum >= 1) && (tNum <= 9)) {
//                    if (flipMode) {
//                        if ((tNum < 4) || (tNum > 6)) {
//                            tNum = (tNum + 6) % 12;
//                        }
//                    }
                    val = tNum - 1;
                    checkPos();
                    gameBoard.staticSave(undoBoard);
                    setVal();
                    checkWon();
                }
            } else {
                if (tNum == 1) {
                    val = val - 1;
                    checkPos();
                } else if (tNum == 2)
                    changePos(0, -1);
                else if (tNum == 3) {
                    val = val + 1;
                    checkPos();
                } else if (tNum == 4)
                    changePos(-1, 0);
                else if (tNum == 5) {
                    doButton(Controller.LEFT);
                } else if (tNum == 6)
                    changePos(1, 0);
                else if (tNum == 7)
                    doCommand("Undo");
                else if (tNum == 8)
                    changePos(0, 1);
                else if (tNum == 9) {
                    doButton(Controller.RIGHT);
                }
            }
            doUpdate(false);
            return true;
        } else {
            doUpdate(false);
            return false;
        }
    }

   /**
    * Initialise the game board, which generally involves doing a repaint of the
    * screen and clearing all the undo / save states in the program.
    */
   public void init() {
      gameBoard.staticSave(undoBoard);
      gameBoard.staticSave(cutBoard);
      doUpdate(true);
   }
   /**
     * Retrieves key/value pairs for game settings (currently only expert mode
     * activation) to be stored in a persistent data storage area
     * 
     * @param keys keys associated with particular game settings
     * @param values values associated with the respective keys
     * @return number of settings saved
     */
   public int saveSettings(String[] keys, int[] values){
       int stopAt = Commander.SETTINGSSIZE;
       int cPos = 0;
       if(keys.length < Commander.SETTINGSSIZE){
           System.out.println("Warning: input array is too small to store all settings");
           stopAt = keys.length;
       }
       if(stopAt > cPos){
           keys[cPos] = "ExpertMode";
           values[cPos] = this.getExpert() ? 0 : 1;
           cPos++;
       }
       return cPos;
   }
   /**
    * Parses key/value pairs for game settings (currently only expert mode
    * activation) from a provided set
    * 
    * @param keys keys associated with particular game settings
    * @param values values associated with the respective keys
    * @return number of settings loaded
    */
   public int loadSettings(String[] keys, int[] values){
       int numSettings = 0;
       int numLoaded = 0;
       for(int i=0; (i < keys.length) && (!keys[i].equals("<FINISH>")); i++){
           numSettings++;
       }
       System.out.println("Loading settings...");
       for(int i=0; i < numSettings; i++){
           if(keys[i].equals("ExpertMode")){
               this.setExpert(values[0] == 0);
               numLoaded++;
           }
           System.out.println(keys[i] + ": " + values[i]);
       }
       return numLoaded;
   }
   /**
    * Changes to and from expert mode, where numbers and locations can be
    * specified by a minimal number of key presses.
    * 
    * @param expVal
    *           True if expert mode should be set, false otherwise.
    */
   public void setExpert(boolean expVal) {
      expertMode = expVal;
      doUpdate(false);
      expertLevel = 0;
   }
   /**
     * Retrieves the current state of expert mode.
     * 
     * @return true if expert mode is enabled.
     */
   public boolean getExpert(){
       return expertMode;
   }

   /**
    * Changes to and from candidate mode, where candidate numbers are displayed.
    * 
    * @param candVal
    *           True if candidate mode should be set, false otherwise.
    */
   public void setCand(boolean candVal) {
      candMode = candVal;
      doUpdate(true);
   }

   /**
    * Carries out functions that would be expected to follow on from a change in
    * the position of the board "cursor". This involves attempting to change the
    * fields associated with the pointer location, checking to make sure that
    * the change is valid, and updating the screen to reflect that change.
    * 
    * @param xAdd
    *           Increment by which the horizontal location of the pointer has
    *           changed
    * @param yAdd
    *           Increment by which the vertical location of the pointer has
    *           changed
    */
   public void changePos(int xAdd, int yAdd) {
      expertMode = false;
      posX += xAdd;
      posY += yAdd;
      checkPos();
      doUpdate(false);
   }
   /**
    * Attempts to set the horizontal location of the pointer
    * 
    * @param xPos horizontal location of pointer
    */
   public void setXPos(int xPos){
      expertMode = false;
      posX = xPos;
      checkPos();
      doUpdate(false);
   }
   /**
    * Attempts to set the vertical location of the pointer
    * 
    * @param yPos vertical location of pointer
    */
   public void setYPos(int yPos){
      expertMode = false;
      posY = yPos;
      checkPos();
      doUpdate(false);
   }
   /**
    * Checks the current pointer location, setting it to a reasonable location
    * inside the game field if the pointer is found to be outside.
    */
   private void checkPos() {
      posX = posX % 9;
      posY = posY % 9;
      // possibly unnecessary, but just in case modulus does
      // different things to negative numbers
      posX = posX < 0 ? posX + 9 : posX;
      posY = posY < 0 ? posY + 9 : posY;
      val = (val % 9 + 9) % 9;
   }

   /* (non-Javadoc)
    * @see java.lang.Runnable#run()
    */
   public void run() {
      this.makePuzzle();
      puzzler = null;
   }

   /**
    * Attempt to create a new puzzle. This function sets up progress dialogs to
    * let the user know what is going on, and provides a way in which the puzzle
    * generation can be cancelled.
    */
   public void makePuzzle() {
      GlobalVar[] gv = new GlobalVar[2];
      String[] labels = new String[2];
      int[] limits = new int[2];
      gv[0] = new GlobalVar();
      gv[1] = new GlobalVar();
      labels[0] = "Placements";
      limits[0] = 81;
      labels[1] = "Attempts";
      limits[1] = 10;
      int tries = 0;
      gameController.makeProgress("Solution", labels, limits, gv,
            Controller.OP_CANCEL);
      gameBoard.unlockBoard();
      gamePainter.startUpdate();
      do {
         gv[1].setValue(tries);
         gameBoard.reset(false);
         gameBoard.makeSolution(gv[0]);
         tries++;
      } while ((tries < 10) && (gameBoard.signature() == 0));
      if(tries == 10){
          System.out.println("Reached limit");
      }
      gamePainter.stopUpdate();
      labels[0] = "Removals";
      limits[0] = 81;
      labels[1] = "Time";
      limits[1] = 100000;
      gameController.makeProgress("Puzzle", labels, limits, gv,
            Controller.OP_CANCEL | Controller.OP_ACCEPT);
      gamePainter.startUpdate();
      gameBoard.makeProblem(100000, true, gv[0], gv[1]);
      gamePainter.stopUpdate();
      hasWon = false;
      gameBoard.lockBoard();
      gameController.recoverDisplay();
      doUpdate(true);
   }

   /**
    * Updates (redraws) the current game board to reflect changes that have
    * happened since the board was last drawn.
    * 
    * @param clearBoard
    *           True if the whole board should be redrawn, false if only the
    *           current location should be redrawn.
    */
   public void doUpdate(boolean clearBoard) {
      if (clearBoard)
         gamePainter.drawBoard(candMode);
      gamePainter.clearPos(oldX, oldY, candMode);
      if (expertMode) {
         if (expertLevel == 1) {
            gamePainter.drawBox(expB);
         } else {
            gamePainter.clearBox(expB);
            if (expertLevel == 2) {
               gamePainter.drawSquare(posX, posY);
            }
         }
      } else {
         if (expertLevel != 0) {
            if (expertLevel == 1) {
               gamePainter.clearBox(expB);
               gamePainter.drawSquare(posX, posY);
            }
            expertLevel = 0;
         }
         gamePainter.drawPos(val, posX, posY, !numMode, candMode);
      }
      gamePainter.drawChoice(val);
      oldX = posX;
      oldY = posY;
      gameController.doUpdate();
   }

   /**
    * Carries out functions that would be expected to follow on from the user
    * believing that they have finished / completed a puzzle. This function
    * checks to see if the winning state has been achieved. If a winning state
    * has not been achieved, the program should point out contradictions;
    * otherwise the program displays a dialog to indicate that the user has
    * finished / won.
    * 
    */
   private void checkWon() {
      if ((gameBoard.signature() == Point.SINGLE) && !hasWon) {
         gameBoard.applyLogic();
         doUpdate(true);
         if (gameBoard.signature() == Point.SINGLE) {
            gameController.infoMsg("You Won!", "Puzzle has been solved.");
            gameController.win();
            hasWon = true;
         }
      }
   }

   /**
    * Carries out functions that would be expected to follow on from a number
    * being placed at a specific position on the board. This function includes
    * automatically applying simple logic (if set), and redrawing the board
    * after the number placement has been attempted.
    */
   private void setVal() {
      if(!candMode)
         gameBoard.set(val, posX, posY);
      else
         gameBoard.flip(val, posX, posY);
      gameBoard.applyLogic(autoLevel);
      expertLevel = 0;
      doUpdate(gameBoard.flagErrors(posX, posY));
   }
}
