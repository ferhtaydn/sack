SACK - SMACK without Mesos

This project is a kind of PoC to develop simple flows with 
(eventually) Spark, Akka, Cassandra, and Kafka. 

It uses confluent.io version of Kafka.

```
$ cd confluent-3.0.1

$ ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties

$ ./bin/kafka-server-start ./etc/kafka/server.properties

$ ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties

$ ./bin/connect-standalone ./etc/schema-registry/connect-avro-standalone.properties /tmp/connect-file-source.properties

$ cat /tmp/connect-file-source.properties
    
    name=product-csv-source
    connector.class=FileStreamSource
    tasks.max=1
    file=/tmp/trendyol-product.csv
    topic=product-csv-raw

$ ./bin/kafka-topics --list --zookeeper localhost:2181

// RawToRawProcessor.scala

$ ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw --from-beginning

// not required, auto create when RawProductConsumerBoot works
// $ ./bin/kafka-topics --zookeeper localhost:2181 --create --topic product-csv-raw-uppercase --partitions 1 --replication-factor 1

$ ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw-uppercase --from-beginning

$ sbt runMain RawToRawProcessorBoot


// RawToAvroGenericProcessor.scala

// NOT REQUIRED, the topic is auto created with schema registration
$ ./bin/kafka-topics --zookeeper localhost:2181 --create --topic product-csv-avro --partitions 1 --replication-factor 1

// NOT REQUIRED, the topic is auto created with schema registration
$  POST http://localhost:8081/subjects/product-csv-avro-key/versions
   
   {
     "schema": "{\"type\": \"string\"}"
   }

$  POST http://localhost:8081/subjects/product-csv-avro-value/versions
   
   {
   	"schema": "{\"type\":\"record\",\"name\":\"Product\",\"namespace\":\"com.ferhtaydn.sack.model\",\"fields\":[{\"name\":\"brand\",\"type\":\"string\"},{\"name\":\"supplierId\",\"type\":\"int\"},{\"name\":\"productType\",\"type\":\"int\"},{\"name\":\"gender\",\"type\":\"int\"},{\"name\":\"category\",\"type\":\"int\"},{\"name\":\"imageUrl\",\"type\":\"string\"}]}"
   }

$ ./bin/kafka-avro-console-consumer --zookeeper localhost:2181 --topic product-csv-avro --from-beginning
```
