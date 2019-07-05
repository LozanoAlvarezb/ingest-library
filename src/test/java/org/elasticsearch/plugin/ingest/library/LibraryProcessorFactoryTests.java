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

import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class LibraryProcessorFactoryTests extends ESTestCase {

    private LibraryProcessor.Factory factory = new LibraryProcessor.Factory();

    public void testBuildDefaults() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "_field");
        String processorTag = randomAlphaOfLength(10);

        LibraryProcessor processor = factory.create(null ,processorTag,config);

        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getField(), equalTo("_field"));
        assertThat(processor.getTargetField(), equalTo("library"));
        assertThat(processor.getModel(), equalTo("jrc-en-model"));
        assertFalse(processor.getIncludeVector());
    }

    public void testBuildTargetField() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "_field");
        config.put("target_field", "_target_field");

        LibraryProcessor processor = factory.create(null, null, config);

        assertThat(processor.getField(), equalTo("_field"));
        assertThat(processor.getTargetField(), equalTo("_target_field"));
        assertFalse(processor.getIncludeVector());
    }

    public void testIncludeVector() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "_field");
        config.put("includeVector", true);


        LibraryProcessor processor = factory.create(null, null, config);
        assertThat(processor.getField(), equalTo("_field"));
        assertThat(processor.getTargetField(), equalTo("library"));
        assertTrue(processor.getIncludeVector());
    }

    public void testModel() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "_field");
        config.put("model", "_model");


        LibraryProcessor processor = factory.create(null, null, config);
        assertThat(processor.getField(), equalTo("_field"));
        assertThat(processor.getModel(), equalTo("_model"));
        assertFalse(processor.getIncludeVector());
    }

    public void testModelDetection() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "_field");
        config.put("modelDetection", true);


        LibraryProcessor processor = factory.create(null, null, config);
        assertThat(processor.getField(), equalTo("_field"));
        assertTrue(processor.getmodelDetection());
    }


}
