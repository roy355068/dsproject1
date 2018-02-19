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

    /* https://docs.oracle.com/javase/7/docs/technotes/guides/reflection/proxy.html

    http://paddy-w.iteye.com/blog/841798 - A Chinese website explaining how to use
     InvocationHandler to perform proxy generation and method invoke

    three bulit-in functions
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


        // TODO :
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

                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                RemoteObject request = new RemoteObject(methodName, paraTypes, args, returnType);
                out.writeObject(request);
                out.flush();

                RemoteObject response = (RemoteObject) in.readObject();
                String statusString = response.getResponseStatus();
                Object returnValue = response.getReturnValue();

                if (statusString.equals("FAILED")) {
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