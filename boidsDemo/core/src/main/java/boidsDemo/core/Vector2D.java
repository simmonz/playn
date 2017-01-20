package boidsDemo.core;

import java.lang.Math.*;

/**
* A 2D float vector class.
*
* <P> Vector math, geometry, and functions.
* 
* @author  SSimmons
* @version 2016-02-01
*/
public class Vector2D {

  public static final float PI = (float) Math.PI;
  public static final float TWO_PI = 2 * PI;
  public static final float HALF_PI = PI / 2;

  private static final int ATAN2_BITS = 7;
  private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
  private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
  private static final int ATAN2_COUNT = ATAN2_MASK + 1;
  private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
  private static final float ATAN2_DIM_MINUS_1 = ATAN2_DIM - 1;
  private static final float[] atan2 = new float[ATAN2_COUNT];
  private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
  private static final float radFull, radToIndex;
  private static final float[] sin, cos;

  static {
    for (int i = 0; i < ATAN2_DIM; i++) {
      for (int j = 0; j < ATAN2_DIM; j++) {
        float x0 = (float) i / ATAN2_DIM;
        float y0 = (float) j / ATAN2_DIM;
        atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
      }
    }

    SIN_BITS = 12;
    SIN_MASK = ~(-1 << SIN_BITS);
    SIN_COUNT = SIN_MASK + 1;

    radFull = (float) (Math.PI * 2.0);
    radToIndex = SIN_COUNT / radFull;

    sin = new float[SIN_COUNT];
    cos = new float[SIN_COUNT];

    for (int i = 0; i < SIN_COUNT; i++) {
      sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
      cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
    }
  }

  float x, y;

  /** 
   * Fast but not incredibly accurate square root of a float.
   *
   * @param x  the float to be rooted
   * @return   the square root
  public static final float sqrt(float x) {
    return Float.intBitsToFloat(532483686 + (Float.floatToRawIntBits(x)>> 1));  
  }
   */
  // just use Math.sqrt for html target
  public static final float sqrt(float x) {
    return (float)Math.sqrt((float)x);
  }

  /** 
   * Returns absolute value of a float.
   *
   * @param x  the float
   * @return   the absolute value
   */
  public static final float abs(float x) {
    return (x <= 0.0f) ? 0.0f - x : x;
  }

  /** 
   * Returns minimum of two floats. 
   * 
   * @param x  a float
   * @param y  a float
   * @return   the minimum 
   */
  public static final float min(float x, float y) {
    return (x <= y) ?  x : y;
  }

  /** 
  * A float version of modulo.
  *
  * @param x    a float
  * @param dim  a dimension, like the (float cast of) the screen width or height
  * @return     x modulo dim.
  */
  public static final float mod(float x, float dim) {
    return (x > dim) ? (x - dim) : ((x < 0) ? (x + dim) : x);
  }

  /**
  * The sole constructor.
  *
  * @param x  the x-coordinate
  * @param y  the y-coordinate
  */
  public Vector2D (float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the vector sum of this vector and other vector.
   *
   * @param other  a vector
   * @return       the vector sum  
   */
  public final Vector2D add(Vector2D other) {
    return new Vector2D(this.x + other.x, this.y + other.y); 
  }

  /**
   * Returns this vector minus other vector.
   *
   * @param other  a vector
   * @return       the vector difference 
   */
  public final Vector2D sub(Vector2D other) {
    return new Vector2D(this.x - other.x, this.y - other.y); 
  }

  /**
   * Checks if this vector is exactly the zero vector.
   *
   * @return true if and only if the vector is the zero vector
   */
  public final boolean isZero() {
    return (this.x == 0.0f) && (this.y == 0.0f); 
  }

  /**
   * Checks whether this vector is equal to other vector.
   *
   * @param other  a vector
   * @return       true if and only if the vectors have the same components
   */
  public final boolean equals(Vector2D other) {
    return this.sub(other).isZero();
  }

  /**
   * Translates the coordinate of vector from the [0, width] x [0, height] fundamental domain 
   * of the torus to equivalent coordinates on the [-width/2, width/2] x [-height/2, height/2]
   * fundmental domain. 
   *
   * @param vector  the position vector of a point on the torus 
   * @param width   the width of the square being glued
   * @param height  the height of the square being glued
   * @return        the position vector of the point in translated coordinates 
   */
  private final Vector2D recenter(Vector2D vector, float width, float height) {
    Vector2D translated = new Vector2D(vector.x , vector.y);
    if (vector.x > width/2) translated.x = vector.x - width;
    if (vector.y > height/2) translated.y = vector.y - height;
    return translated;
    }

  /**
   * Returns the shortest vector difference (on the torus) between this vector and other vector;
   * shortest in that, if we consider the points on the Torus with position vectors this vector 
   * and other vector, the shortest distance on the torus between the points is the magnitude 
   * of the vector difference.
   * <p>
   * Here the torus is rectangle with top and bottom sown together, and left 
   * and right edges glued together.
   *
   * @param other   the position vector
   * @param width   the width of the square being glued
   * @param height  the height of the square being glued
   * @return        the shortest vector difference
   */
  public final Vector2D toralSub(Vector2D other, float width, float height) {
    Vector2D diff = this.sub(other);
    Vector2D translateThenSub = recenter(this, width, height).sub(recenter(other, width, height));
    if (diff.normSquared() <= translateThenSub.normSquared()) 
      return diff;
    else
      return translateThenSub;
  }

  /* another version of toralSub:
  public final Vector2D toralSub(Vector2D other, float width, float height) {
    float dx = this.x - other.x;
    float dy = this.y - other.y;
    float absDx = abs(dx);
    float absDy = abs(dy);
    if (absDx < width - absDx && absDy < width - absDy) return this.sub(other); 
    else {
      if (absDx > width - absDx) {
        if (this.x <= other.x)  dx = other.x - (this.x + width);
        else  dx = other.x + width - this.x; 
      }
      if (absDy > height - absDy)  {
        if (this.y <= other.y)  dy = other.y - (this.y + height);
        else  dy = other.y + height - this.y; 
      }
      return new Vector2D(-dx, -dy);     
    }
  } */

  /**
   * Returns the shortest distance on the torus between the points with position vectors this vector
   * and other vector.
   * <p>
   * Note: this is the magnitude of vector returned by {@link #toralSub}, computed indirectly.
   *
   * @param other   the position vector
   * @param width   the width of the square being glued
   * @param height  the height of the square being glued
   * @return        the shortest distance 
   */
  public final float toralNorm(Vector2D other, float width, float height) {
    float dx = abs(this.x - other.x);
    float dy = abs(this.y - other.y);
    float minDx = min(dx, width - dx);
    float minDy = min(dy, height - dy); 
    return minDx * minDx + minDy * minDy; 
  }

  /** 
   * Returns the magnitude squared of this vector.
   *
   * @return the magnitude squared 
   */
  public final float normSquared() {
    return this.x * this.x + this.y * this.y;
  } 

  /** 
   * Returns the magnitude of this vector.
   *
   * @return the magnitude
   */
  public final float norm() {
    return sqrt(this.normSquared());
  }

  /**
   * Returns this vector multiplied by scalar.
   *
   * @param scalar  a scalar
   * @return        the scaled vector
   */
  public final Vector2D scale(float scalar) {
    return new Vector2D(this.x * scalar, this.y * scalar);
  }

  /**
   * Returns this vector if the length of this vector is less than 
   * or equal to the specified maximum; otherwise returns this vector 
   * with length adjusted to exactly maximum.
   *
   * @param max  a positive scalar
   * @return     the limited length vector
   */ 
  public final Vector2D limit(float max) {
    float norm = this.norm();
    if (norm <= max)
      return this.scale(1.0f);
    else
      return this.scale(max / norm);
  }

  /**
   * Returns same as {@link Math#sin(double)} but a lot faster and a bit less accurate.
   * 
   * @param rad  the angle in radians
   * @return     the sine of the angle 
   */
  public static final float sin(float rad) {
    return sin[(int) (rad * radToIndex) & SIN_MASK];
  }

  /**
   * Returns same as {@link Math#cos(double)} but a lot faster and a bit less accurate.
   * 
   * @param rad  the angle in radians
   * @return     the cosine of the angle
   */
  public static final float cos(float rad) {
    return cos[(int) (rad * radToIndex) & SIN_MASK];
  }

  /**
   * Returns the angle in polar coordinates associated to the point with rectangular
   * coordinates (x,y). 
   *
   * @param y  y-coordinate
   * @param x  x-coordinate
   * @return   the angle between the vector with components x and y, and the positive x-axis.
   */
  public static final float atan2(float y, float x) {
    float add, mul;

    if (x < 0.0f) {
      if (y < 0.0f) {
        x = -x;
        y = -y;

        mul = 1.0f;
      } else {
        x = -x;
        mul = -1.0f;
      }
      add = -3.141592653f;
    } else {
      if (y < 0.0f) {
        y = -y;
        mul = -1.0f;
      } else {
        mul = 1.0f;
      }
      add = 0.0f;
    }
    float invDiv = ATAN2_DIM_MINUS_1 / (x < y ? y : x);
    int xi = (int) (x * invDiv);
    int yi = (int) (y * invDiv);
    return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
  }

  /**
   * Converts degrees to radians.
   *
   * @param degrees  degree measure of an angle
   * @return         the radians value
   */
  public static final float toRadians(float degrees) {
    return degrees / 180.0f * PI;
  }

  /**
   * Converts radians to degrees.
   *
   * @param radians  radian measure of an angle
   * @return         the degrees value
   */
  public static final float toDegrees(float radians) {
    return radians * 180.0f / PI;
  }
}
