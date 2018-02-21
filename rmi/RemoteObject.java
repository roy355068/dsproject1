package rmi;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Wrapper class for streaming object. The object implemented Serializable interface and could be serialize to
 * transmit through the network
 */
class RemoteObject implements Serializable {
    /**
     * Name of the method on remote object which been called.
     */
    private String methodName;
    /**
     * An array of class of the types of parameters.
     */
    private Class[] parameterTypes;
    /**
     * The input arguments of the method.
     */
    private Object[] args;
    /**
     * The method on the remote object which been called.
     */
    private Method method;
    /**
     * The returnType of the remote method.
     */
    private Class returnType;


    /**
     * Return value.
     */
    private Object returnValue;
    /**
     * Return status, which could be "success" or "failed".
     */
    private String responseStatus;


    /**
     * Used when stub construct the request.
     * @param methodName name of the method been called
     * @param parameterTypes array of class of the input parameters
     * @param args array of the input parameters
     * @param returnType type of the return value.
     */
    RemoteObject(String methodName, Class[] parameterTypes, Object[] args, Class returnType) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.args = args;
        this.returnType = returnType;
    }

    /**
     * Used when skeleton construct the response.
     * @param responseStatus response state, whether is "success" or "failed"
     * @param returnValue responded value from remote method call
     */
    RemoteObject(String responseStatus, Object returnValue) {
        this.returnValue = returnValue;
        this.responseStatus = responseStatus;
    }


    // getters and setters
    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class getReturnType() {
        return returnType;
    }

    public void setReturnType(Class returnType) {
        this.returnType = returnType;
    }


}
