package physicsDemo.android;

import playn.android.GameActivity;

import physicsDemo.core.PhysicsDemo;

public class PhysicsDemoActivity extends GameActivity {

  @Override public void main () {
    new PhysicsDemo(platform());
  }
}
