package life.core;

import react.Slot;

import playn.core.*;
import playn.scene.*;

import pythagoras.f.IDimension;

import java.util.Arrays;
import java.util.Random;

public class Life extends SceneGame {

  // size of each individual square of the grid
  private final int cellSize = 5;

  // number of cell across and down the grid
  private static int numRows;
  private static int numCols;

  private int counter = 0;

  // used for initialization
  private static Random rand = new Random();

  // Fix Java's modulo so that it a % b returns a number between 0 and b-1
  int modulo(int a, int b) {
    return (a % b + b) % b;
  } 

  private class Cells {

    // Each entry in the array grid represents a cell.
    private int [][] grid = new int[numRows][numCols];

    // Initialize the entries in array grid with 0 and 1, randomly chosen. 
    // An entry of 1 represents a living cell; 0 represents a dead cell.  
    private void initializeRandomly () {
      for (int i=0; i < numRows; i++)  
        for (int j=0; j < numCols; j++) 
          grid[i][j] = rand.nextInt(2);
    }

    // Return the number of living cells surrounding the cell at (i,j).
    private int countNeighbors (int i, int j) {
      int count = 0;
      for (int k = -1; k <= 1; k++) 
        for (int l = -1; l <= 1; l++) 
          count += grid[modulo(i+k,numRows)][modulo(j+l,numCols)];
      return count - grid[i][j];
    }

    // Update grid so that it contains a new generation of cells. 
    private void nextGeneration () {
      int [][] newGrid = new int[numRows][numCols]; 
      for (int i=0; i < numRows; i++) { 
        for (int j=0; j < numCols; j++) {
          int numNeighbors = countNeighbors(i,j);
          if ( grid[i][j] == 0 && numNeighbors == 3 ) 
            newGrid[i][j] = 1;
          else if ( grid[i][j] == 1 ) {
            if (numNeighbors < 2 || numNeighbors > 3)
              newGrid[i][j] = 0;
            else newGrid[i][j] = 1;
          }
        }
      }
      this.grid = newGrid;
    }
  }


  public Life (Platform plat) {
    super(plat, 30); // update our "simulation" 33ms (30 times per second)

    // find out how big the game view is
    final IDimension size = plat.graphics().viewSize;

    numRows = (int)size.width() / cellSize; 
    numCols = (int)size.height() / cellSize;

    final Cells cells = new Cells();

    //initialize the background to gray and draw some lines.
    rootLayer.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFF8F8F8).fillRect(0,0, size.width(), size.height());
        // horizontal lines
        for (int i = 0; i <= numRows; i++) 
           surf.setFillColor(0xFF050505).fillRect(i*cellSize, 0, 1, (int)size.height());
        // vertical lines
        for (int j = 0; j <= numCols; j++) 
          surf.setFillColor(0xFF050505).fillRect(0, j*cellSize, (int)size.width(), 1);
      }
    });

    final GroupLayer aGeneration = new GroupLayer();
    rootLayer.add(aGeneration);

    cells.initializeRandomly();

    update.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        cells.nextGeneration();
      }
    });

    paint.connect(new Slot<Clock>() {
      @Override public void onEmit (Clock clock) {
        // draw the next generation
        aGeneration.disposeAll();
        aGeneration.add(new Layer() {
          protected void paintImpl (Surface surf) {
            for (int i = 0; i < numRows; i++) 
              for (int j = 0; j < numCols; j++) 
                if (cells.grid[i][j] == 1) 
                  surf.setFillColor(0xFF080808).fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
          }
        });
      }
    });
  }
}
