package vertletDemo.android;

import playn.android.GameActivity;

import vertletDemo.core.VertletDemo;

public class VertletDemoActivity extends GameActivity {

  @Override public void main () {
    new VertletDemo(platform());
  }
}
