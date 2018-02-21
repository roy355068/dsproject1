package pingpong.pingpongserver;

import rmi.RMIException;
import rmi.Skeleton;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PingPongTest {
    public static void main(String[] args) {
        PingPongServerFactory factory = new RealFactory();
        Skeleton<PingPongServerFactory> skeleton = null;

        try {
            InetSocketAddress inet = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 80);
            skeleton = new Skeleton<>(PingPongServerFactory.class, factory, inet);

            skeleton.start();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                skeleton.stop();
            } catch (NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }
}
