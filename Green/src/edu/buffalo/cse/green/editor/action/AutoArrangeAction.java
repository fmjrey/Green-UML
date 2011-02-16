/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * 
 */
package edu.buffalo.cse.green.editor.action;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import ccvisu.CCVisu;
import ccvisu.GraphData;
import ccvisu.GraphEdgeInt;
import ccvisu.GraphEvent;
import ccvisu.GraphEventListener;
import ccvisu.GraphVertex;

import edu.buffalo.cse.green.editor.DiagramEditor;
import edu.buffalo.cse.green.editor.model.AbstractModel;
import edu.buffalo.cse.green.editor.model.RelationshipModel;
import edu.buffalo.cse.green.editor.model.RootModel;
import edu.buffalo.cse.green.editor.model.TypeModel;
import edu.buffalo.cse.green.editor.model.commands.AutoArrangeCommand;

/**
 * @author zgwang
 *
 */
public class AutoArrangeAction extends ContextAction {

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#doRun()
	 */
	@Override
	protected void doRun() throws JavaModelException {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		
		editor.execute(new AutoArrangeCommand());
	}

	/**
	 * Calculates the actual size of a figure whose size parameters
	 * are specified as (-1, -1) so that it sizes to its contents.
	 * 
	 * @param fig Given figure
	 * @return true dimension of the given figure
	 */
	private Dimension getRealSize(IFigure fig) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Auto-Arrange Diagram";
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getPath()
	 */
	@Override
	public Submenu getPath() {
		return Submenu.AutoArrange;//Invisible;//None;
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#getSupportedModels()
	 */
	@Override
	protected int getSupportedModels() {
		return CM_EDITOR;
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.green.editor.action.ContextAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		//This is bad, replace this
		return true;
	}

}
