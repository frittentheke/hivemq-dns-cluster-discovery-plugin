package com.hivemq.plugin.discovery.dns;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hivemq.spi.callback.cluster.ClusterNodeAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DNSDiscoveryCallback.class);
    private static int ClusterPort; // all pods will use the same config and port
    private static String ServiceName;
    private List<ClusterNodeAddress> LastClusterNodes = new ArrayList<>();

    @Override
    public void init(String nodeid, ClusterNodeAddress cna) {
        LOGGER.info("This node runs on host " + cna.getHost() + " and listens on port " + cna.getPort() + ". The cluster-ID is " + nodeid);
        DNSDiscoveryCallback.ClusterPort = cna.getPort();

        ServiceName = System.getenv("SERVICE_NAME");
        LOGGER.info("DNS-based cluster discovery using SERVICE_HOSTNAME " + ServiceName);
    }

    @Override
    public ListenableFuture<List<ClusterNodeAddress>> getNodeAddresses() {
        LOGGER.debug("List of cluster node addresses requested via ClusterDiscoveryCallback!");

        List<ClusterNodeAddress> ClusterNodes = new ArrayList<>();

        // check if we can resolve the IPs for our serviceName
        try {
            LOGGER.debug("Resolving all IPs of for hostname " + ServiceName);
            Arrays.stream(InetAddress.getAllByName(ServiceName)).
                    sorted(new InetAddressComparator()).
                    map(inetAddress -> new ClusterNodeAddress(inetAddress.getHostAddress(), DNSDiscoveryCallback.ClusterPort)).
                    collect(Collectors.toCollection(() -> ClusterNodes));
        } catch (UnknownHostException ex) {
            LOGGER.warn(ex.getMessage());
        }

        String PreviousNodes = joinListOfNodes(LastClusterNodes);
        String CurrentNodes = joinListOfNodes(ClusterNodes);

        // Let's see if anything even changed
        if (PreviousNodes.equals(CurrentNodes)) {
            LOGGER.debug("List of cluster nodes did not change since last lookup  - " + CurrentNodes);
        } else {
            LOGGER.info("List of cluster nodes changed! Previous: {} || Current: {}", PreviousNodes, CurrentNodes);
            LastClusterNodes = ClusterNodes; // Remember the last set of nodes
        }

        return Futures.immediateFuture(ClusterNodes);
    }

    @Override
    public void destroy() {
        LOGGER.debug("Destroying DNSDiscoveryCallback.");
    }

    private String joinListOfNodes(List<ClusterNodeAddress> ClusterNodesToJoin) {
        return ClusterNodesToJoin.stream()
                .map(ClusterNodeAddress::getHost)
                .collect(Collectors.joining(", "));
    }

}