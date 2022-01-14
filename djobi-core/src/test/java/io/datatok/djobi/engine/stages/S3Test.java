package io.datatok.djobi.engine.stages;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.plugins.s3.actions.FSOutputPostCheckerS3Spark;
import io.datatok.djobi.plugins.s3.actions.FSOutputS3Spark;
import io.datatok.djobi.spark.actions.generator.SparkGeneratorRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.TestEngine;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(MyTestRunner.class)
public class S3Test extends ActionTest {

    static public final String ACCESS_KEY = "root";
    static public final String SECRET_KEY = "rootroot";
    static public final String S3_REGION = "us-east-1";
    static public final String S3_ENDPOINT = "http://minio:9000";

    static public final String BUCKET = "djobi";

    private final AmazonS3 conn;

    @Inject
    private TestEngine engine;

    S3Test() {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);

        this.conn = new AmazonS3Client(credentials, clientConfig);
        this.conn.setEndpoint(S3_ENDPOINT);
        this.conn.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
    }


    @BeforeEach
    public void setupS3()
    {
        cleanS3();

        this.conn.createBucket(BUCKET);

        engine.newStage();
    }

    @AfterEach
    public void cleanS3()
    {
        if (this.conn.doesBucketExistV2(BUCKET)) {
            this.conn.listObjects(BUCKET).getObjectSummaries().forEach(obj -> this.conn.deleteObject(BUCKET, obj.getKey()));

            this.conn.deleteBucket(BUCKET);
        }
    }

    @Test void selectS3ActionTest() {
        engine
            .withKind(FSOutputType.TYPE)
            .withSpec(new Bag("path", "s3a://toto/hello"))
            .configure()
        ;

        Assertions.assertSame(engine.getRunner().getClass(), FSOutputS3Spark.class);
        Assertions.assertSame(engine.getPostChecker().getClass(), FSOutputPostCheckerS3Spark.class);
    }

    @Test void testJSONFile() throws Exception {

        Bag spec = new Bag(
            "mode", "overwrite",
            "path", "s3a://" + BUCKET + "/test_json",
            "s3", MyMapUtils.mapString(
                "endpoint", S3_ENDPOINT,
                "access_key", ACCESS_KEY,
                "secret_key", SECRET_KEY,
                "ssl", "false",
                "path_style", "true",
                "bucket", BUCKET
            )
        );

        getSparkExecutor().connect();

        Dataset<Row> df = SparkGeneratorRunner.runOnSpark(getSparkExecutor(), 1000);

        df = df.repartition(5);

        engine
            .withKind(FSOutputType.TYPE)
            .withSpec(spec)
            .withJobData(new SparkDataframe(df))
            .configure()
        ;

        engine.run();

        Assertions.assertDoesNotThrow(() -> conn.getObjectMetadata(BUCKET,"test_json/_SUCCESS"));

        CheckResult result = engine.postCheck();

        Assertions.assertEquals(CheckStatus.DONE_OK, result.getStatus());
        Assertions.assertTrue(((Number) result.getMeta("value")).intValue() > 1000);
    }

    /**
     * Only for dev purposes.
     */
    public void testFileExists()
    {
        try {
            ObjectMetadata s3Object = conn.getObjectMetadata(BUCKET, "test_parquet4/_temporary/0/_temporary/attempt_202009031645_0001_m_000000_0/part-r-00000-add88822-d661-4bbd-addf-8d98afb2f445.gz.parquet");

            System.out.println(s3Object.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
