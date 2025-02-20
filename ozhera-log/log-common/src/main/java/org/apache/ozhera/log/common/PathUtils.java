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
package org.apache.ozhera.log.common;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.SYMBOL_MULTI;

/**
 * @author milog
 */
@Slf4j
public class PathUtils {

    /**
     * Minimum number of directory hierarchies
     */
    private static final int MINIMUM_LEVELS = 3;
    /**
     * Directory wildcards
     */
    public static final String PATH_WILDCARD = "*";

    public static final String MULTI_FILE_PREFIX = "(";
    public static final String MULTI_FILE_SUFFIX = ")";
    private static final String NEO_FILE_PREFIX = "/home/work/logs/neo-logs/(";
    public static final String SPLIT_VERTICAL_LINE = "\\|";

//    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    public static final String SEPARATOR = "/";

    /**
     * Used to parse the penultimate directory wildcard character example: /home/work/log/xapp/ * /server.log
     *
     * @param origPath
     * @return
     */
    public static List<String> parseLevel5Directory(String origPath) {
        ArrayList<String> pathList = Lists.newArrayList();
        if (StringUtils.isEmpty(origPath)) {
            return pathList;
        }
        String[] pathArray = origPath.split(",");
        for (String path : pathArray) {
            path = pathTrim(path);
            String basePath = path.substring(0, path.lastIndexOf(SEPARATOR));
            String fileName = path.substring(path.lastIndexOf(SEPARATOR) + 1);
            String[] fileArray = path.split(SEPARATOR);
            if (fileArray.length < MINIMUM_LEVELS) {
                pathList.add(path);
            } else {
                String fixedBasePath = basePath.substring(0, basePath.lastIndexOf(SEPARATOR));
                String regexPattern = basePath.substring(basePath.lastIndexOf(SEPARATOR) + 1);
                if (StringUtils.startsWith(fixedBasePath, NEO_FILE_PREFIX)) {
                    regexPattern = MULTI_FILE_PREFIX + StringUtils.substringBetween(fixedBasePath, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX) + MULTI_FILE_SUFFIX;
                    fixedBasePath = NEO_FILE_PREFIX.substring(0, NEO_FILE_PREFIX.length() - 2);
                }
                // * Wildcard reads multi-level directories
                if (regexPattern.trim().equals(PATH_WILDCARD)) {
                    try {
                        readFile(fixedBasePath, fileName, pathList);
                    } catch (FileNotFoundException e) {
                        log.error("[PathUtils.ParseLevel5Directory] file[{}] not found err:", basePath + fileName, e);
                    } catch (Exception e) {
                        log.error("[PathUtils.ParseLevel5Directory] path:[{}],err:", basePath + fileName, e);
                    }
                } else if (basePath.contains(MULTI_FILE_PREFIX) && basePath.contains(MULTI_FILE_SUFFIX) && !fileName.contains(PATH_WILDCARD)) {
                    String multiDirectories = StringUtils.substringBetween(basePath, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX);
                    String directoryPrefix = StringUtils.substringBefore(basePath, MULTI_FILE_PREFIX);
                    String directorySuffix = StringUtils.substringAfter(basePath, MULTI_FILE_SUFFIX);
                    handleMultiDirectories(multiDirectories, fileName, directoryPrefix, directorySuffix, SEPARATOR, pathList);
                } else if (fileName.contains(PATH_WILDCARD)) {
                    //Matches many files
                    handleMultipleDirectoryFile(basePath, fileName, pathList);
                } else {
                    if (origPath.contains(PATH_WILDCARD)) {
                        basePath = StringUtils.substringBefore(origPath, PATH_WILDCARD);
                        String fileSuffix = StringUtils.substringAfter(origPath, PATH_WILDCARD);
                        try {
                            readFile(basePath, fileSuffix, pathList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                    for (String serverName : fileName.split(SPLIT_VERTICAL_LINE)) {
                        pathList.add(basePath + SEPARATOR + serverName);
                    }
                }
            }
        }
        return pathList;
    }

    public static List<String> buildMultipleDirectories(String multipleDire) {
        List<String> pathList = Lists.newArrayList();
        String[] multipleDts = multipleDire.split(",");
        for (String multipleDt : multipleDts) {
            if (multipleDt.contains(MULTI_FILE_PREFIX) && multipleDt.contains(MULTI_FILE_SUFFIX)) {
                String[] directoryArray = multipleDt.split(SEPARATOR);
                String directory = "";
                List<String> directories = Lists.newArrayList();
                for (String perDire : directoryArray) {
                    if (perDire.startsWith(MULTI_FILE_PREFIX) && perDire.endsWith(MULTI_FILE_SUFFIX)) {
                        directory = perDire;
                        directories = Arrays.asList(StringUtils.substringBetween(perDire, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX).split(SPLIT_VERTICAL_LINE));
                    }
                }
                if (StringUtils.isNotBlank(directory)) {
                    String prefix = StringUtils.substringBeforeLast(multipleDt, directory);
                    String suffix = StringUtils.substringAfter(multipleDt, directory);
                    for (String directoryTemp : directories) {
                        pathList.add(String.format("%s%s%s", prefix, directoryTemp, suffix));
                    }
                }
            } else {
                pathList.add(multipleDt);
            }
        }
        return pathList;
    }

    private static void handleMultipleDirectoryFile(String basePath, String fileNamePattern, List<String> pathList) {
        if (basePath.contains(MULTI_FILE_PREFIX) && basePath.contains(MULTI_FILE_SUFFIX)) {
            String multiDirectories = StringUtils.substringBetween(basePath, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX);
            String directoryPrefix = StringUtils.substringBefore(basePath, MULTI_FILE_PREFIX);
            String directorySuffix = StringUtils.substringAfter(basePath, MULTI_FILE_SUFFIX);
            for (String directory : multiDirectories.split(SPLIT_VERTICAL_LINE)) {
                String basePathSingle = directoryPrefix + directory + directorySuffix;
                pathList.addAll(findRulePatternFiles(basePathSingle, fileNamePattern));
            }
        } else {
            pathList.addAll(findRulePatternFiles(basePath, fileNamePattern));
        }

    }

    private static void handleMultiDirectories(String multiDirectories, String fileName, String directoryPrefix,
                                               String directorySuffix, String separator, List<String> pathList) {
        for (String directory : multiDirectories.split(SPLIT_VERTICAL_LINE)) {
            if (fileName.contains(MULTI_FILE_PREFIX) || fileName.contains(MULTI_FILE_SUFFIX)) {
                for (String singleFileName : StringUtils.substringBetween(fileName, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX).split(SPLIT_VERTICAL_LINE)) {
                    pathList.add(directoryPrefix + directory + directorySuffix + separator + singleFileName);
                }
            } else {
                for (String singleFileName : fileName.split(SPLIT_VERTICAL_LINE)) {
                    pathList.add(directoryPrefix + directory + directorySuffix + separator + singleFileName);
                }
            }
        }
    }

    /**
     * Return to the list of monitoring folders
     *
     * @param origPath
     * @return`
     */
    public static List<String> parseWatchDirectory(String origPath) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(origPath)) {
            return result;
        }
        String[] pathArray = origPath.split(",");
        for (String path : pathArray) {
            path = pathTrim(path);
            String basePath = path.substring(0, path.lastIndexOf(SEPARATOR));

            String fixedBasePath = basePath.substring(0, basePath.lastIndexOf(SEPARATOR));
            String regexPattern = basePath.substring(basePath.lastIndexOf(SEPARATOR) + 1);

            if (StringUtils.startsWith(fixedBasePath, NEO_FILE_PREFIX)) {
                regexPattern = MULTI_FILE_PREFIX + StringUtils.substringBetween(fixedBasePath, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX) + MULTI_FILE_SUFFIX;
                fixedBasePath = NEO_FILE_PREFIX.substring(0, NEO_FILE_PREFIX.length() - 2);
            }

            if (regexPattern.trim().equals(PATH_WILDCARD)) {
                result.add(fixedBasePath);
            } else if (regexPattern.startsWith(MULTI_FILE_PREFIX) && regexPattern.endsWith(MULTI_FILE_SUFFIX)) {
                String patterns = regexPattern.substring(1, regexPattern.length() - 1);
                String[] patternArr = patterns.split(SPLIT_VERTICAL_LINE);
                String originFilePrefix = fixedBasePath + SEPARATOR + regexPattern;
                //Adapt the remaining directories on the right side of the regular rule
                // /home/work/logs/neo-logs/(xxx|yy)/zz/server.log => add /zz
                String regexRightPath = path.substring(originFilePrefix.length() + 1);
                String rightDir = "";
                if (regexRightPath.split(SEPARATOR).length > 1) {
                    rightDir = regexRightPath.substring(0, regexRightPath.lastIndexOf(SEPARATOR));
                    rightDir = SEPARATOR + Arrays.stream(rightDir.split(SEPARATOR)).findFirst().get();
                }
                for (String p : patternArr) {
                    String watchPath = fixedBasePath + SEPARATOR + p + rightDir;
                    result.add(watchPath);
                }
            } else if (basePath.contains(MULTI_FILE_PREFIX) && basePath.contains(MULTI_FILE_SUFFIX)) {
                String multiDirectories = StringUtils.substringBetween(basePath, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX);
                String directoryPrefix = StringUtils.substringBefore(basePath, MULTI_FILE_PREFIX);
                String directorySuffix = StringUtils.substringAfter(basePath, MULTI_FILE_SUFFIX);
                for (String directory : multiDirectories.split(SPLIT_VERTICAL_LINE)) {
                    result.add(directoryPrefix + directory + directorySuffix);
                }
            } else {
                result.add(basePath);
            }
        }
        return result.stream().distinct().collect(Collectors.toList());
    }

    private static String pathTrim(String path) {
        path = path.replaceAll("//", "/");
        return path;
    }

    private static void readFile(String filepath, String fileName, List<String> list) throws FileNotFoundException, IOException {
        try {
            File file = new File(filepath);
            if (!file.isDirectory()) {
                return;
            } else if (file.isDirectory()) {
                String[] fileList = file.list();
                for (int i = 0; i < fileList.length; i++) {
                    String subPath;
                    if (filepath.endsWith("/")) {
                        subPath = filepath + fileList[i];
                    } else {
                        subPath = filepath + "/" + fileList[i];
                    }

                    File subFile = new File(subPath);
                    if (!subFile.isDirectory() && StringUtils.equals(subFile.getName(), fileName)) {
                        list.add(subFile.getPath());
                    } else if (subFile.isDirectory()) {
                        readFile(subPath, fileName, list);
                    }
                }

            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return;
    }

    private static void readFile(String filepath, String fileName, String dictionaries, List<String> list) throws FileNotFoundException, IOException {
        try {
            File file = new File(filepath);
            if (!file.isDirectory()) {
                return;
            } else if (file.isDirectory()) {
                String[] fileList = file.list();
                for (int i = 0; i < fileList.length; i++) {
                    String subPath;
                    if (filepath.endsWith("/")) {
                        subPath = filepath + fileList[i];
                    } else {
                        subPath = filepath + "/" + fileList[i];
                    }

                    File subFile = new File(subPath);
                    if (!subFile.isDirectory() && StringUtils.equals(subFile.getName(), fileName)) {
                        list.add(subFile.getPath());
                    } else if (subFile.isDirectory()) {
                        readFile(subPath, fileName, list);
                    }
                }

            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return;
    }


    public static List<String> findRulePatternFiles(String directory, String patternStr) {
        File file = new File(directory);
        if (!patternStr.contains(SYMBOL_MULTI)) {
            patternStr = patternStr.replaceAll("\\*", SYMBOL_MULTI);
        }
        if (patternStr.startsWith(PATH_WILDCARD)) {
            patternStr = patternStr.replaceFirst("\\*", SYMBOL_MULTI);
        }
        Pattern compile = Pattern.compile(patternStr);
        if (file.isDirectory()) {
            return Arrays.stream(file.list())
                    .filter(name -> compile.matcher(name).matches())
                    .map(s -> String.format("%s%s%s", directory, SEPARATOR, s))
                    .collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * /home/work/log/log-agent/server.log.*
     * /logSplitExpress:/home/work/log/log-agent/(server.log.*|error.log.*)
     * /logSplitExpress:/home/work/log/(log-agent|log-stream)/(a|b)/server.log.*
     * Clean the marked path
     *
     * @param originStr
     * @return
     */
    public static void dismantlingStrWithSymbol(String originStr, List<String> cleanedPathList) {
        if (StringUtils.isBlank(originStr)) {
            return;
        }
        String pathPrefix = StringUtils.substringBefore(originStr, MULTI_FILE_PREFIX);
        String betweenStr = StringUtils.substringBetween(originStr, MULTI_FILE_PREFIX, MULTI_FILE_SUFFIX);
        if (StringUtils.isBlank(betweenStr)) {
            cleanedPathList.add(originStr);
            return;
        }
        String pathSuffix = StringUtils.substringAfter(originStr, MULTI_FILE_SUFFIX);

        if (StringUtils.isNotBlank(betweenStr)) {
            String[] directories = StringUtils.split(betweenStr, SPLIT_VERTICAL_LINE);
            for (String perDirectory : directories) {
                String realPath = String.format("%s%s%s", pathPrefix, perDirectory, pathSuffix);
                dismantlingStrWithSymbol(realPath, cleanedPathList);
            }
        }
    }
}
