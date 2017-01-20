package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Body;
import org.jbox2d.collision.shapes.PolygonShape;

import playn.scene.GroupLayer;
import playn.scene.Layer;
import playn.core.Surface;

class StaticRectangle {

  /**
   * Create a static rectangular body
   *
   * @param physicsLL  the lower-left physical coordinates of the rectangle
   * @param physicsUR  the upper-right physical coordinates of the rectangle
   */
  StaticRectangle (GroupLayer layer, World world, Vec2 physicsLL, Vec2 physicsUR, final int color) { 

    BodyDef bodyDef = new BodyDef();
    bodyDef.position.set((physicsUR.x + physicsLL.x)/2f, (physicsUR.y + physicsLL.y)/2f);
    Body body = world.createBody(bodyDef);
    PolygonShape rect = new PolygonShape();
    //SetAsBox takes half-width and half-height as arguments
    rect.setAsBox((physicsUR.x - physicsLL.x)/2.0f, (physicsUR.y - physicsLL.y)/2.0f);
    body.createFixture(rect, 0);

    // make a rectangle representing the ground
    final Vec2 upperLeft = PhysicsDemo.toSceneCoords(physicsLL);
    final Vec2 lowerRight = PhysicsDemo.toSceneCoords(physicsUR);
    layer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFF000000).fillRect(upperLeft.x, upperLeft.y, lowerRight.x-upperLeft.x, lowerRight.y-upperLeft.y);
        surf.setFillColor(color).fillRect(upperLeft.x+3, upperLeft.y+3, lowerRight.x-upperLeft.x-6, lowerRight.y-upperLeft.y);
      }
    });
  }
}
