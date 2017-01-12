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
public class Node {
   String name;
   double x, y, z;
   public Node() {
       name = "";
       x = 0;
       y = 0;
       z = 0;
   }
   
   public Node (String name, double x, double y, double z) {
       this.name = name;
       this.x = x;
       this.y = y;
       this.z = z;
   }
   
   public Node (String name, double[] coord) {
       if (coord.length != 3){
           return;
       }
       this.name = name;
       this.x = coord[0];
       this.y = coord[1];
       this.z = coord[2];
   }
   
   public String getName() {
       return name;
   }
   
   public double[] getCoord() {
       return new double[]{x,y,z};
   }
   
   public void setCoord(double[] coords) {
       x = coords[0];
       y = coords[1];
       z = coords[2];
   }
   
   public String toDxf(boolean top) {
        if (top)
            return "10\n" + x * 10 + "\n" + "20\n" + y * 10 + "\n" + "30\n" + z * 10 + "\n";
        else
            return "11\n" + x * 10 + "\n" + "21\n" + y * 10 + "\n" + "31\n" + z * 10 + "\n";
   }
   
   public void rotateNode(double[] ref, double[] axis, double angle) {
       double[][] coord = new double[1][3];
       coord[0][0] = x;
       coord[0][1] = y;
       coord[0][2] = z;
       double[][] result = MatrixMath.rotate(coord, ref, axis, angle);
       setCoord(result[0]);    
   }
   
   public void translateNode(double[] translation) {
       double[] coord = new double[] {x,y,z};
       setCoord(MatrixMath.translate(coord, translation));
   }
   
   public void scaleNode(double scale) {
       double[] coord = new double[] {x,y,z};
       setCoord(MatrixMath.scale(coord, scale));
   }
   
   public String toString() {
       return name + "[" + x + "," + y + "," + z + "]";
   }
}
