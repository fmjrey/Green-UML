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

/*****************************************************************
 * Reader for input data.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class ReaderData {
  /** Input stream reader object. */
  protected BufferedReader in;

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderData(BufferedReader in) {
    this.in = in;
  }

  /*****************************************************************
   * Reads the graph or layout data from stream reader <code>in</code>.
   * @param graph  <code>GraphData</code> object to store the read graph or layout data in.
   *****************************************************************/
  abstract public void read(GraphData graph);

};

