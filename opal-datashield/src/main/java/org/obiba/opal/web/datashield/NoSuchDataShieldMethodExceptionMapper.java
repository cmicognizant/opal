/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.datashield.NoSuchDataShieldMethodException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchDataShieldMethodExceptionMapper implements ExceptionMapper<NoSuchDataShieldMethodException> {

  @Override
  public Response toResponse(NoSuchDataShieldMethodException exception) {
    return Response.status(Status.NOT_FOUND).build();
  }

}
