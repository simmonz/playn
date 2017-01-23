package vertletDemo.core;

import react.Slot;

import playn.core.Image;
import playn.scene.Layer;
import playn.core.Clock;
import playn.core.Surface;
import playn.core.Platform;
import playn.scene.ImageLayer;
import playn.scene.GroupLayer;
import playn.scene.SceneGame;
import playn.scene.Pointer;
import playn.scene.Mouse;
import playn.core.Keyboard;
import playn.core.Canvas;
import playn.core.Texture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tearable fabric simulation
 *
 * @author Scott Simmons
 *         2016.03.08
 * @version 1.0
 * 
 * Copyright 2016 Scott Simmons
 */
public class VertletDemo extends SceneGame {
  
  static float width, height;

  BubbleTextures bubbleTextures;

  //float meshWidth = 12; int numBubbles = 8;
  float meshWidth = 15; int numBubbles = 8;

  /** 
   * Make a line consisting of bubbles.
   *
   * This puts dots along a line determined by a Link. It puts nothing at the endpoint 
   * corresponding to pointMass B, and it puts a clickable layer (that is much bigger 
   * than a dot) at the endpoint corresponding to pointMass A. 
   */
  public class Line {

    ImageLayer[] bubbles;
    int i, num;
    final Fabric.Link link;

    public Line(GroupLayer groupLayer, final Fabric.Link link, int num) {
      bubbles = new ImageLayer[num];
      this.num = num;
      this.link = link;
      float stepX = (link.B.x-link.A.x)/num;
      float stepY = (link.B.y-link.A.y)/num;
      for (i=1 ; i<num; i++) {
        bubbles[i]  = new ImageLayer(BubbleTextures.bubbleTiles.get("SMALL"));
        bubbles[i].setOrigin(ImageLayer.Origin.CENTER);
        groupLayer.addCenterAt(bubbles[i], 0, 0);
        bubbles[i].setTranslation(link.A.x + i * stepX, link.B.x + i * stepY);
      }

      // add some transparent imageLayers for grabbing
      bubbles[0] = new ImageLayer();
      bubbles[0].setSize(2*meshWidth,2*meshWidth);
      
      /* 
       * Replace the two lines above with this if you want to see the clickable layers.
       *
      Canvas canvas = plat.graphics().createCanvas(meshWidth, meshWidth);
      canvas.setFillColor(0x10020000).fillRect(0,0,meshWidth,meshWidth);
      Texture texture = canvas.toTexture();
      bubbles[0] = new ImageLayer(texture);
      */

      bubbles[0].setOrigin(ImageLayer.Origin.CENTER);
      groupLayer.addCenterAt(bubbles[0], 0, 0);
      
      bubbles[0].setTranslation(link.A.x, link.A.y);
      bubbles[0].events().connect(new Pointer.Listener() {
        @Override public void onDrag (Pointer.Interaction iact) {

          link.A.x = iact.x();
          link.A.y = iact.y();
        }
      });
    }
      
    // paint a line of bubbles, properly interpolating
    void paint(Clock clock) {
      float Ax = link.A.lastX * clock.alpha + link.A.x * (1 - clock.alpha);
      float Ay = link.A.lastY * clock.alpha + link.A.y * (1 - clock.alpha);
      float Bx = link.B.lastX * clock.alpha + link.B.x * (1 - clock.alpha);
      float By = link.B.lastY * clock.alpha + link.B.y * (1 - clock.alpha);
      float stepX = (Bx-Ax)/num;
      float stepY = (By-Ay)/num;
      for (int i=0 ; i<num; i++) {
        bubbles[i].setTranslation(Ax + i * stepX, Ay + i * stepY);
      }
    }
  }

  public final Pointer pointer;

  public VertletDemo (Platform plat) {
    super(plat, 25); // update our "simulation" 33ms (30 times per second)

    width = plat.graphics().viewSize.width();
    height = plat.graphics().viewSize.height();
   
    pointer = new Pointer(plat, rootLayer, false);
    plat.input().mouseEvents.connect(new Mouse.Dispatcher(rootLayer, false));

    plat.input().keyboardEvents.connect(new Keyboard.KeySlot() {
      @Override public void onEmit (Keyboard.KeyEvent event) {
        if (event.down) {
          switch (event.key) {
            case G:
              if (Fabric.accY.get() > 0) 
                Fabric.accY.update(0f);
              else 
                Fabric.accY.update(.143f);
              break;
          }
        }
      }
    });

    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xfff8f8f8).fillRect(0, 0, width, height);  // set background
      }
    });

    final Fabric fabric = new Fabric(rootLayer, width/10, height/12, 9*width/10, height, meshWidth);

    BubbleTextures bubbleLayer = new BubbleTextures(plat, 2f, 1.5f, 1f, .6f, true);
    rootLayer.add(bubbleLayer);
    final Map<Fabric.Link, Line> linkToLines = new HashMap<Fabric.Link, Line>(); 

    // Make PlayN lines (out of bubbles) for all of the links.
    for (Fabric.Link link: Fabric.links) {
      Line line = new Line(rootLayer, link, numBubbles);
      linkToLines.put(link, line);
    }

    update.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        fabric.update();
      }
    });

    /*
    // drawing lines by overriding the paintimpl for Layer was too slow. 
    // 
    paint.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        rootLayer.add(new Layer() {
          protected void paintImpl(Surface surf) { 
          for (Fabric.Link link: Fabric.links)
            surf.setFillColor(0xff000000).drawLine(link.A.x,link.A.y,link.B.x,link.B.y,1);
          }
        });
      }
    });
    */
  
    paint.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        for (int k=0; k<Fabric.numLinks; k++)
          linkToLines.get(Fabric.links[k]).paint(clock);
      }
    });
  }
}
