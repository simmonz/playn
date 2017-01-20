package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.collision.shapes.PolygonShape;

import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.core.Canvas;
import playn.core.Texture;
import playn.core.Platform;
import playn.core.Clock;

class DynamicRectangle extends RemovableEntity {

  /**
   * Create a dynamic rectangular body
   *
   * @param width  the width w/r to physics dimensions
   * @param height  the height w/r to physics dimensions
   * @param posX  the x-coordinate of the center w/r to physics dimensions
   * @param posy  the y-coordinate of the center w/r to physics dimensions
   */
  DynamicRectangle (Platform plat, GroupLayer groupLayer, World world, float width, float height, float posX, float posY) { 
    super(plat, groupLayer, world, width, height, posX, posY);
  }

  @Override ImageLayer initRectangleImage(Platform plat, GroupLayer groupLayer, float width, float height) {
    // make and attach a PlayN texture
    Canvas canvas = plat.graphics().createCanvas(width*PhysicsDemo.scale, height*PhysicsDemo.scale);
    canvas.setFillColor(0xFF000000).fillRect(0, 0, width*PhysicsDemo.scale, height*PhysicsDemo.scale);
    canvas.setFillColor(0xFFffd000).fillRect(1, 1, width*PhysicsDemo.scale-2, height*PhysicsDemo.scale-2);
    Texture texture = canvas.toTexture();
    imageLayer = new ImageLayer(texture);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addAt(imageLayer, 0f, 0f);
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    return imageLayer;
  }

  @Override Body initRectanglePhysics(World world, float width, float height, float posX, float posY) {

    // Create a JBox2D body definition for this rectangle.
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position.set(posX, posY);

    // Define a JBox2D shape of the rectangle.
    PolygonShape rect = new PolygonShape();   
    // setAsBox takes half-width and half-height as arguments
    rect.setAsBox(width/2.0f, height/2.0f);

    // Define a JBox2D fixture for this rectangle. 
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = rect;
    fixtureDef.density = 0.4f;
    fixtureDef.friction = 0.5f;
    fixtureDef.restitution = 0.65f;

    // Create the Body and the Fixture for the rectangle.
    body = world.createBody(bodyDef);
    body.createFixture(fixtureDef);
    body.setAngularVelocity(0.2f);

    return body;
  }

  void update() { _updateWithRotation(); }
  void paint(Clock clock) { _paintWithRotation(clock); }
}

