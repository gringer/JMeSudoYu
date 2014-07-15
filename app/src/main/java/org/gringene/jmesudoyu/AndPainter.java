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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AndPainter extends SurfaceView implements Painter, Runnable {
  static int STARTX = 0;
  static int STARTY = 0;
  static int WHITE = 0x00FFFFFF;
  static int BLACK = 0x00000000;
  static int GREEN = 0x0000FF00;
  static int RED = 0x00FF0000;
  static int BLUE = 0x000000FF;
  static int DKGREY = 0x007F7F7F;
  static int GREY = 0x00B0B0B0;
  static Typeface BOARDFONT = Typeface.MONOSPACE;
  static float BFSIZE = 15;
  int displayWidth = 320;
  int displayHeight = 240;
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
  Paint brushes;
  Bitmap backBuffer;
  Context paintContext;
  Rect fontXRect;

  int old0, old1;
  GlobalVar v0, v1;
  Thread thread;
  public AndPainter(Context tContext, AttributeSet tAttributeSet) {
    super(tContext, tAttributeSet);
  }
  /**
   * Constructor for this class. Sets all fields to their default values, and
   * links the class to a org.gringene.jmesudoyu.base.Controller and a Board.
   */
  public AndPainter(Context tContext, AndController tPanel, Board tBoard) {
    super(tContext);
    paintContext = tContext;
    gamePanel = tPanel;
    gameBoard = tBoard;
    // just in case update gets called on them
    v0 = new GlobalVar();
    v1 = new GlobalVar();
    brushes.setTextSize(AndPainter.BFSIZE);
    brushes.setTypeface(AndPainter.BOARDFONT);
    brushes.getTextBounds("X",0,1,fontXRect);
  }

  public void surfaceCreated(SurfaceHolder tHolder){
    displayWidth = this.getWidth();
    displayHeight = this.getHeight();
    easel = new Canvas();
    backBuffer = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
    easel.setBitmap(backBuffer);
    brushes = new Paint();
  }


  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#getFontHeight()
   */
  public int getFontHeight() {
    return fontXRect.height();
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
      brushes.setColor(BLACK);
//         if (i == tVal) {
//            easel.setColor(RED);
//         }
      if(gameBoard.numberComplete(i)){
        brushes.setColor(GREEN);
      }
      easel.drawText(
          "" + (i + 1),
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * i * SQUAREWIDTH + TX,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * i * SQUAREHEIGHT + TY,
          brushes);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawBoard(boolean)
   */
  public void drawBoard(boolean doCands) {
    erase();
    brushes.setColor(GREY);
    for (int i = 0; i < 10; i++) {
      easel.drawLine(
          STARTX + i * SQUAREWIDTH,
          STARTY,
          STARTX + i * SQUAREWIDTH,
          STARTY + SQUAREHEIGHT * 9, brushes);
      easel.drawLine(
          STARTX,
          STARTY + i * SQUAREHEIGHT,
          STARTX + SQUAREWIDTH * 9,
          STARTY + i * SQUAREHEIGHT, brushes);
    }
    brushes.setColor(BLACK);
    for (int i = 0; i < 4; i++) {
      easel.drawLine(
          STARTX + i * SQUAREWIDTH * 3,
          STARTY,
          STARTX + i * SQUAREWIDTH * 3,
          STARTY + SQUAREHEIGHT * 9, brushes);
      easel.drawLine(
          STARTX,
          STARTY + i * SQUAREHEIGHT * 3,
          STARTX + SQUAREWIDTH * 9,
          STARTY + i * SQUAREHEIGHT * 3, brushes);
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
      brushes.setColor(GREY);
      brushes.setStyle(Paint.Style.FILL);
      easel.drawRect(
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * r * SQUAREWIDTH,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * r * SQUAREHEIGHT,
          SQUAREWIDTH,
          SQUAREHEIGHT, brushes);
      brushes.setColor(BLACK);
      brushes.setStyle(Paint.Style.STROKE);
      easel.drawRect(
          STARTX + ymul * 9 * SQUAREWIDTH + xmul * r * SQUAREWIDTH,
          STARTY + xmul * 9 * SQUAREHEIGHT + ymul * r * SQUAREHEIGHT,
          SQUAREWIDTH,
          SQUAREHEIGHT, brushes);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawSquare(int, int)
   */
  public void drawSquare(int tx, int ty) {
    brushes.setColor(BLUE);
    brushes.setStyle(Paint.Style.STROKE);
    easel.drawRect(
        STARTX + tx * SQUAREWIDTH + 1,
        STARTY + ty * SQUAREHEIGHT + 1,
        SQUAREWIDTH - 2,
        SQUAREHEIGHT - 2, brushes);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#clearPos(int, int, boolean)
   */
  public void clearPos(int tx, int ty, boolean doCands){
    brushes.setColor(WHITE);
    brushes.setStyle(Paint.Style.FILL);
    easel.drawRect(
        STARTX + tx * SQUAREWIDTH + 1,
        STARTY + ty * SQUAREHEIGHT + 1,
        SQUAREWIDTH - 1,
        SQUAREHEIGHT - 1, brushes);
    drawPos(tx,ty, doCands);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawPos(int, int, int, boolean, boolean)
   */
  public void drawPos(int pVal, int tx, int ty, boolean doNum, boolean doCands) {
    clearPos(tx,ty, doCands);
    drawSquare(tx, ty);
    if (doNum) {
      brushes.setColor(DKGREY);
      easel.drawText(
          "" + (pVal + 1),
          tx * SQUAREWIDTH + STARTX + TX,
          ty * SQUAREHEIGHT + STARTY + TY,
          brushes);
    }
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawBox(int)
   */
  public void drawBox(int boxNum) {
    int bx = boxNum % 3;
    int by = boxNum / 3;
    brushes.setColor(BLUE);
    brushes.setStyle(Paint.Style.STROKE);
    easel.drawRect(
        STARTX + bx * 3 * SQUAREWIDTH + 1,
        STARTY + by * 3 * SQUAREHEIGHT + 1,
        SQUAREWIDTH * 3 - 2,
        SQUAREHEIGHT * 3 - 2, brushes);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#clearBox(int)
   */
  public void clearBox(int boxNum) {
    int bx = boxNum % 3;
    int by = boxNum / 3;
    brushes.setColor(WHITE);
    brushes.setStyle(Paint.Style.STROKE);
    easel.drawRect(
        STARTX + bx * 3 * SQUAREWIDTH + 1,
        STARTY + by * 3 * SQUAREHEIGHT + 1,
        SQUAREWIDTH * 3 - 2,
        SQUAREHEIGHT * 3 - 2, brushes);
    brushes.setColor(GREY);
    for(int i=1; i<3; i++){
      easel.drawLine(
          STARTX + (bx * 3 + i) * SQUAREWIDTH,
          STARTY + by * 3 * SQUAREHEIGHT + 1,
          STARTX + (bx * 3 + i) * SQUAREWIDTH,
          STARTY + (by + 1) * 3 * SQUAREHEIGHT - 1, brushes);
      easel.drawLine(
          STARTX + bx * 3 * SQUAREWIDTH + 1,
          STARTY + (by * 3 + i) * SQUAREHEIGHT,
          STARTX + (bx + 1) * 3 * SQUAREWIDTH - 1,
          STARTY + (by * 3 + i) * SQUAREHEIGHT, brushes);
    }
  }
  /**
   * Clear the entire board
   */
  private void erase(){
    brushes.setColor(WHITE);
    brushes.setStyle(Paint.Style.FILL);
    easel.drawRect(0, 0, displayWidth, displayHeight, brushes);
  }
  /* (non-Javadoc)
   * @see org.gringene.jmesudoyu.base.Painter#drawPos(int, int, boolean)
   */
  public void drawPos(int tx, int ty, boolean doCands){
    Point tPoint = gameBoard.getPoint(tx, ty);
    if (tPoint.getLocked())
      brushes.setColor(BLACK);
    else{
      if (tPoint.getError())
        brushes.setColor(RED);
      else
        brushes.setColor(BLUE);
    }
    if(!doCands){
      easel.drawText(
          tPoint.toString(),
          tx * SQUAREWIDTH + STARTX + TX,
          ty * SQUAREHEIGHT + STARTY + TY,
          brushes);
    }
    else{
      if((tPoint.countBits() > 1) && (tPoint.countBits() < 9)){
        int pVal = tPoint.getValue();
        brushes.setStyle(Paint.Style.FILL);
        for(int i=0; i < 9; i++){
          if((pVal & (1 << i)) != 0){
            int px = CANDX - ((CANDWIDTH >> 1) + CANDWIDTH) - (CANDSEP << 1)
                + (tx * SQUAREWIDTH) + STARTX;
            int py = CANDY - ((CANDHEIGHT >> 1) + CANDHEIGHT) - (CANDSEP << 1)
                + (ty * SQUAREHEIGHT) + STARTY;
            easel.drawRect(
                px + (i % 3) * (CANDWIDTH + CANDSEP) + CANDSEP,
                py + (i / 3) * (CANDWIDTH + CANDSEP) + CANDSEP,
                CANDWIDTH, CANDHEIGHT, brushes);
          }
        }
      }
      else{
        easel.drawText(
            tPoint.toString(),
            tx * SQUAREWIDTH + STARTX + TX,
            ty * SQUAREHEIGHT + STARTY + TY,
            brushes);
      }
    }
  }
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run() {
    Thread myThread = Thread.currentThread();
    while (myThread == thread) {
      if(v0.notEquals(old0)){
        old0 = v0.getValue();

      }
      if(v1.notEquals(old1)){
        old1 = v1.getValue();
      }
      try{
        Thread.sleep(200);
      }
      catch(Exception e){
        System.err.println("Error: unable to sleep");
      }
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
