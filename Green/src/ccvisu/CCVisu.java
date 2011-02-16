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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/*****************************************************************
 * Main class of the CCVisu package.
 * Contains the main pogram and some auxiliary methods.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class CCVisu {

  /** End of line.*/
  public final static String endl = System.getProperty("line.separator");

  /** Global options **/
  private static boolean hideSource;
  private static int     verbosityLevel;

  // Format identifiers.
  /** CVS log format (only input).*/
  public final static int CVS  = 0;
  /** Graph (relation) in relational standard format.*/
  public final static int RSF  = 1;
  /** Graph layout in textual format.*/
  public final static int LAY  = 2;
  /** Graph layout in VRML format (only output).*/
  public final static int VRML = 3;
  /** Graph layout in SVG format (only output).*/
  public final static int SVG  = 4;
  /** Display graph layout on screen (only output).*/
  public final static int DISP = 5;

  // Marker.
  // Emphasize, i.e., add annotation of vertex name for some vertices.
  // Change to a subclass of Marker.
  public static Marker marker = new Marker();
  
  /*****************************************************************
   * Main program. Performs the following steps.
   * 1) Parses and handles the command line options.
   * 2) Creates the appropriate input reader and reads the input.
   * 3) Computes the layout (if necessary).
   * 4) Creates the appropriate output writer and writes the output.
   * @param args  Command line arguments.
   *****************************************************************/
  public static void main(String[] args) {
    if (args.length == 0) {
      printHelp();
      System.exit(0);
    }
    // Default I/O.
    BufferedReader in  = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter    out = new PrintWriter(new BufferedWriter(
                             new OutputStreamWriter(System.out)));
    // General.
    CCVisu.verbosityLevel  = 0;
    // Input format
    int inFormat  = RSF;
    String inputName = "";  // Empty string for no input file name (standard input).
    // Output format.
    int outFormat = DISP;

    // For CVS reader. Time constant for sliding window.
    int timeWindow = 180000;
    boolean sliding = false;
    
    // For layout.
    int            nrDim               = 2;
    int            nrIterations        = 100;
    GraphData      initialLayout       = null;
    boolean        fixedInitPos        = false;
    
    // For energy model.
    // Exponent of the Euclidian distance in the attraction term of the energy (default: 1).
    float   attrExponent = 1.0f;
    // Exponent of the Euclidian distance in the repulsion term of the energy (default: 0).
    float   repuExponent = 0.0f;
    boolean vertRepu     = false;
    boolean noWeight     = false;
    float   gravitation  = 0.001f;

    // For layout output.
    CCVisu.hideSource   = false;
    float   minVert     = 2.0f;
    int     fontSize    = 14;
    Color   backColor   = Color.WHITE;
    boolean blackCircle = true;
    boolean showEdges   = false;
    float   scalePos    = 1.0f;
    // If true, the layout is already displayed while the minimizer is still improving it,
    // and a simple mouse click on the canvas updates the current layout on the screen.
    // If false, the layout is displayed only after minimization is completed.
    boolean anim        = true;
    boolean annotAll    = false;
    boolean annotNone   = false;
    boolean URL   = false;
    String browser   = null;

    // Parse command-line options.
    for (int i = 0; i < args.length; ++i) {
      // General options without argument.
      // Help.
      if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
        printHelp();
        out.close();
        System.exit(0);
      }
      // Version.
      else if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("--version")) {
        printVersion();
        out.close();
        System.exit(0);
      }
      // Quiet.
      else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--nowarnings")) {
        CCVisu.verbosityLevel = 0;
      }
      // Warnings on.
      else if (args[i].equalsIgnoreCase("-w") || args[i].equalsIgnoreCase("--warnings")) {
        CCVisu.verbosityLevel = 1;
      }
      // Verbose.
      else if (args[i].equalsIgnoreCase("-verbose")) {
        CCVisu.verbosityLevel = 2;
      }

      // General options with argument.
      // Change input reader.
      else if (args[i].equalsIgnoreCase("-i")) {
        ++i;
        chkAvail(args, i);
        try {
          inputName = args[i];
          in = new BufferedReader(new FileReader(args[i]));
        }
        catch (Exception e) {
          System.err.println("Exception while opening file '" + args[i] + "' for reading: ");
              System.err.println(e);
              System.exit(1);
        }
      }
      // Change output writer.
      else if (args[i].equalsIgnoreCase("-o")) {
        ++i;
        chkAvail(args, i);
        try {
          out = new PrintWriter(new BufferedWriter(new FileWriter(args[i])));
        }
        catch (Exception e) {
          System.err.println("Exception while opening file '" + args[i] + "' for writing: ");
          System.err.println(e);
          System.exit(1);
        }
      }
      // Input format.
      else if (args[i].equalsIgnoreCase("-inFormat")) {
        ++i;
        chkAvail(args, i);
        inFormat = getFormat(args[i]);
        if (inFormat > LAY) {
          System.err.println("Usage error: '" + args[i] + "' is not supported as input format.");
          System.exit(1);
        }
      }
      // Output format.
      else if (args[i].equalsIgnoreCase("-outFormat")) {
        ++i;
        chkAvail(args, i);
        outFormat = getFormat(args[i]);
        if (outFormat < RSF) {
          System.err.println("Usage error: '" + args[i] + "' is not supported as output format.");
          System.exit(1);
        }
      }

      // Options for CVS reader.
      // Time-constant of sliding-window for change transaction recovery 
      //   (in milli-seconds).
      else if (args[i].equalsIgnoreCase("-timeWindow")) {
        ++i;
        chkAvail(args, i);
        timeWindow = Integer.parseInt(args[i]);
      }
      
      // sliding/fixed-window for change transaction recovery 
      else if (args[i].equalsIgnoreCase("-slidingTW")) {
        sliding = true;
      }

      // Options for layout.
      // Number of dimensions (up to 3).
      else if (args[i].equalsIgnoreCase("-dim")) {
        ++i;
        chkAvail(args, i);
        nrDim = Integer.parseInt(args[i]);
      }
      // Number of iterations for minimization.
      else if (args[i].equalsIgnoreCase("-iter")) {
        ++i;
        chkAvail(args, i);
        nrIterations = Integer.parseInt(args[i]);
      }
      // Initial layout.
      else if (args[i].equalsIgnoreCase("-initLayout")) {
        ++i;
        chkAvail(args, i);
        BufferedReader initialLayoutStream = null;
        initialLayout = new GraphData();
        try {
          initialLayoutStream = new BufferedReader(new FileReader(args[i]));
        }
        catch (Exception e) {
          System.err.println("Exception while opening file '" + args[i] + "' for reading: ");
          System.err.println(e);
        }
        // Read initial (pre-computed) layout from file.
        (new ReaderDataLAY(initialLayoutStream)).read(initialLayout);
        if (CCVisu.verbosityLevel >= 2) {
          System.err.println("" + initialLayout.vertices.size() + " vertices read."); 
          System.err.println("Initial layout reading finished.");
        }
        // Close the input file.
        try {
          initialLayoutStream.close();
        } catch (Exception e) {
          System.err.println("Exception while closing input file: ");
          System.err.println(e);
        }
        // Reset vertex degrees, 
        // i.e., use the degrees from the graph and ignore the degree from read layout.
        for (int j = 0; j < initialLayout.vertices.size(); ++j) {
          initialLayout.vertices.get(j).degree = 0;
        }
      }
      // Fixed positions for nodes in the initial layout given by option -initLayout.
      else if (args[i].equalsIgnoreCase("-fixedInitPos")) {
    	fixedInitPos = true;
      }
     
      // Energy model.
      // Attraction exponent.
      else if (args[i].equalsIgnoreCase("-attrExp")) {
        ++i;
        chkAvail(args, i);
        attrExponent = Float.parseFloat(args[i]);
      }
      // Repulsion exponent.
      else if (args[i].equalsIgnoreCase("-repuExp")) {
        ++i;
        chkAvail(args, i);
        repuExponent = Float.parseFloat(args[i]);
      }
      // Node repulsion.
      else if (args[i].equalsIgnoreCase("-vertRepu")) {
        vertRepu = true;
      }
      // No weights.
      else if (args[i].equalsIgnoreCase("-noWeight")) {
        noWeight = true;
      }
      // Gravitation factor.
      else if (args[i].equalsIgnoreCase("-grav")) {
        ++i;
        chkAvail(args, i);
        gravitation = Float.parseFloat(args[i]);
      }

      // Options for output writers.
      // Show source vertices (first vertex of an edge).
      else if (args[i].equalsIgnoreCase("-hideSource")) {
        hideSource = true;
      }
      // Scale circles for vertices in the layout.
      else if (args[i].equalsIgnoreCase("-minVert")) {
        ++i;
        chkAvail(args, i);
        minVert = Float.parseFloat(args[i]);
      }
      // Font size for annotations in the layout.
      else if (args[i].equalsIgnoreCase("-fontSize")) {
        ++i;
        chkAvail(args, i);
        fontSize = Integer.parseInt(args[i]);
      }
      // Background color.
      else if (args[i].equalsIgnoreCase("-backcolor")) {
        ++i;
        chkAvail(args, i);
        if (args[i].equalsIgnoreCase("black")) {
          backColor = Color.BLACK;
        } else if (args[i].equalsIgnoreCase("white")) {
          backColor = Color.WHITE;
                } else if (args[i].equalsIgnoreCase("gray")) {
                  backColor = Color.GRAY;
                } else if (args[i].equalsIgnoreCase("lightgray")) {
                  backColor = Color.LIGHT_GRAY;
        } else {
          System.err.println("Usage error: Color '" + args[i] + "' unknown.");
        }
      }
      // Avoid black circles around the filled circles for vertices (strokes).
      else if (args[i].equalsIgnoreCase("-noBlackCircle")) {
        blackCircle = false;
      }
      
      // Show the Edges
      else if (args[i].equalsIgnoreCase("-showEdges")) {
        showEdges = true;
      }
      
      // Options for VRML writer.
      // Scale positions in the layout.
      else if (args[i].equalsIgnoreCase("-scalePos")) {
        ++i;
        chkAvail(args, i);
        scalePos = Float.parseFloat(args[i]);
      }
      // Only for display writer.
      // Animation of layout during minimization, if outFormat is DISP.
      else if (args[i].equalsIgnoreCase("-noAnim")) {
        anim = false;
      }
      // For all.
      // Annotate each vertex with its name.
      else if (args[i].equalsIgnoreCase("-annotAll")) {
        annotAll = true;
      }
      // Annotate no vertex.
      else if (args[i].equalsIgnoreCase("-annotNone")) {
        annotNone = true;
      }
      //Allow to open an the URLs
      else if (args[i].equalsIgnoreCase("-openURL")) {
          URL = true;
      }
      //the browser cmd
      else if (args[i].equalsIgnoreCase("-browser")) {
          ++i;
          chkAvail(args, i);
          browser = args[i];
      }

      // Switch on user-defined marker.
      // To emphasize certain vertices.
      else if (args[i].equalsIgnoreCase("-mark")) {
    	  System.err.println("Implement marker first.");
    	  System.exit(1);
    	  //marker = new MarkerExp();
      }
      else if (args[i].equalsIgnoreCase("-markScript")) {
          ++i;
          chkAvail(args, i);
          try {
            marker = new MarkerScript(new BufferedReader(new FileReader(args[i])));
          } catch (FileNotFoundException e) {
            System.err.println("Impossible to read file: "+ args[i]);
            marker = new Marker();
            e.printStackTrace();
          }
      }
      
      // Unknown option.
      else {
        System.err.println("Usage error: Option '" + args[i] + "' unknown.");
        System.exit(1);
      }
    } // for parsing command-line options.


    
    if (inFormat > outFormat) {
      System.err.println("Usage error: Combination of input and output formats not supported.");
      System.exit(1);
    }

    // Initialize the graph representation.
    GraphData graph = new GraphData();
    
    // Set input reader.
    ReaderData graphReader;              // Default: CVS log format.
    graphReader = new ReaderDataGraphCVS(in, timeWindow, sliding);
    if (inFormat == RSF) {               // Graph in RSF format.
      graphReader = new ReaderDataGraphRSF(in);
    } else if (inFormat == LAY) {        // Layout in text format LAY.
      graphReader = new ReaderDataLAY(in);
    }
    // Read the data using the reader (i.e., fill into existing graph structure).
    graphReader.read(graph);
    if (CCVisu.verbosityLevel >= 2) {
      System.err.println("" + graph.vertices.size() + " vertices read."); 
      System.err.println("Graph reading finished.");
    }
    // Close the input file.
    try {
      in.close();
    } catch (Exception e) {
      System.err.println("Exception while closing input file: ");
      System.err.println(e);
    }

    // Handle vertex options.
    for (int i = 0; i < graph.vertices.size(); ++i) {
      GraphVertex curVertex = graph.vertices.get(i);
      // annotAll (annotate each vertex with its name).
      if (annotAll) {
        curVertex.showName = true;
      }
      // annotNone (annotate no vertex).
      if (annotNone) {
        curVertex.showName = false;
      }
      // hideSource (do not show vertex if it is source of an edge).
      if (hideSource && curVertex.isSource) {
        curVertex.showVertex = false;
      }
    }
    
    // Output writer.
    WriterData dataWriter = null;

    // Determine if it is necessary to compute the layout.
    if (inFormat < LAY && outFormat >= LAY) {
      // Initialize layout.
      initializeLayout(graph, nrDim, initialLayout);
      // Show display for layout animation during minimization.
      if (outFormat == DISP && anim) {   // Display layout on screen already now.
        dataWriter = new WriterDataGraphicsDISP(graph, minVert, fontSize, backColor,
                                                blackCircle, showEdges, URL, inputName,
                                                browser);
      }
      
      
      // Compute the layout.
      if(dataWriter == null){
          //no animation
          computeLayout(graph, nrIterations, attrExponent, repuExponent, 
                        vertRepu, noWeight, gravitation, 
                        initialLayout, fixedInitPos, null);
      }else{
          //animation
          WriterDataGraphicsDISP displ = (WriterDataGraphicsDISP) dataWriter;
          GraphEventListener listener = displ.getDisplay();
          computeLayout(graph, nrIterations, attrExponent, repuExponent, 
                        vertRepu, noWeight, gravitation, 
                        initialLayout, fixedInitPos, listener);
      }
      
    }

    // Set output writer.
    if (outFormat == RSF) {                               // Co-change graph in RSF.
      dataWriter = new WriterDataRSF (graph, out);
    } else if (outFormat == LAY) {                        // Layout in text format LAY.
      dataWriter = new WriterDataLAY (graph, out);
    } else if (outFormat == VRML) {                       // Layout in VRML format.
      dataWriter = new WriterDataGraphicsVRML(graph, out, minVert, fontSize, backColor,
                                              blackCircle, showEdges, URL, scalePos);
    } else if (outFormat == SVG) {                        // Layout in SVG format.
      dataWriter = new WriterDataGraphicsSVG (graph, out, minVert, fontSize, backColor,
                                              blackCircle, showEdges, URL, scalePos, inputName);
    } else if (outFormat == DISP && dataWriter == null) { // Display layout on screen
      // ... if the view is not already there, i.e. animation is not activated.
      dataWriter = new WriterDataGraphicsDISP(graph, minVert, fontSize, backColor,
                                              blackCircle, showEdges, URL, 
                                              inputName, browser);
    }
        
    // Write the data using the writer.
    dataWriter.write();
    // Close the output file.
    out.close();
  }

  /*****************************************************************
   * Prints version information.
   *****************************************************************/
  private static void printVersion() {
    System.out.println(
        "CCVisu 2.1, 2007-12-12. " + endl
      + "Copyright (C) 2005-2007  Dirk Beyer (SFU, B.C., Canada). " + endl
      + "CCVisu is free software, released under the GNU LGPL. ");
  }

  /*****************************************************************
   * Prints usage information.
   *****************************************************************/
  private static void printHelp() {
    // Usage and info message.
    System.out.print( 
        endl
      + "This is CCVisu, a tool for visual graph clustering " + endl
      + "and general force-directed graph layout. " + endl
      + "   " + endl
      + "Usage: java ccvisu.CCVisu [OPTION]... " + endl
      + "Compute a layout for a given (co-change) graph (or convert). " + endl
      + "   " + endl
      + "Options: " + endl
      + "General options: " + endl
      + "   -h  --help        display this help message and exit. " + endl
      + "   -v  --version     print version information and exit. " + endl
      + "   -q  --nowarnings  quiet mode (default). " + endl
      + "   -w  --warnings    enable warnings. " + endl
      + "   -verbose          verbose mode. " + endl
      + "   -i <file>         read input data from given file (default: stdin). " + endl
      + "   -o <file>         write output data to given file (default: stdout). " + endl
      + "   -inFormat FORMAT  read input data in format FORMAT (default: RSF, see below). " + endl
      + "   -outFormat FORMAT write output data in format FORMAT (default: DISP, see below). " + endl
      + "   " + endl
      + "Layouting options: " + endl
      + "   -dim <int>        number of dimensions of the layout (2 or 3, default: 2). " + endl
      + "   -iter <int>       number of iterations of the minimizer (default: 100). " + endl
      + "   -initLayout <file>  use layout from file (LAY format) as initial layout " + endl
      + "                     (default: random layout). " + endl
      + "   " + endl
      + "Energy model options: " + endl
      + "   -attrExp <int>    exponent for the distance in the attraction term " + endl
      + "                     (default: 1). " + endl
      + "   -repuExp <int>    exponent for the distance in the repulsion term " + endl
      + "                     (default: 0). " + endl
      + "   -vertRepu         use vertex repulsion instead of edge repulsion " + endl
      + "                     (default: edge repulsion). " + endl
      + "   -noWeight         use unweighted model (default: weighted). " + endl
      + "   -grav <float>     gravitation factor for the Barnes-Hut-procedure " + endl
      + "                     (default: 0.001). " + endl
      + "   " + endl
      + "CVS reader option: " + endl
      + "   -timeWindow <int> time window for transaction recovery, in milli-seconds " + endl
      + "                     (default: 180'000). " + endl
      + "	-slidingTW		  the time window 'slides': a new commit nodes is created" + endl
      + "					  when the time difference between two commited files is bigger" + endl
      + "					  than the time window (default: fixed time window)." + endl
      + "   " + endl
      + "Layout writer options: " + endl
      + "   -hideSource       draw only vertices that are not source of an edge. " + endl
      + "                     In co-change graphs, all change-transaction vertices  " + endl
      + "                     are source vertices (default: no hide). " + endl
      + "   -minVert <float>  size of the smallest vertex (diameter, default: 2.0). " + endl
      + "   -fontSize <int>   font size of vertex annotations (default: 14). " + endl
      + "   -backColor COLOR  background color (default: WHITE). " + endl
      + "                     Colors: BLACK, GRAY, LIGHTGRAY, WHITE." + endl
      + "   -noBlackCircle    no black circle around each vertex (default: with). " + endl
      + "   -showEdges        Show the edges of the graph (available only for CVS and RFS inFomat)" + endl
      + "                     (default: hide)" + endl
      + "   -scalePos <float> scaling factor for the layout to adjust " + endl
      + "                     (VRML and SVG only, default: 1.0). " + endl
      + "   -noAnim           don't  show layout while minimizer is still improving it " + endl
      + "                     (default: show). " + endl
      + "   -annotAll         annotate each vertex with its name (default: no). " + endl
      + "   -annotNone        annotate no vertex (default: no). " + endl
      + "   -mark             highlight vertices using the MarkerExp class " + endl
      + "                     (see source code for more details)" + endl
      + "   -markScript<file> highlight vertices using condition parsed from a file " + endl
      + "                     (see file marker_script.example to see how it works)" + endl
      + "   -openURL          The node's name can be considered as URL and opened in a web Broswer. " + endl
      + "                     This option used with DISP output require to hold CTRL KEY while clicking" + endl
      + "   " + endl
      + "DISP specific option" + endl
      + "   -browser <Cmd>    The browser command. if not available, CCVisu will try to guess." + endl
      + "   " + endl
      + "Formats: " + endl
      + "   CVS               CVS log format (only input). " + endl
      + "   RSF               graph in relational standard format. " + endl
      + "   LAY               graph layout in textual format. " + endl
      + "   VRML              graph layout in VRML format (only output). " + endl
      + "   SVG               graph layout in SVG format (only output). " + endl
      + "   DISP              display gaph layout on screen (only output). " + endl
      + "To produce a file for input format CVS log, use e.g. 'cvs log -Nb'. " + endl
      + "   " + endl
      + "http://www.cs.sfu.ca/~dbeyer/CCVisu/ " + endl
      + "   " + endl
      + "Report bugs to Dirk Beyer <firstname.lastname@sfu.ca>. " + endl
      + "   " + endl
          );
  }

  /*****************************************************************
   * Transforms the format given as a string into the appropriate integer value.
   * @param format  File format string to be transformed to int.
   * @return        File format identifier.
   *****************************************************************/
  private static int getFormat(String format) {
    int result = 0;
    if (format.equalsIgnoreCase("CVS")) {
      result = CVS;
    } else if (format.equalsIgnoreCase("RSF")) {
      result = RSF;
    } else if (format.equalsIgnoreCase("LAY")) {
      result = LAY;
    } else if (format.equalsIgnoreCase("VRML")) {
      result = VRML;
    } else if (format.equalsIgnoreCase("SVG")) {
      result = SVG;
    } else if (format.equalsIgnoreCase("DISP")) {
      result = DISP;
    } else {
      System.err.println("Usage error: '" + format + "' is not a valid format.");
      System.exit(1);
    }
    return result;
  }

  /*****************************************************************
   * Checks whether the command line argument at index i has a follower argument.
   * If there is no follower argument, it exits the program.
   * @param args  String array containing the command line arguments.
   * @param i     Index to check.
   *****************************************************************/
  private static void chkAvail(String[] args, int i) {
    if (i == args.length) {
      System.err.println("Usage error: Option '" + args[i-1] 
                         + "' requires an argument (file).");
      System.exit(1);
    }
  }

  /*****************************************************************
   * Compute randomized initial layout for a given graph 
   * with the given number of dimensions.
   * @param graph  Graph representation, in/out parameter.
   * @param nrDim  Number of dimensions for the initial graph.
   * @param initialLayout  Initial layout representaiton as read from file.
   *****************************************************************/
  public static void initializeLayout(GraphData graph, 
                                      int nrDim,
                                      GraphData initialLayout) {
    // Initialize with random positions.
    graph.pos = new float[graph.vertices.size()][3];
    for (int i = 0; i < graph.vertices.size(); ++i) {
      graph.pos[i][0] = 2 * (float) Math.random() - 1;
      
      if (nrDim >= 2) {
        graph.pos[i][1] = 2 * (float) Math.random() - 1;
      } else {
        graph.pos[i][2] = 0;
      }
      
      if (nrDim == 3) {
        graph.pos[i][2] = 2 * (float) Math.random() - 1;
      } else {
        graph.pos[i][2] = 0;
      }
    }
    
    // Copy positions from the initial layout that was read from file.
    if (initialLayout != null) {
      for (int i = 0; i < graph.vertices.size(); ++i) {
        GraphVertex curVertex = graph.vertices.get(i);
        GraphVertex oldVertex = initialLayout.nameToVertex.get(curVertex.name);
        if (oldVertex != null) {
          graph.pos[i][0] = initialLayout.pos[oldVertex.id][0];
          graph.pos[i][1] = initialLayout.pos[oldVertex.id][1];
          graph.pos[i][2] = initialLayout.pos[oldVertex.id][2];
        }
      }
    }
    //mark the nodes
    mark(marker,graph);
  }

  /*****************************************************************
   * Compute layout for a given graph.
   * @param graph         In/Out parameter representing the graph.
   * @param nrIterations  Number of iterations.
   * @param attrExponent  Exponent of the Euclidian distance in the attraction term
   *                      of the energy (default: 1).
   * @param vertRepu      Use vertex repulsion instead of edge repulsion,
   *                      true for vertex repulsion, false for edge repulsion
   *                      (default: edge repulsion).
   * @param noWeight      Use unweighted model by ignoring the edge weights,
   *                      true for unweighted, false for weighted 
   *                      (default: weighted).
   * @param gravitation   Gravitation factor for the Barnes-Hut-procedure,
   *                      attraction to the barycenter
   *                      (default: 0.001).
   * @param listener      A listener that to the graph's changes
   *****************************************************************/
  public static void computeLayout(GraphData graph, 
                                   int nrIterations, 
                                   float attrExponent, 
                                   float repuExponent, 
                                   boolean vertRepu,
                                   boolean noWeight,
                                   float gravitation,
                                   GraphData initialLayout,
                                   boolean fixedInitPos,
                                   GraphEventListener listener) {

    // Create graph layout data structure, allocate memory.
    int verticeNr = graph.vertices.size();

    // Positions are already initialized.

    // Initialize repulsions.
    float[] repu = new float[verticeNr];
    for (int i = 0; i < verticeNr; ++i) {
      // Set repulsion according to the energy model.
      if (vertRepu) {
        repu[i] = 1.0f;
      } else {
        GraphVertex curVertex = graph.vertices.get(i);
        repu[i] = curVertex.degree;
      }
    }
    

    // Initialize attractions.
    // Vertex indexes of the similarity lists. 
    int[][] attrIndexes = new int[verticeNr][];
    // Similarity values of the similarity lists.
    float[][] attrValues = new float[verticeNr][];
    {
      // Compute length of row lists.
      int[] attrCounter = new int[verticeNr];
      for (int i = 0; i < graph.edges.size(); ++i) {
        GraphEdgeInt e = graph.edges.get(i);
        if (e.x == e.y) {
          if (CCVisu.verbosityLevel >= 1) {
            GraphVertex curVertex = graph.vertices.get(e.x);
            System.err.println("Layout warning: Reflexive edge for vertex '" + 
                               curVertex.name + "' found." );
          }
        } else {
          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }
      
      // Allocate the rows.
      for (int i = 0; i < verticeNr; i++) {
        attrIndexes[i] = new int[attrCounter[i]];
        attrValues[i] = new float[attrCounter[i]];
      }

      // Transfer the edges to the similarity lists.
      attrCounter = new int[verticeNr];
      for (int i = 0; i < graph.edges.size(); ++i) {
        GraphEdgeInt e = graph.edges.get(i);
        if (e.x != e.y) {
          // Similarity list must be symmetric.
          attrIndexes[e.x][attrCounter[e.x]] = e.y;
          attrIndexes[e.y][attrCounter[e.y]] = e.x;
          // Set similarities according to the energy model.
          if (noWeight) {
            attrValues[e.x][attrCounter[e.x]] = 1.0f;
            attrValues[e.y][attrCounter[e.y]] = 1.0f;
          } else {
            attrValues[e.x][attrCounter[e.x]] = e.w;
            attrValues[e.y][attrCounter[e.y]] = e.w;
          }
          
          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }
      
    }
    
    // fixedPos[i] == true means that the minimizer does not change 
    //   the i-th vertex's position.
    boolean[] fixedPos = new boolean[verticeNr];
    if (fixedInitPos && initialLayout != null) {
      for (int i = 0; i < verticeNr; i++) {
        // If the current vertex exists in the read initial layout,
    	// then fix its position.
    	GraphVertex curVertex = graph.vertices.get(i);
        if (initialLayout.nameToVertex.containsKey(curVertex.name) ) {
          fixedPos[i] = true;
        }
      }
    }

    
    // Set minimizing algorithm. So far there is only one implemented in CCVisu.
    Minimizer minimizer 
      = new MinimizerBarnesHut(verticeNr, attrIndexes, attrValues, 
                               repu, graph.pos, fixedPos,
                               attrExponent, repuExponent, gravitation);
    
    //Add GraphEventListener.
    if(listener != null){
        minimizer.addGraphEventListener(listener);
    }
    
    // Compute layout.
    minimizer.minimizeEnergy(nrIterations);
  }
  
  /*****************************************************************
   * Compute layout for a given graph.
   * @param graph         In/Out parameter representing the graph.
   * @param nrIterations  Number of iterations.
   * @param attrExponent  Exponent of the Euclidian distance in the attraction term
   *                      of the energy (default: 1).
   * @param vertRepu      Use vertex repulsion instead of edge repulsion,
   *                      true for vertex repulsion, false for edge repulsion
   *                      (default: edge repulsion).
   * @param noWeight      Use unweighted model by ignoring the edge weights,
   *                      true for unweighted, false for weighted 
   *                      (default: weighted).
   * @param gravitation   Gravitation factor for the Barnes-Hut-procedure,
   *                      attraction to the barycenter
   *                      (default: 0.001).
   * @param listener      A listener that to the graph's changes
   *****************************************************************/
  public static Minimizer computeLayout(GraphData graph, 
                                   int nrIterations, 
                                   float attrExponent, 
                                   float repuExponent, 
                                   boolean vertRepu,
                                   boolean noWeight,
                                   float gravitation,
                                   GraphData initialLayout,
                                   boolean fixedInitPos) {

    // Create graph layout data structure, allocate memory.
    int verticeNr = graph.vertices.size();

    // Positions are already initialized.

    // Initialize repulsions.
    float[] repu = new float[verticeNr];
    for (int i = 0; i < verticeNr; ++i) {
      // Set repulsion according to the energy model.
      if (vertRepu) {
        repu[i] = 1.0f;
      } else {
        GraphVertex curVertex = graph.vertices.get(i);
        repu[i] = curVertex.degree;
      }
    }
    

    // Initialize attractions.
    // Vertex indexes of the similarity lists. 
    int[][] attrIndexes = new int[verticeNr][];
    // Similarity values of the similarity lists.
    float[][] attrValues = new float[verticeNr][];
    {
      // Compute length of row lists.
      int[] attrCounter = new int[verticeNr];
      for (int i = 0; i < graph.edges.size(); ++i) {
        GraphEdgeInt e = graph.edges.get(i);
        if (e.x == e.y) {
          if (CCVisu.verbosityLevel >= 1) {
            GraphVertex curVertex = graph.vertices.get(e.x);
            System.err.println("Layout warning: Reflexive edge for vertex '" + 
                               curVertex.name + "' found." );
          }
        } else {
          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }
      
      // Allocate the rows.
      for (int i = 0; i < verticeNr; i++) {
        attrIndexes[i] = new int[attrCounter[i]];
        attrValues[i] = new float[attrCounter[i]];
      }

      // Transfer the edges to the similarity lists.
      attrCounter = new int[verticeNr];
      for (int i = 0; i < graph.edges.size(); ++i) {
        GraphEdgeInt e = graph.edges.get(i);
        if (e.x != e.y) {
          // Similarity list must be symmetric.
          attrIndexes[e.x][attrCounter[e.x]] = e.y;
          attrIndexes[e.y][attrCounter[e.y]] = e.x;
          // Set similarities according to the energy model.
          if (noWeight) {
            attrValues[e.x][attrCounter[e.x]] = 1.0f;
            attrValues[e.y][attrCounter[e.y]] = 1.0f;
          } else {
            attrValues[e.x][attrCounter[e.x]] = e.w;
            attrValues[e.y][attrCounter[e.y]] = e.w;
          }
          
          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }
      
    }
    
    // fixedPos[i] == true means that the minimizer does not change 
    //   the i-th vertex's position.
    boolean[] fixedPos = new boolean[verticeNr];
    if (fixedInitPos && initialLayout != null) {
      for (int i = 0; i < verticeNr; i++) {
        // If the current vertex exists in the read initial layout,
    	// then fix its position.
    	GraphVertex curVertex = graph.vertices.get(i);
        if (initialLayout.nameToVertex.containsKey(curVertex.name) ) {
          fixedPos[i] = true;
        }
      }
    }

    
    // Set minimizing algorithm. So far there is only one implemented in CCVisu.
    Minimizer minimizer 
      = new MinimizerBarnesHut(verticeNr, attrIndexes, attrValues, 
                               repu, graph.pos, fixedPos,
                               attrExponent, repuExponent, gravitation);
    
    //Add GraphEventListener.
    //if(listener != null){
    //    minimizer.addGraphEventListener(listener);
    //}
    
    // Compute layout.
    return minimizer;//.minimizeEnergy(nrIterations);
  }

  /*****************************************************************
   * Get value of option hideSource. 
   * @return   Value of option hideSource.
   *****************************************************************/
  public static boolean getHideSource() {
        return hideSource;
  }

  /*****************************************************************
   * Get value of option verbosityLevel. 
   * @return   Value of option verbosityLevel.
   *****************************************************************/
  public static int getVerbosityLevel() {
        return verbosityLevel;
  }
  
  /**
   * Highlight nodes
   * @param m the marker to use
   * @param graph the graph to mark
   */
  public static void mark(Marker m, GraphData graph){
      int end = graph.vertices.size();
      for(int i = 0; i < end; ++i){
          GraphVertex curVertex = graph.vertices.get(i);
          m.mark(curVertex);
      }
  }

  public final static Color white     = new Color(255, 255, 255);
  public final static Color lightGray = new Color(192, 192, 192);
  public final static Color gray      = new Color(128, 128, 128);
  public final static Color darkGray  = new Color(64, 64, 64);
  public final static Color black 	  = new Color(0, 0, 0);
  
  public final static Color red       = new Color(255, 0, 0);
  public final static Color green 	  = new Color(0, 255, 0);
  public final static Color blue 	  = new Color(0, 0, 255);

  public final static Color yellow 	  = new Color(255, 255, 0);
  public final static Color magenta	  = new Color(255, 0, 255);
  public final static Color cyan 	  = new Color(0, 255, 255);
  
  public final static Color lightRed        = new Color(255, 128, 128);
  public final static Color lightGreen 	    = new Color(128, 255, 128);
  public final static Color lightBlue 	    = new Color(128, 128, 255);

  public final static Color darkYellow 	    = new Color(128, 128, 0);
  public final static Color darkMagenta	    = new Color(128, 0, 128);
  public final static Color darkCyan 	    = new Color(0, 128, 128);
  
  public final static Color pink            = new Color(255, 175, 175);
  public final static Color orange 	        = new Color(255, 200, 0);
  
  public final static Color chocolate4      = new Color(139, 69, 19);
  public final static Color darkOliveGreen4 = new Color(110, 139, 61);	
};

