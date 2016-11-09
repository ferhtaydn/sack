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
    file=/tmp/products.csv
    topic=product-csv-raw

$ ./bin/kafka-topics --list --zookeeper localhost:2181

// RawToRawProcessor.scala

$ ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw --from-beginning

// not required, auto create when RawProductConsumerBoot works
// $ ./bin/kafka-topics --zookeeper localhost:2181 --create --topic product-csv-raw-uppercase --partitions 1 --replication-factor 1

$ ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw-uppercase --from-beginning

$ sbt runMain RawToRawProcessorBoot


// RawToAvroGenericProcessor.scala

$ sbt runMain com.ferhtaydn.sack.avro.RawToAvroGenericProcessorBoot

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


CASSANDRA SINK - Complete workflow
// alternatif to combine both connect in one use.

$ ~/workspace/datamountaineer/stream-reactor/kafka-connect-cassandra/build/libs(branch:master) » export CLASSPATH=kafka-connect-cassandra-0.2-3.0.1-all.jar
$ ~/workspace/datamountaineer/stream-reactor/kafka-connect-cassandra/build/libs(branch:master) » ~/workspace/confluent/confluent-3.0.1/bin/connect-distributed /tmp/connect-distributed.properties
$ ~/workspace/datamountaineer/kafka-connect-tools/build/libs(branch:master) » java -jar kafka-connect-cli-0.6-all.jar create product-csv-source < /tmp/connect-file-source.properties

$ ~/workspace/confluent/confluent-3.0.1 » ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw --from-beginning

$ apache-cassandra-3.9 » ./bin/cqlsh
$ CREATE KEYSPACE demo WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};
$ use demo;
$ cqlsh:demo> create table products (barcode varchar, brand varchar, supplierId int, productType int, gender int, category int, imageUrl varchar, PRIMARY KEY (barcode));

$ cat cassandra-sink-distributed-products.properties 

    name=cassandra-sink-products
    connector.class=com.datamountaineer.streamreactor.connect.cassandra.sink.CassandraSinkConnector
    tasks.max=1
    topics=product-csv-avro,product-http-avro
    connect.cassandra.export.route.query=INSERT INTO products SELECT * FROM product-csv-avro;INSERT INTO products SELECT * FROM product-http-avro
    connect.cassandra.contact.points=localhost
    connect.cassandra.port=9042
    connect.cassandra.key.space=demo
    connect.cassandra.username=cassandra
    connect.cassandra.password=cassandra


$ ~/workspace/datamountaineer/kafka-connect-tools/build/libs(branch:master) » java -jar kafka-connect-cli-0.6-all.jar create cassandra-sink-products < /tmp/cassandra-sink-distributed-products.properties

$ sbt runMain com.ferhtaydn.sack.cassandra.RawToAvroGenericProcessorBoot

$ ~/workspace/confluent/confluent-3.0.1 » ./bin/kafka-avro-console-consumer --zookeeper localhost:2181 --topic product-csv-avro --from-beginning

$ cqlsh:demo> select * from products;

```
