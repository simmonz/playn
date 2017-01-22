package openglDemo.java;

import playn.java.LWJGLPlatform;

import openglDemo.core.OpenglDemo;

public class OpenglDemoJava {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new OpenglDemo(plat);
    plat.start();
  }
}
