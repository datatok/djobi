package io.datatok.djobi.engine.actions.net.ftp.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.DataException;
import io.datatok.djobi.exceptions.NetworkAccessException;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FTPOutputRunner implements ActionRunner
{
    private static Logger logger = Logger.getLogger(FTPOutputRunner.class);

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();

        if (job.getData() == null) {
            throw new DataException(stage, "Missing job data!");
        }

        final FTPOutputConfig config = (FTPOutputConfig) stage.getParameters();
        final File file = new File(config.path);
        final String pathDirectory = file.getParent();
        final String pathFilename = file.getName();

        final FTPClient ftpClient;

        if (config.proxyHost == null || config.proxyHost.isEmpty()) {
            ftpClient = new FTPSClient();
        } else {
            ftpClient = new FTPHTTPClient(config.proxyHost, config.proxyPort);
            logger.info(String.format("use proxy %s:%d ...", config.proxyHost, config.proxyPort));
        }

        ftpClient.setConnectTimeout(2 * config.timeout);
        ftpClient.setDefaultTimeout(config.timeout);

        if (logger.isDebugEnabled()) {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        }

        try {
            logger.info(String.format("connecting to %s:%d ...", config.host, config.port));

            ftpClient.connect(config.host, config.port);

            logger.debug("connected");

            ftpClient.setSoTimeout(config.timeout);

            int reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new NetworkAccessException(stage, "Exception in connecting to FTP Server");
            }

            logger.debug(String.format("login as %s", config.user));

            if (ftpClient.login(config.user, config.password)) {

                logger.debug("logged");

                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                ftpClient.changeWorkingDirectory(pathDirectory);

                logger.debug("uploading...");

                final String jobData = getJobData(job.getData());
                final InputStream jobDataAsStream = new ByteArrayInputStream(jobData.getBytes(StandardCharsets.UTF_8));

                boolean uploadStatus = ftpClient.storeFile(pathFilename, jobDataAsStream);

                if (uploadStatus) {
                    logger.debug("uploaded");
                } else {
                    throw new StageException(stage, "Upload failed: "+ ftpClient.getReplyString());
                }
            } else {
                throw new NetworkAccessException(stage, "Login authentication failed");
            }

        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return ActionRunResult.success();
    }

    /**
     * Get the job data.
     *
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     * @throws ClassCastException
     */
    private String getJobData(final Object data) throws Exception {
        if (data instanceof String) {
            return (String) data;
        } else if (data instanceof byte[]) {
            return new String( (byte[]) data, StandardCharsets.UTF_8);
        } else {
            throw new Exception("incorrect data type, must be a String!");
        }
    }
}
