package physicsDemo.core;

import org.jbox2d.dynamics.Body;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.core.Platform;
import playn.core.Clock;

/**
 * Moving entity class 
 *
 * @author Scott Simmons
 *         2016.03.01
 * @version 1.0
 */
abstract class MovingEntity {
/** 
 * Any moving entity has to be updated in some way:  either it's position or velocity or the way 
 * is image is constructed, or all of those.
 */ 

  ImageLayer imageLayer;
  Body body;

  private float prevX, prevY, prevAngle;  // for interpolating the difference between updating and painting.

  // constructor for player and rectangles
  MovingEntity(Platform plat, GroupLayer groupLayer, World world,  float width, float height, float posX, float posY) {
    if (this instanceof Player) {
      body = initPlayerPhysics(world, width, height, posX, posY);
      imageLayer = initPlayerImage(plat, groupLayer, height);
    } else {
      body = initRectanglePhysics(world, width, height, posX, posY);
      imageLayer = initRectangleImage(plat, groupLayer, width, height);
    }

    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    prevX = scenePosition.x;
    prevY = scenePosition.y;
    prevAngle = -body.getAngle();
  }

  // constructor for bubbles
  MovingEntity(World world, GroupLayer groupLayer, String size, float posX, float posY) {
    body = initBubblePhysics(world, size, posX, posY);
    imageLayer = initBubbleImage(groupLayer, size);
  }

  Body initRectanglePhysics(World world, float width, float height, float posX, float posY) {return body;}
  ImageLayer initRectangleImage(Platform plat, GroupLayer groupLayer, float width, float height) {return imageLayer;}

  Body initBubblePhysics(World world, String size, float posX, float posY) {return body;}
  ImageLayer initBubbleImage(GroupLayer groupLayer, String size) {return imageLayer;}

  Body initPlayerPhysics(World world, float width, float height, float posX, float posY) {return body;}
  ImageLayer initPlayerImage(Platform plat, GroupLayer groupLayer, float height) {return imageLayer;}

  void _update () {
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    prevX = scenePosition.x;
    prevY = scenePosition.y;
  };

  void _updateWithRotation () {
    _update();
    prevAngle = -body.getAngle();
  };

  void _paint(Clock clock) {
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x * clock.alpha + prevX * (1f - clock.alpha),
                              scenePosition.y * clock.alpha + prevY * (1f - clock.alpha));
  }

  void _paintWithRotation(Clock clock) {
    _paint(clock);
    imageLayer.setRotation(-body.getAngle() * clock.alpha + prevAngle * (1f - clock.alpha));
  }

  abstract void update();
  abstract void paint(Clock clock);
}
