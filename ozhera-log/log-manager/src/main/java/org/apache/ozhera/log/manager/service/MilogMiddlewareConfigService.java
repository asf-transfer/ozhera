/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.log.api.model.bo.MiLogResource;
import org.apache.ozhera.log.api.model.bo.ResourcePage;
import org.apache.ozhera.log.api.model.vo.ResourceInfo;
import org.apache.ozhera.log.api.model.vo.ResourceUserSimple;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.bo.MiddlewareAddParam;
import org.apache.ozhera.log.manager.model.bo.MiddlewareQueryParam;
import org.apache.ozhera.log.manager.model.bo.MiddlewareUpdateParam;
import org.apache.ozhera.log.manager.model.page.PageInfo;
import org.apache.ozhera.log.manager.model.pojo.MilogMiddlewareConfig;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/22 11:41
 */
public interface MilogMiddlewareConfigService {
    PageInfo<MilogMiddlewareConfig> queryMiddlewareConfigPage(MiddlewareQueryParam middlewareQueryParam);

    Result addMiddlewareConfig(MiddlewareAddParam middlewareQueryParam);

    Result updateMiddlewareConfig(MiddlewareUpdateParam middlewareAddParam);

    Result deleteMiddlewareConfig(Long id);

    List<MilogMiddlewareConfig> queryMiddlewareConfigList();

    Result<MilogMiddlewareConfig> queryMiddlewareConfigById(Long id);

    PageInfo<ResourceInfo> queryResourceWithTab(ResourcePage resourcePage);

    Result<String> resourceOperate(MiLogResource miLogResource);

    String synchronousResourceLabel(Long id);

    ResourceUserSimple userResourceList(String regionCode, Integer logTypeCode);

    ResourceInfo resourceDetail(Integer resourceCode, Long id);

    MilogMiddlewareConfig queryMiddlewareConfigDefault(String regionCode);
}
