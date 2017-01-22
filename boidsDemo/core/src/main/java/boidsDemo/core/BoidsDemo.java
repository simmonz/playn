package boidsDemo.core;

import react.Slot;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.random;
import java.lang.StringBuilder;

import playn.core.Clock;
import playn.core.Image;
import playn.core.Platform;
import playn.core.Pointer;
import playn.scene.ImageLayer;
import playn.scene.CanvasLayer;
import playn.scene.GroupLayer;
import playn.scene.SceneGame;
import playn.core.Font;
import playn.core.TextBlock;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.Canvas;

/**
 * Flappy Boids!
 *
 * @author Scott Simmons
 *         2016.02.08
 * @version 1.0
 */
public class BoidsDemo extends SceneGame {
/* 
*  Extending SceneGame allows us to build up the graphics in a game by adding
*  layers for all the elements of the game (background, sprites, scoreboards,
*  instructions, "game over" notifications, etc.)  The layers are organized in
*  a tree structure (see the "Scene graph" part of playn.io/docs/overview.html).
*/
  
  private static float width, height;

  /**
   * An animated pterodactyl
   */
  public class Ptero extends Boid {

    // This has to do with cycling through the images[].
    int imageCounter = (int) (random() * 8.9); 

    // These are used for interpolation since the paint signal
    // may 'click' a various different points between update clicks. 
    //float preVelX, preVelY;
    float preX, preY, preTheta;

    // The constructor.
    public Ptero(final GroupLayer boidsLayer, float x, float y) {
      super(x, y, width, height);
      numberOfPteros++;

      preX = getX(); 
      preY = getY();
      preTheta = getTheta();

      // Give the boid the image of an animated pterodactyl
      final ImageLayer layer = new ImageLayer(images[imageCounter]);
      layer.setScale(0.3f);
      layer.setOrigin(ImageLayer.Origin.CENTER);
      boidsLayer.addCenterAt(layer, x, y);

      // Define and connect a Slot that reacts to Clock events to an 'update' Signal that emits Clock events.
      //
      // Here 'update' is a field of SceneGame (SceneGame extends playn.core.Game). The field
      // 'update' is of type Signal<Clock>.  It's a signal that gets emitted every time the game
      // is updated.  Now -- still referring only to 'update' -- Signal<Clock>, hence 'update', is
      // a Signal that emits events of type Clock.  A Signal is a gadget that we can attach a Slot to.
      // The Slot then reacts when the Signal emits an event.   
      // That's what's happening below: we are connecting the Signal called 'update' (which emits 
      // events of type Clock) to a new Slot that reacts to those events.  The  new Slot reacts as 
      // defined by the code below.
      //
      // Note: the update() method of SceneGame (the discussion above is about the 'update' field) 
      // is what gets called every time the game simulation is updated.  By default, update() just
      // emits the Clock to the 'update' field.   
      // Note also: onEmit is a method of, of course, Slot.
      //
      // UPDATE: Changed this to interpolate position (and angle). So, now, keep track of previous
      // position and angle
      update.connect(new Slot<Clock>() {
        @Override public void onEmit (Clock clock) {
          if (clock.tick % 7 == 0) { 
            if (imageCounter == 9) 
              imageCounter = 0;
            layer.setSource(images[imageCounter++]);
          }
          //preVelX = getVelX();
          //preVelY = getVelY();
          preX = getX(); 
          preY = getY();
          preTheta = getTheta();
        }
      });

      // Define and connect a Slot that reacts to Clock events to a 'paint' Signal that emits Clock events.
      //
      // What's happening here is similar to above except that it's paint signal that we are
      // connecting to the new Slot.
      paint.connect(new Slot<Clock>() {
        @Override public void onEmit (Clock clock) {
          // Here, we interpolate from the last updated position to alpha of the way the
          // the next (dead-reckoned, based on velocity) position. We don't worry about
          // interpolating a boid's theta.
          //
          // UPDATE: Changed this to interpolate w/r to position, and now interpolate the angle.
          //layer.setTranslation(getX() + preVelX * clock.alpha, 
          //                     getY() + preVelY * clock.alpha);
          //layer.setRotation(getTheta());
          layer.setTranslation(getX() * clock.alpha + preX * (1f - clock.alpha), 
                               getY() * clock.alpha + preY * (1f - clock.alpha));
          layer.setRotation(getTheta() * clock.alpha + preTheta * (1f - clock.alpha));
        }
      });
    }
  } 

  // Cycling through the images makes instances of the below pterodactyl class 
  // appear to fly.
  // TODO:  we might need to put these all into a single texture and move through
  // it to create the animation.  Then, be sure to dispose() of things correctly, which
  // may help performance on phones. 
  // NOTE:  We should consider using TriplePlay's Animator for this.
  private final Image [] images = {
    plat.assets().getImage("images/ptero0.png"),
    plat.assets().getImage("images/ptero1.png"),
    plat.assets().getImage("images/ptero2.png"),
    plat.assets().getImage("images/ptero3.png"),
    plat.assets().getImage("images/ptero4.png"),
    plat.assets().getImage("images/ptero5.png"),
    plat.assets().getImage("images/ptero6.png"),
    plat.assets().getImage("images/ptero7.png"),
    plat.assets().getImage("images/ptero8.png")
  };

  static {
    // you can play with these constants:
    Ptero.separateRadiusSquared = 625.0f;
    Ptero.alignRadiusSquared = 2500.0f;
    Ptero.cohesionRadiusSquared = 500.0f;
    // and these:
    Ptero.separationScaleFactor = 1.5f;
    Ptero.alignmentScaleFactor = 1.0f;
    Ptero.cohesionScaleFactor = 1.0f;
    // and also these: 
    Ptero.maxSpeed = 3.0f;
    Ptero.maxForce = 0.04f;
  }

  /**
   * An instance of this class creates and updates a collection of pterodactyls.
   */
  public class Pteros extends Boids {

    public Pteros() {
      super(); 

      update.connect(new Slot<Clock>() {
        @Override public void onEmit (Clock clock) {
          flock();
        }
      });
    }
  }

  public final Pointer pointer;
  int numberOfPteros = 0;

  public BoidsDemo (final Platform plat) {
    super(plat, 33); // update our "simulation" 33ms (30 times per second)

    // combine mouse and touch into pointer events
    pointer = new Pointer(plat);

    // get the dimensions of the window screen.
    width = plat.graphics().viewSize.width();
    height = plat.graphics().viewSize.height();

    // create and add background ImageLayer
    Image bgImage = plat.assets().getImage("images/marscrater1.png");
    ImageLayer bgLayer = new ImageLayer(bgImage);
    bgLayer.setSize(plat.graphics().viewSize); // scale the background to fill the screen

    rootLayer.add(bgLayer);  // create the root node of the scene graph 

    // create a group layer to hold boids' ImageLayers
    final GroupLayer pterosLayer = new GroupLayer();
    rootLayer.add(pterosLayer);

    // create a collection (initially empty) of boids:
    final Pteros pteros = new Pteros();

    // add some pterodactyls, randomly:
    for (int i = 0; i < 40; i++)
      pteros.add(new Ptero(pterosLayer, (float)(width/3+random()*width/3), (float)(height/3+random()*height/3)));

    // Display the number of Pteros and other info in the corner of the screen:
    final StringBuilder msg = new StringBuilder();
    if (numberOfPteros < 100) msg.append(" ");
    msg.append(numberOfPteros+" pteros \n"+ Ptero.separationScaleFactor + " sep \n" 
                                        + Ptero.alignmentScaleFactor + " align \n" 
                                        + Ptero.cohesionScaleFactor + " coh " );
    TextBlock block = new TextBlock(plat.graphics().layoutText(
          msg.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 14)),
          TextWrap.MANUAL));
    final Canvas canvas = plat.graphics().createCanvas(block.bounds.width()+8, block.bounds.height()+4);
    canvas.setFillColor(0xFF000000);
    block.fill(canvas, TextBlock.Align.LEFT, 2, 2);
    final CanvasLayer numPterosLayer = new CanvasLayer(plat.graphics(), canvas);
    rootLayer.addFloorAt(numPterosLayer, 20, 20);

    // Define and connect a Slot that reacts to signals of type Pointer.Event to the Signal, called pointer.events,
    // that emits signals of type Pointer.Event.  
    pointer.events.connect(new Slot<Pointer.Event>() {
      @Override public void onEmit (Pointer.Event event) {
        if (event.kind.isStart) {
          if (numberOfPteros <= 1000)
            pteros.add(new Ptero(pterosLayer, event.x(), event.y()));

          // update the on screen counter
          msg.delete(0,msg.length());
          if (numberOfPteros < 100) msg.append(" ");
          msg.append(numberOfPteros+" pteros \n"+ Ptero.separationScaleFactor + " sep \n" 
                                                + Ptero.alignmentScaleFactor + " align \n" 
                                                + Ptero.cohesionScaleFactor + " coh " );
          numPterosLayer.begin();
          TextBlock block = new TextBlock(plat.graphics().layoutText(
                msg.toString(), 
                new TextFormat(new Font("Helvetica", Font.Style.BOLD, 14)),
                TextWrap.MANUAL));
          canvas.clear();
          canvas.setFillColor(0xFF000000);
          block.fill(canvas, TextBlock.Align.LEFT, 2, 2);
          numPterosLayer.end();
        }
      } 
    });
  }
}
