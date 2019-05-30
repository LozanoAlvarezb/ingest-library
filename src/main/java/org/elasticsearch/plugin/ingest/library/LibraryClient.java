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

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.SuppressForbidden;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

final class LibraryClient {

    @SuppressForbidden(reason="Access PTM model hosted in remote server")
    static JSONObject projectDoc(String model,String doc) throws IOException,PrivilegedActionException {
        // check that its not unprivileged code like a script
        SpecialPermission.check();

        String endpoint = "http://librairy.linkeddata.es/"+model+"/inferences";
        StringBuilder content = new StringBuilder();
        JSONObject body = new JSONObject();
        body.put("text",doc);
        body.put("topics",true);

        return AccessController.doPrivileged((PrivilegedExceptionAction<JSONObject>)
                () -> {
                    try {
                        URL url = new URL(endpoint);
                        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                        httpConnection.setDoOutput(true);
                        httpConnection.setRequestMethod("POST");
                        httpConnection.setRequestProperty("Content-Type", "application/json");
                        httpConnection.setRequestProperty("Accept", "application/json");

                        DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
                        wr.write(body.toString().getBytes());
                        Integer responseCode = httpConnection.getResponseCode();

                        BufferedReader bufferedReader;

                        // Creates a reader buffer
                        if (responseCode > 199 && responseCode < 300) {
                            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                        } else {
                            bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                        }

                        // To receive the response

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return new JSONObject(content.toString());
                    }catch(Exception e){
                        Throwable cause = e.getCause();
                        if(cause instanceof PrivilegedActionException) {
                            throw (PrivilegedActionException) cause;
                        } else {
                            throw new AssertionError(cause);
                        }

                    }
                }
        );


    }
}
