/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityIdFactory;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.TimePageData;
import org.thingsboard.server.common.data.page.TimePageLink;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuditLogController extends BaseController {

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/audit/logs/customer/{customerId}", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TimePageData<AuditLog> getAuditLogsByCustomerId(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset,
            @RequestParam(name = "actionTypes", required = false) String actionTypesStr) throws ThingsboardException {
        try {
            checkParameter("CustomerId", strCustomerId);
            TenantId tenantId = getCurrentUser().getTenantId();
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            List<ActionType> actionTypes = parseActionTypesStr(actionTypesStr);
            return checkNotNull(auditLogService.findAuditLogsByTenantIdAndCustomerId(tenantId, new CustomerId(UUID.fromString(strCustomerId)), actionTypes, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/audit/logs/user/{userId}", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TimePageData<AuditLog> getAuditLogsByUserId(
            @PathVariable("userId") String strUserId,
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset,
            @RequestParam(name = "actionTypes", required = false) String actionTypesStr) throws ThingsboardException {
        try {
            checkParameter("UserId", strUserId);
            TenantId tenantId = getCurrentUser().getTenantId();
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            List<ActionType> actionTypes = parseActionTypesStr(actionTypesStr);
            return checkNotNull(auditLogService.findAuditLogsByTenantIdAndUserId(tenantId, new UserId(UUID.fromString(strUserId)), actionTypes, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/audit/logs/entity/{entityType}/{entityId}", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TimePageData<AuditLog> getAuditLogsByEntityId(
            @PathVariable("entityType") String strEntityType,
            @PathVariable("entityId") String strEntityId,
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset,
            @RequestParam(name = "actionTypes", required = false) String actionTypesStr) throws ThingsboardException {
        try {
            checkParameter("EntityId", strEntityId);
            checkParameter("EntityType", strEntityType);
            TenantId tenantId = getCurrentUser().getTenantId();
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            List<ActionType> actionTypes = parseActionTypesStr(actionTypesStr);
            return checkNotNull(auditLogService.findAuditLogsByTenantIdAndEntityId(tenantId, EntityIdFactory.getByTypeAndId(strEntityType, strEntityId), actionTypes, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/audit/logs", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TimePageData<AuditLog> getAuditLogs(
            @RequestParam int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "false") boolean ascOrder,
            @RequestParam(required = false) String offset,
            @RequestParam(name = "actionTypes", required = false) String actionTypesStr) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TimePageLink pageLink = createPageLink(limit, startTime, endTime, ascOrder, offset);
            List<ActionType> actionTypes = parseActionTypesStr(actionTypesStr);
            return checkNotNull(auditLogService.findAuditLogsByTenantId(tenantId, actionTypes, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private List<ActionType> parseActionTypesStr(String actionTypesStr) {
        List<ActionType> result = null;
        if (StringUtils.isNoneBlank(actionTypesStr)) {
            String[] tmp = actionTypesStr.split(",");
            result = Arrays.stream(tmp).map(at -> ActionType.valueOf(at.toUpperCase())).collect(Collectors.toList());
        }
        return result;
    }
}
