/*
 * Copyright 2014, The OpenNMS Group
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.newts.rest;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;

import org.opennms.newts.api.SampleProcessor;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;


/**
 * Guice configuration for Cassandra sample storage.
 * 
 * @author eevans
 */
public class CassandraModule extends AbstractModule {

    private final NewtsConfig m_newtsConf;

    public CassandraModule(NewtsConfig newtsConfig) {
        m_newtsConf = checkNotNull(newtsConfig, "newtsConfig argument");
    }

    @Override
    protected void configure() {

        bind(String.class).annotatedWith(named("samples.cassandra.keyspace")).toInstance(m_newtsConf.getCassandraKeyspace());
        bind(String.class).annotatedWith(named("samples.cassandra.host")).toInstance(m_newtsConf.getCassandraHost());
        bind(Integer.class).annotatedWith(named("samples.cassandra.port")).toInstance(m_newtsConf.getCassandraPort());
        bind(Integer.class).annotatedWith(named("sampleProcessor.maxThreads")).toInstance(m_newtsConf.getMaxSampleProcessorThreads());

        bind(SampleRepository.class).to(CassandraSampleRepository.class);

        Multibinder.newSetBinder(binder(), SampleProcessor.class);

    }

}
