package io.datatok.djobi.engine.actions;

import io.datatok.djobi.engine.actions.csv.CSVType;
import io.datatok.djobi.engine.actions.fs.input.FSInputConfigurator;
import io.datatok.djobi.engine.actions.fs.input.FSInputPreChecker;
import io.datatok.djobi.engine.actions.fs.input.FSInputRunner;
import io.datatok.djobi.engine.actions.fs.input.FSInputType;
import io.datatok.djobi.engine.actions.fs.output.FSOutputConfigurator;
import io.datatok.djobi.engine.actions.fs.output.FSOutputPostChecker;
import io.datatok.djobi.engine.actions.fs.output.FSOutputRunner;
import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.actions.net.email.output.EmailOutputRunner;
import io.datatok.djobi.engine.actions.net.email.output.NetEmailOutputType;
import io.datatok.djobi.engine.actions.net.ftp.output.FTPOutputConfigurator;
import io.datatok.djobi.engine.actions.net.ftp.output.FTPOutputRunner;
import io.datatok.djobi.engine.actions.net.ftp.output.FTPOutputType;
import io.datatok.djobi.engine.actions.net.scp.output.SCPOutputConfigurator;
import io.datatok.djobi.engine.actions.net.scp.output.SCPOutputPostChecker;
import io.datatok.djobi.engine.actions.net.scp.output.SCPOutputRunner;
import io.datatok.djobi.engine.actions.net.scp.output.SCPOutputType;
import io.datatok.djobi.engine.actions.net.sftp.output.SFTPOutputConfigurator;
import io.datatok.djobi.engine.actions.net.sftp.output.SFTPOutputPostChecker;
import io.datatok.djobi.engine.actions.net.sftp.output.SFTPOutputRunner;
import io.datatok.djobi.engine.actions.net.sftp.output.SFTPOutputType;
import io.datatok.djobi.engine.actions.sql.SQLConfigurator;
import io.datatok.djobi.engine.actions.sql.SQLType;
import io.datatok.djobi.engine.actions.transformer.serializer.SerializerConfigurator;
import io.datatok.djobi.engine.actions.transformer.serializer.SerializerRunner;
import io.datatok.djobi.engine.actions.transformer.serializer.SerializerType;
import io.datatok.djobi.engine.actions.transformer.serializers.TemplateRenderActionRunner;
import io.datatok.djobi.engine.actions.csv.CSVFilterConfigurator;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorConfigurator;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorRunner;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorType;
import io.datatok.djobi.engine.actions.utils.stdout.StdoutRunner;
import io.datatok.djobi.engine.actions.utils.stdout.StdoutType;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.stage.ActionProvider;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.utils.StageCandidate;
import io.datatok.djobi.utils.ClassUtils;

import java.util.Arrays;
import java.util.List;

public class DjobiActionProvider extends ActionProvider {

    static public final String NAME = "io.djobi";

    static private final List<String> whitelistStages = Arrays.asList(StdoutType.NAME, FSInputType.TYPE, FSOutputType.TYPE, "generator");

    @Override
    public void configure() {
        registerConfigurator(RandomGeneratorType.NAME, RandomGeneratorConfigurator.class);
        registerConfigurator(SQLType.NAME, SQLConfigurator.class);

        registerRunner(DjobiActionProvider.resolveFullname(RandomGeneratorType.NAME), RandomGeneratorRunner.class);

        registerRunner(StdoutType.TYPE, StdoutRunner.class);

        configureFS();

        configureNet();

        configureTransformers();

        registerProvider(DjobiActionProvider.NAME, this);
    }

    @Override
    public StageCandidate getGenericActionCandidates(Stage stage, String phase, ExecutionContext executionContext) {
        final String type = stage.getKind();

        // Be sure action exists.
        if (!hasAction(phase, resolveFullname(type))) {
            return null;
        }

        if (whitelistStages.contains(type)) {
            return new StageCandidate(resolveFullname(type), 10);
        }

        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static String resolveFullname(String name) {
        return NAME + "." + name;
    }

    protected void configureFS() {
        registerConfigurator(FSInputType.TYPE, FSInputConfigurator.class);
        registerRunner(resolveFullname(FSInputType.TYPE), FSInputRunner.class);
        registerPreChecker(resolveFullname(FSInputType.TYPE), FSInputPreChecker.class);

        registerConfigurator(FSOutputType.TYPE, FSOutputConfigurator.class);
        registerRunner(resolveFullname(FSOutputType.TYPE), FSOutputRunner.class);
        registerPostChecker(resolveFullname(FSOutputType.TYPE), FSOutputPostChecker.class);
    }

    protected void configureNet() {
        if (ClassUtils.isClass("javax.mail.MessagingException")) {
            registerRunner(NetEmailOutputType.TYPE, EmailOutputRunner.class);
        }

        registerConfigurator(FTPOutputType.TYPE, FTPOutputConfigurator.class);
        registerRunner(FTPOutputType.TYPE, FTPOutputRunner.class);

        registerConfigurator(SCPOutputType.TYPE, SCPOutputConfigurator.class);
        registerRunner(SCPOutputType.TYPE, SCPOutputRunner.class);
        registerPostChecker(SCPOutputType.TYPE, SCPOutputPostChecker.class);

        if (ClassUtils.isClass("com.jcraft.jsch.ChannelSftp")) {
            registerConfigurator(SFTPOutputType.TYPE, SFTPOutputConfigurator.class);
            registerRunner(SFTPOutputType.TYPE, SFTPOutputRunner.class);
            registerPostChecker(SFTPOutputType.TYPE, SFTPOutputPostChecker.class);
        }
    }

    protected void configureTransformers() {
        registerConfigurator(CSVType.NAME, CSVFilterConfigurator.class);

        registerRunner(TemplateRenderActionRunner.TYPE, TemplateRenderActionRunner.class);

        registerConfigurator(SerializerType.TYPE, SerializerConfigurator.class);
        registerRunner(SerializerType.TYPE, SerializerRunner.class);
    }

}
