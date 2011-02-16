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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/*****************************************************************
 * Reader for CVS log files. 
 * Extracts the co-change graph from the CVS log info.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataGraphCVS extends ReaderDataGraph {

  /** Time-window constant for transaction recovery, in milli-seconds.*/
  private int timeWindow;

  private boolean sliding; 
  
  /**
   * Constructor.
   * @param in          Stream reader object.
   * @param timeWindow  Time window for transaction recovery, in milli-seconds
   *                    (default: 180'000).
   */
  public ReaderDataGraphCVS(BufferedReader in, int timeWindow) {
    super(in);
    this.timeWindow = timeWindow;
    sliding = false;
  }
  
  /**
   * Constructor.
   * @param in          Stream reader object.
   * @param timeWindow  Time window for transaction recovery, in milli-seconds
   *                    (default: 180'000).
   * @param sliding     sliding or fixed time window
   */
  public ReaderDataGraphCVS(BufferedReader in, int timeWindow, boolean sliding) {
    super(in);
    this.timeWindow = timeWindow;
    this.sliding = sliding;
  }

  /** 
   * Represents a CVS revision entry (an abstraction of it).
   * @author Dirk Beyer
   */
  private class Revision implements Comparable {
    String relName;
    String filename;
    Long   time;
    String user;
    String logmsg;
    /** The internal number (id) of the change transaction.*/
    int    transaction;
    
    /*****************************************************************
     * Compares this revision with the specified object for order.  
     * Returns a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.<p>
     *
     * @param   o Object to be compared for order with this revision.
     * @return  a negative integer, zero, or a positive integer as this object
     *              is less than, equal to, or greater than the specified object.
     * 
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     *****************************************************************/
    public int compareTo(Object o) {
      Revision rev = (Revision) o;
      if  (rev == null) { // Either o is null or o is not of class Revision.
        throw new ClassCastException("Object of class Revision required.");
      }
      if (this.time.compareTo(rev.time) == 0) {
        return  this.hashCode() - rev.hashCode();
      } else {
        return  this.time.compareTo(rev.time);
      }
    }
    
    /*****************************************************************
     * Compares the specified object with this revision for equality.
     * Returns <tt>true</tt> if the specified object is identical with this object. 
     * The method is based on <tt>compareTo</tt> to make the ordering 
     * <i>consistent with equals</i>.<p>
     * 
     * @param o  Object to be compared for equality with this revision.
     * @return <tt>true</tt> if the specified Object is equal to this revision.
     *****************************************************************/
    public boolean equals(Object o) {
      Revision rev = (Revision) o;
      if (rev == null) { // Either o is null or o is not of class Revision.
        return false;
      }
      return (this.compareTo(o) == 0);
    }
  };

  /*****************************************************************
   * Reads the edges of a graph in CVS log format
   * from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  protected Vector<GraphEdgeString> readEdges() {
    Vector<GraphEdgeString> result = new Vector<GraphEdgeString>();

    Vector<Revision> revisionList = readRevisionList();
    SortedMap transMap = recoverTransactions(revisionList);
    
    Set timeSet = transMap.keySet();
    Iterator timeIt = timeSet.iterator();
    while( timeIt.hasNext() ) {
      Long time = (Long) timeIt.next();
      Collection transColl = (Collection) transMap.get(time);

      Iterator transIt = transColl.iterator();
      while( transIt.hasNext() ) {
        Set revSet = (Set) transIt.next();

        Iterator revIt = revSet.iterator();
        while( revIt.hasNext() ) {
          Revision revision = (Revision) revIt.next();

          GraphEdgeString edge = new GraphEdgeString();
          //relation name
          edge.relName = revision.relName;
          // Source vertex.
          edge.x = Integer.toString(revision.transaction); 
          // Target vertex.
          edge.y = revision.filename;
          // Edge weight.
          edge.w = "1.0";
          result.add(edge);

          // Print revision entry with timestamp and user of the changes to stdout.
          if (CCVisu.getVerbosityLevel() >= 1) { 
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(revision.time.longValue());
            System.out.println("REV  \t" + Integer.toString(revision.transaction) +
                                    "\t" + "\"" + cal.getTime() + "\"" +
                                    "\t" + revision.user + 
                                    "\t" + revision.filename +
                                    "\t" + revision.relName);
          }
        }
      }
    }
    return result;
  }

  /*****************************************************************
   * Parses the date entry.
   * @param dateStr   The CVS date entry string.
   * @return  Long value of the date, or 
   *          <code>null</code> if <code>dateStr</code> 
   *          is not a valid date entry.
   *****************************************************************/
  private Long parseDate(String dateStr) {
    // Delimiter for year/month/day.
    char delim = '/';
    int posEnd = dateStr.indexOf(delim);
    if (posEnd < 0 || posEnd > 9) {
          delim = '-';
          posEnd = dateStr.indexOf(delim);
          if (posEnd < 0 || posEnd > 9) {
            return null;
          }
    } 
    int posBegin = 0;
    int year = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(delim, posBegin);
            
    int month = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(' ', posBegin);
            
    int day = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(':', posBegin);
            
    int hour = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(':', posBegin);
            
    int min = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = posBegin + 2;
    int sec =Integer.parseInt(dateStr.substring(posBegin, posEnd));
            
    Calendar cal = Calendar.getInstance();
    cal.clear();     // Erase the milli secs.
    cal.set(year, month-1, day, hour, min, sec);

    return new Long(cal.getTimeInMillis());
  }            

  /*****************************************************************
   * Parses the CVS log data and extracts revisions.
   * @return  List of revisions.
   *****************************************************************/
  private Vector<Revision> readRevisionList() {
    Vector<Revision> result = new Vector<Revision>();

    String lLine = "";
    //String relName = "CO-CHANGE";
    String filename = null;
    Long   time;
    String user;
    String logmsg;

    int lineno = 1;
    try {
      while ((lLine = in.readLine()) != null) {
        // New working file.
        if (lLine.startsWith("Working file: ")) {
          // Set name of the current working file, 
          //   for which we pasre the revisions.
          filename = lLine.substring(14);
        }
                  
        // New revision.
        if (lLine.startsWith("date: ")) {
          // Set date, author, and logmsg of the current revision.

          // Parse date. Start right after "date: ".
          time = parseDate(lLine.substring(6, lLine.indexOf("author: ")));
          if (time == null) {
            System.err.print("Error while reading the CVS date info for file: ");
            System.err.println(filename + ".");
          }
            
          // Parse author. Start right after "author: ".
          int posBegin = lLine.indexOf("author: ") + 8;
          int posEnd = lLine.indexOf(';', posBegin);
          user = lLine.substring(posBegin, posEnd);
                  
          // Parse logmsg. Start on next line the date/author line.
          logmsg = "";
          ++lineno;
          while ( ((lLine = in.readLine()) != null)  &&
                   !lLine.startsWith("----")         &&
                   !lLine.startsWith("====")              ) {
            if (!lLine.startsWith("branches: ")) {
              logmsg += lLine + endl;
            }
            ++lineno;
          }

          // Create revision and add revision to resulting list.
          Revision revision = new Revision();
          //revision.relName = relName.replace(' ', '_');  // Replace blanks by underline.
          revision.filename = filename.replace(' ', '_');  // Replace blanks by underline.
          revision.time = time;
          revision.user = user;
          revision.logmsg = logmsg;
          result.add(revision);
          
          //System.out.print("Relation: "+ relName +
          //                 " File: " + filename +
          //                 " Time: " + time.toString() +
          //                 " User: " + user + " LogMsg: " + logmsg);
          
        }
                ++lineno;
      } // while
    } catch (Exception e) {
      System.err.println("Exception while reading the CVS log at line " 
                         + lineno + ":");
      System.err.println(e);
      System.err.print("Read line: ");
      System.err.println(lLine); 
    }
    return result;
  }

  /*****************************************************************
   * Recovers the change transactions for the co-change graph 
   *   from the revision information, i.e., it assignes
   *   the transaction ids for the revisions.
   * @param   revisionList is a list of revisions.
   * @return  Sorted map that maps timestamps to collections of transactions,
   *          where transactions are sets of revisions.
   *****************************************************************/
  private SortedMap recoverTransactions(Vector<Revision> revisionList) {

    // Step 1: Transform the list of revisions to a sorted data structure.
    
    // A map user -> msg-entry.
    Map<String,Map<String,SortedMap<Long,SortedSet<String>>>> userMap 
      = new HashMap<String,Map<String,SortedMap<Long,SortedSet<String>>>>();
    // A map logmsg -> time-entry.
    Map<String,SortedMap<Long,SortedSet<String>>> msgMap;
    // A map time -> file.
    SortedMap<Long,SortedSet<String>> timeMap;
    // A set of files.
    SortedSet<String> fileSet;
    
    for (int i = 0; i < revisionList.size(); ++i) {
      Revision revision = revisionList.get(i);
      // A map logmsg -> time-entry.
      msgMap = userMap.get(revision.user);
      if (msgMap == null) {
        msgMap = new HashMap<String,SortedMap<Long,SortedSet<String>>>();
        userMap.put(revision.user, msgMap);
      }
      
      // A map time -> file.
      timeMap = msgMap.get(revision.logmsg);
      if (timeMap == null) {
        timeMap = new TreeMap<Long,SortedSet<String>>();
        msgMap.put(revision.logmsg, timeMap);
      }
        
      // A set of files.
      fileSet = timeMap.get(revision.time);
      if (fileSet == null) {
        fileSet = new TreeSet<String>();
        timeMap.put(revision.time, fileSet);
      }

      // Add file to set.
      fileSet.add(revision.filename);
    }


    // Step 2: Create the result, which is
    // a map timestamp -> set of transactions (Long -> SortedSet), 
    // where one transaction is a set of revisions.
    SortedMap<Long,Collection<SortedSet<Revision>>> result 
      = new TreeMap<Long,Collection<SortedSet<Revision>>>();
    
    int transaction = 0;
    Set userSet = userMap.keySet();
    Iterator userIt = userSet.iterator();
    while( userIt.hasNext() ) {
      String user = (String) userIt.next();
      msgMap = userMap.get(user);

      Set msgSet = msgMap.keySet();
      Iterator msgIt = msgSet.iterator();
      while( msgIt.hasNext() ) {
        String logmsg = (String) msgIt.next();
        timeMap = msgMap.get(logmsg);

        Set timeSet = timeMap.keySet();
        Iterator timeIt = timeSet.iterator();

        long firstTime = 0;
        Set<String> tmpFilesSeen = new TreeSet<String>(); // Detect a time window that is too long.
        Collection<SortedSet<Revision>> transColl;        // Collection of transactions.
        SortedSet<Revision> revSet = null;                // Transaction, i.e., set of revisions.
        while( timeIt.hasNext() ) {
          Long time = (Long) timeIt.next();
          if (time.longValue() - firstTime > timeWindow) {
            // Start new transaction.
            ++transaction;
            firstTime = time.longValue();
            tmpFilesSeen.clear();
            // Retrieve (or create new) set of transactions for the timestamp.
            transColl = result.get(time);
            if (transColl == null) {
              transColl = new Vector<SortedSet<Revision>>();
              result.put(time, transColl);
            }
            // New transaction (set of revisions).
            revSet = new TreeSet<Revision>();
            transColl.add(revSet);
          } else if(sliding){
            // The time window 'slides' with the files.
            firstTime = time.longValue();
          }

          fileSet = timeMap.get(time);
          Iterator fileIt = fileSet.iterator();
          while( fileIt.hasNext() ) {
            String filename = (String) fileIt.next();
            // Detect a time window that is too long.
            if (CCVisu.getVerbosityLevel() >= 1 && tmpFilesSeen.contains(filename)) {
              System.err.println(
                  "Transaction-recovery warning: Time window might be to wide " + endl
                + "(currently '" + timeWindow + "' milli-seconds). " + endl
                + "File '" + filename + "' already contained in current transaction."
              );
            }
            tmpFilesSeen.add(filename);

            // Create revision and add revision to resulting list.
            Revision revision = new Revision();
            revision.relName = "CO-CHANGE";
            revision.filename = filename;
            revision.time = time;
            revision.user = user;
            revision.logmsg = logmsg;
            revision.transaction = transaction;
            revSet.add(revision);
          }
        }
      }
//    }
    }
    return result;
  }

};

