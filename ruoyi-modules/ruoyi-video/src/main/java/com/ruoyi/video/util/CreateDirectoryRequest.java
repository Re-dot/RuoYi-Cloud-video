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

public class CreateDirectoryRequest extends GenericRequest {

    public CreateDirectoryRequest(String bucketName, String directoryName) {
        super(bucketName, directoryName);
    }

    public void setDirectoryName(String directoryName) {
        super.setKey(directoryName);
    }

    public String getDirectoryName() {
        return super.getKey();
    }
}