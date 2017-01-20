package boidsDemo.java;

import playn.java.LWJGLPlatform;

import boidsDemo.core.BoidsDemo;

public class BoidsDemoJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    config.width = 1020;
    config.height = 650;
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new BoidsDemo(plat);
    plat.start();
  }
}
