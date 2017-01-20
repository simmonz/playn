package boidsDemo.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import boidsDemo.core.BoidsDemo;

public class BoidsDemoHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("boidsDemo/");
    new BoidsDemo(plat);
    plat.start();
  }
}
