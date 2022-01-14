package io.datatok.djobi.engine.actions.fs.output;

import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Save content on a FileSystem.
 */
public class FSOutputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(FSOutputRunner.class);

    FSOutputConfig config;

    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        this.config = (FSOutputConfig) stage.getParameters();

        if (contextData == null)
        {
            throw new Exception("context is missing data!");
        }

        if (contextData.getKind().getType().equals(BasicDataKind.TYPE_STRING)) {
            String inData = (String) contextData.getData();
            if (config.path.startsWith("hdfs://")) {
                writeToHDFS((FileSystem) contextExecutor.get("hdfs"), inData, config);
            } else {
                Files.write(Paths.get(config.path), inData.getBytes(StandardCharsets.UTF_8));
            }
        } else if (contextData.getData() instanceof InputStream) {
            InputStream inData = (InputStream) contextData.getData();
            if (config.path.startsWith("hdfs://")) {
                writeToHDFS((FileSystem) contextExecutor.get("hdfs"), inData.toString(), config);
            } else {
                FileOutputStream outStream = new FileOutputStream(config.path);

                IOUtils.copy(inData, outStream);

                outStream.close();
            }
        } else {
            throw new StageException(stage, "data type not supported!");
        }

        return ActionRunResult.success();
    }

    private void writeToHDFS(final FileSystem fileSystem, final String data, final FSOutputConfig config) throws Exception {
        final FSDataOutputStream fileHandle = fileSystem.create(new Path(config.path), true);

        fileHandle.write(data.getBytes(StandardCharsets.UTF_8));

        fileHandle.hsync();
        fileHandle.flush();
        fileHandle.close();

        logger.info(String.format("Write file %s", config.path));
    }

}
