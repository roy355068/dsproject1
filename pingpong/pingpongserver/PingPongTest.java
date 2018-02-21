package pingpong.pingpongserver;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PingPongTest {
    /**
     * Main driver of the PingPongTest
     * @param args command line argument
     */
    public static void main(String[] args) {
        PingPongServerFactory factory = new RealFactory();
        Skeleton<PingPongServerFactory> skeleton = null;


        try {
            // take the localHost's hostname and create the skeleton at port 80
            InetSocketAddress inet = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 80);
            skeleton = new Skeleton<PingPongServerFactory>(PingPongServerFactory.class, factory, inet);
            // start the skeleton and wait for requests
            skeleton.start();

        } catch (Exception e) {
            e.printStackTrace();
            skeleton.stop();
        }
    }
}
