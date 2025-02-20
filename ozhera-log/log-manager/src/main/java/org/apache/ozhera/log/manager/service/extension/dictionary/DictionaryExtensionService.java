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
package org.apache.ozhera.log.manager.service.extension.dictionary;

import org.apache.ozhera.log.manager.model.dto.DictionaryDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/12 10:35
 */
public interface DictionaryExtensionService {

    String DEFAULT_DICTIONARY_EXTENSION_SERVICE_KEY = "defaultDictionaryExtensionService";

    List<DictionaryDTO<?>> queryMiddlewareConfigDictionary(String monitorRoomEn);

    List<DictionaryDTO<?>> queryResourceDictionary();

    List<DictionaryDTO<?>> queryAppType();

    List<MilogLogTailDo> querySpecialTails();

    List<DictionaryDTO<?>> queryMachineRegion();

    List<DictionaryDTO<?>> queryDeployWay();

    List<DictionaryDTO<?>> queryResourceTypeDictionary();

    List<DictionaryDTO> queryExistsTopic(String ak, String sk, String nameServer, String serviceUrl,
                                         String authorization, String orgId, String teamId);

    List<DictionaryDTO<?>> queryMQDictionary();
}
