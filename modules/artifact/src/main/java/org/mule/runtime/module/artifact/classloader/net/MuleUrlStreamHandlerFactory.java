/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.artifact.classloader.net;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for loading URL protocol handlers. This factory is necessary to make Mule work in cases where the standard approach
 * using system properties does not work, e.g. in application servers or with maven's surefire tests.
 * <p>
 * Client classes can register a subclass of {@link URLStreamHandler} for a given protocol. This implementation first checks its
 * registered handlers before resorting to the default mechanism.
 * <p>
 * 
 * @see java.net.URL#URL(String, String, int, String)
 */
public class MuleUrlStreamHandlerFactory extends Object implements URLStreamHandlerFactory {

  private static final String HANDLER_PKGS_SYSTEM_PROPERTY = "java.protocol.handler.pkgs";
  private static final Logger log = LoggerFactory.getLogger(MuleUrlStreamHandlerFactory.class);

  private static Map registry = Collections.synchronizedMap(new HashMap());

  /**
   * Install an instance of this class as UrlStreamHandlerFactory. This may be done exactly once as {@link URL} will throw an
   * {@link Error} on subsequent invocations.
   * <p>
   * This method takes care that multiple invocations are possible, but the UrlStreamHandlerFactory is installed only once.
   */
  public static synchronized void installUrlStreamHandlerFactory() {
    /*
     * When running under surefire, this class will be loaded by different class loaders and will be running in multiple "main"
     * thread objects. Thus, there is no way for this class to register a globally available variable to store the info whether
     * our custom UrlStreamHandlerFactory was already registered.
     * 
     * The only way to accomplish this is to catch the Error that is thrown by URL when trying to re-register the custom
     * UrlStreamHandlerFactory.
     */
    try {
      URL.setURLStreamHandlerFactory(new MuleUrlStreamHandlerFactory());
    } catch (Error err) {
      if (log.isDebugEnabled()) {
        log.debug("Custom MuleUrlStreamHandlerFactory already registered", err);
      }
    }
  }

  public static void registerHandler(String protocol, URLStreamHandler handler) {
    registry.put(protocol, handler);
  }

  public URLStreamHandler createURLStreamHandler(String protocol) {
    URLStreamHandler handler = (URLStreamHandler) registry.get(protocol);
    if (handler == null) {
      handler = this.defaultHandlerCreateStrategy(protocol);
    }
    return handler;
  }

  private URLStreamHandler defaultHandlerCreateStrategy(String protocol) {
    String packagePrefixList = System.getProperty(HANDLER_PKGS_SYSTEM_PROPERTY, "");

    if (packagePrefixList.endsWith("|") == false) {
      packagePrefixList += "|sun.net.www.protocol";
    }

    StringTokenizer tokenizer = new StringTokenizer(packagePrefixList, "|");

    URLStreamHandler handler = null;
    while (handler == null && tokenizer.hasMoreTokens()) {
      String packagePrefix = tokenizer.nextToken().trim();
      String className = packagePrefix + "." + protocol + ".Handler";
      try {
        handler = (URLStreamHandler) instanciateClass(className);
      } catch (Exception ex) {
        // not much we can do here
      }
    }

    return handler;
  }

  public static <T> T instanciateClass(Class<? extends T> clazz, Object... constructorArgs) throws SecurityException,
      NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
    Class<?>[] args;
    if (constructorArgs != null) {
      args = new Class[constructorArgs.length];
      for (int i = 0; i < constructorArgs.length; i++) {
        if (constructorArgs[i] == null) {
          args[i] = null;
        } else {
          args[i] = constructorArgs[i].getClass();
        }
      }
    } else {
      args = new Class[0];
    }

    // try the arguments as given
    // Constructor ctor = clazz.getConstructor(args);
    Constructor<?> ctor = getConstructor(clazz, args, false);

    if (ctor == null) {
      // try again but adapt value classes to primitives
      ctor = getConstructor(clazz, ClassUtils.wrappersToPrimitives(args), false);
    }

    if (ctor == null) {
      StringBuilder argsString = new StringBuilder(100);
      for (Class<?> arg : args) {
        argsString.append(arg.getName()).append(", ");
      }
      throw new NoSuchMethodException("could not find constructor on class: " + clazz + ", with matching arg params: "
          + argsString);
    }

    return (T) ctor.newInstance(constructorArgs);
  }

  public static Object instanciateClass(String name, Object... constructorArgs) throws ClassNotFoundException, SecurityException,
      NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
    return instanciateClass(name, constructorArgs, (ClassLoader) null);
  }

  public static Object instanciateClass(String name, Object[] constructorArgs, Class<?> callingClass)
      throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    Class<?> clazz = ClassUtils.getClass(callingClass.getClassLoader(), name);
    return instanciateClass(clazz, constructorArgs);
  }

  public static Object instanciateClass(String name, Object[] constructorArgs, ClassLoader classLoader)
      throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    Class<?> clazz;
    if (classLoader != null) {
      clazz = ClassUtils.getClass(classLoader, name);
    } else {
      clazz = ClassUtils.getClass(ClassUtils.class.getClassLoader(), name);
    }
    if (clazz == null) {
      throw new ClassNotFoundException(name);
    }
    return instanciateClass(clazz, constructorArgs);
  }

  public static Constructor getConstructor(Class clazz, Class[] paramTypes, boolean exactMatch) {
    for (Constructor ctor : clazz.getConstructors()) {
      Class[] types = ctor.getParameterTypes();
      if (types.length == paramTypes.length) {
        int matchCount = 0;
        for (int x = 0; x < types.length; x++) {
          if (paramTypes[x] == null) {
            matchCount++;
          } else {
            if (exactMatch) {
              if (paramTypes[x].equals(types[x]) || types[x].equals(paramTypes[x])) {
                matchCount++;
              }
            } else {
              if (paramTypes[x].isAssignableFrom(types[x]) || types[x].isAssignableFrom(paramTypes[x])) {
                matchCount++;
              }
            }
          }
        }
        if (matchCount == types.length) {
          return ctor;
        }
      }
    }
    return null;
  }

}
