package us.deans.raven.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.deans.raven.processor.M3Result;
import us.deans.raven.processor.M3Service;
import us.deans.raven.processor.Maria_DAO;
import us.deans.raven.processor.MongoDao;

import java.util.Collections;

public class OperationM3 {

    private static final Logger log = LoggerFactory.getLogger(OperationM3.class);

    private final Maria_DAO mariaDao;
    private final MongoDao mongoDao;

    public OperationM3() {
        this.mariaDao = new Maria_DAO();
        this.mongoDao = new MongoDao();
    }

    public M3Result run() {
        log.info("Starting Operation M3: Daily Verification...");
        try {
            M3Result result = new M3Service(mariaDao, mongoDao).execute();
            if (result.isMatch()) {
                log.info("M3: verification passed — mariaDb={} mongoDb={}", result.getMariaDbCount(), result.getMongoDbCount());
            } else {
                log.error("M3: verification FAILED — mariaDb={} mongoDb={}", result.getMariaDbCount(), result.getMongoDbCount());
            }
            return result;
        } catch (Exception e) {
            log.error("M3: fatal error during verification", e);
            return new M3Result(0, 0, false, e.getMessage(), Collections.emptyMap());
        } finally {
            mongoDao.close();
        }
    }
}
