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

import java.util.Map;

public class SetTaggingRequest extends GenericRequest {

    protected TagSet tagSet = null;

    public SetTaggingRequest(String bucketName, String key) {
        super(bucketName, key);
        this.tagSet = new TagSet();
    }

    public SetTaggingRequest(String bucketName, String key, Map<String, String> tags) {
        super(bucketName, key);
        this.tagSet = new TagSet(tags);
    }

    public SetTaggingRequest(String bucketName, String key, TagSet tagSet) {
        super(bucketName, key);
        this.tagSet = tagSet;
    }

    public void setTag(String key, String value) {
        this.tagSet.setTag(key, value);
    }

    public String getTag(String key) {
        return this.tagSet.getTag(key);
    }

    public TagSet getTagSet() {
        return tagSet;
    }

    public void setTagSet(TagSet tagSet) {
        this.tagSet = tagSet;
    }

}
