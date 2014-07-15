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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import org.gringene.jmesudoyu.base.Board;
import org.gringene.jmesudoyu.base.Commander;
import org.gringene.jmesudoyu.base.Controller;
import org.gringene.jmesudoyu.base.GlobalVar;
import org.gringene.jmesudoyu.base.SaveResource;

import java.io.*;

public class AndController implements Controller, DialogInterface.OnClickListener {
  private static byte[] SAVEVERSION = {2, 0}; // version 2 is the first Android version
  Board gameBoard;
  Commander gameCommand;
  AndPainter gamePainter;
  Activity gameActivity;
  Thread thread;
  int w, h;
  boolean makePuzzle;
  public AndController(Activity tActivity, Board tBoard) {
    super();
    gameBoard = tBoard;
    gameActivity = tActivity;
    gamePainter = new AndPainter(gameActivity, this, gameBoard);
    w = Math.min(gamePainter.getWidth(), gamePainter.getHeight());
    h = Math.min(gamePainter.getWidth(), gamePainter.getHeight());
    //System.out.println("screen is " + w + "x" + h);
    int fh = gamePainter.getFontHeight();
    gamePainter.setVertical(gamePainter.getWidth() > gamePainter.getHeight());
    gamePainter.setSize(
        w / 9,
        h / 9,
        w / 18 - fh / 4 + 1,
        h / 18 - fh / 2 + 1);
    gameCommand = new Commander(this, gameBoard, gamePainter);
  }

  protected void keyRepeated(int keyCode) {}

  protected void keyPressed(int GAKey) {
    if (gameCommand.numChange(GAKey - KeyEvent.KEYCODE_NUMPAD_0) ||
        gameCommand.numChange(GAKey - KeyEvent.KEYCODE_0));
    else if (GAKey == KeyEvent.KEYCODE_DPAD_CENTER)
      gameCommand.doButton(Controller.RIGHT);
    else if ((GAKey == KeyEvent.KEYCODE_SOFT_LEFT) || (GAKey == KeyEvent.KEYCODE_DPAD_LEFT))
      gameCommand.changePos(-1, 0);
    else if ((GAKey == KeyEvent.KEYCODE_SOFT_RIGHT) || (GAKey == KeyEvent.KEYCODE_DPAD_RIGHT))
      gameCommand.changePos(1, 0);
    else if ((GAKey == KeyEvent.KEYCODE_VOLUME_UP) || (GAKey == KeyEvent.KEYCODE_DPAD_UP))
      gameCommand.changePos(0, -1);
    else if ((GAKey == KeyEvent.KEYCODE_VOLUME_DOWN) || (GAKey == KeyEvent.KEYCODE_DPAD_DOWN))
      gameCommand.changePos(0, 1);
    else if (GAKey == KeyEvent.KEYCODE_STAR)
      gameCommand.doButton(Controller.RIGHT);
    else if (GAKey == KeyEvent.KEYCODE_POUND)
      gameCommand.doCommand("Create");
  }
  protected void pointerPressed(int tx, int ty) {
    gameCommand.doPointerPress(tx,ty);
  }
  protected void pointerDragged(int tx, int ty) {}
  protected void pointerReleased(int tx, int ty) {}
  public void commandAction(String actionText) {
    boolean numResult;
    try {
      numResult = gameCommand.numChange(Integer.parseInt(actionText));
    }
    catch (NumberFormatException e) {
      numResult = false;
    }
    if (!numResult)
      gameCommand.doCommand(actionText);
  }
  public void init() {
    loadBoard();
    gameCommand.init();
  }
  public void pause() throws InterruptedException {
    saveBoard();
  }
  public void resume() {
  }
  public void destroy(boolean unconditional) {
    saveBoard();
    if (unconditional) {
      System.out.println("Quitting due to a command, not a request");
    }
    gameActivity.finish();
  }
  public void quit(){
    saveBoard();
    gameActivity.finish();
  }

  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Controller#loadBoard()
   */
  public void loadBoard() {
    int firstRecordSize = 0;
    System.out.println("Attempting to load from record store");
    SharedPreferences prefStore = gameActivity.getPreferences(Context.MODE_PRIVATE);
    int numRecords = prefStore.getInt("saveCount",0);
    int saveVersion = prefStore.getInt("saveVersion",0);
    SaveResource tmpSR = new SaveResource();
    byte[] tmpResult = null;
    System.out.println("Save version appears to be "+saveVersion);
    if(saveVersion == 2){
      // The assumed record format is as follows
      // Preference setting 'saveVersion': record Version number (int)
      // Record n: org.gringene.jmesudoyu.base.SaveResource ID + data (org.gringene.jmesudoyu.base.SaveResource.IDLENGTH + arbitrary bytes)
      for(int i = 0; i < numRecords; i++ ){
        try{
          System.out.println("Loading up record #"+i);
          tmpResult = null;
          // get the record length (first 2 bytes of each record)
          int recordLength = prefStore.getInt(String.format("recordSize_%03d",i), -1);
          if(recordLength > 0){
            tmpResult = new byte[recordLength];
            FileInputStream fin = gameActivity.openFileInput(String.format("sudRStore_%03d",i));
            int bytesRead = fin.read(tmpResult);
            if(bytesRead != tmpResult.length){
              System.out.printf("Warning: record #%d was smaller than expected",i);
            }
            tmpResult = tmpSR.loadData(tmpResult);
            fin.close();
          }
        } catch (Exception e){
          System.out.println("Error loading resource " + i);
        }
        if((tmpResult != null) && (tmpResult.length > 0)){
          System.out.println("Warning: " + tmpResult.length +
              " bytes left over from resource " + i);
        }
        if(tmpSR.getIDInt() == SaveResource.BOARDDATA){
          System.out.println("Loading game board");
          tmpSR.getData(gameBoard);
        }
        if(tmpSR.getIDInt() == SaveResource.GAMESETTINGS){
          System.out.println("Loading game settings");
          String[] settingsKeys = new String[Commander.SETTINGSSIZE];
          int[] settingsValues = new int[Commander.SETTINGSSIZE];
          tmpSR.getData(settingsKeys, settingsValues);
          gameCommand.loadSettings(settingsKeys, settingsValues);
        }
      }
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Controller#saveBoard()
   */
  public void saveBoard() {
    SharedPreferences prefStore = gameActivity.getPreferences(Context.MODE_PRIVATE);
    prefStore.edit().putInt("saveVersion",AndController.SAVEVERSION[0] + AndController.SAVEVERSION[1] << 8);
    SaveResource tmpSR = new SaveResource();
    tmpSR.setID(SaveResource.BOARDDATA); tmpSR.setData(gameBoard);
    this.rsSave(0,tmpSR.saveData());
    String[] settingsKeys = new String[Commander.SETTINGSSIZE];
    int[] settingsValues = new int[Commander.SETTINGSSIZE];
    gameCommand.saveSettings(settingsKeys, settingsValues);
    tmpSR.setID(SaveResource.GAMESETTINGS); tmpSR.setData(settingsKeys, settingsValues);
    this.rsSave(1,tmpSR.saveData());
    prefStore.edit().putInt("saveCount",2);
  }
  /**
   * Function to store a byte array into a particular (opened) record store.
   * This method is set up to reduce the mess of exceptions and repeated code.
   *
   * @param tmpNum
   *            Record number to store data in
   * @param data
   *            data to save into RecordStore
   */
  private void rsSave(int tmpNum, byte[] data){
    if(data != null){
      try{
        FileOutputStream fos = gameActivity.openFileOutput(String.format("sudRStore_%03d",tmpNum),Context.MODE_PRIVATE);
        fos.write(data);
        fos.close();
        System.out.println("Stored data in record # "+ tmpNum);
      } catch (Exception e){
        System.out.println("Unable to store data in record # "+ tmpNum +
            ": " + e.getMessage());
      }
    }
  }
  public void makeProgress(String title, String[] labels, int[] limits, GlobalVar[] values, int cancelOptions){
    ProgressDialog p = new ProgressDialog(gameActivity);
    p.setTitle(title);
    if((cancelOptions & Controller.OP_CANCEL) != 0){
      p.setButton(DialogInterface.BUTTON_NEGATIVE,gameActivity.getResources().getText(R.string.button_cancel),this);
    }
    if((cancelOptions & Controller.OP_ACCEPT) != 0){
      p.setButton(DialogInterface.BUTTON_NEGATIVE,gameActivity.getResources().getText(R.string.button_accept),this);
    }
    int num = labels.length;
    if(num < 1){
      return;
    }
    p.setMax(limits[0]);
    p.setMessage(labels[0]);
    p.setIndeterminate(false);
    //NOTE: progress indicators beyond the first are ignored. While a secondary indicator
    //      can be used, it must have the same limit as the first progress bar
  }

  @Override
  public int getWidth() {
    return 0;
  }

  @Override
  public int getHeight() {
    return 0;
  }

  @Override
  public void recoverDisplay() {
  }

  @Override
  public void doUpdate() {
  }

  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Controller#helpMessage()
   */
  public void helpMessage(){
    this.infoMsg(
        "About AndMeSudoYu",
        "Sudoku is a game involving a 9x9 grid of numbers, "
            + "where each 3x3 box, as well as each row and each column, "
            + "must contain all the numbers 1 to 9.\n"
            + "\n"
            + "This program generates sudoku puzzles, and provides "
            + "an interface to complete the puzzles. The active cell "
            + "is changed by directional keys, numbers are placed "
            + "when a number is pressed on the keypad, the "
            + "current cell is cleared by either the select key or "
            + "the '*' key, and '#' generates a puzzle.\n"
            + "\n"
            + "Any comments/suggestions, please contact me: "
            + "jmesudoyu@gringer.org\n\n");
  }
  public void keysMessage(){
    this.infoMsg(
        "Keypad Control",
        "The game can also be played entirely on the keypad. "
            + "Keys are as follows:\n\n"
            + "0 switches the keypad between move and expert mode. "
            + "\n"
            + "The expert mode (toggled via menus, or by pressing 0), "
            + "allows numbers to be placed anywhere on the board by "
            + "pressing three of the keypad buttons. The first press "
            + "selects the box, the second press selects the cell "
            + "inside the box, and the last press sets the number at "
            + "that cell (cancel by pressing 0).\n"
            + "\n"
            + "The underlying logic of each cell is also displayed "
            + "when there are at least two candidate numbers. Each "
            + "block in the 3x3 grid indicates a possible candidate.");
  }
  // alert with no timeout, no System.out printing
   /* (non-Javadoc)
    * @see org.gringene.jmesudoyu.base.Controller#infoMsg(java.lang.String, java.lang.String)
    */
  public void infoMsg(String title, String msg) {
    AlertDialog.Builder builder = new AlertDialog.Builder(gameActivity);
    builder
        .setMessage(msg)
        .setTitle(title).setPositiveButton(R.string.ok,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which){}})
        .setIcon(R.drawable.ic_action_about);
    builder.create().show();
  }
  // alert with timeout, logs to System.out
   /* (non-Javadoc)
    * @see org.gringene.jmesudoyu.base.Controller#alertMsg(java.lang.String, java.lang.String)
    */
  public void alertMsg(String title, String msg) {
    // title could probably be ignored
    Toast.makeText(gameActivity, String.format("%s -- %s", title, msg), Toast.LENGTH_LONG);
    System.out.println(msg);
  }
  public void alertMsg(String msg) {
    alertMsg("Alert", msg);
  }

  public void win(){
    // vibrating is surprisingly easier in Android than in j2me
    long[] vibratePattern = {500,200,500,800};
    Vibrator v = (Vibrator)gameActivity.getSystemService(Context.VIBRATOR_SERVICE);
    v.vibrate(vibratePattern,2);
  }

  @Override
  public void onClick(DialogInterface dialogInterface, int i) {

  }
}
