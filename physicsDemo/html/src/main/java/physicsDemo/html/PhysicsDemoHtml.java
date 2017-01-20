package physicsDemo.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import physicsDemo.core.PhysicsDemo;

public class PhysicsDemoHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("physicsDemo/");
    new PhysicsDemo(plat);
    plat.start();
  }
}
