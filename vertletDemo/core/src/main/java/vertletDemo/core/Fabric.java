package vertletDemo.core;

import react.Value;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import playn.core.Clock;
import playn.scene.Layer;
import playn.scene.GroupLayer;
import playn.core.Surface;

public class Fabric {

  static final Value<Float> accY = Value.create(.143f); 

  public final class PointMass {

    float x, y, lastX, lastY, velX, initialX, initialY;
    boolean stationary;

    public PointMass(float posX, float posY, boolean stationary) {
      if (stationary) stationaryPointMasses.add(this);
      else movingPointMasses.add(this);
      initialX = posX;
      initialY = posY; 
      x = posX;
      y = posY;
      this.stationary = stationary;
      lastX = this.x;
      lastY = this.y;
      //accX = 0;
      //accY = .143f;  This is now reactive
    }

    void update() {
    //  float velX = x - lastX;
    //  float velY = y - lastY;
    //  // use inertia to find nextX and nextY
    //  float nextX = x + accX + velX; 
    //  float nextY = y + accY + velY; 
    
    // same as above but slightly more efficient
    //  float nextX = 2*x + accX - lastX;
      float nextX = 2*x - lastX;
      float nextY = 2*y + accY.get() - lastY;

      if (nextX < 0) nextX = 0;
      else if (nextX > VertletDemo.width) nextX = VertletDemo.width;
      if (nextY < 0) nextY = 0;
      else if (nextY > VertletDemo.height) nextY = VertletDemo.height;

      lastX = x;
      lastY = y;

      x = nextX;
      y = nextY;
    }
  }

  public final class Link {
    PointMass A, B;
    int tearingThreshold = 0;
    float restDist = restingDistance;

    public Link(PointMass A, PointMass B, float multiplier) {
      this.A = A;
      this.B = B;
      if ((A.stationary || B.stationary) && ! (A.stationary && B.stationary))  restDist = .4f * restingDistance;
      else restDist = multiplier * restingDistance;
    }

    void constrain() {
      float diffX = A.x - B.x;
      float diffY = A.y - B.y;

      float d = (float)Math.sqrt(diffX * diffX + diffY * diffY);

      // tearing
      if (d > 4 * restingDistance) {
        if (tearingThreshold++ > 3 && d > 6 * restingDistance) {
          if (Math.random() > .5) { 
            float xx = A.x;
            float yy = A.y;
            this.A = new PointMass(xx, yy, false); 
          } else {
            float xx = B.x;
            float yy = B.y;
            this.B = new PointMass(xx, yy, false); 
          }
          tearingThreshold = 0;
        }
      } else if (tearingThreshold > 0 && d < restDist)  tearingThreshold--;

      float difference = (restDist - d) / d;
    
      float translateX = diffX * .5f * difference;
      float translateY = diffY * .5f * difference;

      // saves a squareroot
      /*
      float dSquared = diffX * diffX + diffY * diffY;
      float difference = (restDSquared - dSquared) / dSquared;

      float translateX = diffX * .25f * difference;
      float translateY = diffY * .25f * difference;
      */

      A.x += translateX;
      A.y += translateY;

      B.x -= translateX;
      B.y -= translateY;

    }
  }

  public List<PointMass> pointMasses; 
  public List<PointMass> stationaryPointMasses = new ArrayList<>(); 
  public List<PointMass> movingPointMasses = new ArrayList<>(); 
  public static Link[] links; 

  GroupLayer groupLayer;
  float restingDistance;
 // float restDSquared; 
  public static int numLinks;

  public Fabric (GroupLayer groupLayer, float ulX, float ulY, float lrX, float lrY, float restingDistance) {
    
    this.restingDistance = restingDistance;
    //this.restDSquared = restingDistance * restingDistance;
    int numRows = (int)Math.floor((lrY - ulY) / restingDistance);
    int numCols = (int)Math.floor((lrX - ulX) / restingDistance);

    // Use an ArrayList here since, if we add tearing, we might have to add new PointMasses.
    pointMasses = new ArrayList<>();

    // Here, use an fixed-length Array, for speed.
    numLinks = (numCols-1)*numRows+(numRows-1)*numCols;// = 2*numRows*numCols-numRows-numCols
    System.out.println("numRows, numCols, numLinks = "+numRows+" "+numCols+" "+numLinks);
    links = new Link[numLinks];
    int k = 0;

    for (int i=0; i<numRows; i++) {
      for (int j=0; j<numCols; j++) {
        if (i == 0)  // fix the upper edge in place
          pointMasses.add(i * numCols + j, new PointMass(ulX + j * restingDistance, ulY + i * restingDistance, true));
        else
          //pointMasses.add(i * numCols + j, new PointMass(ulX + j * restingDistance, ulY + i * restingDistance, false));
          pointMasses.add(i * numCols + j, new PointMass(ulX + j * restingDistance, ulY + i * .95f*restingDistance, false));
        if (i == 0 && j > 0) 
          // top horizontal links
          links[k++] = new Link(pointMasses.get(j), pointMasses.get(j-1),1); 
        else if (i > 0 && j == 0) 
          // left edge vertical links
          links[k++]=new Link(pointMasses.get(i * numCols), pointMasses.get((i-1)*numCols),.6f+.4f*i/numRows); 
        else if (i != 0 && j != 0) {
          // horizontal links
          links[k++]=new Link(pointMasses.get(i * numCols + j), pointMasses.get(i * numCols + j - 1),1); 
          // vertical links
          links[k++]=new Link(pointMasses.get(i * numCols + j), pointMasses.get((i - 1) * numCols  + j),.6f+.4f*i/numRows); 
        }
      }
    }
    pointMasses.clear(); //pointMasses is only used to define the initial mesh correctly. (See the first two lines of
                         //the Link constructor
  }

  void update() {
    for (int i=0; i<3; i++ ) {
      for (int k=0; k<numLinks; k++) {
        links[k].constrain();
      }
      for (PointMass point: stationaryPointMasses) {
        point.x = point.initialX;
        point.y = point.initialY;
      }
    }
    for (PointMass point : movingPointMasses) {
      point.update();
    }
  }
}
