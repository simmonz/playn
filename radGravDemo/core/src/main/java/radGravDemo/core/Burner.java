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
 * Burner class
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
class Burner extends RemovableEntity {

  private Vec2 localInsidePoint, localOutsidePoint;

  Burner(Platform plat, GroupLayer groupLayer, World world, Vec2 position, Vec2 offset) {
    super(plat, groupLayer, world, position, offset);
    if (offset.x > 0) {
      localInsidePoint =  new Vec2(-Ship.burnerWidth/2, -Ship.burnerHeight/2);
      localOutsidePoint =  new Vec2(Ship.burnerWidth/2, -Ship.burnerHeight/2);
    } else {
      localOutsidePoint =  new Vec2(-Ship.burnerWidth/2, -Ship.burnerHeight/2);
      localInsidePoint =  new Vec2(Ship.burnerWidth/2, -Ship.burnerHeight/2);
    }
  }

  @Override
  protected ImageLayer initBurnerImage(Platform plat, GroupLayer groupLayer) {
    Canvas canvas = plat.graphics().createCanvas(Ship.burnerWidth * RadGravDemo.scalePhysToScene, 
                                                 Ship.burnerHeight * RadGravDemo.scalePhysToScene);
    float sw = .5f*RadGravDemo.maxZoom;
    canvas.setFillColor(0xFF000000).fillRect(0, 0, Ship.burnerWidth * RadGravDemo.scalePhysToScene, 
                                                 Ship.burnerHeight * RadGravDemo.scalePhysToScene);
    canvas.setFillColor(0xFFF8F8F8).fillRect(sw, sw, Ship.burnerWidth * RadGravDemo.scalePhysToScene-2f*sw, 
                                                 Ship.burnerHeight * RadGravDemo.scalePhysToScene-2f*sw);
    Texture texture = canvas.toTexture();
    imageLayer = new ImageLayer(texture);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addAt(imageLayer, 0f, 0f);
    Vec2 scenePosition = RadGravDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    return imageLayer; 
  }

  @Override
  protected Body initBurnerPhysics(World world, Vec2 position, Vec2 offset) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position.set(position.add(offset));

    Body body = world.createBody(bodyDef);

    PolygonShape rect = new PolygonShape(); 
    rect.setAsBox(Ship.burnerWidth/2f, Ship.burnerHeight/2f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = rect;
    fixtureDef.density = 1;
    Fixture leftBurnerFixture = body.createFixture(fixtureDef);

    return body;
  }

  /** 
   * Returns physics coordinate of a point on the bottom edge of the left burner: t = 0 returns
   * the innnermost point; t = 1 the outermost. 
   */ 
  protected Vec2 burnerPos(float t) {
    return body.getWorldPoint((localInsidePoint.mul(1f - t)).add(localOutsidePoint.mul(t))); 
  }

  @Override protected void update() { _updatePosition(); _updateRotation(); }
  @Override protected void paint(Clock clock) { _paintPosition(clock); _paintRotation(clock); }

}
