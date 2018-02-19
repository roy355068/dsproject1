package pingpong.pingpongclient;

import pingpong.pingpongserver.PingPongServer;
import pingpong.pingpongserver.PingPongServerFactory;
import rmi.Stub;

import java.net.InetSocketAddress;

public class PingPongClient {
    // Magic number. Test 4 times as the handout specified
    private static final int TEST_ROUND = 4;

    // pass in the skeleton address and port number as command line arguments
    public static void main(String[] args) {

        String skeletonHostname = args[0];
        int skeletonPort = Integer.valueOf(args[1]);

        // use create(Class<T> c, InetSocketAddress address) method to create proxy
        InetSocketAddress skeletonSocket = new InetSocketAddress(skeletonHostname, skeletonPort);
        PingPongServerFactory pFactory = Stub.create(PingPongServerFactory.class, skeletonSocket);

        // invoke the makePingServer in the pFactory and get the remote object (PingPongServer)
        PingPongServer pServer = pFactory.makePingPongServer();

        for (int i = 0 ; i < TEST_ROUND ; i ++) {
            pServer.ping(i);
        }

    }
}
