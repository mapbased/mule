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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public interface EmbeddedContainerFactory {

    static EmbeddedContainer create(String muleVersion, ArtifactInfo application) {
        String m2BasePath = " ";
        URLClassLoader urlClassLoader = new URLClassLoader((URL[]) buildDefaultClassPath(m2BasePath).toArray());

        //        ClassLoader classLoader = new MavenContainerClassLoaderFactory().create(muleVersion);
        ClassLoader classLoader = urlClassLoader;

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

    static List<URL> buildDefaultClassPath(String m2BasePath) {
        List<String> cp = new ArrayList<>();

        cp.add(
            "file:" + m2BasePath + "/.m2/repository/com/mulesoft/mule/mule-core-ee/4.0-SNAPSHOT/mule-core-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/mule-core/4.0-SNAPSHOT/mule-core-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-dsl-api/1.0.0-SNAPSHOT/mule-module-dsl-api-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-metadata-model-xml/1.0.0-SNAPSHOT/mule-metadata-model-xml-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/xmlbeans/xmlbeans/2.6.0/xmlbeans-2.6.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/stax/stax-api/1.0.1/stax-api-1.0.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-metadata-model-java/1.0.0-SNAPSHOT/mule-metadata-model-java-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-metadata-model-json/1.0.0-SNAPSHOT/mule-metadata-model-json-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/everit/json/org.everit.json.schema/1.1.0/org.everit.json.schema-1.1.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/everit/osgi/bundles/org.everit.osgi.bundles.org.json/1.0.0-v20140107/org.everit.osgi.bundles.org.json-1.0.0-v20140107.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/stephenc/eaio-uuid/uuid/3.4.0/uuid-3.4.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/stephenc/eaio-grabbag/grabbag/1.8.1/grabbag-1.8.1.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/javax/transaction/javax.transaction-api/1.2/javax.transaction-api-1.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-connector_1.5_spec/2.0.0/geronimo-j2ee-connector_1.5_spec-2.0.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/lmax/disruptor/3.3.0/disruptor-3.3.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-api/2.5/log4j-api-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-core/2.5/log4j-core-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-slf4j-impl/2.5/log4j-slf4j-impl-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-1.2-api/2.5/log4j-1.2-api-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-jcl/2.5/log4j-jcl-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/logging/log4j/log4j-jul/2.5/log4j-jul-2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/asm/asm/3.1/asm-3.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/asm/asm-commons/3.1/asm-commons-3.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/asm/asm-tree/3.1/asm-tree-3.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/mvel/mule-mvel2/2.1.9-MULE-010/mule-mvel2-2.1.9-MULE-010.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jgrapht/jgrapht-jdk1.5/0.7.3/jgrapht-jdk1.5-0.7.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/reflections/reflections/0.9.10/reflections-0.9.10.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/licm/licm/1.1.6/licm-1.1.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/de/schlichtherle/truexml/1.29/truexml-1.29.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/opensymphony/propertyset/1.3/propertyset-1.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/opensymphony/oscore/2.2.4/oscore-2.2.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/aspectj/aspectjrt/1.8.5/aspectjrt-1.8.5.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-reboot/4.0-SNAPSHOT/mule-module-reboot-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/tanukisoft/wrapper/3.2.3/wrapper-3.2.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-builders/4.0-SNAPSHOT/mule-module-builders-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/spring-context/4.1.9.RELEASE/spring-context-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/spring-expression/4.1.9.RELEASE/spring-expression-4.1.9.RELEASE.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/springframework/spring-web/4.1.9.RELEASE/spring-web-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-extensions-spring-support/4.0-SNAPSHOT/mule-module-extensions-spring-support-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/jboss/javassist/3.7.ga/javassist-3.7.ga.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-extensions-xml-support/4.0-SNAPSHOT/mule-module-extensions-xml-support-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-jbossts/4.0-SNAPSHOT/mule-module-jbossts-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jboss/jbossts/jbossjta/4.15.0.Final/jbossjta-4.15.0.Final.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/jboss/logging/jboss-logging/3.0.0.Beta5/jboss-logging-3.0.0.Beta5.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-management/4.0-SNAPSHOT/mule-module-management-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mx4j/mx4j-jmx/2.1.1/mx4j-jmx-2.1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mx4j/mx4j-impl/2.1.1/mx4j-impl-2.1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mx4j/mx4j-tools/2.1.1/mx4j-tools-2.1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mx4j/mx4j-remote/2.1.1/mx4j-remote-2.1.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/yourkit/yjp-controller-api-redist/9.0.8/yjp-controller-api-redist-9.0.8.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-scripting/4.0-SNAPSHOT/mule-module-scripting-4.0-SNAPSHOT.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/springframework/spring-aop/4.1.9.RELEASE/spring-aop-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/codehaus/groovy/groovy-all/2.4.4/groovy-all-2.4.4-indy.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/python/jython-standalone/2.7.0/jython-standalone-2.7.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/script/js-engine/1.1/js-engine-1.1-jdk14.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mozilla/rhino/1.7R4/rhino-1.7R4.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-scripting-jruby/4.0-SNAPSHOT/mule-module-scripting-jruby-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jruby/jruby-stdlib/1.7.24/jruby-stdlib-1.7.24.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jruby/joni/joni/2.1.9/joni-2.1.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-netdb/1.1.2/jnr-netdb-1.1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-enxio/0.9/jnr-enxio-0.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-x86asm/1.0.2/jnr-x86asm-1.0.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-unixsocket/0.8/jnr-unixsocket-0.8.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-posix/3.0.27/jnr-posix-3.0.27.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jruby/extras/bytelist/1.0.11/bytelist-1.0.11.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jnr-constants/0.9.0/jnr-constants-0.9.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jruby/jcodings/jcodings/1.0.16/jcodings-1.0.16.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jffi/1.2.10/jffi-1.2.10.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/jnr/jffi/1.2.10/jffi-1.2.10-native.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/yaml/snakeyaml/1.15/snakeyaml-1.15.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/jcraft/jzlib/1.1.3/jzlib-1.1.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/headius/invokebinder/1.2/invokebinder-1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/headius/options/1.3/options-1.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/martiansoftware/nailgun-server/0.9.1/nailgun-server-0.9.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jruby/yecht/1.1/yecht-1.1-jruby.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-schedulers/4.0-SNAPSHOT/mule-module-schedulers-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-spring-config/4.0-SNAPSHOT/mule-module-spring-config-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/jaxen/jaxen/1.1.1/jaxen-1.1.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-spring-security/4.0-SNAPSHOT/mule-module-spring-security-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/security/spring-security-core/4.0.4.RELEASE/spring-security-core-4.0.4.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/security/spring-security-config/4.0.4.RELEASE/spring-security-config-4.0.4.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/security/spring-security-web/4.0.4.RELEASE/spring-security-web-4.0.4.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/security/spring-security-ldap/4.0.4.RELEASE/spring-security-ldap-4.0.4.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/ldap/spring-ldap-core/2.0.2.RELEASE/spring-ldap-core-2.0.2.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-tomcat/4.0-SNAPSHOT/mule-module-tomcat-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-tooling-support/4.0-SNAPSHOT/mule-module-tooling-support-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-repository/4.0-SNAPSHOT/mule-module-repository-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-xml/4.0-SNAPSHOT/mule-module-xml-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/thoughtworks/xstream/xstream/1.4.9/xstream-1.4.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xpp3/xpp3_min/1.1.3.4.O/xpp3_min-1.1.3.4.O.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/codehaus/woodstox/woodstox-core-asl/4.4.1/woodstox-core-asl-4.4.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/codehaus/woodstox/stax2-api/3.1.4/stax2-api-3.1.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/java/dev/stax-utils/stax-utils/20080702/stax-utils-20080702.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/saxon/Saxon-HE/9.7.0-3/Saxon-HE-9.7.0-3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/saxon/Saxon-HE/9.7.0-3/Saxon-HE-9.7.0-3-xqj.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/xml/xquery/xqj-api/1.0/xqj-api-1.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-boot-ee/4.0-SNAPSHOT/mule-module-boot-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-saml-ee/4.0-SNAPSHOT/mule-module-saml-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/bouncycastle/bcprov-jdk15on/1.54/bcprov-jdk15on-1.54.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/opensaml/opensaml/2.6.4/opensaml-2.6.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/opensaml/openws/1.5.4/openws-1.5.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/opensaml/xmltooling/1.4.4/xmltooling-1.4.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/ca/juliusdavies/not-yet-commons-ssl/0.3.9/not-yet-commons-ssl-0.3.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/velocity/velocity/1.7/velocity-1.7.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/santuario/xmlsec/1.5.7/xmlsec-1.5.7.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/owasp/esapi/esapi/2.1.0/esapi-2.1.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/ws/security/wss4j/1.6.3/wss4j-1.6.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-http-ee/4.0-SNAPSHOT/mule-module-http-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-core-tests/4.0-SNAPSHOT/mule-core-tests-4.0-SNAPSHOT-tests.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-spring-config-ee/4.0-SNAPSHOT/mule-module-spring-config-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-xa-tx-ee/4.0-SNAPSHOT/mule-module-xa-tx-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-tracking-ee/4.0-SNAPSHOT/mule-module-tracking-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-http-policy/4.0-SNAPSHOT/mule-module-http-policy-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-batch-ee/4.0-SNAPSHOT/mule-module-batch-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-kryo-serializer-ee/4.0-SNAPSHOT/mule-module-kryo-serializer-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/objenesis/objenesis/2.4/objenesis-2.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/esotericsoftware/kryo-shaded/4.0.0/kryo-shaded-4.0.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/esotericsoftware/minlog/1.3.0/minlog-1.3.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/de/javakaffee/kryo-serializers/0.38/kryo-serializers-0.38.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/google/protobuf/protobuf-java/2.6.1/protobuf-java-2.6.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-cache-ee/4.0-SNAPSHOT/mule-module-cache-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/spring-context-support/4.1.9.RELEASE/spring-context-support-4.1.9.RELEASE.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/springframework/spring-core/4.1.9.RELEASE/spring-core-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/ehcache/ehcache-core/2.5.1/ehcache-core-2.5.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-bti-ee/4.0-SNAPSHOT/mule-module-bti-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/btm/mule-btm/2.1.5/mule-btm-2.1.5.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-cluster-ee/4.0-SNAPSHOT/mule-module-cluster-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/hazelcast/hazelcast/3.6.2/hazelcast-3.6.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/eclipsesource/minimal-json/minimal-json/0.9.2/minimal-json-0.9.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/hazelcast/hazelcast-client-protocol/1.0.0/hazelcast-client-protocol-1.0.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-launcher/4.0-SNAPSHOT/mule-module-launcher-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/de/schlichtherle/truelicense/1.29/truelicense-1.29.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-plugin-ee/4.0-SNAPSHOT/mule-module-plugin-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/spring-beans/4.1.9.RELEASE/spring-beans-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/jersey/core/jersey-common/2.11/jersey-common-2.11.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/ws/rs/javax.ws.rs-api/2.0/javax.ws.rs-api-2.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/glassfish/jersey/bundles/repackaged/jersey-guava/2.11/jersey-guava-2.11.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/hk2/hk2-api/2.3.0-b05/hk2-api-2.3.0-b05.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/hk2/hk2-utils/2.3.0-b05/hk2-utils-2.3.0-b05.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/hk2/hk2-locator/2.3.0-b05/hk2-locator-2.3.0-b05.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/glassfish/hk2/osgi-resource-locator/1.0.1/osgi-resource-locator-1.0.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-deployment/4.0-SNAPSHOT/mule-module-deployment-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/ant/ant/1.7.0/ant-1.7.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/ant/ant-launcher/1.7.0/ant-launcher-1.7.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/aspectj/aspectjweaver/1.8.5/aspectjweaver-1.8.5.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/gateway-core/4.0-SNAPSHOT/gateway-core-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-policies/4.0-SNAPSHOT/mule-module-policies-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-autodiscovery/4.0-SNAPSHOT/mule-module-autodiscovery-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-deployment-model-impl/4.0-SNAPSHOT/mule-module-deployment-model-impl-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mapdb/mapdb/1.0.6/mapdb-1.0.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/spullara/mustache/java/compiler/0.9.2/compiler-0.9.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-threat-protection-gw/4.0-SNAPSHOT/mule-module-threat-protection-gw-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-ip-filter-gw/4.0-SNAPSHOT/mule-module-ip-filter-gw-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-proxy/4.0-SNAPSHOT/mule-module-proxy-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-http-ext/4.0-SNAPSHOT/mule-module-http-ext-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-sockets/4.0-SNAPSHOT/mule-module-sockets-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/api-gateway-client/4.0-SNAPSHOT/api-gateway-client-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-endpoint-aliases-gw/4.0-SNAPSHOT/mule-module-endpoint-aliases-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.4.3/jackson-core-2.4.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/jaxrs/jackson-jaxrs-json-provider/2.4.3/jackson-jaxrs-json-provider-2.4.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/jaxrs/jackson-jaxrs-base/2.4.3/jackson-jaxrs-base-2.4.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/module/jackson-module-jaxb-annotations/2.4.3/jackson-module-jaxb-annotations-2.4.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/joda-time/joda-time/2.9.1/joda-time-2.9.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/httpclient-utils-gw/4.0-SNAPSHOT/httpclient-utils-gw-4.0-SNAPSHOT.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/apache/httpcomponents/httpclient-cache/4.4.1/httpclient-cache-4.4.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/httpcomponents/httpasyncclient/4.1/httpasyncclient-4.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/httpcomponents/httpcore-nio/4.4.1/httpcore-nio-4.4.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/httpcomponents/httpasyncclient-cache/4.1/httpasyncclient-cache-4.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-pingfederate-gw/4.0-SNAPSHOT/mule-module-pingfederate-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-federation-gw/4.0-SNAPSHOT/mule-module-federation-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-wsdl-el-gw/4.0-SNAPSHOT/mule-module-wsdl-el-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/mule-module-spring-config-gw/4.0-SNAPSHOT/mule-module-spring-config-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/license-utils-gw/4.0-SNAPSHOT/license-utils-gw-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/services/mule-service-scheduler/4.0-SNAPSHOT/mule-service-scheduler-4.0-SNAPSHOT-mule-service.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/quartz-scheduler/quartz/2.2.3/quartz-2.2.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mchange/c3p0/0.9.5.2/c3p0-0.9.5.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mchange/mchange-commons-java/0.2.11/mchange-commons-java-0.2.11.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/services/mule-service-http/4.0-SNAPSHOT/mule-service-http-4.0-SNAPSHOT-mule-service.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-service-http-api/4.0-SNAPSHOT/mule-service-http-api-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/mule-api/1.0.0-SNAPSHOT/mule-api-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-metadata-model-api/1.0.0-SNAPSHOT/mule-metadata-model-api-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-http/4.0-SNAPSHOT/mule-module-http-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-tls/4.0-SNAPSHOT/mule-module-tls-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/geronimo/specs/geronimo-servlet_3.0_spec/1.0/geronimo-servlet_3.0_spec-1.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-file/4.0-SNAPSHOT/mule-module-file-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-http-ext/4.0-SNAPSHOT/mule-module-http-ext-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/glassfish/grizzly/grizzly-framework/2.3.26/grizzly-framework-2.3.26.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/glassfish/grizzly/grizzly-http-server/2.3.26/grizzly-http-server-2.3.26.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/grizzly/grizzly-http/2.3.26/grizzly-http-2.3.26.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/glassfish/grizzly/connection-pool/2.3.26/connection-pool-2.3.26.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/mail/mail/1.4.3/mail-1.4.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/ning/async-http-client/1.9.39/async-http-client-1.9.39.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/glassfish/grizzly/grizzly-websockets/2.3.26/grizzly-websockets-2.3.26.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-codec/commons-codec/1.9/commons-codec-1.9.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-sockets/4.0-SNAPSHOT/mule-module-sockets-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/plugins/mule-ee-compatibility-plugin/4.0-SNAPSHOT/mule-ee-compatibility-plugin-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/mule-compatibility-core-ee/4.0-SNAPSHOT/mule-compatibility-core-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-compatibility-core/4.0-SNAPSHOT/mule-compatibility-core-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-transport-module-support/4.0-SNAPSHOT/mule-transport-module-support-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-cxf/4.0-SNAPSHOT/mule-module-cxf-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-core/2.7.18/cxf-rt-core-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-api/2.7.18/cxf-api-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-frontend-simple/2.7.18/cxf-rt-frontend-simple-2.7.18.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-bindings-soap/2.7.18/cxf-rt-bindings-soap-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-frontend-jaxws/2.7.18/cxf-rt-frontend-jaxws-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xml-resolver/xml-resolver/1.2/xml-resolver-1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-bindings-xml/2.7.18/cxf-rt-bindings-xml-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-databinding-aegis/2.7.18/cxf-rt-databinding-aegis-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-databinding-jaxb/2.7.18/cxf-rt-databinding-jaxb-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/sun/xml/bind/jaxb-impl/2.1.13/jaxb-impl-2.1.13.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/sun/xml/bind/jaxb-xjc/2.1.13/jaxb-xjc-2.1.13.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-databinding-jibx/2.7.18/cxf-rt-databinding-jibx-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-tools-common/2.7.18/cxf-tools-common-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jibx/jibx-run/1.2.5/jibx-run-1.2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xpp3/xpp3/1.1.3.4.O/xpp3-1.1.3.4.O.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jibx/jibx-schema/1.2.5/jibx-schema-1.2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jibx/jibx-extras/1.2.5/jibx-extras-1.2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jdom/jdom/1.1.3/jdom-1.1.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-transports-local/2.7.18/cxf-rt-transports-local-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-ws-security/2.7.18/cxf-rt-ws-security-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-ws-rm/2.7.18/cxf-rt-ws-rm-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-ws-policy/2.7.18/cxf-rt-ws-policy-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/neethi/neethi/3.0.3/neethi-3.0.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-management/2.7.18/cxf-rt-management-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/cxf/cxf-rt-ws-addr/2.7.18/cxf-rt-ws-addr-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-rt-transports-http/2.7.18/cxf-rt-transports-http-2.7.18.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/cxf/cxf-wstx-msv-validation/2.7.18/cxf-wstx-msv-validation-2.7.18.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/java/dev/msv/msv-core/2011.1/msv-core-2011.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/sun/msv/datatype/xsd/xsdlib/2010.1/xsdlib-2010.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/isorelax/isorelax/20030108/isorelax-20030108.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/relaxngDatatype/relaxngDatatype/20020414/relaxngDatatype-20020414.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/annotation/javax.annotation-api/1.2/javax.annotation-api-1.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-spring-extras/4.0-SNAPSHOT/mule-module-spring-extras-4.0-SNAPSHOT.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/springframework/spring-jdbc/4.1.9.RELEASE/spring-jdbc-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/springframework/spring-tx/4.1.9.RELEASE/spring-tx-4.1.9.RELEASE.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/springframework/spring-jms/4.1.9.RELEASE/spring-jms-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/springframework/spring-messaging/4.1.9.RELEASE/spring-messaging-4.1.9.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-file/4.0-SNAPSHOT/mule-transport-file-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-http/4.0-SNAPSHOT/mule-transport-http-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/tomcat/coyote/6.0.44/coyote-6.0.44.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/tomcat/juli/6.0.44/juli-6.0.44.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/samba/jcifs/jcifs/1.3.3/jcifs-1.3.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-jms/4.0-SNAPSHOT/mule-transport-jms-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-sockets/4.0-SNAPSHOT/mule-transport-sockets-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-ssl/4.0-SNAPSHOT/mule-transport-ssl-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-tcp/4.0-SNAPSHOT/mule-transport-tcp-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/transports/mule-transport-vm/4.0-SNAPSHOT/mule-transport-vm-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/mule/modules/mule-module-saml-cxf-ee/4.0-SNAPSHOT/mule-module-saml-cxf-ee-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-pool/commons-pool/1.6/commons-pool-1.6.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-apikit/4.0.0-SNAPSHOT/mule-module-apikit-4.0.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/raml/parser-interface-impl-v1/4.0.0-SNAPSHOT/parser-interface-impl-v1-4.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/raml/raml-parser/0.9-SNAPSHOT/raml-parser-0.9-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/module/jackson-module-jsonSchema/2.4.4/jackson-module-jsonSchema-2.4.4.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/googlecode/juniversalchardet/juniversalchardet/1.0.3/juniversalchardet-1.0.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/raml/parser-interface/4.0.0-SNAPSHOT/parser-interface-4.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/raml/parser-interface-impl-v2/4.0.0-SNAPSHOT/parser-interface-impl-v2-4.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/raml/raml-parser-2/1.0.5-SNAPSHOT/raml-parser-2-1.0.5-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/raml/yagi/1.0.5-SNAPSHOT/yagi-1.0.5-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/ws/xmlschema/xmlschema-core/2.2.1/xmlschema-core-2.2.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-apikit-spi/4.0.0-SNAPSHOT/mule-module-apikit-spi-4.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/modules/mule-module-cors/2.1.2/mule-module-cors-2.1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/wsdl4j/wsdl4j/1.6.2/wsdl4j-1.6.2.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/com/github/fge/json-schema-validator/2.2.6/json-schema-validator-2.2.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/googlecode/libphonenumber/libphonenumber/6.2/libphonenumber-6.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/fge/json-schema-core/1.2.5/json-schema-core-1.2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/fge/uri-template/0.9/uri-template-0.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/fge/msg-simple/1.1/msg-simple-1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/fge/btf/1.2/btf-1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/github/fge/jackson-coreutils/1.8/jackson-coreutils-1.8.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/javax/mail/mailapi/1.4.3/mailapi-1.4.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.4.3/jackson-databind-2.4.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.4.0/jackson-annotations-2.4.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-file-extension-common/4.0-SNAPSHOT/mule-module-file-extension-common-4.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/weave/mule-plugin-weave/4.0-SNAPSHOT/mule-plugin-weave-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/scala-lang/scala-library/2.11.7/scala-library-2.11.7.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/weave/runtime/1.2.0-SNAPSHOT/runtime-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/weave/wlang/1.2.0-SNAPSHOT/wlang-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/weave/core/1.2.0-SNAPSHOT/core-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/weave/parser/1.2.0-SNAPSHOT/parser-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/parboiled/parboiled_2.11/2.1.3/parboiled_2.11-2.1.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/chuusai/shapeless_2.11/2.3.0/shapeless_2.11-2.3.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/typelevel/macro-compat_2.11/1.1.1/macro-compat_2.11-1.1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/spire-math/spire_2.11/0.11.0/spire_2.11-0.11.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/threeten/threetenbp/1.3.1/threetenbp-1.3.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/weave/core-modules/1.2.0-SNAPSHOT/core-modules-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/fasterxml/woodstox/woodstox-core/5.0.2/woodstox-core-5.0.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/fasterxml/aalto-xml/1.0.0/aalto-xml-1.0.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/helger/jcodemodel/2.8.6/jcodemodel-2.8.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/google/code/findbugs/annotations/2.0.3/annotations-2.0.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/mulesoft/weave/flatfile/1.2.0-SNAPSHOT/flatfile-1.2.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/edi/edi-parser/1.2.2/edi-parser-1.2.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/edi/ltm-datamodel/1.2.2/ltm-datamodel-1.2.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/scala-lang/scala-reflect/2.11.2/scala-reflect-2.11.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/scala-lang/modules/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/spire-math/spire-macros_2.11/0.9.0/spire-macros_2.11-0.9.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/typelevel/machinist_2.11/0.3.0/machinist_2.11-0.3.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/scala-lang/scala-compiler/2.11.5/scala-compiler-2.11.5.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/scala-lang/modules/scala-parser-combinators_2.11/1.0.3/scala-parser-combinators_2.11-1.0.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/activemq/activemq-client/5.11.1/activemq-client-5.11.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/slf4j/slf4j-api/1.7.10/slf4j-api-1.7.10.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/fusesource/hawtbuf/hawtbuf/1.1/hawtbuf-1.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.8.0/jackson-core-asl-1.8.0.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.8.0/jackson-mapper-asl-1.8.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/h2database/h2/1.3.166/h2-1.3.166.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/json-lib/json-lib/2.2/json-lib-2.2-jdk15.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-logging/commons-logging/1.1/commons-logging-1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/ezmorph/ezmorph/1.0.4/ezmorph-1.0.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xom/xom/1.2.5/xom-1.2.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xml-apis/xml-apis/1.3.03/xml-apis-1.3.03.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xerces/xercesImpl/2.8.0/xercesImpl-2.8.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mockobjects/mockobjects-core/0.09/mockobjects-core-0.09.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/junit/junit/4.12/junit-4.12.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jdom/jdom2/2.0.6/jdom2-2.0.6.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-extension/2.0.0-SNAPSHOT/munit-extension-2.0.0-SNAPSHOT-mule-plugin.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-assert/2.0.0-SNAPSHOT/munit-assert-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/hamcrest/hamcrest-all/1.3/hamcrest-all-1.3.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-common/2.0.0-SNAPSHOT/munit-common-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mockito/mockito-all/1.8.2/mockito-all-1.8.2.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/com/mulesoft/munit/munit-mock/2.0.0-SNAPSHOT/munit-mock-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-remote/2.0.0-SNAPSHOT/munit-remote-2.0.0-SNAPSHOT-jar-with-dependencies.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-runner/2.0.0-SNAPSHOT/munit-runner-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/plugins/munit-coverage-plugin/2.0.0-SNAPSHOT/munit-coverage-plugin-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/json/json/20160810/json-20160810.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/utils/munit-mailserver-module/1.0.0/munit-mailserver-module-1.0.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/icegreen/greenmail/1.3/greenmail-1.3.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/oauth/signpost/signpost-core/1.2.1.1/signpost-core-1.2.1.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/utils/munit-mclient-module/1.0.0/munit-mclient-module-1.0.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/utils/munit-dbserver-module/1.0.0/munit-dbserver-module-1.0.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/net/sf/opencsv/opencsv/2.0/opencsv-2.0.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/mysql/mysql-connector-java/5.1.13/mysql-connector-java-5.1.13.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jasypt/jasypt-spring31/1.9.2/jasypt-spring31-1.9.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jasypt/jasypt/1.9.2/jasypt-1.9.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/jhades/jhades/1.0.4/jhades-1.0.4.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xalan/xalan/2.7.2/xalan-2.7.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xalan/serializer/2.7.2/serializer-2.7.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/xmlunit/xmlunit/1.6/xmlunit-1.6.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/modules/mule-interceptor-module/2.0.0-SNAPSHOT/mule-interceptor-module-2.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/cglib/cglib-nodep/2.2/cglib-nodep-2.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/cglib/cglib/3.2.2/cglib-3.2.2.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/ow2/asm/asm/5.0.4/asm-5.0.4.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/tests/mule-tests-runner/4.0-SNAPSHOT/mule-tests-runner-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-api/1.0.2.v20150114/aether-api-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-impl/1.0.2.v20150114/aether-impl-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-spi/1.0.2.v20150114/aether-spi-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-util/1.0.2.v20150114/aether-util-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/maven/maven-aether-provider/3.3.9/maven-aether-provider-3.3.9.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/maven/maven-model-builder/3.3.9/maven-model-builder-3.3.9.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/org/codehaus/plexus/plexus-interpolation/1.21/plexus-interpolation-1.21.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/maven/maven-artifact/3.3.9/maven-artifact-3.3.9.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/maven/maven-builder-support/3.3.9/maven-builder-support-3.3.9.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/apache/maven/maven-repository-metadata/3.3.9/maven-repository-metadata-3.3.9.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/codehaus/plexus/plexus-component-annotations/1.6/plexus-component-annotations-1.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/codehaus/plexus/plexus-utils/3.0.22/plexus-utils-3.0.22.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/maven/maven-model/3.3.9/maven-model-3.3.9.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-connector-basic/1.0.2.v20150114/aether-connector-basic-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-transport-file/1.0.2.v20150114/aether-transport-file-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/eclipse/aether/aether-transport-http/1.0.2.v20150114/aether-transport-http-1.0.2.v20150114.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/apache/httpcomponents/httpclient/4.2.6/httpclient-4.2.6.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/slf4j/jcl-over-slf4j/1.6.2/jcl-over-slf4j-1.6.2.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-extensions-support/4.0-SNAPSHOT/mule-module-extensions-support-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/extensions/mule-extensions-api-dsql/1.0.0-SNAPSHOT/mule-extensions-api-dsql-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/antlr/antlr-runtime/3.5/antlr-runtime-3.5.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/antlr/stringtemplate/3.2.1/stringtemplate-3.2.1.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/extensions/mule-extensions-api/1.0.0-SNAPSHOT/mule-extensions-api-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/reactivestreams/reactive-streams/1.0.0/reactive-streams-1.0.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/extensions/mule-extensions-api-persistence/1.0.0-SNAPSHOT/mule-extensions-api-persistence-1.0.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/mule-metadata-model-persistence/1.0.0-SNAPSHOT/mule-metadata-model-persistence-1.0.0-SNAPSHOT.jar");
        cp.add(
            "file:" + m2BasePath + "/.m2/repository/io/projectreactor/reactor-core/3.0.5.RELEASE/reactor-core-3.0.5.RELEASE.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-service/4.0-SNAPSHOT/mule-module-service-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-container/4.0-SNAPSHOT/mule-module-container-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-artifact/4.0-SNAPSHOT/mule-module-artifact-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/modules/mule-module-deployment-model/4.0-SNAPSHOT/mule-module-deployment-model-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/com/google/guava/guava/18.0/guava-18.0.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/munit/munit-remote/2.0.0-SNAPSHOT/munit-remote-2.0.0-SNAPSHOT-jar-with-dependencies.jar");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/services/mule-service-scheduler/4.0-SNAPSHOT/mule-service-scheduler-4.0-SNAPSHOT.zip");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/org/mule/services/mule-service-http/4.0-SNAPSHOT/mule-service-http-4.0-SNAPSHOT.zip");
        cp.add("file:" + m2BasePath
                   + "/.m2/repository/com/mulesoft/anypoint/policy-templates/4.0-SNAPSHOT/policy-templates-4.0-SNAPSHOT-dist.zip");

        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/modules/mule-module-embedded-api-4.0-SNAPSHOT.jar");
        cp.add("file:" + m2BasePath + "/.m2/repository/org/mule/modules/mule-module-embedded-impl-4.0-SNAPSHOT.jar");

        List<URL> urls = new ArrayList<>();
        for (String e : cp) {
            try {
                urls.add(new URL(e));
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        }


        return urls;
    }

}
