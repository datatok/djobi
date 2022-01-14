package io.datatok.djobi.engine.actions.net.sftp.output;

import com.google.inject.Inject;
import com.jcraft.jsch.*;
import com.typesafe.config.Config;
import io.datatok.djobi.configuration.Configuration;
import org.apache.log4j.Logger;

import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class SFTPLib {

    private static Logger logger = Logger.getLogger(SFTPLib.class);

    @Inject
    private Configuration configuration;

    public ChannelSftp connect(SFTPOutputConfig config) throws JSchException {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        session = jsch.getSession(config.user, config.host, config.port);
        session.setPassword(config.password);

        setupProxy(config.host, session);

        java.util.Properties connectionConfig = new java.util.Properties();
        connectionConfig.put("StrictHostKeyChecking", "no");
        session.setConfig(connectionConfig);
        session.connect();

        logger.debug("Host connected");

        channel = session.openChannel("sftp");
        channel.connect();

        logger.debug("sftp channel opened and connected.");

        channelSftp = (ChannelSftp) channel;

       return channelSftp;
    }

    /**
     * If transport have proxy attached.
     */
    private void setupProxy(String host, Session session) {
        final String transport = "web";

        if (host.contains(".") && this.configuration.hasPath("djobi.http.transports." + transport)) {
            final Config configValue = this.configuration.getConfig("djobi.http.transports." + transport);

            if (configValue != null) {
                if (configValue.hasPath("proxy") && configValue.getObject("proxy").size() > 0) {
                    try {
                        final URL proxyHttpUrl = new URL(configValue.getString("proxy.http"));

                        session.setProxy(new ProxyHTTP(proxyHttpUrl.getHost(),proxyHttpUrl.getPort()));
                    } catch(MalformedURLException e) {
                        logger.error("Cannot use proxy", e);
                    }
                }
            }
        }
    }

}
