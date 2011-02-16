/*
 * CCVisu is a tool for visual graph clustering
 * and general force-directed graph layout.
 * This file is part of CCVisu. 
 * 
 * Copyright (C) 2005-2007  Dirk Beyer
 * 
 * CCVisu is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * CCVisu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CCVisu; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Please find the GNU Lesser General Public License in file
 * license_lgpl.txt or http://www.gnu.org/licenses/lgpl.txt
 * 
 * Dirk Beyer    (firstname.lastname@sfu.ca)
 * Simon Fraser University (SFU), B.C., Canada
 */

package ccvisu;

import java.awt.Color;
import java.util.Iterator;
import java.util.Vector;

/**
 * A class with a list of nodes that compute some informations on them
 * 
 * @version  $Revision$; $Date$
 * @author Damien Zufferey
 */
public class Cluster {
    
    /**used as mode for the method addPattern*/
    public static final int EQUALS = 0;
    /**used as mode for the method addPattern*/
    public static final int CONTAINS = 1;
    /**used as mode for the method addPattern*/
    public static final int STARTS = 2;
    /**used as mode for the method addPattern*/
    public static final int ENDS = 3;
    
    /**contain the name of node's cluster*/
    private static String[] indexToCltName;
    /**name of the cluster*/
    private String name;
    /**list of int representing the index of nodes in the GraphData*/
    private Vector<Integer> nodes;
    /**color of the cluster*/
    private Color color;
  
    
    /**x-coordinate of the barycenter*/
    private float x;
    /**y-coordinate of the barycenter*/
    private float y;
    /**z-coordinate of the barycenter*/
    private float z;
    
    private float averageRadius;
    
    /** pointer to the data*/
    private static GraphData graph;
    private static WriterDataGraphicsDISP writer;
    
    /**used to know if the cluster should be drawn*/
    public boolean visible = true;
    /**used to know if the circle and cross should be drawn */
    public boolean info = false; 
    /** true if needs to recompute radius, center,... */
    private boolean changed = true;
    
    /**
     * Constructor
     * @param name the cluster's name
     */
    public Cluster(String name){
        this.name = name;
        color = CCVisu.red;
        nodes = new Vector<Integer>();
    }
    
    /**
     * Constructor
     * @param name the cluster's name
     * @param color the cluster's color
     */
    public Cluster(String name, Color color){
        this.name = name;
        this.color = color;
        nodes = new Vector<Integer>();
    }
    
    /**
     * add the given node to the cluster
     * @param vertex
     */
    public void addNode(GraphVertex vertex){
        int index = graph.vertices.indexOf(vertex);
        nodes.add(new Integer(index));
        vertex.color = color;
        changed = true;
        
        Cluster clt = writer.getCluster(indexToCltName[index]);
        if(clt != null){
            clt.removeNodeByIndex(index);
        }
        indexToCltName[index] = name;
    }
    
    /**
     * add the node that corresponds to the index-th node in graph(GraphData)
     * @param index
     */
    public void addNodeByIndex(int index){
        nodes.add(new Integer(index));
        GraphVertex vertex = graph.vertices.get(index);
        vertex.color = color;
        changed = true;
        
        Cluster clt = writer.getCluster(indexToCltName[index]);
        if(clt != null){
            clt.removeNodeByIndex(index);
        }
        indexToCltName[index] = name;
    }
    
    /**
     * add the node that corresponds to the index-th node in graph(GraphData)
     * without changing his color
     * function used only to assign to default cluster at begining
     * @param index
     */
    public void addNodeByIndex_WO_COLOR(int index){
        nodes.add(new Integer(index));
        changed = true;
        
        Cluster clt = writer.getCluster(indexToCltName[index]);
        if(clt != null){
            clt.removeNodeByIndex(index);
        }
        indexToCltName[index] = name;
    }
    
    /**
     * remove from cluster the given node
     * @param vertex
     */
    public void removeNode(GraphVertex vertex){
        nodes.remove(new Integer(graph.vertices.indexOf(vertex)));
        changed = true;
    }
    
    /**
     * remove the node that corresponds to the index-th node in graph(GraphData)
     * @param index
     */
    public void removeNodeByIndex(int index){
        nodes.remove(new Integer(index));
        changed = true;
    }
    
    /**
     * return an iterator on the index of the cluster's nodes
     * @return iterator
     */
    public Iterator<Integer> Iterator(){
        return nodes.iterator();
    }

    /**
     * adds nodes to a cluster in function of a given pattern 
     * @param pattern 
     * @param mode the way of using the pattern.
     */
    public void addPattern(String pattern, int mode){
        for (int i = 0; i < graph.vertices.size(); ++i) {
            GraphVertex curVertex = graph.vertices.get(i);
            if(mode == EQUALS){
                if(curVertex.name.equals(pattern)){
                    addNodeByIndex(i);
                }
            }else if(mode == CONTAINS){
                if(curVertex.name.matches(".*"+pattern+".*")){
                    addNodeByIndex(i);
                }
            }else if(mode == STARTS){
                if(curVertex.name.startsWith(pattern)){
                    addNodeByIndex(i);
                }
            }else if(mode == ENDS){
                if(curVertex.name.endsWith(pattern)){
                    addNodeByIndex(i);
                } 
            }
        }
    }
    
    
    public void filter(String pattern, int mode, boolean keep){
        int end = size();
        boolean match[] = new boolean[end];
        for (int i = 0; i < end; ++i) {
            match[i] = !keep;
            GraphVertex curVertex = getNode(i);
            if(mode == EQUALS){
                if(curVertex.name.equals(pattern)){
                    match[i] = keep;
                }
            }else if(mode == CONTAINS){
                if(curVertex.name.matches(".*"+pattern+".*")){
                    match[i] = keep;
                }
            }else if(mode == STARTS){
                if(curVertex.name.startsWith(pattern)){
                    match[i] = keep;
                }
            }else if(mode == ENDS){
                if(curVertex.name.endsWith(pattern)){
                    match[i] = keep;
                } 
            }
        }
        GraphVertex selected[] = new GraphVertex[end];
        for(int i = 0; i<end; ++i){
            if(!match[i]){
                selected[i] = getNode(i);
            }
        }
        Cluster defaultClt = writer.getCluster(0);
        for(int i = 0; i<end; ++i){
            if(selected[i] != null){
                defaultClt.addNode(selected[i]);
            }
        }
    }
    
    /**
     * return the i-th element of the Cluster
     * this method's purpose is to easily iterate on each node of the cluster
     * @param i index
     * @return a vertex of the graph
     */
    public GraphVertex getNode(int i){
        int index = ((Integer)nodes.get(i)).intValue();
        return graph.vertices.get(index);
    }
    
    /**
     * @return return the color of the cluster.
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color color to define.
     */
    public void setColor(Color color) {
        this.color = color;
        int end = nodes.size();
        for(int i = 0; i < end; ++i){
            int index = ((Integer)nodes.get(i)).intValue();
            GraphVertex curVertex = graph.vertices.get(index);
            curVertex.color = color;
        }
    }

    /**
     * @return return the name of the cluster.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name name to define.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return return the averageRadius.
     */
    public float getAverageRadius() {
        if(changed){
            compute();
        }
        return averageRadius;
    }

    /**
     * return the x-coordinate of the barycenter
     * @return x
     */
    public float getX(){
        if(changed){
            compute();
        }
        return x;
    }

    /**
     * return the y-coordinate of the barycenter
     * @return y
     */
    public float getY(){
        if(changed){
            compute();
        }
        return y;
    }
    
    /**
     * return the z-coordinate of the barycenter
     * @return z
     */
    public float getZ(){
        if(changed){
            compute();
        }
        return z;
    }
    
    /**
     * return the size of the cluster
     * @return return the size of the cluster
     */
    public int size(){
        return nodes.size();
    }
    
    /**
     * compute the informations provided by the cluster
     */
    private void compute(){
        int nbr = nodes.size();
        //barycenter
        x = 0;
        y = 0;
        z = 0;
        for(int i = 0; i < nbr; ++i){
            int index = ((Integer)nodes.get(i)).intValue();
            x += graph.pos[index][0];
            y += graph.pos[index][1];
            z += graph.pos[index][2];
        }
        x /= nbr;
        y /= nbr;
        z /= nbr;
        //radius
        averageRadius = 0;
        for(int i = 0; i < nbr; ++i){
            int index = ((Integer)nodes.get(i)).intValue();
            float delta_x = (float)Math.pow(graph.pos[index][0] - x,2);
            float delta_y = (float)Math.pow(graph.pos[index][1] - y,2);
            float delta_z = (float)Math.pow(graph.pos[index][2] - z,2);
            averageRadius += Math.sqrt(delta_x + delta_y + delta_z);
        }
        averageRadius /= nbr;
        
        changed = false;
    }
    
    /**
     * initialize Data common to all clusters
     * @param writer
     * @param graph
     */
    public static void init(WriterDataGraphicsDISP writer, GraphData graph){
        Cluster.writer = writer;
        Cluster.graph = graph;
        indexToCltName = new String[graph.vertices.size()];
    }
    
    /**
     * tells the cluster to recompute its informations
     */
    public void graphchanged(){
        this.changed = true;
    }

}
