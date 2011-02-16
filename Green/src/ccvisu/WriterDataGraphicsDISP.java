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
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/*****************************************************************
 * Writer for displaying the layout on the screen device.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class WriterDataGraphicsDISP extends WriterDataGraphics {

  private ScreenDisplay display;
  private Vector<Set<String>> xMap;
  private Vector<Set<String>> yMap;
  private Vector<Vector<Set<Integer>>> edgeMap;
  private boolean[] edgeAnnot;
  
  //group of nodes
  private Vector<Cluster> clusters;
  
  //write-color (computed once and stored)
  private Color frontColor;

  // The name of the input file (or empty if input was read from standard input).
  protected String inputName;
  
  //cmd to open a URL
  private String browser;
  
  // Temporarily associated during callback from ScreenDisplay.
  private Graphics area;
  private int insetleft;
  private int insetbottom;
  private int xSize = 0;
  private int ySize = 0;
  /**
   * Constructor.
   * @param graph       Graph representation, contains the positions of the vertices.
   * @param minVert     Diameter of the smallest vertex.
   * @param fontSize    Font size of vertex annotations.
   * @param backColor   Background color.
   * @param blackCircle If true, draw black circle around each vertex.
   * @param showEdges   If true, draw the edges between the vertices (if possible).
   * @param openURL     is Opening nodes as url allowed
   * @param inputName   the windows title
   * @param browser     the browser cmd
   */
  public WriterDataGraphicsDISP(GraphData graph, 
					            float     minVert,
					            int       fontSize,
					            Color     backColor,
					            boolean   blackCircle,
                                boolean   showEdges,
                                boolean   openURL,
					            String    inputName,
                                String    browser) {
    super(graph, minVert, fontSize, backColor, blackCircle, showEdges, openURL);
    
    this.inputName = inputName;
    this.browser   = browser;
    
    //init
    clusters = new Vector<Cluster>();
    adjustFrontColor();
    
    //create default cluster with all the nodes in it
    Cluster.init(this,graph);
    Cluster defaultCluster = new Cluster("default",CCVisu.green);
    addCluster(defaultCluster);
    for (int i = 0; i < graph.vertices.size(); ++i) {
        defaultCluster.addNodeByIndex_WO_COLOR(i);
        
    }
    //edges annotation
    if(!graph.edges.isEmpty()){
        int size = graph.edges.size();
        edgeAnnot = new boolean[size];
        for(int i = 0; i < size; ++i){
            edgeAnnot[i] = false;
        }
    }
    
    display = new ScreenDisplay(this);
    if (display == null) {
      System.err.println("Runtime error: Could not open ScreenDisplay.");
      System.exit(1);
    }
  }

  /*****************************************************************
   * Nothing to do here.
   * The constructor initializes the ScreenDisplay (frame and canvas), 
   * and that calls back to the methods below 
   * (writeDISP, writeLAY, toggleVertexNames, getVertexNames).
   *****************************************************************/
  public void write() {
  }

  /*****************************************************************
   * Write graphics layout.
   * @param size  Size of output area (e.g., number of pixel).
   *****************************************************************/
  public void writeGraphicsLayout(int size) {
    float xPosMin = 1000000;
    float xPosMax = -1000000;
    float yPosMin = 1000000;
    float yPosMax = -1000000;
    float zPosMin = 1000000;
    float zPosMax = -1000000;
    for (int i = 0; i < graph.vertices.size(); ++i) {
      if ( graph.vertices.get(i).showVertex ) {
        xPosMin = Math.min(xPosMin, graph.pos[i][0]);
        xPosMax = Math.max(xPosMax, graph.pos[i][0]);
        yPosMin = Math.min(yPosMin, graph.pos[i][1]);
        yPosMax = Math.max(yPosMax, graph.pos[i][1]);
        zPosMin = Math.min(zPosMin, graph.pos[i][2]);
        zPosMax = Math.max(zPosMax, graph.pos[i][2]);
      } 
    }

    float layoutDist;
    layoutDist = Math.max(xPosMax - xPosMin,  yPosMax - yPosMin);
    layoutDist = Math.max(layoutDist,         zPosMax - zPosMin);
    float xOffset = - xPosMin + 0.05f * layoutDist;
    float yOffset = - yPosMin + 0.05f * layoutDist;
    float zOffset = - zPosMin + 0.05f * layoutDist;
    float scale = 0.9f * size / layoutDist;
    
    
    //draw edges
    if(showEdges && !graph.edges.isEmpty()){
        int end = graph.edges.size();
        for (int i = 0; i < end; ++i) {
            GraphEdgeInt e = (GraphEdgeInt) graph.edges.get(i);
            writeEdge( i,
                      (int)((graph.pos[e.x][0]+ xOffset) *  scale),
                      (int)((graph.pos[e.x][1]+ yOffset) * -scale + size),
                      (int)((graph.pos[e.x][2]+ zOffset) *  scale),
                      (int)((graph.pos[e.y][0]+ xOffset) *  scale),
                      (int)((graph.pos[e.y][1]+ yOffset) * -scale + size),
                      (int)((graph.pos[e.y][2]+ zOffset) *  scale));
        }
    }
    
    int end = clusters.size();
    //draw the vertices
    for (int i = 0; i < end; ++i){
        Cluster clt = clusters.get(i);
        if(clt.visible){
            //draw the vertices that are not annotated
            Iterator<Integer> it = clt.Iterator();
            while(it.hasNext()){
                int index = it.next().intValue();
                GraphVertex curVertex = graph.vertices.get(index);
                if (curVertex.showVertex && !curVertex.showName) {
                    int radius  = (int) Math.max(Math.pow(curVertex.degree, 0.5) * minVert,
                                             minVert);
                    int xPos = (int) ((graph.pos[index][0] + xOffset) *  scale);
                    int yPos = (int) ((graph.pos[index][1] + yOffset) * -scale + size);
                    int zPos = (int) ((graph.pos[index][2] + zOffset) *  scale);
                    
                    writeVertex(curVertex, xPos, yPos, zPos, radius);
                }
            }
            //Draw the annotated vertices.
            it = clt.Iterator();
            while(it.hasNext()){
                int index = it.next().intValue();
                GraphVertex curVertex = graph.vertices.get(index);
                if (curVertex.showVertex && curVertex.showName) {
                    int radius  = (int) Math.max(Math.pow(curVertex.degree, 0.5) * minVert,
                                             minVert);
                    int xPos = (int) ((graph.pos[index][0] + xOffset) *  scale);
                    int yPos = (int) ((graph.pos[index][1] + yOffset) * -scale + size);
                    int zPos = (int) ((graph.pos[index][2] + zOffset) *  scale);
                    
                    writeVertex(curVertex, xPos, yPos, zPos, radius);
                }
            }
        }
    }
    
    //draw the cluster specific information (except default cluster)
    for (int i = 1; i < end; ++i){//set begin to 1, 0 only for test
        Cluster clt = clusters.get(i);
        if(clt.visible && clt.info){
            int x = (int)((clt.getX() + xOffset)*scale + insetleft);
            int y = (int)((clt.getY() + yOffset)* -scale + size - insetbottom);
            int l = x-5;
            int r = x+5;
            int u = y-5;
            int b = y+5;
            area.setColor(Color.BLACK);
            area.drawLine(l, u, r, b);
            area.drawLine(r, u, l, b);
            area.setColor(clt.getColor());
            area.drawLine(l, y, r, y);
            area.drawLine(x, u, x, b);
            int radius = (int)(clt.getAverageRadius()*scale);
            int diam = (radius+radius);
            area.drawOval(x-radius, y-radius, diam, diam);
            area.setColor(Color.BLACK);
        }
    }
  }
  
  /**
   * Writes the layout on the screen device (DISP output format).
   * Call-back method, invoked from <code>ScreenDisplay</code>.
   * @param size         Size of the output drawing square.
   * @param area         The drawing area of the canvas.
   * @param xCanvasSize  Width of the canvas.
   * @param yCanvasSize  Height of the canvas. 
   * @param insetleft    Left inset of the drawing frame.
   * @param insetbottom  Bottom inset of the drawing frame.
   */
  public void writeDISP(int size, 
                        Graphics area, 
                        int xCanvasSize, int yCanvasSize,
                        int insetleft,
                        int insetbottom) {
    this.area = area;
    this.insetbottom = insetbottom;
    this.insetleft = insetleft;
    
    
    
    // Maps for getting the vertices at mouse positions.
    xMap = new Vector<Set<String>>(xCanvasSize);
    yMap = new Vector<Set<String>>(yCanvasSize);
    for(int i = 0; i < xCanvasSize; ++i) {
      xMap.add(new TreeSet<String>());
    }
    for(int i = 0; i < yCanvasSize; ++i) {
      yMap.add(new TreeSet<String>());
    }
    //Maps for getting the edges at mouse positions.
    if(showEdges){
      if(xSize == xCanvasSize && ySize == yCanvasSize){
        for(int x = 0; x < xCanvasSize; ++x){
          Vector<Set<Integer>> lVec = edgeMap.get(x);
          for(int y = 0; y < yCanvasSize; ++y){
            lVec.get(y).clear();
          }
        }
      }else{
        edgeMap = new Vector<Vector<Set<Integer>>>(xCanvasSize);
        for(int x = 0; x < xCanvasSize; ++x){
          Vector<Set<Integer>> lVec = new Vector<Set<Integer>>(yCanvasSize);
          edgeMap.add(lVec);
          for(int y = 0; y < yCanvasSize; ++y){
            lVec.add(new TreeSet<Integer>());
          }
        }
        xSize = xCanvasSize;
        ySize = yCanvasSize;
      }
    }
    
    writeGraphicsLayout(size);
  }

  /**
   * Writes a vertex on screen.
   * @param curVertex  The vertex object, to access vertex attributes.
   * @param xPos       x coordinate of the vertex.
   * @param yPos       y coordinate of the vertex.
   * @param zPos       z coordinate of the vertex.
   * @param radius     Radius of the vertex.
   */
  public void writeVertex(GraphVertex curVertex, 
                          int xPos, int yPos, int zPos, int radius) {
    // Correction for inset.left.
    xPos = xPos + insetleft;
    //  Correction for inset.bottom.
    yPos = yPos - insetbottom;
    
    int startX = xPos - radius;
    int startY = yPos - radius;
    
    // Draw the vertex.
    int diam = 2*radius;
    area.setColor(curVertex.color);
    area.fillOval(startX, startY, diam, diam);
    if (blackCircle) {
      area.setColor(frontColor);
      area.drawOval(startX, startY, diam, diam);
    }

    if (curVertex.showName) {
      // Draw annotation.
      // Use inverted background color for the annotation.
      area.setColor(frontColor);
      area.drawString(curVertex.name, xPos + radius + 3, yPos + 3);
    }

    // For interactive annotation: Store vertex names at their positions in the maps.
    int endX = Math.min(xPos + radius, xMap.size() - 1);
    for (int pos = Math.max(startX, 0); pos <= endX; ++pos) {
      xMap.get(pos).add(curVertex.name);
    }
    int endY = Math.min(yPos + radius, yMap.size() - 1);
    for (int pos = Math.max(startY, 0); pos <= endY; ++pos) {
      yMap.get(pos).add(curVertex.name);
    }
  }
  
  /**
   * Writes an edge.
   * @param index       index of the edge in graph.edges
   * @param xPos1       x coordinate of the first point.
   * @param yPos1       y coordinate of the first point.
   * @param zPos1       z coordinate of the first point.
   * @param xPos2       x coordinate of the second point.
   * @param yPos2       y coordinate of the second point.
   * @param zPos2       z coordinate of the second point.
   */
  public void writeEdge(int index, int xPos1, int yPos1, int zPos1,
                        int xPos2, int yPos2, int zPos2){
      //reflexive edges are not allowed by specification
      if(xPos1 == xPos2 && yPos1 == yPos2){
          return;
      }
      
      GraphEdgeInt edge = graph.edges.get(index);
      
      //Correction for inset.left
      xPos1 = xPos1 + insetleft;
      xPos2 = xPos2 + insetleft;
      // Correction for inset.bottom
      yPos1 = yPos1 - insetbottom;
      yPos2 = yPos2 - insetbottom;
      
      //////
      //Draw
      //////
      area.setColor(frontColor);
      area.drawLine(xPos1, yPos1, xPos2, yPos2);
      //Draw the annotation
      if(edgeAnnot[index]){
          int xPos = (xPos1+xPos2+fontSize)/2 ;
          int yPos = (yPos1+yPos2+fontSize)/2 ;
          area.drawString(edge.relName, xPos, yPos);
      }
      ///////////////////////////////////////////////////
      //store for annotation (Bresenham's line algorithm)
      boolean steep = Math.abs(yPos2 - yPos1) > Math.abs(xPos2 - xPos1);
      if(steep){
          int tmp = xPos1;
          xPos1 = yPos1;
          yPos1 = tmp;
          tmp = xPos2;
          xPos2 = yPos2;
          yPos2 = tmp;
      }
      if(xPos1 > xPos2){
          int tmp = xPos1;
          xPos1 = xPos2;
          xPos2 = tmp;
          tmp = yPos1;
          yPos1 = yPos2;
          yPos2 = tmp;
      }
      int deltax = xPos2 - xPos1;
      int deltay = Math.abs(yPos2 - yPos1);
      float error = 0;
      float deltaerr = ((float)deltay) / deltax;
      int y = yPos1;
      int ystep;
      if(yPos1 < yPos2){
          ystep = 1;
      }else{
          ystep = -1;
      }
      for(int x = xPos1; x <= xPos2; ++x){
          if(steep){
              if(y >= 0 && y < xSize &&  x >= 0 && x < ySize){
                  edgeMap.get(y).get(x).add(new Integer(index));
              }
          }else{
              if(x >= 0 && x < xSize &&  y >= 0 && y < ySize){
                  edgeMap.get(x).get(y).add(new Integer(index));
              }
          }
          error += deltaerr;
          if(error >= 0.5){
              y += ystep;
              error -= 1.0;
          }
      }
  }  
  
  /*****************************************************************
   * Writes layout to file using an implementation of class <code>WriterData</code>.
   * Call-back method, invoked from within ScreenDisplay.
   * @param fileName     Name of the output file to write the layout to.
   *****************************************************************/
  public void writeFileLayout(String fileName) {
	try {
	  PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
	  WriterData dataWriter = new WriterDataLAY (graph, out);  // Default, also .lay.
      if (fileName.endsWith(".svg")) {
	    dataWriter = new WriterDataGraphicsSVG (graph,     out,
		                                        minVert,   fontSize,
		                                        backColor, blackCircle,
                                                showEdges, openURL,
                                                1.0f,      inputName);
      } else if (fileName.endsWith(".wrl")) {
  	    dataWriter = new WriterDataGraphicsVRML (graph,     out,
  	                                             minVert,   fontSize,
  	                                             backColor, blackCircle,
                                                 showEdges, openURL,
                                                 1.0f);
      }
	  dataWriter.write();
	  out.close();
	  System.err.println("Wrote layout to output file '" + fileName + "'.");
	}
	catch (Exception e) {
	  System.err.println("Exception while writing file '" + fileName + "': ");
	  System.err.println(e);
	}
  }

  /*****************************************************************
   * Marks all vertices whose node names match the given regular expression.
   * Call-back method, invoked from within ScreenDisplay.
   * @param regEx     Regular expression.
   *****************************************************************/
  public void markVertices(String regEx) {
      Color color = CCVisu.red;
      for (int i = 0; i < graph.vertices.size(); ++i) {
          GraphVertex curVertex = graph.vertices.get(i);
          if (curVertex.name.matches(regEx)) {
              curVertex.color    = color;
              curVertex.showName = true;
          }
      }
  }

  /****************************************************************************
   * Toggle the showName flag of the vertices and edges at the given position.
   * Call-back method, invoked from within ScreenDisplay.
   * @param p       coordinates of the vertex.
   * @return number of names toggled
   ***************************************************************************/
  public int toggleNames(Point p) {
      
      int xPos = (int) p.getX();
      int yPos = (int) p.getY();
      Set<String> tmp = new TreeSet<String>(xMap.get(xPos));
      tmp.retainAll(yMap.get(yPos));
      Iterator<String> it = tmp.iterator();
      int nb = 0;
      while (it.hasNext()) {
          ++nb;
          String name = it.next();
          GraphVertex curVertex = graph.nameToVertex.get(name);
          curVertex.showName = !curVertex.showName;
      }
      
      //edges
      if(showEdges){
          Set<Integer> edgesIndex = edgeMap.get(xPos).get(yPos);
          Iterator<Integer> edgeIt = edgesIndex.iterator();
          while(edgeIt.hasNext()){
              ++nb;
              int index = edgeIt.next().intValue();
              edgeAnnot[index] = !edgeAnnot[index];
          }
      }
      
      return nb;
  }

  /*****************************************************************
   * Show all labels (vertices and edges names).
   * Call-back method, invoked from within ScreenDisplay.
   *****************************************************************/
  public void showAllLabels() {
    //vertices
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  if (curVertex.showVertex) {
		curVertex.showName = true;
	  }
	}
    //edges
    int end = graph.edges.size();
    for(int i = 0; i < end; ++i){
        edgeAnnot[i] = true;
    }
  }
  
  /*****************************************************************
   * Hide all labels (vertice names).
   * Call-back method, invoked from within ScreenDisplay.
   *****************************************************************/
  public void hideAllLabels() {
    //vertices
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  if (curVertex.showVertex) {
          curVertex.showName = false;
	  }
	}
	//edges
    int end = graph.edges.size();
    for(int i = 0; i < end; ++i){
        edgeAnnot[i] = false;
    }
  }
  
  /**
   * show/hide the name of a vertex
   * @param vertex a vertex
   */
  public void showLabel(GraphVertex vertex, boolean show){
      if(show){
          if (vertex.showVertex) {
              vertex.showName = true;
          }
      }else{
          if (vertex.showVertex) {
              vertex.showName = false;
          }
      }
  }

  /************************************************************************
   * Compute list of names of the vertices and edges at the given position.
   * Call-back method, invoked from within ScreenDisplay.
   * @param p       coordinates.
   ***********************************************************************/
  public String getNames(Point p) {
	int xPos = (int) p.getX();
	int yPos = (int) p.getY();
	if (xPos >= xMap.size()  ||  yPos >= yMap.size()) {
	  return new String("");
	}
	Set<String> tmp = new TreeSet<String>(xMap.get(xPos));
	tmp.retainAll(yMap.get(yPos));
    
    //edges
    if(showEdges){
        Set<Integer> edgesIndex = edgeMap.get(xPos).get(yPos);
        Iterator<Integer> it = edgesIndex.iterator();
        while(it.hasNext()){
            int index = it.next().intValue();
            tmp.add(graph.edges.get(index).relName);
        }
    }
    
	return(tmp.toString());
  }

  /*****************************************************************
   * Restrict the set of vertices displayed on the screen to
   * the vertices within the given rectangular (i.e., zoom).
   * Call-back method, invoked from within ScreenDisplay.
   * @param pTopLeft      coordinates of the top left corner of the rectangular.
   * @param pBottomRight  coordinates of the bottom right corner of the rectangular.
   *****************************************************************/
  public void restrictShowedVertices(Point pTopLeft, 
		                             Point pBottomRight) {
    int end = (int)Math.min(pBottomRight.getX(),xMap.size());
    Set<String> xNodes = new TreeSet<String>();
	for (int i = (int) pTopLeft.getX(); i < end; ++i) {
      xNodes.addAll(xMap.get(i));
	}
    end = (int)Math.min(pBottomRight.getY(),yMap.size());
    Set<String> yNodes = new TreeSet<String>();
	for (int i = (int) pTopLeft.getY(); i < end; ++i) {
	  yNodes.addAll(yMap.get(i));
	}
    Set<String> lNodesToKeep = xNodes;
    lNodesToKeep.retainAll(yNodes);
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  if (!lNodesToKeep.contains(curVertex.name)) {
        curVertex.showVertex = false;
	  }
	}
  }

  /*****************************************************************
   * Reset vertex restriction that was set by restrictShowedVertices.
   * Call-back method, invoked from within ScreenDisplay.
   *****************************************************************/
  public void resetRestriction() {
	// Handle vertex options.
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  // hideSource (do not show vertex if it is source of an edge).
	  if (CCVisu.getHideSource() && curVertex.isSource) {
	    curVertex.showVertex = false;
	  } else {
		curVertex.showVertex = true;
	  }
	}
  }

  /*****************************************************************
   * Sets the local graph representation (layout) to a new value.
   * Call-back method, invoked from within ScreenDisplay.
   * @param layout     Graph/layout representation to switch to.
   *****************************************************************/
  public void setGraphData(GraphData layout) {
    this.graph = layout;
    
    //create default cluster with all the nodes in it
    Cluster.init(this,graph);
    Cluster defaultCluster = new Cluster("default",CCVisu.green);
    this.clusters.removeAllElements();
    addCluster(defaultCluster);
    for (int i = 0; i < graph.vertices.size(); ++i) {
        defaultCluster.addNodeByIndex_WO_COLOR(i);
        
    }
    showEdges = showEdges && !graph.edges.isEmpty();
    //edges annotation
    if(!graph.edges.isEmpty()){
        int size = graph.edges.size();
        edgeAnnot = new boolean[size];
        for(int i = 0; i < size; ++i){
            edgeAnnot[i] = false;
        }
    }
    
    display.refresh();
  }

  /**
   * get showEdges
   */
  public boolean isshowEdgesPossible(){
      return !graph.edges.isEmpty();
  }

  /**
   * set showEdges
   */
  public void setshowEdges(boolean se){
      this.showEdges = se && isshowEdgesPossible();
  }
  
  /*****************************************************************
   * Set a color to all vertices 
   * Call-back method, invoked from within ScreenDisplay.
   *****************************************************************/
  public void setColorToAll(Color color) {
    for (int i = 0; i < graph.vertices.size(); ++i) {
      GraphVertex curVertex = graph.vertices.get(i);
      if (curVertex.showVertex) {
       curVertex.color = color;
      }
    }
  }

  /**
   * adjust frontColor
   */
  public void adjustFrontColor(){
    frontColor = new Color( 0xffffffff - backColor.getRGB() );
    //problem when using gray: colors too close => hard to read
    //ignore alpha
    if( Math.abs(frontColor.getRed()   - backColor.getRed())   < 10 &&
        Math.abs(frontColor.getBlue()  - backColor.getBlue())  < 10 &&
        Math.abs(frontColor.getGreen() - backColor.getGreen()) < 10 ) {
      frontColor = Color.BLACK;
    }
  }
  
  /**
   * the color of the text
   * @return the color of the text
   */
  public Color getWriteColor() {
      return frontColor;
  }
  
  /**
   * add a new cluster in the list
   * @param clt
   */
  public void addCluster(Cluster clt){
      clusters.add(clt);
  }
  
  /**
   * remove the cluster at the specified index
   * @param index
   */
  public void removeCluster(int index){
      Cluster clt = clusters.get(index);
      Cluster defaultClt = clusters.get(0);
      int end = clt.size();
      GraphVertex[] nodes = new GraphVertex[end];
      for(int i = 0; i < end; ++i){
          nodes[i] = clt.getNode(i);
      }for(int i = 0; i < end; ++i){
          defaultClt.addNode(nodes[i]);
      }
      clusters.remove(index);
  }
  
  /**
   * return the cluster at the specified index
   * @param index
   * @return the cluster at the specified index
   */
  public Cluster getCluster(int index){
      if(index >= 0 && index < clusters.size()){
          return clusters.elementAt(index);
      }else{
          return null;
      }
  }
  
  /**
   * return the cluster with the specified name
   * @param name
   * @return the cluster with the specified name
   */
  public Cluster getCluster(String name){
      Iterator<Cluster> it = clusters.iterator();
      while(it.hasNext()){
          Cluster clt = it.next();
          if(clt.getName().equals(name)){
              return clt;
          }
      }
      return null;
  }
  
  /**
   * get the number of cluster
   * @return return the number of cluster
   */
  public int getNbOfCluster(){
      return clusters.size();
  }
  
  /**
   * move the cluster at index one place higher in the list
   * => cluster drawn sooner
   * @param index
   */
  public void moveClusterUp(int index){
      if(index > 1){
          Cluster tmp = clusters.get(index);
          clusters.remove(index);
          clusters.insertElementAt(tmp, index -1);
      }
  }
  
  /**
   * move the cluster at index one place lower in the list
   * => drawn later (more on top)
   * @param index
   */
  public void moveClusterDown(int index){
      if(index < clusters.size() -1 && index > 0){
          Cluster tmp = clusters.get(index);
          clusters.remove(index);
          clusters.insertElementAt(tmp, index +1);
      }
  }
  
  /**
   * tells the cluster that the graph has changed => recompute some data
   *
   */
  public void refreshCluster(){
      for(int i = 0; i < clusters.size(); ++i){
          clusters.get(i).graphchanged();
      }
  }

  /**
   * Open the name of what is under the cursor as if it is an URL.
   * @param p   Coordinates
   */
  public void openURL(Point p){
      String targets = getNames(p);
      int lght = targets.length();
      if(lght <= 2){
          return;
      }
      StringTokenizer st = new StringTokenizer(targets.substring(1,lght-1));
      if(st.hasMoreTokens()){
          String URL = st.nextToken();
          if(URL.startsWith("\"") && URL.endsWith("\"")){
              URL = URL.substring(1,URL.length()-1);
          }
          if(browser == null){
              if(!guessBrowser(URL)){
                  System.err.println("Unable to find browser");
                  return;
              }
          }else{
              String cmd[] = {browser,URL};
              
              System.err.println("opening: "+URL);
              
              try{            
                  Runtime rt = Runtime.getRuntime();
                  rt.exec(cmd);
              } catch (Throwable t){
                  t.printStackTrace();
              }
          }
      }
  }
  
  private boolean guessBrowser(String URL){
      
      String[] possibility = {
              "firefox",
              "mozilla",
              "opera",
              "safari",
              "iexplorer",
              "epiphany",
              "konqueror"
      };
            
      for(int i = 0; i < possibility.length; ++i){
          browser = possibility[i];
          
          String cmd[] = {browser,URL};
          
          try{
              Runtime rt = Runtime.getRuntime();
              rt.exec(cmd);
              return true;
          }catch(Throwable t){}
      }
      browser = null;
      return false;
  }

  /**
   * @return the display
   */
  public ScreenDisplay getDisplay() {
    return display;
  }

};
