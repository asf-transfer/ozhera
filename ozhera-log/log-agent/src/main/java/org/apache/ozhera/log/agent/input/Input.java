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
package org.apache.ozhera.log.agent.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ozhera.log.api.enums.LogTypeEnum;

/**
 * @author shanwb
 * @date 2021-07-19
 */
@Data
@EqualsAndHashCode
public abstract class Input {
    /**
     * LogTypeEnum.name()，
     * You can decide whether to collect single lines or multiple lines based on this type (supports java exception stack)
     *
     * @see LogTypeEnum
     */
    private String type;

    /**
     * log path format
     * Currently supports single-file, multi-file, and directory-level wildcard modes
     * <p>
     * single file: /home/work/log/xxapp/server.log
     * multiple files: /home/work/log/neo-logs/(cxx01|cxx022)/server.log
     * Directory level wildcarding:/home/work/log/xxapp/ * /server.log
     */
    private String logPattern;

    /**
     *
     */
    private String patternCode;

    private String logSplitExpress;
    /**
     * User-defined regularity at the beginning of the line, the default is ""
     */
    private String linePrefix;

}
