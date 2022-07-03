# spring-boot-elk

The example spring boot project demonstrates the use of [Elasticsearch Java API client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/usage.html). to index and search documents.

## Getting started

1. Spin a Elasticsearch docker container if you do not installation already
<pre> docker run --name es01 -p 9200:9200 -p 9300:9300 -it docker.elastic.co/elasticsearch/elasticsearch:8.3.1 </pre>

2. Configure the properties in application.properties with the host, port, username and the password.
<pre>
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.username=elastic
elasticsearch.password=elk123
</pre>

3. Run `mvn clean spring-boot:run` to start the application
