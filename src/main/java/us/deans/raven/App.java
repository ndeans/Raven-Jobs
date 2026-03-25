package us.deans.raven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.jobs.PruneJob;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Raven-Jobs is starting...");

        PruneJob job = new PruneJob();
        job.run();

        log.info("Raven-Jobs has finished.");
    }
}
