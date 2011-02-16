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

/*****************************************************************
 * Writer for graphical output of layout data.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class WriterDataGraphics extends WriterData {

  protected float   minVert;
  protected int     fontSize;
  protected Color   backColor;
  protected boolean blackCircle;
  protected boolean showEdges;
  protected boolean openURL;

  /**
   * Constructor.
   * @param graph       Graph representation, contains the positions of the vertices.
   * @param minVert     Diameter of the smallest vertex.
   * @param fontSize    Font size of vertex annotations.
   * @param backColor   Background color.
   * @param blackCircle If true, draw black circle around each vertex.
   * @param showEdges   If true, draw the edges between the vertices (if possible).
   */
  public WriterDataGraphics(GraphData graph, 
                            float     minVert,
	                        int       fontSize,
	                        Color     backColor,
	                        boolean   blackCircle,
                            boolean   showEdges,
                            boolean   openURL) {
    super(graph);
    this.minVert     = minVert;
    this.fontSize    = fontSize;
    this.backColor   = backColor;
    this.blackCircle = blackCircle;
    this.showEdges   = showEdges;
    this.openURL     = openURL;
  }

  /*****************************************************************
   * Writes the layout data in a graphics format.
   *****************************************************************/
  abstract public void write();

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
	
    // Draw the edges.
    if(showEdges && !graph.edges.isEmpty()){
        int end = graph.edges.size();
        for (int i = 0; i < end; ++i) {
            GraphEdgeInt e = graph.edges.get(i);
            writeEdge( i,
                      (int)((graph.pos[e.x][0]+ xOffset) *  scale),
                      (int)((graph.pos[e.x][1]+ yOffset) * -scale + size),
                      (int)((graph.pos[e.x][2]+ zOffset) *  scale),
                      (int)((graph.pos[e.y][0]+ xOffset) *  scale),
                      (int)((graph.pos[e.y][1]+ yOffset) * -scale + size),
                      (int)((graph.pos[e.y][2]+ zOffset) *  scale));
        }
    }
        
	// Draw the vertices.
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  if (!curVertex.showName) {
		if (curVertex.showVertex) {
		  int radius  = (int) Math.max(Math.pow(curVertex.degree, 0.5) * minVert, 
									   minVert);
		  int xPos = (int) ((graph.pos[i][0] + xOffset) *  scale);
		  int yPos = (int) ((graph.pos[i][1] + yOffset) * -scale + size);
		  int zPos = (int) ((graph.pos[i][2] + zOffset) *  scale);
		  
		  writeVertex(curVertex, xPos, yPos, zPos, radius);
		}
	  }
	}

	// Draw the annotated vertices.
	for (int i = 0; i < graph.vertices.size(); ++i) {
	  GraphVertex curVertex = graph.vertices.get(i);
	  if (curVertex.showName) {
		if (curVertex.showVertex) {
		  int dia  = (int) Math.max(Math.pow(curVertex.degree, 0.5) * minVert, 
									minVert);
		  int xPos = (int) ((graph.pos[i][0] + xOffset) *  scale);
		  int yPos = (int) ((graph.pos[i][1] + yOffset) * -scale + size);
		  int zPos = (int) ((graph.pos[i][2] + zOffset) *  scale);

		  writeVertex(curVertex, xPos, yPos, zPos, dia);
		}
	  }
	}

  }

  /**
   * Writes a vertex.
   * @param curVertex  The vertex object, to access vertex attributes.
   * @param xPos       x coordinate of the vertex.
   * @param yPos       y coordinate of the vertex.
   * @param zPos       z coordinate of the vertex.
   * @param radius     Radius of the vertex.
   */
  abstract public void writeVertex(GraphVertex curVertex, 
                                   int xPos, int yPos, int zPos, int radius);
  
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
  abstract public void writeEdge(int index, int xPos1, int yPos1, int zPos1,
                               int xPos2, int yPos2, int zPos2);

};
