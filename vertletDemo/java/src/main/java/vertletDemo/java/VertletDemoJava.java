package vertletDemo.java;

import playn.java.LWJGLPlatform;

import vertletDemo.core.VertletDemo;

public class VertletDemoJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    config.width = 800;
    config.height = 650;
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new VertletDemo(plat);
    plat.setTitle("Vertlet Demo");
    plat.start();
  }
}
