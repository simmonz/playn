package radGravDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.WeldJointDef;
import org.jbox2d.dynamics.joints.WeldJoint;

import playn.core.Platform;
import playn.scene.GroupLayer;
import playn.core.Clock;
import playn.core.Keyboard;

import java.util.List;
import java.util.ArrayList;

/**
 * Ship class
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
class Ship extends RemovableEntity {

  // how the ship is made:  rects for fuselage and left/right burner.
  static final float burnerWidth = .1f, burnerHeight = 1.5f* burnerWidth, 
                     fuselageWidth = 2 * burnerWidth, fuselageHeight = 4* burnerWidth;
  final Fuselage fuselage;
  private final Burner leftBurner, rightBurner;

  private final FuelGauge fuelGauge;

  // for moving the ship
  private boolean thrustRight, thrustLeft; 
  private boolean thrust = false;

  // for the fuel (bubbles) for the ship
  private static BubbleTextures bubbleLayer;
  private static final List<Bubble> bubbleList = new ArrayList<>(); 

  // if an exhaust bubble wanders too far off the screen, it gets removed from the reactive list above, added
  // to this list, and then promptly removed.
  private final List<Bubble> bubbleRemoveList = new ArrayList<>(); 

  private World world;

  Ship(Platform plat, GroupLayer universeLayer, GroupLayer rootLayer, final World world, Vec2 position) {
    super();
    this.world = world;

    // create the ship
    fuselage = new Fuselage(plat, universeLayer, world, position, new Vec2(0, fuselageHeight/5));
    rightBurner = new Burner(plat, universeLayer, world, position, new Vec2(fuselageWidth*.9f , -fuselageHeight/6));
    leftBurner = new Burner(plat, universeLayer, world, position, new Vec2(-fuselageWidth*.9f, -fuselageHeight/6));
    fuselage.update();  // update here to be safe
    leftBurner.update();
    rightBurner.update();

    WeldJointDef jointDef = new WeldJointDef();
    //weld left burner to fuselage
    jointDef.initialize(leftBurner.body, fuselage.body, (leftBurner.body.getPosition().add(fuselage.body.getPosition()).mul(.5f)));
    jointDef.collideConnected = false;
    jointDef.frequencyHz = 1.8f;
    jointDef.dampingRatio = .7f;
    world.createJoint(jointDef);
    //weld right burner to fuselage
    jointDef.initialize(rightBurner.body, fuselage.body, (rightBurner.body.getPosition().add(fuselage.body.getPosition()).mul(.5f)));
    jointDef.collideConnected = false;
    jointDef.frequencyHz = 1.8f;
    jointDef.dampingRatio = .7f;
    world.createJoint(jointDef);

    // add a fuel gauge
    fuelGauge = new FuelGauge(plat, rootLayer, RadGravDemo.physWidth/20, RadGravDemo.physHeight/20, 200);

    // make some bubbles to use for the exhaust of the ship
    //float large = .18f * RadGravDemo.scalePhysToScene, medium = .15f * RadGravDemo.scalePhysToScene; 
    //float small = .12f * RadGravDemo.scalePhysToScene, tiny = .1f * RadGravDemo.scalePhysToScene; 
    float large = .09f * RadGravDemo.scalePhysToScene, medium = .075f * RadGravDemo.scalePhysToScene; 
    float small = .06f * RadGravDemo.scalePhysToScene, tiny = .05f * RadGravDemo.scalePhysToScene; 
    bubbleLayer = new BubbleTextures(plat, large, medium, small, tiny, false);
    universeLayer.add(bubbleLayer);

    // connect to the keyboard signals 
    plat.input().keyboardEvents.connect(new Keyboard.KeySlot() {
      @Override public void onEmit (Keyboard.KeyEvent event) {
        if (event.down && !RadGravDemo.startScreen.get()) {
          switch (event.key) {
            case UP: 
              thrustRight = true;
              thrustLeft = true;
              break;
            case RIGHT: 
              thrustRight = true;
              break;
            case LEFT: 
              thrustLeft = true;
              break;
            default: break;
          } 
        }
        else {
          switch (event.key) {
            case UP: 
              thrustRight = false;
              thrustLeft = false;
              break;
            case RIGHT:
              thrustRight = false;
              break;
            case LEFT:
              thrustLeft = false;
              break;
            default: break;
          }
        }
        if (thrustLeft && fuelGauge.leftFuelLevel.get() > 0) {
          bubbleList.add(new Bubble(bubbleLayer, world, "TINY", leftBurner.burnerPos(.5f),
              //new Vec2(-.01f*(float)Math.sin(leftBurner.body.getAngle()), .01f*(float)Math.cos(leftBurner.body.getAngle()))));
              new Vec2(-.001f*(float)Math.sin(leftBurner.body.getAngle()), .001f*(float)Math.cos(leftBurner.body.getAngle()))));
          fuelGauge.burnFuel.updateForce("LEFT");
        }
        if (thrustRight && fuelGauge.rightFuelLevel.get() > 0) {
          bubbleList.add(new Bubble(bubbleLayer, world, "TINY", rightBurner.burnerPos(.5f),
              //new Vec2(-.01f*(float)Math.sin(rightBurner.body.getAngle()), .01f*(float)Math.cos(rightBurner.body.getAngle()))));
              new Vec2(-.001f*(float)Math.sin(rightBurner.body.getAngle()), .001f*(float)Math.cos(rightBurner.body.getAngle()))));
          fuelGauge.burnFuel.updateForce("RIGHT");
        }
      }
    });
  }

  @Override protected void update() {
    fuselage.update();
    leftBurner.update();
    rightBurner.update();
    for (Bubble bubble : bubbleList) {
      bubble.update();
      if (bubble.isOffScreen()) { 
        bubbleRemoveList.add(bubble);
      }
    }
    for (Bubble bubble : bubbleRemoveList) {
      bubble.remove();
      bubbleList.remove(bubble); 
    }
    bubbleRemoveList.clear();
  }

  @Override protected void paint(Clock clock) {
    fuselage.paint(clock);
    leftBurner.paint(clock);
    rightBurner.paint(clock);
    for (Bubble bubble : bubbleList)
      bubble.paint(clock);
  }

  protected void remove() {
    for (Bubble bubble : bubbleList)
      bubble.remove();
    bubbleList.clear();
    fuelGauge.disposeAll();
    fuelGauge.close();
    bubbleLayer.disposeAll();
    bubbleLayer.close();
    fuselage.remove();
    leftBurner.remove(); 
    rightBurner.remove();
  }
}
