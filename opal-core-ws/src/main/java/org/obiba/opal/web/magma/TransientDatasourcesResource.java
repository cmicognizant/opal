/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/transient-datasources")
public class TransientDatasourcesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TransientDatasourcesResource.class);

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  public TransientDatasourcesResource(DatasourceFactoryRegistry datasourceFactoryRegistry) {
    if(datasourceFactoryRegistry == null) {
      throw new IllegalArgumentException("datasourceFactoryRegistry cannot be null");
    }
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
  }

  @POST
  public Response createDatasource(@Context final UriInfo uriInfo, Magma.DatasourceFactoryDto factoryDto) {
    String uid = null;
    ResponseBuilder response = null;
    try {
      DatasourceFactory factory = datasourceFactoryRegistry.parse(factoryDto);
      uid = MagmaEngine.get().addTransientDatasource(factory);
      Datasource ds = MagmaEngine.get().getTransientDatasourceInstance(uid);
      UriBuilder ub = UriBuilder.fromPath("/").path(DatasourceResource.class);
      response = Response.created(ub.build(uid)).entity(Dtos.asDto(ds).build());
      Disposables.silentlyDispose(ds);
    } catch(NoSuchDatasourceFactoryException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "UnidentifiedDatasourceFactory").build());
    } catch(DatasourceParsingException pe) {
      removeTransientDatasource(uid);
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DatasourceCreationFailed", pe).build());
    } catch(MagmaRuntimeException e) {
      removeTransientDatasource(uid);
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DatasourceCreationFailed", e).build());
    }

    return response.build();
  }

  private void removeTransientDatasource(String uid) {
    if(uid != null) {
      MagmaEngine.get().removeTransientDatasource(uid);
    }
  }

}
