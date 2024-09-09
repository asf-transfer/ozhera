/*
 *  Copyright (C) 2020 Xiaomi Corporation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaomi.mone.monitor.service.impl;

import com.google.gson.Gson;
import com.xiaomi.mone.monitor.service.AppGrafanaMappingService;
import com.xiaomi.mone.monitor.service.GrafanaApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gaoxihui
 * @date 2021/7/10 5:23 PM
 */
@Slf4j
@Service(registry = "registryConfig",interfaceClass = GrafanaApiService.class, retries = 0,group = "${dubbo.group}")
public class GrafanaServiceImpl implements GrafanaApiService {

    @Autowired
    AppGrafanaMappingService appGrafanaMappingService;

    @Override
    public String getUrlByAppName(String appName) {
        log.info("Dubbo.GrafanaServiceImpl.getUrlByAppName param appName : {}" ,appName);

        String result = new Gson().toJson(appGrafanaMappingService.getGrafanaUrlByAppName(appName));
        log.info("Dubbo.GrafanaServiceImpl.getUrlByAppName param appName : {} ,return result : {}" ,appName,result);
        return result;
    }

    @Override
    public String createGrafanaUrlByAppName(String appName,String area) {
        log.info("Dubbo.GrafanaServiceImpl.createGrafanaUrlByAppName param appName : {}" ,appName);

        String result = appGrafanaMappingService.createGrafanaUrlByAppName(appName,area);
        log.info("Dubbo.GrafanaServiceImpl.createGrafanaUrlByAppName param appName : {} ,return result : {}" ,appName,result);
        return result;
    }
}
