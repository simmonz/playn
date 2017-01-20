package physicsDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.collision.shapes.PolygonShape;
import java.lang.Math;

import playn.scene.Layer;
import playn.scene.ImageLayer;
import playn.core.Image;
import playn.core.Tile;
import playn.core.Platform;
import playn.scene.GroupLayer;
import playn.core.Keyboard;
import playn.core.Clock;

import tripleplay.anim.Flipbook;
import tripleplay.util.SimpleFrames;
import tripleplay.anim.Animator;
import tripleplay.anim.Animation.Flip;


class Player extends RemovableEntity {

  Animator anim; 
  private float scale;

  private static final float HOR_VELOCITY = 8f; 
  private static final float VERT_VELOCITY = 10f; 

  int numFeetContacts; // the number of entities this player is standing on (can't jump unless > 0)
  private int jumpTimeout;

  private float xMultiplier = 1, desiredVx, desiredVy;

  Player (Platform plat, GroupLayer groupLayer, World world, float height, float posX, float posY) {
    super(plat, groupLayer, world, 331*height/360, height, posX, posY); // Note: the width is specific to running.png 

    plat.input().keyboardEvents.connect(new Keyboard.KeySlot() {
      @Override public void onEmit (Keyboard.KeyEvent event) {
        if (event.down) {
          switch(event.key) {
            case LEFT: 
              xMultiplier = -1; 
              if (numFeetContacts > 0) {
                desiredVx = - HOR_VELOCITY;  
              }
              break;
            case RIGHT: 
              xMultiplier = 1; 
              if (numFeetContacts > 0) {
                desiredVx = HOR_VELOCITY;  
              }
              break;
            case UP: 
              if (numFeetContacts < 1) break; 
              if (jumpTimeout > 0) break;
              desiredVy = VERT_VELOCITY; jumpTimeout = 15; break;
            case DOWN:
              body.applyLinearImpulse(new Vec2(100000000000f,0f), body.getPosition());//can get you unstuck
              break;
            default: break;
          }
        }
        else {
          switch(event.key) {
            case LEFT: if (numFeetContacts > 0) desiredVx = 0; break;
            case RIGHT: if (numFeetContacts > 0) desiredVx = 0; break;
            case UP: desiredVy =  0; break;
            case DOWN:
              body.applyLinearImpulse(new Vec2(-100000000000f,0f), body.getPosition());//can get you unstuck
              break;
            default: break;
          }
        }
        body.applyLinearImpulse(new Vec2(body.getMass() * (desiredVx - body.getLinearVelocity().x), 
                                         body.getMass() * desiredVy), body.getPosition());
        desiredVy = 0;
      }
    });
  }

  @Override Body initPlayerPhysics(World world, float width, float height, float posX, float posY) {

    // Create a JBox2D body definition for this rectangle.
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position.set(posX, posY);

    // Define a JBox2D shape of the rectangle.
    PolygonShape rect = new PolygonShape();   
    // setAsBox takes half-width and half-height as arguments
    rect.setAsBox(width/5f, height/3.4f); // account for whitespace around the character's image

    // Define a JBox2D fixture for this rectangle. 
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = rect;
    fixtureDef.density = 1f;
    fixtureDef.friction = 0.3f;
    fixtureDef.restitution = 0.1f;

    // Create the Body and the Fixture for the rectangle.
    body = world.createBody(bodyDef);
    body.createFixture(fixtureDef);

    // Put a fixture for the feet.
    rect.setAsBox(width/12f, height/10f, new Vec2(0, - height/4f ), 0);
    FixtureDef feetFixtureDef = new FixtureDef();
    feetFixtureDef.shape = rect;
    feetFixtureDef.density = 1;
    feetFixtureDef.isSensor = true;
    Fixture feetFixture = body.createFixture(feetFixtureDef);
    feetFixture.setUserData(3);

    numFeetContacts = 0;
    jumpTimeout = 0;

    //body.applyLinearImpulse(new Vec2(1000000000f,1000000000f), body.getPosition());

    return body;
  }

  private Flipbook running, standing;

  @Override ImageLayer initPlayerImage(Platform plat, GroupLayer groupLayer, float height) {

    imageLayer = new ImageLayer();
    imageLayer.setOrigin(ImageLayer.Origin.CENTER);
    Vec2 scenePosition = PhysicsDemo.toSceneCoords(body.getPosition());
    groupLayer.addCenterAt(imageLayer, 0f, 0f);
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    Image sheet = plat.assets().getImage("images/running.png");
    SimpleFrames runningFrames = new SimpleFrames(sheet, 331, 360, 12); 
    SimpleFrames standingFrames = new SimpleFrames(sheet, 331, 360, 1); 
    this.scale = height*PhysicsDemo.scale/360;
    imageLayer.setVisible(false).setScale(this.scale); 

    anim = new Animator();
    anim.setVisible(imageLayer,true);
    running = new Flipbook(runningFrames, 80);
    standing = new Flipbook(standingFrames, 80);
    anim.repeat(imageLayer).flipbook(imageLayer, standing);

    return imageLayer;
  }

  @Override void update () {
    //keeps the player upright by gradually bringing it's angle back to zero.  We want there to be a slight rotation
    //so the player doesn't get frozen on the edge of a block.
    body.setTransform(body.getPosition(), -body.getAngle() + Math.min( .1f, Math.max( -.1f, body.getAngle())));
    _updateWithRotation();

  } 

  @Override void paint(Clock clock) {
    _paintWithRotation(clock);

    imageLayer.setScaleX(xMultiplier * this.scale);
    imageLayer.setTx(imageLayer.tx() + xMultiplier*12f); //the 12f fixes noncentered pngs
    imageLayer.setRotation((xMultiplier < 0) ? body.getAngle(): -body.getAngle());

    // animation stuff
    if (Math.abs(body.getLinearVelocity().x) > .05 && numFeetContacts > 0) { // if running and feet touching something
      anim.add(new Flip(imageLayer, running));
      anim.addBarrier();
    }
    else if (Math.abs(body.getLinearVelocity().x) > .05) { 
      anim.clear();
    }
    else {
      anim.clear();
      anim.add(new Flip(imageLayer, standing));
    }

    // used to prevent jumping in the air
    if (jumpTimeout > 0) jumpTimeout--;
  }
}
