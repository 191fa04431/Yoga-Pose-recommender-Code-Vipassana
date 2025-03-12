package com.example.demo;

/* Copyright 2025 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Example POJO to demonstrate Spring Cloud GCP Spring Data Firestore operations. */

public class Pose {
private String id;
private String name;
private String urlname;
private String breath;
private String  posture;
private String query;
private String response;
private List<String> poseList;
private List<String> urltitleList;
private List<Pose> posesList;
private String base64;
private String base64Audio;

public String getBase64Audio() {
    return base64Audio;
}
public void setBase64Audio(String base64Audio) {
    this.base64Audio = base64Audio;
}
public String getBase64() {
    return base64;
}
public void setBase64(String base64) {
    this.base64 = base64;
}


public List<Pose> getPosesList() {
    return posesList;
}
public void setPosesList(List<Pose> posesList) {
    this.posesList = posesList;
}
public List<String> getUrltitleList() {
    return urltitleList;
}
public void setUrltitleList(List<String> urltitleList) {
    this.urltitleList = urltitleList;
}
public List<String> getPoseList() {
    return poseList;
}
public void setPoseList(List<String> poseList) {
    this.poseList = poseList;
}
public String getResponse() {
    return response;
}
public void setResponse(String response) {
    this.response = response;
}

public String getQuery() {
    return query;
}
public void setQuery(String query) {
    this.query = query;
}

public String getId() {
    return id;
}
public void setId(String id) {
    this.id = id;
}

public String getUrlname() {
    return urlname;
}
public void setUrlname(String urlname) {
    this.urlname = urlname;
}

public String getName() {
    return name;
}
public void setName(String name) {
    this.name = name;
}
public String getBreath() {
    return breath;
}
public void setBreath(String breath) {
    this.breath = breath;
}
public String getPosture() {
    return posture;
}
public void setPosture(String posture) {
    this.posture = posture;
}
  @Override
  public String toString() {
    return "{'name':'" + urlname + "','fields':{'name':{'stringValue':'" + name + "'},'breath':{'stringValue':'" + breath + "'},'posture':{'stringValue':'" + posture + "'}}}";
  } 
}
