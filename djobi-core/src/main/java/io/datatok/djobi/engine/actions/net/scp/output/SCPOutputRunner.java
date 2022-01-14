package io.datatok.djobi.engine.actions.net.scp.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

public class SCPOutputRunner implements ActionRunner
{
    private static Logger logger = Logger.getLogger(SCPOutputRunner.class);

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final SCPOutputConfig config = (SCPOutputConfig) stage.getParameters();

        final File temp = File.createTempFile("datahub", ".csv");
        final BufferedWriter bw = new BufferedWriter(new FileWriter(temp));

        bw.write(getJobData(job.getData()));
        bw.close();

        final String[] command              = buildSCPCommand(config.port.toString(), config.identity, temp.getAbsolutePath(), String.format("%s@%s:%s", config.user, config.host, config.path));
        final Runtime runtime               = Runtime.getRuntime();
        final Process process               = runtime.exec(command);
        final BufferedReader outputBuffer   = getOutput(process);
        final BufferedReader error          = getError(process);
        final StringBuilder logs            = new StringBuilder();
        final StringBuilder errStringBuffer = new StringBuilder();

        logger.debug(String.join(" ", command));

        String ligne;

        while ((ligne = outputBuffer.readLine()) != null) {
            logs.append("out: ").append(ligne).append("\n");
        }

        while ((ligne = error.readLine()) != null) {
            logs.append("err: ").append(ligne).append("\n");
            errStringBuffer.append(ligne).append("\n");
        }

        if (errStringBuffer.length() > 0) {
            throw new Exception(errStringBuffer.toString());
        }

        logger.info(String.format("[output:scp] Sent file to [%s:%s] [%s]", config.host, config.path, logs.toString()));

        process.waitFor();

        return ActionRunResult.success();
    }

    /**
     * Build the SCP shell command.
     *
     * @param port
     * @param identityFile
     * @param sourcePath
     * @param target
     * @return
     */
    private String[] buildSCPCommand(final String port, final String identityFile, final String sourcePath, final String target) {
        Stream<String> buffer =  Arrays.stream(new String[]{
                "scp",
                "-o",
                "LogLevel=ERROR",
                "-o",
                "StrictHostKeyChecking=no",
                "-o",
                "BatchMode=yes",
                "-o",
                "UserKnownHostsFile=/dev/null",
                "-P",
                port
        });

        if (identityFile != null && !identityFile.isEmpty()) {
            buffer = Stream.concat(buffer, Arrays.stream(new String[]{"-i", identityFile}));
        }

        buffer = Stream.concat(buffer, Arrays.stream(new String[]{sourcePath, target}));

        return buffer.toArray(String[]::new);
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

    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }
}
