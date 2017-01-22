package radGravDemo.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.collision.shapes.CircleShape;

import playn.core.Surface;
import playn.core.Platform;
import playn.core.Canvas;
import playn.core.Texture;
import playn.core.Clock;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;

/**
 * Another planet class
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
public class Planet2 extends RemovableEntity {

  public Planet2 (Platform plat, GroupLayer groupLayer, World world, Vec2 position, float radius) {
    super(plat, groupLayer, world, position, radius);
  }

  protected ImageLayer initPlanetImage(Platform plat, GroupLayer groupLayer, Vec2 position, float radius) { 
    // initialize an imageLayer
    float sceneRadius = RadGravDemo.scalePhysToScene * radius;
    Canvas canvas = plat.graphics().createCanvas(2*sceneRadius, 2*sceneRadius);
    //canvas.setFillColor(0xFF000000).fillCircle(sceneRadius, sceneRadius, sceneRadius);
    int n = 3*360;
    int m = 128;

    Vec2 [] radialVecs = new Vec2 [n];
    int [] colors = new int [m]; 

    // generates m equally spaced greys from black to white
    for (int k=0; k<m; k++) {
      colors[k] = 0xFF000000 + k * (256*256+256+1)*256/m;
      //System.out.println("color"+k+" "+Integer.toHexString(colors[k]));
    }

    for (int k=0; k<m; k++) { 
      float [] perlin1D = new float [n]; 
      for (int i=0;i<n;i++) { 
        //perlin1D[i] = .9f*(1-k/(float)m)+(float)perlinNoise.noise((double)i/(7f-.015*k),2.3+.05*k,3.4)/5;
       // perlin1D[i] = .9f*(1-k/(float)m)+(float)perlinNoise.noise((double)i/(30f-.07*k),2.3+.025*k,1.6)/5;
        perlin1D[i] = .9f*(1-k/(float)m)+(float)ImprovedNoise.noise((double)i/30,2.3+.025*k,3.9/(30f-.07*k))/(8+.25f*k);
      }
      //canvas.setStrokeColor(colors[k/4]).setStrokeWidth((.5f-.003f*k)*RadGravDemo.maxZoom);
      canvas.setStrokeColor(colors[k/4]);
      radialVecs[0] = (new Vec2(sceneRadius,sceneRadius)).add(new Vec2(sceneRadius*perlin1D[0], 0));
      double dTheta = 2f*Math.PI/n;
      for (int i=0; i<n-1; i++) { 
        radialVecs[i+1] = (new Vec2(sceneRadius,sceneRadius))
          .add(new Vec2(sceneRadius*perlin1D[i+1]*(float)Math.cos((i+1)*dTheta), 
                        sceneRadius*perlin1D[i+1]*(float)Math.sin((i+1)*dTheta)));
       // canvas.drawLine(radialVecs[i].x, radialVecs[i].y, radialVecs[i+1].x, radialVecs[i+1].y); 
        canvas.fillRect(radialVecs[i].x, radialVecs[i].y,(1f-.003f*k)*RadGravDemo.maxZoom,(1f-.003f*k)*RadGravDemo.maxZoom ); 
      }
   //   canvas.drawLine(radialVecs[n-1].x, radialVecs[n-1].y, radialVecs[0].x, radialVecs[0].y); 
    }

    Texture texture = canvas.toTexture();
    ImageLayer imageLayer = new ImageLayer(texture);
    imageLayer.setOrigin(ImageLayer.Origin.CENTER); 
    groupLayer.addCenterAt(imageLayer, 0f, 0f);

    // connect the graphics to the 
    Vec2 scenePosition = RadGravDemo.toSceneCoords(body.getPosition());
    imageLayer.setTranslation(scenePosition.x, scenePosition.y);

    update();

    return imageLayer;
  }
    
  protected Body initPlanetPhysics(World world, Vec2 position, float radius) {
    // initialize physics
    BodyDef bodyDef = new BodyDef();    
    bodyDef.type = BodyType.DYNAMIC; 
    bodyDef.position.set(position); 
    Body body = world.createBody(bodyDef);

    CircleShape disk = new CircleShape();
    disk.setRadius(.88f*radius);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = disk;
    fixtureDef.density = 8f;
    fixtureDef.friction = .2f;
    fixtureDef.restitution = .1f;
    body.createFixture(fixtureDef);
    body.applyAngularImpulse(-67.5f);

    return body;
  }

  protected void reset(Vec2 position) {
    body.setTransform(position,0);
    body.applyAngularImpulse(-67.5f);
  }

  protected void update() { _updatePosition(); _updateRotation(); }
  protected void paint(Clock clock) { _paintPosition(clock); _paintRotation(clock); }
}

