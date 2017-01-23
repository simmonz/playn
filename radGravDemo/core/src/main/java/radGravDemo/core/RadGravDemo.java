package radGravDemo.core;

import react.Slot;
import react.Value;

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
import playn.core.Keyboard;
import playn.core.Font;
import playn.core.TextBlock;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.Canvas;
import playn.scene.CanvasLayer;

import java.util.List;
import java.util.ArrayList;

/**
 * Radial gravity demo
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 *
 * Copyright 2016 Scott Simmons
 */
public class RadGravDemo extends SceneGame {

  // physWidth and physHeight are the dimensions of the physical universe.
  // width and height are already determined. Just set physWidth, then physHeight will
  // be calculated so that circles, etc., in physical coords get mapped to circles in playN coords.
  // (I.e., such that physHeight/physWidth = height/width.)
  static float physWidth = 40, physHeight, width, height;

  // Jbox2D physics world
  final World world;

  // IMPORTANT: So that we can keep the integrity of the graphics when we zoom in, all obects are
  // created on a scale maxZoom times width x height.  The dimensions of this very large layer
  // are referred to below as 'scene' dimensions.  (But to remember to think in physics world scale.)  

  // the maximum that objects will be magnified without losing resolution.
  static float maxZoom = 5f;

  // current zoom factor 
  static float zoom = 5f;

  // multiply by this to convert from physical dimensions to (large) scene dimensions
  static float scalePhysToScene; 

  // multiply by this to convert from physical dimensions to playn dimensions
  static float scalePhysToRoot; 

  // scales and accounts for the fact that Jbox2D has the y-axis pointing up instead of down.
  final static Vec2 toSceneCoords (Vec2 physicsCoords) {
    return new Vec2(scalePhysToScene * physicsCoords.x, maxZoom*height - scalePhysToScene * physicsCoords.y);
  }

  final static Vec2 toRootCoords (Vec2 physicsCoords) {
    return new Vec2(scalePhysToRoot * physicsCoords.x, height - scalePhysToRoot * physicsCoords.y);
  }

  // This list holds the ship and planets, etc.
  final List<RemovableEntity> universeObjects = new ArrayList<>(); 

  // gaveOver and startScreen are reactive values
  static final Value<Boolean> gameOver = Value.create(true);
  static final Value<Boolean> startScreen = Value.create(true);

  // The universe is bigger than the screen.
  // Control the position and zoom-level of the view window reactively.
  //protected Value<Vec2> viewPosition = new Value.create(null);
  //protected Value<Vec2> zoom = new Value.create(null);

  private float prevX, prevY;

  public RadGravDemo (final Platform plat) {
    super(plat, 33); // update our "simulation" 33ms (30 times per second)

    final IDimension screenSize = plat.graphics().viewSize;
    width = screenSize.width();
    height = screenSize.height();
    scalePhysToRoot = width/physWidth;
    physHeight = height/scalePhysToRoot;
    scalePhysToScene = maxZoom*width/physWidth;

    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFF8F8F8).fillRect(0, 0, width, height);
      }
    });

    Vec2 gravity = new Vec2(0f, 0f); // no JBox2D gravity
    world = new World(gravity);

    final GroupLayer universeLayer = new GroupLayer(maxZoom * width, maxZoom * height); 
    
    // add a planet
    //universeObjects.add(new Planet2(plat, universeLayer, world, new Vec2(physWidth/5, physHeight/6), 5f));
    universeObjects.add(new Planet(plat, universeLayer, world, new Vec2(physWidth/5, physHeight/6), 5f));

    // add a ship
    //universeObjects.add(new Ship(plat, universeLayer, world, new Vec2(physWidth/5, physHeight/4)));

    final Ship ship = new Ship(plat, universeLayer, rootLayer, world, new Vec2(physWidth/5, physHeight/6+5f));
    universeObjects.add(ship);

    universeLayer.setScale(zoom/maxZoom);

//    universeLayer.setOrigin(Layer.Origin.CENTER);
    Vec2 scenePosition = toSceneCoords(ship.fuselage.body.getPosition());
    universeLayer.setOrigin(scenePosition.x, scenePosition.y);
    rootLayer.addAt(universeLayer,width/2,height/2);

//    rootLayer.add(universeLayer);

    //universeObjects.add(new ViewPort(universeLayer, ship.fuselage.body));

    // make a game begin layer with some text in it. 
    StringBuilder beginMsg = new StringBuilder();
    beginMsg.append("Use LEFT, RIGHT,\nor UP to burn.\n\n Hit b to begin."); 
    TextBlock beginBlock = new TextBlock(plat.graphics().layoutText(
          beginMsg.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 30)),
          TextWrap.MANUAL));
    final Canvas beginCanvas = plat.graphics().createCanvas(beginBlock.bounds.width()+2, beginBlock.bounds.height());
    beginCanvas.setFillColor(0xFF3e606f);
    beginBlock.fill(beginCanvas, TextBlock.Align.CENTER, 2, 2);
    final CanvasLayer beginLayer = new CanvasLayer(plat.graphics(), beginCanvas);

    // define and connect a listener to the reactive value startScreen
    startScreen.connect(new Slot<Boolean> () {
      @Override public void onEmit(Boolean starting) {  
        if (starting) {
          Vec2 point = toRootCoords(new Vec2(physWidth/2, 5*physHeight/8));
          rootLayer.addCenterAt(beginLayer, point.x, point.y);
        }
        else {
          rootLayer.remove(beginLayer);
        }
      }
    });

    startScreen.updateForce(true);

    plat.input().keyboardEvents.connect(new Keyboard.KeySlot() {
      @Override public void onEmit (Keyboard.KeyEvent event) {
        if (event.down && gameOver.get()) {
          switch (event.key) {
            case R: 
              gameOver.update(false);
              break;
            case B: 
              startScreen.update(false);
              break;
            case F: 
              if (zoom >= 1) zoom -= .05;
              break;
            case D: 
              if (zoom <= 5) zoom += .05;
              break;
            default: break;
          } 
        }
      }
    });

    // make a game over layer with some text in it. 
    StringBuilder msg = new StringBuilder();
    msg.append("Fuel is necessary for\nspace travel\nbut you are out of it!\n\nYou may want to\nadmit failure!\nPress r to restart."); 

    TextBlock block = new TextBlock(plat.graphics().layoutText(
          msg.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 30)),
          TextWrap.MANUAL));
    final Canvas textcanvas = plat.graphics().createCanvas(block.bounds.width()+2, block.bounds.height());
    textcanvas.setFillColor(0xFF3e606f);
    block.fill(textcanvas, TextBlock.Align.CENTER, 2, 2);
    final CanvasLayer gameOverLayer = new CanvasLayer(plat.graphics(), textcanvas);

    // create a listener for the gameOver signal
    gameOver.connect(new Slot<Boolean> () {
      @Override public void onEmit(Boolean over) {  
        if (over) {
          Vec2 point = toRootCoords(new Vec2(physWidth/2, 2*physHeight/3));
          rootLayer.addCenterAt(gameOverLayer, point.x, point.y);
        }
        else {
          rootLayer.remove(gameOverLayer);
          for (RemovableEntity entity : universeObjects) 
            entity.remove();
          universeObjects.clear();
          universeLayer.disposeAll();
          // add a planet
          universeObjects.add(new Planet(plat, universeLayer, world, new Vec2(physWidth/5, physHeight/6), 4f));
          // add a ship
          Ship ship = new Ship(plat, universeLayer, rootLayer, world, new Vec2(physWidth/5, physHeight/4));
          universeObjects.add(ship);
        }
      }
    });

    // set DebugDraw
    /*
    final DebugDrawBox2D debugDraw;  
    final ImageLayer debugLayer;

    debugDraw = new DebugDrawBox2D(plat, (int) screenSize.width(), (int) screenSize.height());
    debugDraw.setFlipY(true);
    debugDraw.setStrokeAlpha(150);
    debugDraw.setFillAlpha(75);
    debugDraw.setStrokeWidth(2.0f);
    debugDraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit | DebugDraw.e_aabbBit | DebugDraw.e_centerOfMassBit);
    debugDraw.setCamera(0, physHeight, scalePhysToScene);
    rootLayer.add(debugLayer = new ImageLayer(debugDraw.canvas.image));
    world.setDebugDraw(debugDraw);
    */

    update.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        world.step(clock.dt/500f, 10,10);
        for (MovingEntity entity : universeObjects) 
          entity.update();

        universeLayer.setScale(zoom/maxZoom);
        Vec2 scenePosition = toSceneCoords(ship.fuselage.body.getPosition());
        prevX = scenePosition.x;
        prevY = scenePosition.y;
      }
    });

    paint.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {

        //debugDraw.canvas.clear();
        //world.drawDebugData();
        //debugLayer.tile().texture().update(debugDraw.canvas.image);
        
        for (MovingEntity entity : universeObjects) 
          entity.paint(clock);

        Vec2 scenePosition = toSceneCoords(ship.fuselage.body.getPosition());
        universeLayer.setOrigin(scenePosition.x * clock.alpha + prevX * (1f - clock.alpha),
                                scenePosition.y * clock.alpha + prevY * (1f - clock.alpha));
      }
    });
  }
}
