package radGravDemo.core;

import org.jbox2d.dynamics.Body;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.core.Platform;
import playn.core.Clock;

/** 
 * Any moving entity has to be updated in some way:  either it's position * or velocity or the 
 * way is image is constructed, or all of those. 
 */ 
abstract class MovingEntity {

  protected ImageLayer imageLayer;
  protected Body body;

  // for interpolating the time difference between updating and painting
  private float prevX, prevY, prevAngle;  

  // constructor for ship and viewport
  MovingEntity() {}

  // constructor for fuselage and burners
  MovingEntity(Platform plat, GroupLayer groupLayer, World world, Vec2 position, Vec2 offset) {
    if (this instanceof Fuselage) { 
      body = initFuselagePhysics(world, position, offset);
      imageLayer = initFuselageImage(plat, groupLayer);
    }
    else {
      body = initBurnerPhysics(world, position, offset);
      imageLayer = initBurnerImage(plat, groupLayer);
    }
  }

  // constructor for planets 
  MovingEntity(Platform plat, GroupLayer groupLayer, World world, Vec2 position, float radius) {
    body = initPlanetPhysics(world, position, radius);
    imageLayer = initPlanetImage(plat, groupLayer, position, radius);
  }

  // constructor for bubbles
  MovingEntity(World world, GroupLayer groupLayer, String size, Vec2 position, Vec2 impulse) {
    body = initBubblePhysics(world, size, position, impulse);
    imageLayer = initBubbleImage(groupLayer, size);
  }

  protected Body initBubblePhysics(World world, String size, Vec2 position, Vec2 impulse) {return body;}
  protected ImageLayer initBubbleImage(GroupLayer groupLayer, String size) {return imageLayer;}

  protected Body initPlanetPhysics(World world, Vec2 position, float radius) {return body;}
  protected ImageLayer initPlanetImage(Platform plat, GroupLayer groupLayer, Vec2 position, float radius) 
            {return imageLayer;}

  protected Body initFuselagePhysics(World world, Vec2 position, Vec2 offset) {return body;}
  protected ImageLayer initFuselageImage(Platform plat, GroupLayer groupLayer) { return imageLayer; }

  protected Body initBurnerPhysics(World world, Vec2 position, Vec2 offset) {return body;}
  protected ImageLayer initBurnerImage(Platform plat, GroupLayer groupLayer) { return imageLayer; }

  protected void _updatePosition () {
    Vec2 scenePosition = RadGravDemo.toSceneCoords(body.getPosition());
    prevX = scenePosition.x;
    prevY = scenePosition.y;
  };

  protected void _updateRotation() {
    prevAngle = -body.getAngle();
  }

  protected abstract void update();
 
  protected void _paintPosition(Clock clock) {
    Vec2 scenePosition = RadGravDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x * clock.alpha + prevX * (1f - clock.alpha),
                              scenePosition.y * clock.alpha + prevY * (1f - clock.alpha));
  }

  protected void _paintRotation(Clock clock) {
    imageLayer.setRotation(-body.getAngle() * clock.alpha + prevAngle * (1f - clock.alpha));
  }

  protected abstract void paint(Clock clock);

}
