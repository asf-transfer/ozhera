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

package com.xiaomi.hera.trace.etl.service;

public interface RedisService {
    
    
    void init();
    
    Boolean sismember(String key, String member);
    
    Long sadd(String key, String... members);
    
    Long setNx(String key, String value);
    
    String get(String key);
    
    String set(String key, String value);
    
    String set(String key, String value, long ttl);
    
    Long del(String key);
    
    boolean getDisLock(String key);
}
