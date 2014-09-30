/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.functional.functional.StringAppendTestTransformer;
import org.mule.functional.junit4.FunctionalTestCase;

import org.junit.Test;

public class TransformerAttributeTestCase extends AbstractVmOsgiTestCase
{

    public static final String OUTBOUND_MESSAGE = "Test message";

    @Override
    protected String getConfigFile()
    {
        return "vm/transformer-attribute-test-flow.xml";
    }

    @Test
    public void testSimple() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage message = client.send("vm://simple", OUTBOUND_MESSAGE, null);
        assertNotNull(message);
        assertEquals(StringAppendTestTransformer.appendDefault(OUTBOUND_MESSAGE)  + " Received",
                message.getPayloadAsString());
    }

    @Test
    public void testThrough() throws Exception
    {
        MuleClient client = muleContext.getClient();
        MuleMessage message = client.send("vm://chained", OUTBOUND_MESSAGE, null);
        assertNotNull(message);
        assertEquals(StringAppendTestTransformer.appendDefault(OUTBOUND_MESSAGE)  + " Received",
                message.getPayloadAsString());
    }
}
