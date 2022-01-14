package io.datatok.djobi.engine.actions.net.sftp.output;

import com.jcraft.jsch.ChannelSftp;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SFTPOutputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(SFTPOutputRunner.class);

    @Inject
    private SFTPLib sftpLib;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {

        if (!contextData.getKind().getType().equals(BasicDataKind.TYPE_STRING))
        {
            throw new StageException(stage, "data must be a plain string!");
        }

        final SFTPOutputConfig config = (SFTPOutputConfig) stage.getParameters();
        final String plainData = (String) contextData.getData();
        final InputStream inputStream = IOUtils.toInputStream(plainData, Charset.defaultCharset());

        ChannelSftp channel = null;

        try {
            channel = sftpLib.connect(config);

            File f = new File(config.path);

            channel.cd(f.getParent());

            channel.put(inputStream, f.getName());

            //System.out.println(channel.lstat(f.getName()).getSize());

            logger.debug("file transferred");

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (channel != null) {
                channel.disconnect();

                channel.getSession().disconnect();
            }
        }

        return ActionRunResult.success();
    }

}
