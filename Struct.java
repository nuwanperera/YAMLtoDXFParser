/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yamltodxf;

import java.lang.Math;
/**
 *
 * @author qiao
 */
public class Struct {
    /**
     * ok, trying to adapt to the formal naming convention
     * In NTRT thou, this is called Rod, so be aware.
     * I know friend, it's the wild west out there, between all different conventions.
     */
    Node top, bot;   // the order of which doesn't matter.
    public Struct(Node top, Node bot){
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
        // note that here we use 'bar' instead of cable. This will be one of the differences between Struct and Cable class.
        return "LINE\n" + "8\n" + "bar\n" + top.toDxf(true) + bot.toDxf(false) + "0\n";
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
