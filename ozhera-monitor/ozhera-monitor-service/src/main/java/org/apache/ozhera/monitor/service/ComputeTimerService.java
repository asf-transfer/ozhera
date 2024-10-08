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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.AppMonitorRequest;
import org.apache.ozhera.monitor.service.model.ProjectInfo;
import org.apache.ozhera.monitor.service.model.redis.AppAlarmData;

import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/8/17 10:08 AM
 */
public interface ComputeTimerService {
    
    
    void destory();
    
    Result<List<AppAlarmData>> getProjectStatistics(AppMonitorRequest param);
    
    /**
     * @param project
     * @param startTime
     * @param endTime
     * @param timeDuration
     * @param param
     */
    AppAlarmData getAppAlarmData(ProjectInfo project, Long startTime, Long endTime, String timeDuration, Long step,
            AppMonitorRequest param);
    
    
    AppAlarmData countAppMetricData(AppMonitorRequest param);
    
}
