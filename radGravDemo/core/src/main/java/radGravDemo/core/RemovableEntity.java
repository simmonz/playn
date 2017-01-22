package radGravDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.core.Platform;

/**
 * A class for moving entities that should be removed for generic reasons.
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
abstract class RemovableEntity extends MovingEntity {

  protected World world;

  // constructor for Ship and ViewPort
  RemovableEntity() {
    super();
  }

  // constructor for Bubbles
  RemovableEntity(World world, GroupLayer groupLayer, String size, Vec2 position, Vec2 impulse) {
    super(world, groupLayer, size, position, impulse);
    this.world = world;
  }

  // constructor for Planet
  RemovableEntity(Platform plat, GroupLayer groupLayer, World world, Vec2 position, float radius) {
    super(plat, groupLayer, world, position, radius);
    this.world = world;
  }

  // constructor for Fuselage and Burner
  RemovableEntity(Platform plat, GroupLayer groupLayer, World world, Vec2 position, Vec2 offset) {
    super(plat, groupLayer, world, position, offset);
    this.world = world;
  }

  // check if this entity is off the screen.
  protected boolean isOffScreen () {
    Vec2 pos = body.getPosition();
    return pos.x < -.25f*RadGravDemo.physWidth  || pos.x > 1.25f*RadGravDemo.physWidth ||
           pos.y < -.25f*RadGravDemo.physHeight  || pos.y > 1.25f*RadGravDemo.physHeight;
  }

  protected void remove() {
    world.destroyBody(this.body);
    this.imageLayer.close();
  }
}
