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
package io.gravitee.rest.api.portal.rest.resource.param;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TicketsParam {

    @QueryParam("apiId")
    @ApiParam(value = "The api identifier used to filter tickets")
    private String api;

    @QueryParam("field")
    @ApiParam(value = "The field to query when doing `sort` queries")
    private String field;
    
    @QueryParam("order")
    @ApiParam(value = "ASC or DESC to define the sort order")
    private String order;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void validate() {
        if (!"ASC".equals(order) && !"DESC".equals(order)) {
            throw new BadRequestException("'order' query parameter value must be 'ASC' or 'DESC'");
        }
    }
}
