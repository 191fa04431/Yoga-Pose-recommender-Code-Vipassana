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

import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import com.google.cloud.Timestamp;
import com.google.api.core.ApiFuture;

import com.google.cloud.Date;
import org.springframework.core.ParameterizedTypeReference;
import java.net.URI;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.RequestEntity;
import org.springframework.http.HttpMethod;

import org.json.JSONObject;  
import org.json.JSONArray;  
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import com.google.auth.oauth2.GoogleCredentials;



import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

import com.google.cloud.firestore.VectorQuery;
import com.google.cloud.firestore.VectorQueryOptions;
import com.google.cloud.firestore.VectorQuerySnapshot;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import com.google.cloud.texttospeech.v1.*;  // Import TTS libraries
import java.util.Base64;

/*
 Controller class for invoking Server REST APIs 
 Example controller demonstrating usage of Spring Data Repositories for Firestore. 
*/

@Controller
class Posecontroller {
private static final String get_pose_by_id = "https://firestore.googleapis.com/v1beta1/";
private static final String param_url = "projects/<<YOUR_PROJECT_ID>>/databases/(default)/documents/poses/";
String project = "<<YOUR_PROJECT_ID>>";
String location = "us-central1";
private static final String queryString = "{\"structuredQuery\":{\"select\":{\"fields\":[{\"fieldPath\":\"name\"}]},\"from\":[{\"collectionId\":\"poses\"}]}}";
private static final String runQuery = "projects/<<YOUR_PROJECT_ID>>/databases/(default)/documents:runQuery";
RestTemplate restTemplate = new RestTemplate();


  /*
        Method that is invoked on search, to return the searchpose HTML page 
  */
    @GetMapping("/search")
    public String searchForm(Pose pose) {
        return "searchpose";
    }

    @GetMapping("/createpose")
    public String createForm(Pose pose) {
        return "createpose";
    }

    @GetMapping("/context_search")
    public String contextForm(Pose pose) {
        return "contextsearch";
    }
    
    /*
        Method to invoke API that retrieves a specific pose
    */
    @GetMapping("/showpose")
    public String callPoseByIdAPI(Pose pose){
        try{
        String nameString = pose.getName().toLowerCase();
        String idParam = param_url + nameString;
        String paramString = get_pose_by_id + idParam;
        System.out.println("res: " + paramString);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> resultString = restTemplate.exchange(paramString, HttpMethod.GET, entity, String.class);
        String result = resultString.toString();
       // System.out.println("RESULTTTTTTT: " + result);
        JSONObject jsonObject = new JSONObject(result.substring(5));
        JSONObject breath = jsonObject.getJSONObject("fields").getJSONObject("breath");
        JSONObject posture = jsonObject.getJSONObject("fields").getJSONObject("posture");
           
        pose.setName(nameString);
        pose.setBreath(breath.get("stringValue").toString());
        pose.setPosture(posture.get("stringValue").toString());
        
        return "showmessage";
        }catch(Exception e){
            System.out.println("Exception in Search Request: " + e);
            return "searchpose";
        }
    }

/*
    Method to invoke API that retrieves a specific pose
*/


@GetMapping("/matchpose")
public String callPoseByIdContext(Pose pose){
    try{
    String nameString = pose.getPosture().toString();
    //System.out.println("res: " + GenerateEmbeddings.predictTextEmbeddings("pose for spine correction"));
    List<Double> queryVector = GenerateEmbeddings.predictTextEmbeddings(nameString);
    double[] doubleArray = queryVector.stream()
    .mapToDouble(Double::doubleValue)
    .toArray(); // Directly collect into double[]
    FirestoreOptions firestoreOptions =
    FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("<<YOUR_PROJECT_ID>>")
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build();
    Firestore firestore = firestoreOptions.getService();

    CollectionReference coll = firestore.collection("poses");
    VectorQuery vectorQuery = coll.findNearest(
        "embedding",
        doubleArray, 
        /* limit */ 3,
        VectorQuery.DistanceMeasure.EUCLIDEAN,
        VectorQueryOptions.newBuilder().setDistanceResultField("vector_distance")
         .setDistanceThreshold(2.0)
          .build());

          ApiFuture<VectorQuerySnapshot> future = vectorQuery.get();
          VectorQuerySnapshot vectorQuerySnapshot = future.get();
          List<Pose> posesList = new ArrayList<Pose>();

          if (!vectorQuerySnapshot.isEmpty()) {
            List<DocumentSnapshot> sortedDocuments = vectorQuerySnapshot.getDocuments().stream()
            .sorted(Comparator.comparingDouble(doc -> doc.getDouble("vector_distance")))
            .collect(Collectors.toList());
            // Get the ID of the closest document (assuming results are sorted by distance)
            String closestDocumentId = vectorQuerySnapshot.getDocuments().get(0).getId();
            System.out.println("closestDocumentId: " + closestDocumentId);
            for (DocumentSnapshot document : sortedDocuments) {
                System.out.println(document.getId() + " Distance: " + document.get("vector_distance"));
                String poseName = document.getId().toString();
                String idParam = param_url + poseName;
                String paramString = get_pose_by_id + idParam;
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
                ResponseEntity<String> resultString = restTemplate.exchange(paramString, HttpMethod.GET, entity, String.class);
                String result = resultString.toString();
                JSONObject jsonObject = new JSONObject(result.substring(5));
                JSONObject breath = jsonObject.getJSONObject("fields").getJSONObject("breath");
                JSONObject posture = jsonObject.getJSONObject("fields").getJSONObject("posture");
                Pose poseTemp = new Pose();
                poseTemp.setName(poseName);
                poseTemp.setBreath(breath.get("stringValue").toString());
                poseTemp.setPosture(posture.get("stringValue").toString());
                try{
                    GenerateImageSample imageGen = new GenerateImageSample();
                    String prompt = "Generate a cartoon performing the following Yoga pose: " + posture.get("stringValue").toString();
                    String base64 = imageGen.generateImage(project, location, prompt);
                    poseTemp.setBase64("data:image/jpg;base64,"  + base64);
                }catch(Exception e){
                    System.out.println("No image generated!");
                    poseTemp.setBase64(null);
                }
                try{
                    String base64Audio = generateAudio(posture.get("stringValue").toString());
                    poseTemp.setBase64Audio("data:audio/mp3;base64," + base64Audio);
                } catch(Exception e){
                    System.out.println("No audio generated!");
                    poseTemp.setBase64Audio(null);
                }
                System.out.println("POSE: " + poseTemp.getName() + " : " + poseTemp.getBreath() + " : " + poseTemp.getPosture());
                posesList.add(poseTemp);
            }
            pose.setPosesList(posesList);
            System.out.println("Poses List: " + posesList.size()); } else {
            System.out.println("vectorQuerySnapshot.getDocuments().size() NOT PRESENT");
        }
    return "contextsearch";
    }catch(Exception e){
        System.out.println("Exception in Search Request: " + e);
        return "index";
    }
}

public String generateAudio(String postureString){
        try {
            // Create a Text-to-Speech client
            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
                // Set the text input to be synthesized
                SynthesisInput input = SynthesisInput.newBuilder().setText(postureString).build();

                // Build the voice request, select the language code ("en-US") and the ssml
                // voice gender
                // ("neutral")
                VoiceSelectionParams voice =
                        VoiceSelectionParams.newBuilder()
                                .setLanguageCode("en-US")
                                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                                .build();

                // Select the type of audio file you want returned
                AudioConfig audioConfig =
                        AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

                // Perform the text-to-speech request on the text input with the selected voice
                // parameters and audio file type
                SynthesizeSpeechResponse response =
                        textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                // Get the audio contents from the response
                ByteString audioContents = response.getAudioContent();

                // Convert to Base64 string
                String base64Audio = Base64.getEncoder().encodeToString(audioContents.toByteArray());

                // Add the Base64 encoded audio to the Pose object
               return base64Audio;
            }

        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately. For a real app, log and provide user feedback.
            return "Error in Audio Generation";
        }
}

@PostMapping("/editpose")
public String callPatch(Pose pose) {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(1000000);
    requestFactory.setReadTimeout(1000000);
    restTemplate.setRequestFactory(requestFactory);
    Gson gson = new Gson();
   try{
    String nameString = pose.getName();
    nameString = nameString.toLowerCase();
    String idParam = param_url + nameString;
    pose.setUrlname(idParam);
    String paramString = get_pose_by_id + idParam;
    String jsonString = pose.toString();
    //jsonString = gson.toJson(jsonString);
    System.out.println("JSON: " + jsonString);
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
    ResponseEntity<String> result = restTemplate.exchange(paramString, HttpMethod.PATCH, entity, String.class);
    return "showmessage";
    } catch(Exception e){
        System.out.println("EXCEPTION in edit" + e);
        return "searchpose";
    }
    
}


@PostMapping("/callgemini")
public String callGemini(Pose pose) {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(1000000);
    requestFactory.setReadTimeout(1000000);
    restTemplate.setRequestFactory(requestFactory);
   try{
    String nameString = pose.getName();
    String breathString = pose.getBreath();
    //nameString = nameString.toLowerCase();
    System.out.println("Generated response nameString: " + nameString);
    String prompt = "Tell me posture details about " + nameString + " with details like instructions, breathing pace (inhale or exhale or both), tummy position, spine position, upper back, lower back position, feet position, legs position, hands and palms position, neck and head position etc.  Your response should be 4 lines of text only and the last line should include the benefit of this pose with respect to wellness and human body.";
    String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=[YOUR_API_KEY]";
    Map<String, Object> requestBody = new HashMap<>();
    List<Map<String, Object>> contents = new ArrayList<>();
    List<Map<String, Object>> tools = new ArrayList<>();
    Map<String, Object> content = new HashMap<>();
    List<Map<String, Object>> parts = new ArrayList<>();
    Map<String, Object> part = new HashMap<>();
    part.put("text", prompt);
    parts.add(part);
    content.put("parts", parts);
    contents.add(content);
    requestBody.put("contents", contents);

    Map<String, Object> googleSearchTool = new HashMap<>();
    googleSearchTool.put("googleSearch", new HashMap<>());
    tools.add(googleSearchTool);
    requestBody.put("tools", tools);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
    System.out.println("Generated response: " + response);
    String responseBody = response.getBody();
    JSONObject jsonObject = new JSONObject(responseBody);
    JSONArray candidates = jsonObject.getJSONArray("candidates");
    JSONObject candidate = candidates.getJSONObject(0);
    JSONObject contentResponse = candidate.getJSONObject("content");
    JSONArray partsResponse = contentResponse.getJSONArray("parts");
    JSONObject partResponse = partsResponse.getJSONObject(0);
    String generatedText = partResponse.getString("text");
    System.out.println("Generated Text: " + generatedText);

    

    List<String> poseList = new ArrayList<String>();
    List<String> urltitleList = new ArrayList<String>();
try{
    JSONObject groundingResponse = candidate.getJSONObject("groundingMetadata");
    JSONArray chunks = groundingResponse.getJSONArray("groundingChunks");
    //get the object for all itens in the chunks JSONArray
    for (int i = 0; i < chunks.length(); i++) {
        JSONObject chunk = chunks.getJSONObject(i);
        JSONObject web = chunk.getJSONObject("web");
        String uri = web.getString("uri");
        String title = web.getString("title");
        System.out.println("URI " + i + ": " + uri);
        poseList.add(uri);
        urltitleList.add(title);
    }
}catch(Exception e){
  poseList = null;
  urltitleList = null;
}
    pose.setName(nameString);
    pose.setBreath(breathString);
    pose.setPosture(generatedText);
    pose.setPoseList(poseList);
    pose.setUrltitleList(urltitleList);
   return "showmessage";
    
}catch(Exception e){
        System.out.println("EXCEPTION in generate with Gemini 2.0 Flash" + e);
        return "showmessage";
    } 
   //return "showmessage";
}

@PostMapping("/callgeminicreate")
public String callGeminiCreate(Pose pose) {
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(1000000);
    requestFactory.setReadTimeout(1000000);
    restTemplate.setRequestFactory(requestFactory);
   try{
    String nameString = pose.getName();
    String breathString = pose.getBreath();
    //nameString = nameString.toLowerCase();
    System.out.println("Generated response nameString: " + nameString);
    String prompt = "Tell me posture details about " + nameString + " with details like instructions, breathing pace (inhale or exhale or both), tummy position, spine position, upper back, lower back position, feet position, legs position, hands and palms position, neck and head position etc.  Your response should be 4 lines of text only and the last line should include the benefit of this pose with respect to wellness and human body.";
    String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=[YOUR_API_KEY]";
    System.out.println("GEMINI: " + apiUrl);
    Map<String, Object> requestBody = new HashMap<>();
    List<Map<String, Object>> contents = new ArrayList<>();
    List<Map<String, Object>> tools = new ArrayList<>();
    Map<String, Object> content = new HashMap<>();
    List<Map<String, Object>> parts = new ArrayList<>();
    Map<String, Object> part = new HashMap<>();
    part.put("text", prompt);
    parts.add(part);
    content.put("parts", parts);
    //content.put("role", user); //vertex
    contents.add(content);
    requestBody.put("contents", contents);

    Map<String, Object> googleSearchTool = new HashMap<>();
    googleSearchTool.put("googleSearch", new HashMap<>());
    tools.add(googleSearchTool);
    requestBody.put("tools", tools);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
    System.out.println("Generated response: " + response);
    String responseBody = response.getBody();
    JSONObject jsonObject = new JSONObject(responseBody);
    JSONArray candidates = jsonObject.getJSONArray("candidates");
    JSONObject candidate = candidates.getJSONObject(0);
    JSONObject contentResponse = candidate.getJSONObject("content");
    JSONArray partsResponse = contentResponse.getJSONArray("parts");
    JSONObject partResponse = partsResponse.getJSONObject(0);
    String generatedText = partResponse.getString("text");
    generatedText = generatedText.replaceAll("[^a-zA-Z0-9 ,.;-_+!#$&()*?@\\s]", "").replaceAll("\\s+", " ");

    System.out.println("Generated Text: " + generatedText);

    

    List<String> poseList = new ArrayList<String>();
    List<String> urltitleList = new ArrayList<String>();
    try{
    JSONObject groundingResponse = candidate.getJSONObject("groundingMetadata");
    JSONArray chunks = groundingResponse.getJSONArray("groundingChunks");
     
    //get the object for all itens in the chunks JSONArray
    for (int i = 0; i < chunks.length(); i++) {
        JSONObject chunk = chunks.getJSONObject(i);
        JSONObject web = chunk.getJSONObject("web");
        String uri = web.getString("uri");
        String title = web.getString("title");
        poseList.add(uri);
        urltitleList.add(title);
        System.out.println("URI TITLE::::: " + i + ": " + uri + ": " + title);
    }
}catch(Exception e){
  poseList = null;
  urltitleList = null;
}
    pose.setName(nameString);
    pose.setBreath(breathString);
    pose.setPosture(generatedText);
    pose.setPoseList(poseList);
    pose.setUrltitleList(urltitleList);
   return "createpose";
    
}catch(Exception e){
        System.out.println("EXCEPTION in generate with Gemini 2.0 Flash" + e);
        return "createpose";
    } 
   //return "showmessage";
}

@PostMapping("/deletepose")
private String removePoseByName(Pose pose) {
    restTemplate = new RestTemplate();
    try{
    String nameString = pose.getName();
    String idParam = param_url + nameString;
    pose.setUrlname(idParam);
    String paramString = get_pose_by_id + idParam;
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    //HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> resultString = restTemplate.exchange(paramString, HttpMethod.DELETE, entity, String.class);
    String result = resultString.toString();
    System.out.println("RESULT: " + result);
    }catch(Exception e){
        System.out.println("EXCEPTION in delete" + e);
        return "searchpose";
    }
   return "searchpose";
}
/*
    Method that is invoked on home page, to return the HomePage HTML page 
*/
@GetMapping("/home")
public String homeForm(Pose pose) {
    return "pose";
}

/*
    Method that is invoked on home page, to return the HomePage HTML page 
*/
@PostMapping("/run")
public String runForm(Pose pose) {
    try{
        String paramString = get_pose_by_id + runQuery;
        //String jsonString = queryString;
        String jsonString = pose.getQuery();
        System.out.println("paramString: " + paramString);
        System.out.println("JSON: " + jsonString);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
        ResponseEntity<String> result = restTemplate.exchange(paramString, HttpMethod.POST, entity, String.class);
        System.out.println(result);
        pose.setResponse(result.toString());
        return "ryoq";
        }catch(Exception e){
            System.out.println("EXCEPTION in edit" + e);
            return "errmessage";
        }
}


  @GetMapping("/run_your_own_query")
  private String runQueryForm(Pose pose) {
    return "ryoq";
  }


}
