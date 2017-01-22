package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.collision.shapes.CircleShape;

import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.core.Tile;
import playn.core.Platform;
import playn.core.Clock;

/**
 * Bubble class 
 *
 * @author Scott Simmons
 *         2016.03.01
 * @version 1.0
 */
class Bubble extends RemovableEntity {

  Bubble (GroupLayer groupLayer, World world, String size, float posX, float posY) {
    super(world, groupLayer, size, posX, posY); 
  }

  @Override ImageLayer initBubbleImage(GroupLayer groupLayer, String size) {

    // Get a tile of size the String argument size.
    Tile tile = BubbleTextures.bubbleTiles.get(size);

    // Create a PlayN layer for the bubble.
    imageLayer= new ImageLayer(tile);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addCenterAt(imageLayer, 0f, 0f);
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    return imageLayer;
  }

  @Override Body initBubblePhysics(World world, String size, float posX, float posY) {

    // Create a JBox2D body definition for this bubble.
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC; // There are 3 types of bodies in JBox2D: static, kinematic, and dynamic. 
    bodyDef.position.set(posX, posY);

    // Define a JBox2D shape of the bubble.
    CircleShape circle = new CircleShape();
    circle.m_radius = BubbleTextures.sizes.get(size)/PhysicsDemo.scale; 

    // Define a JBox2D fixture for this bubble.
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = circle;
    fixtureDef.density = 0.1f;
    fixtureDef.friction = 0.3f;
    fixtureDef.restitution = 0.85f; // 0, no bounce; 1 bounces forever

    // Create the Body and the Fixture for the bubble.
    body = world.createBody(bodyDef);
    body.createFixture(fixtureDef);

    return body;
  }

  @Override void update() { _update(); }
  @Override void paint(Clock clock) { _paint(clock); }
}
