/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yamltodxf;

/**
 *
 * @author qiao
 */
public class Cable {
     /**
     * Exact same as Struct, in fact the code is copy and pasted, in case you wonder.
     */
    Node top, bot;   // the order of which doesn't matter.
    public Cable(Node top, Node bot){
        this.top = top;
        this.bot = bot;
    }
    
    public double getLength() {
        // There's really no point to look at this comment again, if you find yourself doing so...
        double[] coordT = top.getCoord();
        double[] coordB = bot.getCoord();
        return Math.sqrt(Math.pow((coordT[0] - coordB[0]),2) + Math.pow((coordT[1] - coordB[1]),2) + Math.pow((coordT[2] - coordB[2]),2));
    }
    
    public String toDxf() {
        return "LINE\n" + "8\n" + "cable\n" + top.toDxf(true) + bot.toDxf(false) + "0\n";
    }
    
    public String printTop () {
        double[] C = top.getCoord();
        return (top.getName() + "(" + C[0] + "," +  C[1] + "," + C[2] +")");
    }
    
    public String printBot () {
        double[] C = bot.getCoord();
        return (bot.getName() + "(" + C[0] + "," +  C[1] + "," + C[2] +")");
    }
    
    public String toString() {
        return printTop() + " - " + printBot();
    }
}
