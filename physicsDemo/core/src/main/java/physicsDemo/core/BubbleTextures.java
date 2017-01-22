package physicsDemo.core;

import playn.scene.GroupLayer;
import playn.core.Platform;
import playn.core.Canvas;
import playn.core.Texture;
import playn.core.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * BubbleTextures class 
 *
 * @author Scott Simmons
 *         2016.03.01
 * @version 1.0
 */
class BubbleTextures extends GroupLayer {

  static final Map<String, Float> sizes = new HashMap<String, Float>();
  static final Map<String, Tile> bubbleTiles = new HashMap<String, Tile>();

  BubbleTextures(Platform plat, float large, float medium, float small, float tiny) {
    this(plat, large, medium, small, tiny, true); 
  }

  BubbleTextures(Platform plat, float large, float medium, float small, float tiny, boolean solid) {

    sizes.put("LARGE", large);
    sizes.put("MEDIUM", medium);
    sizes.put("SMALL", small);
    sizes.put("TINY", tiny);

    float sw = 1;

    if (tiny < sw/2) 
      throw new IllegalArgumentException("tiniest bubble radius " + tiny + " is too small for strokewidth = " + sw);

    int outerColor =  0xFF000000;
    int innerColor =  0xFFffd700;
    if (solid) innerColor = outerColor;

    /** Make four bubbles of different sizes on the same Texture:*/
    // Create a canvas (a CPU bitmap) and draw four bubbles on it:
    Canvas canvas = plat.graphics().createCanvas(8*sizes.get("LARGE"),2*sizes.get("LARGE"));
    canvas.setFillColor(innerColor).fillCircle(sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("LARGE")).
      setStrokeColor(outerColor).setStrokeWidth(sw).strokeCircle(sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("LARGE")-sw/2);
    canvas.setFillColor(innerColor).fillCircle(3*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("MEDIUM")).
      setStrokeColor(outerColor).setStrokeWidth(sw).strokeCircle(3*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("MEDIUM")-sw/2);
    canvas.setFillColor(innerColor).fillCircle(5*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("SMALL")).
      setStrokeColor(outerColor).setStrokeWidth(sw).strokeCircle(5*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("SMALL")-sw/2);
    canvas.setFillColor(innerColor).fillCircle(7*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("TINY")).
      setStrokeColor(outerColor).setStrokeWidth(sw).strokeCircle(7*sizes.get("LARGE"), sizes.get("LARGE"), sizes.get("TINY")-sw/2);

    // Convert it to a texture (a GPU bitmap).  UNMANAGED results in the texture not being garbage 
    // collected if all of the bubbles happen to disappear.  We'll manually dispose it later. 
    Texture texture = canvas.toTexture(Texture.Config.UNMANAGED); // Note: toTexture disposes canvas
      
    // Extract the bubbles as tiles:
    bubbleTiles.put("LARGE", texture.tile(0, 0, 2*sizes.get("LARGE"), 2*sizes.get("LARGE")));
    bubbleTiles.put("MEDIUM", texture.tile(3*sizes.get("LARGE")-sizes.get("MEDIUM"), 
        sizes.get("LARGE")-sizes.get("MEDIUM"), 2*sizes.get("MEDIUM"), 2*sizes.get("MEDIUM")));
    bubbleTiles.put("SMALL", texture.tile(5*sizes.get("LARGE")-sizes.get("SMALL"), 
        sizes.get("LARGE")-sizes.get("SMALL"), 2*sizes.get("SMALL"), 2*sizes.get("SMALL")));
    bubbleTiles.put("TINY", texture.tile(7*sizes.get("LARGE")-sizes.get("TINY"), 
        sizes.get("LARGE")-sizes.get("TINY"), 2*sizes.get("TINY"), 2*sizes.get("TINY")));

    // Dispose the texture when this layer is disposed:
    // Here disposeSlot() returns a Slot that dispose of texture when triggered; i.e., when this texture is disposed. 
    onDisposed(texture.disposeSlot());
  }

  @Override public void close () {
    super.close();
    bubbleTiles.get("LARGE").texture().close(); // We only have to close one since all tiles reference the
  }                                             // the same texture.
} 
