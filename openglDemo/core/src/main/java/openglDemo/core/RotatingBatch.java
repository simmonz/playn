package openglDemo.core;

import playn.core.GL20;
import playn.core.Texture;
import pythagoras.f.AffineTransform;

/**
 * Generate a rotating batch
 *
 * @author SSimmons
 *         2016.05.01
 * @version 1.0
 */
public class RotatingBatch extends Triangle3dBatch {

  public float angle;
  public float eyeX, eyeY;
  private final int uAngle, uEye;

  public RotatingBatch(GL20 gl, final float zScale) {
    super(gl, new Source ()  {@Override public String vertex () {
      return RotatingBatch.vertex(zScale);
    }});
    uAngle = program.getUniformLocation("u_Angle");
    uEye = program.getUniformLocation("u_Eye");
  }

  @Override public void begin (float fbufWidth, float fbufHeight, boolean flip) {
    super.begin(fbufWidth, fbufHeight, flip);
    program.activate();
    gl.glUniform1f(uAngle, angle);
    gl.glUniform2f(uEye, eyeX, eyeY);
  }

  static String vertex (float zScale) {
    return Triangle3dBatch.Source.VERT_UNIFS +
      "uniform float u_Angle;\n" +
      "uniform vec2 u_Eye;\n" +
      Triangle3dBatch.Source.VERT_ATTRS +

      "attribute vec3 a_Position;\n" +
      "attribute vec2 a_TexCoord;\n" +

      Triangle3dBatch.Source.VERT_VARS +

      "void main(void) {\n" +
      // Transform the vertex per the normal screen transform
      "  mat4 transform = mat4(\n" +
      "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
      "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
      "    0,                0,                1, 0,\n" +
      "    a_Translation[0], a_Translation[1], 0, 1);\n" +
      "  vec4 pos = transform * vec4(a_Position, 1);\n" +

     // Rotate the vertex per our 3D rotation
      "  float cosa = cos(u_Angle);\n" +
      "  float sina = sin(u_Angle);\n" +
      "  mat4 rotmat = mat4(\n" +
      "    cosa, 0, sina, 0,\n" +
      "    0,    1, 0,    0,\n" +
      "   -sina, 0, cosa, 0,\n" +
      "    0,    0, 0,    1);\n" +
      "  pos = rotmat * vec4(pos.x - u_Eye.x,\n" +
      "                      pos.y - u_Eye.y,\n" +
      "                      pos.z, 1);\n" +

      // Perspective project the vertex back into the plane
      /*
      "  mat4 persp = mat4(\n" +
      "    1, 0, 0, 0,\n" +
      "    0, 1, 0, 0,\n" +
      "    0, 0, 1, -1.0/2000.0,\n" +
      "    0, 0, 0, 1);\n" +
      "  pos = persp * pos;\n" +
      */
      "  pos += vec4(u_Eye.x,\n" +
      "              u_Eye.y, 0, 0);\n" +

      // Finally convert the coordinates into OpenGL space
      "  pos.xy /= u_HScreenSize.xy;\n" +
      "  pos.z  /= (u_HScreenSize.x * " + format(zScale) + ");\n" +
      "  pos.xy -= 1.0;\n" +
      // z may already be rotated into negative space so we don't shift it
      "  pos.y  *= u_Flip;\n" +
      "  gl_Position = pos;\n" +


      Triangle3dBatch.Source.VERT_SETTEX +
      Triangle3dBatch.Source.VERT_SETCOLOR +
      "}";
  }

  @Override public void addTris (float[] xys, int xysOffset, int xysLen, float tw, float th,
                       int[] indices, int indicesOffset, int indicesLen, int indexBase) {
    int vertIdx = beginPrimitive(xysLen/3, indicesLen), offset = vertPos;
    float[] verts = vertices, stables = stableAttrs;
    for (int ii = xysOffset, ll = ii+xysLen; ii < ll; ii += 3) {
      float x = xys[ii], y = xys[ii+1], z = xys[ii+2];
      //offset = add(verts, add(verts, offset, stables), x, y, z, x/tw, y/th);

      // Spherical coords:
      float rho = (float)Math.sqrt(x*x+y*y+z*z);
      float u = (float)(Math.atan2(x,z)/(2*Math.PI)+.5f);
      float v = (float)(1-Math.acos(y/rho)/Math.PI);
      offset = add(verts, add(verts, offset, stables), x, y, z, u, v);
    }
    vertPos = offset;

    addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase);
  }

  protected static int add (float[] into, int offset, float x, float y, float z, float sx, float sy) {
    into[offset++] = x;
    into[offset++] = y;
    into[offset++] = z;
    into[offset++] = sx;
    into[offset++] = sy;
    return offset;
  }

  public static String format (float value) {
    String fmt = String.valueOf(value);
    return fmt.indexOf('.') == -1 ? (fmt + ".0") : fmt;
  }
}
