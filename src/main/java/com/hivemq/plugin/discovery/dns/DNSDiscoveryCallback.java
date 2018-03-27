package com.hivemq.plugin.discovery.dns;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hivemq.spi.callback.cluster.ClusterNodeAddress;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This callback will be registered with the HiveMQ callbackRegistry to called
 * at cluster discovery intervals
 *
 * @author Christian Rohmann, inovex GmbH
 */
public class DNSDiscoveryCallback implements com.hivemq.spi.callback.cluster.ClusterDiscoveryCallback {

    private static final Logger Log = LoggerFactory.getLogger(DNSDiscoveryCallback.class);
    private int clusterPort; // all pods will use the same config and port
    private static String ServiceName;

    @Override
    public void init(String nodeid, ClusterNodeAddress cna) {
        this.clusterPort = cna.getPort();
        Log.info("This node runs on host " + cna.getHost() + " and listens on port " + cna.getPort() + ". The cluster-ID is " + nodeid);

        ServiceName = System.getenv("SERVICE_NAME");
        Log.info("DNS-based cluster discovery using SERVICE_HOSTNAME " + ServiceName);
    }

    @Override
    public ListenableFuture<List<ClusterNodeAddress>> getNodeAddresses() {
        Log.debug("List of cluster node addresses requested via ClusterDiscoveryCallback!");

        List<ClusterNodeAddress> clusterNodes = new ArrayList<>();
        InetAddress[] ips = null;

        try {
            // check if we can resolve the IPs for our serviceName
            //ips = InetAddress.getAllByName(ServiceName);                            
            ips = InetAddress.getAllByName(ServiceName);
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(DNSDiscoveryCallback.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (InetAddress addr : ips) {
            clusterNodes.add(new ClusterNodeAddress(addr.getHostAddress(), this.clusterPort));
        }

        String joinedClusterNodes = clusterNodes.stream()
                .map(ClusterNodeAddress::getHost)
                .collect(Collectors.joining(", "));

        Log.info("Those are our cluster members IP addresses: {}", joinedClusterNodes);
        return Futures.immediateFuture(clusterNodes);
    }

    @Override
    public void destroy() {
        Log.debug("Destroying DNSDiscoveryCallback.");
    }

}
