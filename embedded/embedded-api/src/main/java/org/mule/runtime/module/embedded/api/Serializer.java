/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.embedded.api;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class Serializer {

  private Serializer() {}

  static void serialize(Serializable obj, OutputStream outputStream) {
    if (outputStream == null) {
      throw new IllegalArgumentException("The OutputStream must not be null");
    }
    ObjectOutputStream out = null;
    try {
      // stream closed in the finally
      out = new ObjectOutputStream(outputStream);
      out.writeObject(obj);

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }
}
