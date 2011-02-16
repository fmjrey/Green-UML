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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * a GUI to manage the clusters define new, remove, ...
 * @version $Revision$; $Date$
 * @author Damien Zufferey
 */
public class ClusterManager extends Dialog {

    private static final long serialVersionUID = 5669096380733602612L;

    /** what display the cluster's names */
    private List lst;
    
    /** to choose the color to display */
    private Choice color;
    /** to choose if the cluster should be diplayed */
    private Checkbox visible;
    /** to choose if the default cluster should be diplayed */
    private Checkbox defaultVisible;
    /** to choose if the cluster's info should be diplayed */
    private Checkbox infoVisible;
    /** to choose if the cluster's nodes should be diplayed */
    private Button showLabel;
    /** to choose if the cluster's nodes should be hidden */
    private Button hideLabel;
    
    /** display informations */
    private Label numberOfNode;
    /** display informations */
    private Label radius;
    
    /** save clusters */
    private Button save;
    /** load clusters */
    private Button load;
    /** to add a new cluster */
    private Button newCluster;
    /** to edit a cluster */
    private Button editCluster;
    /** to remove a cluster */
    private Button removeCluster;
    /** to change the position of a cluster in the rendering order */
    private Button up;
    /** to change the position of a cluster in the rendering order */
    private Button down;
    
    // temporarily used
    private Dialog diag;
    private Dialog diag2;
    /** cluster curently edited */
    private Cluster curClt;
    // used to get some input
    private TextField txt;
    // nodes list from a specific cluster
    private List cltNodes;
    // list from all nodes in the graph
    private List allNodes;
    // to select the way the pattern handled
    private Choice mode;// equals,contains,...
    private Choice mode2;// keep, remove
    
    /** where the clusters are stocked */
    private WriterDataGraphicsDISP parent;
    
    /** notify when significant changes are done */
    private ScreenDisplay display;
    
    
    /**
     * Constructor
     * @param pparent - WriterDataGraphicsDISP containing the clusters
     * @param disp - a GraphEventListener charged of the rendering
     */
    public ClusterManager(ScreenDisplay disp,WriterDataGraphicsDISP pparent){
        super(disp,"Group highlighting");
        this.parent = pparent;
        this.display = disp;
        this.setLayout(new BorderLayout());
        
        
        save = new Button("Save as...");
        load = new Button("Load ...");
        
        Panel north = new Panel();
        north.add(save);
        north.add(load);
        add(north,BorderLayout.NORTH);
        
        lst = new List(10, false);
        refreshList();
        add(lst, BorderLayout.CENTER);
        
        radius = new Label("        ");
        radius.setAlignment(Label.RIGHT);
        numberOfNode = new Label("        ");
        numberOfNode.setAlignment(Label.RIGHT);
        
        visible = new Checkbox("Show group", true);
        infoVisible = new Checkbox("Graphic informations", false);
        defaultVisible = new Checkbox("Show group-free vertices",true);
        
        color = new Choice();
        color.add("red");
        color.add("green");
        color.add("blue");
        color.add("yellow");
        color.add("magenta");
        color.add("cyan");
        color.add("light red");
        color.add("light green");
        color.add("light blue");
        color.add("dark yellow");
        color.add("dark magenta");
        color.add("dark cyan");
        color.add("pink");
        color.add("white");
        color.add("light gray");
        color.add("gray");
        color.add("dark gray");
        color.add("black");
        color.select(0);
        
        newCluster = new Button("New");
        removeCluster = new Button("Del");
        editCluster = new Button("Edit");
        up = new Button("up");
        down = new Button("down");
        showLabel = new Button("Show labels");
        hideLabel = new Button("Hide labels");
        
        //Panel info = new Panel(new GridLayout(12,1));
        Panel info = new Panel(new GridLayout(10,1));
        info.add(new Panel().add(defaultVisible));
        info.add(new Panel().add(visible));
        info.add(new Panel().add(infoVisible));
        info.add(new Panel().add(showLabel));
        info.add(new Panel().add(hideLabel));
        
        Panel p1 = new Panel();
        p1.add(new Label("Color:"));
        p1.add(color);
        info.add(p1);
        
        info.add(new Label("Number of Vertices:"));
        info.add(numberOfNode);
        
        info.add(new Label("Average radius:"));
        info.add(radius);
        
        add(info, BorderLayout.EAST);
        
        Panel buttons = new Panel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        
        int y = 0;
        c.gridy = y;
        buttons.add(newCluster,c);
        c.gridy = ++y;
        buttons.add(editCluster,c);
        c.gridy = ++y;
        buttons.add(removeCluster,c);
        c.gridy = ++y;
        buttons.add(up,c);
        c.gridy = ++y;
        buttons.add(down,c);
        
        add(buttons, BorderLayout.WEST);
        
        setLocation(900,150);
        pack();
        //setVisible(true);
        
        
        //construct the choices for a later usage
        //it always use the same object => it "remembers" last choice
        mode = new Choice();
        mode.add("Equals");
        mode.add("Contains");
        mode.add("Starts with");
        mode.add("Ends with");
        
        mode2 = new Choice();
        mode2.add("Keep");
        mode2.add("Remove");
        
        // Listeners
        
        save.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                saveClt();
            } 
        });
        
        
        load.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                loadClt();
            }
        });
        
        // set the selected color to the selected cluster
        color.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                Choice ch = (Choice)evt.getSource();
                // string (not index)
                // possible to change the order without changing this part
                String choice = ch.getSelectedItem();
                Color tmp = CCVisu.green;
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
                }
                int index = lst.getSelectedIndex();
                int end = parent.getNbOfCluster()-1;
                Cluster clt = parent.getCluster(end-index);
                if(clt != null){
                    clt.setColor(tmp);
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // toggle the visible attribut of the selected cluster
        visible.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent evt) {
                Cluster clt = getClusterFromListIndex(lst.getSelectedIndex());
                if(clt != null){
                    clt.visible = visible.getState();
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // toggle the visible attribut of the selected cluster
        infoVisible.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent evt) {
                Cluster clt = getClusterFromListIndex(lst.getSelectedIndex());
                if(clt != null){
                    clt.info = infoVisible.getState();
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // toggle the visible attribut of the default cluster
        defaultVisible.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent evt) {
                parent.getCluster(0).visible = defaultVisible.getState();
                display.onGraphEvent(new GraphEvent(this));

            }
        });
        
        // call the method in charge of creating a new cluster
        newCluster.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if(diag != null){
                    diag.setVisible(false);
                    diag.dispose();
                }
                intputDialog();
                refreshList();
            } 
        });
        
        // open a dialog to edit the nodes of the selected cluster
        editCluster.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                Cluster clt = getClusterFromListIndex(lst.getSelectedIndex());
                if(clt != null){
                    if(diag != null){
                        diag.setVisible(false);
                         diag.dispose();
                    }
                    editDialog(clt);
                }
            }
        });
        
        // remove the selected cluster
        removeCluster.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                int index = lst.getSelectedIndex();
                if(index >= 0){
                    parent.removeCluster(parent.getNbOfCluster()-1-index);
                    refreshList();
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // put the selected cluster one rank higher in the rendering list
        up.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                int index = lst.getSelectedIndex();
                if(index >= 0){
                    parent.moveClusterDown(parent.getNbOfCluster()-1-index);
                    refreshList();
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // put the selected cluster one rank lower in the rendering list
        down.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                int index = lst.getSelectedIndex();
                if(index >= 0){
                    parent.moveClusterUp(parent.getNbOfCluster()-1-index);
                    refreshList();
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        //diplays the label of the nodes in the cluster
        showLabel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                Cluster clt = getClusterFromListIndex(lst.getSelectedIndex());
                if(clt != null){
                    int end = clt.size();
                    for( int i = 0; i < end; ++i){
                        parent.showLabel(clt.getNode(i), true);
                    }
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        //hides the label of the nodes in the cluster
        hideLabel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                Cluster clt = getClusterFromListIndex(lst.getSelectedIndex());
                if(clt != null){
                    int end = clt.size();
                    for( int i = 0; i < end; ++i){
                        parent.showLabel(clt.getNode(i), false);
                    }
                    display.onGraphEvent(new GraphEvent(this));
                }
            }
        });
        
        // refresh displayed attributs
        lst.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent evt) {
                refreshInfo();
            }
        });
        
        
        // process the windowClosing event
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                setVisible(false);
            }
        });
    }
    
    /**
     * refresh the list that contains the clusters WARNING: the order of the
     * list is the inverse of the groups order
     */
    private void refreshList(){
        int nbClt = parent.getNbOfCluster();
        lst.removeAll();
        for(int i = 1; i < nbClt; ++i){// begin at one to ignore default
                                        // cluster
            Cluster clt = parent.getCluster(i);
            lst.add(clt.getName(), 0);
        }
    }
    
    /**
     * return a cluster in function of an integer
     * 
     * @param index
     *            the index of the selected list item
     * @return the selected cluster
     */
    private Cluster getClusterFromListIndex(int index){
        int end = parent.getNbOfCluster()-1;
        if(index >= 0){
            return parent.getCluster(end-index);
        }else{
            return null;
        }
    }
    
    /**
     * refresh the list that contains the nodes of the current cluster
     */
    private void refreshCltNodes(){
        int end = curClt.size();
        cltNodes.removeAll();
        for(int i = 0; i < end; ++i){
            cltNodes.add(curClt.getNode(i).name);
        }
    }
    
    
    /**
     * create a dialog that asks the user for a name and create a new cluster
     */
    private void intputDialog(){
        diag = new Dialog(this, "Name of the new group:",true);
        txt = new TextField(30);
        diag.add(txt,BorderLayout.CENTER);
        Button ok = new Button("Ok");
        Button cancel = new Button("Cancel");
        Panel btt = new Panel();
        btt.add(ok);
        btt.add(cancel);
        diag.add(btt,BorderLayout.SOUTH);
        ok.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                String name = txt.getText();
                if(name != null){
                    if(parent.getCluster(name) == null){
                        parent.addCluster(new Cluster(name));
                    }
                }
                txt = null;
                diag.setVisible(false);
                diag.dispose();
                diag = null;
            }     
        });
        
        txt.addKeyListener( new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String name = txt.getText();
                    if(name != null){
                        if(parent.getCluster(name) == null){
                            parent.addCluster(new Cluster(name));
                        }
                    }
                    txt = null;
                    diag.setVisible(false);
                    diag.dispose();
                    diag = null;
                 }
              }
            }
        );
        
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {;
                diag.setVisible(false);
                diag.dispose();
                diag = null;
            }     
        });
        diag.pack();
        diag.setLocationRelativeTo(null);// center of screen
        diag.setVisible(true);
    }
    
    /**
     * create a dialog to edit the cluster
     * @param clt - the cluster to edit
     */
    private void editDialog(Cluster clt){
        curClt = clt;
        
        cltNodes = new List(40);
        refreshCltNodes();
        cltNodes.setMultipleMode(true);
        
        allNodes = new List(40);
        allNodes.setMultipleMode(true);
        GraphData graph = parent.graph;
        for (int i = 0; i < graph.vertices.size(); ++i){
            allNodes.add(graph.vertices.get(i).name);
        }
        
        diag = new Dialog(this,clt.getName());
        
        Button addNode = new Button("Add");
        Button addPattern = new Button("Add (pattern)");
        Button filter = new Button("filter (pattern)");
        Button remove = new Button("Remove");
        
        Panel up = new Panel(new GridLayout(1,2,20,10));
        up.add(new Label("Vertices of "+ clt.getName()));
        up.add(new Label("List of all vertices"));
        diag.add(up, BorderLayout.NORTH);
        
        
        Panel center = new Panel(new GridLayout(1,2,20,10));
        center.add(cltNodes);
        center.add(allNodes);
        diag.add(center, BorderLayout.CENTER);
        Panel buttons = new Panel();
        buttons.add(addNode);
        buttons.add(addPattern);
        buttons.add(filter);
        buttons.add(remove);
        diag.add(buttons, BorderLayout.SOUTH);
        
        diag.setSize(330,300);
        diag.setLocationRelativeTo(this);// center of screen
        diag.setVisible(true);
        
        diag.addWindowListener(new WindowAdapter() {
                  public void windowClosing(WindowEvent evt) {
                      diag.setVisible(false);
                      diag.dispose(); // Close.
                      diag = null;
                  }
             }
        );
        
        addNode.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                int selected[] = allNodes.getSelectedIndexes();
                for( int i = 0; i < selected.length; ++i){
                    curClt.addNodeByIndex(selected[i]);
                }
                refreshCltNodes();
                display.onGraphEvent(new GraphEvent(this));
                refreshInfo();
            }     
        });
        
        remove.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                // affect selected node to the default cluster
                int selected[] = cltNodes.getSelectedIndexes();
                GraphVertex vertex[] = new GraphVertex[selected.length];
                for( int i = 0; i < selected.length; ++i){
                    vertex[i] = curClt.getNode(selected[i]);
                }
                for( int i = 0; i < selected.length; ++i){
                    ((Cluster)parent.getCluster(0)).addNode(vertex[i]);
                }
                refreshCltNodes();
                display.onGraphEvent(new GraphEvent(this));
                refreshInfo();
            }     
        });
        
        addPattern.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                diag2 = new Dialog(diag,"Enter a pattern",true);
                
                
                txt = new TextField(30);
                Panel center = new Panel(new GridLayout(2,1));
                center.add(txt);
                center.add(mode);
                diag2.add(center,BorderLayout.CENTER);
                
                Button ok = new Button("Ok");
                Button cancel = new Button("Cancel");
                Panel btt = new Panel();
                btt.add(ok);
                btt.add(cancel);
                diag2.add(btt,BorderLayout.SOUTH);
                
                AddPatternOk listener = new AddPatternOk();
                
                txt.addKeyListener(listener);
                ok.addActionListener(listener);
                
                cancel.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {;
                        diag2.setVisible(false);
                        diag2.dispose();
                    }     
                });
                
                diag2.pack();
                diag2.setLocationRelativeTo(null);// center of screen
                diag2.setVisible(true);
            }
        });
        
        filter.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                diag2 = new Dialog(diag,"Enter a pattern",true);
                
                txt = new TextField(30);
                Panel center = new Panel(new GridLayout(3,1));
                center.add(txt);
                center.add(mode);
                center.add(mode2);
                diag2.add(center,BorderLayout.CENTER);
                
                Button ok = new Button("Ok");
                Button cancel = new Button("Cancel");
                Panel btt = new Panel();
                btt.add(ok);
                btt.add(cancel);
                diag2.add(btt,BorderLayout.SOUTH);
                
                
                FilterPatternOk listener = new FilterPatternOk();
                txt.addKeyListener(listener);
                ok.addActionListener(listener);
                
                
                cancel.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {;
                        diag2.setVisible(false);
                        diag2.dispose();
                    }     
                });
                diag2.pack();
                diag2.setLocationRelativeTo(null);// center of screen
                diag2.setVisible(true);
            }     
        });
        
    }
    
    /**
     * refresh the list of clusters and closes the dialogs (edit,new,...) 
     * method invoqued by ScreenDisplay
     */
    public void refresh(){
        if (diag != null){
            diag.setVisible(false);
            diag.dispose();
            diag = null;
        }
        if(diag2 != null){
            diag2.setVisible(false);
            diag2.dispose();
            diag2 = null;
        }
        refreshList();
    }

    /**
     * Create a fileDialog and try to load the info from the selected file
     * Callback method
     */
    private void loadClt(){    
    	FileDialog fileDialog = new FileDialog(display, 
            "Load groups", 
            FileDialog.LOAD);
        fileDialog.setFilenameFilter(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.endsWith(".grp")) {
                            return true;
                        }
                        return false;
                    }
                }
        );
        fileDialog.setResizable(true);
        fileDialog.setFile(".clt");
        fileDialog.pack();
        fileDialog.setVisible(true);
        
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(fileName));
            }
            catch (Exception e) {
                System.err.println("Exception while opening file '" + fileName + "' for reading: ");
                System.err.println(e);
            }
        //  Read layout from file.
            ClusterReaderWriter.read(in, parent);
            refreshList();
            display.onGraphEvent(new GraphEvent(this));
        //  Close the input file.
            try {
                in.close();
            } catch (Exception e) {
                System.err.println("Exception while closing input file: ");
                System.err.println(e);
            }
        }
    }

    /**
     * Create a fileDialog and save the info in the selected file
     * Callback method
     */
    private void saveClt(){
        FileDialog fileDialog = new FileDialog(display, 
                "Save layout", 
                FileDialog.SAVE);
        fileDialog.setResizable(true);
        fileDialog.setFile(".grp");
        fileDialog.pack();
        fileDialog.setVisible(true);

        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            try {
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new FileWriter(fileName)));
                ClusterReaderWriter.write(out, parent);
                System.err.println("Wrote groups informations to output '"
                        + fileName + "'.");
                out.close();
            } catch (IOException e) {
                System.err.println("error while writing (ClusterManager.saveClt):");
                e.printStackTrace();
            }
            
        }
    }
    
    /**
     * refresh the informations of the selected cluster
     *
     */
    private void refreshInfo(){
        int index = lst.getSelectedIndex();
        int end = parent.getNbOfCluster()-1;
        Cluster clt = parent.getCluster(end-index);
        if(clt != null){
            // refresh the display with the correct informations
            radius.setText(Float.toString(clt.getAverageRadius()));
            numberOfNode.setText(Integer.toString(clt.size()));
            visible.setState(clt.visible);
            infoVisible.setState(clt.info);
            // select matching color
            Color tmp = clt.getColor();
            if(tmp.equals(CCVisu.red)){
                color.select("red");
            }else if(tmp.equals(CCVisu.green)){
                color.select("green");
            }else if(tmp.equals(CCVisu.blue)){
                color.select("blue");
            }else if(tmp.equals(CCVisu.yellow)){
                color.select("yellow");
            }else if(tmp.equals(CCVisu.magenta)){
                color.select("magenta");
            }else if(tmp.equals(CCVisu.cyan)){
                color.select("cyan");
            }else if(tmp.equals(CCVisu.lightRed)){
                color.select("light red");
            }else if(tmp.equals(CCVisu.lightGreen)){
                color.select("light green");
            }else if(tmp.equals(CCVisu.lightBlue)){
                color.select("light blue");
            }else if(tmp.equals(CCVisu.darkYellow)){
                color.select("dark yellow");
            }else if(tmp.equals(CCVisu.darkMagenta)){
                color.select("dark magenta");
            }else if(tmp.equals(CCVisu.darkCyan)){
                color.select("dark cyan");
            }else if(tmp.equals(CCVisu.pink)){
                color.select("pink");
            }else if(tmp.equals(CCVisu.white)){
                tmp = CCVisu.white;
            }else if(tmp.equals(CCVisu.lightGray)){
                color.select("light gray");
            }else if(tmp.equals(CCVisu.gray)){
                color.select("gray");
            }else if(tmp.equals(CCVisu.darkGray)){
                color.select("dark gray");
            }else if(tmp.equals(CCVisu.black)){
                color.select("black");
            }
        }
    }

    private class AddPatternOk  extends KeyAdapter implements ActionListener{

        //ok button event
        //same effect as Enter key
        public void actionPerformed(ActionEvent arg0) {
            process();
        }
        
        //Enter key
        //same effect as ok button
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                process();
            }
        }
        
        private void process(){
            String pattern = txt.getText();
            if(pattern != null){
                curClt.addPattern(pattern, mode.getSelectedIndex());
                refreshCltNodes();
                display.onGraphEvent(new GraphEvent(this));
                refreshInfo();
            }
            txt = null;
            diag2.setVisible(false);
            diag2.dispose();
        }
    }
    
    private class FilterPatternOk  extends KeyAdapter implements ActionListener{
        
        //ok button event
        //same effect as Enter key
        public void actionPerformed(ActionEvent arg0) {
            process();
        }
        
        //Enter key
        //same effect as ok button
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                process();
            }
        }
        
        private void process(){
            String pattern = txt.getText();
            if(pattern != null){
                boolean keep = true;
                if(mode2.getSelectedIndex() == 1){
                    keep = false;
                }
                curClt.filter(pattern, mode.getSelectedIndex(), keep);
                refreshCltNodes();
                display.onGraphEvent(new GraphEvent(this));
                refreshInfo();
            }
            txt = null;
            diag2.setVisible(false);
            diag2.dispose();
        }
    }
}
