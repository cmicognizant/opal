/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.system.subject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.security.Group;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.EntityValidationService;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Scope("request")
@Path("/system/subject-credential/{name}")
public class SubjectCredentialResource {

  @PathParam("name")
  private String name;

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @Autowired
  private EntityValidationService entityValidationService;

  @GET
  public Response get() {
    SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(name);
    return (subjectCredentials == null //
        ? Response.status(Response.Status.NOT_FOUND) //
        : Response.ok().entity(Dtos.asDto(subjectCredentials))).build();
  }

  @PUT
  public Response update(Opal.SubjectCredentialsDto dto) {
    if(!name.equals(dto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    if (getSubjectCredentials() == null) return Response.status(Response.Status.NOT_FOUND).build();

      SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
      validateGroups(subjectCredentials);

      switch(subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        if(dto.hasPassword() && !dto.getPassword().isEmpty()) {
          subjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
        }
        break;
      case CERTIFICATE:
        if(dto.hasCertificate() && !dto.getCertificate().isEmpty()) {
          subjectCredentials.setCertificate(dto.getCertificate().toByteArray());
        }
        break;
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }

  @DELETE
  public Response delete() {
    SubjectCredentials subjectCredentials = getSubjectCredentials();
    if(subjectCredentials == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    subjectCredentialsService.delete(subjectCredentials);
    return Response.ok().build();
  }

  private SubjectCredentials getSubjectCredentials() {
    return subjectCredentialsService.getSubjectCredentials(name);
  }

    private void validateGroups(SubjectCredentials user) {

        Set<String> groupNames =  user.getGroups();
        for (String groupName: groupNames) {
            Group group = new Group(groupName);
            entityValidationService.validate(group);
        }
    }

}