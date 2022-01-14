package io.datatok.djobi.engine.actions.net.sftp.output;

import com.jcraft.jsch.ChannelSftp;
import io.datatok.djobi.engine.check.CheckResult;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionPostChecker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.File;

public class SFTPOutputPostChecker implements ActionPostChecker {

    private static Logger logger = Logger.getLogger(SFTPOutputPostChecker.class);

    @Inject
    private SFTPLib sftpLib;

    @Override
    public CheckResult postCheck(Stage stage) throws Exception {
        final SFTPOutputConfig config = (SFTPOutputConfig) stage.getParameters();

        ChannelSftp channel = null;

        try {
            channel = sftpLib.connect(config);

            File f = new File(config.path);

            channel.cd(f.getParent());

            final long fileSize = channel.lstat(f.getName()).getSize();

            return CheckResult.ok(
                    "display", FileUtils.byteCountToDisplaySize(fileSize),
                    "unit", "byte",
                    "value", fileSize
            );

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (channel != null) {
                channel.disconnect();

                channel.getSession().disconnect();
            }
        }
    }
}
