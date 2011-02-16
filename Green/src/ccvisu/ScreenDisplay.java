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

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;

/*****************************************************************
 * Frame implementation for displaying the layout on the screen device.
 * Used by WriterDataGraphicsDISP.
 * @version  $Revision$; $Date$
 * @author   Dirk Beyer
 *****************************************************************/
public class ScreenDisplay  extends Frame
                            implements GraphEventListener {
  private static final long serialVersionUID = 200510192213L;

  /*****************************************************************
   * Canvas implementation for displaying the layout on the screen.
   * @author   Dirk Beyer
   *****************************************************************/
  private class MyCanvas extends Canvas {
    private static final long serialVersionUID = 200510192212L;
            
    private Frame parent;
    private WriterDataGraphicsDISP writer;
    
    // image used for off-screen work
    private BufferedImage img;
    // dimension of the image used
    private Dimension size = new Dimension(0,0);
    
    private Label  vertexName;
    private Choice fontSizeChoice;
    private Choice minVertChoice;
    private Choice colorChoice;
    // normal vertex color
    private Color chosenColor = CCVisu.green;
    private TextField markerRegExTextField;
    private Button saveButton;
    private Button loadButton;
    
    // Coordinates for zooming rectangle.
    private Point rectTopLeft;
    private Point rectBottomRight;
    private boolean rectShow;
    
    // Coordinates of the mouse when MOUSE_PRESSED.
    private int mouseX;
    private int mouseY;
    private int tolerance;
    
    /**
     * Constructor.
     * @param parent The parent frame.
     * @param writer The writer that uses this object to draw on.
     *               The painting is delegated to the writer object.
     */
    private MyCanvas(ScreenDisplay parent,
                     WriterDataGraphicsDISP writer) {
      this.parent     = parent;
      this.writer     = writer;
      rectTopLeft     = new Point(0, 0);
      rectBottomRight = new Point(0, 0);
      rectShow        = false;
    }

    /*****************************************************************
     * Draws the layout on the screen.
     * @param area  The graphics area for drawing.
     *****************************************************************/
    public void paint(Graphics area) {
      // Size info.
      setSize(parent.getSize());
      int xSize = getSize().width  - getInsets().left - getInsets().right;
      int ySize = getSize().height - getInsets().top  - getInsets().bottom;

      if(xSize != size.width || ySize != size.height || img == null){
          update();
      }
      
      //draw img on area
      area.drawImage(img, 0, 0, null);
      
      // Zooming rectangle.
      if (rectShow) {
        int x = (int) rectTopLeft.getX();
        int y = (int) rectTopLeft.getY();
        int width = (int) (rectBottomRight.getX() - rectTopLeft.getX());
        int height = (int) (rectBottomRight.getY() - rectTopLeft.getY());
        if ( width < 0 ) {
          width = Math.abs(width);
          x = (int) rectBottomRight.getX();
        }
        if ( height < 0 ) {
          height = Math.abs(height);
          y = (int) rectBottomRight.getY();
        }
        area.drawRect(x, y, width, height);
      }
    } // method paint
    
    /**
     * update the image used to refresh the screen
     */
    public void update(){
        setSize(parent.getSize());
        int xSize = getSize().width  - getInsets().left - getInsets().right;
        int ySize = getSize().height - getInsets().top  - getInsets().bottom;

        if(xSize != size.width || ySize != size.height || img == null){
            size = new Dimension(xSize,ySize);
            img = new BufferedImage(size.width,size.height,
                                    BufferedImage.TYPE_INT_RGB);
        }
        
        
        //WARNING GCJ: Graphics2D needs  gcc 4
        Graphics2D graphimg = (Graphics2D)img.getGraphics();
        
        //set some rendering preferences
        graphimg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphimg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        //set the font
        graphimg.setFont(new Font("SansSerif", Font.BOLD, writer.fontSize ));
        
        //fill background
        graphimg.setColor(this.getBackground());
        graphimg.fillRect(0, 0, size.width, size.height);
        graphimg.setColor(Color.BLACK);
        
        writer.writeDISP(Math.min(xSize,ySize), 
                         graphimg, 
                         size.width,
                         size.height, 
                         getInsets().left,
                         getInsets().bottom);
        
        //free some resources
        graphimg.dispose();
    }//method update
    
    
    /**
     * to use when changes are done and you want to display them
     */
    public void updateAndPaint(){
        this.updateWait();
        this.update();
        this.repaint();
    }
    
    /**
     * override standard => no more flickering
     */
    public void repaint(){
        paint(this.getGraphics());
    }
    
    /**
     * write "Refreshing..." in the center of the canvas
     */
    private void updateWait(){
        Graphics g = this.getGraphics();
        g.setFont(new Font("SansSerif", Font.ITALIC, 30));
        
        //computing position
        int x = this.getWidth()/2 -70;
        int y = this.getHeight()/2;
        //drawing
        g.setColor(canvas.writer.getWriteColor());
        g.drawString("Refreshing...", x, y);
    }
    
  }; // class MyCanvas
  
  /** Canvas for graphics.*/
  private MyCanvas canvas;

  /** part in charge of managing the clusters*/
  private ClusterManager clusters;
  
  private Dialog vertexNameDialog;
  
  private Checkbox edgesCheck;
  
  /**
   * Constructor.
   * @param writer      The writer that uses this frame to display the layout.
   */
  public ScreenDisplay(WriterDataGraphicsDISP writer) {
      
    
    // The frame.
    setTitle("Visualization " + writer.inputName);     // Set window title.
    setLocation(50,50);                         // Set initial window position.
    setSize(700, 700);                          // Set windows size.

    
    // The graphics canvas.
    canvas = new MyCanvas(this, writer);
    canvas.setBackground(writer.backColor);
    // Add canvas to the frames content pane.
    add(canvas);
    
    // The dialog (extra window), for the vertex names, etc.
    vertexNameDialog = new Dialog(this);
    vertexNameDialog.setTitle("Visualization control panel");
    vertexNameDialog.setLocation(800,50);
    vertexNameDialog.setResizable(true);
    
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    vertexNameDialog.setLayout(lay);
    
    
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridx = 0;
    int y = 0;
    
    constraints.gridy = y;
    constraints.anchor = GridBagConstraints.NORTH;
    //constraints.fill = GridBagConstraints.HORIZONTAL;
    Panel buttonPanel = new Panel();
    vertexNameDialog.add(buttonPanel, constraints);

    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    //constraints.fill = GridBagConstraints.NONE;
    Panel sizePanel = new Panel();
    vertexNameDialog.add(sizePanel, constraints);
    
    constraints.gridy = ++y;
    Panel renderPanel = new Panel();
    vertexNameDialog.add(renderPanel, constraints);
    
    constraints.gridy = ++y;
    Panel colorPanel = new Panel();
    vertexNameDialog.add(colorPanel, constraints);
    
    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.NORTH;
    //constraints.fill = GridBagConstraints.HORIZONTAL;
    Panel fileButtonPanel = new Panel();
    vertexNameDialog.add(fileButtonPanel, constraints);
    
    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    //constraints.fill = GridBagConstraints.NONE;
    Panel searchPanel = new Panel();
    vertexNameDialog.add(searchPanel, constraints);
    
    //Button Panel
    Button resetZoomButton = new Button("Reset zoom selection");
    buttonPanel.add(resetZoomButton);

    Button showAllLabelsButton = new Button("Show all labels");
    buttonPanel.add(showAllLabelsButton);

    Button hideAllLabelsButton = new Button("Hide all labels");
    buttonPanel.add(hideAllLabelsButton);

    //Size Panel
    
    canvas.fontSizeChoice = new Choice();
    canvas.fontSizeChoice.add("" + writer.fontSize);
    canvas.fontSizeChoice.add("6");
    canvas.fontSizeChoice.add("8");
    canvas.fontSizeChoice.add("10");
    canvas.fontSizeChoice.add("12");
    canvas.fontSizeChoice.add("14");
    canvas.fontSizeChoice.add("16");
    canvas.fontSizeChoice.add("18");
    canvas.fontSizeChoice.add("20");
    canvas.fontSizeChoice.add("22");
    canvas.fontSizeChoice.add("24");
    canvas.fontSizeChoice.add("26");
    canvas.fontSizeChoice.add("28");
    canvas.fontSizeChoice.add("30");
    sizePanel.add(new Label("Font size:"));
    sizePanel.add(canvas.fontSizeChoice);

    canvas.minVertChoice = new Choice();
    canvas.minVertChoice.add("" + writer.minVert);
    canvas.minVertChoice.add("1");
    canvas.minVertChoice.add("2");
    canvas.minVertChoice.add("3");
    canvas.minVertChoice.add("4");
    canvas.minVertChoice.add("5");
    canvas.minVertChoice.add("6");
    canvas.minVertChoice.add("7");
    canvas.minVertChoice.add("8");
    canvas.minVertChoice.add("9");
    canvas.minVertChoice.add("10");
    canvas.minVertChoice.add("15");
    canvas.minVertChoice.add("20");
    sizePanel.add(new Label("   Min vertex size:"));
    sizePanel.add(canvas.minVertChoice);
    

    //Color Panel
    Button setColor = new Button("Set to all vertices");
    
    canvas.colorChoice = new Choice();
    canvas.colorChoice.add("red");
    canvas.colorChoice.add("green");
    canvas.colorChoice.add("blue");
    canvas.colorChoice.add("yellow");
    canvas.colorChoice.add("magenta");
    canvas.colorChoice.add("cyan");
    canvas.colorChoice.add("light red");
    canvas.colorChoice.add("light green");
    canvas.colorChoice.add("light blue");
    canvas.colorChoice.add("dark yellow");
    canvas.colorChoice.add("dark magenta");
    canvas.colorChoice.add("dark cyan");
    canvas.colorChoice.add("pink");
    canvas.colorChoice.add("white");
    canvas.colorChoice.add("light gray");
    canvas.colorChoice.add("gray");
    canvas.colorChoice.add("dark gray");
    canvas.colorChoice.add("black");
    
    canvas.colorChoice.setName("normal");
    canvas.colorChoice.select("green");
    
    colorPanel.add(new Label("Vertice color:"));
    colorPanel.add(canvas.colorChoice);
    colorPanel.add(setColor);
    
    //Render Panel
    Checkbox circleCheck = new Checkbox("Black Circle");
    circleCheck.setState(writer.blackCircle);
    edgesCheck = new Checkbox("Edges");
    edgesCheck.setState(writer.showEdges);
    edgesCheck.setEnabled(writer.isshowEdgesPossible());
    renderPanel.add(circleCheck);
    renderPanel.add(edgesCheck);
    
    
    Choice backGroundColor = new Choice();
    backGroundColor.setName("back");
    backGroundColor.add("white");
    backGroundColor.add("light gray");
    backGroundColor.add("gray");
    backGroundColor.add("dark gray");
    backGroundColor.add("black");
    renderPanel.add(new Label("   Background color:"));
    renderPanel.add(backGroundColor);
    
    //File Panel
    canvas.saveButton = new Button("Save layout");
    fileButtonPanel.add(canvas.saveButton);

    canvas.loadButton = new Button("Load layout");
    fileButtonPanel.add(canvas.loadButton);

    Button prevButton = new Button("Prev layout");
    fileButtonPanel.add(prevButton);

    Button nextButton = new Button("Next layout");
    fileButtonPanel.add(nextButton);

    //searchPanel
    Label spaceLabel = new Label("Search:");
    searchPanel.add(spaceLabel);

    canvas.markerRegExTextField = new TextField("", 30);
    searchPanel.add(canvas.markerRegExTextField);
    
    Button advanced = new Button("Advanced");
    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.EAST;
    vertexNameDialog.add(advanced,constraints);
    
    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    vertexNameDialog.add(new Label(
      "Click on vertex to annote with name, click again to remove."), constraints);
    
    constraints.gridy = ++y;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    canvas.vertexName = new Label(
      "Vertex names appear here when mouse is moved.");
    vertexNameDialog.add(canvas.vertexName, constraints);
 
    vertexNameDialog.pack();
    vertexNameDialog.setVisible(true);

    clusters = new ClusterManager(this,writer);
    
    // Show canvas.
    setVisible(true);

    // Adds ActionListener for action event ''Reset Zoom button pressed''.
    // Resets the visibility of all vertices.
    resetZoomButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          canvas.writer.resetRestriction();
          canvas.updateAndPaint();
        }
      }
    );

    // Adds ActionListener for action event ''Show All Labels button pressed''.
    // Display the labels of all vertices.
    showAllLabelsButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          canvas.writer.showAllLabels();
          canvas.updateAndPaint();
        }
      }
    );

    // Adds ActionListener for action event ''Hide All Labels button pressed''.
    // Hide the labels of all vertices.
    hideAllLabelsButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          canvas.writer.hideAllLabels();
          canvas.updateAndPaint();
        }
      }
    );

    // Adds ItemListener for item event.
    // Repaint the layout.
    canvas.minVertChoice.addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
          canvas.writer.minVert = Float.parseFloat(canvas.minVertChoice.getSelectedItem());
          canvas.updateAndPaint();
        }
      }
    );
    
    // Adds ItemListener for item event.
    // Repaint the layout.
    canvas.fontSizeChoice.addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
          canvas.writer.fontSize = Integer.parseInt(canvas.fontSizeChoice.getSelectedItem());
          canvas.updateAndPaint();
        }
      }
    );
    
    //Adds actionListener for action event "Set color pressed".
    // set the color form colorChoice to all vertex and repaint
    setColor.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                canvas.writer.setColorToAll(canvas.chosenColor);
                canvas.updateAndPaint();
            }
    });
    
    //Adds actionListener for action event "Advanced pressed".
    //show the clusterManager
    advanced.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if(!clusters.isShowing()){
                    int y = vertexNameDialog.getY();
                    y += vertexNameDialog.getHeight();
                    y += 30;
                    clusters.setLocation(vertexNameDialog.getX(), y );
                    clusters.setVisible(true);
                }
            }
    });
    
    //Adds ItemListener for item event.
    // change chosenColor 
    ItemListener chooseColor = new ItemListener() {
        public void itemStateChanged(ItemEvent evt) {
            Choice ch = (Choice)evt.getSource();
            //string (not index)
            //possible to change the order without changing this part
            String choice = ch.getSelectedItem();
            Color tmp = null;
            if(choice.equals("red")){
                tmp = CCVisu.red;
            }else if(choice.equals("green")){
                tmp = CCVisu.green;
            }else if(choice.equals("blue")){
                tmp = CCVisu.blue;
            }else if(choice.equals("yellow")){
                tmp = CCVisu.yellow;
            }else if(choice.equals("magenta")){
                tmp = CCVisu.magenta;
            }else if(choice.equals("cyan")){
                tmp = CCVisu.cyan;
            }else if(choice.equals("light red")){
                tmp = CCVisu.lightRed;
            }else if(choice.equals("light green")){
                tmp = CCVisu.lightGreen;
            }else if(choice.equals("light blue")){
                tmp = CCVisu.lightBlue;
            }else if(choice.equals("dark yellow")){
                tmp = CCVisu.darkYellow;
            }else if(choice.equals("dark magenta")){
                tmp = CCVisu.darkMagenta;
            }else if(choice.equals("dark cyan")){
                tmp = CCVisu.darkCyan;
            }else if(choice.equals("pink")){
                tmp = CCVisu.pink;
            }else if(choice.equals("white")){
                tmp = CCVisu.white;
            }else if(choice.equals("light gray")){
                tmp = CCVisu.lightGray;
            }else if(choice.equals("gray")){
                tmp = CCVisu.gray;
            }else if(choice.equals("dark gray")){
                tmp = CCVisu.darkGray;
            }else if(choice.equals("black")){
                tmp = CCVisu.black;
            }else{
                //unknown color
                tmp = CCVisu.black;
            }
            String name = ch.getName();
            if(name.equals("normal")){
                canvas.chosenColor = tmp;
                canvas.writer.getCluster(0).setColor(tmp);
                canvas.updateAndPaint();
            }else if(name.equals("back")){
                canvas.writer.backColor = tmp;
                canvas.writer.adjustFrontColor();
                canvas.setBackground(tmp);
                canvas.updateAndPaint();
            }else{
                System.err.println("error: unknown event source (ScreenDisplay, itemlistener)");
            }
        }
    };
    
    backGroundColor.addItemListener(chooseColor);
    canvas.colorChoice.addItemListener(chooseColor);
    //canvas.colorHighLightChoice.addItemListener(chooseColor);
    
    // Adds ItenListener for item event "circleCheck"
    circleCheck.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent evt){
                    canvas.writer.blackCircle = ((Checkbox)evt.getSource()).getState();
                    canvas.updateAndPaint();
                }
            }
    );
    
    //Adds ItenListener for item event "edgesCheck"
    edgesCheck.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent evt){
                    canvas.writer.setshowEdges(
                            ((Checkbox)evt.getSource()).getState());
                    canvas.updateAndPaint();
                }
            }
    );
    
    // Adds ActionListener for action event ''Save button pressed''.
    // Opens a dialog for choosing the file to which the layout is saved.
    canvas.saveButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          FileDialog fileDialog = new FileDialog(canvas.parent, 
                                                 "Save layout", 
                                                 FileDialog.SAVE);
          fileDialog.setResizable(true);
          fileDialog.setFile(".lay");
          fileDialog.pack();
          fileDialog.setVisible(true);

          if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            canvas.writer.writeFileLayout(fileName);
          }
        }
      }
    );

    // Adds ActionListener for action event ''Load button pressed''.
    // Opens a dialog for choosing the file from which the next layout is loaded.
    canvas.loadButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          FileDialog fileDialog = new FileDialog(canvas.parent, 
                                                 "Load layout", 
                                                 FileDialog.LOAD);
          fileDialog.setFilenameFilter(
            new FilenameFilter() {
              public boolean accept(File dir, String name) {
                System.out.println(dir);
                System.out.println(name);
                if (name.endsWith(".lay")) {
                  return true;
                }
                return false;
              }
            }
          );
          fileDialog.setResizable(true);
          fileDialog.setFile(".lay");
          fileDialog.pack();
          fileDialog.setVisible(true);

          if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            setTitle("Visualization " + fileName);
            BufferedReader in = null;
            GraphData layout = new GraphData();
            try {
              in = new BufferedReader(new FileReader(fileName));
            }
            catch (Exception e) {
              System.err.println("Exception while opening file '" + fileName + "' for reading: ");
              System.err.println(e);
            }
            // Read layout from file.
            (new ReaderDataLAY(in)).read(layout);
            canvas.writer.setGraphData(layout);
            canvas.updateAndPaint();
            // Close the input file.
            try {
              in.close();
            } catch (Exception e) {
              System.err.println("Exception while closing input file: ");
              System.err.println(e);
            }
          }
        }
      }
    );

    // New ActionListener class for next and prev buttons.
    final class NextPrevButtonListener implements ActionListener {
      boolean next; // Otherwise prev.
      private NextPrevButtonListener(boolean next) {
        this.next = next;
      }
      public void actionPerformed(ActionEvent evt) {
    	loadOtherFile(next);
      }
    } // end NextPrevButtonListener.
    // Adds ActionListener for action event ''Next button pressed''.
    // Loads next layout from the directory.
    nextButton.addActionListener(new NextPrevButtonListener(true));
    // Adds ActionListener for action event ''Prev button pressed''.
    // Loads previous layout from the directory.
    prevButton.addActionListener(new NextPrevButtonListener(false));

    // New KeyListener class for loading the next or previous layout from the directory.
    final class MyKeyAdapter extends KeyAdapter {
   	  public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
   	    if (key == KeyEvent.VK_O || key == KeyEvent.VK_LEFT) {
   	  	  loadOtherFile(false);
   	    }else
   	    if (key == KeyEvent.VK_P || key == KeyEvent.VK_RIGHT) {
     	  loadOtherFile(true);
    	}else
   	    if (key == KeyEvent.VK_S) {
   	    	canvas.saveButton.dispatchEvent(
   	    			new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Save")) ;
      	}else
   	    if (key == KeyEvent.VK_L) {
   	    	canvas.loadButton.dispatchEvent(
   	    			new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Load")) ;
      	}
      }
    };
    this.addKeyListener(new MyKeyAdapter());
    canvas.addKeyListener(new MyKeyAdapter());
    

    // Adds KeyListener for action event ''Search text field filled''.
    // Marks the vertices according to the expression.
    canvas.markerRegExTextField.addKeyListener(
      new KeyAdapter() {
    	public void keyPressed(KeyEvent e) {
    	  if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            canvas.writer.markVertices(".*" + canvas.markerRegExTextField.getText() + ".*");
            canvas.updateAndPaint();
         }
        }
      }
    );


    // Adds MouseMotionListener for mouse event ''Mouse moved on vertex''.
    // Show the name(s) of the vertex(vertices) in the vertexNameDialog.
    canvas.addMouseMotionListener(
      new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent evt) {
          // Names in vertexNameDialog.
          canvas.vertexName.setText(canvas.writer.getNames(evt.getPoint()));
        }
        public void mouseDragged(MouseEvent evt) {
            if (canvas.rectShow) {
              // Zooming rectangle, set end corner.
              canvas.rectBottomRight.setLocation(evt.getPoint());
              canvas.repaint();
            }
        }
      }
    );

    // Adds MouseListener for mouse event ''Mouse clicked on vertex''.
    // Draw the name(s) of the vertex(vertices) as annotation on the canvas.
    canvas.addMouseListener(
      new MouseAdapter() {
        public void mousePressed(MouseEvent evt) {
            if(evt.getButton() == MouseEvent.BUTTON1){
                canvas.mouseX = evt.getX();
                canvas.mouseY = evt.getY();
                canvas.rectTopLeft.setLocation(evt.getPoint());
                canvas.repaint();
                canvas.rectShow = true;
                canvas.tolerance = Math.max(canvas.getHeight(), canvas.getWidth());
                canvas.tolerance /= 300;
            }
        }
        
        public void mouseReleased(MouseEvent evt){
            if (evt.getButton() == MouseEvent.BUTTON1){
                int x = evt.getX();
                int y = evt.getY();
                canvas.rectShow = false;
                if(Math.abs(x-canvas.mouseX) > canvas.tolerance ||
                        Math.abs(y-canvas.mouseY) > canvas.tolerance ){
                    canvas.rectBottomRight.setLocation(evt.getPoint());
                    canvas.writer.restrictShowedVertices(canvas.rectTopLeft, 
                            canvas.rectBottomRight);
                    canvas.updateAndPaint();
                }else{
                    if(canvas.writer.openURL && 
                       (evt.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK)){
                        canvas.writer.openURL(evt.getPoint());
                    }else if(canvas.writer.toggleNames(evt.getPoint()) > 0){
                        //if something changed then recompute the img
                        canvas.updateAndPaint();
                    }
                }
                
            }
        }
      }
    );

    // Dialog: Adds WindowListener for window event ''Close''.
    vertexNameDialog.addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
          dispose(); // Close.
          System.exit(0);
        }
      }
    );

    // Frame: Adds WindowListener for window event ''Close''.
    addWindowListener(
      new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
          dispose(); // Close.
          System.exit(0);
        }
      }
    );

  } // constructor
  

  /**
   * Repaint the drawing on the frame, i.e., delegate to canvas.
   */
  public void repaint() {
	  this.canvas.repaint();
  } // method repaint


  /**
   * Load next/previous layout from file, 
   * to leaf through the directory file by file.
   * @param next  True if next file should be loaded, 
   *              false if previous file should be loaded.
   */
  private void loadOtherFile(boolean next) {
	File     fileDir     = new File(".");
	String[] fileList = fileDir.list(
      new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (name.endsWith(".lay")) {
            return true;
          }
          return false;
        }
      }
    );
	if (fileList.length == 0) {
      System.err.println("No layout (.lay) file available in current directory.");
      return;
    }
    Arrays.sort(fileList);
	int fileCurrent = Arrays.binarySearch(fileList, new File(canvas.writer.inputName).getName());
	if (fileCurrent < 0) {  // File not found in current directory.
	  fileCurrent = 0;
	} else if (   next  && (fileCurrent < fileList.length - 1) ) {
      fileCurrent++;
    } else if ( (!next) && (fileCurrent > 0) ) {
      fileCurrent--;
    }
    canvas.writer.inputName = fileList[fileCurrent];
      
    // Load next layout.
    setTitle("Visualization " + canvas.writer.inputName);
    BufferedReader in = null;
    GraphData layout = new GraphData();
    try {
      in = new BufferedReader(new FileReader(canvas.writer.inputName));
    }
    catch (Exception e) {
      System.err.println("Exception while opening file '" + canvas.writer.inputName + "' for reading: ");
      System.err.println(e);
    }
    // Read layout from file.
    (new ReaderDataLAY(in)).read(layout);
    canvas.writer.setGraphData(layout);
    canvas.updateAndPaint();
    // Close the input file.
    try {
      in.close();
    } catch (Exception e) {
      System.err.println("Exception while closing input file: ");
      System.err.println(e);
    }
  }
  
  /**
   * repaint when the graph change
   * @param evt a GraphEvent
   */
  public void onGraphEvent(GraphEvent evt) {
      canvas.writer.refreshCluster();
      this.canvas.updateAndPaint();
  }
  
  /**
   * tells the clusterManager to refresh its list of clusters and ...
   * method invoked when the layout changes
   */
  public void refresh(){
      edgesCheck.setState(canvas.writer.showEdges);
      edgesCheck.setEnabled(canvas.writer.isshowEdgesPossible());
      this.clusters.refresh();
  }
  
}; // class frame
