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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.gson.Gson;
import com.xiaomi.data.push.client.HttpClientV5;
import com.xiaomi.mone.monitor.DashboardConstant;
import com.xiaomi.mone.monitor.bo.*;
import com.xiaomi.mone.monitor.dao.GrafanaTemplateDao;
import com.xiaomi.mone.monitor.dao.model.GrafanaTemplate;
import com.xiaomi.mone.monitor.result.ErrorCode;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.prometheus.JobService;
import com.xiaomi.mone.monitor.utils.FreeMarkerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * @author zhangxiaowei6
 * @date 2023-02-22
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class HeraDashboardService {
    private Gson gson = new Gson();

    @Autowired
    GrafanaTemplateDao grafanaTemplateDao;
    @Autowired
    JobService jobService;

    @Value("${nacos.config.addrs}")
    private String nacosAddress;

    @NacosValue(value = "${grafana.address}", autoRefreshed = true)
    public String grafanaUrl;

    @NacosValue(value = "${grafana.domain}", autoRefreshed = true)
    public String grafanaDomain;
    @NacosValue(value = "${grafana.apikey.url}", autoRefreshed = true)
    public String grafanaApiKeyUrl;

    @NacosValue(value = "${grafana.datasource.url}", autoRefreshed = true)
    public String grafanaDatasourceUrl;
    @NacosValue(value = "${grafana.folder.url}", autoRefreshed = true)
    public String grafanaFolderUrl;

    @NacosValue(value = "${grafana.jaeger.query.token}", autoRefreshed = true)
    public String jaegerQueryToken;

    @NacosValue(value = "${grafana.createDashboard.url}", autoRefreshed = true)
    public String grafanaDashboardUrl;

    public static final String HERA_GRAFANA_TEMPLATE = "/heraGrafanaTemplate";
    public static final String HERA_SCRAPE_JOB_TEMPLATE = "/heraScrapeJobTemplate";

    @Value("${server.type}")
    private String serverType;

    @NacosValue(value = "${prometheus.url}", autoRefreshed = true)
    private String prometheusUrl;

    @NacosValue(value = "${grafana.username}", autoRefreshed = true)
    private String grafanaUserName;

    @NacosValue(value = "${grafana.password}", autoRefreshed = true)
    private String grafanaPassword;

    @PostConstruct
    public void init() {
        try {
            log.info("begin createDefaultGrafanaResource");
            createDefaultScrapeJob();
            createDefaultDashboardTemplate();
            DashboardDTO dataSourceDTO = new DashboardDTO();
            if (StringUtils.isBlank(dataSourceDTO.getPrometheusDatasource())) {
                dataSourceDTO.setPrometheusDatasource(prometheusUrl);
            }
            if (StringUtils.isBlank(dataSourceDTO.getUsername())) {
                dataSourceDTO.setUsername(grafanaUserName);
            }
            if (StringUtils.isBlank(dataSourceDTO.getPassword())) {
                dataSourceDTO.setPassword(grafanaPassword);
            }
            if (StringUtils.isBlank(dataSourceDTO.getDashboardFolderName())) {
                dataSourceDTO.setDashboardFolderName(DashboardConstant.DEFAULT_FOLDER_NAME);
            }
            Result dashboard = createGrafanaResources(dataSourceDTO);
        } catch (Exception e) {
            log.error("GrafanaInitController init error:", e);
            throw new RuntimeException("GrafanaInitController init error");
        }
    }

    public Result createGrafanaResources(DashboardDTO dashboardDTO) {
        log.info("HeraDashboardService.createGrafanaResources param:{}", gson.toJson(dashboardDTO));
        //base64 username & password
        String base64Str = dashboardDTO.getUsername() + ":" + dashboardDTO.getPassword();
        String basicAuth = Base64.getEncoder().encodeToString(base64Str.getBytes());
        //request api key
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Basic " + basicAuth);
        String apiKey = createApiKey(header);
        if (apiKey == null || apiKey.isEmpty()) {
            return Result.fail(ErrorCode.API_KEY_CREATE_FAIL);
        }
        log.info("createGrafanaResources.apikey:{}", apiKey);
        //by api key to create prometheus dataSource
        header.put("Authorization", "Bearer " + apiKey);
        String datasourceUid = createDataSource(header, dashboardDTO.getPrometheusDatasource());
        if (datasourceUid == null || datasourceUid.isEmpty()) {
            return Result.fail(ErrorCode.DATASOURCE_CREATE_FAIL);
        }
        log.info("createGrafanaResources.datasourceUid:{}", datasourceUid);
        //by api key to create hera folder
        int folderId = createFolder(header, dashboardDTO.getDashboardFolderName());
        if (folderId == -1) {
            return Result.fail(ErrorCode.FOLDER_CREATE_FAIL);
        }
        //grafana template  replace template variable，request grafana generate dashboard and panel.
        createDefaultGrafanaDashboard(datasourceUid, header);

        //Write the url returned by the panel back to mimonitor's nacos configuration
        try {
            ConfigService configService = NacosFactory.createConfigService(nacosAddress);
            String nacosResult = configService.getConfig(DashboardConstant.DEFAULT_MIMONITOR_NACOS_CONFIG,
                    DashboardConstant.DEFAULT_MIMONITOR_NACOS_GROUP, 5000);
            Properties props = new Properties();
            props.load(new StringReader(nacosResult));
            props.setProperty("grafana.api.key", apiKey);
            props.setProperty("grafana.prometheus.datasource", DashboardConstant.GRAFANA_DATASOURCE_NAME);
            props.setProperty("grafana.folder.uid", DashboardConstant.GRAFANA_FOLDER_UID);
            props.setProperty("prometheusUid", datasourceUid);

            StringWriter writer = new StringWriter();
            props.store(writer, "after replace!");
            String finalNacosConfig = writer.getBuffer().toString();
            //request nacos cover config
            log.info("createGrafanaResources.before overlays nacos config:{}", finalNacosConfig);
            boolean postResult = configService.publishConfig(DashboardConstant.DEFAULT_MIMONITOR_NACOS_CONFIG,
                    DashboardConstant.DEFAULT_MIMONITOR_NACOS_GROUP, finalNacosConfig);
            if (!postResult) {
                log.error("createGrafanaResources.create nacos config failed:{}", postResult);
            } else {
                log.info("createGrafanaResources.create nacos config success");
            }
        } catch (IOException | NacosException e) {
            log.error("createGrafanaResources.request nacos error:{}", e.getMessage());
        }
        return Result.success("success");
    }

    //create biz、docker、node、serviceMarket、interfaceMarket etc...
    public void createDefaultDashboardTemplate() {
        //Only the first initialization is created using freeMarker. First check if the templates already exist in the database.
        // If so, do not create them again
        DashboardConstant.GRAFANA_SRE_TEMPLATES.forEach(
                name -> {
                    GrafanaTemplate grafanaTemplate = grafanaTemplateDao.fetchOneByName(name);
                    if (grafanaTemplate == null) {
                        //If it has not been created, it is created from the ftl file
                        try {
                            insertDashboardTemplate(name);
                        } catch (IOException e) {
                            log.error("HeraDashboardService.createDefaultDashboardTemplate error :{}", e.getMessage());
                        }
                    } else {
                        // If it already exists, delete it from db first, then fetch the latest one from the template and create a new one
                        int resCount = grafanaTemplateDao.deleteHard(grafanaTemplate.getId());
                        if (resCount >= 1) {
                            try {
                                insertDashboardTemplate(name);
                            } catch (IOException e) {
                                log.error("HeraDashboardService.createDefaultDashboardTemplate Multiple create error :{}", e.getMessage());
                            }
                        }
                    }
                });

        //create java biz template
        GrafanaTemplate grafanaTemplate = grafanaTemplateDao.fetchOneByName("hera-java模板");
        if (grafanaTemplate == null) {
            try {
                String content = FreeMarkerUtil.getTemplateStr(HERA_GRAFANA_TEMPLATE, DashboardConstant.JAEGER_QUERY_File_NAME);
                GrafanaTemplate template = new GrafanaTemplate();
                template.setName("hera-java模板");
                template.setCreateTime(new Date());
                template.setUpdateTime(new Date());
                template.setLanguage(0);
                template.setPlatform(0);
                template.setAppType(0);
                template.setTemplate(content);
                template.setDeleted(false);
                template.setPanelIdList(DashboardConstant.DEFAULT_PANEL_ID_LIST);
                int insertRes = grafanaTemplateDao.insert(template);
                log.info("HeraDashboardService.createDefaultDashboardTemplate name:{},insertRes:{}", "hera-java模板", insertRes);
            } catch (IOException e) {
                log.error("HeraDashboardService.createDefaultDashboardTemplate java template error :{}", e.getMessage());
            }
        }

        //create golang biz template
        GrafanaTemplate grafanaGoTemplate = grafanaTemplateDao.fetchOneByName("hera-golang模板");
        if (grafanaGoTemplate == null) {
            try {
                String content = FreeMarkerUtil.getTemplateStr(HERA_GRAFANA_TEMPLATE, DashboardConstant.GOLANG_File_NAME);
                GrafanaTemplate template = new GrafanaTemplate();
                template.setName("hera-golang模板");
                template.setCreateTime(new Date());
                template.setUpdateTime(new Date());
                template.setLanguage(1);
                template.setPlatform(0);
                template.setAppType(0);
                template.setTemplate(content);
                template.setDeleted(false);
                template.setPanelIdList(DashboardConstant.DEFAULT_GOLANG_ID_LIST);
                int insertRes = grafanaTemplateDao.insert(template);
                log.info("HeraDashboardService.createDefaultDashboardTemplate name:{},insertRes:{}", "hera-golang模板", insertRes);
            } catch (IOException e) {
                log.error("HeraDashboardService.createDefaultDashboardTemplate golang template error :{}", e.getMessage());
            }
        }

    }

    private void insertDashboardTemplate(String name) throws IOException {
        String content = FreeMarkerUtil.getTemplateStr(HERA_GRAFANA_TEMPLATE, name + ".ftl");
        GrafanaTemplate template = new GrafanaTemplate();
        template.setName(name);
        template.setCreateTime(new Date());
        template.setUpdateTime(new Date());
        template.setLanguage(0);
        template.setPlatform(0);
        template.setAppType(1);
        template.setTemplate(content);
        template.setDeleted(false);
        int insertRes = grafanaTemplateDao.insert(template);
        log.info("HeraDashboardService.insertDashboardTemplate name:{},insertRes:{}", name, insertRes);
    }

    //request prometheus-agent create biz、docker、node、jvm ...etc prometheus job
    public void createDefaultScrapeJob() {
        //Get jobJson from a file
        Map<String, Object> jaegerQueryMap = new HashMap<>();
        jaegerQueryMap.put("token", jaegerQueryToken);
        try {
            //create jaeger_query monitor
            Result jaegerResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_JAEGER_QUERY_JOB_NAME);
            log.info("jaegerResult:{}", jaegerResult);
            if (jaegerResult.getData().equals("null")) {
                log.info("jaeger_query job begin create");
                String jaegerQueryJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "jaegerQueryScrapeJob.ftl", jaegerQueryMap);
                Result jaegerQueryJobRes = jobService.createJob(null, "Hera", jaegerQueryJobJson, "初始化创建业务监控");
                log.info("HeraDashboardService.createDefaultScrapeJob jaeger_query res: {}", jaegerQueryJobRes.getData());
            }

            //create jvm monitor
            Result jvmResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_JVM_JOB_NAME);
            if (jvmResult.getData().equals("null")) {
                log.info("jvm job begin create");
                String jvmJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "jvmScrapeJob.ftl", new HashMap<>());
                Result jvmJobJsonRes = jobService.createJob(null, "Hera", jvmJobJson, "初始化创建jvm监控");
                log.info("HeraDashboardService.createDefaultScrapeJob jvm res: {}", jvmJobJsonRes.getData());
            }

            //create docker monitor
            Result dockerResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_DOCKER_JOB_NAME);
            if (dockerResult.getData().equals("null")) {
                log.info("docker job begin create");
                String dockerJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "dockerScrapeJob.ftl", new HashMap<>());
                Result dockerJobJsonRes = jobService.createJob(null, "Hera", dockerJobJson, "初始化创建容器监控");
                log.info("HeraDashboardService.createDefaultScrapeJob docker res: {}", dockerJobJsonRes.getData());
            }

            //create node monitor
            Result nodeResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_NODE_JOB_NAME);
            if (nodeResult.getData().equals("null")) {
                log.info("node job begin create");
                String nodeJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "nodeScrapeJob.ftl", new HashMap<>());
                Result nodeJobJsonRes = jobService.createJob(null, "Hera", nodeJobJson, "初始化创建物理机监控");
                log.info("HeraDashboardService.createDefaultScrapeJob node res: {}", nodeJobJsonRes.getData());
            }
            //create custom monitor
            Result customizeResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_CUSTOMIZE_JOB_NAME);
            if (customizeResult.getData().equals("null")) {
                log.info("customize job begin create");
                String customizeJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "customizeScrapeJob.ftl", new HashMap<>());
                Result customizeJobJsonRes = jobService.createJob(null, "Hera", customizeJobJson, "初始化创建自定义监控");
                log.info("HeraDashboardService.createDefaultScrapeJob customize res: {}", customizeJobJsonRes.getData());
            }
            //create golang runtime monitor
            Result golangRuntimeResult = jobService.searchJobByName(null, "hera", DashboardConstant.DEFAULT_GOLANG_RUNTIME_JOB_NAME);
            if (golangRuntimeResult.getData().equals("null")) {
                log.info("golangRuntimeResult job begin create");
                String golangRuntimeJobJson = FreeMarkerUtil.getContent(HERA_SCRAPE_JOB_TEMPLATE, "golangRuntimeScrapeJob.ftl", new HashMap<>());
                Result golangRuntimeJobJsonRes = jobService.createJob(null, "Hera", golangRuntimeJobJson, "初始化创建golang Runtime监控");
                log.info("HeraDashboardService.createDefaultScrapeJob golang runtime res: {}", golangRuntimeJobJsonRes.getData());
            }
        } catch (Exception e) {
            log.error("HeraDashboardService.createDefaultScrapeJob error :{}", e.getMessage());
        }

    }

    private String createApiKey(Map<String, String> header) {
        String apiKeyName = DashboardConstant.GRAFANA_API_KEY_NAME + "-" + System.currentTimeMillis();
        GrafanaApiKeyReq req = new GrafanaApiKeyReq(apiKeyName, DashboardConstant.GRAFANA_API_KEY_ROLE);
        log.info("GrafanaApiKeyReq:{}", gson.toJson(req));
        try {
            String grafanaApiKeyResStr = HttpClientV5.post(grafanaUrl + grafanaApiKeyUrl, gson.toJson(req), header);
            log.info("HeraDashboardService.createApiKey request apikey res:{}", grafanaApiKeyResStr);
            GrafanaApiKeyRes grafanaApiKeyRes = gson.fromJson(grafanaApiKeyResStr, GrafanaApiKeyRes.class);
            return grafanaApiKeyRes.getKey();
        } catch (Exception e) {
            log.error("HeraDashboardService.createApiKey error :{}", e.getMessage());
            return null;
        }
    }

    private String createDataSource(Map<String, String> header, String prometheusDatasourceUrl) {
        GrafanaCreateDataSourceReq req = new GrafanaCreateDataSourceReq();
        req.setName(DashboardConstant.GRAFANA_DATASOURCE_NAME);
        req.setType(DashboardConstant.GRAFANA_DATASOURCE_TYPE);
        req.setAccess("proxy");
        req.setBasicAuth(false);
        req.setUrl(prometheusDatasourceUrl);
        log.info("GrafanaCreateDataSourceReq:{}", gson.toJson(req));
        try {
            //If yes, no creation is required
            String getDatasourceRes = HttpClientV5.get(grafanaUrl + grafanaDatasourceUrl + "/name/" +
                    DashboardConstant.GRAFANA_DATASOURCE_NAME, header);
            log.info("HeraDashboardService.createDataSource getDatasourceRes:{}", getDatasourceRes);
            GrafanaGetDataSourceRes grafanaGetDataSourceRes = gson.fromJson(getDatasourceRes, GrafanaGetDataSourceRes.class);
            if (grafanaGetDataSourceRes.getUid() != null) {
                return grafanaGetDataSourceRes.getUid();
            }
            String grafanaDatasourceResStr = HttpClientV5.post(grafanaUrl + grafanaDatasourceUrl, gson.toJson(req), header);
            log.info("HeraDashboardService.createDataSource request res:{}", grafanaDatasourceResStr);
            GrafanaCreateDataSourceRes grafanaCreateDataSourceRes = gson.fromJson(grafanaDatasourceResStr, GrafanaCreateDataSourceRes.class);
            return grafanaCreateDataSourceRes.getDatasource().getUid();
        } catch (Exception e) {
            log.error("HeraDashboardService.createDataSource error :{}", e.getMessage());
            return null;
        }
    }

    private int createFolder(Map<String, String> header, String folderTitle) {
        GrafanaFolderReq req = new GrafanaFolderReq();
        req.setTitle(folderTitle);
        req.setUid(DashboardConstant.GRAFANA_FOLDER_UID);
        log.info("GrafanaCreateFolderReq:{}", gson.toJson(req));
        try {
            String getFolderRes = HttpClientV5.get(grafanaUrl + grafanaFolderUrl + "/" + DashboardConstant.GRAFANA_FOLDER_UID, header);
            log.info("HeraDashboardService.createFolder getFolderRes:{}", getFolderRes);
            GrafanaGetFolderRes grafanaGetFolderRes = gson.fromJson(getFolderRes, GrafanaGetFolderRes.class);
            if (grafanaGetFolderRes.getId() != null) {
                return grafanaGetFolderRes.getId();
            }
            String grafanaFolderResStr = HttpClientV5.post(grafanaUrl + grafanaFolderUrl, gson.toJson(req), header);
            log.info("HeraDashboardService.createFolder request res:{}", grafanaFolderResStr);
            GrafanaFolderRes grafanaFolderRes = gson.fromJson(grafanaFolderResStr, GrafanaFolderRes.class);
            return grafanaFolderRes.getId();
        } catch (Exception e) {
            log.error("HeraDashboardService.createFolder error :{}", e.getMessage());
            return -1;
        }
    }

    private void createDefaultGrafanaDashboard(String prometheusUid, Map<String, String> header) {
        Map<String, Object> map = new HashMap<>();
        map.put("prometheusUid", prometheusUid);
        map.put("serviceMarketUrl", grafanaDomain + "/d/${__data.fields.application.text}/ye-wu-jian-kong-" +
                "${__data.fields.application.text}?orgId=1&refresh=30s&theme=light");
        map.put("query0", "${query0}");
        map.put("env", serverType);
        map.put("serviceName", "hera");

        DashboardConstant.GRAFANA_SRE_TEMPLATES.forEach(
                name -> {
                    GrafanaTemplate grafanaTemplate = grafanaTemplateDao.fetchOneByName(name);
                    try {
                        if (grafanaTemplate != null) {
                            String template = FreeMarkerUtil.freemarkerProcess(map, grafanaTemplate.getTemplate());
                            log.info("HeraDashboardService.createDefaultGrafanaDashboard name :{} begin!", name);
                            //request grafana
                            String grafanaDashboardResStr = HttpClientV5.post(grafanaUrl + grafanaDashboardUrl, template, header);
                            log.info("HeraDashboardService.createDefaultGrafanaDashboard request " + name + " template res:{}", grafanaDashboardResStr);
                            GrafanaCreateDashboardRes grafanaCreateDashboardRes = gson.fromJson(grafanaDashboardResStr, GrafanaCreateDashboardRes.class);
                            if (!grafanaCreateDashboardRes.getStatus().equals("success")) {
                                log.error("HeraDashboardService.createDefaultGrafanaDashboard name:{},status:{},message:{}",
                                        name, grafanaCreateDashboardRes.getStatus(), grafanaCreateDashboardRes.getMessage());
                            }
                        } else {
                            log.error("HeraDashboardService.createDefaultGrafanaDashboard " + name + " template fetch error!");
                        }
                    } catch (Exception e) {
                        log.error("HeraDashboardService.createDefaultGrafanaDashboard error:{}", e);
                    }
                });
    }
}
