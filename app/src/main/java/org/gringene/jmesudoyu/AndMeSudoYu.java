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
package org.gringene.jmesudoyu;

import android.support.v7.app.ActionBarActivity;

import org.gringene.jmesudoyu.base.Board;

/**
 * The main class of the Android port of this program, and the base from which
 * other versions of this program should be derived. If any new functions are
 * added, please take the small amount of time require to write short JavaDoc
 * notes on the function. A mistake in not doing this was made early on, and
 * trying to re-learn code and attempting to work out the intention of functions
 * took a long time.
 * 
 * @author gringer
 */
public class AndMeSudoYu extends ActionBarActivity {
   private AndController pInput;
   private Board gameBoard;
   Display display;
   boolean paused;
   /**
    * Sets up the j2me application
    */
   public AndMeSudoYu() {
      paused = false;
      display = Display.getDisplay(this);
      gameBoard = new Board();
      pInput = new AndController(gameBoard, this);
      display.setCurrent(pInput);
      pInput.init();
      pInput.setCommandListener(pInput);
   }
   /* (non-Javadoc)
    * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
    */
   public void destroyApp(boolean unconditional) {
      if (pInput != null) {
         pInput.destroy(unconditional);
      }
      display.setCurrent((Displayable) null);
   }
   /* (non-Javadoc)
    * @see android.support.v7.app.ActionBarActivity#onStop()
    */
   protected void onStop() {
      if (pInput != null) {
         paused = true;
         try {
            pInput.pause();
         }
         catch (Exception e) {
           System.err.println("Unable to stop the application");
         }
      }
   }
  /* (non-Javadoc)
   * @see android.support.v7.app.ActionBarActivity#onPostResume()
   */
   protected void onRestart() {
      display.setCurrent(pInput);
      if(paused){
         paused = false;
         pInput.resume();
      }      
   }
}
