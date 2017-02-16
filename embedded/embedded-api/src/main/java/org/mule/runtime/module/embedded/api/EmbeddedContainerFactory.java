/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.embedded.api;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface EmbeddedContainerFactory {

  static EmbeddedContainer create(String muleVersion, ArtifactInfo application) {
    ClassLoader classLoader = new MavenContainerClassLoaderFactory().create(muleVersion);

    try {
      Class<?> controllerClass = classLoader.loadClass("org.mule.runtime.module.embedded.impl.EmbeddedController");

      Constructor<?> constructor = controllerClass.getConstructor(ArtifactInfo.class);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);
      Serializer.serialize(application, outputStream);
      Object o = constructor.newInstance(outputStream);

      return new EmbeddedContainer() {

        @Override
        public void start() {
          try {
            Method startMethod = o.getClass().getMethod("start");
            startMethod.invoke(o);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        @Override
        public void stop() {
          try {
            Method stopMethod = o.getClass().getMethod("stop");
            stopMethod.invoke(o);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
    } catch (Exception e) {
      throw new IllegalStateException("Cannot create embedded container", e);
    }
  }
}
