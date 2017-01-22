package radGravDemo.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import radGravDemo.core.RadGravDemo;

public class RadGravDemoHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("radGravDemo/");
    new RadGravDemo(plat);
    plat.start();
  }
}
