package radGravDemo.android;

import playn.android.GameActivity;

import radGravDemo.core.RadGravDemo;

public class RadGravDemoActivity extends GameActivity {

  @Override public void main () {
    new RadGravDemo(platform());
  }
}
