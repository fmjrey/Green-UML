/*
 * CCVisu is a tool for visual graph clustering
 * and general force-directed graph layout.
 * This file is part of CCVisu. 
 * 
 * Copyright (C) 2005-2007  Andreas Noack, Dirk Beyer
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
 * Andreas Noack (an@informatik.tu-cottbus.de)
 * University of Technology at Cottbus, Germany
 * Dirk Beyer    (firstname.lastname@sfu.ca)
 * Simon Fraser University (SFU), B.C., Canada
 */

package ccvisu;

import java.util.Iterator;

/*****************************************************************
 * Minimizer for the (weighted) (edge-repulsion) LinLog energy model,
 * based on the Barnes-Hut algorithm.
 * @version  $Revision$; $Date$
 * @author   Andreas Noack and Dirk Beyer
 * Created:  Andreas Noack, 2004-04-01.
 * Changed:  Dirk Beyer:
 *           Extended to edge-repulsion, according to the IWPC 2005 paper.
 *           Data structures changed to achieve O(n log n) space complexity.
 *           Energy model extended to a weighted version.
 *           2006-02-08: Energy model changed to flexible repulsion exponent.
 *                       Some bug fixes from Andreas integrated.
 *****************************************************************/
public class MinimizerBarnesHut extends Minimizer{ 
    /** Number of nodes. */
    private int nodeNr;
    /** Position in 3-dimensional space for every node. */
    private float pos[][];
    /** The minimizer does not change the position for nodes with entry true. */
    private boolean fixedPos[];

    /** The following two must be symmetric. */
    /** Node indexes of the similarity lists. */ 
    private int attrIndexes[][];
    /** Similarity values of the similarity lists. */
    private float attrValues[][];
    /** Repulsion vector. */
    private float repu[];
    /** Exponent of the Euclidean distance in the attraction energy. */
    private float attrExponent = 1.0f;
    /** Exponent of the Euclidean distance in the repulsion energy. */
    private float repuExponent = 0.0f;
    
    /** Position of the barycenter of the nodes. */
    private float[] baryCenter = new float[3];
    /** Factor for the gravitation energy (attraction to the barycenter),
      * 0.0f for no gravitation. */
    private float gravitationFactor = 0.0f;

    /** Factor for repulsion energy that normalizes average distance 
      * between pairs of nodes with maximum similarity to (roughly) 1. */
    private float repuFactor = 1.0f;
    /** Factors for the repulsion force for pulsing. */
    private static final float[] repuStrategy 
        = { 0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f, 1.0f,
            1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1.0f };
    /** Octtree for repulsion computation. */               
    private OctTree octTree = null;
    

    /**
     * Sets the number of nodes, the similarity matrices (edge weights), 
     *   and the position matrix.
     * @param nodeNr  number of nodes.
     * @param attrIndexes  Node indexes of the similarity list for each node.
     *                Is not copied and not modified by this class.
     *                (attrIndexes[i][k] == j) represents the k-th edge 
     *                of node i, namely (i,j).
     *                Omit edges with weight 0.0 (i.e. non-edges). 
     *                Preconditions:
     *                  (attrIndexes[i][k] != i) for all i,k (irreflexive);
     *                  (attrIndexes[i][k1] == j) iff (attrIndexes[j][k2] == i) 
     *                  for all i, j, exists k1, k2 (symmetric).
     * @param attrValues  Similarity values of the similarity lists for each node.
     *                Is not copied and not modified by this class.
     *                For each (attrIndexes[i][k] == j), (attrValues[i][k] == w)
     *                represents the weight w of edge (i,j).
     *                For unweighted graphs use only 1.0f as edge weight. 
     *                Preconditions:
     *                  exists k1: (attrIndexes[i][k1] == j) and (attrValues[i][k1] == w) iff
     *                  exists k2: (attrIndexes[j][k2] == i) and (attrValues[j][k2] == w) 
     *                  (symmetric).
     * @param repu    Repulsion vector (node weights).
     *                Is not copied and not modified by this class.
     *                For for node repulsion use 1.0f for all nodes.
     *                Preconditions: dimension at least [nodeNr];
     *                  repu[i] >= 1 for all i.
     * @param pos     Position matrix.
     *                Is not copied and serves as input and output 
     *                  of <code>minimizeEnergy</code>.
     *                If the input is two-dimensional (i.e. pos[i][2] == 0
     *                for all i), the output is also two-dimensional.
     *                Random initial positions are appropriate.
     *                Preconditions: dimension at least [nodeNr][3];
     *                  no two different nodes have the same position
     *                The input positions should be scaled such that 
     *                  the average Euclidean distance between connected nodes 
     *                  is roughly 1.
     * @param fixedPos The minimizer does not change the position of a node i 
     *                 with fixedPos[i] == true.
     * @param attrExp  Exponent of the distance in the attraction term of the energy model.
     *                 1.0f for the LinLog models, 3.0f for the energy
     *                 version of the Fruchterman-Reingold model.
     *                 If 0.0f, the log of the distance is taken instead of a constant fun.
     *                 (Value 0.0f not yet tested.)
     * @param repuExp  Exponent of the distance in the repulsion term of the energy model.
     *                 0.0f for the LinLog models, 0.0f for the energy
     *                 version of the Fruchterman-Reingold model.
     *                 If 0.0f, the log of the distance is taken instead of a constant fun.
     * @param gravitationFactor  Factor for the gravitation energy
     *                           (attraction to the barycenter).
     *                           0.0f for no gravitation.
     */
    public MinimizerBarnesHut(int nodeNr, int[][] attrIndexes, float[][] attrValues, 
                              float[] repu, float[][] pos, boolean[] fixedPos,
							  float attrExp, float repuExp, float gravitationFactor) {
        this.nodeNr = nodeNr;
        this.attrIndexes = attrIndexes;
        this.attrValues = attrValues;
        this.repu = repu;
        this.pos = pos;
        this.fixedPos = fixedPos;
        this.attrExponent = attrExp;
        this.repuExponent = repuExp;
        this.gravitationFactor = gravitationFactor;
    }

    /**
     * Iteratively minimizes energy using the Barnes-Hut algorithm.
     * Starts from the positions in <code>pos</code>, 
     * and stores the computed positions in <code>pos</code>.
     * @param nrIterations  Number of iterations. Choose appropriate values
     *                      by observing the convergence of energy.
     */
    public void minimizeEnergy(int nrIterations) {
        if (nodeNr <= 1) {
            return;
        }

        //System.err.println();
        //System.err.println("Note: Minimizer will run " + nrIterations + " iterations,");
        //System.err.println("increase (decrease) this number with option -iter to");
        //System.err.println("increase quality of layout (decrease runtime).");
        
        analyzeDistances();

        final float finalRepuFactor = computeRepuFactor();
        repuFactor = finalRepuFactor;
        
        // compute initial energy
        buildOctTree();
        float energySum = 0.0f;
        for (int i = 0; i < nodeNr; i++) {
            energySum += getEnergy(i);
        }
        //System.err.println();
        //System.err.println("initial   energy " + energySum
        //   + "   repulsion " + repuFactor);
        
        //notify the listeners
        GraphEvent evt = new GraphEvent(this);
        Iterator it = listener.iterator(); 
        while(it.hasNext()){
            ((GraphEventListener)it.next()).onGraphEvent(evt);
        }
        
        // minimize energy
        float[] oldPos = new float[3];
        float[] bestDir = new float[3];
        for (int step = 1; step <= nrIterations; step++) {

            computeBaryCenter();
            buildOctTree();

            // except in the last 20 iterations, vary the repulsion factor
            // according to repuStrategy
            if (step < (nrIterations-20)) {
              repuFactor = finalRepuFactor 
                           * (float)Math.pow(repuStrategy[step%repuStrategy.length], 
                    		                 attrExponent - repuExponent);
            } else {
              repuFactor = finalRepuFactor;
            }

            // for all non-fixed nodes: minimize energy, i.e., move each node
            energySum = 0.0f;
            for (int i = 0; i < nodeNr; i++) {
              if(!fixedPos[i]) {
                float oldEnergy = getEnergy(i);
                // compute direction of the move of the node
                getDirection(i, bestDir);

                // line search: compute length of the move
                oldPos[0] = pos[i][0]; oldPos[1] = pos[i][1]; oldPos[2] = pos[i][2]; 
                float bestEnergy = oldEnergy;
                int bestMultiple = 0;
                bestDir[0] /= 32; bestDir[1] /= 32; bestDir[2] /= 32;
                for (int multiple = 32;
                     multiple >= 1 && (bestMultiple==0 || bestMultiple/2==multiple);
                     multiple /= 2) {
                    pos[i][0] = oldPos[0] + bestDir[0] * multiple;
                    pos[i][1] = oldPos[1] + bestDir[1] * multiple; 
                    pos[i][2] = oldPos[2] + bestDir[2] * multiple; 
                    float curEnergy = getEnergy(i);
                    if (curEnergy < bestEnergy) {
                        bestEnergy = curEnergy;
                        bestMultiple = multiple;
                    }
                }
                    
                for (int multiple = 64; 
                     multiple <= 128 && bestMultiple == multiple/2; 
                     multiple *= 2) {
                    pos[i][0] = oldPos[0] + bestDir[0] * multiple;
                    pos[i][1] = oldPos[1] + bestDir[1] * multiple; 
                    pos[i][2] = oldPos[2] + bestDir[2] * multiple; 
                    float curEnergy = getEnergy(i);
                    if (curEnergy < bestEnergy) {
                        bestEnergy = curEnergy;
                        bestMultiple = multiple;
                    }
                }

                pos[i][0] = oldPos[0] + bestDir[0] * bestMultiple;
                pos[i][1] = oldPos[1] + bestDir[1] * bestMultiple; 
                pos[i][2] = oldPos[2] + bestDir[2] * bestMultiple;
                if (bestMultiple > 0) {
                  octTree.moveNode(oldPos, pos[i], repu[i]); //1.0f);
                }
                energySum += bestEnergy;
              } // for
            }
            //System.err.println("iteration " + step 
            //  + "   energy " + energySum
            //  + "   repulsion " + repuFactor);
            
            
            //notify the listeners
            it = listener.iterator(); 
            while(it.hasNext()){
                ((GraphEventListener)it.next()).onGraphEvent(evt);
            }
            
        }
        analyzeDistances();
        //new JTreeFrame(octTree);
    }


    /**
     * Returns the Euclidean distance between the specified positions.
     * @return Euclidean distance between the specified positions.
     */
    private float getDist(float[] pos1, float[] pos2) {
        float xDiff = pos1[0] - pos2[0];
        float yDiff = pos1[1] - pos2[1];
        float zDiff = pos1[2] - pos2[2];
        return (float)Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
    }


    /**
     * Returns the Euclidean distance between node i and the baryCenter.
     * @return Euclidean distance between node i and the baryCenter.
     */
    private float getDistToBaryCenter(int i) {
        float xDiff = pos[i][0] - baryCenter[0];
        float yDiff = pos[i][1] - baryCenter[1];
        float zDiff = pos[i][2] - baryCenter[2];
        return (float)Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
    }


    /** 
     * Returns the repulsion energy between the node with the specified index
     * and the nodes in the octtree.
     * 
     * @param index Index of the repulsing node.
     * @param tree  Octtree containing repulsing nodes.
     * @return Repulsion energy between the node with the specified index
     *         and the nodes in the octtree.
     */
    private float getRepulsionEnergy(int index, OctTree tree) {
        if (tree == null || tree.index == index || index >= repu.length) {
            return 0.0f;
        }
        
        float dist = getDist(pos[index], tree.position);
        if (tree.index < 0 && dist < 2.0f * tree.width()) {
            float energy = 0.0f;
            for (int i = 0; i < tree.children.length; i++) {
                energy += getRepulsionEnergy(index, tree.children[i]);
            }
            return energy;
        } 
        
        if (repuExponent == 0.0f) {
          return -repuFactor * tree.weight * (float)Math.log(dist)    * repu[index];
        } else {
          return -repuFactor * tree.weight * (float)Math.pow(dist, repuExponent) / repuExponent    * repu[index];
        }
    }

    /**
     * Returns the energy of the specified node.
     * @param   index   Index of a node.
     * @return  Energy of the node.
    */
    private float getEnergy(int index) {
        // repulsion energy
        float energy = getRepulsionEnergy(index, octTree);
        
        // attraction energy
        for (int i = 0; i < attrIndexes[index].length; i++) {
            if (attrIndexes[index][i] != index) {
                float dist = getDist(pos[attrIndexes[index][i]], pos[index]);
                if (attrExponent == 0.0f) {
                  energy += attrValues[index][i] * (float)Math.log(dist);
                } else {
                  energy += attrValues[index][i] * (float)Math.pow(dist, attrExponent) / attrExponent;
                }
            }
        }
        
        // gravitation energy
        float dist = getDistToBaryCenter(index);
        if (attrExponent == 0.0f) {
          energy += gravitationFactor * repuFactor * repu[index] 
                    * (float)Math.log(dist);
        } else {
          energy += gravitationFactor * repuFactor * repu[index] 
                    * (float)Math.pow(dist, attrExponent) / attrExponent;
        }
        return energy;
    }
    
    /**
     * Computes the direction of the repulsion force from the tree 
     *     on the specified node.
     * @param  index   Index of the repulsed node.
     * @param  tree    Repulsing octtree.
     * @param  dir     Direction of the repulsion force acting on the node
     *                 is added to this variable (output parameter).
     * @return Approximate second derivation of the repulsion energy.
     */
    private float addRepulsionDir(int index, OctTree tree, float[] dir) {
        if (tree == null || tree.index == index) {
            return 0.0f;
        }
        
        float dist = getDist(pos[index], tree.position);
        if (tree.index < 0 && dist < tree.width()) {
            float dir2 = 0.0f;
            for (int i = 0; i < tree.children.length; i++) {
                dir2 += addRepulsionDir(index, tree.children[i], dir);
            }
            return dir2;
        } 

        if (dist != 0.0) {
            float tmp = repuFactor * tree.weight    * repu[index]    * (float)Math.pow(dist, repuExponent-2);
            for (int j = 0; j < 3; j++) {
                dir[j] -= (tree.position[j] - pos[index][j]) * tmp;
            }
            return tmp * Math.abs(repuExponent-1);
        }
        
        return 0.0f;
    }

    /**
     * Computes the direction of the total force acting on the specified node.
     * @param  index   Index of a node.
     * @param  dir     Direction of the total force acting on the node
     *                 (output parameter).
     */
    private void getDirection(int index, float[] dir) {
        dir[0] = 0.0f; dir[1] = 0.0f; dir[2] = 0.0f;

        // compute repulsion force vector        
        float dir2 = addRepulsionDir(index, octTree, dir);

        // compute attraction force vector
        for (int i = 0; i < attrIndexes[index].length; i++) {
            if (attrIndexes[index][i] != index) {
                float dist = getDist(pos[attrIndexes[index][i]], pos[index]);
                float tmp = attrValues[index][i] * (float)Math.pow(dist, attrExponent-2);
                dir2 += tmp * Math.abs(attrExponent-1);
                for (int j = 0; j < 3; j++) {
                    dir[j] += (pos[attrIndexes[index][i]][j] - pos[index][j]) * tmp;
                }
            }
        }
        
        // compute gravitation force vector      
        float dist = getDist(pos[index], baryCenter);
        dir2 += gravitationFactor * repuFactor * repu[index] 
                * (float)Math.pow(dist, attrExponent-2)
                * Math.abs(attrExponent-1);
        for (int j = 0; j < 3; j++) {
            dir[j] += gravitationFactor * repuFactor * repu[index] 
                      * (float)Math.pow(dist, attrExponent-2)
                      * (baryCenter[j] - pos[index][j]);
        }

        // normalize force vector with second derivation of energy
        dir[0] /= dir2; dir[1] /= dir2; dir[2] /= dir2;
         
        // ensure that the length of dir is at most 1/8
        // of the maximum Euclidean distance between nodes
        float length = (float)Math.sqrt(dir[0]*dir[0] + dir[1]*dir[1] + dir[2]*dir[2]);
        if (length > octTree.width()/8) {
            length /= octTree.width()/8;
            dir[0] /= length; dir[1] /= length; dir[2] /= length;
        }
    }    
    
    /**
     * Builds the octtree.
     */
    private void buildOctTree() {
        // compute mimima and maxima of positions in each dimension
        float[] minPos = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
        float[] maxPos = new float[] { Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE };
        for (int i = 0; i < nodeNr; i++) {
            for (int j = 0; j < 3; j++) {
                if (pos[i][j] < minPos[j]) {
                    minPos[j] = pos[i][j];
                }
                if (pos[i][j] > maxPos[j]) {
                    maxPos[j] = pos[i][j];
                }
            }
        }
        
        // add nodes to the octtree
        octTree = new OctTree(0, pos[0], repu[0], minPos, maxPos);
        for (int i = 1; i < nodeNr; i++) {
          octTree.addNode(i, pos[i],   repu[i]); // 1.0f);
        }
    }

    /**
     * Computes the factor for repulsion forces <code>repuFactor</code>
     * such that in the energy minimum the average Euclidean distance
     * between pairs of nodes with similarity 1.0 is approximately 1.
     */
    private float computeRepuFactor() {
        float attrSum = 0.0f;
        for (int i = 1; i < nodeNr; i++) {
            for (int j = 0; j < attrValues[i].length; j++) {
            	attrSum += attrValues[i][j];
            }
        }
        
		float repuSum = 0.0f;
		for (int i = 0; i < nodeNr; i++) repuSum += repu[i];
		if (repuSum > 0 && attrSum > 0) {
		  return attrSum / (repuSum * repuSum)
				       * (float)Math.pow(repuSum, 0.5f * (attrExponent - repuExponent));
		}
		return 1.0f;
    }

    /** 
     * Computes the position of the barycenter <code>baryCenter</code>
     * of all nodes.
     */
    private void computeBaryCenter() {
        baryCenter[0] = 0.0f; baryCenter[1] = 0.0f; baryCenter[2] = 0.0f;
        for (int i = 0; i < nodeNr; i++) {
            baryCenter[0] += pos[i][0];
            baryCenter[1] += pos[i][1];
            baryCenter[2] += pos[i][2];
        }
        baryCenter[0] /= nodeNr;
        baryCenter[1] /= nodeNr;
        baryCenter[2] /= nodeNr;
    }

    /**
     * Computes and outputs some statistics. 
     */
    private void analyzeDistances() {
        float edgeLengthSum = 0.0f;
        float edgeLengthLogSum = 0.0f;
        float attrSum = 0.0f;

        for (int i = 0; i < nodeNr; i++) {
            for (int j = 0; j < attrValues[i].length; j++) {
                float dist = getDist(pos[i], pos[attrIndexes[i][j]]);
                float distLog = (float)Math.log(dist);
                edgeLengthSum += attrValues[i][j] * dist;
                edgeLengthLogSum += attrValues[i][j] * distLog;
                attrSum += attrValues[i][j];
            }
        }
        edgeLengthSum /= 2;
        edgeLengthLogSum /= 2;
        attrSum /= 2;
        //System.err.println();
        //System.err.println("Number of Nodes: " + nodeNr);
        //System.err.println("Overall Attraction: " + attrSum);
        //System.err.println("Arithmetic mean of edge lengths: " + edgeLengthSum / attrSum);
        //System.err.println("Geometric mean of edge lengths: "
        //                                   + (float)Math.exp(edgeLengthLogSum / attrSum));
    }

    /**
     * Octtree for graph nodes with positions in 3D space.
     * Contains all graph nodes that are located in a given cuboid in 3D space.
     * 
     * @author Andreas Noack
     */
    private class OctTree {
        /** For leafs, the unique index of the graph node; for non-leafs -1. */
        private int index;
        /** Children of this tree node. */
        private OctTree[] children = new OctTree[8];
        /** Barycenter of the contained graph nodes. */
        private float[] position;
        /** Total weight of the contained graph nodes. */
        private float weight;
        /** Minimum coordinates of the cuboid in each of the 3 dimensions. */
        private float[] minPos;
        /** Maximum coordinates of the cuboid in each of the 3 dimensions. */
        private float[] maxPos;
    
        /**
         * Creates an octtree containing one graph node.
         *  
         * @param index    Unique index of the graph node.
         * @param position Position of the graph node.
         * @param weight   Weight of the graph node.
         * @param minPos   Minimum coordinates of the cuboid.
         * @param maxPos   Maximum coordinates of the cuboid.
         */
        private OctTree(int index, float[] position, float weight, float[] minPos, float[] maxPos) {
            this.index = index;
            this.position = new float[] { position[0], position[1], position[2] };
            this.weight = weight;
            this.minPos = minPos;
            this.maxPos = maxPos;
        }
    
        /**
         * Adds a graph node to the octtree.
         * 
         * @param nodeIndex  Unique index of the graph node.
         * @param nodePos    Position of the graph node.
         * @param nodeWeight Weight of the graph node.
         */
        private void addNode(int nodeIndex, float[] nodePos, float nodeWeight) {
            if (nodeWeight == 0.0f) {
                return;
            }
        
            if (index >= 0) {
                addNode2(index, position, weight);
                index = -1;
            }

            for (int i = 0; i < 3; i++) {
                position[i] = (position[i]*weight + nodePos[i]*nodeWeight) / (weight+nodeWeight);
            }
            weight += nodeWeight;
        
            addNode2(nodeIndex, nodePos, nodeWeight);
        }
    
        /**
         * Adds a graph node to the octtree, 
         * without changing the position and weight of the root.
         * 
         * @param nodeIndex  Unique index of the graph node.
         * @param nodePos    Position of the graph node.
         * @param nodeWeight Weight of the graph node.
         */
        private void addNode2(int nodeIndex, float[] nodePos, float nodeWeight) {
            int childIndex = 0;
            for (int i = 0; i < 3; i++) {
                if (nodePos[i] > (minPos[i]+maxPos[i])/2) {
                    childIndex += 1 << i;
                }
            }
        
            if (children[childIndex] == null) {
                float[] newMinPos = new float[3];           
                float[] newMaxPos = new float[3];
                for (int i = 0; i < 3; i++) {
                    if ((childIndex & 1<<i) == 0) {
                        newMinPos[i] = minPos[i];
                        newMaxPos[i] = (minPos[i] + maxPos[i]) / 2;
                    } else {
                        newMinPos[i] = (minPos[i] + maxPos[i]) / 2;
                        newMaxPos[i] = maxPos[i];
                    }
                }
                children[childIndex] = new OctTree(nodeIndex, nodePos, nodeWeight, newMinPos, newMaxPos);
            } else {
                children[childIndex].addNode(nodeIndex, nodePos, nodeWeight);
            }
        }
    
        /**
         * Updates the positions of the octtree nodes 
         * when the position of a graph node has changed.
         * 
         * @param oldPos     Previous position of the graph node.
         * @param newPos     New position of the graph node.
         * @param nodeWeight Weight of the graph node.
         */
        private void moveNode(float[] oldPos, float[] newPos, float nodeWeight) {
            for (int i = 0; i < 3; i++) {
                position[i] += (newPos[i]-oldPos[i]) * (nodeWeight/weight);
            }
        
            int childIndex = 0;
            for (int i = 0; i < 3; i++) {
                if (oldPos[i] > (minPos[i]+maxPos[i])/2) {
                    childIndex += 1 << i;
                }
            }
            if (children[childIndex] != null) {
                children[childIndex].moveNode(oldPos, newPos, nodeWeight);
            }
        }

        /**
         * Returns the maximum extension of the octtree.
         * 
         * @return Maximum over all dimensions of the extension of the octtree.
         */
        private float width() {
            float width = 0.0f;
            for (int i = 0; i < 3; i++) {
                if (maxPos[i] - minPos[i] > width) {
                    width = maxPos[i] - minPos[i];
                }
            }
            return width;
        }
    }

}
