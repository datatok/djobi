package io.datatok.djobi.plugins.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import io.datatok.djobi.utils.Bag;

public class S3Client {

    public AmazonS3 getClient(Bag args) {
        AWSCredentials credentials = new BasicAWSCredentials(args.getString(S3BagKeys.ACCESS_KEY), args.getString(S3BagKeys.SECRET_KEY));

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(args.getString(S3BagKeys.SSL, "false").equals("true") ? Protocol.HTTPS : Protocol.HTTP);

        AmazonS3 conn = new AmazonS3Client(credentials, clientConfig);

        conn.setEndpoint(args.getString(S3BagKeys.ENDPOINT));
        conn.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(args.getString(S3BagKeys.PATH_STYLE, "false").equals("true")));

        return conn;
    }

    public void makeFolderPublic(Bag args, String bucket, String keyPrefix) {
        final AmazonS3 client = getClient(args);

        client.listObjects(bucket, keyPrefix).getObjectSummaries().forEach(obj -> {
            client.setObjectAcl(bucket, obj.getKey(), CannedAccessControlList.PublicRead);
        });
    }

}
