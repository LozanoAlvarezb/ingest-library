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

import org.elasticsearch.cli.SuppressForbidden;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
//import org.elasticsearch.SpecialPermission;


//import java.net.URLConnection;
import java.util.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class LibraryProcessor extends AbstractProcessor {

    public static final String TYPE = "library";

    private final String field;
    private final String targetField;

    public LibraryProcessor(String tag, String field, String targetField) throws IOException {
        super(tag);
        this.field = field;
        this.targetField = targetField;
    }

    @Override
    @SuppressForbidden(reason = "Socket")
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {

        // Test URL
        String url = "http://127.0.0.1:5000/user/Nicholas";

        AccessController.doPrivileged((PrivilegedAction<Void>)
                () -> {
                    try {
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("GET");
                        if(con.getResponseCode()!=200){
                            return null;
                        }
                        else{
                            InputStream is = con.getInputStream();
                            BufferedReader in = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
                            StringBuilder response = new StringBuilder();

                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            //Read JSON response and print
                            JSONObject myResponse = new JSONObject(response.toString());
                            ingestDocument.setFieldValue(targetField, myResponse.getString("occupation"));
                            return null;
                        }
                    }catch(IOException ex){
                        return null;
                    }
                }
        );


        return ingestDocument;
    }


    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public LibraryProcessor create(Map<String, Processor.Factory> factories, String tag, Map<String, Object> config) 
            throws Exception {
            String field = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field", "default_field_name");

            return new LibraryProcessor(tag, field, targetField);
        }
    }
}
