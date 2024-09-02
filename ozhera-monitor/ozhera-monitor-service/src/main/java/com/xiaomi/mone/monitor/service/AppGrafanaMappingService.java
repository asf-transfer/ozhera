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

package com.xiaomi.mone.monitor.service;

import com.xiaomi.mone.app.api.model.HeraAppBaseInfoModel;
import com.xiaomi.mone.monitor.dao.model.AppGrafanaMapping;
import com.xiaomi.mone.monitor.result.Result;

/**
 * @author gaoxihui
 * @date 2021/7/8 11:05 PM
 */

public interface AppGrafanaMappingService {
    
    
    void exeReloadTemplateBase(Integer pSize);
    
    void reloadTmpByAppId(Integer id);
    
    void createTmpByAppBaseInfo(HeraAppBaseInfoModel baseInfo);
    
    String createGrafanaUrlByAppName(String appName, String area);
    
    Result getGrafanaUrlByAppName(String appName);
    
    Result<String> getGrafanaUrlByAppId(Integer appId);
    
    Integer save(AppGrafanaMapping appGrafanaMapping);
    
    Integer saveOrUpdate(AppGrafanaMapping appGrafanaMapping);
    
    
}
