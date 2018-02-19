package rmi;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Wrapper class for streaming object. The object implemented Serializable interface and could be serialize to
 * transmit through the network
 */
class RemoteObject implements Serializable {
    private String methodName;
    private Class[] parameterTypes;
    private Object[] args;
    private Method method;
    private Class returnType;


    private Object returnValue;
    private String responseStatus;


    // Used when stub construct the request
    RemoteObject(String methodName, Class[] parameterTypes, Object[] args, Class returnType) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.args = args;
        this.returnType = returnType;
    }

    // Used when skeleton construct the response
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
