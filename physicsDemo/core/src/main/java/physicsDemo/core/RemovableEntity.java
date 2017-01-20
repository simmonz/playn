package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import playn.scene.GroupLayer;
import playn.core.Platform;

/** 
 * A class for moving entities that should be removed for generic reasons.
 */ 
abstract class RemovableEntity extends MovingEntity {

  // constructor for player and rectangles
  RemovableEntity(Platform plat, GroupLayer groupLayer, World world,  float width, float height, float posX, float posY) {
    super(plat, groupLayer, world, width, height, posX, posY);
  }

  // constructor for bubbles
  RemovableEntity(World world, GroupLayer groupLayer, String size, float posX, float posY) {
    super(world, groupLayer, size, posX, posY);
  }

  // check if this entity is off the screen; that is, too far left or right.
  boolean isOffScreen () {
    Vec2 pos = body.getPosition();
    //return pos.x < 0 || pos.x > PhysicsDemo.physicsWidth || pos.y < PhysicsDemo.physicsHeight/10;
    return pos.x < -0.1*PhysicsDemo.physicsWidth || pos.x > 1.1*PhysicsDemo.physicsWidth ;
  }
}
