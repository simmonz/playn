package life.android;

import playn.android.GameActivity;

import life.core.Life;

public class LifeActivity extends GameActivity {

  @Override public void main () {
    new Life(platform());
  }
}
