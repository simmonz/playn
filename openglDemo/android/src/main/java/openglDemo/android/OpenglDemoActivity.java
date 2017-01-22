package openglDemo.android;

import playn.android.GameActivity;

import openglDemo.core.OpenglDemo;

public class OpenglDemoActivity extends GameActivity {

  @Override public void main () {
    new OpenglDemo(platform());
  }
}
