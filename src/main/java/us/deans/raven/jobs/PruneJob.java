package us.deans.raven.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.processor.Maria_DAO;
import us.deans.raven.processor.MongoDao;

import java.util.List;

public class PruneJob {

    private final Logger log = LoggerFactory.getLogger(PruneJob.class);
    private final Maria_DAO mariaDao;
    private final MongoDao mongoDao;

    public PruneJob() {
        this.mariaDao = new Maria_DAO();
        this.mongoDao = new MongoDao();
    }

    public void run() {
        log.info("Starting Operation M1: Remove Duplicates...");

        try {
            // Stage 1: Mark old uploads for pruning in MariaDB
            log.info("Phase 1: Marking old uploads for pruning...");
            mariaDao.markForPruning();

            // Stage 2: Sweep MongoDB and finish deletion in MariaDB
            log.info("Phase 2: Sweeping marked uploads...");
            List<Long> prunedIds = mariaDao.getPrunedUploadIds();
            log.info("Found {} uploads marked for pruning.", prunedIds.size());

            for (Long uploadId : prunedIds) {
                log.info("Processing pruning for upload_id: {}", uploadId);

                // Delete from MongoDB
                long deletedCount = mongoDao.deletePosts(uploadId);
                log.info("Deleted {} posts from MongoDB for upload_id: {}", deletedCount, uploadId);

                // Delete from MariaDB (only if MongoDB deletion was "confirmed" - meaning we
                // attempted it)
                // Note: Even if deletedCount is 0 (e.g. posts already gone), we proceed to
                // remove the tombstone
                mariaDao.deleteUpload(uploadId);
                log.info("Completed pruning for upload_id: {}", uploadId);
            }

            log.info("Operation M1 completed successfully.");

        } catch (Exception e) {
            log.error("Fatal error during Operation M1", e);
        } finally {
            mongoDao.close();
        }
    }
}
