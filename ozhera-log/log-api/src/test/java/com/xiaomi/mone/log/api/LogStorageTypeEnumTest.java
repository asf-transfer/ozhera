/*
 * Copyright (C) 2020 Xiaomi Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.log.api;

import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:35
 */
@Slf4j
public class LogStorageTypeEnumTest {

    @Test
    public void queryByNameTest() {
        String name = "doris";
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(name);
        Assert.assertNotNull(storageTypeEnum);
        log.info("result:{}", storageTypeEnum);
    }
}
