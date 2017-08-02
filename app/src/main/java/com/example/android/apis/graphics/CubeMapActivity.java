/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.apis.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import com.example.android.apis.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Demonstrate how to use the OES_texture_cube_map extension, available on some
 * high-end OpenGL ES 1.x GPUs. Shows how to load and use GL_TEXTURE_CUBE_MAP
 * textures to animate a gyrating Torus.
 */
public class CubeMapActivity extends Activity {
    /**
     * TAG used for logging.
     */
    private static final String TAG = "CubeMapAct...";
    /**
     * {@code GLSurfaceView} we use as our content view, allocated, configured to use our class
     * {@code Renderer} as its {@code GLSurfaceView.Renderer}, and then set as our content view
     * in our {@code onCreate} method.
     */
    private GLSurfaceView mGLSurfaceView;

    /**
     * {@code GLSurfaceView.Renderer} that generates and draws the gyrating, cube map textured torus
     * for our demo.
     */
    private class Renderer implements GLSurfaceView.Renderer {
        /**
         * Flag that indicates (if true) that our context supports the GL_OES_texture_cube_map according
         * to the string describing the current connection for the GL10.GL_EXTENSIONS token.
         */
        private boolean mContextSupportsCubeMap;
        /**
         * Topologically rectangular array of vertices describing our torus.
         */
        private Grid mGrid;
        /**
         * Texture name generated by {@code glGenTextures}, we use {@code glBindTexture} to bind this
         * to GL_TEXTURE_CUBE_MAP
         */
        private int mCubeMapTextureID;
        @SuppressWarnings("unused")
        private boolean mUseTexGen = false;
        /**
         * Angle we use to rotate our torus both around the vector (0,1,0) and (1,0,0), it is advanced
         * by 1.2f degrees every time {@code onDrawFrame} is called
         */
        private float mAngle;

        /**
         * Called to draw the current frame. First we call our method {@code checkGLError} which
         * calls {@code glGetError} to get any error conditions and throws a {@code RuntimeException}
         * if any error other than GL_NO_ERROR is returned. If {@code boolean mContextSupportsCubeMap}
         * is true we set our clear color to blue, otherwise we set it to red. We proceed then to
         * clear both the color buffer and the depth buffer, enable depth test, select the model view
         * as our current matrix and initialize it with the identity matrix. We define our viewing
         * transformation with the eye at (0,0,-5), the center at (0,0,0), and (0.0, 1.0, 0.0) as the
         * up vector. We rotate our model {@code float mAngle} degrees around the y axis, and by
         * {@code mAngle*0.25} degrees around the x axis. Next we enable the client side capability
         * GL_VERTEX_ARRAY (If enabled, the vertex array is enabled for writing and used during
         * rendering when glArrayElement, glDrawArrays, glDrawElements, glDrawRangeElements
         * glMultiDrawArrays, or glMultiDrawElements is called). We again call {@code checkGLError}
         * to catch any errors that may have occurred up to this point.
         * <p>
         * Now is our flag {@code mContextSupportsCubeMap} is true, we set the active texture to
         * GL_TEXTURE0, then call {@code checkGLError} to catch an error that may have occurred.
         * Enable the GL_TEXTURE_CUBE_MAP server-side GL capability, then call {@code checkGLError}
         * to catch an error that may have occurred. We bind the texture GL_TEXTURE_CUBE_MAP to our
         * texture ID {@code mCubeMapTextureID} then call {@code checkGLError} to catch an error
         * that may have occurred. We cast {@code gl} to an instance of {@code GL11ExtensionPack} to
         * initialize {@code GL11ExtensionPack gl11ep}. We call {@code gl11ep.glTexGeni} to control
         * the generation of texture coordinates for texture coordinate GL_TEXTURE_GEN_STR, to be
         * texture-coordinate generation function GL_TEXTURE_GEN_MODE, with the texture generation
         * parameter GL_REFLECTION_MAP (used to create a realistically reflective surface). And once
         * again we call {@code checkGLError} to catch any errors that may have occurred. We enable
         * the GL_TEXTURE_GEN_STR server-side GL capability (texture coordinates with be generated),
         * then call {@code checkGLError} to catch an error that may have occurred. And now we call
         * {@code glTexEnvx} to set the GL_TEXTURE_ENV environment parameter GL_TEXTURE_ENV_MODE to
         * the value GL_DECAL (a decal overlay effect is used).
         * <p>
         * In either case, once again we call {@code checkGLError} to catch any errors, then instruct
         * {@code Grid mGrid} to draw itself. Then if {@code mContextSupportsCubeMap} is true, we
         * disable the server side capability GL_TEXTURE_GEN_STR, and call {@code checkGLError} to
         * catch any errors in both cases.
         * <p>
         * Finally we advance {@code float mAngle} by 1.2 degrees and return to caller.
         *
         * @param gl the GL interface.
         */
        @Override
        public void onDrawFrame(GL10 gl) {
            checkGLError(gl);
            if (mContextSupportsCubeMap) {
                gl.glClearColor(0, 0, 1, 0);
            } else {
                // Current context doesn't support cube maps.
                // Indicate this by drawing a red background.
                gl.glClearColor(1, 0, 0, 0);
            }
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            gl.glRotatef(mAngle, 0, 1, 0);
            gl.glRotatef(mAngle * 0.25f, 1, 0, 0);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            checkGLError(gl);

            if (mContextSupportsCubeMap) {
                gl.glActiveTexture(GL10.GL_TEXTURE0);
                checkGLError(gl);
                gl.glEnable(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP);
                checkGLError(gl);
                gl.glBindTexture(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP, mCubeMapTextureID);
                checkGLError(gl);
                GL11ExtensionPack gl11ep = (GL11ExtensionPack) gl;
                gl11ep.glTexGeni(GL11ExtensionPack.GL_TEXTURE_GEN_STR,
                        GL11ExtensionPack.GL_TEXTURE_GEN_MODE,
                        GL11ExtensionPack.GL_REFLECTION_MAP);
                checkGLError(gl);
                gl.glEnable(GL11ExtensionPack.GL_TEXTURE_GEN_STR);
                checkGLError(gl);
                gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_DECAL);
            }

            checkGLError(gl);
            mGrid.draw(gl);

            if (mContextSupportsCubeMap) {
                gl.glDisable(GL11ExtensionPack.GL_TEXTURE_GEN_STR);
            }
            checkGLError(gl);

            mAngle += 1.2f;
        }

        /**
         * Called when the surface changed size. Called after the surface is created and whenever
         * the OpenGL ES surface size changes. First we call our method {@code checkGLError} to
         * catch any errors that may have occurred. Then we call glViewport to set the viewport with
         * the lower left corner at (0,0), {@code width} as the width of the viewport, and
         * {@code height} as the height of the viewport. We calculate the aspect ratio
         * {@code float ratio} to be {@code width/height}, set the the current matrix to the projection
         * matrix, load it with the identity matrix, and call {@code glFrustumf} to multiply that
         * matrix by the projection matrix created with the left vertical clipping plane {@code -ratio},
         * right vertical clipping plane {@code +ratio}, the bottom clipping plane of -1, top clipping
         * plane of 1, near clipping plane of 1, and far clipping plane of 10. Finally we call our
         * method {@code checkGLError} to catch any errors that may have occurred.
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param width  width of new surface
         * @param height height of new surface
         */
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            checkGLError(gl);
            gl.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            checkGLError(gl);
        }

        /**
         * Called when the surface is created or recreated. Called when the rendering thread starts
         * and whenever the EGL context is lost. The EGL context will typically be lost when the
         * Android device awakes after going to sleep. First we call our method {@code checkGLError}
         * to catch any errors that may have occurred. The we check whether the current context
         * supports the cube map extension and set our field {@code boolean mContextSupportsCubeMap}
         * accordingly. We call our method {@code generateTorusGrid} to create a {@code Grid mGrid}
         * defining our torus. If the current context supports the cube map extension (i.e. our field
         * {@code mContextSupportsCubeMap} is true) we initialize {@code int[] cubeMapResourceIds}
         * with the resource ID's of the six jpg raw resources we will use for the six sides of the
         * cube: R.raw.skycubemap0, R.raw.skycubemap1, R.raw.skycubemap2, R.raw.skycubemap3,
         * R.raw.skycubemap4, and R.raw.skycubemap5. Then we set our field {@code int mCubeMapTextureID}
         * to the texture ID returned from our method {@code generateCubeMap} after it turns these
         * jpg images into a cube map texture. Finally we call our  method {@code checkGLError} to
         * catch any errors that may have occurred.
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param config the EGLConfig of the created surface. Can be used
         *               to create matching pbuffers.
         */
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            checkGLError(gl);
            // This test needs to be done each time a context is created,
            // because different contexts may support different extensions.
            mContextSupportsCubeMap = checkIfContextSupportsCubeMap(gl);

            mGrid = generateTorusGrid(gl, 60, 60, 3.0f, 0.75f);

            if (mContextSupportsCubeMap) {
                int[] cubeMapResourceIds = new int[]{
                        R.raw.skycubemap0, R.raw.skycubemap1, R.raw.skycubemap2,
                        R.raw.skycubemap3, R.raw.skycubemap4, R.raw.skycubemap5};
                mCubeMapTextureID = generateCubeMap(gl, cubeMapResourceIds);
            }
            checkGLError(gl);
        }

        /**
         * Configures the cube map texture for use, and loads the 6 jpeg's with the resource IDs in
         * the array {@code int[] resourceIds} into the 6 cube map texture images (one for each face
         * of the cube). First we call our method {@code checkGLError} to catch any errors that may
         * have occurred. Next we allocate {@code int[] ids} and fill it with one texture ID generated
         * by {@code glGenTextures} which we assign to {@code int cubeMapTextureId}. We bind the texture
         * ID to GL_TEXTURE_CUBE_MAP. We set the parameter GL_TEXTURE_MIN_FILTER (The texture minifying
         * function is used whenever the level-of-detail function used when sampling from the texture
         * determines that the texture should be minified) of GL_TEXTURE_CUBE_MAP to GL_LINEAR (uses
         * the weighted average of the four texture elements that are closest to the specified texture
         * coordinates), and the parameter GL_TEXTURE_MAG_FILTER (The texture magnification function is
         * used whenever the level-of-detail function used when sampling from the texture determines
         * that the texture should be magnified) to GL_LINEAR as well.
         * <p>
         * Then we loop through the six images in {@code int[] resourceIds}, open each raw resource
         * using {@code InputStream is}, decode that image into {@code Bitmap bitmap}, specify that
         * {@code bitmap} for the face of the six sided cube map texture it is meant for (one of
         * GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_NEGATIVE_X, GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
         * GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL_TEXTURE_CUBE_MAP_POSITIVE_Z, and GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
         * The code relies on the fact that they happen to have sequential ID numbers), and then we
         * recycle the {@code Bitmap bitmap}.
         * <p>
         * Finally we call our method {@code checkGLError} to catch any errors that may have occurred
         * and return {@code cubeMapTextureId} to the caller.
         *
         * @param gl          the GL interface.
         * @param resourceIds the resource IDs of the six jpeg's to be used for the cube map texture
         * @return texture ID of the cube map texture we create
         */
        private int generateCubeMap(GL10 gl, int[] resourceIds) {
            checkGLError(gl);
            int[] ids = new int[1];
            gl.glGenTextures(1, ids, 0);
            int cubeMapTextureId = ids[0];
            gl.glBindTexture(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP, cubeMapTextureId);
            gl.glTexParameterf(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

            for (int face = 0; face < 6; face++) {
                InputStream is = getResources().openRawResource(resourceIds[face]);
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(is);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e("CubeMap", "Could not decode texture for face " + Integer.toString(face));
                    }
                }
                GLUtils.texImage2D(GL11ExtensionPack.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, bitmap, 0);
                bitmap.recycle();
            }
            checkGLError(gl);
            return cubeMapTextureId;
        }

        /**
         * Generates a {@code Grid} describing our torus. First we allocate a new {@code Grid grid}
         * sized to hold (uSteps+1)*(vSteps+1) vertices. Next we loop using {@code double angleV} to
         * divide the outside radius of the torus into {@code vSteps} segments, calculating the cos
         * {@code cosV} of {@code angleV} and sin {@code sinV} of {@code angleV}. In the inner loop
         * we loop using {@code double angleU} to divide the body of the torus into {@code uSteps}
         * segments, calculating the cos {@code cosU} of {@code angleU} and sin {@code sinU} of
         * {@code angleU}. Using these values we are able to calculate the (x,y,z) location of the
         * vertex, and the normal vector of the vertex (nx,ny,nz) and we call {@code grid.set} to
         * store these in their appropriate (i,j) places in the Grid's vertex buffer.
         * <p>
         * When done loading the vertex buffer of {@code Grid grid} we call {@code grid.createBufferObjects}
         * to load the buffer objects describing our torus into the openGL engine and return {@code grid}
         * to the caller.
         *
         * @param gl          the GL interface.
         * @param uSteps      number of steps for u dimension (width) 60 in our case
         * @param vSteps      number of steps for v dimension (height) 60 in our case
         * @param majorRadius Outer radius of torus donut 3.0f in our case
         * @param minorRadius Radius of body of torus 0.75f in our case
         * @return {@code Grid} describing our torus
         */
        private Grid generateTorusGrid(GL gl, int uSteps, int vSteps, float majorRadius, float minorRadius) {
            Grid grid = new Grid(uSteps + 1, vSteps + 1);
            for (int j = 0; j <= vSteps; j++) {
                double angleV = Math.PI * 2 * j / vSteps;
                float cosV = (float) Math.cos(angleV);
                float sinV = (float) Math.sin(angleV);
                for (int i = 0; i <= uSteps; i++) {
                    double angleU = Math.PI * 2 * i / uSteps;
                    float cosU = (float) Math.cos(angleU);
                    float sinU = (float) Math.sin(angleU);
                    float d = majorRadius + minorRadius * cosU;
                    float x = d * cosV;
                    float y = d * (-sinV);
                    float z = minorRadius * sinU;

                    float nx = cosV * cosU;
                    float ny = -sinV * cosU;
                    float nz = sinU;

                    float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    nx /= length;
                    ny /= length;
                    nz /= length;

                    grid.set(i, j, x, y, z, nx, ny, nz);
                }
            }
            grid.createBufferObjects(gl);
            return grid;
        }

        /**
         * Convenience function to call {@code checkIfContextSupportsExtension} to check whether the
         * "GL_OES_texture_cube_map" extension is present in the current context.
         *
         * @param gl GL interface
         * @return true if the "GL_OES_texture_cube_map" extension is present in the current context.
         */
        private boolean checkIfContextSupportsCubeMap(GL10 gl) {
            return checkIfContextSupportsExtension(gl, "GL_OES_texture_cube_map");
        }

        /**
         * This is not the fastest way to check for an extension, but fine if we are only checking
         * for a few extensions each time a context is created. We add spaces at the beginning and
         * end of the the string returned by {@code glGetString} for the GL_EXTENSIONS string
         * (which returns the extension string supported by the implementation). Then we use
         * {@code indexOf} to search withing that string for our parameter {@code String extension},
         * and return true if it is found, false if not.
         *
         * @param gl        GL interface
         * @param extension extension to test for
         * @return true if the extension is present in the current context.
         */
        private boolean checkIfContextSupportsExtension(GL10 gl, String extension) {
            String extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " ";
            // The extensions string is padded with spaces between extensions, but not
            // necessarily at the beginning or end. For simplicity, add spaces at the
            // beginning and end of the extensions string and the extension string.
            // This means we can avoid special-case checks for the first or last
            // extension, as well as avoid special-case checks when an extension name
            // is the same as the first part of another extension name.
            Log.i(TAG, "Supports: " + extensions);
            //noinspection IndexOfReplaceableByContains
            return extensions.indexOf(" " + extension + " ") >= 0;
        }
    }

    /**
     * A grid is a topologically rectangular array of vertices. This grid class is customized for
     * the vertex data required for this example. The vertex and index data are held in VBO objects
     * (Vertex buffer objects) because on most GPUs VBO objects are the fastest way of rendering
     * static vertex and index data.
     */
    @SuppressWarnings("WeakerAccess")
    private static class Grid {
        /**
         * Size of vertex data float elements in bytes:
         */
        final static int FLOAT_SIZE = 4;
        /**
         * Size of index data char elements in bytes:
         */
        final static int CHAR_SIZE = 2;

        // Vertex structure:
        // float x, y, z;
        // float nx, ny, nx;

        /**
         * 6 floats are used for each vertex, 3 for the (x,y.z) coordinate, and 3 for the normal vector
         */
        final static int VERTEX_SIZE = 6 * FLOAT_SIZE;
        /**
         * Offset to the normal vector in a vertex data point.
         */
        final static int VERTEX_NORMAL_BUFFER_INDEX_OFFSET = 3;

        /**
         * Buffer object name that we upload our vertex buffer {@code ByteBuffer mVertexByteBuffer} to
         */
        private int mVertexBufferObjectId;
        /**
         * Buffer object name that we upload our index buffer {@code CharBuffer mIndexBuffer} to
         */
        private int mElementBufferObjectId;

        // These buffers are used to hold the vertex and index data while
        // constructing the grid. Once createBufferObjects() is called
        // the buffers are nulled out to save memory.

        /**
         * {@code ByteBuffer} we use to build our vertex and normal vector data in
         */
        private ByteBuffer mVertexByteBuffer;
        /**
         * a view of {@code ByteBuffer mVertexByteBuffer} as a float buffer
         */
        private FloatBuffer mVertexBuffer;
        /**
         * {@code CharBuffer} we use to build our index data array
         */
        private CharBuffer mIndexBuffer;

        private int mW;
        private int mH;
        private int mIndexCount;

        public Grid(int w, int h) {
            if (w < 0 || w >= 65536) {
                throw new IllegalArgumentException("w");
            }
            if (h < 0 || h >= 65536) {
                throw new IllegalArgumentException("h");
            }
            if (w * h >= 65536) {
                throw new IllegalArgumentException("w * h >= 65536");
            }

            mW = w;
            mH = h;
            int size = w * h;

            mVertexByteBuffer = ByteBuffer.allocateDirect(VERTEX_SIZE * size).order(ByteOrder.nativeOrder());
            mVertexBuffer = mVertexByteBuffer.asFloatBuffer();

            int quadW = mW - 1;
            int quadH = mH - 1;
            int quadCount = quadW * quadH;
            int indexCount = quadCount * 6;
            mIndexCount = indexCount;
            mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount).order(ByteOrder.nativeOrder()).asCharBuffer();

            /*
             * Initialize triangle list mesh.
             *
             *     [0]-----[  1] ...
             *      |    /   |
             *      |   /    |
             *      |  /     |
             *     [w]-----[w+1] ...
             *      |       |
             *
             */

            {
                int i = 0;
                for (int y = 0; y < quadH; y++) {
                    for (int x = 0; x < quadW; x++) {
                        char a = (char) (y * mW + x);
                        char b = (char) (y * mW + x + 1);
                        char c = (char) ((y + 1) * mW + x);
                        char d = (char) ((y + 1) * mW + x + 1);

                        mIndexBuffer.put(i++, a);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, b);

                        mIndexBuffer.put(i++, b);
                        mIndexBuffer.put(i++, c);
                        mIndexBuffer.put(i++, d);
                    }
                }
            }
        }

        public void set(int i, int j, float x, float y, float z, float nx, float ny, float nz) {
            if (i < 0 || i >= mW) {
                throw new IllegalArgumentException("i");
            }
            if (j < 0 || j >= mH) {
                throw new IllegalArgumentException("j");
            }

            int index = mW * j + i;

            mVertexBuffer.position(index * VERTEX_SIZE / FLOAT_SIZE);
            mVertexBuffer.put(x);
            mVertexBuffer.put(y);
            mVertexBuffer.put(z);
            mVertexBuffer.put(nx);
            mVertexBuffer.put(ny);
            mVertexBuffer.put(nz);
        }

        public void createBufferObjects(GL gl) {
            checkGLError(gl);
            // Generate a the vertex and element buffer IDs
            int[] vboIds = new int[2];
            GL11 gl11 = (GL11) gl;
            gl11.glGenBuffers(2, vboIds, 0);
            mVertexBufferObjectId = vboIds[0];
            mElementBufferObjectId = vboIds[1];

            // Upload the vertex data
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            mVertexByteBuffer.position(0);
            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexByteBuffer.capacity(), mVertexByteBuffer, GL11.GL_STATIC_DRAW);

            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            mIndexBuffer.position(0);
            gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity() * CHAR_SIZE, mIndexBuffer, GL11.GL_STATIC_DRAW);

            // We don't need the in-memory data any more
            mVertexBuffer = null;
            mVertexByteBuffer = null;
            mIndexBuffer = null;
            checkGLError(gl);
        }

        public void draw(GL10 gl) {
            checkGLError(gl);
            GL11 gl11 = (GL11) gl;

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectId);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, VERTEX_SIZE, 0);

            gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
            gl11.glNormalPointer(GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_NORMAL_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            checkGLError(gl);
        }
    }

    static void checkGLError(GL gl) {
        int error = ((GL10) gl).glGetError();
        if (error != GL10.GL_NO_ERROR) {
            throw new RuntimeException("GLError 0x" + Integer.toHexString(error));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our surface view and set it as the content of our
        // Activity
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(new Renderer());
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }
}
