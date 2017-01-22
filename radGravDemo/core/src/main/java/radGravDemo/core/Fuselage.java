package radGravDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.collision.shapes.PolygonShape;

import playn.scene.ImageLayer;
import playn.core.Image;
import playn.core.Tile;
import playn.core.Texture;
import playn.core.Canvas;
import playn.core.Platform;
import playn.scene.GroupLayer;
import playn.core.Clock;

/**
 * Fuselage class
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
class Fuselage extends RemovableEntity {

  Fuselage(Platform plat, GroupLayer groupLayer, World world, Vec2 position, Vec2 offset) {
    super(plat, groupLayer, world, position, offset);
  }

  @Override
  protected ImageLayer initFuselageImage(Platform plat, GroupLayer groupLayer) {
    Canvas canvas = plat.graphics().createCanvas(Ship.fuselageWidth * RadGravDemo.scalePhysToScene, 
                                                 Ship.fuselageHeight * RadGravDemo.scalePhysToScene);
    float sw = .5f * RadGravDemo.maxZoom;
    canvas.setFillColor(0xFF000000).fillRect(0, 0, Ship.fuselageWidth * RadGravDemo.scalePhysToScene, 
                                                  Ship.fuselageHeight * RadGravDemo.scalePhysToScene);
    canvas.setFillColor(0xFFF8F8F8).fillRect(sw, sw, Ship.fuselageWidth * RadGravDemo.scalePhysToScene - 2*sw, 
                                                  Ship.fuselageHeight * RadGravDemo.scalePhysToScene - 2*sw);
    Texture texture = canvas.toTexture();
    imageLayer = new ImageLayer(texture);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addAt(imageLayer, 0f, 0f);
    Vec2 scenePosition = RadGravDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    return imageLayer; 
  }

  @Override
  protected Body initFuselagePhysics(World world, Vec2 position, Vec2 offset) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position.set(position.add(offset));

    Body body = world.createBody(bodyDef);

    PolygonShape rect = new PolygonShape(); 
    rect.setAsBox(Ship.fuselageWidth/2f, Ship.fuselageHeight/2f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = rect;
    fixtureDef.density = 1;
    Fixture fuselageFixture = body.createFixture(fixtureDef);

    return body;
  }

  protected void update() { _updatePosition(); _updateRotation(); }
  protected void paint(Clock clock) { _paintPosition(clock); _paintRotation(clock); }
}
