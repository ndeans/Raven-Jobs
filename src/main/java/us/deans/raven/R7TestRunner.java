package us.deans.raven;

import us.deans.raven.processor.MongoDao;
import us.deans.raven.processor.R7Conversation;
import us.deans.raven.processor.R7Post;
import us.deans.raven.processor.R7Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Console-based test runner for Operation R7 (Conversation Linking).
 * Not a production feature — exists to verify R7 output during development.
 *
 * Accepts upload_id as constructor argument. Pass via App.java args or hardcode
 * for testing.
 */
public class R7TestRunner {

    private static final Logger log = LoggerFactory.getLogger(R7TestRunner.class);

    private final String uploadId;

    public R7TestRunner(String uploadId) {
        this.uploadId = uploadId;
    }

    public void run() {
        log.info("R7TestRunner starting for upload_id={}", uploadId);
        MongoDao mongoDao = new MongoDao();
        try {
            R7Service service = new R7Service(mongoDao);
            List<R7Conversation> conversations = service.execute(uploadId);

            System.out.println("\n=== R7 Results for upload_id: " + uploadId + " ===");
            System.out.println("Total conversations found: " + conversations.size());
            System.out.println();

            int i = 1;
            for (R7Conversation conv : conversations) {
                System.out.println("Conversation " + i++ + " (" + conv.length() + " posts):");
                for (R7Post post : conv.getPosts()) {
                    System.out.println("  [post_id: " + post.getPostId() + "] "
                            + post.getAuthor() + " — " + post.getHead());
                }
                System.out.println();
            }
        } finally {
            mongoDao.close();
        }
    }
}
