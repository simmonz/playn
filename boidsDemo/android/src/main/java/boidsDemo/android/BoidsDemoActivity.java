package boidsDemo.android;

import playn.android.GameActivity;

import boidsDemo.core.BoidsDemo;

public class BoidsDemoActivity extends GameActivity {

  @Override public void main () {
    new BoidsDemo(platform());
  }
}
