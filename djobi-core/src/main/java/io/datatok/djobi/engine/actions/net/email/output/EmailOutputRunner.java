package io.datatok.djobi.engine.actions.net.email.output;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Created by tom on 25/07/2017.
 */
public class EmailOutputRunner implements ActionRunner {

    @Inject
    protected Configuration configuration;

    @Inject
    protected TemplateUtils templateUtils;

    private static Logger logger = Logger.getLogger(EmailOutputRunner.class);

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final Bag stageConfiguration = stage.getSpec();

        final String messageContentRaw = job.getPipeline().getResources(stageConfiguration.getString("template"));

        final String messageContent = templateUtils.render(messageContentRaw, job);
        final String messageSubject = templateUtils.render(stageConfiguration.getString("title"), job);

        Properties mailServerProperties = System.getProperties();

        mailServerProperties.put("mail.smtp.port", configuration.getInt("email.port"));
        mailServerProperties.put("mail.smtp.host",  configuration.getString("email.host"));

        Session getMailSession = Session.getDefaultInstance(mailServerProperties);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(getMailSession);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress( configuration.getString("email.author")));

        message.addRecipient(Message.RecipientType.TO, new InternetAddress(stageConfiguration.getString("to")));

        if (stageConfiguration.containsKey("bcc")) {
            final Class clazz = stageConfiguration.getClass("bcc");
            if (clazz == String.class) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(stageConfiguration.getString("bcc")));
            } else if (clazz != null) {
                for (String recipient : stageConfiguration.getStringList("bcc")) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
                }
            }
        }

        // Set Subject: header field
        message.setSubject(messageSubject);

        // Body content
        MimeBodyPart bodyPart = new MimeBodyPart();

        bodyPart.setText(messageContent);

        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(bodyPart);

        /*if (job instanceof PipelineDefinition) {
            for (JobDefinition job : ((PipelineDefinition) job).getJobs().values()) {
                for (Job JobRun : job.getJobs()) {
                    if (JobRun.isExecuted() && JobRun.getData() != null) {
                        // Attached part
                        MimeBodyPart attachedPart = new MimeBodyPart();

                        try {
                            attachedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(((String) JobRun.getData()).getBytes("UTF-8"), "application/octet-stream")));
                            attachedPart.setFileName(MustacheView.render(stageConfiguration.getString("attached"), (PipelineDefinition) job, JobRun));

                            multipart.addBodyPart(attachedPart);
                        } catch (MessagingException e) {
                            logger.error("sending email", e);
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            logger.error("sending email", e);
                        }
                    }
                }
            }
        } else {*/

            if (contextData != null) {
                if (contextData.getKind().getType().equals(BasicDataKind.TYPE_STRING)) {
                    // Attached part
                    MimeBodyPart attachedPart = new MimeBodyPart();

                    try {
                        attachedPart.setDataHandler(new DataHandler(new ByteArrayDataSource(((String) contextData.getData()).getBytes(StandardCharsets.UTF_8), "application/octet-stream")));
                        attachedPart.setFileName(templateUtils.render(stageConfiguration.getString("attached"), job));

                        multipart.addBodyPart(attachedPart);

                        logger.info("added attachment from job data");
                    } catch (MessagingException e) {
                        logger.error("sending email", e);
                        e.printStackTrace();
                    }
                }
            }

        message.setContent(multipart);

        // Send message
        Transport.send(message);
        
        logger.info(String.format("[output:email] Sent message [%s] to [%s] successfully", messageSubject, stageConfiguration.get("to")));

        return ActionRunResult.success();
    }

}
