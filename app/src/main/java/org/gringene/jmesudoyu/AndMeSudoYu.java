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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
   boolean paused;

  @Override
   public void onCreate(Bundle savedInstance) {
     super.onCreate(savedInstance);
    setContentView(R.layout.activity_my);
      paused = false;
      gameBoard = new Board();
      pInput = new AndController(gameBoard, this);
      pInput.init();
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
      if(paused){
         paused = false;
         pInput.resume();
      }      
   }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.my, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    return ((id == R.id.action_settings) || super.onOptionsItemSelected(item));
  }
}
