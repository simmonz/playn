package life.java;

import playn.java.LWJGLPlatform;

import life.core.Life;

public class LifeJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    
    // simmons added
    config.width = 800;
    config.height = 600;

    LWJGLPlatform plat = new LWJGLPlatform(config);
    new Life(plat);
    plat.start();
  }
}
