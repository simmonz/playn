package boidsDemo.core;

import java.util.List;

import static java.lang.Math.random;

/**
 * A Boid class.
 * <p>
 * Boids flock on a torus according the collective rules: 
 * <p><ul>
 * <li> separation
 * <li> alignment
 * <li> cohesion 
 * </ul><p>
 * Notes:
 * <p>
 * This is an abstract class.  Subclass this class with, for example,
 * a constructor with signature <code> Boid(float x, float y) </code> and which sets
 * the static variables screenWidth and screenHeight.
 * <p>
 * An instance of (a subclass) this class is a single boid. 
 * Use this class in conjunction with a subclass of {@link Boids}, an instance
 * of which is a list of boids that flock together.
 *
 * @author Scott Simmons
 *         2016-02-08
 */
public abstract class Boid {

  private static float screenWidth;
  private static float screenHeight; 

  private Vector2D position;
  private Vector2D velocity;
  private Vector2D acceleration;

  /**
   * the maximum steering force of each boid
   */
  static float maxForce = 0.03f; 

  /**
   * the maximum speed of each boid
   */
  static float maxSpeed = 2.0f;

  /**
   * the square of the radius of the neighborhood used in {@link Boid#separate}
   */
  static float separateRadiusSquared = 625.0f;  

  /**
   * the square of the radius of the neighborhood used in {@link Boid#align}
   */
  static float alignRadiusSquared = 2500.0f;  

  /**
   * the square of the radius of the neighborhood used in {@link Boid#cohesion}
   */
  static float cohesionRadiusSquared = 2500.0f;  

  /**
   * a mulitiplicative scaling factor that is applied to the separation steering vector
   * before computing a boids new position, velocity, and acceleration. 
   */
   static float separationScaleFactor = 1.5f;

  /**
   * a mulitiplicative scaling factor that is applied to the alignment steering vector
   * before computing a boids new position, velocity, and acceleration. 
   */
   static float alignmentScaleFactor = 1.0f;

  /**
   * a mulitiplicative scaling factor that is applied to the cohesion steering vector
   * before computing a boids new position, velocity, and acceleration. 
   */
   static float cohesionScaleFactor = 1.0f;

  /**
   * Constructor
   *
   * @param x            the x-coordinate of the boid's location
   * @param y            the x-coordinate of the boid's location
   * @param screenWidth  the width of the screen
   * @param screenHeight the height of the screen
   */
  public Boid (float x, float y, float screenWidth, float screenHeight) {

    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;

    position = new Vector2D(x, y);

    float angle = (float)random() * 2f * Vector2D.PI;
    float mag = 0.75f * maxSpeed + (float)random() * 0.25f * maxSpeed;
    velocity = new Vector2D(mag*Vector2D.cos(angle), mag*Vector2D.sin(angle));

    acceleration = new Vector2D(0, 0);

  }

  /**
   * Updates the position, velocity, and acceleration according to the three
   * rules of flocking.
   *
   * @param boids a list of boids
   */
  public void flock (List<? extends Boid> boids) {
    
    // Apply force. Here A = F/M  with M = 1.0
    // Note: can play with weights like the 1.5
    this.acceleration = this.acceleration.add(separate(boids).scale(separationScaleFactor));
    this.acceleration = this.acceleration.add(align(boids).scale(alignmentScaleFactor));
    this.acceleration = this.acceleration.add(cohesion(boids).scale(cohesionScaleFactor));

    this.velocity = this.velocity.add(this.acceleration);
    this.velocity = this.velocity.limit(maxSpeed);
    this.position = this.position.add(this.velocity); 
    this.acceleration = new Vector2D(0.0f,0.0f);
    modCoords();  // map the coordinates in this.position to the torus.
  }

  /**
   * Returns the magnitude-limited steering direction.
   *
   * @param desired  the desired direction to steer toward
   * @return         the {@link #maxForce}-limited {@link #maxSpeed}-limited desired 
   *                 minus this velocity
   */
  private Vector2D reynolds(Vector2D desired) {

    Vector2D steer = new Vector2D(0, 0);

    if (! desired.isZero()) {
      steer = desired.scale(maxSpeed / desired.norm()).sub(this.velocity).limit(maxForce);
    }  

    return steer; 
  } 

  /** 
   * Returns the steering vector that corresponds to separating this boid from its 
   * neighbors.
   *
   * @param boids  a list of boids 
   * @return       a force vector that will steer toward the weighted (by the inverse of 
   *               their distances to this boid) average of the postion vector from this 
   *               boid to the others who are near.
   */
  private Vector2D separate (List<? extends Boid> boids) {

    Vector2D steer = new Vector2D(0, 0);
    int count = 0; // counts the number of boids close to this boid

    for (Boid boid : boids) {
      if (boid != this) {
        Vector2D relativePosition 
                        = this.position.toralSub(boid.position, screenWidth, screenHeight);
        float squaredDistanceTo = relativePosition.normSquared();
        if (squaredDistanceTo < separateRadiusSquared) {
          steer = steer.add(relativePosition.scale(1/squaredDistanceTo));
          count++;
        }
      }
    }

    if (count > 0) 
      steer = steer.scale(1 / (float)count); 

    return reynolds(steer);
  }

  /**
   * Returns the steering vector that corresponds to aligning this boid with its neighbors.
   *
   * @param boids  a list of boids
   * @return       a force vector that will steer toward alignment of this boid's velocity
   *               with the average of it's neighbors velocities
   */
  private Vector2D align (List<? extends Boid> boids) {

    Vector2D steer = new Vector2D(0,0);
    int count = 0; 

    for (Boid boid : boids) {
      if (boid != this) {
        Vector2D relativePosition 
                         = this.position.toralSub(boid.position,screenWidth,screenHeight);
        float squaredDistanceTo = relativePosition.normSquared();
        if (squaredDistanceTo < alignRadiusSquared) {
          steer = steer.add(boid.velocity);
          count++;
        }
      }
    }

    if (count > 0) 
      steer = steer.scale(1 / (float)count); 
    return reynolds(steer);
  }

  /**
   * Return the steering force that will steer this toward a target position.
   *
   * @param target  the position vector of the target
   * @return        the steering force vector
   */
  private Vector2D seek(Vector2D target) {
    Vector2D desired = target.sub(this.position);// vector pointing from this boid to target
    desired = desired.scale(maxSpeed / desired.norm()); // adjust magnitude
    return desired.sub(this.velocity).limit(maxForce); // steer toward target and limit mag
  }

  /**
   * Returns the steering vector that corresponds to this boid being uniformly equally 
   * position with its neighbors; thus "gluing" the flock together.
   *
   * @param boids  a list of boids
   * @return       a force vector that will steer toward the average location of this boid 
   *               neighbors. 
   */
  private Vector2D cohesion (List<? extends Boid> boids) {

    Vector2D sum = new Vector2D(0,0);
    int count = 0;

    for (Boid boid : boids) {
      if (boid != this) {
        float squaredDistanceTo 
            = this.position.toralSub(boid.position,screenWidth,screenHeight).normSquared();
        if (squaredDistanceTo < cohesionRadiusSquared) {
          sum = sum.add(boid.position);
          count++;
        }
      }
    }

    if (count > 0)
      sum = seek(sum.scale(1 / (float)count));

    return sum;
  }

  private void modCoords () {
    position.x = Vector2D.mod(position.x, screenWidth);
    position.y = Vector2D.mod(position.y, screenHeight);
  }

  /**
   * Returns the x-coordinate of the position of the boid.
   */
  public float getX () {
    return position.x;
  }

  /**
   * Returns the y-coordinate of the position of the boid.
   */
  public float getY () {
    return position.y;
  }

  /**
   * Returns the x-coordinate of the position of the boid.
   */
  public float getVelX () {
    return velocity.x;
  }

  /**
   * Returns the y-coordinate of the position of the boid.
   */
  public float getVelY () {
    return velocity.y;
  }

  /**
   * Returns the angle of rotation the velocity vector of the boid.
   *
   * @return the arctangent of - y-component / x-component
   */
  public float getTheta () {
    return Vector2D.atan2(velocity.x, -velocity.y);
  }
}

