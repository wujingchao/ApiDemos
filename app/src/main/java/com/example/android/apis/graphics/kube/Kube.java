/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics.kube;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import android.opengl.GLSurfaceView;

import java.util.Random;

/**
 * Animates a Rubic cube, randomly spinning layers one by one.
 */
public class Kube extends Activity implements KubeRenderer.AnimationCallback {
    /**
     * {@code GLSurfaceView} that we use as our content view, if uses {@code KubeRenderer mRenderer}
     * as its renderer.
     */
    GLSurfaceView mView;
    /**
     * Renderer which performs the drawing to our {@code GLSurfaceView mView} using the Rubic cube
     * we construct and initialize in the instance of {@code GLWorld} we pass to its constructor for
     * its data (see our method {@code makeGLWorld} for how we do this).
     */
    KubeRenderer mRenderer;
    /**
     * The 27 {@code Cube} objects which represent our Rubic cube
     */
    Cube[] mCubes = new Cube[27];
    /**
     * a {@code Layer} for each possible move, each layer consists of the 9 {@code Cube} objects in
     * a plane which can be rotated around its center {@code Cube}, 3 rotating around x, 3 rotating
     * around y, and 3 rotating around z.
     */
    Layer[] mLayers = new Layer[9];
    /**
     * permutations corresponding to a pi/2 (90 degree) rotation of each of the layers about its axis.
     * They are chosen at random in our method {@code animate}, and applied in our method {@code updateLayers}
     * to assign the {@code GLShapes} to the layer they belong to, both before any rotations have been
     * done and after the current rotation has finished. Each of these has the same index as the layer
     * it permutates, and represents the layer assignments of each of the {@code GLShape} objects that
     * need to be made after the rotation completes.
     */
    static int[][] mLayerPermutations = {
            /* permutation for UP layer
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}*/
            {2, 5, 8, 1, 4, 7, 0, 3, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26},
            /* permutation for DOWN layer
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}*/
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 23, 26, 19, 22, 25, 18, 21, 24},
            /* permutation for LEFT layer
            {0, 1, 2,  3, 4, 5, 6,  7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,18, 19, 20,21, 22, 23, 24, 25, 26}*/
            {6, 1, 2, 15, 4, 5, 24, 7, 8, 3, 10, 11, 12, 13, 14, 21, 16, 17, 0, 19, 20, 9, 22, 23, 18, 25, 26},
            /* permutation for RIGHT layer
            {0, 1, 2, 3, 4,  5, 6, 7,  8, 9, 10,11, 12, 13, 14, 15, 16, 17, 18, 19,20, 21, 22, 23, 24, 25, 26}*/
            {0, 1, 8, 3, 4, 17, 6, 7, 26, 9, 10, 5, 12, 13, 14, 15, 16, 23, 18, 19, 2, 21, 22, 11, 24, 25, 20},
            /* permutation for FRONT layer
            {0, 1, 2, 3, 4, 5,  6,  7, 8, 9, 10, 11, 12, 13, 14, 15, 16,17, 18, 19, 20, 21, 22, 23, 24, 25, 26}*/
            {0, 1, 2, 3, 4, 5, 24, 15, 6, 9, 10, 11, 12, 13, 14, 25, 16, 7, 18, 19, 20, 21, 22, 23, 26, 17, 8},
            /* permutation for BACK layer
            {0,  1, 2, 3, 4, 5, 6, 7, 8,  9, 10,11, 12, 13, 14, 15, 16, 17, 18, 19,20, 21, 22, 23, 24, 25, 26}*/
            {18, 9, 0, 3, 4, 5, 6, 7, 8, 19, 10, 1, 12, 13, 14, 15, 16, 17, 20, 11, 2, 21, 22, 23, 24, 25, 26},
            /* permutation for MIDDLE layer
            {0, 1, 2, 3,  4, 5, 6,  7, 8, 9,10, 11, 12, 13, 14, 15, 16, 17, 18,19, 20, 21, 22, 23, 24, 25, 26}*/
            {0, 7, 2, 3, 16, 5, 6, 25, 8, 9, 4, 11, 12, 13, 14, 15, 22, 17, 18, 1, 20, 21, 10, 23, 24, 19, 26},
            /* permutation for EQUATOR layer
            {0, 1, 2, 3, 4, 5, 6, 7, 8,  9, 10, 11, 12, 13, 14,15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}*/
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 14, 17, 10, 13, 16, 9, 12, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26},
            /* permutation for SIDE layer
            {0, 1, 2,  3,  4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14, 15, 16, 17, 18, 19, 20, 21, 22,23, 24, 25, 26}*/
            {0, 1, 2, 21, 12, 3, 6, 7, 8, 9, 10, 11, 22, 13, 4, 15, 16, 17, 18, 19, 20, 23, 14, 5, 24, 25, 26}
    };

    /**
     * permutation that needs to be done after the current rotation is finished (and the initial solved
     * permutation (0, 1, ... ,26) as well).
     */
    int[] mPermutation;

    /**
     * random number generator for random cube movements
     */
    Random mRandom = new Random(System.currentTimeMillis());
    /**
     * currently turning layer, set to null when the layer reaches its mEndAngle. It is set to a
     * random value in our method {@code animate} if it is null, and is the same layer as the layer
     * chosen to be permutated from {@code mLayerPermutations} of course.
     */
    Layer mCurrentLayer = null;
    /**
     * current and final angle for current Layer animation
     */
    float mCurrentAngle, mEndAngle;
    /**
     * amount to increment angle
     */
    float mAngleIncrement;
    /**
     * Temporary storage for the {@code mLayerPermutations} chosen for the next rotation which is
     * then used to permutate {@code mLayerPermutation} to create a new {@code mLayerPermutation}
     * when the rotation has completed
     */
    int[] mCurrentLayerPermutation;

    // names for our 9 layers (based on notation from http://www.cubefreak.net/notation.html)
    /**
     * Up layer (top 9 cubes)
     */
    static final int kUp = 0;
    /**
     * Down layer (bottom 9 cubes)
     */
    static final int kDown = 1;
    /**
     * Left layer (9 cubes on left side)
     */
    static final int kLeft = 2;
    /**
     * Right layer (9 cubes on right side)
     */
    static final int kRight = 3;
    /**
     * Front layer (layer in front of you)
     */
    static final int kFront = 4;
    /**
     * Back layer (layer at the back of the Rubic cube)
     */
    static final int kBack = 5;
    /**
     * Middle layer (layer between left and right)
     */
    static final int kMiddle = 6;
    /**
     * Equator layer (layer between up and down)
     */
    static final int kEquator = 7;
    /**
     * Side layer (layer between front and back)
     */
    static final int kSide = 8;

    /**
     * Creates, configures and returns a new instance of {@code GLWorld} which consists of a 27
     * {@code Cube} cube (3 by 3 by 3) representing a Rubic cube. First we create a new instance
     * for {@code GLWorld world}. Then we initialize some constants to use to create the seven
     * {@code GLColor} objects we use to color our {@code Cube} objects, and the six coordinate
     * values we use when we construct the 27 {@code Cube} objects used in our Rubic cube. We
     * construct the 27 instances of {@code Cube} in our Rubic cube to initialize our the contents
     * of our field {@code Cube[] mCubes}, then we loop through {@code mCubes} using there method
     * {@code setFaceColor} to set all the faces to the default black. We now go through all of the
     * {@code Cube} objects setting the color of the {@code GLFace} facing out as follows:
     * <ul>
     * <li>
     * top of Rubic cube - orange. This includes all the {@code Cube.kTop} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (0,1,2,3,4,5,6,7,8)
     * </li>
     * <li>
     * bottom of Rubic cube - red. This includes all the {@code Cube.kBottom} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (18,19,20,21,22,23,24,25,26)
     * </li>
     * <li>
     * left of Rubic cube - yellow. This includes all the {@code Cube.kLeft} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (0,3,6,9,12,15,18,21,24)
     * </li>
     * <li>
     * right of Rubic cube - white. This includes all the {@code Cube.kRight} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (2,5,8,11,15,17,20,23,26)
     * </li>
     * <li>
     * back of Rubic cube - blue. This includes all the {@code Cube.kBack} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (0,1,2,9,10,11,18,19,20)
     * </li>
     * <li>
     * front of Rubic cube - green. This includes all the {@code Cube.kFront} faces of the 9
     * {@code Cube} objects in {@code mCubes[]} (6,7,8,15,16,17,24,25,26)
     * </li>
     * </ul>
     * Then for each of the 27 {@code Cube} objects in {@code Cube[] mCubes} we call the method
     * {@code world.addShape} to add the {@code GLShape} to its list of {@code Cube} objects in
     * {@code ArrayList<GLShape> mShapeList}.
     * <p>
     * Next we initialize our field {@code int[] mPermutation} with the numbers (0,1,...,26) representing
     * an initial "solved" ordering of {@code Cube} objects. We call our method {@code createLayers}
     * to allocate and construct the 9 layers initializing the axis they can rotate about, and call
     * our method {@code updateLayers} to assign each of the {@code Cube} objects to the {@code Layer}
     * that it belongs to in the solved initial position that {@code int[] mPermutation} presently
     * represents.
     * <p>
     * Then we call the method {@code world.generate} which allocates and fills the direct allocated
     * buffers required by the method {@code glDrawElements} when it draws our Rubic cube. Finally we
     * return {@code world} to the caller (in our case the call to the constructor of the
     * {@code KubeRenderer} that is used to initialize our field {@code KubeRenderer mRenderer} in
     * this activities override of {@code onCreate}.
     *
     * @return a new instance of {@code GLWorld} configured with Rubic cube cubes.
     */
    private GLWorld makeGLWorld() {
        GLWorld world = new GLWorld();

        int one = 0x10000;
        int half = 0x08000;
        GLColor red = new GLColor(one, 0, 0);
        GLColor green = new GLColor(0, one, 0);
        GLColor blue = new GLColor(0, 0, one);
        GLColor yellow = new GLColor(one, one, 0);
        GLColor orange = new GLColor(one, half, 0);
        GLColor white = new GLColor(one, one, one);
        GLColor black = new GLColor(0, 0, 0);

        // coordinates for our cubes
        float c0 = -1.0f;
        float c1 = -0.38f;
        float c2 = -0.32f;
        float c3 = 0.32f;
        float c4 = 0.38f;
        float c5 = 1.0f;

        // top back, left to right
        mCubes[0] = new Cube(world, c0, c4, c0, c1, c5, c1);
        mCubes[1] = new Cube(world, c2, c4, c0, c3, c5, c1);
        mCubes[2] = new Cube(world, c4, c4, c0, c5, c5, c1);
        // top middle, left to right
        mCubes[3] = new Cube(world, c0, c4, c2, c1, c5, c3);
        mCubes[4] = new Cube(world, c2, c4, c2, c3, c5, c3);
        mCubes[5] = new Cube(world, c4, c4, c2, c5, c5, c3);
        // top front, left to right
        mCubes[6] = new Cube(world, c0, c4, c4, c1, c5, c5);
        mCubes[7] = new Cube(world, c2, c4, c4, c3, c5, c5);
        mCubes[8] = new Cube(world, c4, c4, c4, c5, c5, c5);
        // middle back, left to right
        mCubes[9] = new Cube(world, c0, c2, c0, c1, c3, c1);
        mCubes[10] = new Cube(world, c2, c2, c0, c3, c3, c1);
        mCubes[11] = new Cube(world, c4, c2, c0, c5, c3, c1);
        // middle middle, left to right
        mCubes[12] = new Cube(world, c0, c2, c2, c1, c3, c3);
        mCubes[13] = null;
        mCubes[14] = new Cube(world, c4, c2, c2, c5, c3, c3);
        // middle front, left to right
        mCubes[15] = new Cube(world, c0, c2, c4, c1, c3, c5);
        mCubes[16] = new Cube(world, c2, c2, c4, c3, c3, c5);
        mCubes[17] = new Cube(world, c4, c2, c4, c5, c3, c5);
        // bottom back, left to right
        mCubes[18] = new Cube(world, c0, c0, c0, c1, c1, c1);
        mCubes[19] = new Cube(world, c2, c0, c0, c3, c1, c1);
        mCubes[20] = new Cube(world, c4, c0, c0, c5, c1, c1);
        // bottom middle, left to right
        mCubes[21] = new Cube(world, c0, c0, c2, c1, c1, c3);
        mCubes[22] = new Cube(world, c2, c0, c2, c3, c1, c3);
        mCubes[23] = new Cube(world, c4, c0, c2, c5, c1, c3);
        // bottom front, left to right
        mCubes[24] = new Cube(world, c0, c0, c4, c1, c1, c5);
        mCubes[25] = new Cube(world, c2, c0, c4, c3, c1, c5);
        mCubes[26] = new Cube(world, c4, c0, c4, c5, c1, c5);

        // paint the sides
        int i, j;
        // set all faces black by default
        for (i = 0; i < 27; i++) {
            Cube cube = mCubes[i];
            if (cube != null) {
                for (j = 0; j < 6; j++)
                    cube.setFaceColor(j, black);
            }
        }

        // paint top
        for (i = 0; i < 9; i++)
            mCubes[i].setFaceColor(Cube.kTop, orange);
        // paint bottom
        for (i = 18; i < 27; i++)
            mCubes[i].setFaceColor(Cube.kBottom, red);
        // paint left
        for (i = 0; i < 27; i += 3)
            mCubes[i].setFaceColor(Cube.kLeft, yellow);
        // paint right
        for (i = 2; i < 27; i += 3)
            mCubes[i].setFaceColor(Cube.kRight, white);
        // paint back
        for (i = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                mCubes[i + j].setFaceColor(Cube.kBack, blue);
        // paint front
        for (i = 6; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                mCubes[i + j].setFaceColor(Cube.kFront, green);

        for (i = 0; i < 27; i++)
            if (mCubes[i] != null)
                world.addShape(mCubes[i]);

        // initialize our permutation to solved position
        mPermutation = new int[27];
        for (i = 0; i < mPermutation.length; i++)
            mPermutation[i] = i;

        createLayers();
        updateLayers();

        world.generate();

        return world;
    }

    /**
     * This initializes our field {@code Layer[] mLayers} with {@code Layer} objects constructed to
     * rotate around their respective axises:
     * <ul>
     * <li>
     * kUp = 0 Up layer (top 9 cubes) rotates around the y axis.
     * </li>
     * <li>
     * kDown = 1 Down layer (bottom 9 cubes) rotates around the y axis.
     * </li>
     * <li>
     * kLeft = 2 Left layer (9 cubes on left side) rotates around the x axis.
     * </li>
     * <li>
     * kRight = 3 Right layer (9 cubes on right side) rotates around the x axis.
     * </li>
     * <li>
     * kFront = 4 Front layer (layer in front of you) rotates around the z axis.
     * </li>
     * <li>
     * kBack = 5 Back layer (layer at the back of the Rubic cube) rotates around the z axis.
     * </li>
     * <li>
     * kMiddle = 6 Middle layer (layer between left and right) rotates around the x axis.
     * </li>
     * <li>
     * kEquator = 7 Equator layer (layer between up and down) rotates around the y axis.
     * </li>
     * <li>
     * kSide = 8 Side layer (layer between front and back) rotates around the z axis.
     * </li>
     * </ul>
     */
    private void createLayers() {
        mLayers[kUp] = new Layer(Layer.kAxisY);
        mLayers[kDown] = new Layer(Layer.kAxisY);
        mLayers[kLeft] = new Layer(Layer.kAxisX);
        mLayers[kRight] = new Layer(Layer.kAxisX);
        mLayers[kFront] = new Layer(Layer.kAxisZ);
        mLayers[kBack] = new Layer(Layer.kAxisZ);
        mLayers[kMiddle] = new Layer(Layer.kAxisX);
        mLayers[kEquator] = new Layer(Layer.kAxisY);
        mLayers[kSide] = new Layer(Layer.kAxisZ);
    }

    /**
     * This method updates all the layers in our field {@code Layer[] mLayers} so that their field
     * {@code GLShape[] mShapes} contains the correct {@code Cube} objects based on the latest
     * {@code mPermutation} performed (including the initial "solved" {@code mPermutation} (0.1,...,26).
     * <p>
     * First we declare the variables we will be using:
     * <ul>
     * <li>
     * {@code Layer layer} will contain a reference to the {@code Layer} object from the {@code Layer[] mLayers}
     * that we are currently working with.
     * </li>
     * <li>
     * {@code GLShape[] shapes} will contain a reference to the {@code GLShape[] mShapes} field
     * of the current {@code Layer layer}
     * </li>
     * <li>
     * {@code i, j, and k} will be used as indices
     * </li>
     * </ul>
     * Next we assign the correct {@code Cube} objects from our field {@code Cube[] mCubes}. This
     * assignment is done for the 9 layers as follows:
     * <ul>
     * <li>
     * kUp = 0 Up layer (top 9 cubes), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (0,1,2,3,4,5,6,7,8)
     * </li>
     * <li>
     * kDown = 1 Down layer (bottom 9 cubes), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (18,19,20,21,22,23,24,25,26)
     * </li>
     * <li>
     * kLeft = 2 Left layer (9 cubes on left side), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (0,3,6,9,12,15,18,21,24)
     * </li>
     * <li>
     * kRight = 3 Right layer (9 cubes on right side), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (2,5,8,11,14,17,20,23,26)
     * </li>
     * <li>
     * kFront = 4 Front layer (layer in front of you), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (6,7,8,15,16,17,24,25,26)
     * </li>
     * <li>
     * kBack = 5 Back layer (layer at the back of the Rubic cube), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (0,1,2,9,10,11,18,19,20)
     * </li>
     * <li>
     * kMiddle = 6 Middle layer (layer between left and right), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (1,4,7,9,13,16,18,21,24)
     * </li>
     * <li>
     * kEquator = 7 Equator layer (layer between up and down), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (9,10,11,12,13,14,15,16,17)
     * </li>
     * <li>
     * kSide = 8 Side layer (layer between front and back), its 9 {@code mShapes} {@code Cube} objects are chosen
     * from our {@code Cube[] mCubes} based on {@code mPermutation} entries (3,4,5,12,13,14,21,22,23)
     * </li>
     * </ul>
     */
    private void updateLayers() {
        Layer layer;
        GLShape[] shapes;
        int i, j, k;

        // up layer
        layer = mLayers[kUp];
        shapes = layer.mShapes;
        for (i = 0; i < 9; i++)
            shapes[i] = mCubes[mPermutation[i]];

        // down layer
        layer = mLayers[kDown];
        shapes = layer.mShapes;
        for (i = 18, k = 0; i < 27; i++)
            shapes[k++] = mCubes[mPermutation[i]];

        // left layer
        layer = mLayers[kLeft];
        shapes = layer.mShapes;
        for (i = 0, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // right layer
        layer = mLayers[kRight];
        shapes = layer.mShapes;
        for (i = 2, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // front layer
        layer = mLayers[kFront];
        shapes = layer.mShapes;
        for (i = 6, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // back layer
        layer = mLayers[kBack];
        shapes = layer.mShapes;
        for (i = 0, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // middle layer
        layer = mLayers[kMiddle];
        shapes = layer.mShapes;
        for (i = 1, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // equator layer
        layer = mLayers[kEquator];
        shapes = layer.mShapes;
        for (i = 9, k = 0; i < 18; i++)
            shapes[k++] = mCubes[mPermutation[i]];

        // side layer
        layer = mLayers[kSide];
        shapes = layer.mShapes;
        for (i = 3, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we request the window feature FEATURE_NO_TITLE. Next we initialize our
     * field {@code GLSurfaceView mView} with an instance of {@code GLSurfaceView}, initialize our
     * field {@code KubeRenderer mRenderer} with an instance of {@code KubeRenderer} constructed using
     * the {@code GLWorld} returned by the method {@code makeGLWorld} and set {@code mRenderer} as
     * the renderer for {@code mView}. Finally we set our content view to {@code mView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We don't need a title either.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mView = new GLSurfaceView(getApplication());
        mRenderer = new KubeRenderer(makeGLWorld(), this);
        mView.setRenderer(mRenderer);
        setContentView(mView);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume} then we call the {@code onResume} method of our field
     * {@code GLSurfaceView mView}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed.  The counterpart to {@link #onResume}. First we call through to our
     * super's implementation of {@code onPause} then we call the {@code onPause} method of our field
     * {@code GLSurfaceView mView}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    /**
     * Called by {@code KubeRenderer.onDrawFrame} to prepare the Rubic cube for the next frame to be
     * drawn. First we instruct our {@code KubeRenderer mRenderer} to add 1.2 degrees to the angle it
     * uses when rotating the entire Rubic cube. Then if {@code Layer mCurrentLayer} is null (the
     * last layer rotation has reached its endpoint, or we are just starting) we set {@code layerID}
     * to a random number between 0 and 8, and use it as an index into {@code mLayers} in order to
     * set {@code mCurrentLayer}, and we use it as an index into {@code mLayerPermutations} in order
     * to set {@code mCurrentLayerPermutation}. We call the method {@code mCurrentLayer.startAnimation}
     * which calls {@code Shape.startAnimation} (which does nothing) for each of the shapes in the
     * layer. We execute some unused code, then set {@code count} to 1, and {@code direction} to
     * false (neither of which is used). We set our field {@code mCurrentAngle} to 0 then (since
     * {@code direction} is always false) we set {@code mAngleIncrement} to PI/50 and {@code mEndAngle}
     * to -PI/2 (since {@code mCurrentAngle} is 0 at this point, and count is 1).
     * <p>
     * Now that {@code mCurrentLayer} is known not to be null, we increment {@code mCurrentAngle} by
     * {@code mAngleIncrement}, and if we have reached our {@code mEndAngle} we set the angle of
     * {@code mCurrentLayer} to {@code mEndAngle}, and call the method {@code mCurrentLayer.endAnimation}
     * which calls the method {@code Shape.endAnimation} for each of the 9 shapes in the layer inorder
     * to update its cumulative transfer matrix {@code mTransform} with the transform matrix it used for
     * the movement to the angle {@code mEndAngle}: {@code mAnimateTransform}. We then set our field
     * {@code mCurrentLayer} to null (so that a new layer will be chosen then next time {@code animate}
     * is called).
     * <p>
     * We now have to adjust {@code mPermutation} so that the next call to {@code updateLayers} will
     * assign the {@code Cube} objects to the correct layer based on the just completed layer rotation.
     * To do this we first allocate temporary storage for {@code int[] newPermutation}, then for each
     * of the current layer assignments for our {@code Cube[] mCubes} which was last specified by the
     * contents of {@code mPermutation} we assign a new layer based on the contents of the respective
     * index entry contained in {@code mCurrentLayerPermutation} (the permutation of the just completed
     * rotation). Then we assign our temporary {@code newPermutation} to {@code mPermutation} and call
     * our method {@code updateLayers} to apply this layer assignment to all the {@code Layer[] mLayers}
     * {@code Layer.mShapes} fields.
     * <p>
     * If we have not yet reached the {@code mEndAngle} we just call the {@code setAngle} method of
     * {@code mCurrentLayer} to set the angle to the new {@code mCurrentAngle}
     */
    @Override
    public void animate() {
        // change our angle of view
        mRenderer.setAngle(mRenderer.getAngle() + 1.2f);

        if (mCurrentLayer == null) {
            int layerID = mRandom.nextInt(9);
            mCurrentLayer = mLayers[layerID];
            mCurrentLayerPermutation = mLayerPermutations[layerID];
            mCurrentLayer.startAnimation();
            @SuppressWarnings("UnusedAssignment")
            boolean direction = mRandom.nextBoolean();
            @SuppressWarnings("UnusedAssignment")
            int count = mRandom.nextInt(3) + 1;

            count = 1;
            direction = false;
            mCurrentAngle = 0;
            //noinspection ConstantConditions
            if (direction) {
                mAngleIncrement = (float) Math.PI / 50;
                mEndAngle = mCurrentAngle + ((float) Math.PI * count) / 2f;
            } else { // This path is always taken.
                mAngleIncrement = -(float) Math.PI / 50;
                mEndAngle = mCurrentAngle - ((float) Math.PI * count) / 2f;
            }
        }

        mCurrentAngle += mAngleIncrement;

        if ((mAngleIncrement > 0f && mCurrentAngle >= mEndAngle) ||
                (mAngleIncrement < 0f && mCurrentAngle <= mEndAngle)) {
            mCurrentLayer.setAngle(mEndAngle);
            mCurrentLayer.endAnimation();
            mCurrentLayer = null;

            // adjust mPermutation based on the completed layer rotation
            int[] newPermutation = new int[27];
            for (int i = 0; i < 27; i++) {
                newPermutation[i] = mPermutation[mCurrentLayerPermutation[i]];
                //    			newPermutation[i] = mCurrentLayerPermutation[mPermutation[i]];
            }
            mPermutation = newPermutation;
            updateLayers();

        } else {
            mCurrentLayer.setAngle(mCurrentAngle);
        }
    }
}
