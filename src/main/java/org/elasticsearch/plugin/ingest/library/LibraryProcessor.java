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
//import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class LibraryProcessor extends AbstractProcessor {

    public static final String TYPE = "library";

    private final String field;
    private final String targetField;
    private final String model;
    private boolean includeVector;

    public LibraryProcessor(String tag, String field, String targetField, String model, boolean includeVector){
        super(tag);
        this.field = field;
        this.targetField = targetField;
        this.includeVector = includeVector;
        this.model = model;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        Map<String, Object> additionalFields = new HashMap<>();
        Map<String, String> topics = new HashMap<>();

        String doc = ingestDocument.getFieldValue(field,String.class);

        if(doc==null){
            ingestDocument.setFieldValue(targetField, "ERROR: field"+field+"not found in document");
            return ingestDocument;
        }

        JSONObject response = LibraryClient.projectDoc(model,doc);

//        Iterator<?> itr  = response.getJSONArray("topics").iterator();
//        while (itr.hasNext())
//        {
//            JSONObject topic = (JSONObject) itr.next();
//            topics.put(topic.getString("id"),topic.getString("description"));
//        }
//        additionalFields.put("topics",topics);
        additionalFields.put("topics", response.getJSONArray("topics").toList());

        if(includeVector){
            double[] vector = Arrays.stream(response.get("vector").toString().replaceAll("\\[|\\]","")
                    .split(",")).mapToDouble(Double::parseDouble)
                .toArray();
            additionalFields.put("vector",vector);
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

    public static final class Factory implements Processor.Factory {

        @Override
        public LibraryProcessor create(Map<String, Processor.Factory> factories, String tag, Map<String, Object> config) 
            throws Exception {
            String field = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field", "library");
            String model = readStringProperty(TYPE, tag, config, "model", "jrc-en-model/");
            boolean includeVector = readBooleanProperty(TYPE, tag, config, "ignore_missing", false);

            return new LibraryProcessor(tag, field, targetField, model, includeVector);
        }
    }
}
