package us.deans.raven.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.processor.Maria_DAO;
import us.deans.raven.processor.MongoDao;
import us.deans.raven.processor.RvnJob;

import java.util.List;

public class BackfillOpAuthor {

    private static final Logger log = LoggerFactory.getLogger(BackfillOpAuthor.class);

    public void run() {
        Maria_DAO mariaDao = new Maria_DAO();
        MongoDao mongoDao = new MongoDao();
        try {
            List<RvnJob> uploads = mariaDao.getMetaData();
            log.info("Backfill: {} uploads to process", uploads.size());
            int updated = 0;
            int skipped = 0;
            for (RvnJob upload : uploads) {
                String uploadId = String.valueOf(upload.getJob_id());
                String author = mongoDao.getFirstAuthor(uploadId);
                if (!author.isEmpty()) {
                    mariaDao.updateOpAuthor(upload.getJob_id(), author);
                    updated++;
                } else {
                    log.warn("No posts found for upload_id: {}", uploadId);
                    skipped++;
                }
            }
            log.info("Backfill complete: {} updated, {} skipped", updated, skipped);
        } catch (Exception e) {
            log.error("Backfill failed", e);
        } finally {
            mongoDao.close();
        }
    }
}
