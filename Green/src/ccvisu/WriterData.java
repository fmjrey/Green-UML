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

/*****************************************************************
 * Writer for output data.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class WriterData {
  /** End of line.*/
  protected final static String endl = CCVisu.endl;

  /** Graph representation.*/
  protected GraphData graph;

  /**
   * Constructor.
   * @param graph  Graph representation.
   */
  public WriterData(GraphData graph) {
    this.graph = graph;
  }

  /*****************************************************************
   * Writes the graph or layout data.
   *****************************************************************/
  public abstract void write();

};
