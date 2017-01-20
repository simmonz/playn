package boidsDemo.core;

import java.util.ArrayList;
import java.util.List;

public abstract class Boids<T extends Boid> {

  List<T> list;

  /**
   * Constructor. 
   */
  public Boids() {
    list = new ArrayList<T>(); 
  }

  /**
   * Add boid to list.
   *
   * @param boid  a boid
   */
  public void add(T boid) {
    list.add(boid);
  }

  /** 
   * Cause the list of boids to flock together.
   *
   * Note that each boid gets passed the entire list of boids. That makes
   * sense since each boid's updated direction, velocity, and acceleration 
   * should depend on the specifics of nearby Boids.  
   */
  public void flock() {
    for (T boid : list) 
      boid.flock(list);
  }
}
