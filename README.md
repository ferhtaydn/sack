SACK - SMACK without Mesos

This project is a kind of PoC to develop simple flows with (eventually) Spark, Akka, Cassandra, and Kafka. 

confluent.io stack is used.

A step-by-step multi node installation manual can be found at this [guide](https://gist.github.com/ferhtaydn/1c803f28a414c75e5d5df365af11f9c7). 

There are multiple projects under the root.

- In `api` project, there are rest endpoints to take products inside. 

```
java -jar api/target/scala-2.11/api.jar
```

or

```
$ sbt project api
$ sbt runMain com.ferhtaydn.http.WebServer
```

> An http layer to post products to http topic that is also consumed by cassandra sink connector to the products table.
> For the large json products content, you can encode the content with Content-Transfer-Encoding header set to gzip.

- In `csv` project, a simple flow of messages processing is simulated. Consuming a topic, some validation, and producing to the another topic.

```
java -jar csv/target/scala-2.11/csv.jar
```

> simple csv file is consumed by Kafka FileStreamSource connector to a raw topic,
> each line is tried to be converted to a Product class,
> successful records are consumed by cassandra sink connector to the products table,
> invalid lines are stored in a failure topic to be able to investigate later

- In `examples` project, there are binary and avro formatted messages used with `scala-kafka-client`.
 
- Common code for all other projects is placed under the `core` project.

#### Step-by-step guide in local

```
$ cd confluent-3.0.1

$ ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties

$ ./bin/kafka-server-start ./etc/kafka/server.properties

$ ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties

$ cat /tmp/connect-file-source.properties
    
    name=product-csv-source
    connector.class=FileStreamSource
    tasks.max=1
    file=/tmp/products.csv
    topic=product-csv-raw

$ ./bin/kafka-topics --list --zookeeper localhost:2181

$ ./bin/kafka-avro-console-consumer --zookeeper localhost:2181 --topic product-csv-avro --from-beginning

$ ~/workspace/datamountaineer/stream-reactor/kafka-connect-cassandra/build/libs(branch:master) » export CLASSPATH=kafka-connect-cassandra-0.2-3.0.1-all.jar
$ ~/workspace/datamountaineer/stream-reactor/kafka-connect-cassandra/build/libs(branch:master) » ~/workspace/confluent/confluent-3.0.1/bin/connect-distributed /tmp/connect-distributed.properties
$ ~/workspace/datamountaineer/kafka-connect-tools/build/libs(branch:master) » java -jar kafka-connect-cli-0.6-all.jar create product-csv-source < /tmp/connect-file-source.properties

$ ~/workspace/confluent/confluent-3.0.1 » ./bin/kafka-console-consumer --zookeeper localhost:2181 --topic product-csv-raw --from-beginning

$ apache-cassandra-3.9 » ./bin/cqlsh
$ CREATE KEYSPACE demo WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};
$ use demo;
$ cqlsh:demo>
        create table products (
         brand varchar, 
         supplierId varchar, 
         productType varchar, 
         gender varchar, 
         ageGroup varchar, 
         category varchar, 
         productFeature varchar, 
         productCode varchar,
         webProductDesc varchar, 
         productDesc varchar, 
         supplierColor varchar, 
         colorFeature varchar, 
         barcode varchar, 
         supplierSize varchar, 
         dsmSize varchar, 
         stockUnit varchar, 
         ftStockQuantity int, 
         ftPurchasePriceVatInc double, 
         psfVatInc double, 
         tsfVatInc double, 
         vatRate double, 
         material varchar, 
         composition varchar,
         productionPlace varchar, 
         productWeightKg double,
         productionContentWriting varchar,
         productDetail varchar,
         sampleSize varchar,
         modelSize varchar,
         supplierProductCode varchar,
         project varchar,
         theme varchar,
         trendLevel varchar,
         designer varchar,
         imageUrl varchar,
         PRIMARY KEY (barcode));

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

$ sbt project csv
$ sbt runMain com.ferhtaydn.csv.RawToAvroGenericProcessorBoot

$ ~/workspace/confluent/confluent-3.0.1 » ./bin/kafka-avro-console-consumer --zookeeper localhost:2181 --topic product-csv-avro --from-beginning

$ cqlsh:demo> select * from products;

```
