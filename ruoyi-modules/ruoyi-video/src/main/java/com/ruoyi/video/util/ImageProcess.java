/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.ruoyi.video.util;

public class ImageProcess {

    public ImageProcess(String compliedHost, Boolean sourceFileProtect, String sourceFileProtectSuffix,
            String styleDelimiters) {
        this.compliedHost = compliedHost;
        this.sourceFileProtect = sourceFileProtect;
        this.sourceFileProtectSuffix = sourceFileProtectSuffix;
        this.styleDelimiters = styleDelimiters;
        this.supportAtStyle = null;
    }

    public ImageProcess(String compliedHost, Boolean sourceFileProtect, String sourceFileProtectSuffix,
            String styleDelimiters, Boolean supportAtStyle) {
        this.compliedHost = compliedHost;
        this.sourceFileProtect = sourceFileProtect;
        this.sourceFileProtectSuffix = sourceFileProtectSuffix;
        this.styleDelimiters = styleDelimiters;
        this.supportAtStyle = supportAtStyle;
    }

    public String getCompliedHost() {
        return compliedHost;
    }

    public void setCompliedHost(String compliedHost) {
        this.compliedHost = compliedHost;
    }

    public Boolean isSourceFileProtect() {
        return sourceFileProtect;
    }

    public void setSourceFileProtect(Boolean sourceFileProtect) {
        this.sourceFileProtect = sourceFileProtect;
    }

    public String getSourceFileProtectSuffix() {
        return sourceFileProtectSuffix;
    }

    public void setSourceFileProtectSuffix(String sourceFileProtectSuffix) {
        this.sourceFileProtectSuffix = sourceFileProtectSuffix;
    }

    public String getStyleDelimiters() {
        return styleDelimiters;
    }

    public void setStyleDelimiters(String styleDelimiters) {
        this.styleDelimiters = styleDelimiters;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isSupportAtStyle() {
        return supportAtStyle;
    }

    public void setSupportAtStyle(Boolean supportAtStyle) {
        this.supportAtStyle = supportAtStyle;
    }

    // Img???????????????????????????????????????Img?????????Both??????oss?????????Img??????????????????
    private String compliedHost;
    // ????????????????????????
    private Boolean sourceFileProtect;
    // ????????????????????????*????????????
    private String sourceFileProtectSuffix;
    // ????????????????????????
    private String styleDelimiters;
    // ??????????????????????????????2???????????????????????????
    private Integer version;
    // ????????????????????????OSS???????????????????????????????????????@???????????????Disabled
    private Boolean supportAtStyle;

}
