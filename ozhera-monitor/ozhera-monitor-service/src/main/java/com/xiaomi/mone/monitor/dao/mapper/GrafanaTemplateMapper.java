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
package com.xiaomi.mone.monitor.dao.mapper;

import com.xiaomi.mone.monitor.dao.model.GrafanaTemplate;
import com.xiaomi.mone.monitor.dao.model.GrafanaTemplateExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GrafanaTemplateMapper {
    long countByExample(GrafanaTemplateExample example);

    int deleteByExample(GrafanaTemplateExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(GrafanaTemplate record);

    int insertSelective(GrafanaTemplate record);

    List<GrafanaTemplate> selectByExampleWithBLOBs(GrafanaTemplateExample example);

    List<GrafanaTemplate> selectByExample(GrafanaTemplateExample example);

    GrafanaTemplate selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") GrafanaTemplate record, @Param("example") GrafanaTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") GrafanaTemplate record, @Param("example") GrafanaTemplateExample example);

    int updateByExample(@Param("record") GrafanaTemplate record, @Param("example") GrafanaTemplateExample example);

    int updateByPrimaryKeySelective(GrafanaTemplate record);

    int updateByPrimaryKeyWithBLOBs(GrafanaTemplate record);

    int updateByPrimaryKey(GrafanaTemplate record);

    int batchInsert(@Param("list") List<GrafanaTemplate> list);

    int batchInsertSelective(@Param("list") List<GrafanaTemplate> list, @Param("selective") GrafanaTemplate.Column ... selective);
}