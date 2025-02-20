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
package org.apache.ozhera.log.utils;

import org.apache.ozhera.log.common.NetUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shanwb
 * @date 2021-08-16
 */
public class NetUtil {

    private static final String HERA_K8S_ENV = "hera_buildin_k8s";
    private static final String HERA_IP_ENV = "host_ip";

    public static String getLocalIp() {
        if (StringUtils.isNotEmpty(System.getenv(HERA_IP_ENV))) {
            return System.getenv(HERA_IP_ENV);
        }
        String localIp = System.getenv("host.ip") == null ? NetUtils.getLocalHost() : System.getenv("host.ip");
        return localIp;
    }

    public static String getHeraK8sEnv() {
        String envStatus = null == System.getenv(HERA_K8S_ENV) ? System.getProperty(HERA_K8S_ENV) : System.getenv(HERA_K8S_ENV);
        if (StringUtils.isEmpty(envStatus)) {
            return System.getenv(HERA_K8S_ENV.toUpperCase());
        }
        return envStatus;
    }

}
