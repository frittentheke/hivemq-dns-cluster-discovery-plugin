package com.hivemq.plugin.discovery.dns;

import com.hivemq.spi.HiveMQPluginModule;
import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.plugin.meta.Information;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the plugin module class, which handles the initialization and
 * configuration of the plugin.
 *
 * @author Christian Rohmann, inovex GmbH
 */
@Information(name = "HiveMQ DNS Cluster Discovery Plugin", author = "Christian Rohmann - inovex GmbH", version = "0.3", description = "Resolve hostname / FQDN to a list of IPs to then form a HiveMQ Cluster")
public class DNSDiscoveryPluginModule extends HiveMQPluginModule {

    Logger log = LoggerFactory.getLogger(DNSDiscoveryPluginModule.class);
    String hivemqClusterID;

    /**
     * This method is provided to execute some custom plugin configuration
     * stuff. Is is the place to execute Google Guice bindings,etc if needed.
     */
    @Override
    protected void configurePlugin() {
        log.debug("Configuration of DNSDiscoveryPluginModule called!");
    }

    /**
     * This method returns the main class of our the plugin.
     *
     * @return callback priority
     */
    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return DNSDiscoveryPluginEntryPoint.class;
    }
}
