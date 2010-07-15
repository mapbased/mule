package org.mule.api.annotations.param;

import org.mule.api.annotations.meta.Evaluator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on component methods, this parameter annotation passes in a reference to a {@link java.util.Map} that can be used to populate
 * outbound attachments that will be set with the outgoing message. For example, when sending an email message,
 * you may want to add attachments such as images or documents.
 * <p/>
 * This annotation must only be defined on a parameter of type {@link java.util.Map}. The elements in the map will be
 * of type {@link javax.activation.DataHandler}, thus the annotated parameter should be {@link java.util.Map&lt;java.lang.String, javax.activation.DataHandler&gt;}
 * Where the key is the attachment name and the value is the handler for the attachment.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Evaluator("sendAttachments")
public @interface OutboundAttachments
{
}