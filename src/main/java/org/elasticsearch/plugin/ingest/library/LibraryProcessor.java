/*
 * Copyright [2018] [Borja Lozano Álvarez]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.library;


import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class LibraryProcessor extends AbstractProcessor {

    public static final String TYPE = "library";

    private final String field;
    private final String targetField;
    private String model;
    private boolean includeVector;
    private boolean modelDetection;

    public LibraryProcessor(String tag, String field, String targetField, String model, boolean includeVector, boolean modelDetection){
        super(tag);
        this.field = field;
        this.targetField = targetField;
        this.includeVector = includeVector;
        this.modelDetection = modelDetection;
        this.model = model;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {

        String[] models = {"jrc-en-model","jrc-es-model"};

        if(modelDetection && ingestDocument.hasField("lang"))
        {
            switch (ingestDocument.getFieldValue("lang",String.class))
            {
                case "en":
                    model = "jrc-en-model";
                    break;

                case "es":
                    model = "jrc-es-model";
                    break;

                default:
                    break;
            }
        }

        else if(!Arrays.stream(models).anyMatch(model::equals)){
            ingestDocument.setFieldValue(targetField, "ERROR: unknown model "+model);
            return ingestDocument;
        }

        Map<String, Object> additionalFields = new HashMap<>();
        String topics_id = "";
        JSONObject topic;


        String doc = ingestDocument.getFieldValue(field,String.class);

        if(doc==null){
            ingestDocument.setFieldValue(targetField, "ERROR: field"+field+"not found in document");
            return ingestDocument;
        }

        additionalFields.put("model", model);

        JSONObject response = LibraryClient.projectDoc(model,doc);

        JSONArray topics = (JSONArray) response.get("topics");
        for (int i = 0; i < 3; i++) {
            topic = (JSONObject) topics.get(i);
            topics_id += topic.get("name").toString()+" ";
        }

        additionalFields.put("topics",topics_id.trim());
//        additionalFields.put("topics", response.getJSONArray("topics").toList());

        if(includeVector){
            double[] vector = Arrays.stream(response.get("vector").toString().replaceAll("\\[|\\]","")
                    .split(",")).mapToDouble(Double::parseDouble)
                .toArray();
            List<Double> vectorList = Arrays.stream(vector).boxed().collect(Collectors.toList());
            additionalFields.put("vector",vectorList);
        }

        ingestDocument.setFieldValue(targetField, additionalFields);

        return ingestDocument;
    }


    @Override
    public String getType() {
        return TYPE;
    }

    String getField() {
        return field;
    }

    String getTargetField() {
        return targetField;
    }

    String getModel() {
        return model;
    }

    boolean getIncludeVector() {
        return includeVector;
    }

    boolean getmodelDetection(){
        return modelDetection;
    }


    public static final class Factory implements Processor.Factory {

        @Override
        public LibraryProcessor create(Map<String, Processor.Factory> factories, String tag, Map<String, Object> config) 
            throws Exception {

            String field = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field", "library");
            String model = readStringProperty(TYPE, tag, config, "model", "jrc-en-model");
            boolean includeVector = readBooleanProperty(TYPE, tag, config, "includeVector", false);
            boolean modelDetection = readBooleanProperty(TYPE, tag, config, "modelDetection", false);


            return new LibraryProcessor(tag, field, targetField, model, includeVector, modelDetection);
        }
    }
}
