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

import android.media.Image;
import android.support.v7.app.ActionBarActivity;

import org.gringene.jmesudoyu.base.Board;
import org.gringene.jmesudoyu.base.Commander;
import org.gringene.jmesudoyu.base.Controller;
import org.gringene.jmesudoyu.base.GlobalVar;
import org.gringene.jmesudoyu.base.SaveResource;

import java.io.*;

public class AndController implements Controller {
  private static byte[] SAVEVERSION = {1, 0};
  Board gameBoard;
  Commander gameCommand;
  AndPainter gamePainter;
  Thread thread;
  int w, h;
  boolean makePuzzle;
  public AndController(Board tBoard) {
    super();
    gameBoard = tBoard;
    gamePainter = new AndPainter(this, gameBoard);
    w = Math.min(tPainter.getWidth(), tPainter.getHeight());
    h = Math.min(tPainter.getWidth(), tPainter.getHeight());
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

  protected void keyPressed(int keyCode) {
    int GAKey = this.getGameAction(keyCode);
    if (gameCommand.numChange(keyCode - Canvas.KEY_NUM0));
    else if (GAKey == Canvas.FIRE)
      gameCommand.doButton(Controller.RIGHT);
    else if (GAKey == Canvas.LEFT)
      gameCommand.changePos(-1, 0);
    else if (GAKey == Canvas.RIGHT)
      gameCommand.changePos(1, 0);
    else if (GAKey == Canvas.UP)
      gameCommand.changePos(0, -1);
    else if (GAKey == Canvas.DOWN)
      gameCommand.changePos(0, 1);
    else if (keyCode == Canvas.KEY_STAR)
      gameCommand.doButton(Controller.RIGHT);
    else if (keyCode == Canvas.KEY_POUND)
      gameCommand.doCommand("Create");
  }
  protected void pointerPressed(int tx, int ty) {
    gameCommand.doPointerPress(tx,ty);
  }
  protected void pointerDragged(int tx, int ty) {}
  protected void pointerReleased(int tx, int ty) {}
  public void commandAction(Command c, Displayable s) {
    String actionText = c.getLabel();
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
    sDisplay.setCurrent((Displayable) null);
  }
  public void recoverDisplay(){
    sDisplay.setCurrent(this);
  }
  public void doUpdate(){
    this.repaint();
  }
  public void paint(Graphics g) {
    g.drawImage(sudImage, 0, 0, Graphics.LEFT | Graphics.TOP);
  }
  public void quit(){
    saveBoard();
    sMIDlet.notifyDestroyed();
  }
  public void saveOptions() {
    System.out.println("Saving legacy format Board");
    try {
      RecordStore.deleteRecordStore("sud_state");
    }
    catch (Exception e) {}
    try {
      sudRStore = RecordStore.openRecordStore("sud_state", true);
    }
    catch (Exception e) {
      alertMsg("Could not open record store: " + e.getMessage());
    }
    int[] tBoard = new int[81];
    gameBoard.staticSave(tBoard);
    byte[] tRec = new byte[162];
    for (int i = 0; i < 81; i++) {
      tRec[i << 1] = (byte) ((tBoard[i] >> 7) & 127);
      tRec[(i << 1) + 1] = (byte) (tBoard[i] & 127);
    }
    try {
      sudRStore.setRecord(1, tRec, 0, tRec.length);
    }
    catch (InvalidRecordIDException ridex) {
      // Records did not exist, create a new record
      try {
        sudRStore.addRecord(tRec, 0, tRec.length);
      }
      catch (RecordStoreException e) {
        alertMsg("Could not add board record");
      }
    }
    catch (Exception e) {
      alertMsg("Could not save board: " + e.getMessage());
    }
    tRec = new byte[81];
    gameBoard.saveFlags(tRec);
    try {
      sudRStore.setRecord(2, tRec, 0, tRec.length);
      sudRStore.closeRecordStore();
    }
    catch (InvalidRecordIDException ridex) {
      // Records did not exist, create a new record
      try {
        sudRStore.addRecord(tRec, 0, tRec.length);
        sudRStore.closeRecordStore();
      }
      catch (RecordStoreException e) {
        alertMsg("Could not add flag record");
      }
    }
    catch (Exception e) {
      alertMsg("Could not save flags: " + e.getMessage());
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Controller#loadBoard()
   */
  public void loadBoard() {
    int firstRecordSize = 0;
    System.out.println("Attempting to load from record store");
    try {
      sudRStore = RecordStore.openRecordStore("sud_state", true);
      firstRecordSize = sudRStore.getRecordSize(1);
    }
    catch (RecordStoreException e) {
      System.out.println("Could not open Record Store: " + e.getMessage());
      sudRStore = null;
    }
    if (sudRStore != null) {
      if(firstRecordSize != 2){
        // We assume an older version of the save format, so try the legacy loading
        try{
          sudRStore.closeRecordStore();
        } catch (Exception e) {}
        legacyLoadBoard();
      }
      else{
        int numRecords = 0;
        int saveVersion = 0;
        SaveResource tmpSR = new SaveResource();
        byte[] tmpResult = null;
        try{
          numRecords = sudRStore.getNumRecords();
          saveVersion = sudRStore.getRecord(1)[0];
        } catch (Exception e){
          saveVersion = 0;
        }
        System.out.println("Save version appears to be "+saveVersion);
        if(saveVersion == 1){
          // The assumed record format is as follows
          // Record 1: record Version number (1 byte)
          // Record n: org.gringene.jmesudoyu.base.SaveResource ID + data (org.gringene.jmesudoyu.base.SaveResource.IDLENGTH + arbitrary bytes)
          // note: the RecordStore ID numbers are 1-based
          for(int i = 2; i < (numRecords+1); i++ ){
            try{
              System.out.println("Loading up record #"+i);
              tmpResult = sudRStore.getRecord(i);
              tmpResult = tmpSR.loadData(tmpResult);
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
        try{
          sudRStore.closeRecordStore();
        } catch (Exception e){}
      }
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Controller#saveBoard()
   */
  public void saveBoard() {
    System.out.println("Attempting to save to record store");
    // I want everyone to upgrade to the new format, so just hose the current record
    // store (this is the way it was done in previous versions of AndMeSudoYu, so this
    // is consistent with how it worked previously).
    // A more intelligent way would enable forward compatibility, keeping the store as
    // it is and just updating things that have the same Resource IDs as those already
    // known... although why someone would want to downgrade is beyond my understanding.
    // However, this alternative approach may have merit in reducing processor stress --
    // overwriting records should be less intensive than clearing everything
    // and starting over.
    try {
      RecordStore.deleteRecordStore("sud_state");
    }
    catch (Exception e) {}
    try {
      sudRStore = RecordStore.openRecordStore("sud_state", true);
    }
    catch (Exception e) {
      alertMsg("Could not open record store: " + e.getMessage());
    }
    this.rsSave(AndController.SAVEVERSION);
    SaveResource tmpSR = new SaveResource();
    tmpSR.setID(SaveResource.BOARDDATA); tmpSR.setData(gameBoard);
    this.rsSave(tmpSR.saveData());
    String[] settingsKeys = new String[Commander.SETTINGSSIZE];
    int[] settingsValues = new int[Commander.SETTINGSSIZE];
    gameCommand.saveSettings(settingsKeys, settingsValues);
    tmpSR.setID(SaveResource.GAMESETTINGS); tmpSR.setData(settingsKeys, settingsValues);
    this.rsSave(tmpSR.saveData());
    try{
      sudRStore.closeRecordStore();
    } catch (Exception e){}
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
  private void rsSave(byte[] data){
    int tmpNum = 0;
    if(data != null){
      try{
        tmpNum = sudRStore.addRecord(data, 0, data.length);
        System.out.println("Stored data in record # "+ tmpNum);
      } catch (Exception e){
        System.out.println("Unable to store data in record # "+ tmpNum +
            ": " + e.getMessage());
      }
    }
  }
  /**
   * The older load method -- from AndMeSudoYu versions 1.60 and below
   */
  private void legacyLoadBoard() {
    System.out.println("Loading legacy format board");
    int[] tBoard = new int[81];
    try {
      sudRStore = RecordStore.openRecordStore("sud_state", true);
    }
    catch (RecordStoreException e) {
      System.out.println("Could not open Record Store: " + e.getMessage());
      sudRStore = null;
    }
    if (sudRStore != null) {
      byte[] tRec;
      try {
        tRec = sudRStore.getRecord(1);
      }
      catch (Exception e) {
        System.out.println("Could not load board: " + e.toString());
        tRec = null;
      }
      if (tRec != null && (tRec.length == 162)) {
        for (int i = 0; i < 81; i++) {
          tBoard[i] = (tRec[i << 1] << 7) | tRec[(i << 1) + 1];
          if (tBoard[i] < 0)
            tBoard[i] = 511;
        }
        gameBoard.staticLoad(tBoard);
        //tRec = sudRStore.getRecord(REC_OPTIONS);
        //pInput.setExpert(tRec[0] == 0);
      }
      try {
        tRec = sudRStore.getRecord(2);
        sudRStore.closeRecordStore();
      }
      catch (Exception e) {
        System.out.println("Could not load board: " + e.toString());
        tRec = null;
      }
      if (tRec != null && (tRec.length == 81)) {
        gameBoard.loadFlags(tRec);
      }
    }
  }
  public void makeProgress(String title, String[] labels, int[] limits, GlobalVar[] values, int cancelOptions){
    Form f = new Form(title);
    Command temp;
    if((cancelOptions & Controller.OP_CANCEL) != 0){
      temp = new Command("Cancel", Command.CANCEL, 1);
      f.addCommand(temp);
      f.setCommandListener(this);
    }
    if((cancelOptions & Controller.OP_ACCEPT) != 0){
      temp = new Command("Accept", Command.OK, 1);
      f.addCommand(temp);
      f.setCommandListener(this);
    }
    int num = labels.length;
    Gauge[] gauges = new Gauge[num];
    for(int i=0; i<num; i++){
      gauges[i] = new Gauge(labels[i], false, limits[i], 0);
      f.append(gauges[i]);
      values[i].setValue(0);
      gamePainter.setGauge(i, gauges[i], values[i]);
    }
    sDisplay.setCurrent(f);
  }

  @Override
  public int getWidth() {
    return 0;
  }

  @Override
  public int getHeight() {
    return 0;
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
    Alert al = new Alert(title);
    al.setTimeout(Alert.FOREVER);
    al.setString(msg);
    sDisplay.setCurrent(al);
  }
  // alert with timeout, logs to System.out
   /* (non-Javadoc)
    * @see org.gringene.jmesudoyu.base.Controller#alertMsg(java.lang.String, java.lang.String)
    */
  public void alertMsg(String title, String msg) {
    Alert al = new Alert(title);
    al.setString(msg);
    sDisplay.setCurrent(al);
    System.out.println(msg);
  }
  public void alertMsg(String msg) {
    alertMsg("Alert", msg);
  }
  public void win(){
    try
    {
      InputStream is = getClass().getResourceAsStream("/v.imy");
      Player m_player = Manager.createPlayer(is, "audio/imelody");
      m_player.prefetch();
      m_player.start();
    }
    catch (Exception ex)
    {
      System.out.println(ex.toString());
    }
  }
}
