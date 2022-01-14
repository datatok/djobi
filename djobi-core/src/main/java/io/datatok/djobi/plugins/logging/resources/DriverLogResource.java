package io.datatok.djobi.plugins.logging.resources;

import io.datatok.djobi.application.ApplicationData;
import io.datatok.djobi.utils.MyMapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Map;

@Singleton
public class DriverLogResource {

    @Inject
    private ApplicationData runData;

    private String getHost() {
        String host;

        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "__UnknownHostException__";
        }

        return host;
    }

    public final void fillEventData(final Map<String, Object> eventData) {
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        eventData.put("process", MyMapUtils.map(
            "args", runData.getJvmArgs(),
                "args_count", runData.getJvmArgs() == null ? 0 : runData.getJvmArgs().length,
                "command_line", runtimeMXBean.getInputArguments() == null ? "" : String.join(" ", runtimeMXBean.getInputArguments()),
                "executable", "java",
                "working_directory", Paths.get(".").toAbsolutePath().normalize().toString()
        ));

        eventData.put("host", MyMapUtils.map(
        "hostname", getHost(),
            "type", "vm",
            "cpu_cores", Runtime.getRuntime().availableProcessors(),
            "memory", Runtime.getRuntime().totalMemory()
        ));

        eventData.put("user", MyMapUtils.map(
            "name", System.getProperty("user.name")
        ));

        eventData.put("jvm", MyMapUtils.map(
            "name", runtimeMXBean.getVmName(),
            "version", runtimeMXBean.getVmVersion(),
            "vendor", runtimeMXBean.getVmVendor()
        ));
    }

}
