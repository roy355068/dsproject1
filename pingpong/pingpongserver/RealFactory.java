package pingpong.pingpongserver;

public class RealFactory implements PingPongServerFactory {

    @Override
    public PingPongServer makePingPongServer() {
        return new PingPongServer();
    }
}
