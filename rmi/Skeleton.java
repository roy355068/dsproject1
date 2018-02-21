package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

/** RMI skeleton

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the <code>Stub</code> class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
    private InetSocketAddress socketAddress;
    private ServerSocket listenSocket;

    // server is the real object that we're going to call the method on
    private T server;
    private Socket listeningConnection;
    private ListenThread listenThread;
    private int port;
    private String hostName;
    private Class<?> IClass;


    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */

    // c is the object of the class of the interface -> so c must be an interface type
    // an object implementing the said interface
    public Skeleton(Class<T> c, T server) throws Error, NullPointerException {
        // if c is not interface, throw exception
        validate(c, server);
        this.server = server;
        this.IClass = c;
    }


    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address) throws Error, NullPointerException {
        validate(c, server);

        this.socketAddress = address;
        this.server = server;
        this.IClass = c;
        if (address != null) {
            this.port = address.getPort();
            this.hostName = address.getHostName();
        }
    }

    /**
     * Getter of the address of the socket.
     * @return inetsocketaddress
     */
    public InetSocketAddress getAddress() {
        return this.socketAddress;
    }

    /**
     * Getter of the port number.
     * @return the port number
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Validate the input of Skeleton constructors. If c is not a interface, then throw Error.
     * If c or the input server is null, then throw NullPointerException. Or, if any of the class
     * doesn't implement Remote, throw RMIException.
     * @param c Class of the interface
     * @param server server
     */
    private void validate(Class<T> c, T server) {
        if (!c.isInterface()) {
            throw new Error("The type of c is not interface, the skeleton creation will be rejected");
        } else if (c == null || server == null) {
            throw new NullPointerException("The types shouldn't be null");
        } else {

            for (Method method : c.getDeclaredMethods()) {
                Class<?> [] exceptionTypes = method.getExceptionTypes();
                boolean isRMI = false;
                for (Class exception : exceptionTypes) {
                    if (exception == RMIException.class)
                        isRMI = true;
                }

                if (!isRMI)
                    throw new Error("The interface is not a remote interface, skeleton creation rejected");
            }
        }
    }

    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    }

    /** Starts the skeleton server.
        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
        if (listenThread != null && listenThread.isAlive()) {
            throw new RMIException("Server is running");
        }


        try {
            this.listenSocket = new ServerSocket();
            // if address is not provided, assign one for it.
            if (socketAddress == null) {
                this.socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1234);

            } else if (listenSocket == null || listenSocket.isClosed()) {
                this.listenSocket = new ServerSocket();

            }
            this.listenSocket.bind(this.socketAddress);
            listenThread = new ListenThread(listenSocket, this.IClass);
            listenThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop()
    {
        // if listenThread exists and running, stop the thread by setting the stop
        if (listenThread != null && listenThread.isAlive()) {
            listenThread.setLive(false);
            try {
                listenSocket.close();
                listenThread.join();
                stopped(null);
            } catch (IOException | InterruptedException e) {
                stopped(e);
            }
        }
    }

    /**
     * ListenThread: It's a thread which create new thread each time the request comes.
     * There is exactly one listen thread.
     */
    private class ListenThread extends Thread {
        /**
         * Flag to indicate whether listen thread is running.
         */
        private boolean live = true;
        private ServerSocket serverSocket;
        private Class<T> IClass;

        ListenThread (ServerSocket listenSocket, Class IClass) {
            this.serverSocket = listenSocket;
            this.IClass = IClass;
        }
        void setLive(boolean status) {
            this.live = status;
        }


        @Override
        public void run() {
            try {
                // keep listening to the requests
                while (live) {
                    // create new worker thread upon receiving request
                    ServiceThread serviceThread = null;
                    try {
                        serviceThread = new ServiceThread(this.serverSocket.accept(), IClass);
                    } catch (IOException e) {
                        if (live) {
                            stopped(e);
                        }
                    }
                    try {
                        serviceThread.start();

                    } catch (NullPointerException e) {
                     //System.out.println("service is down");
                        System.out.print("");
                    }
                }
            } finally {
                try {
                    this.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * ServiceThread: It's the thread been created each time a new client call come.
     * And service thread is the thread which actually handle the request, get the remote
     * method and handle the response.
     */
    private class ServiceThread extends Thread {
        private Socket socket;
        private Class<T> IClass;

        public ServiceThread(Socket socket, Class<T> IClass) {
            this.socket = socket;
            this.IClass = IClass;
        }

        @Override
        public void run() {
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            Object result = null;
            String statusString = null;
            RemoteObject response = null;

            try {
                out = new ObjectOutputStream(this.socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(this.socket.getInputStream());

                // Wrap the request to a RemoteObject object which implements serializable.
                // And get the method, parameters and any required input to call the remote method.
                RemoteObject request = (RemoteObject) in.readObject();
                String methodName = request.getMethodName();
                Class<T> [] parameterTypes = request.getParameterTypes();
                Object[] args = request.getArgs();
                Class<T> returnType = request.getReturnType();

                Method method = this.IClass.getMethod(methodName, parameterTypes);

                try {
                    // here we invoke the real method on the server object.
                    // If the returnType is void then return null.
                    result = returnType.toString().equals("Void") ? null : method.invoke(server, args);
                    statusString = result == null ? "void" : "SUCCESS";

                } catch (InvocationTargetException e) {
                    result = e.getTargetException();
                    statusString = "FAILED";

                }
            } catch (Exception e) {
                service_error(new RMIException(e));

            } finally {
                try {
                    // Write the method result to response.
                    response = new RemoteObject(statusString, result);
                    out.writeObject(response);
                    out.flush();
                    out.close();
                    in.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Please don't write null to the output stream");
                }
            }
        }
    }


}
