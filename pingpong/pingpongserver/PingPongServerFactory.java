package pingpong.pingpongserver;

import rmi.RMIException;

public interface PingPongServerFactory {
    PingPongServer makePingPongServer() throws RMIException;
}
