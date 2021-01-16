
# Elasticsearch LibrAIry Ingest Processor

Integrate Artificial Inteligence in elasticsearch with LibrAIry

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-library/build/distribution/ingest-library-0.0.1-SNAPSHOT.zip
```

## Usage

Create a ingest pipeline with a librairy processor
```
PUT _ingest/pipeline/{name of the pipeline}
{
  "description": "An ingest pipeline for integrating LibrAIry in Elasticsearch",
  "processors": [
    {
      "library" : {
        "field" : "{field where the source of the document is}"
      }
    }
  ]
}

PUT /{index}/doc/{id}?pipeline={name-of-the-pipeline}
{
  "{field where the source of the document is}" : "{text of the document}"
}

GET /{index}/doc/{id}
{
  "{field where the source of the document is}" : "{text of the document}",
  "librairy": {
	  "model" : "{ model where the document was projected}",
	  "topics" : "{ topic hierarchy}"
  }
}
```

## Configuration

| Name | Required | Default|Description|
| --- | --- |--|--|
| field  | yes |-|Document text field
| includeVector | no |fase|Include topic vector
| model | yes | jrc-en | LibrAIry model to project documents
|modelDetection|false|false| Automaticly detect the model based on the languaje of the document





## Bugs & TODO

* Add exception  and failure handling.
* Add configuration to use local endpoints for LibrAIry models
