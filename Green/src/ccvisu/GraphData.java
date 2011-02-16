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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/*****************************************************************
 * Contains the representation of a graph.
 * This class is a collection of the data structures needed 
 * for layout and transformation processing.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class GraphData {
  /** Maps a vertex id to a GraphVertex.*/
  public Vector<GraphVertex> vertices;
  /** Maps a vertex name to a GraphVertex.*/
  public Map<String,GraphVertex> nameToVertex;
  /** Edges of type GraphEdgeInt. Only used if (inFormat < LAY).*/
  public Vector<GraphEdgeInt> edges;
  /** Layout. Only used if (outFormat >= LAY).*/
  public float[][] pos;

  /** Constructor.*/
  public GraphData() {
    vertices      = new Vector<GraphVertex>();
    nameToVertex  = new HashMap<String,GraphVertex>(); //TreeMap<String,GraphVertex>();
    edges         = new Vector<GraphEdgeInt>();
    // The initialization of 'pos' is postponed until the number of vertices is known,
    // done by method 'initializeLayout'. 
  }
};

