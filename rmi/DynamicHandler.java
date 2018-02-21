package rmi;

import rmi.RMIException;
import rmi.RemoteObject;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/*

    - https://docs.oracle.com/javase/7/docs/technotes/guides/reflection/proxy.html
    Official documentation of Proxy class

    - http://paddy-w.iteye.com/blog/841798
    A Chinese website explaining how to use InvocationHandler to perform proxy generation
    and method invocation

    - https://docs.oracle.com/javase/tutorial/java/IandI/objectclass.html
    - http://www.logicbig.com/how-to/code-snippets/jcode-reflection-class-getmethod/
    Use these two reference to get the entity of the three methods (equals, hashCode, toString)

*/

/**
 * DynamicHandler is an implementing class that implements InvocationHandler and Serializable and will be
 * used as invocation handler in the proxy creation
 * @param <T> type of the interface we are going to implement
 */
class DynamicHandler<T> implements InvocationHandler, Serializable {

    private Class<T> interfaceClass;
    private InetSocketAddress address;
    private InetAddress ipAddress;
    private int port;

    DynamicHandler(InetSocketAddress address, Class<T> interfaceClass) {
        this.address = address;
        this.interfaceClass = interfaceClass;
        this.ipAddress = this.address.getAddress();
        this.port = this.address.getPort();
    }

    /**
     * Overriding the invoke method in the InvocationHandler interface
     * @param proxy the proxy instance that the method was invoked on
     * @param method the Method instance corresponding to the interface method invoked on the proxy instance.
     *               The declaring class of the Method object will be the interface that the method was declared in,
     *               which may be a superinterface of the proxy interface that the proxy class inherits the method through.
     * @param args an array of objects containing the values of the arguments passed in the method invocation on the proxy
     *             instance, or null if interface method takes no arguments.
     * @return the value to return from the method invocation on the proxy instance.
     * @throws Throwable the exception to throw from the method invocation on the proxy instance.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Class<?> objClass = Object.class;

        // method attributes
        String methodName = method.getName();
        Class<?> [] paraTypes = method.getParameterTypes();
        Class returnType = method.getReturnType();
        Class<?> [] exceptionTypes = method.getExceptionTypes();

        // built-in functions
        Method hashCodeMethod = objClass.getMethod("hashCode", null);
        Method equalsMethod = objClass.getMethod("equals", Object.class);
        Method toStringMethod = objClass.getMethod("toString", null);

        if (methodName == "equals" && method.equals(equalsMethod)) {
            if (args[0] instanceof Proxy) {
                DynamicHandler handler = (DynamicHandler) Proxy.getInvocationHandler((Proxy) args[0]);
                return this.interfaceClass.equals(handler.interfaceClass) && this.address.equals(handler.address);
            }
            return false;
        } else if (methodName == "hashCode" && method.equals(hashCodeMethod)) {
            return this.interfaceClass.hashCode() * 31 + this.address.hashCode() * 31;
        } else if (methodName == "toString" && method.equals(toStringMethod)) {
            return this.interfaceClass.getCanonicalName() + ", " + this.address.toString();
        } else {

            ObjectOutputStream out;
            ObjectInputStream in;
            Socket socket;
            Object result;

            try {

                // open a single connection per method call
                socket = new Socket();
                socket.connect(this.address, this.port);

                // out.flush() before instantiate in to avoid deadlock
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                // pack the essential data for method invocation into a RemoteObject object and send it
                RemoteObject request = new RemoteObject(methodName, paraTypes, args, returnType);
                out.writeObject(request);
                out.flush();

                // unpack the returning response and extract the statusString and returnValue
                RemoteObject response = (RemoteObject) in.readObject();
                String statusString = response.getResponseStatus();
                Object returnValue = response.getReturnValue();

                // check the statusString and execute corresponding error handling or value returning
                if (statusString.equals("failed")) {
                    throw (Exception) returnValue;
                } else {
                    in.close();
                    out.close();
                    socket.close();
                }
                return returnValue;

            } catch (Exception e) {
                // check if the exception is results from the method's exception or not
                for (Class ex : exceptionTypes) {
                    if (ex.equals(e.getClass())) {
                        throw e;
                    }
                }
                // if not, then we confirm that there's something wrong with the remote method
                // call procedure, we throw RMIException
                throw new RMIException(e);
            }
        }
    }
}