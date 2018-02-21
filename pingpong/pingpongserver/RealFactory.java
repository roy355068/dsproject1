package pingpong.pingpongserver;

import rmi.RMIException;

public class RealFactory implements PingPongServerFactory {

    /**
     * Return the newly constructed PingPongServer
     * @return the newly constructed PingPongServer
     * @throws RMIException when the server cannot be created or the connection is failed
     */
    @Override
    public PingPongServer makePingPongServer() throws RMIException{
        return new PingPongServer();
    }
}
