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
 * Manages platform-specific operations involved with program flow and control,
 * including dialogs and progress indication. Instances of this class will be
 * platform-specific, so this provides a more generic interface to those
 * instances.
 * 
 * @author gringer
 */
public interface Controller {
   static int OP_ACCEPT = 1;
   static int OP_CANCEL = 2;
   static int LEFT = 1;
   static int MIDDLE = 2;
   static int RIGHT = 3;
   static int TOUCH = 4;
   /**
    * Quits the program. 
    */
   abstract public void quit();
   /**
    * Displays (as an information message dialog) help/about information. This
    * should be information that would be useful for a first-time user of the
    * program.
    */
   abstract public void helpMessage();
   /**
    * Displays (as an information message dialog) key/input information. This
    * should give an overview of the keys/inputs that can be used for the
    * program.
    */
   abstract public void keysMessage(); 
   /**
    * Generates a low priority information message dialog. This dialog will wait
    * until the user has indicated that they have read the message.
    * 
    * @param title
    *           title for the dialog
    * @param msg
    *           message body of the dialog
    */
   abstract public void infoMsg(String title, String msg);
   // alert with timeout, logs to System.out
   /**
    * Generate a high priority alert dialog, which reports information to
    * System.out as well. This alert will remain on the screen for a certain
    * length of time, then dismiss itself.
    * 
    * @param title
    *           title for the dialog
    * @param msg
    *           message body of the dialog
    */
   abstract public void alertMsg(String title, String msg);
   /**
    * Generate a high priority alert dialog, with a default title, which reports
    * information to System.out as well. This alert will remain on the screen
    * for a certain length of time, then dismiss itself.
    * 
    * @param msg
    *           message body of the dialog
    */
   abstract public void alertMsg(String msg);
   /**
    * Creates a set of progress bars for display on the screen. The current
    * valid options are as follows:
    * <ul>
    * <li>org.gringene.jmesudoyu.base.Controller.OP_ACCEPT &mdash; include an "accept" option</li>
    * <li>org.gringene.jmesudoyu.base.Controller.OP_CANCEL &mdash; include a "cancel" option</li>
    * </ul>
    * 
    * @param title
    *           title of the progress bar
    * @param labels
    *           array of labels to attach to each progress bar
    * @param limits
    *           upper limit (array of integers) of variables that are attached
    *           to each progress bar
    * @param values
    *           GlobalVars that are associated with the current state of each
    *           progress bar
    * @param cancelOptions
    *           a bit-packed set of options for stopping the operation that the
    *           progress bars are representing
    */
   abstract public void makeProgress(String title, String[] labels,
         int[] limits, GlobalVar[] values, int cancelOptions); 
   /**
    * Returns the current width of the game board.
    * NOTE: This should be the job of the org.gringene.jmesudoyu.base.Painter, not the org.gringene.jmesudoyu.base.Controller
    * 
    * @return the width of the game board, in pixels
    */
   abstract public int getWidth();
   /**
    * Returns the current height of the game board.
    * NOTE: This should be the job of the org.gringene.jmesudoyu.base.Painter, not the org.gringene.jmesudoyu.base.Controller
    * 
    * @return the height of the game board, in pixels
    */
   abstract public int getHeight();
   /**
    * Attempt to reset the focus of the system to the default display / input
    * area of this program.
    */
   abstract public void recoverDisplay();
   /**
    * Redraw the graphics on the screen. This should only require a copy from a
    * buffer to the screen, rather than a complete recalculation of the current
    * board state.
    */
   abstract public void doUpdate();
   /**
    * Carry out platform-specific functions related to a win of the game.
    * Examples of this would be displaying a graphic, or playing a tune.
    */
   abstract public void win();
   /**
    * Loads a board definition from a file / resource. This should include (if
    * possible) a file chooser for selection of relevant resources.
    */
   abstract public void loadBoard();
   /**
    * Saves a board definition to a file / resource. This should include (if
    * possible) a method for the user to provide a name for the board.
    */
   abstract public void saveBoard();
}
