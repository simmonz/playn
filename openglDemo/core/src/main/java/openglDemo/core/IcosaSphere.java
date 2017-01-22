package openglDemo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Generate and multiply subdivide an icosahedron, pushing the vertices out to a sphere.
 *
 * @author SSimmons
 *         2016.05.01
 * @version 1.0
 */
public class IcosaSphere {
  /*
   * A little math:
   *
   * The sphere has genus g = 0 and, hence, for a sphere, the Euler characteristic Chi = 2-2g = 2.
   * To say the same thing: any triangulation of the sphere satisfies
   *      number of faces - number of edges + number of vertices = 2.
   * The icosahedron, which is essentially a triangulation of a sphere, has 20 faces and 12 vertices
   * and, therefore, 20+12-2 = 30 edges.  
   *
   * When subdividing, we replace each isosceles triangular face with four smaller such faces whose
   * vertices are the original plus the midpoints of the edges.
   *
   * After one subdivision we have 4*20 = 80 faces, 2*30+3*20 = 120 edges, and 2-80+120=42 vertices.  
   *
   * Generally, let F_n, E_n, and V_n represent the number of faces, edges and vertices after n (n>=0) 
   * subdivisions. We have 
   *               F_n = 4^n*20,  E_n = 2*E_(n-1)+3*F_(n-1),  V_n = V_(n-1)+E_(n-1)
   */

  public class Vertex {
    public final float x, y, z;

    // creates a vertex whose length as a vector is lambda
    public Vertex(float x, float y, float z, float lambda) {
      float length = (float)Math.sqrt(x*x+y*y+z*z);
      this.x = lambda*x/length; this.y = lambda*y/length; this.z = lambda*z/length;
    }
  }

  // a pair of integers a, b with a<=b where a and b are indices -- so an unoriented edge
  // (this is used to avoid duplicating midpoint vertices when subdividing) 
  public class Edge {

    final int a, b;

    public Edge(int x, int y) {
      if (x < y) { a = x; b =y;}
      else {a = y; b =x;}
    }

    // Note: this equals (along with hashcode below is sufficient for our purposes but 
    // wouldn't be if we had subclasses of Edge.
    @Override public boolean equals(Object other) {
      boolean result = false;
      if (other instanceof Edge) {
        Edge that = (Edge) other;
        result = (this.a == that.a && this.b == that.b);
      }
      return result;
    }

    @Override public int hashCode() {
      return (41 * (41 + this.a) + this.b);
    }
  }


  // subdivides a face, maintaining correct orientation
  public int[] subDivideFaces (int[] faces, float lambda) {

    // We are replacing each face with 4 small faces -- we get (4-1)*num_old_faces total small faces
    int[] newFaces = new int[4*faces.length];

    HashMap<Edge,Integer> existingMidpoints = new HashMap<>(); 

    int[] m = new int[3]; // temporarily holds indices of midpoints in verts

    // for each face
    for (int i=0; i<faces.length; i+=3) {


      // Get the midpoint of each edge:  either create a new vertex or, if it already exists, get index of it.
      // Put the edge of any newly created midpoint into the hashamp so we can check the hashmap to see if a 
      // midpoint already exists.
      for (int j=0; j<3; j++) {
        Edge edge = new Edge(faces[i+j], faces[i+((j+1) % 3)]);
        Integer mj = existingMidpoints.get(edge);
        if (mj == null) {
          Vertex newMidpoint = new Vertex((verts.get(faces[i+j]).x+verts.get(faces[i+((j+1) % 3)]).x)/2, 
                                          (verts.get(faces[i+j]).y+verts.get(faces[i+((j+1) % 3)]).y)/2, 
                                          (verts.get(faces[i+j]).z+verts.get(faces[i+((j+1) % 3)]).z)/2, lambda);
          int currentSize = verts.size();
          verts.add(currentSize, newMidpoint);
          m[j] = currentSize;
          existingMidpoints.put(edge, currentSize);
        } else { 
          m[j] = mj; 
        }
      } 

      // add the four new small faces
      int startIdx = 12*i/3;
      newFaces[startIdx] = faces[i];
      newFaces[startIdx+1] = m[0];
      newFaces[startIdx+2] = m[2];
      newFaces[startIdx+3] = faces[i+1];
      newFaces[startIdx+4] = m[1];
      newFaces[startIdx+5] = m[0];
      newFaces[startIdx+6] = faces[i+2];
      newFaces[startIdx+7] = m[2];
      newFaces[startIdx+8] = m[1];
      newFaces[startIdx+9] = m[0];
      newFaces[startIdx+10] = m[1];
      newFaces[startIdx+11] = m[2];

    }

    return newFaces;
  }

  // verts contains all of the vertices
  public List<Vertex> verts = new ArrayList<>(); 

  // indices[] contains the triangular faces
  // The vertices of the triangles are verts[3i], verts[3i+1], verts[3i+2], i = 0, 1, 2, ...
  public int[] indices;

  public IcosaSphere(int depth, float lambda, boolean seamless)  {

    int[] newIndices;

    // generate an icosahedron.  If seamless, then add two more vertices and four more faces so that
    // the intersection of the icosahedron with the plane z=0 consists completely of edges.

    if (seamless) 
      indices = new int[72]; // 24 faces
    else
      indices = new int[60]; // 20 faces

    float phi = (1f + (float)Math.sqrt(5))/2f;

    // define the vertices:
    verts.add(0, new Vertex(-1f,  phi, 0, lambda)); 
    verts.add(1, new Vertex( 1f,  phi, 0, lambda)); 
    verts.add(2, new Vertex(-1f, -phi, 0, lambda)); 
    verts.add(3, new Vertex( 1f, -phi, 0, lambda)); 

    verts.add(4, new Vertex(0, -1f,  phi, lambda)); 
    verts.add(5, new Vertex(0,  1f,  phi, lambda)); 
    verts.add(6, new Vertex(0, -1f, -phi, lambda)); 
    verts.add(7, new Vertex(0,  1f, -phi, lambda)); 

    verts.add(8, new Vertex( phi, 0, -1f, lambda)); 
    verts.add(9, new Vertex( phi, 0,  1f, lambda)); 
    if (seamless) {
      verts.add(10, new Vertex(-phi, 0.0001f, -1f, lambda)); 
      verts.add(11, new Vertex(-phi, 0.0001f,  1f, lambda)); 
      verts.add(12, new Vertex(-phi, -0.0001f, -1f, lambda)); 
      verts.add(13, new Vertex(-phi, -0.0001f,  1f, lambda)); 
      verts.add(14, new Vertex(0, 0, phi, lambda)); // midpoint 4 to 5
      verts.add(15, new Vertex(0, 0, -phi, lambda)); // midpoint 6 to 7
    } else {
      verts.add(10, new Vertex(-phi, 0, -1f, lambda)); 
      verts.add(11, new Vertex(-phi, 0,  1f, lambda)); 
    }

    // define the triangular faces:
    
    // the faces forming the cap around verts[0] oriented counterclockwise looking from the outside:
    indices[0] = 0; indices[1] = 11; indices[2] = 5; 
    indices[3] = 0; indices[4] = 5; indices[5] = 1; 
    indices[6] = 0; indices[7] = 1; indices[8] = 7; 
    indices[9] = 0; indices[10] = 7; indices[11] = 10; 
    indices[12] = 0; indices[13] = 10; indices[14] = 11; 

    // the five faces sharing in edge with the cap around verts[0] oriented the same as above:
    indices[15] = 1; indices[16] = 5; indices[17] = 9; 
    if (seamless) {
      indices[18] = 5; indices[19] = 11; indices[20] = 14; 
      indices[60] = 14; indices[61] = 13; indices[62] = 4; 
    } else {
      indices[18] = 5; indices[19] = 11; indices[20] = 4; 
    }
    if (seamless) {
      indices[21] = 13; indices[22] = 12; indices[23] = 2; 
    } else {
      indices[21] = 11; indices[22] = 10; indices[23] = 2; 
    }
    if (seamless) {
      indices[24] = 10; indices[25] = 7; indices[26] = 15; 
      indices[66] = 12; indices[67] = 15; indices[68] = 6; 
    } else {
      indices[24] = 10; indices[25] = 7; indices[26] = 6; 
    }
    indices[27] = 7; indices[28] = 1; indices[29] = 8; 

    // the five faces forming the cap around verts[3] (which is antipodal to verts[0]) oriented as above:
    indices[30] = 3; indices[31] = 9; indices[32] = 4; 
    indices[33] = 3; indices[34] = 4; indices[35] = 2; 
    indices[36] = 3; indices[37] = 2; indices[38] = 6; 
    indices[39] = 3; indices[40] = 6; indices[41] = 8; 
    indices[42] = 3; indices[43] = 8; indices[44] = 9; 

    // the five faces sharing in edge with the cap around verts[3] oriented the same as above:
    if (seamless) {
      indices[45] = 14; indices[46] = 9; indices[47] = 5; 
      indices[63] = 14; indices[64] = 9; indices[65] = 4; 
    } else {
      indices[45] = 4; indices[46] = 9; indices[47] = 5; 
    }
    if (seamless) {
      indices[48] = 2; indices[49] = 4; indices[50] = 13; 
    } else {
      indices[48] = 2; indices[49] = 4; indices[50] = 11; 
    }
    if (seamless) {
      indices[51] = 6; indices[52] = 2; indices[53] = 12; 
    } else {
      indices[51] = 6; indices[52] = 2; indices[53] = 10; 
    }
    if (seamless) {
      indices[54] = 8; indices[55] = 15; indices[56] = 7; 
      indices[69] = 8; indices[70] = 6; indices[71] = 15; 
    } else {
      indices[54] = 8; indices[55] = 6; indices[56] = 7; 
    }
    indices[57] = 9; indices[58] = 8; indices[59] = 1; 

    for (int i=0; i<depth; i++) {
      newIndices = subDivideFaces(indices, lambda);
      indices = new int[newIndices.length];
      System.arraycopy(newIndices, 0, indices, 0, newIndices.length);
    }

    System.out.println("length indices is: "+indices.length+". So "+indices.length/3+" triangles.");
    System.out.println("length verts is: "+verts.size());
  }
}
