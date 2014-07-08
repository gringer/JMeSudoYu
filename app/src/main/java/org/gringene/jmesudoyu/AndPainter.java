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

import org.gringene.jmesudoyu.base.Board;
import org.gringene.jmesudoyu.base.GlobalVar;
import org.gringene.jmesudoyu.base.Painter;
import org.gringene.jmesudoyu.base.Point;

import android.graphics.Canvas;

public class AndPainter implements Runnable, Painter {
  static int STARTX = 0;
  static int STARTY = 0;
  static int WHITE = 0x00FFFFFF;
  static int BLACK = 0x00000000;
  static int GREEN = 0x0000FF00;
  static int RED = 0x00FF0000;
  static int BLUE = 0x000000FF;
  static int DKGREY = 0x007F7F7F;
  static int GREY = 0x00B0B0B0;
  static int BOARDFONT = Font.FACE_MONOSPACE;
  static int BFSIZE = Font.SIZE_MEDIUM;
  int SQUAREWIDTH = 14;
  int SQUAREHEIGHT = 14;
  int TX = 2;
  int TY = 2;
  int CANDWIDTH = SQUAREWIDTH / 4;
  int CANDHEIGHT = SQUAREHEIGHT / 4;
  int CANDSEP = 1;
  int CANDX = SQUAREWIDTH / 2;
  int CANDY = SQUAREHEIGHT / 2;
  boolean drawVertical;
  AndController gamePanel;
  Board gameBoard;
  Canvas easel;

  int old0, old1;
  GlobalVar v0, v1;
  Gauge g0, g1;
  Thread thread;
  /**
   * Constructor for this class. Sets all fields to their default values, and
   * links the class to a org.gringene.jmesudoyu.base.Controller and a Board.
   */
  public AndPainter(AndController tPanel, Board tBoard) {
    super();
    gamePanel = tPanel;
    gameBoard = tBoard;
    // just in case update gets called on them
    g0 = new Gauge("Nothing", false, 100, 0);
    g1 = new Gauge("Nothing", false, 100, 0);
    v0 = new GlobalVar();
    v1 = new GlobalVar();
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#getFontHeight()
   */
  public int getFontHeight() {
    return Font.getFont(BOARDFONT, Font.STYLE_BOLD, BFSIZE).getHeight();
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#setSize(int, int, int, int)
   */
  public void setSize(int tsw, int tsh, int tpx, int tpy) {
    SQUAREWIDTH = tsw;
    SQUAREHEIGHT = tsh;
    TX = tpx;
    TY = tpy;
    CANDWIDTH = SQUAREWIDTH / 4;
    CANDHEIGHT = SQUAREHEIGHT / 4;
    CANDSEP = Math.min(CANDWIDTH, CANDHEIGHT) > 1 ? 1 : 0;
    CANDX = SQUAREWIDTH / 2;
    CANDY = SQUAREHEIGHT / 2;
    easel = gamePanel.getImage();
    easel.setFont(Font.getFont(BOARDFONT, Font.STYLE_BOLD, BFSIZE));
  }
  /**
   * @param gaugeID gauge ID to modify
   * @param tGauge new Gauge to replace old version
   * @param tVar GlobalVar to link to gauge
   */
  public void setGauge(int gaugeID, Gauge tGauge, GlobalVar tVar){
    if(gaugeID == 0){
      g0 = tGauge;
      v0 = tVar;
    }
    else if (gaugeID == 1){
      g1 = tGauge;
      v1 = tVar;
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#startUpdate()
   */
  public void startUpdate(){
    old0 = 0;
    old1 = 0;
    thread = new Thread(this);
    thread.start();
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#stopUpdate()
   */
  public void stopUpdate(){
    thread = null;
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawChoice(int)
   */
  public void drawChoice(int tVal) {
    int xmul = 0;
    int ymul = 0;
    if (drawVertical)
      ymul = 1;
    else
      xmul = 1;
    for (int i = 0; i < 9; i++) {
      easel.setColor(BLACK);
//         if (i == tVal) {
//            easel.setColor(RED);
//         }
      if(gameBoard.numberComplete(i)){
        easel.setColor(GREEN);
      }
      easel.drawString(
          "" + (i + 1),
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * i * SQUAREWIDTH + TX,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * i * SQUAREHEIGHT + TY,
          Graphics.TOP | Graphics.LEFT);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawBoard(boolean)
   */
  public void drawBoard(boolean doCands) {
    erase();
    easel.setColor(GREY);
    for (int i = 0; i < 10; i++) {
      easel.drawLine(
          STARTX + i * SQUAREWIDTH,
          STARTY,
          STARTX + i * SQUAREWIDTH,
          STARTY + SQUAREHEIGHT * 9);
      easel.drawLine(
          STARTX,
          STARTY + i * SQUAREHEIGHT,
          STARTX + SQUAREWIDTH * 9,
          STARTY + i * SQUAREHEIGHT);
    }
    easel.setColor(BLACK);
    for (int i = 0; i < 4; i++) {
      easel.drawLine(
          STARTX + i * SQUAREWIDTH * 3,
          STARTY,
          STARTX + i * SQUAREWIDTH * 3,
          STARTY + SQUAREHEIGHT * 9);
      easel.drawLine(
          STARTX,
          STARTY + i * SQUAREHEIGHT * 3,
          STARTX + SQUAREWIDTH * 9,
          STARTY + i * SQUAREHEIGHT * 3);
    }
    int xmul = 0;
    int ymul = 0;
    if (drawVertical)
      ymul = 1;
    else
      xmul = 1;
    for (int r = 0; r < 9; r++) {
      for (int c = 0; c < 9; c++) {
        drawPos(c, r, doCands);
      }
      easel.setColor(GREY);
      easel.fillRect(
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * r * SQUAREWIDTH,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * r * SQUAREHEIGHT,
          SQUAREWIDTH,
          SQUAREHEIGHT);
      easel.setColor(BLACK);
      easel.drawRect(
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * r * SQUAREWIDTH,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * r * SQUAREHEIGHT,
          SQUAREWIDTH,
          SQUAREHEIGHT);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawSquare(int, int)
   */
  public void drawSquare(int tx, int ty) {
    easel.setColor(BLUE);
    easel.drawRect(
        STARTX + tx * SQUAREWIDTH + 1,
        STARTY + ty * SQUAREHEIGHT + 1,
        SQUAREWIDTH - 2,
        SQUAREHEIGHT - 2);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#clearPos(int, int, boolean)
   */
  public void clearPos(int tx, int ty, boolean doCands){
    easel.setColor(WHITE);
    easel.fillRect(
        STARTX + tx * SQUAREWIDTH + 1,
        STARTY + ty * SQUAREHEIGHT + 1,
        SQUAREWIDTH - 1,
        SQUAREHEIGHT - 1);
    drawPos(tx,ty, doCands);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawPos(int, int, int, boolean, boolean)
   */
  public void drawPos(int pVal, int tx, int ty, boolean doNum, boolean doCands) {
    clearPos(tx,ty, doCands);
    drawSquare(tx, ty);
    if (doNum) {
      easel.setColor(DKGREY);
      easel.drawString(
          "" + (pVal + 1),
          tx * SQUAREWIDTH + STARTX + TX,
          ty * SQUAREHEIGHT + STARTY + TY,
          Graphics.TOP | Graphics.LEFT);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawBox(int)
   */
  public void drawBox(int boxNum) {
    int bx = boxNum % 3;
    int by = boxNum / 3;
    easel.setColor(BLUE);
    easel.drawRect(
        STARTX + bx * 3 * SQUAREWIDTH + 1,
        STARTY + by * 3 * SQUAREHEIGHT + 1,
        SQUAREWIDTH * 3 - 2,
        SQUAREHEIGHT * 3 - 2);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#clearBox(int)
   */
  public void clearBox(int boxNum) {
    int bx = boxNum % 3;
    int by = boxNum / 3;
    easel.setColor(WHITE);
    easel.drawRect(
        STARTX + bx * 3 * SQUAREWIDTH + 1,
        STARTY + by * 3 * SQUAREHEIGHT + 1,
        SQUAREWIDTH * 3 - 2,
        SQUAREHEIGHT * 3 - 2);
    easel.setColor(GREY);
    for(int i=1; i<3; i++){
      easel.drawLine(
          STARTX + (bx * 3 + i) * SQUAREWIDTH,
          STARTY + by * 3 * SQUAREHEIGHT + 1,
          STARTX + (bx * 3 + i) * SQUAREWIDTH,
          STARTY + (by + 1) * 3 * SQUAREHEIGHT - 1);
      easel.drawLine(
          STARTX + bx * 3 * SQUAREWIDTH + 1,
          STARTY + (by * 3 + i) * SQUAREHEIGHT,
          STARTX + (bx + 1) * 3 * SQUAREWIDTH - 1,
          STARTY + (by * 3 + i) * SQUAREHEIGHT);
    }
  }
  /**
   * Clear the entire board
   */
  private void erase(){
    easel.setColor(WHITE);
    easel.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawPos(int, int, boolean)
   */
  public void drawPos(int tx, int ty, boolean doCands){
    Point tPoint = gameBoard.getPoint(tx, ty);
    if (tPoint.getLocked())
      easel.setColor(BLACK);
    else{
      if (tPoint.getError())
        easel.setColor(RED);
      else
        easel.setColor(BLUE);
    }
    if(!doCands){
      easel.drawString(
          tPoint.toString(),
          tx * SQUAREWIDTH + STARTX + TX,
          ty * SQUAREHEIGHT + STARTY + TY,
          Graphics.TOP | Graphics.LEFT);
    }
    else{
      if((tPoint.countBits() > 1) && (tPoint.countBits() < 9)){
        int pVal = tPoint.getValue();
        for(int i=0; i < 9; i++){
          if((pVal & (1 << i)) != 0){
            int px = CANDX - ((CANDWIDTH >> 1) + CANDWIDTH) - (CANDSEP << 1)
                + (tx * SQUAREWIDTH) + STARTX;
            int py = CANDY - ((CANDHEIGHT >> 1) + CANDHEIGHT) - (CANDSEP << 1)
                + (ty * SQUAREHEIGHT) + STARTY;
            easel.fillRect(
                px + (i % 3) * (CANDWIDTH + CANDSEP) + CANDSEP,
                py + (i / 3) * (CANDWIDTH + CANDSEP) + CANDSEP,
                CANDWIDTH, CANDHEIGHT);
          }
        }
      }
      else{
        easel.drawString(
            tPoint.toString(),
            tx * SQUAREWIDTH + STARTX + TX,
            ty * SQUAREHEIGHT + STARTY + TY,
            Graphics.TOP | Graphics.LEFT);
      }
    }
  }
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run() {
    Thread mythread = Thread.currentThread();
    while (mythread == thread) {
      if(v0.notEquals(old0)){
        old0 = v0.getValue();
        g0.setValue(old0);

      }
      if(v1.notEquals(old1)){
        old1 = v1.getValue();
        g1.setValue(old1);
      }
      try{
        Thread.sleep(200);
      }
      catch(Exception e){}
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#pos2cellX(int)
   */
  public int pos2cellX(int tx){
    return tx / SQUAREWIDTH;
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#pos2cellY(int)
   */
  public int pos2cellY(int ty){
    return ty / SQUAREHEIGHT;
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#getVertical()
   */
  public boolean getVertical(){
    return drawVertical;
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#setVertical(boolean)
   */
  public void setVertical(boolean tVert){
    drawVertical = tVert;
  }
}
