package us.deans.raven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.jobs.PruneJob;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Raven-Jobs is starting...");

        String op = args.length > 0 ? args[0].toUpperCase() : "M1";

        switch (op) {
            case "R7" -> {
                if (args.length < 2) {
                    log.error("R7 requires an upload_id argument. Usage: App R7 <upload_id>");
                    System.exit(1);
                }
                String uploadId = args[1];
                new R7TestRunner(uploadId).run();
            }
            case "M1" -> new PruneJob().run();
            default -> {
                log.error("Unknown operation: {}. Valid options: M1, R7", op);
                System.exit(1);
            }
        }

        log.info("Raven-Jobs has finished.");
    }
}
