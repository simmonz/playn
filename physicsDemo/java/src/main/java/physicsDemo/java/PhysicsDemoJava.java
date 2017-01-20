package physicsDemo.java;

import playn.java.LWJGLPlatform;

import physicsDemo.core.PhysicsDemo;

public class PhysicsDemoJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    config.width = 800; 
    config.height = 600; 
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new PhysicsDemo(plat);
    plat.start();
  }
}
