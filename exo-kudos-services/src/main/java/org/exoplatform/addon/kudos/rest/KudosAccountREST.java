/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.kudos.rest;

import static org.exoplatform.addon.kudos.service.utils.Utils.getCurrentUserId;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.kudos.model.AccountSettings;
import org.exoplatform.addon.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to retrieve detailed information about
 * users and spaces
 */
@Path("/kudos/api/account")
@RolesAllowed("users")
public class KudosAccountREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(KudosAccountREST.class);

  private KudosService     kudosService;

  public KudosAccountREST(KudosService kudosService) {
    this.kudosService = kudosService;
  }

  /**
   * Retrieves the user settings for kudos
   * 
   * @return
   */
  @Path("settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getSettings() {
    try {
      AccountSettings accountDetail = kudosService.getAccountSettings(getCurrentUserId());
      if (accountDetail == null) {
        return Response.ok("{}").build();
      }
      return Response.ok(accountDetail).build();
    } catch (Exception e) {
      LOG.warn("Error getting kudos settings", e);
      return Response.serverError().build();
    }
  }

  /**
   * Chacks if username is authorized to use Kudos
   * 
   * @param username
   * @return
   */
  @Path("isAuthorized")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response isAuthorized(@QueryParam("username") String username) {
    try {
      if (kudosService.isAuthorized(username)) {
        return Response.ok().build();
      } else {
        return Response.status(403).build();
      }
    } catch (Exception e) {
      LOG.warn("Error getting kudos authorization for user {}", username, e);
      return Response.serverError().build();
    }
  }

}
