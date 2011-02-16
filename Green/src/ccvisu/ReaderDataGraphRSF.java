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
import java.util.StringTokenizer;
import java.util.Vector;

/*****************************************************************
 * Reader for co-change graphs in RSF format.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataGraphRSF extends ReaderDataGraph {

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderDataGraphRSF(BufferedReader in) {
    super(in);
  }

  // Helper.
  public static String readEntry(StringTokenizer st) {
    String result = st.nextToken();
    if (result.charAt(0) == '"') {
      while (result.charAt(result.length() - 1) != '"') {
        result = result + ' ' + st.nextToken();
      }
      result = result.substring(1, result.length() - 1);
    }
    return result;
  }
  
  /*****************************************************************
   * Reads the edges of a graph in RSF (relational standard format)
   * from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  protected Vector<GraphEdgeString> readEdges() {
    Vector<GraphEdgeString> result = new Vector<GraphEdgeString>();
    int lineno = 1;
    try {
      String lLine;
      while ((lLine = in.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(lLine);
        
        GraphEdgeString edge = new GraphEdgeString();
		if (st.hasMoreTokens() && lLine.charAt(0)!='#') {
          // Relation name.
          edge.relName = st.nextToken();
          // Source vertex.
          edge.x = readEntry(st);
          // Target vertex.
          edge.y = readEntry(st);
          if (st.hasMoreTokens()) {
		    edge.w = st.nextToken();
		  } else {
		    edge.w = "1.0";
		  }

		  /*
		  int conf = Integer.parseInt(st.nextToken());
		  if (conf >300) {
		    result.add(edge);
		  }
		  */
		
		  result.add(edge);
		}
		++lineno;
      }
    }
    catch (Exception e) {
      System.err.println("Exception while reading the graph (readGraph) at line " 
                         + lineno + ":");
      System.err.println(e);
    }
    return result;
  }

};

