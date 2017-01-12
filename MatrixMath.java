/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package yamltodxf;

/**
 *
 * @author Nuwan
 */
public class MatrixMath {
    // Scale a vector by x
    public static double[] scale(double [] vec, double scalar){
        for (int i = 0; i < 3; i++){
            vec[i] *= scalar;
        }
        return vec;
    }
    
    // Scale a matrix by x
    public static double[][] scale(double[][] matrix, double scalar){
        int rows = matrix.length;
        int cols = matrix[0].length;
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                matrix[i][j] *= scalar;
            }
        }
        return matrix;
    }
    
    // Rotate a vector about a reference point, axis vector and angle in degrees
    public static double[][] rotate(double[][] matrix, double[] ref, double[] axis, double angle){
        //int rows = matrix.length;
        //int cols = matrix[0].length;
        double rad = Math.toRadians(angle);
        double ux = axis[0];
        double uy = axis[1];
        double uz = axis[2];
        /*double[][] rotationMatrix = {{Math.cos(rad)+ux*ux*(1-Math.cos(rad)), ux*uy*(1-Math.cos(rad))-uz*Math.sin(rad), ux*uz*(1-Math.cos(rad))+uy*Math.sin(rad)},
        {uy*ux*(1-Math.cos(rad)) + uz*Math.sin(rad), Math.cos(rad)+uy*uy*(1-Math.cos(rad)), uy*ux*(1-Math.cos(rad))-ux*Math.sin(rad)},
        {uz*ux*(1-Math.cos(rad))- uy*Math.sin(rad), uz*uy*(1-Math.cos(rad))+ux*Math.sin(rad), Math.cos(rad)+uz*uz*(1-Math.cos(rad))}}; */
        double [][] rotationMatrix = {{Math.cos(rad)+ux*ux*(1-Math.cos(rad)),uy*ux*(1-Math.cos(rad)) + uz*Math.sin(rad), uz*ux*(1-Math.cos(rad))- uy*Math.sin(rad)},
            {ux*uy*(1-Math.cos(rad))-uz*Math.sin(rad), Math.cos(rad)+uy*uy*(1-Math.cos(rad)), uz*uy*(1-Math.cos(rad))+ux*Math.sin(rad)},
            {ux*uz*(1-Math.cos(rad))+uy*Math.sin(rad), uy*ux*(1-Math.cos(rad))-ux*Math.sin(rad),  Math.cos(rad)+uz*uz*(1-Math.cos(rad))}};
        double[][] result = multiply(matrix, rotationMatrix);
        result = translate(result, ref);
        return result;
        
    }
    
    
    // Translate matrix - overloaded methods for matrix translation and vector translation
    public static double[] translate(double[] vec, double[] translation){
        int aRows = vec.length;
        int bRows = translation.length;
        for (int i = 0; i < aRows; i++){
            vec[i] += translation[i];
        }
        return vec;
    }
    
    public static double[][] translate(double[][] matrix, double[] translation){
        int aRows = matrix.length;
        int bRows = translation.length;
        int aCols = matrix[0].length;
        int bCols = 1;
        for (int i = 0; i < aRows; i++){
            for (int j = 0; j < aCols; j++){
                for (int k = 0; k < bRows; k++){
                    matrix[i][j] += translation[k];
                }
            }
        }
        return matrix;
    }
    
    // Overloaded methods to handle matrix-matrix and matrix-vector multiplication
    //  Vector-matrix
    public static double[] multiply(double[] matB, double[][] matA){
        int aRows = matA.length;
        int aCols = matA[0].length;
        int bRows = matB.length;
        int bCols = 1;
        if (aCols != bRows){
            throw new IllegalArgumentException ("Columns and rows do not match!");
        }
        double[]matC = new double [aRows];
        for(int i = 0; i<bRows; i++){
            matC[i] = 0.00000;
        }
        for (int i = 0; i < aRows; i++){
            for(int j = 0; j < bCols; j++){
                for (int k = 0; k < aCols; k++){
                    matC[i] += matA[i][k] * matB[j];
                }
            }
        }
        return matC;
    }
    // Matrix-vector
    public static double[] multiply(double[][]matA, double[] matB){
        int aRows = matA.length;
        int aCols = matA[0].length;
        int bRows = matB.length;
        int bCols = 1;
        if (aCols != bRows){
            throw new IllegalArgumentException ("Columns and rows do not match!");
        }
        double[]matC = new double [aRows];
        for(int i = 0; i<bRows; i++){
            matC[i] = 0.00000;
        }
        for (int i = 0; i < aRows; i++){
            for(int j = 0; j < bCols; j++){
                for (int k = 0; k < aCols; k++){
                    matC[i] += matA[i][k] * matB[j];
                }
            }
        }
        return matC;
    }
    // Matrix-matrix
    public static double[][] multiply(double[][] matA, double[][] matB){
        int aRows = matA.length;
        int aCols = matA[0].length;
        int bRows = matB.length;
        int bCols = matB[0].length;
        if (aCols != bRows){
            throw new IllegalArgumentException ("Columns and rows do not match!");
        }
        double[][] matC = new double [aRows][bCols];
        for(int i = 0; i<aRows; i++){
            for (int j = 0; j<bCols; j++){
                matC[i][j] = 0.00000;
            }
        }
        for (int i = 0; i < aRows; i++){
            for(int j = 0; j < bCols; j++){
                for (int k = 0; k < aCols; k++){
                    matC[i][j] += matA[i][k] * matB[k][j];
                }
            }
        }
        return matC;
    }
    
    /*
    // Test cases example
    public static void main(String[] args) {
        double[][] A = {{3},{-3},{5},{0}};
        double[][] B = {};
        double [][] result = multiply(A,B);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++)
                System.out.print(result[i][j] + " ");
            System.out.println();
        }
    }
    */
}
