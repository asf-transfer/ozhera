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
package org.apache.ozhera.monitor.bo;

import lombok.Data;
import lombok.ToString;

/**
 * 
 * @author zhanggaofeng1
 */
@ToString
@Data
public class RulePromQLTemplateParam {

    private int id;
    private String name;
    private String promql;
    private Integer type;
    private String remark;
    private String creater;
    private Integer status;
    private boolean paging;
    private int page;
    private int pageSize;

    public void pageQryInit() {
        if (!paging) {
            page = 1;
            pageSize = 50;
        } else {
            if (page <= 0) {
                page = 1;
            }
            if (pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize >= 100) {
                pageSize = 100;
            }
        }
    }

}
