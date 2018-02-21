package pingpong.pingpongserver;

public class PingPongServer {

    /**
     * Remote method ping
     * @param id id number to indicate the number of round of test
     * @return "pong" concatenate with the id
     */
    public String ping(int id) {
        return "pong" + id;
    }
}
