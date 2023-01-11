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

package com.ruoyi.video.domain;

/**
 * Udf Application Configuration
 * 
 */
public class UdfApplicationConfiguration {

    public UdfApplicationConfiguration(Integer imageVersion, Integer instanceNum) {
        this.imageVersion = imageVersion;
        this.instanceNum = instanceNum;
        this.flavor = new InstanceFlavor(InstanceFlavor.DEFAULT_INSTANCE_TYPE);
    }

    public UdfApplicationConfiguration(Integer imageVersion, Integer instanceNum, InstanceFlavor flavor) {
        this.imageVersion = imageVersion;
        this.instanceNum = instanceNum;
        this.flavor = flavor;
    }

    public Integer getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(Integer imageVersion) {
        this.imageVersion = imageVersion;
    }

    public Integer getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(Integer instanceNum) {
        this.instanceNum = instanceNum;
    }

    public InstanceFlavor getFlavor() {
        return flavor;
    }

    public void setFlavor(InstanceFlavor flavor) {
        this.flavor = flavor;
    }

    private Integer imageVersion;
    private Integer instanceNum;
    private InstanceFlavor flavor;
}
