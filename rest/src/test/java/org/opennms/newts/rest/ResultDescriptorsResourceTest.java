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


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;


public class ResultDescriptorsResourceTest {

    private final static String JSON_SAMPLE = "/temperature.json";

    private final SampleRepository m_repository = mock(SampleRepository.class);
    private final Map<String, ResultDescriptorDTO> m_reports = Maps.newHashMap();
    private final ResultDescriptorsResource m_resultsResource = new ResultDescriptorsResource(m_reports);
    private final MeasurementsResource m_measurementsResource = new MeasurementsResource(m_repository, m_reports);

    @Before
    public void setUp() throws Exception {
        m_reports.put("temps", getResultDescriptorDTO());
    }

    @Test
    public void testGetReport() throws Exception {
        assertThat(
        		m_resultsResource.getReport("temps").toString(),
                CoreMatchers.equalTo(getResultDescriptorDTO().toString()));
    }

    @Test
    public void testWriteReport() throws Exception {
        Response response = m_resultsResource.writeReport("temps2", m_reports.get("temps"));

        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));

        assertThat(
        		m_resultsResource.getReport("temps2").toString(),
                CoreMatchers.equalTo(getResultDescriptorDTO().toString()));
    }

    @Test
    public void testWriteAndUse() throws Exception {
        @SuppressWarnings("unchecked")
        final Results<Measurement> results = mock(Results.class);

        // Write the report
        Response response = m_resultsResource.writeReport("temps3", m_reports.get("temps"));

        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));

        // Manually retrieve the expected data
        when(
                m_repository.select(
                        eq("localhost"),
                        eq(Optional.of(Timestamp.fromEpochSeconds(900000000))),
                        eq(Optional.of(Timestamp.fromEpochSeconds(900003600))),
                        any(ResultDescriptor.class),
                        eq(Duration.seconds(900)))
        ).thenReturn(results);

        // Retrieve the results through the resource using the report and compare
        assertThat(
        		m_measurementsResource.getMeasurements(
                        "temps3",
                        "localhost",
                        Optional.of("1998-07-09T11:00:00-0500"),
                        Optional.of("1998-07-09T12:00:00-0500"),
                        Optional.of("15m")),
                CoreMatchers.is(results));
    }

    private static ResultDescriptorDTO getResultDescriptorDTO() throws JsonProcessingException, IOException {
        InputStream json = ResultDescriptorsResourceTest.class.getResourceAsStream(JSON_SAMPLE);
        return new ObjectMapper().reader(ResultDescriptorDTO.class).readValue(json);
    }

}
