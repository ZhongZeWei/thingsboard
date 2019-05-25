/**
 * Copyright © 2016-2019 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.service.install.InstallScripts;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

/**
 * 租客管理
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class TenantController extends BaseController {

    @Autowired
    private InstallScripts installScripts;

    @Autowired
    private TenantService tenantService;

    /**
     * 根据id查询
     * @param strTenantId
     * @return
     * @throws ThingsboardException
     */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/{tenantId}", method = RequestMethod.GET)
    @ResponseBody
    public Tenant getTenantById(@PathVariable("tenantId") String strTenantId) throws ThingsboardException {
        //检查是否为空
        checkParameter("tenantId", strTenantId);
        try {
            //把查询结果封装到实体类
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            //这里要检查，其实已经查了一次
            checkTenantId(tenantId, Operation.READ);
            //有重复查询了一次
            return checkNotNull(tenantService.findTenantById(tenantId));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * 增加租客
     * @param tenant
     * @return
     * @throws ThingsboardException
     */
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenant", method = RequestMethod.POST)
    @ResponseBody
    public Tenant saveTenant(@RequestBody Tenant tenant) throws ThingsboardException {
        try {
            //判断tenant是否有id
            boolean newTenant = tenant.getId() == null;
            //确认是新建操作
            Operation operation = newTenant ? Operation.CREATE : Operation.WRITE;
            //授权
            accessControlService.checkPermission(getCurrentUser(), Resource.TENANT, operation,
                    tenant.getId(), tenant);
            //保存用户
            tenant = checkNotNull(tenantService.saveTenant(tenant));
            if (newTenant) {

                installScripts.createDefaultRuleChains(tenant.getId());
            }
            return tenant;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * 删除
     * @param strTenantId
     * @throws ThingsboardException
     */
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenant/{tenantId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@PathVariable("tenantId") String strTenantId) throws ThingsboardException {
        //检查是否为空
        checkParameter("tenantId", strTenantId);
        try {
            //把条件封装到实体类
            TenantId tenantId = new TenantId(toUUID(strTenantId));
            //检查是否有这个租客
            checkTenantId(tenantId, Operation.DELETE);
            //删除用户
            tenantService.deleteTenant(tenantId);
            //权限检查
            actorService.onEntityStateChange(tenantId, tenantId, ComponentLifecycleEvent.DELETED);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    /**
     * api/tenants?limit=30&textSearch=
     * @param limit
     * @param textSearch
     * @param idOffset
     * @param textOffset
     * @return
     * @throws ThingsboardException
     */
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/tenants", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Tenant> getTenants(@RequestParam int limit,
                                           @RequestParam(required = false) String textSearch,
                                           @RequestParam(required = false) String idOffset,
                                           @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            //页面查询条件
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            //检查结果
            return checkNotNull(tenantService.findTenants(pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
