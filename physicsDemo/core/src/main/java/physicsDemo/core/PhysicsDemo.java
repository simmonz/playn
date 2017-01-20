package physicsDemo.core;

import react.Slot;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
//import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.dynamics.joints.DistanceJointDef;

import pythagoras.f.IDimension;

import playn.core.Image;
import playn.scene.Layer;
import playn.core.Platform;
import playn.core.Surface;
import playn.core.Tile;
import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.scene.SceneGame;
import playn.core.Clock;
import playn.scene.LayerUtil;
import playn.core.DebugDrawBox2D;

import java.util.List;
import java.util.ArrayList;

//import tripleplay.anim.Flipbook;
//import tripleplay.util.SimpleFrames;
//import tripleplay.anim.Animator;


/** 
 * JBox2D physics demo in PlayN.
 * <p>
 * As eplained in the manual, JBox2D physics works well if the object sizes 
 * are in the range 0.1 to 10 meters (JBox2D physics uses MKS units). In JBox2D,
 * one unit (i.e., 1.0f) is one meter.
 * <p>
 * Box2D doesn't draw graphics. It only does physics. We'll use PlayN to do the
 * graphics.
 * <p> 
 * Supposing that our PlayN window is 800x600, let's divide the both width and height 
 * by 10, obtaining 60x80, so that a circle of radius 10f on the screen corresponds 
 * to a physical disk of radius 1 meter, about the size of a table top in the 
 * cafeteria.
 * <p> 
 * Remembering that, as far as PlayN (or vitually any computer graphics system)
 * is concerned, the positive y-axis points downward, the toSceneCoords method  below
 * converts from physical JBox2D world coordinates to PlayN SceneGame coordinates.
 * <p>
 * When defining objects below, let's think in terms of physical coordinates and pass
 * into constructors positions, widths, heights, etc. expressed in terms of physical
 * coordinates.  Inside the classes defining the various objects, we will convert to
 * PlayN coordinates when defining and painted graphics.
 *
 * @author SSimmons
 *         2016-2-20
 */

public class PhysicsDemo extends SceneGame implements ContactListener {

  final World world;

  private Player player;

  private static float width, height; // of the PlayN window.
  static float physicsWidth, physicsHeight; // of the physical world.
  static float scale = 10f;  // factor to divide PlayN dimensions by to get physical dimensions. 
  static BubbleTextures bubbleTextures;

  // this list holds entities that move and therefore need their position (or whatever) updated
  // and that don't ever need to be removed from the game.
  private List<MovingEntity> nonRemovable = new ArrayList<>();

  // this list holds entities that might need to be removed (for example, if they go off the screen
  // to the left or right. 
  private List<RemovableEntity> removable = new ArrayList<>();

  // list holding the removable entities that should be removed at the end of each update.
  private List<RemovableEntity> toRemove = new ArrayList<>();

  // Set this to true to paint rudimentary graphics for your physical objects while, for example,
  // you are trying to get your PlayN graphics to work correctly.
  private boolean showDebugDrawGraphics = false;

  /** Convert from physical coordinates to game screen coordinates, */
  static Vec2 toSceneCoords (Vec2 physicsCoords) {
  // In the physical world the positive y-axis is upward.  In PlayN world is downward.  In
  // both worlds the positive x-axis points to the right.
    return new Vec2(scale * physicsCoords.x, height - scale*physicsCoords.y);
  }

  public PhysicsDemo (Platform plat) {
    super(plat, 25); // update every 25ms 

    final IDimension size = plat.graphics().viewSize;

    // PlayN window size
    width = size.width();
    height = size.height();

    // Physical world size
    physicsWidth = width/scale;   // divide by scale to get 
    physicsHeight = height/scale; // the physical dimensions

    // set PlayN background
    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFF87cefa).fillRect(0, 0, width, height);
      }
    });

    /** define physics world */
    // g = -9.8m/s^2 is the acceleration due to gravity on the earth's surface. 
    // Since the x-coordinate of the gravity vector is 0, gravity is acting
    // staight downward, as locally on the surface of the earth.
    Vec2  gravity = new Vec2(0f,-9.8f); 
    world = new World(gravity); 
    world.setContactListener(this);

    /** Define some (static) walls */
    // Create a GroupLayer for static objects and add it to the scene graph.
    GroupLayer staticLayer = new GroupLayer();
    rootLayer.add(staticLayer);

    // Define the ground, represented by a rectangular box at the bottom
    // of the screen, running the length the window, determined in the physical
    // world by giving the lower left and upper right vertices of the rectangle:  
    new StaticRectangle(staticLayer, world, new Vec2(0,0), new Vec2(physicsWidth, physicsHeight/15f), 0xFF8b4513); 

    // add a wall on the left side.
    new StaticRectangle(staticLayer, world, new Vec2(0f, physicsHeight/15f), 
                                                                      new Vec2(2f, physicsHeight/2f), 0xFF494A46); 
    // add a wall on the right side.
    new StaticRectangle(staticLayer, world, new Vec2(physicsWidth-2f, physicsHeight/15f), 
                                                             new Vec2(physicsWidth,physicsHeight/2f), 0xFF494A46); 
    /** Define some dynamic bubbles */
    // Create textures for efficiently displaying bubbles. The four floats correspond
    // to "LARGE", "MEDIUM", "SMALL", and "TINY" sized bubbles.
    // BubbleTextures extends GroupLayer so we can treat it like a GroupLayer (that, upon instantiation,
    // efficiently makes some textures for us).
    // Here 1f, for example, is a physics world dimension.
    float large = 1f * scale, medium = .75f * scale, small = .5f *scale, tiny = .25f *scale; 
    final BubbleTextures bubbleLayer = new BubbleTextures(plat, large, medium, small, tiny, false);
    rootLayer.add(bubbleLayer);
    for (int i = 0; i < 20; i++)
      removable.add(new Bubble(bubbleLayer, world, "LARGE", physicsWidth/4, 20f));

    /** Define some dynamic rectangles */
    // Creat a layer to hold the rectangles.
    final GroupLayer dynamicRects = new GroupLayer();
    rootLayer.add(dynamicRects);

    removable.add(new DynamicRectangle(plat, dynamicRects, world, physicsWidth/15, physicsHeight/15, 
                                                                       physicsWidth/3, physicsHeight));
    removable.add(new DynamicRectangle(plat, dynamicRects, world, physicsWidth/15, physicsHeight/20, 
                                                                       physicsWidth/4, physicsHeight));
    removable.add(new DynamicRectangle(plat, dynamicRects, world, physicsWidth/15, physicsHeight/20, 
                                                                       physicsWidth/2.5f, 3* physicsHeight/4));
    removable.add(new DynamicRectangle(plat, dynamicRects, world, 2*physicsWidth/15, physicsHeight/18, 
                                                                       3*physicsWidth/4, physicsHeight));
    removable.add(new DynamicRectangle(plat, dynamicRects, world, 1.5f*physicsWidth/15, physicsHeight/18, 
                                                                       physicsWidth/2, physicsHeight));
    removable.add(new DynamicRectangle(plat, dynamicRects, world, 2.5f*physicsWidth/15, physicsHeight/14, 
                                                                       3*physicsWidth/2, physicsHeight));

    /** Define some kinematic rectangles */
    final GroupLayer kinematicRects = new GroupLayer();
    rootLayer.add(kinematicRects);

    nonRemovable.add(new KinematicRectangle(plat, kinematicRects, world, physicsWidth/5, physicsWidth/100, 
                                                                           physicsWidth/3, physicsHeight/2));
    nonRemovable.add(new KinematicRectangle(plat, kinematicRects, world, physicsWidth/5, physicsWidth/100, 
                                                                           3*physicsWidth/4, 3*physicsHeight/4));

    /** Define a player */
    final GroupLayer playerLayer = new GroupLayer();
    rootLayer.add(playerLayer);
    player = new Player(plat, playerLayer, world, 10f, physicsWidth/2, physicsHeight/5);

    // connect the player's animation onPaint Slot to this SceneGame's paint Signal.
    paint.connect(player.anim.onPaint);
    
    // connect the code below to the update signal
    update.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        // the step delta is fixed so box2d isn't affected by framerate
        world.step(clock.dt/500f, 10, 10);
        // Update moving objects (doesn't update player)
        for (MovingEntity entity : nonRemovable) 
          entity.update();
        for (RemovableEntity entity : removable) {
          entity.update();
          if (entity.isOffScreen()) {
            toRemove.add(entity);
          }
        }
        if (toRemove.size() > 0) {
          for (RemovableEntity entity: toRemove) {
            world.destroyBody(entity.body);
            removable.remove(entity);
            entity.imageLayer.close();
          }
          toRemove.clear();
        }
        player.update();
      }
    });

    // Setup debugDraw so we can see shapes even before we attach PlayN ImageLayers to them.
    final DebugDrawBox2D debugDraw;  
    final ImageLayer debugLayer;

    debugDraw = new DebugDrawBox2D(plat, (int) width, (int) height);
    debugDraw.setFlipY(true);
    debugDraw.setStrokeAlpha(150);
    debugDraw.setFillAlpha(75);
    debugDraw.setStrokeWidth(2.0f);
    debugDraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit | DebugDraw.e_aabbBit | DebugDraw.e_centerOfMassBit);
    debugDraw.setCamera(0, height/scale, scale);
    rootLayer.add(debugLayer = new ImageLayer(debugDraw.canvas.image));
    //debugDraw.drawString(player.body.getPosition(), "Can jump here? "+player.numFeetContacts, new Color3f(200,9,9)); 
    world.setDebugDraw(debugDraw);


    paint.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        if (showDebugDrawGraphics) {
          debugDraw.canvas.clear();
          world.drawDebugData();
          debugLayer.tile().texture().update(debugDraw.canvas.image);
        }
        for (MovingEntity entity : nonRemovable) 
          entity.paint(clock);
        for (RemovableEntity entity : removable) {
          entity.paint(clock);
        }
        player.paint(clock);
      }
    });
  }
  
  // Box2d's begin contact
  @Override public void beginContact(Contact contact) {
    //contacts.push(contact);
    Fixture FixtureA = contact.getFixtureA();
    Fixture FixtureB = contact.getFixtureB();
    if (FixtureA.getUserData() != null && (int)FixtureA.getUserData()  == 3)
      player.numFeetContacts++;
    else if (FixtureB.getUserData() != null && (int)FixtureB.getUserData()  == 3)
      player.numFeetContacts++;
  }

  // Box2d's end contact
  @Override public void endContact(Contact contact) {
    Fixture FixtureA = contact.getFixtureA();
    Fixture FixtureB = contact.getFixtureB();
    if (FixtureA.getUserData() != null && (int)FixtureA.getUserData()  == 3)
      player.numFeetContacts--;
    else if (FixtureB.getUserData() != null && (int)FixtureB.getUserData()  == 3)
      player.numFeetContacts--;
  }

  // Box2d's pre solve
  @Override public void preSolve(Contact contact, Manifold oldManifold) {}

  // Box2d's post solve
  @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
}
