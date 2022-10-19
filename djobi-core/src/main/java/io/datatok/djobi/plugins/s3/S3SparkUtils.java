package io.datatok.djobi.plugins.s3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider;
import org.apache.spark.SparkContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class S3SparkUtils {

    static public FileSystem getFileSystem(SparkContext sc, Map<String, String> spec) throws IOException, URISyntaxException {

        Configuration conf = sc.hadoopConfiguration();

        configure(conf, spec);

        return FileSystem.get(new java.net.URI("s3a://" + spec.get(S3BagKeys.BUCKET)), conf);
    }

    static public void configure(Configuration conf, Map<String, String> spec) {
        //System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");

        conf.set("fs.s3a.aws.credentials.provider", SimpleAWSCredentialsProvider.NAME);

        conf.set("fs.s3a.endpoint", spec.get(S3BagKeys.ENDPOINT));
        conf.set("fs.s3a.access.key", spec.get(S3BagKeys.ACCESS_KEY));
        conf.set("fs.s3a.secret.key", spec.get(S3BagKeys.SECRET_KEY));

        conf.set("fs.s3a.path.style.access", spec.getOrDefault(S3BagKeys.PATH_STYLE, "false"));
        conf.set("fs.s3a.connection.ssl.enabled", spec.getOrDefault(S3BagKeys.SSL, "false"));
        conf.set("fs.s3a.attempts.maximum", "1");
    }

}
