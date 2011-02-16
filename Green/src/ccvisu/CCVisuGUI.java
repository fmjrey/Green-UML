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
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;


/**
 * GUI for those who don't like command line 
 * @version  $Revision$; $Date$
 * @author Damien Zufferey
 */
public class CCVisuGUI extends Frame {
    
    private static final long serialVersionUID = 200604191040L;

    public static final String[] FORMAT = { "CVS", "RSF", "LAY", "VRML", "SVG", "DISP"};
    
    /**a choice for the input format*/
    private Choice inFormat;
    /**a choice for the output format*/
    private Choice outFormat;
    
    /** textfield to enter the input file*/
    private TextField inFile;
    /** textfield to enter the output file (when needed)*/
    private TextField outFile;
    
    private Button loadInFile;
    private Button saveOutFile;
    /** validate choices and execute*/
    private Button exec;
    
    private Panel MinimizerOptions;
    private Panel DISPOptions; //also VRML and SVG
    private Panel CVSOptions;
    
    //minimizer
    private Choice dim;
    private TextField iter;
    private TextField initLayout;
    private TextField attrExp;
    private TextField repuExp;
    private TextField grav;
    private Checkbox noWeight;
    private Choice vertRepu;
    private Button loadInitLayout;
    
    //DISP: screen vrml svg
    private Checkbox hideSource;
    private Checkbox blackCircle;
    private Checkbox anim;
    private TextField minVert;
    private TextField fontSize;
    private TextField scale;
    private Choice annot;
    private Choice backColor;
    
    //CVS
    private TextField timeWindow;
    
    //used by the filedialog
    private FilenameFilter layFilter;
    private FilenameFilter CVSFilter;
    private FilenameFilter svgFilter;
    private FilenameFilter vrmlFilter;
    private FilenameFilter rsfFilter;
    
    /**
     * Constructor with parameters
     * @param inForm     
     * @param inF       
     * @param outForm     
     * @param outF       
     * @param ddim           minimizer
     * @param iiter          minimizer
     * @param iinitLayout    minimizer
     * @param aattrExp       energy model
     * @param rrepuExp       energy model
     * @param vvertRepu      energy model
     * @param nnoWeight      energy model
     * @param ggrav          energy model
     * @param time           CVS
     * @param hhideSource    Layout writer
     * @param minVertex      Layout writer
     * @param ffontSize      Layout writer
     * @param bbackColor     Layout writer
     * @param noBlackCircle  Layout writer
     * @param scalePos       VRML and SVG
     * @param aanim          DISP
     * @param annotAll       Layout writer
     * @param annotNone      Layout writer
     * @throws HeadlessException
     */
    public CCVisuGUI(int inForm, String inF, int outForm, String outF,
                        int ddim, int iiter, String iinitLayout,
                        int aattrExp, int rrepuExp, boolean vvertRepu, boolean nnoWeight,
                        float ggrav, int time, boolean hhideSource,
                        float minVertex, int ffontSize, String bbackColor,
                        boolean noBlackCircle, float scalePos, boolean aanim,
                        boolean annotAll, boolean annotNone) throws HeadlessException {
        super("CCVisu");        
        
        
        
        //contruction of the GUI's widgets
        this.inFormat = new Choice();
        this.inFormat.add("CVS");
        this.inFormat.add("RSF");
        this.inFormat.add("LAY");
        this.inFormat.select(inForm);
        this.inFormat.setName("in");
        
        
        this.outFormat = new Choice();
        this.outFormat.add("RSF");
        this.outFormat.add("LAY");
        this.outFormat.add("VRML");
        this.outFormat.add("SVG");
        this.outFormat.add("DISP");
        this.outFormat.select(outForm -1 );
        this.outFormat.setName("out");
        
        ItemListener formatListener = new ItemListener(){
            public void itemStateChanged(ItemEvent evt) {
                Choice scr = (Choice)evt.getSource();
                int nb = scr.getSelectedIndex();
                if(scr.getName().equals("in")){
                    switch(nb){
                    case CCVisu.LAY:
                        enableMinimizerOptions(false);
                        enableCVSOptions(false);
                        break;
                    case CCVisu.RSF:
                        enableMinimizerOptions(true);
                        enableCVSOptions(false);
                        break;
                    case CCVisu.CVS:
                        enableMinimizerOptions(true);
                        enableCVSOptions(true);
                        break;
                    }
                    if(outFormat.getSelectedIndex() < nb){
                        outFormat.select(--nb);
                    }
                }else if(scr.getName().equals("out")){
                    ++nb;
                    switch(nb){
                    case CCVisu.DISP:
                        enableDISPOptions(true);
                        outFile.setText("");
                        outFile.setEnabled(false);
                        saveOutFile.setEnabled(false);
                        break;
                    case CCVisu.SVG:
                    case CCVisu.VRML:
                        enableSVGOptions(true);
                        outFile.setEnabled(true);
                        saveOutFile.setEnabled(true);
                        break;
                    case CCVisu.LAY:
                    case CCVisu.RSF:
                        enableDISPOptions(false);
                        outFile.setEnabled(true);
                        saveOutFile.setEnabled(true);
                        break;
                    }
                    --nb;
                    if(inFormat.getSelectedIndex() > nb){
                        inFormat.select(nb);
                    }
                    
                }else{
                    System.err.println("PromptGUI.java, itemlistener: unknown source");
                }
                
            }
        };
        this.outFormat.addItemListener(formatListener);
        this.inFormat.addItemListener(formatListener);
        
        
        this.inFile = new TextField(inF,30);
        this.outFile = new TextField(outF,30);
        
        this.loadInFile = new Button("open ...");
        this.loadInFile.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent evt){
                        String file = loadDialog();
                        inFile.setText(file);
                    }
                }
            );
        
        this.saveOutFile = new Button("save as ...");
        this.saveOutFile.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent evt){
                        String file = saveDialog();
                        outFile.setText(file);
                    }
                }
            );
        
        this.exec = new Button("Go");
        this.exec.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent evt){
                        //fetch the arguments
                        Vector<String> arg = new Vector<String>();
                        arg.add("-inFormat");
                        int in = inFormat.getSelectedIndex();
                        arg.add(FORMAT[in]);
                        arg.add("-i");
                        arg.add(inFile.getText());
                        int out = (outFormat.getSelectedIndex());
                        ++out;
                        arg.add("-outFormat");
                        arg.add(FORMAT[out]);
                        if(out != CCVisu.DISP){
                            arg.add("-o");
                            arg.add(outFile.getText());
                        }
                            
                        if(in <= CCVisu.RSF){
                            if(in == CCVisu.CVS){
                                arg.add("-timeWindow");
                                arg.add(timeWindow.getText());
                            }
                            arg.add("-dim");
                            arg.add(dim.getSelectedItem());
                            arg.add("-iter");
                            arg.add(iter.getText());
                            String init = initLayout.getText();
                            if(!init.equals("")){
                                arg.add("-initLayout");
                                arg.add(initLayout.getText());
                            }
                            arg.add("-attrExp");
                            arg.add(attrExp.getText());
                            arg.add("-repuExp");
                            arg.add(repuExp.getText());
                            arg.add("-grav");
                            arg.add(grav.getText());
                            if(vertRepu.getSelectedIndex() == 1){
                                arg.add("-vertRepu");
                            }
                            if(noWeight.getState()){
                                arg.add("-noWeight");
                            }
                        }else if(in == CCVisu.LAY){
                            //nothing to do
                        }
                        
                        if(out == CCVisu.RSF){
                            //nothing to do
                        }else if(out >= CCVisu.LAY){
                            arg.add("-minVert");
                            arg.add(minVert.getText());
                            arg.add("-fontSize");
                            arg.add(fontSize.getText());
                            arg.add("-backColor");
                            arg.add(backColor.getSelectedItem());
                            if(!blackCircle.getState()){
                                arg.add("-noBlackCircle");
                            }
                            if(hideSource.getState()){
                                arg.add("-hideSource");
                            }
                            if(annot.getSelectedIndex() == 1){ //all
                                arg.add("-annotAll");
                            }else if(annot.getSelectedIndex() == 2){ //none
                                arg.add("-annotNone");
                            }
                            if(out == CCVisu.VRML || out == CCVisu.SVG){
                                arg.add("-scalePos");
                                arg.add(scale.getText());
                            }
                            if(out == CCVisu.DISP){
                                if(!anim.getState()){
                                    arg.add("-noAnim");
                                }
                            }
                        }
                        
                        
                        //build the argument's array of string 
                        int lgth = arg.size();
                        String args[] = new String[lgth];
                        for(int i = 0; i < lgth; ++i){
                            args[i] = (String)arg.get(i);
                        }
                        //launch the program
                        if(out == CCVisu.DISP){
                            dispose();
                        }
                        CCVisu.main(args);
                    }
                }
            );
        
        createFilenameFilter();
        
        //minimizer panel
        createMinimizerOptions(ddim, iiter, iinitLayout, aattrExp, rrepuExp,
                                vvertRepu, nnoWeight, ggrav);
        //disp panel
        createDISPOptions(hhideSource,minVertex,ffontSize, bbackColor,
                noBlackCircle, scalePos, aanim, annotAll, annotNone);
        //CVS panel
        createCVSOptions(time);
        
        this.addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e){
                        System.exit(0);
                    }

                }
             );
        
        //layout
        Panel main = new Panel();
        this.add(main);
        main.setLayout(new GridBagLayout());
        
        Panel up = new Panel();
        up.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        up.add(new Label("Input format:"), c);
        c.gridx = 1;
        up.add(this.inFormat, c);
        c.gridx = 2;
        up.add(new Label("File:"),c);
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        up.add(this.inFile, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        up.add(this.loadInFile, c);
        c.gridx = 0;
        c.gridy = 1;
        up.add(new Label("Output format:"), c);
        c.gridx = 1;
        up.add(this.outFormat, c);
        c.gridx = 2;
        up.add(new Label("File:"),c);
        c.gridx = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        up.add(this.outFile, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        up.add(this.saveOutFile, c);

        c.gridx = 0;
        c.gridy = 0;
        main.add(up,c);
        
        Panel down = new Panel();
        down.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        down.add(new Label("------- CVS options -------"),c);
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        down.add(CVSOptions, c);
        c.anchor = GridBagConstraints.NORTH;
        c.gridy = 2;
        down.add(new Label("------- Minimizer options -------"),c);
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        down.add(MinimizerOptions, c);
        c.anchor = GridBagConstraints.NORTH;
        c.gridy = 4;
        down.add(new Label("------- Display, SVG and VRML options -------"),c);
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        down.add(DISPOptions, c);
        
        c.gridx = 0;
        c.gridy = 1;
        main.add(down, c);
        c.gridy = 2;
        c.anchor = GridBagConstraints.CENTER;
        main.add(exec, c);
        
        
        //activate default
        enableMinimizerOptions(true);
        enableCVSOptions(false);
        enableDISPOptions(true);
        outFile.setEnabled(false);
        saveOutFile.setEnabled(false);
        
        this.pack();
        this.setSize(this.getWidth() , this.getHeight() + 40); //a little bigger
        this.setLocationRelativeTo(null);//center of screen
        this.setVisible(true);
    }
    
    /**
     * construct the panel for the minimizer options
     * @param ddim
     * @param iiter
     * @param iinitLayout
     * @param attrExp
     * @param repuExp
     * @param vertRepu
     * @param noWeight
     * @param grav
     */
    private void createMinimizerOptions(int ddim, int iiter, String iinitLayout,
                                        int attrExp, int repuExp, boolean vertRepu,
                                        boolean noWeight, float grav){
        //widgets creation
        this.dim = new Choice();
        this.dim.add("2");
        this.dim.add("3");
        this.dim.select(Integer.toString(ddim));
        
        this.iter = new TextField(Integer.toString(iiter),5);
        
        this.initLayout = new TextField(iinitLayout,30);
        
        this.attrExp = new TextField(Integer.toString(attrExp),5);
        this.repuExp = new TextField(Integer.toString(repuExp),5);
        
        this.grav = new TextField(Float.toString(grav));
        
        this.vertRepu = new Choice();
        this.vertRepu.add("Edge repulsion");
        this.vertRepu.add("Vertex repulsion");
        if(vertRepu){
            this.vertRepu.select(1);
        }else{
            this.vertRepu.select(0);
        }
        
        this.noWeight = new Checkbox("No weight", noWeight);
        
        this.loadInitLayout = new Button("open ...");
        this.loadInitLayout.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    String file = loadInitlayDialog();
                    initLayout.setText(file);
                }
            }
        );
        
        //layout
        MinimizerOptions = new Panel();
        MinimizerOptions.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        MinimizerOptions.add(new Label("Dimention:"),c);
        c.gridx = 1;
        MinimizerOptions.add(this.dim,c);
        c.gridx = 2;
        MinimizerOptions.add(new Label("# of iteration:"),c);
        c.gridx = 3;
        MinimizerOptions.add(this.iter,c);
        c.gridx = 0;
        c.gridy = 1;
        MinimizerOptions.add(new Label("Initial layout:"),c);
        c.gridx = 1;
        c.gridwidth = 2;
        MinimizerOptions.add(this.initLayout,c);
        c.gridwidth = 1;
        c.gridx = 3;
        MinimizerOptions.add(this.loadInitLayout,c);
        c.gridx = 0;
        c.gridy = 2;
        MinimizerOptions.add(new Label("Attraction:"),c);
        c.gridx = 1;
        MinimizerOptions.add(this.attrExp,c);
        c.gridx = 2;
        MinimizerOptions.add(new Label("Repulsion:"),c);
        c.gridx = 3;
        MinimizerOptions.add(this.repuExp,c);
        c.gridx = 0;
        c.gridy = 3;
        MinimizerOptions.add(new Label("Gravitation:"),c);
        c.gridx = 1;
        MinimizerOptions.add(this.grav,c);
        c.gridx = 2;
        MinimizerOptions.add(this.noWeight,c);
        c.gridx = 3;
        MinimizerOptions.add(this.vertRepu,c);
    }

    /**
     * construct the panel for the display options 
     * @param hideSource
     * @param minVertex
     * @param fontSize
     * @param backColor
     * @param noBlackCircle
     * @param scalePos
     * @param noAnim
     * @param annotAll
     * @param annotNone
     */
    private void createDISPOptions(boolean hideSource, float minVertex,
                            int fontSize, String backColor, boolean noBlackCircle,
                            float scalePos, boolean noAnim,
                            boolean annotAll, boolean annotNone){
        
        //creations of widgets
        this.anim = new Checkbox("Animation",!noAnim);
        
        this.blackCircle = new Checkbox("Black circle", !noBlackCircle);
        
        this.hideSource = new Checkbox("Hide source Vertex", hideSource);
        
        this.annot = new Choice();
        this.annot.add("default");
        this.annot.add("all");
        this.annot.add("none");
        if(annotAll){
            if(annotNone){
                this.annot.select("default");
            }else{
                this.annot.select("all");
            }
        }else{
            if(annotNone){
                this.annot.select("none");
            }else{
                this.annot.select("default");
            }
        }
        
        this.backColor = new Choice();
        this.backColor.add("black");
        this.backColor.add("gray");
        this.backColor.add("lightgray");
        this.backColor.add("white");
        this.backColor.select(backColor.toLowerCase());
        
        this.scale = new TextField(Float.toString(scalePos));
        
        this.minVert = new TextField(Float.toString(minVertex));
        
        this.fontSize = new TextField(Integer.toString(fontSize));
        
        //layout
        DISPOptions = new Panel();
        DISPOptions.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        DISPOptions.add(this.hideSource,c);
        c.gridx = 1;
        DISPOptions.add(this.blackCircle,c);
        c.gridx = 3;
        DISPOptions.add(this.anim,c);
        c.gridx = 0;
        c.gridy = 1;
        DISPOptions.add(new Label("Min vertex size:"),c);
        c.gridx = 1;
        DISPOptions.add(this.minVert,c);
        c.gridx = 2;
        DISPOptions.add(new Label("Font size:"),c);
        c.gridx = 3;
        DISPOptions.add(this.fontSize,c);
        c.gridx = 0;
        c.gridy = 2;
        DISPOptions.add(new Label("Annotation:"),c);
        c.gridx = 1;
        DISPOptions.add(this.annot,c);
        c.gridx = 2;
        DISPOptions.add(new Label("Background:"),c);
        c.gridx = 3;
        DISPOptions.add(this.backColor,c);
        c.gridx = 0;
        c.gridy = 3;
        DISPOptions.add(new Label("Scale:"),c);
        c.gridx = 1;
        DISPOptions.add(this.scale,c);
    }
    
    /**
     * construct the panel for the CVS options
     * @param timeWindow
     */
    private void createCVSOptions(int timeWindow){
        //creations of widgets
        this.timeWindow = new TextField(Integer.toString(timeWindow));
        
        //layout
        CVSOptions = new Panel();
        CVSOptions.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        CVSOptions.add(new Label("Time window:"), c);
        c.gridx = 1;
        CVSOptions.add(this.timeWindow,c);
    }
    
    private void createFilenameFilter(){
        layFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".lay")) {
                    return true;
                }
                return false;
            }
        };
        CVSFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".log")) {
                    return true;
                }
                return false;
            }
        };
        svgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".svg")) {
                    return true;
                }
                return false;
            }
        };
        vrmlFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".vrml")) {
                    return true;
                }
                return false;
            }
        };
        rsfFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".rsf")) {
                    return true;
                }
                return false;
            }
        };
    }
    
    
    /**
     * en/disable the part concerning the minimizer
     * @param b
     */
    private void enableMinimizerOptions(boolean b){
        dim.setEnabled(b);
        iter.setEnabled(b);
        initLayout.setEnabled(b);
        attrExp.setEnabled(b);
        repuExp.setEnabled(b);
        grav.setEnabled(b);
        noWeight.setEnabled(b);
        vertRepu.setEnabled(b);
        loadInitLayout.setEnabled(b);
    }
    
    /**
     * en/disable the part concerning CVS
     * @param b
     */
    private void enableCVSOptions(boolean b){
        timeWindow.setEnabled(b);
    }
    
    /**
     * en/disable the part concerning the display
     * @param b
     */
    private void enableDISPOptions(boolean b){
        hideSource.setEnabled(b);
        blackCircle.setEnabled(b);
        anim.setEnabled(b);
        minVert.setEnabled(b);
        fontSize.setEnabled(b);
        scale.setEnabled(!b);
        annot.setEnabled(b);
        backColor.setEnabled(b);
    }
    
    /**
     * en/disable the part concerning SVG and VRML format
     * @param b
     */
    private void enableSVGOptions(boolean b){
        hideSource.setEnabled(b);
        blackCircle.setEnabled(b);
        anim.setEnabled(!b);
        minVert.setEnabled(b);
        fontSize.setEnabled(b);
        scale.setEnabled(b);
        annot.setEnabled(b);
        backColor.setEnabled(b);
    }
    
    /**
     * dialog to select a file to load
     * @return return a String containing the fliename
     */
    private String loadDialog(){
        FileDialog fileDialog = new FileDialog(this, 
                "Load ...", FileDialog.LOAD);
        switch(inFormat.getSelectedIndex()){
        case CCVisu.CVS:
            fileDialog.setFilenameFilter(CVSFilter);
            fileDialog.setFile(".log");
            break;
        case CCVisu.RSF:
            fileDialog.setFilenameFilter(rsfFilter);
            fileDialog.setFile(".rsf");
            break;
        case CCVisu.LAY:
            fileDialog.setFilenameFilter(layFilter);
            fileDialog.setFile(".lay");
            break;
        case CCVisu.SVG:
            fileDialog.setFilenameFilter(svgFilter);
            fileDialog.setFile(".svg");
            break;
        case CCVisu.VRML:
            fileDialog.setFilenameFilter(vrmlFilter);
            fileDialog.setFile(".vrml");
            break;
        }  
        fileDialog.setResizable(true);
        fileDialog.pack();
        fileDialog.setVisible(true);
        String fileName = "";
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            fileName = fileDialog.getDirectory() + fileDialog.getFile();
        }
        return fileName;
    }
    
    /**
     * dialog to select the initial layout
     * @return return a String containing the fliename
     */
    private String loadInitlayDialog(){
        FileDialog fileDialog = new FileDialog(this, 
                "Load initial Layout", FileDialog.LOAD); 
        fileDialog.setFilenameFilter(layFilter);
        fileDialog.setResizable(true);
        fileDialog.setFile(".lay");
        fileDialog.pack();
        fileDialog.setVisible(true);
        String fileName = "";
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            fileName = fileDialog.getDirectory() + fileDialog.getFile();
        }
        return fileName;
    }
    
    /**
     * dialog to select an output file
     * @return return a String containing the filename
     */
    private String saveDialog(){
        FileDialog fileDialog = new FileDialog(this, 
                "Save as ...", FileDialog.SAVE);
        int format = outFormat.getSelectedIndex();
        ++format;
        switch(format){
        case CCVisu.RSF:
            fileDialog.setFilenameFilter(rsfFilter);
            fileDialog.setFile(".rsf");
            break;
        case CCVisu.LAY:
            fileDialog.setFilenameFilter(layFilter);
            fileDialog.setFile(".lay");
            break;
        case CCVisu.SVG:
            fileDialog.setFilenameFilter(svgFilter);
            fileDialog.setFile(".svg");
            break;
        case CCVisu.VRML:
            fileDialog.setFilenameFilter(vrmlFilter);
            fileDialog.setFile(".vrml");
            break;
        }  
        fileDialog.setResizable(true);
        fileDialog.pack();
        fileDialog.setVisible(true);
        String fileName = "";
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            fileName = fileDialog.getDirectory() + fileDialog.getFile();
        }
        return fileName;
    }
    
    public static void main(String[] args) {
        new CCVisuGUI(CCVisu.RSF, "", CCVisu.DISP, "",
                      2, 100, "", 1, 0, false, false, 0.001f, 180000, false,
                      7.0f, 12, "white", false, 1.0f, false, false, false);
    }
}
