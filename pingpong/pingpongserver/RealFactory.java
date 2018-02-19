package pingpong.pingpongserver;

import rmi.RMIException;

public class RealFactory implements PingPongServerFactory {

    @Override
    public PingPongServer makePingPongServer() throws RMIException{
        return new PingPongServer();
    }
}
