/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yamltodxf;

import javax.swing.*;
import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;
/**
 *
 * @author qiao
 */
public class YamlToDxf {

    /**
     * Glossary -
     * Node: a coordinate in 3d space denoted as (x,y,z). It marks the endpoint of a paired component.
     * Paired component/Edges: Structs/Cables, which contains two node at each end. They could be either defined as two end point, or a starting point + direction + length.
     * Struct: Interchangeably used with "rod", assumed to be an indefinitely stiff component (will not deform due to stress).
     * Cable: Interchangeably used with "string", a soft component that has a resting length attribute. An cabled pull beyond its resting lengh
     *      will excert pulling force while shrinking beyond resting length will make the cable buckle. 
     * Substructure: Independent simple structure that are being used as part of a bigger structure.
     * Simple Structure: Structure that are independent (in terms of material property), but in terms of yaml specification (since material information has been ignored)
     *      a simple structure has a specification that does not contain other structures.
     * Complex Structure: Of course, structure that is made up of many simple structures & some addons, and have them in their specification. 
     */
    HashMap<String, Node> nodeList;
    ArrayList<Struct> structList;
    ArrayList<Cable> cableList;
    HashMap<String, Structure> subList;
    
    public static void main(String[] args) throws IOException {
        YamlToDxf ytd = new YamlToDxf();
        ytd.nodeList = new HashMap<>();
        ytd.structList = new ArrayList<>();
        ytd.cableList = new ArrayList<>();
        ytd.subList = new HashMap<>();
        // save a string representing the root directory for this list of yaml files.
        // String fileEx;

        // Start by picking a file to be read. This would be where we pick our yaml file.
        JFileChooser fileChooser = new JFileChooser();
        JFrame mainframe = new JFrame();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(mainframe);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // fileEx = selectedFile.getParent();
            InputStream input = new FileInputStream(selectedFile);
            ytd.decodeYAML(input, ytd.nodeList, ytd.structList, ytd.cableList, ytd.subList, selectedFile.getParent());
            // Open another window to save output once all the processing work has been done.
            // this process where we create a new file/open a blank file (hopefully blank file)
            JFileChooser fileSaver = new JFileChooser();
            fileSaver.setCurrentDirectory(new File(System.getProperty("user.home")));
            result = fileSaver.showOpenDialog(mainframe);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileSaver.getSelectedFile();
                try  
                {
                    FileWriter fstream = new FileWriter(outputFile, false); //false so we overwrite any existing data in the file
                    ytd.writeOutputDxf(fstream, ytd.nodeList, ytd.structList, ytd.cableList, ytd.subList);
                    System.out.println("after write");
                    fstream.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("File failed to save file.");
            }
        } else {
            System.out.println("File failed to open file.");
        }
    }

    public void decodeYAML(InputStream open, HashMap<String, Node> nodeList, ArrayList<Struct> structList, ArrayList<Cable> cableList, HashMap<String, Structure> subList, String root) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(open);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            // System.out.println(entry.getKey() + "/" + entry.getValue());
            // this is the first level down: there would be two keywords we need to check:
            // nodes and pair_groups
            if (entry.getKey().contains("nodes")) {
                // this is where we parse all the points under nodes section
                Map<String, Object> nodes = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> node : nodes.entrySet()) {
                    // System.out.println(node.getKey() + "/" + node.getValue());
                    // here node.getKey() will be the name of each node.
                    // while node.getValue() are the coordinate of each node.
                    String name = node.getKey();
                    ArrayList coord = (ArrayList) node.getValue();
                    double[] coordinate = decodeArrayListToDouble(coord);
                    // we add that node to our total list of nodes.
                    // System.out.println(name + "--" + coordinate[0] + "," + coordinate[1] + "," + coordinate[2]);
                    nodeList.put(name, new Node(name, coordinate));
                    //  System.out.println();
                }
            }
            else if (entry.getKey().contains("pair_groups")){
                // this is where we parse all the pairs under pair_groups section
                Map<String, Object> pairs = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> type : pairs.entrySet()) {
                    // System.out.println(type.getKey() + "/" + type.getValue());
                    // there are two types of paired components: structs and cables.
                    if (type.getKey().contains("rod")){
                        // this checks if a paired component is a struct
                        ArrayList structs = (ArrayList) type.getValue();
                        for (Object s : structs) {
                            // System.out.println(s);
                            // This prints out each pairs. Each of which is a 2-tuple
                            ArrayList<String> pair = (ArrayList<String>) s;
                            Node top, bot;
                            String[] comb = new String[pair.size()];
                            comb = pair.toArray(comb);          // this digest the pair into a 2-element array
                            top = nodeList.get(comb[0]);    // retrieve 1st element, look up from map, retrieve corresponding node.
                            bot = nodeList.get(comb[1]);    // retrieve 2nd element, look up from map, retrieve corresponding node.
                            // Note this last line will differ
                            structList.add(new Struct(top, bot));
                        }
                    }
                    else if (type.getKey().contains("string")){
                        // this checks if a paired component is a cable
                        ArrayList cables = (ArrayList) type.getValue();
                        for (Object s : cables) {
                            // System.out.println(s);
                            // This prints out each pairs. Each of which is a 2-tuple
                            ArrayList<String> pair = (ArrayList<String>) s;
                            Node top, bot;
                            String[] comb = new String[pair.size()];
                            comb = pair.toArray(comb);          // this digest the pair into a 2-element array
                            top = nodeList.get(comb[0]);    // retrieve 1st element, look up from map, retrieve corresponding node.
                            bot = nodeList.get(comb[1]);    // retrieve 2nd element, look up from map, retrieve corresponding node.
                            // Note this last line will differ
                            cableList.add(new Cable(top, bot));
                        }
                    }
                }
            }
            // The third part in a yaml file usually contains material information, such as pretension, damping, etc.
            // But since it is still questionable whether those parameters do show up in pmpm, it is left out here intentionally.
            // For future implementations, just use the format above, and one should be fine.

            // The forth part here is used just in case if there are other sub-structures:
            else if (entry.getKey().contains("substructures")){
                Map<String, Object> subs = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> sub : subs.entrySet()) {
                    // in this case the Keys are names of each substructure that are involved in this bonding.
                    // values are parameters about their combination.
                    String[] names = sub.getKey().split("/");
                    for (String name : names) {
                        if (!subList.containsKey(name))
                            subList.put(name, new Structure(name));
                    }
                    Map<String, Object> params = (Map<String, Object>) sub.getValue();
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        // Param.getKey would return the name of the parameter.
                        // Param.getValue would return the value of those parameters.
                        // in this case we have to deal with each possible parameter (each getKey) to invoke proper method.
                        // we set them for every singe substructures that appeared in the name sequence above.
                        if (param.getKey().contains("path")){
                            for (String name : names) {
                                String path = (String)param.getValue();
                                if (!path.contains(root))
                                    path = root + "\\" + path;
                                subList.get(name).setFilePath(path);
                            }
                        } else if (param.getKey().contains("translation")) {
                            // Same as what we did when we decode node, we use the same method to get a vector out of param.getValue()
                            ArrayList coord = (ArrayList) param.getValue();
                            double[] coordinate = decodeArrayListToDouble(coord);
                            for (String name : names) {
                                subList.get(name).setTranslation(coordinate);
                            }
                        } else if (param.getKey().contains("rotation")) {
                            // in this case we need to go down one more layer.
                            Map<String, Object> rotation = (Map<String, Object>) param.getValue();
                            for (Map.Entry<String, Object> aspect : rotation.entrySet()) {
                                // a rotation is make up of three aspect/attribute - reference point, axis of rotation, degree of rotation.
                                if (aspect.getKey().contains("reference")){
                                    ArrayList coord = (ArrayList) aspect.getValue();
                                    double[] coordinate = decodeArrayListToDouble(coord);
                                    for (String name : names) {
                                        subList.get(name).setReference(coordinate);
                                    }
                                } else if (aspect.getKey().contains("axis")) {
                                    ArrayList coord = (ArrayList) aspect.getValue();
                                    double[] coordinate = decodeArrayListToDouble(coord);
                                    for (String name : names) {
                                        subList.get(name).setAxis(coordinate);
                                    }
                                } else if (aspect.getKey().contains("angle")) {
                                    Object c = aspect.getValue();
                                    if (c instanceof java.lang.Integer){
                                        for (String name : names) {
                                            subList.get(name).setAngle((double)(int)c);
                                        }
                                    } else if (c instanceof java.lang.Double){
                                        for (String name : names) {
                                            subList.get(name).setAngle((double)c);
                                        }
                                    } else {
                                        // if there is some kind of syntax mistake, we can for now take it in as a 0.
                                        // in the future we could throw exception in here.
                                        for (String name : names) {
                                            subList.get(name).setAngle(0.0);
                                        }
                                    }
                                }
                            }
                        } else if (param.getKey().contains("scale")) {
                            Object c = param.getValue();
                            if (c instanceof java.lang.Integer){
                                for (String name : names) {
                                    subList.get(name).setScale((double)(int)c);
                                }
                            } else if (c instanceof java.lang.Double){
                                for (String name : names) {
                                    subList.get(name).setScale((double)c);
                                }
                            } else {
                                // if there is some kind of syntax mistake, we can for now take it in as a 0.
                                // in the future we could throw exception in here.
                                for (String name : names) {
                                    subList.get(name).setScale(1.0);
                                }
                            }
                        } else if (param.getKey().contains("offset")) {
                            // first extract the amount to offset.
                            ArrayList offset = (ArrayList) param.getValue();
                            double[] vec = decodeArrayListToDouble(offset);
                            // then iterate through all structures in names with a for loop.
                            for (int i = 1; i < names.length; i++) {    // note here we start from one because the first structure in line
                                                                        // won't have a offset. It is rather the reference point for all future points.
                                subList.get(names[i]).setOffset(vec, subList.get(names[i-1]));
                            }
                        }
                    }
                }
                // now here we may need to decode each structure.
                // I'm thinking of passing in each structure recursively.
                // this should be run after we ran through every single sub-block in the substructure section.
                Iterator it = subList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    Structure next = (Structure)pair.getValue();
                    try {
                        // This file path problem might be a problem, since this filepath could either be absolute or relative to the original parent file.
                        File child = new File(next.getFilePath());
                        InputStream file = new FileInputStream(child);
                        decodeYAML(file, next.nodeList, next.structList, next.cableList, next.subList, child.getParent());
                    } catch (FileNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            // The fifth part here is used to define substructure bonding:
            else if (entry.getKey().contains("bond_groups")){
                Map<String, Object> groups = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> group : groups.entrySet()) {
                    // getKey would return the bonding type: whether it's cable or struct
                    // getValue return the bonds, which points of those structures connect.
                    if (group.getKey().contains("rod")){
                        // same code as below.
                        // We go through each block of specifications, featuring a sequence of names followed by their connections 
                        Map<String, Object> blocks = (Map<String, Object>) group.getValue();
                        for (Map.Entry<String, Object> block : blocks.entrySet()) {
                            // gets the names of structure that are involved in additional bonding.
                            String[] names = block.getKey().split("/");
                            // translate names into their structure value.
                            Structure[] structsInvolved = new Structure[names.length - 1];
                            for (int i = 0; i < structsInvolved.length; i++)
                                structsInvolved[i] = subList.get(names[i]);
                            // now here comes the hardest part.
                            // we decode each pair, then look for the name of those nodes in every substructure within structsInvolved array.
                            // until we find the corresponding node name, or we quite because we can't find any.
                            // this will be a recursive process as well, since every sub-structure has to recursive down on their substructure, if they can't find the exact node.
                            ArrayList structs = (ArrayList) block.getValue();
                            for (Object s : structs) {
                                // System.out.println(s);
                                // This prints out each pairs. Each of which is a 2-tuple
                                // used the same lines as above where I extract structs and cables from current file.
                                // except this time we locate the individual nodes through traversing every substructures nodeList.
                                ArrayList<String> pair = (ArrayList<String>) s;
                                Node top, bot;
                                String[] comb = new String[pair.size()];
                                comb = pair.toArray(comb);          // this digest the pair into a 2-element array
                                // there are two types of bonding - one that are strictly between two structures, and one that are between a sequence of structures.
                                // under circumstances where there are a sequence of structures, we bond them two at a time.
                                for (int i = 0; i < structsInvolved.length - 1; i++) {
                                    // Here we assume that first node is from the first structure, second node is from the second structure.
                                    top = processNestedNodes(comb[0], structsInvolved[i]);
                                    // System.out.println(top);
                                    bot = processNestedNodes(comb[1], structsInvolved[i + 1]);
                                    // System.out.println(bot);
                                    // Note this last line will differ
                                    structList.add(new Struct(top, bot));
                                }
                            }

                        }
                    }
                    else if (group.getKey().contains("string")){
                        // exact same code as above, of course.
                        // We go through each block of specifications, featuring a sequence of names followed by their connections 
                        Map<String, Object> blocks = (Map<String, Object>) group.getValue();
                        for (Map.Entry<String, Object> block : blocks.entrySet()) {
                            // gets the names of structure that are involved in additional bonding.
                            String[] names = block.getKey().split("/");
                            // translate names into their structure value.
                            Structure[] structsInvolved = new Structure[names.length - 1];  // minus 1 here to accomadate the last element, which is the type of bonding
                            for (int i = 0; i < structsInvolved.length; i++)    
                                structsInvolved[i] = subList.get(names[i]);
                            // now here comes the hardest part.
                            // we decode each pair, then look for the name of those nodes in every substructure within structsInvolved array.
                            // until we find the corresponding node name, or we quite because we can't find any.
                            // this will be a recursive process as well, since every sub-structure has to recursive down on their substructure, if they can't find the exact node.
                            ArrayList structs = (ArrayList) block.getValue();
                            for (Object s : structs) {
                                // System.out.println(s);
                                // This prints out each pairs. Each of which is a 2-tuple
                                // used the same lines as above where I extract structs and cables from current file.
                                // except this time we locate the individual nodes through traversing every substructures nodeList.
                                ArrayList<String> pair = (ArrayList<String>) s;
                                Node top, bot;
                                String[] comb = new String[pair.size()];
                                comb = pair.toArray(comb);          // this digest the pair into a 2-element array
                                // there are two types of bonding - one that are strictly between two structures, and one that are between a sequence of structures.
                                // under circumstances where there are a sequence of structures, we bond them two at a time.
                                for (int i = 0; i < structsInvolved.length - 1; i++) {
                                    // Here we assume that first node is from the first structure, second node is from the second structure.
                                    top = processNestedNodes(comb[0], structsInvolved[i]);
                                    // System.out.println(top);
                                    bot = processNestedNodes(comb[1], structsInvolved[i + 1]);
                                    // System.out.println(bot);
                                    // Note this last line will differ
                                    cableList.add(new Cable(top, bot));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public double[] decodeArrayListToDouble(ArrayList obj) {
        double[] coordinate = new double[3];
        int counter = 0;
        for (Object c : obj) {
            // System.out.print(c.getClass());
            // these are the coordinate of each node.
            // each interation of the this for-loop print the coordinate in one dimension
            // this loop runs 3 times for every node.
            // According to the yaml parser we have to read mapped value as object.
            // but according to java syntax rule we can't parse object type directly to Double, even if we know that they will be numbers most likely.
            // so here we have to check its type, then parse accordingly.
            if (c instanceof java.lang.Integer){
                coordinate[counter] = (double)(int)c;
            } else if (c instanceof java.lang.Double){
                coordinate[counter] = (double)c;
            } else {
                // if there is some kind of syntax mistake, we can for now take it in as a 0.
                // in the future we could throw exception in here.
                coordinate[counter] = 0;
            }
            counter++;
        }  
        return coordinate;
    }

    public Node processNestedNodes(String str, Structure st) {
        // in this case the style of reference is different, based on the string/node combination we get from the yaml specification.
        // I believe the convention would be:
        // - if the string came in the format of filename.nodename, it means that the node is located 2 level down.
        // - where if the string only has nodename, it means it is located 1 level down
        // so we have overloaded method in structure class that deals with each situation.
        String[] param = str.split("\\.");
        Node found;
        if (param.length == 1) {    
            found = st.findNode(param[0]);
            if (found != null)
                return found;
            return null;
        } else if (param.length == 2) {
            found = st.findNode(param[0], param[1]);
            if (found != null)
                return found;
            return null;
        }
        return null;
    }

    public void writeOutputDxf(FileWriter writer, HashMap<String, Node> nodeList, ArrayList<Struct> structList, ArrayList<Cable> cableList, HashMap<String, Structure> subList) throws IOException{
        BufferedWriter out = new BufferedWriter(writer);
        // in this case the yaml file is a simple structure and we dont have to worry about the other two parts: substructure and inter-structural bonding.
        // we first write each rod and string base on node positions, then we are good to go.
        // begin by write the header of dxf to the output file.
        out.write("0\n");
        out.write("SECTION\n");
        out.write("2\n");
        out.write("ENTITIES\n");
        out.write("0\n");
        
        // we have to handle all the substructures as well.
        System.out.println("Size " + subList.size());
        for (Map.Entry<String, Structure> entry: subList.entrySet()) {
            // we use Structure class's output method.
            // System.out.println(entry.getKey());
            out.write(entry.getValue().toDxf());
        }
        // Then we handle all the structs and cables
        // either there are a list of substructures, of which we need to handle any kind of inter-connections between them.
        // or there isn't any substructures, in which case we treat this as a simple structure and process all its edges.
        for (Struct struct : structList)
            out.write(struct.toDxf());
        for (Cable cable : cableList) {
            // System.out.println(cable);
            out.write(cable.toDxf());
        }
        
        // this marks the end of file
        out.write("ENDSEC\n");
        out.write("0\n");
        out.write("EOF\n");
        out.close();
    }
}
