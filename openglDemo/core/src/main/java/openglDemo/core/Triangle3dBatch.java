/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  Modification 2015 by Scott Simmons
 */ 
package openglDemo.core;

import pythagoras.f.AffineTransform;
import static playn.core.GL20.*;
import playn.core.*;

/**
 * A batch which renders indexed triangles. It serves as a {@link QuadBatch}, but can also render
 * arbitrary triangles via {@link #addTris}.
 */
public class Triangle3dBatch extends QuadBatch {

  /** The source for the stock triangle batch shader program. */
  public static class Source extends TexturedBatch.Source {

    /** Declares the uniform variables for our shader. */
    public static final String VERT_UNIFS =
      "uniform vec2 u_HScreenSize;\n" +
      "uniform float u_Flip;\n";

    /** The same-for-all-verts-in-a-quad attribute variables for our shader. */
    public static final String VERT_ATTRS =
      "attribute vec4 a_Matrix;\n" +
      "attribute vec2 a_Translation;\n" +
      "attribute vec2 a_Color;\n";

    /** The varies-per-vert attribute variables for our shader. */
    public static final String PER_VERT_ATTRS =
      "attribute vec2 a_Position;\n" +
      "attribute vec2 a_TexCoord;\n";

    /** Declares the varying variables for our shader. */
    public static final String VERT_VARS =
      "varying vec2 v_TexCoord;\n" +
      "varying vec4 v_Color;\n";

    /** The shader code that computes {@code gl_Position}. */
    public static final String VERT_SETPOS =
      // Transform the vertex.
      "mat3 transform = mat3(\n" +
      "  a_Matrix[0],      a_Matrix[1],      0,\n" +
      "  a_Matrix[2],      a_Matrix[3],      0,\n" +
      "  a_Translation[0], a_Translation[1], 1);\n" +
      "gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
      // Scale from screen coordinates to [0, 2].
      "gl_Position.xy /= u_HScreenSize.xy;\n" +
      // Offset to [-1, 1].
      "gl_Position.xy -= 1.0;\n" +
      // If requested, flip the y-axis.
      "gl_Position.y *= u_Flip;\n";

    /** The shader code that computes {@code v_TexCoord}. */
    public static final String VERT_SETTEX =
      "v_TexCoord = a_TexCoord;\n";

    /** The shader code that computes {@code v_Color}. */
    public static final String VERT_SETCOLOR =
      // tint is encoded as two floats A*R and G*B where A, R, G, B are (0 - 255)
      "float red = mod(a_Color.x, 256.0);\n" +
      "float alpha = (a_Color.x - red) / 256.0;\n" +
      "float blue = mod(a_Color.y, 256.0);\n" +
      "float green = (a_Color.y - blue) / 256.0;\n" +
      "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n";

    /** Returns the source of the vertex shader program. */
    public String vertex () {
      return (VERT_UNIFS +
              VERT_ATTRS +
              PER_VERT_ATTRS +
              VERT_VARS +
              "void main(void) {\n" +
              VERT_SETPOS +
              VERT_SETTEX +
              VERT_SETCOLOR +
              "}");
    }
  }

  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
  private static final int FLOAT_SIZE_BYTES = 4;

  private final boolean delayedBinding;

  protected final GLProgram program;
  protected final int uTexture;
  protected final int uHScreenSize;
  protected final int uFlip;
  protected final int aMatrix, aTranslation, aColor; // stable (same for whole quad)
  protected final int aPosition, aTexCoord; // changing (varies per quad vertex)

  protected final int verticesId, elementsId;
  protected final float[] stableAttrs;
  protected float[] vertices;
  protected short[] elements;
  protected int vertPos, elemPos;

  /** Creates a triangle batch with the default shader program. */
  public Triangle3dBatch (GL20 gl) {
    this(gl, new Source());
  }

  /** Creates a triangle batch with the supplied custom shader program. */
  public Triangle3dBatch (GL20 gl, Source source) {
    super(gl);
    delayedBinding = "Intel".equals(gl.glGetString(GL20.GL_VENDOR));

    program = new GLProgram(gl, source.vertex(), source.fragment());
    uTexture = program.getUniformLocation("u_Texture");
    uHScreenSize = program.getUniformLocation("u_HScreenSize");
    uFlip = program.getUniformLocation("u_Flip");
    aMatrix = program.getAttribLocation("a_Matrix");
    aTranslation = program.getAttribLocation("a_Translation");
    aColor = program.getAttribLocation("a_Color");
    aPosition = program.getAttribLocation("a_Position");
    aTexCoord = program.getAttribLocation("a_TexCoord");

    // create our vertex and index buffers
    stableAttrs = new float[stableAttrsSize()];
    vertices = new float[START_VERTS*vertexSize()];
    elements = new short[START_ELEMS];

    // create our GL buffers
    int[] ids = new int[2];
    gl.glGenBuffers(2, ids, 0);
    verticesId = ids[0]; elementsId = ids[1];

    gl.checkError("Triangle3dBatch end ctor");
  }

  /**
   * Prepares to add primitives with the specified tint and transform. This configures
   * {@link #stableAttrs} with all of the attributes that are the same for every vertex.
   */
  public void prepare (int tint, AffineTransform xf) {
    prepare(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty);
  }

  /**
   * See {@link #prepare(int,AffineTransform)}.
   */
  public void prepare (int tint, float m00, float m01, float m10, float m11, float tx, float ty) {
    float[] stables = stableAttrs;
    stables[0] = m00;
    stables[1] = m01;
    stables[2] = m10;
    stables[3] = m11;
    stables[4] = tx;
    stables[5] = ty;
    stables[6] = (tint >> 16) & 0xFFFF; // ar
    stables[7] = (tint >>  0) & 0xFFFF; // gb
    addExtraStableAttrs(stables, 8);
  }

  /**
   * Adds a collection of textured triangles to the current render operation.
   *
   * @param xys a list of x/y coordinates as: {@code [x1, y1, x2, y2, ...]}.
   * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
   * {@code xys.length}. Note: this is an absolute offset; since {@code xys} contains pairs of
   * values, this will be some multiple of two.
   * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
   * {@code xys.length - xysOffset}. Note: this is an absolute length; since {@code xys} contains
   * pairs of values, this will be some multiple of two.
   * @param tw the width of the texture for which we will auto-generate texture coordinates.
   * @param th the height of the texture for which we will auto-generate texture coordinates.
   * @param indices the index of the triangle vertices in the {@code xys} array. Because this
   * method renders a slice of {@code xys}, one must also specify {@code indexBase} which tells us
   * how to interpret indices. The index into {@code xys} will be computed as:
   * {@code 2*(indices[ii] - indexBase)}, so if your indices reference vertices relative to the
   * whole array you should pass {@code xysOffset/2} for {@code indexBase}, but if your indices
   * reference vertices relative to <em>the slice</em> then you should pass zero.
   * @param indicesOffset the offset of the indices array, must not be negative and no greater than
   * {@code indices.length}.
   * @param indicesLen the number of indices to read, must be no less than zero and no greater than
   * {@code indices.length - indicesOffset}.
   * @param indexBase the basis for interpreting {@code indices}. See the docs for {@code indices}
   * for details.
   */
  public void addTris (Texture tex, int tint, AffineTransform xf,
                       float[] xys, int xysOffset, int xysLen, float tw, float th,
                       int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    setTexture(tex);
    prepare(tint, xf);
    addTris(xys, xysOffset, xysLen, tw, th, indices, indicesOffset, indicesLen, indexBase);
  }

  /**
   * Adds a collection of textured triangles to the current render operation. See
   * {@link #addTris(Texture,int,AffineTransform,float[],int,int,float,float,int[],int,int,int)}
   * for parameter documentation.
   *
   * @param sxys a list of sx/sy texture coordinates as: {@code [sx1, sy1, sx2, sy2, ...]}. This
   * must be of the same length as {@code xys}.
   */
  public void addTris (Texture tex, int tint, AffineTransform xf,
                       float[] xys, float[] sxys, int xysOffset, int xysLen,
                       int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    setTexture(tex);
    prepare(tint, xf);
    addTris(xys, sxys, xysOffset, xysLen, indices, indicesOffset, indicesLen, indexBase);
  }

  /**
   * Adds triangle primitives to a prepared batch. This must be preceded by calls to
   * {@link #setTexture} and {@link #prepare} to configure the texture and stable attributes.
   */
  public void addTris (float[] xys, int xysOffset, int xysLen, float tw, float th,
                       int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    int vertIdx = beginPrimitive(xysLen/2, indicesLen), offset = vertPos;
    float[] verts = vertices, stables = stableAttrs;
    for (int ii = xysOffset, ll = ii+xysLen; ii < ll; ii += 2) {
      float x = xys[ii], y = xys[ii+1];
      offset = add(verts, add(verts, offset, stables), x, y, x/tw, y/th);
    }
    vertPos = offset;

    addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase);
  }

  /**
   * Adds triangle primitives to a prepared batch. This must be preceded by calls to
   * {@link #setTexture} and {@link #prepare} to configure the texture and stable attributes.
   */
  public void addTris (float[] xys, float[] sxys, int xysOffset, int xysLen,
                       int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    int vertIdx = beginPrimitive(xysLen/2, indicesLen), offset = vertPos;
    float[] verts = vertices, stables = stableAttrs;
    for (int ii = xysOffset, ll = ii+xysLen; ii < ll; ii += 2) {
      offset = add(verts, add(verts, offset, stables), xys[ii], xys[ii+1], sxys[ii], sxys[ii+1]);
    }
    vertPos = offset;

    addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase);
  }

  @Override public void addQuad (int tint,
                                 float m00, float m01, float m10, float m11, float tx, float ty,
                                 float x1, float y1, float sx1, float sy1,
                                 float x2, float y2, float sx2, float sy2,
                                 float x3, float y3, float sx3, float sy3,
                                 float x4, float y4, float sx4, float sy4) {
    prepare(tint, m00, m01, m10, m11, tx, ty);

    int vertIdx = beginPrimitive(4, 6); int offset = vertPos;
    float[] verts = vertices, stables = stableAttrs;
    offset = add(verts, add(verts, offset, stables), x1, y1, sx1, sy1);
    offset = add(verts, add(verts, offset, stables), x2, y2, sx2, sy2);
    offset = add(verts, add(verts, offset, stables), x3, y3, sx3, sy3);
    offset = add(verts, add(verts, offset, stables), x4, y4, sx4, sy4);
    vertPos = offset;

    addElems(vertIdx, QUAD_INDICES, 0, QUAD_INDICES.length, 0);
  }

  @Override public void begin (float fbufWidth, float fbufHeight, boolean flip) {
    super.begin(fbufWidth, fbufHeight, flip);
    program.activate();
    gl.glUniform2f(uHScreenSize, fbufWidth/2f, fbufHeight/2f);
    gl.glUniform1f(uFlip, flip ? -1 : 1);
    // certain graphics cards (I'm looking at you, Intel) exhibit broken behavior if we bind our
    // attributes once during activation, so for those cards we bind every time in flush()
    if (!delayedBinding) bindAttribsBufs();
    gl.checkError("Triangle3dBatch begin");
  }

  private void bindAttribsBufs () {
    gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId);

    // bind our stable vertex attributes
    int stride = vertexStride();
    glBindVertAttrib(aMatrix, 4, GL_FLOAT, stride, 0);
    glBindVertAttrib(aTranslation, 2, GL_FLOAT, stride, 16);
    glBindVertAttrib(aColor, 2, GL_FLOAT, stride, 24);

    // bind our changing vertex attributes
    int offset = stableAttrsSize()*FLOAT_SIZE_BYTES;
    //glBindVertAttrib(aPosition, 2, GL_FLOAT, stride, offset);  
    glBindVertAttrib(aPosition, 3, GL_FLOAT, stride, offset);  // Simmons changed this 
    //glBindVertAttrib(aTexCoord, 2, GL_FLOAT, stride, offset+8);
    glBindVertAttrib(aTexCoord, 2, GL_FLOAT, stride, offset+12); // Simmons changed this

    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId);
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glUniform1i(uTexture, 0);

    gl.glEnable(GL_DEPTH_TEST); // Simmons added this line
  }

  @Override public void flush () {
    super.flush();
    if (vertPos > 0) {
      bindTexture();

      gl.glClear(GL_DEPTH_BUFFER_BIT); // Simmons added this line

      if (delayedBinding) {
        bindAttribsBufs(); // see comments in activate()
        gl.checkError("Triangle3dBatch.flush bind");
      }


      gl.bufs.setFloatBuffer(vertices, 0, vertPos);
      gl.glBufferData(GL_ARRAY_BUFFER, vertPos*4, gl.bufs.floatBuffer, GL_STREAM_DRAW);

      gl.bufs.setShortBuffer(elements, 0, elemPos);
      gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elemPos*2, gl.bufs.shortBuffer, GL_STREAM_DRAW);
      gl.checkError("Triangle3dBatch.flush BufferData");


      gl.glDrawElements(GL_TRIANGLES, elemPos, GL_UNSIGNED_SHORT, 0);
      gl.checkError("Triangle3dBatch.flush DrawElements");

      vertPos = 0;
      elemPos = 0;
    }
  }

  @Override public void end () {
    super.end();
    gl.glDisableVertexAttribArray(aMatrix);
    gl.glDisableVertexAttribArray(aTranslation);
    gl.glDisableVertexAttribArray(aColor);
    gl.glDisableVertexAttribArray(aPosition);
    gl.glDisableVertexAttribArray(aTexCoord);
    gl.glDisable(GL_DEPTH_TEST); // Simmons added this line
    gl.checkError("Triangle3dBatch end");
  }

  @Override public void close () {
    super.close();
    program.close();
    gl.glDeleteBuffers(2, new int[] { verticesId, elementsId }, 0);
    gl.checkError("Triangle3dBatch close");
  }

  @Override public String toString () { return "tris/" + (elements.length/QUAD_INDICES.length); }

  /** Returns the size (in floats) of the stable attributes. If a custom shader adds additional
    * stable attributes, it should use this to determine the offset at which to bind them, and
    * override this method to return the new size including their attributes. */
  protected int stableAttrsSize() { return 8; }
 // protected int vertexSize () { return stableAttrsSize() + 4; }
  protected int vertexSize () { return stableAttrsSize() + 5; }  //Simmons changed this
  protected int vertexStride () { return vertexSize() * FLOAT_SIZE_BYTES; }

  protected int addExtraStableAttrs (float[] buf, int sidx) {
    return sidx;
  }

  protected int beginPrimitive (int vertexCount, int elemCount) {
    // check whether we have enough room to hold this primitive
    int vertIdx = vertPos / vertexSize();
    int verts = vertIdx + vertexCount, elems = elemPos + elemCount;
    int availVerts = vertices.length / vertexSize(), availElems = elements.length;
    if (verts <= availVerts && elems <= availElems) return vertIdx;

    // otherwise, flush and expand our buffers if needed
    flush();
    if (verts > availVerts) expandVerts(verts);
    if (elems > availElems) expandElems(elems);
    return 0;
  }

  protected final void glBindVertAttrib (int loc, int size, int type, int stride, int offset) {
    gl.glEnableVertexAttribArray(loc);
    gl.glVertexAttribPointer(loc, size, type, false, stride, offset);
  }

  protected final void addElems (int vertIdx, int[] indices, int indicesOffset, int indicesLen,
                                 int indexBase) {
    short[] data = elements;
    int offset = elemPos;
    for (int ii = indicesOffset, ll = ii+indicesLen; ii < ll; ii++) {
      data[offset++] = (short)(vertIdx+indices[ii]-indexBase);
    }
    elemPos = offset;
  }

  private final void expandVerts (int vertCount) {
    int newVerts = vertices.length / vertexSize();
    while (newVerts < vertCount) newVerts += EXPAND_VERTS;
    vertices = new float[newVerts*vertexSize()];
  }

  private final void expandElems(int elemCount) {
    int newElems = elements.length;
    while (newElems < elemCount) newElems += EXPAND_ELEMS;
    elements = new short[newElems];
  }

  protected static int add (float[] into, int offset, float[] stables) {
    System.arraycopy(stables, 0, into, offset, stables.length);
    return offset + stables.length;
  }

  protected static int add (float[] into, int offset, float[] stables, int soff, int slen) {
    System.arraycopy(stables, soff, into, offset, slen);
    return offset + slen;
  }

  protected static int add (float[] into, int offset, float x, float y, float sx, float sy) {
    into[offset++] = x;
    into[offset++] = y;
    into[offset++] = sx;
    into[offset++] = sy;
    return offset;
  }

  protected static final int[] QUAD_INDICES = { 0, 1, 2, 1, 3, 2 };
}
