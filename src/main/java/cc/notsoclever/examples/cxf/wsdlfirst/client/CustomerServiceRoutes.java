package cc.notsoclever.examples.cxf.wsdlfirst.client;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.example.customerservice.Customer;
import com.example.customerservice.NoSuchCustomerException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.cxf.message.MessageContentsList;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomerServiceRoutes extends RouteBuilder {
    private static final transient Logger LOG = LoggerFactory.getLogger(CustomerServiceRoutes.class);


    @Override
    public void configure() throws Exception {

        LOG.info("Starting client routes");

        onException(NoSuchCustomerException.class)
                .log("SUCCESS NotFoundTest - NoSuchCustomerException detected.")
                .handled(true);

        from("timer://NotFoundTest?repeatCount=1")
                .setHeader(CxfConstants.OPERATION_NAMESPACE, simple("http://customerservice.example.com/"))
                .setHeader(CxfConstants.OPERATION_NAME, simple("getCustomersByName"))
                .setBody(simple("None"))
                .to("cxf:bean:customerService")
                .log(LoggingLevel.ERROR, "ERROR - NoSuchCustomerException should have been thrown");

        from("timer://FindTest?repeatCount=1")
                .setHeader(CxfConstants.OPERATION_NAMESPACE, simple("http://customerservice.example.com/"))
                .setHeader(CxfConstants.OPERATION_NAME, simple("getCustomersByName"))
                .setBody(simple("Smith"))
                .to("cxf:bean:customerService")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        MessageContentsList contents = exchange.getIn().getBody(MessageContentsList.class);
                        List<Customer> customers = (List<Customer>) contents.get(0);
                        Assert.assertEquals(2, customers.size());
                        Assert.assertEquals("Smith", customers.get(0).getName());
                        LOG.info("SUCCESS getCustomersByName");
                    }
                });
    }
}
