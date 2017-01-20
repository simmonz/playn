package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Body;
import org.jbox2d.collision.shapes.PolygonShape;

import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.core.Canvas;
import playn.core.Texture;
import playn.core.Platform;
import playn.core.Clock;

class KinematicRectangle extends MovingEntity {

  /**
   * Create a kinematic rectangular body
   *
   * @param width  the width w/r to physics dimensions
   * @param height  the height w/r to physics dimensions
   * @param posX  the x-coordinate of the center w/r to physics dimensions
   * @param posy  the y-coordinate of the center w/r to physics dimensions
   */
  KinematicRectangle(Platform plat, GroupLayer groupLayer, World world, float width, float height, float posX, float posY) { 
    super(plat, groupLayer, world, width, height, posX, posY);
  }

  @Override Body initRectanglePhysics(World world, float width, float height, float posX, float posY) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.KINEMATIC;
    bodyDef.position.set(posX, posY);
    body = world.createBody(bodyDef);
    PolygonShape rect = new PolygonShape();
    //SetAsBox takes half-width and half-height as arguments
    rect.setAsBox(width/2.0f, height/2.0f);
    body.createFixture(rect, 0);
    body.setLinearVelocity(new Vec2(1f,0));

    return body;
  }

  // make and attach a PlayN texture
  @Override ImageLayer initRectangleImage(Platform plat, GroupLayer groupLayer, float width, float height) {
    Canvas canvas = plat.graphics().createCanvas(width*PhysicsDemo.scale, height*PhysicsDemo.scale);
    canvas.setFillColor(0xFF000000).fillRect(0, 0, width*PhysicsDemo.scale, height*PhysicsDemo.scale);
    canvas.setFillColor(0xFFAA0000).fillRect(1, 1, width*PhysicsDemo.scale-2, height*PhysicsDemo.scale-2);
    Texture texture = canvas.toTexture();
    imageLayer = new ImageLayer(texture);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addAt(imageLayer, 0f, 0f);
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    return imageLayer;
  }

  void update() {
    Vec2 position = body.getPosition();
    if (position.x > 4*PhysicsDemo.physicsWidth/5) 
      body.setLinearVelocity(new Vec2(-1f,0));
    else if (position.x < PhysicsDemo.physicsWidth/5) 
      body.setLinearVelocity(new Vec2(1f,0));
    _update();
  }

  void paint(Clock clock) { _paint(clock); }
}
