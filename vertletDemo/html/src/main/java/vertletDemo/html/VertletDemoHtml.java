package vertletDemo.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import vertletDemo.core.VertletDemo;

public class VertletDemoHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("vertletDemo/");
    new VertletDemo(plat);
    plat.start();
  }
}
