package play.modules.springtester;

import play.Logger;
import play.PlayPlugin;

public final class MyPlugin extends PlayPlugin {

    public void onApplicationStart() {
        Logger.info("********** yay!!!");
    }
}
