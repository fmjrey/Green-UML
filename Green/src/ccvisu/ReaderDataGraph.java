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

import java.io.BufferedReader;
import java.util.Vector;

/*****************************************************************
 * Reader for input graphs.
 * Different concrete graph readers return what they read in String format 
 * (list of edges of type <code>GraphEdgeString</code>)
 * when <code>readEdges()</code> is called.
 * One single transformation method (<code>readGraph()</code> of this class)
 * transforms the string representation into the final format 
 * (<code>GraphData</code> object with edges of type <code>GraphEdgeInt</code>).
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class ReaderDataGraph extends ReaderData {
  /** End of line.*/
  protected final static String endl = CCVisu.endl;

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderDataGraph(BufferedReader in) {
    super(in);
  }

  /*****************************************************************
   * Reads the graph data from stream reader <code>in</code>.
   * @param graph  <code>GraphData</code> object to store the graph data in.
   *****************************************************************/
  public void read(GraphData graph){
    Vector stringEdges = readEdges();
    readGraph(graph, stringEdges);
  }

  /*****************************************************************
   * Reads the graph data from list of string edges (see class comment).
   *****************************************************************/
  public static void readGraph(GraphData graph, Vector stringEdges) {
    for (int i = 0; i < stringEdges.size(); ++i) {
      GraphEdgeString curStringEdge = (GraphEdgeString) stringEdges.get(i);
      // Source vertex.
      GraphVertex x = new GraphVertex();
      x.name = curStringEdge.x;
      // Target vertex.
      GraphVertex y = new GraphVertex();
      y.name = curStringEdge.y;

      // Insert x-vertex to graph.
      if (! graph.nameToVertex.containsKey(x.name)) {
      	x.id = graph.vertices.size();
        graph.vertices.add(x);
        graph.nameToVertex.put(x.name, x);
      }
      // Use existing vertex, if not the same (id field).
      x = graph.nameToVertex.get(x.name);
      x.isSource = true;

      // Insert y-vertex to graph.
      if (! graph.nameToVertex.containsKey(y.name)) {
		y.id = graph.vertices.size();
        graph.vertices.add(y);
        graph.nameToVertex.put(y.name, y);
      }
      // Use existing vertex, if not the same (id field).
      y = graph.nameToVertex.get(y.name);
      y.isSource = false;

      // Insert edge to graph.
      GraphEdgeInt edge = new GraphEdgeInt();
      edge.relName = curStringEdge.relName;
 	  edge.x = x.id;
	  edge.y = y.id;
      // (Detection of reflexive edges is done by CCVisu.computeLayout().)
      edge.w = Math.abs(Float.parseFloat(curStringEdge.w));

      graph.edges.add(edge);
      // Adjust degrees of the vertices.
      x.degree += edge.w;
      y.degree += edge.w;
      if (x.degree < 0 || y.degree < 0) {
      	System.err.println("Invalid graph: edge {" + x.name + "," + y.name + "} " + 
      		           "has weight: " + edge.w + ".");
      }
    }
    return;
  }

  /*****************************************************************
   * Reads the edges of a graph from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  abstract protected Vector readEdges();

};

