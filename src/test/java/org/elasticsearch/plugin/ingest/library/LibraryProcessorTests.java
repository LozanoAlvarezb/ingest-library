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

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;


public class LibraryProcessorTests extends ESTestCase {

    private LibraryProcessor processor;
    private String sampleText;

    @Before
    public void createStandardProcessor(){
     processor = new LibraryProcessor(randomAlphaOfLength(10),"Source_field","Target_field","jrc-en-model/",true);
     sampleText = "Just a test to se what rolls";
    }

    public void baseTest() throws Exception {
        Map<String, Object> libraryData = projectDoc(sampleText, processor);
        assertThat(libraryData.keySet(), containsInAnyOrder("vector","topics"));
        assertThat((Collection<?>) libraryData.get("topics"), hasSize(3));
    }

    private Map<String, Object> projectDoc(String doc, LibraryProcessor processor) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field",doc);
        document.put("includeVector",true);

        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(),document);
        processor.execute(ingestDocument);

        @SuppressWarnings("unchecked")
        Map<String, Object> libraryData = (Map<String, Object>) ingestDocument.getSourceAndMetadata().get("target_field");
        return libraryData;
    }
}

