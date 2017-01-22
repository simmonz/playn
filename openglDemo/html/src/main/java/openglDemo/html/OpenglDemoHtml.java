package openglDemo.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import openglDemo.core.OpenglDemo;

public class OpenglDemoHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("openglDemo/");
    new OpenglDemo(plat);
    plat.start();
  }
}
