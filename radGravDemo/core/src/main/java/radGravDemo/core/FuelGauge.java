package radGravDemo.core;

import react.Slot;
import react.Value;

import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.scene.CanvasLayer;
import playn.core.Platform;
import playn.core.Canvas;
import playn.core.Texture;
import playn.core.Tile;
import playn.core.Font;
import playn.core.TextBlock;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.TextWrap;
 
/**
 * Fuel gauge class
 *
 * @author Scott Simmons
 *         2016.04.20
 * @version 1.0
 */
class FuelGauge extends GroupLayer {

  private Tile[] tiles;

  // Let us use reactive values to track of exhaust bubbles expended.  When either burner emits an exhuast bubble, 
  // the corresponding reactive Value gets decremented.  Below we will add a listeners that detect low/no fuel, etc. 
  // start with 1000 units (i.e., bubbles) of fuel in each burner
  final Value<String> burnFuel = Value.create(null);

  final Value<Integer> leftFuelLevel = Value.create(null);
  final Value<Integer> rightFuelLevel = Value.create(null);

  private int maxFuel;

  private Canvas textcanvas, textcanvas2, textcanvas3;
  private CanvasLayer fuelLayer, fuelLayer2, fuelLayer3;
  private ImageLayer leftGauge, rightGauge;

  public FuelGauge(Platform plat, GroupLayer groupLayer, float ulX, float ulY, final int maxFuel) {

    this.maxFuel = maxFuel;
    ulX *= RadGravDemo.scalePhysToRoot;
    ulY *= RadGravDemo.scalePhysToRoot;
    
    reFuel();

    int outerColor =  0xFF000000; // black
    int innerColor =  0xFFF8F8F8; // make this transparent
    int levelColor =  0xFF00c5cc; 
    int lowLevelColor =  0xFFb7f2ff; 

    float meterWidth = 60;
    float meterHeight = 10;

    final int num = 100; // number of increments; num = 20,for example, results in of 5% jumps

    if (num >= maxFuel) 
      throw new IllegalArgumentException("gradation of fuel level " + num + " is too large for maxFuel = " + maxFuel);
    if (maxFuel % num != 0) 
      throw new IllegalArgumentException("num " + num + " should divide maxFuel = " + maxFuel);

    tiles = new Tile[num];   

    Canvas c = plat.graphics().createCanvas(num * meterWidth, meterHeight);
    for (int i=0; i<num; i++) {
      c.setFillColor(outerColor).fillRect(i*meterWidth, 0, meterWidth, meterHeight);
      c.setFillColor(innerColor).fillRect(i*meterWidth+1, 1, meterWidth-2, meterHeight-2);
      if (i < .2* num && i % 4 == 0)
        c.setFillColor(lowLevelColor).fillRect(i*meterWidth+1, 1, i * (meterWidth-2)/num, meterHeight-2);
      else if (i < .2* num && i % 4 == 1)
        c.setFillColor(lowLevelColor).fillRect(i*meterWidth+1, 1, i * (meterWidth-2)/num, meterHeight-2);
      else
        c.setFillColor(levelColor).fillRect(i*meterWidth+1, 1, i * (meterWidth-2)/num, meterHeight-2);
    }
    Texture texture = c.toTexture(Texture.Config.UNMANAGED);

    // extract tiles
    for (int i=0; i<num; i++) {
      tiles[i] = texture.tile(i*meterWidth, 0, meterWidth, meterHeight);
    }

    onDisposed(texture.disposeSlot());

    // Add the text for the fuel gauge. This won't need to be updated.
    final StringBuilder msg = new StringBuilder();
    final StringBuilder msg2 = new StringBuilder();
    final StringBuilder msg3 = new StringBuilder();
    msg.append("Fuel");
    TextBlock block = new TextBlock(plat.graphics().layoutText(
          msg.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 12)),
          TextWrap.MANUAL));
    msg2.append("left");
    TextBlock block2 = new TextBlock(plat.graphics().layoutText(
          msg2.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 10)),
          TextWrap.MANUAL));
    msg3.append("right");
    TextBlock block3 = new TextBlock(plat.graphics().layoutText(
          msg3.toString(), 
          new TextFormat(new Font("Helvetica", Font.Style.BOLD, 10)),
          TextWrap.MANUAL));
    textcanvas = plat.graphics().createCanvas(block.bounds.width()+2, block.bounds.height());
    textcanvas2 = plat.graphics().createCanvas(block2.bounds.width()+2, block2.bounds.height()+2);
    textcanvas3 = plat.graphics().createCanvas(block3.bounds.width()+2, block3.bounds.height()+2);
    textcanvas.setFillColor(0xFF222222); textcanvas2.setFillColor(0xFF222222); textcanvas3.setFillColor(0xFF222222);
    block.fill(textcanvas, TextBlock.Align.LEFT, 2, 2);
    block2.fill(textcanvas2, TextBlock.Align.RIGHT, block2.bounds.width()-block2.textWidth(), 2);
    block3.fill(textcanvas3, TextBlock.Align.RIGHT, block3.bounds.width()-block3.textWidth(), 2);
    fuelLayer = new CanvasLayer(plat.graphics(), textcanvas);
    fuelLayer2 = new CanvasLayer(plat.graphics(), textcanvas2);
    fuelLayer3 = new CanvasLayer(plat.graphics(), textcanvas3);
    fuelLayer.setOrigin(CanvasLayer.Origin.CENTER);
    fuelLayer2.setOrigin(CanvasLayer.Origin.CENTER);
    fuelLayer3.setOrigin(CanvasLayer.Origin.CENTER);
    groupLayer.addCenterAt(fuelLayer, ulX+block.bounds.width()/2, ulY+block.bounds.height()/2);
    groupLayer.addCenterAt(fuelLayer2, ulX+5+block3.bounds.width()/2, ulY+20+block2.bounds.height()/2);
    groupLayer.addCenterAt(fuelLayer3, ulX+5+block3.bounds.width()/2, ulY+35+block3.bounds.height()/2);

    // draw the bar gauges
    final ImageLayer leftGauge = new ImageLayer(tiles[num-1]); 
    final ImageLayer rightGauge = new ImageLayer(tiles[num-1]); 
    leftGauge.setOrigin(ImageLayer.Origin.CENTER);
    rightGauge.setOrigin(ImageLayer.Origin.CENTER);
    groupLayer.addCenterAt(leftGauge, ulX+5+block3.bounds.width()/2+meterWidth+1, ulY+20+block2.bounds.height()/2-2);
    groupLayer.addCenterAt(rightGauge, ulX+5+block3.bounds.width()/2+meterWidth+1, ulY+35+block3.bounds.height()/2-2);

    // add a listener to the reactive Value burnFuel  
    burnFuel.connect(new Slot<String>() {
      @Override public void onEmit(String burner) {
        if (burner == "LEFT") leftFuelLevel.update(leftFuelLevel.get()-1);
        else if (burner == "RIGHT") rightFuelLevel.update(rightFuelLevel.get()-1);
        if (leftFuelLevel.get() == 0 && rightFuelLevel.get() == 0) RadGravDemo.gameOver.updateForce(true);
      }
    }); 

    // add listeners to the reactive values leftFuelLevel and rightFuelLevel
    leftFuelLevel.connect(new Slot<Integer> () {
      @Override public void onEmit(Integer level) {
        if ( level > 0 && (level % (maxFuel/num)) == 0)
          leftGauge.setSource(tiles[level/(maxFuel/num)-1]); 
      }
    });

    rightFuelLevel.connect(new Slot<Integer> () {
      @Override public void onEmit(Integer level) {
        if ( level > 0 && (level % (maxFuel/num)) == 0)
          rightGauge.setSource(tiles[level/(maxFuel/num)-1]); 
      }
    });
  }

  private void reFuel() {
    leftFuelLevel.updateForce(maxFuel);
    rightFuelLevel.updateForce(maxFuel);
  }

  @Override public void close () {
    fuelLayer.close(); fuelLayer2.close();
    textcanvas.close(); textcanvas2.close();
    //leftGauge.close(); rightGauge.close();
    super.close();
    tiles[0].texture().close();
  }
}
