package radGravDemo.java;

import playn.java.LWJGLPlatform;

import radGravDemo.core.RadGravDemo;

public class RadGravDemoJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    config.width = 800;
    config.height = 700;
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new RadGravDemo(plat);
    plat.setTitle("Radial Gravity Demo");
    plat.start();
  }
}
