package life.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import life.core.Life;

public class LifeHtml implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("life/");
    new Life(plat);
    plat.start();
  }
}
