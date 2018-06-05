/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.metrics.reused;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class ReusedClassLevelCounterTest {

    static final String COUNTER_NAME = "log-invocations";

    @Inject
    LogService logger;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    MetricRegistry appMetrics;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(ReusedClassLevelCounterTest.class.getPackage());
    }

    @Test
    public void testCounter() {
        logger.info("foo");
        logger.info("foo");
        logger.error("ble");
        Counter counter = appMetrics.getCounters().get(COUNTER_NAME);
        assertNotNull(
                COUNTER_NAME + " not found; registered counters: "
                        + appMetrics.getCounters().entrySet().stream().map(e -> e.getKey() + " = " + e.getValue().getCount()).collect(Collectors.joining(", ")),
                counter);
        // 1x LogService constructor, 1x error(), 1x info
        assertThat(counter.getCount(), allOf(equalTo(4l), equalTo(LogService.COUNTER.get())));
    }

}