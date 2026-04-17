package us.deans.raven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.jobs.OperationM3;
import us.deans.raven.jobs.PruneJob;
import us.deans.raven.processor.M2Result;
import us.deans.raven.processor.M2Service;
import us.deans.raven.processor.M3Result;
import us.deans.raven.processor.Maria_DAO;
import us.deans.raven.processor.MongoDao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            case "M2" -> {
                if (args.length < 2) {
                    log.error("M2 requires at least one upload_id. Usage: App M2 <upload_id> [upload_id ...]");
                    System.exit(1);
                }
                List<Long> uploadIds = Arrays.stream(args, 1, args.length)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                List<M2Result> results = new M2Service(new MongoDao(), new Maria_DAO()).execute(uploadIds);
                for (M2Result r : results) {
                    if (r.isSuccess()) {
                        System.out.printf("upload_id=%-6d  posts_deleted=%-6d  OK%n",
                                r.getUploadId(), r.getPostsDeleted());
                    } else {
                        System.out.printf("upload_id=%-6d  posts_deleted=%-6d  FAILED — %s%n",
                                r.getUploadId(), r.getPostsDeleted(), r.getMessage());
                    }
                }
            }
            case "M3" -> {
                M3Result result = new OperationM3().run();
                if (!result.isMatch()) System.exit(1);
            }
            default -> {
                log.error("Unknown operation: {}. Valid options: M1, M2, M3, R7", op);
                System.exit(1);
            }
        }

        log.info("Raven-Jobs has finished.");
    }
}
