/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package yamltodxf;

import java.util.*;
public class Structure {
    // class representing a substructure object.
    // it has a few parameters, including a name, file path, a translation vector, and a rotation -> which is made up of a reference point,
    // axis, and an angle.
    String name, filepath;
    double[] translation, reference, axis, offset;
    Structure offsetTarget;
    double angle, scale;
    
    public HashMap<String, Node> nodeList;
    public ArrayList<Struct> structList;
    public ArrayList<Cable> cableList;
    public HashMap<String, Structure> subList;
    
    public Structure(String name) {
        this.name = name;
        filepath = "";
        scale = 1;
        translation = new double[] {0,0,0};
        // reference = new double[] {0,0,0};
        axis = new double[] {0,0,0};
        offset = new double[] {0,0,0};
        
        nodeList = new HashMap<>();
        structList = new ArrayList<>();
        cableList = new ArrayList<>();
        subList = new HashMap<>();
    }
    
    public String getFilePath() {
        return filepath;
    }
    
    public void setFilePath(String path) {
        filepath = path;
    }
    
    public void setTranslation(double[] trans) {
        translation = trans;
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public void setReference(double[] ref) {
        reference = ref;
    }
    
    public void setOffset(double[] offset, Structure target) {
        // in this method we handle offset and the target that it is offseted from
        // if offset and translation are conflicting, in here offset will override translation
        this.offset = offset;
        offsetTarget = target;
    }
    
    public void setAxis(double[] axis) {
        this.axis = axis;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    public Node findNode(String filename, String nodename) {
        // this method is strictly 2 level down.
        // future changes/implementations might be needed to accomadate more levels.
        if (name.equals(filename)) {
            return nodeList.get(nodename);
        } else {
            if (subList.get(filename) != null)
                return subList.get(filename).findNode(filename, nodename);
            return null;
        }
    }
    
    public Node findNode(String nodename) {
        // prioritize first level 
        // future changes/implementations might be needed to accomadate more levels.
        if (nodeList.get(nodename) != null) {
            return nodeList.get(nodename);
        } else {
            for (Map.Entry<String, Structure> entry : subList.entrySet()) {
                if (entry.getValue().findNode(nodename) != null)
                    return entry.getValue().findNode(nodename);
            }
            return null;
        }
    }
    
    public void calcOffset(Structure struc, double[] offset) {
       // here we assume that structures who can be offseted must be the same/contain the same set of nodes. 
       for (Map.Entry<String, Node> entry: nodeList.entrySet()) {
           Node temp = entry.getValue();
           // we go in and change every coordinate in the current list of structure.
           temp.setCoord(MatrixMath.translate(struc.nodeList.get(entry.getKey()).getCoord(), offset));
       }
    }
    
    public double[] findCenter() {
        double[] center = new double[] {0,0,0};
        for (Map.Entry<String, Node> entry: nodeList.entrySet()) {
            double[] temp = entry.getValue().getCoord();
            center[0] += temp[0]; 
            center[1] += temp[1]; 
            center[2] += temp[2]; 
        }
        for (int i=0; i<center.length; i++) {
            center[i] = center[i]/nodeList.size();
        }
        System.out.println(center[0] + "," + center[1] + "," + center[2]);
        return center;
    }
    
    public String toDxf() {
        // initialize output string
        String out = "";
        // we need to translate and rotate all the points
        // *** Note here rotation is not applied to any substrctures, which I assume will be very important and might be needed for future work.
        if (reference == null)
            reference = findCenter();
        for (Map.Entry<String, Node> entry: nodeList.entrySet()) {
            Node temp = entry.getValue();
            System.out.println(temp);
            temp.rotateNode(reference, axis, angle);
            System.out.println(temp);
            // temp.scaleNode(scale);
            // System.out.println(temp); be aware, if scale is initialized to 0, it will set all node coordinates to 0. PLS Dont ask me how do I know.
        }
        // offset first then translate and scale
        //if (offsetTarget != null)
            //calcOffset(offsetTarget, offset);
        /*
        for (Map.Entry<String, Node> entry: nodeList.entrySet()) {
            Node temp = entry.getValue();
            temp.translateNode(translation);
            // System.out.println(temp);
        }
        */
        // we have to handle all the substructures as well.
        for (Map.Entry<String, Structure> entry: subList.entrySet()) {
            // we use Structure class's output method.
            out += entry.getValue().toDxf();
        }
        // Then we handle all the structs and cables
        // either there are a list of substructures, of which we need to handle any kind of inter-connections between them.
        // or there isn't any substructures, in which case we treat this as a simple structure and process all its edges.
        for (Struct struct : structList)
            out += struct.toDxf();
        for (Cable cable : cableList)
            out += cable.toDxf();
        return out;
    }
}
