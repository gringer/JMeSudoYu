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
 * Class that describes / stores different data. This is primarily used for the
 * resources in j2me, but could also be used to save state for other versions.
 * The code has lots of error checking in it because it's not likely to be run
 * often, and it would be nice to be able to handle some kinds of data corruption.
 * 
 * @author gringer
 * 
 */
public class SaveResource {
    private static int IDLENGTH = 2;

    public static int NA = 0;

    public static int BOARDDATA = 1;

    public static int GAMESETTINGS = 2;

    public static int SAVEGAME = 3;

    public static int MAXID = SaveResource.SAVEGAME;

    int resourceID;

    byte[] resourceData;

    public SaveResource() {
        resourceID = SaveResource.NA;
        resourceData = null;
    }

    /**
     * Retrieves the resource ID as a two byte array.
     * @return A two-byte version of the resource ID
     */
    public byte[] getID() {
        byte[] result = new byte[SaveResource.IDLENGTH];
        result[0] = (byte) ((resourceID & 0xff00) >>> 8);
        result[1] = (byte) ((resourceID & 0x00ff));
        return (result);
    }

    /**
     * Retrieves the resource ID for this org.gringene.jmesudoyu.base.SaveResource object.
     * @return the resourceID of this resource
     */
    public int getIDInt() {
        return this.resourceID;
    }

    /**
     * Sets the resource ID, using input from a two byte array.
     * 
     * @param tResource
     *            Two-byte array containing the intended resource ID.
     * @return true if the resourceID was successfully set
     */
    public boolean setID(byte[] tResource) {
        boolean retVal = false;
        if (tResource.length != SaveResource.IDLENGTH) {
            this.resourceID = SaveResource.NA;
            this.resourceData = null;
        } else {
            this.resourceID = ((tResource[0] & 0xff) << 8)
                    | ((tResource[1] & 0xff));
            retVal = true;
        }
        return retVal;
    }

    /**
     * Sets the resource ID. The highest 16 bits are ignored, as the resource ID
     * should fit into a 2-byte array. This method clears the current data
     * stored in the org.gringene.jmesudoyu.base.SaveResource.
     * 
     * @param tResource
     *            integer containing the intended resource ID.
     */
    public void setID(int tResource) {
        this.resourceID = tResource & 0xffff;
        this.resourceData = null;
    }

    /**
     * Sets the resource data to a set of key/value pairs. Keys should be
     * strings (with a maximum length 100 characters &mdash; anything beyond
     * this will be dropped), while values are stored as ints. The store is
     * currently limited to a maximum of 1000 settings.
     * 
     * @param keys Set of keys to place in org.gringene.jmesudoyu.base.SaveResource
     * @param values Corresponding set of values to place in org.gringene.jmesudoyu.base.SaveResource
     * @return true if the resource data was entered successfully.
     */
    public boolean setData(String[] keys, int[] values) {
        byte[] keyBytes;
        int totalLength = 2; // array size: 2 bytes
        boolean retVal = ((keys.length > 0) && (keys.length <= 1000) && 
                (keys.length == values.length));
        if (this.resourceID != SaveResource.GAMESETTINGS) {
            retVal = false;
        }
        if (retVal) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].length() > 100) {
                    keys[i] = keys[i].substring(0, 100);
                }
                if (keys[i].length() == keys[i].getBytes().length) {
                    // length of string: 1 byte
                    // key: 1 byte per character (assured by above check)
                    // value: 4 bytes
                    totalLength = totalLength + 1 + keys[i].length() + 4;
                } else {
                    retVal = false;
                }
            }
        }
        if (retVal) {
            this.resourceData = new byte[totalLength];
            int currentPos = 0;
            resourceData[currentPos++] = (byte) ((keys.length & 0xff00) >>> 8);
            resourceData[currentPos++] = (byte) ((keys.length & 0x00ff) >>> 0);
            for (int i = 0; i < keys.length; i++) {
                keyBytes = keys[i].getBytes();
                resourceData[currentPos++] = (byte) keyBytes.length;
                for (int j = 0; j < keyBytes.length; j++) {
                    resourceData[currentPos++] = keyBytes[j];
                }
                resourceData[currentPos++] = (byte) ((values[i] & 0xff000000) >>> 24);
                resourceData[currentPos++] = (byte) ((values[i] & 0x00ff0000) >>> 16);
                resourceData[currentPos++] = (byte) ((values[i] & 0x0000ff00) >>> 8);
                resourceData[currentPos++] = (byte) ((values[i] & 0x000000ff) >>> 0);
            }
            if (currentPos != totalLength) {
                this.resourceData = null;
                retVal = false;
            }
        }
        return retVal;
    }

    /**
     * Places the resource data into a given set of keys / values. It's a good
     * idea to be a bit creative here, because the calling class may not
     * necessarily know in advance how many keys are going to be used. The
     * current behaviour is to store the String &quot;&lt;FINISH&gt;&quot; in
     * the key <em>after</em> the last key/value pair to indicate the end. If
     * there are fewer key/value pair slots available than necessary, then only
     * that many will have entries. Another potential option [not currently
     * implemented] is having a getNumRecords() function that returns the number
     * of key / value pairs.
     * 
     * @param keys
     *            Set of keys to put org.gringene.jmesudoyu.base.SaveResource into
     * @param values
     *            Corresponding set of values to put org.gringene.jmesudoyu.base.SaveResource into
     * @return true if the resource data was extracted successfully
     */
    public boolean getData(String[] keys, int[] values) {
        boolean retVal = true;
        char[] keyChars;
        if (this.resourceID != SaveResource.GAMESETTINGS) {
            retVal = false;
        }
        if (retVal) {
            int keysLength = ((this.resourceData[0] & 0xff) << 8)
                    | ((this.resourceData[1] & 0xff) << 0);
            keysLength = Math.min(Math.min(keys.length, values.length),
                    keysLength);
            int curPos = 2; // start after the key length bit
            for (int keyNum = 0; keyNum < keysLength; keyNum++) {
                if (this.resourceData[curPos] <= 100) {
                    keyChars = new char[resourceData[curPos]];
                    keys[keyNum] = "";
                    for (int i = 0; i < keyChars.length; i++) {
                        // could be dataloss if the keys are not ASCII
                        keyChars[i] = (char) this.resourceData[curPos + 1 + i];
                    }
                    keys[keyNum] = String.valueOf(keyChars);
                    curPos = curPos + 1 + keyChars.length;
                    values[keyNum] = ((this.resourceData[curPos + 0] & 0xff) << 24)
                            | ((this.resourceData[curPos + 1] & 0xff) << 16)
                            | ((this.resourceData[curPos + 2] & 0xff) << 8)
                            | ((this.resourceData[curPos + 3] & 0xff) << 0);
                    curPos = curPos + 4;
                }
            }
            if(curPos > 2){
                if(keys.length > keysLength){
                    keys[keysLength] = "<FINISH>";
                }
            }
            else {
                retVal = false;
            }
        }
        return retVal;
    }
    /**
     * Loads up a given game board into the resource data. This stores the org.gringene.jmesudoyu.base.Point
     * representation of the data as well as the flags associated with the
     * Points.
     * 
     * @param tBoard
     *            Board to extract org.gringene.jmesudoyu.base.Point information from
     * @return true if the resource data was entered successfully
     */
    public boolean setData(Board tBoard) {
        boolean retVal = true;
        this.resourceData = new byte[1 + Point.MAX * Point.MAX * 3];
        this.resourceData[0] = (byte) Point.MAX;
        byte[] boardData = new byte[Point.MAX * Point.MAX * 3];
        tBoard.staticSave(boardData);
        for (int i = 0; i < boardData.length; i++) {
            this.resourceData[1 + i] = boardData[i];
        }
        return retVal;
    }

    /**
     * stores the resource data in a given game Board. This stores the org.gringene.jmesudoyu.base.Point
     * representation of the data as well as the flags associated with the
     * Points.
     * 
     * @param tBoard
     *            Board to place org.gringene.jmesudoyu.base.Point information into
     * @return true if the resource data was entered successfully
     */
    public boolean getData(Board tBoard) {
        boolean retVal = true;
        int tPMax = 0;
        if (this.resourceID != SaveResource.BOARDDATA) {
            retVal = false;
        }
        if (retVal) {
            tPMax = this.resourceData[0];
            retVal = retVal && (tPMax == Point.MAX);
        }
        if (retVal) {
            byte[] boardData = new byte[tPMax * tPMax * 3];
            if ((boardData.length + 1) == this.resourceData.length) {
                for (int i = 0; i < boardData.length; i++) {
                    boardData[i] = this.resourceData[i + 1];
                }
                tBoard.staticLoad(boardData);
            } else {
                retVal = false;
            }
        }
        return retVal;
    }
    /**
     * Saves a data resource to a byte array.
     * 
     * @return byte array containing the data from this org.gringene.jmesudoyu.base.SaveResource
     */
    public byte[] saveData(){
        byte[] tRes = new byte[0];
        if((this.resourceID != SaveResource.NA) && (this.resourceData != null)){
            tRes = new byte[SaveResource.IDLENGTH + this.resourceData.length];
            tRes[0] = (byte) ((resourceID & 0xff00) >>> 8);
            tRes[1] = (byte) ((resourceID & 0x00ff));
            for(int i = 0; i < this.resourceData.length; i++){
                tRes[2+i] = this.resourceData[i];
            }
        }
        return tRes;
    }
    /**
     * Loads up a data resource from a byte array. The remainder (everything not
     * consumed after the load is completed) is returned as a byte array.
     * 
     * @param tData
     *            byte array containing data to load into this org.gringene.jmesudoyu.base.SaveResource
     * @return The remaining data after one org.gringene.jmesudoyu.base.SaveResource has been consumed
     */
    public byte[] loadData(byte[] tData) {
        byte[] tRes = new byte[0];
        this.resourceData = null;
        if (tData.length > SaveResource.IDLENGTH) {
            this.resourceID = ((tData[0] & 0xff) << 8)
                    | ((tData[1] & 0xff) << 0);
        } else {
            this.resourceID = SaveResource.NA;
            this.resourceData = null;
        }
        if (this.resourceID == SaveResource.BOARDDATA) {
            int tPMax = tData[SaveResource.IDLENGTH] & 0xff;
            this.resourceData = new byte[1 + tPMax * tPMax * 3];
            for (int i = 0; i < this.resourceData.length; i++) {
                this.resourceData[i] = tData[i + SaveResource.IDLENGTH];
            }
        } else if (this.resourceID == SaveResource.GAMESETTINGS) {
            int keysLength = ((tData[SaveResource.IDLENGTH + 0] & 0xff) << 8)
                    | ((tData[SaveResource.IDLENGTH + 1] & 0xff) << 0);
            int totalLength = 2; // start after the key length bit
            for (int keyNum = 0; keyNum < keysLength; keyNum++) {
                if (tData[totalLength + SaveResource.IDLENGTH] <= 100) {
                    totalLength = totalLength + 1 + 
                    tData[totalLength + SaveResource.IDLENGTH]+ 4; // size, string, value
                }
            }
            if((totalLength > 2) && (tData.length >= totalLength + SaveResource.IDLENGTH)){
                this.resourceData = new byte[totalLength];
                for(int i=0; i < totalLength; i++){
                    this.resourceData[i] = tData[i+SaveResource.IDLENGTH];
                }
            }
        }
        if (this.resourceData != null) {
            tRes = new byte[tData.length - SaveResource.IDLENGTH
                    - resourceData.length];
            for (int i = 0; i < tRes.length; i++) {
                tRes[i] = tData[i + SaveResource.IDLENGTH + resourceData.length];
            }
        }
        return tRes;
    }
}
