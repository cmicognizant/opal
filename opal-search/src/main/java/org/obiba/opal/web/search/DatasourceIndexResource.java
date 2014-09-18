/*
 * Copyright (c) 2014 The Hyve B.V. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0 or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiOperation;
import org.obiba.magma.*;
import org.obiba.opal.web.model.Opal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@Scope("request")
@Path("/datasource/{ds}/indexes")
public class DatasourceIndexResource extends IndexResource {

    @PathParam("ds")
    private String datasource;

    @GET
    @ApiOperation(value = "Get index status of all tables in this datasource")
    public Response allIndexes() {
        if(!valuesIndexManager.isReady()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
        }

        List<Opal.TableIndexStatusDto> dtos = Lists.newArrayList();
        for(ValueTable table : getDatasource().getValueTables()) {
            dtos.add(getTableIndexationDto(datasource, table.getName()).build());
        }

        //note: based on /org/jboss/resteasy/core/ResourceMethodInvoker.java line 304...

        return Response.ok().entity(
                new GenericEntity<List<Opal.TableIndexStatusDto>>(dtos){/*no body, only to preserve generic type*/}
        ).build();
    }

    @PUT
    public Response updateAllIndexes() {

        if(!esProvider.isEnabled()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
        }

        for(ValueTable table : getDatasource().getValueTables()) {
            updateIndex(table);
        }

        return Response.noContent().build();
    }

    Datasource getDatasource() {
        return MagmaEngine.get().hasDatasource(datasource)
                ? MagmaEngine.get().getDatasource(datasource)
                : MagmaEngine.get().getTransientDatasourceInstance(datasource);
    }

    void updateIndex(ValueTable table) {
        if (!synchroManager.isAlreadyQueued(variablesIndexManager, getValueTableIndex(datasource, table.getName()))) {
            synchroManager.synchronizeIndex(variablesIndexManager, table, 0);
        }
        if (!synchroManager.isAlreadyQueued(valuesIndexManager, getValueTableIndex(datasource, table.getName()))) {
            synchroManager.synchronizeIndex(valuesIndexManager, table, 0);
        }
    }
}
