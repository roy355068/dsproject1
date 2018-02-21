package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;
import java.lang.reflect.Constructor;

/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub
{


    /** Creates a stub, given a skeleton with an assigned address.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException {
        validation(c, new Object [] {skeleton});

        // Check if the input address is assigned, else throw UnknownHostException
        if (InetAddress.getLocalHost().getHostAddress() == null) {
            throw new UnknownHostException();
        }

        // If the skeleton has not been assigned an address by the user and has not yet
        // been started, throw IllegalStateException.
        InetSocketAddress skeletonAddress = skeleton.getAddress();
        if (skeletonAddress == null) {
            throw new IllegalStateException("Skeleton has not been initialized");
        }

        // After validation of input, create new DynamicHandler which then be used
        // to create new proxy for the connection with skeleton.
        InvocationHandler handler = new DynamicHandler(skeletonAddress, c);
        return createProxy(handler, c);
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname) {

        // pack the arguments and validate the input
        Object [] args = new Object [] {skeleton, hostname};
        validation(c, args);

        int skeletonPort = skeleton.getPort();
        InetSocketAddress skeletonAddress = skeleton.getAddress();

        // if the address of skeleton is null, or the port number isn't valid (hasn't been assigned a port)
        if (skeletonAddress == null || (skeletonPort <= 0 || skeletonPort > 65536)) {
            throw new IllegalStateException("The skeleton has not been assigned a port");
        }

        // After validation of input, create new DynamicHandler which then be used
        // to create new proxy for the connection with skeleton.
        InetSocketAddress overwrittenAddress = new InetSocketAddress(hostname, skeletonPort);
        InvocationHandler handler = new DynamicHandler(overwrittenAddress, c);
        return createProxy(handler, c);


    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address) {
        Object [] args = new Object [] {address};
        validation(c, args);

        // After validation of input, create new DynamicHandler which then be used
        // to create new proxy for the connection with skeleton.
        InvocationHandler handler = new DynamicHandler(address, c);
        return createProxy(handler, c);
    }

    /**
     * check the validity of the input arguments of the Stub.create methods
     * @param c A <code>Class</code> object representing the interface
    implemented by the remote object.
     * @param args arguments passed in to the create method.
     */
    private static <T> void validation(Class<T> c, Object [] args) {
        // check if the c arg is null
        if (c == null) {
            throw new NullPointerException("The interface is null");
        } else if (!c.isInterface()) { // check if c is an interface, reject if it is not
            throw new Error("The type of c is not interface, the proxy creation will be rejected");
        } else {
            // check if the class of c is remote interface
            Method [] methods = c.getMethods();
            for (Method m : methods) {
                boolean isRMI = false;
                Class<?> [] exceptionTypes = m.getExceptionTypes();
                for (Class exception : exceptionTypes) {
                    if (exception.equals(RMIException.class)) {
                        isRMI = true;
                        break;
                    }
                }

                // To check if the interface implement Remote.
                if (!isRMI) {
                    throw new Error("The interface is not a remote interface, proxy creation rejected");
                }
            }
        }

        // check if any arg in args is null, else throw NullPointerException
        for (Object arg : args) {
            if (arg == null) {
                throw new NullPointerException("One of the arguments in args is null, reject the proxy creation");
            }
        }
    }


    /**
     * Create proxy based on the InvocationHandler and the interface class c
     * @param handler InvocationHandler for create a new instance of proxy
     * @param c interface that is going to be implemented by the proxy
     * @return newly created proxy
     */
    private static <T> T createProxy(InvocationHandler handler, Class<T> c) {
        // the Proxy.newProxyInstance will take in the array of interfaces for the proxy to implement
        // since c itself is the interface, we passed in a Class array with c as the only element to create new proxy
        Class [] interfaces = new Class [] {c};
        Object tempProxy = Proxy.newProxyInstance(c.getClassLoader(), interfaces, handler);
        if (tempProxy instanceof Proxy) {

            // TODO: still don't know how to solve the unchecked cast problem...
            return (T) tempProxy;
        }
        return null;
    }
}
