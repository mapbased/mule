/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.transport.file;

import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleMessage;
import org.mule.runtime.core.transport.AbstractMuleMessageFactory;

import java.io.File;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

/**
 * <code>FileMuleMessageFactory</code> creates a new {@link MuleMessage} with a
 * {@link File} or {@link InputStream} payload. Users can obtain the filename and
 * directory in the properties using <code>FileConnector.PROPERTY_FILENAME</code> and
 * <code>FileConnector.PROPERTY_DIRECTORY</code>.
 */
public class FileMuleMessageFactory extends AbstractMuleMessageFactory
{
    private final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    @Override
    protected Class<?>[] getSupportedTransportMessageTypes()
    {
        return new Class[]{File.class, ReceiverFileInputStream.class};
    }

    @Override
    protected Object extractPayload(Object transportMessage, String encoding) throws Exception
    {
        return transportMessage;
    }

    @Override
    protected void addProperties(DefaultMuleMessage message, Object transportMessage) throws Exception
    {
        super.addProperties(message, transportMessage);
        File file = convertToFile(transportMessage);
        setPropertiesFromFile(message, file);
    }

    @Override
    protected String getMimeType(Object transportMessage)
    {
        File file = convertToFile(transportMessage);

        return mimetypesFileTypeMap.getContentType(file.getName().toLowerCase());
    }

    protected File convertToFile(Object transportMessage)
    {
        File file = null;

        if (transportMessage instanceof File)
        {
            file = (File) transportMessage;
        }
        else if (transportMessage instanceof ReceiverFileInputStream)
        {
            file = ((ReceiverFileInputStream) transportMessage).getCurrentFile();
        }

        return file;
    }

    protected void setPropertiesFromFile(DefaultMuleMessage message, File file)
    {
        message.setInboundProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, file.getName());
        message.setInboundProperty(FileConnector.PROPERTY_DIRECTORY, file.getParent());
        message.setInboundProperty(FileConnector.PROPERTY_FILE_SIZE, file.length());
        message.setInboundProperty(FileConnector.PROPERTY_FILE_TIMESTAMP, file.lastModified());
    }
}
