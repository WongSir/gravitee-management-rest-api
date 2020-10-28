/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.management.rest.resource;

import io.gravitee.common.data.domain.Page;
import io.gravitee.repository.analytics.query.Order;
import io.gravitee.repository.management.api.search.TicketCriteria;
import io.gravitee.rest.api.model.TicketEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;

import static io.gravitee.common.http.HttpStatusCode.OK_200;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PlatformTicketsResourceTest extends AbstractResourceTest {

    private static final String TICKET = "my-ticket";

    private TicketEntity fakeTicketEntity;

    @Override
    protected String contextPath() {
        return "tickets/";
    }

    @Before
    public void setUp() {
        reset(ticketService);

        fakeTicketEntity = new TicketEntity();
        fakeTicketEntity.setId(TICKET);
        fakeTicketEntity.setSubject("subject");
    }

    @Test
    public void shouldFindTicket() {

        when(ticketService.findById(any(String.class))).thenReturn(fakeTicketEntity);

        Response ticket = envTarget(TICKET)
                .request()
                .get();

        assertEquals(OK_200, ticket.getStatus());
    }

    @Test
    public void shouldSearchTickets() {

        ArgumentCaptor<TicketCriteria> criteriaCaptor = ArgumentCaptor.forClass(TicketCriteria.class);

        when(ticketService.search(criteriaCaptor.capture(), any()))
                .thenReturn(new Page<>(singletonList(fakeTicketEntity), 1, 1, 1));

        Response response = envTarget()
                .queryParam("page", 1)
                .queryParam("size", 10)
                .queryParam("field", "subject")
                .queryParam("order", true)
                .request()
                .get();

        assertEquals(OK_200, response.getStatus());
        TicketCriteria criteria = criteriaCaptor.getValue();
        assertEquals("Criteria user", USER_NAME, criteria.getFromUser());
        assertEquals("Criteria sort field", "subject", criteria.getSort().getField());
        assertEquals("Criteria sort order", Order.ASC, criteria.getSort().getOrder());

        verify(ticketService, Mockito.times(1))
                .search(any(), argThat(o -> o.getPageNumber() == 1 && o.getPageSize() == 10));
    }
}