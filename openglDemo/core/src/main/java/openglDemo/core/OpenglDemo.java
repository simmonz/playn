package openglDemo.core;

import pythagoras.f.AffineTransform;

import playn.scene.*;
import playn.core.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import react.RFuture;
import react.UnitSlot;
import react.Slot;

/**
 * OpenGL hacking demo
 *
 * @author SSimmons
 *         2016.05.01
 * @version 1.0
 */
public class OpenglDemo extends SceneGame {

  final float width, height;

  public OpenglDemo (final Platform plat) {
    super(plat, 33); // update our "simulation" 33ms (30 times per second)

    width = plat.graphics().viewSize.width();
    height = plat.graphics().viewSize.height();

    final Image venus = plat.assets().getImage("images/venus.jpg");
    //final Image asteroid = plat.assets().getImage("images/asteroid.jpg");
    final Image earth = plat.assets().getImage("images/earth.jpg");
    
    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xfff8f8f8).fillRect(0, 0, width, height); 
      }
    });
    
    RFuture.collect(Arrays.asList(venus.state, earth.state)).onSuccess(new UnitSlot() {
      public void onEmit () { 
        addTriBatch(venus, 2, 150, width/2, height/2, .005f); 
        addTriBatch(earth, 1, 40, width/6, height/2, .01f);
      } 
    });
  }
    
  void addTriBatch(Image tile, int  depth, float size, float centerX, float centerY, final float dAngle) {
    final Texture tileTex = tile.createTexture(Texture.Config.DEFAULT.repeat(true,true));
    //final Texture tileTex = tile.createTexture(new Texture.Config(true,true,false,1,1,true));

    // get the data for an icosaSphere
    IcosaSphere icosaSphere = new IcosaSphere(depth, size, true); 
    
    final float verts[] = new float[icosaSphere.verts.size()*3]; 
    final int indices[] = new int[icosaSphere.indices.length]; 

    // notice rotation about x = y = z
    for (int i=0; i<icosaSphere.verts.size(); i++) {
      IcosaSphere.Vertex vert = icosaSphere.verts.get(i);
      verts[3*i] = vert.y;
      verts[3*i+1] = vert.z;
      verts[3*i+2] = vert.x;
    }

    for (int i=0; i<icosaSphere.indices.length; i++)
      indices[i] = icosaSphere.indices[i]; 

    final AffineTransform af = new AffineTransform().
      scale(plat.graphics().scale().factor, plat.graphics().scale().factor).
      translate(centerX, centerY);

    final RotatingBatch rotatingBatch = new RotatingBatch(plat.graphics().gl, 2);
    rotatingBatch.angle = 0;
    rotatingBatch.eyeX = width/2;
    rotatingBatch.eyeY = height/2;

    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        rotatingBatch.addTris(tileTex, Tint.NOOP_TINT, af,
          verts, 0, verts.length, tileTex.width(), tileTex.height(),
          indices, 0, indices.length, 0);
      }
    }.setBatch(rotatingBatch));

    update.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        rotatingBatch.angle += dAngle;
      }
    });
  }
}
