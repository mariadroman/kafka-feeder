# Kafka cluster setup
- Download the most recent kafka distribution (0.8.8.2)
- Extract the tar file somewhere convenient :
	- tar zxf kafka_2.10-0.8.2.0.tgz
- Step into the directory you extracted kafka in
- Start a zookeeper instance :
	- bin/zookeeper-server-start.sh config/zookeeper.properties
- Start the first kafka broker in the cluster :
  bin/kafka-server-start.sh config/server.properties
- Make a copy of the configuration file for the second kafka broker in the cluster :
	- cp config/server.properties config/server-1.properties
- Adjust the configuration for the second kafka broker to avoid brokerId, portNumber and logfile clashes
	- Change 'broker.id' to 1, 'port' to 9093, 'log.dir' to '/tmp/kafka-logs-1' in config/server-1.properties
- Adjust the configuration for the third kafka broker to avoid brokerId, portNumber and logfile clashes
	- Change 'broker.id' to 2, 'port' to 9094, 'log.dir' to '/tmp/kafka-logs-2' in config/server-2.properties
- Start the second kafka broker in the cluster:
	- bin/kafka-server-start.sh config/server-1.properties
- Start the third kafka broker in the cluster:
	- bin/kafka-server-start.sh config/server-2.properties
- Create a new 'github' topic, that will be used by producers and consumers of github events:
	- bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic github
- Verify that the topic was created succesfully
	- bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic github
	
        	Topic:github	PartitionCount:1	ReplicationFactor:3 Configs:
			Topic: github	Partition: 0	Leader: 1 Replicas: 1,2,0	Isr: 1,2,0
			
- We should now have a kafka cluster with three brokers that looks something like this:

                /  broker-0
      zookeeper -  broker-1
                \  broker-2
                

# Producing Kafka events
- Checkout the kafka-feeder repository from github:
	- git clone https://github.com/daanhoogenboezem/kafka-feeder
- We'll be analyzing a dataset that contains a very on all public github activity. Create a directory for that and step into it.
	- mkdir ~/githubdata && cd ~/githubdata
- Download a dataset from githubarchive.org that represents 1 weeks worth of events on all public repositories:
	- wget http://data.githubarchive.org/2015-01-0{1..7}-{0..23}.json.gz
- We'll be receiving a file gunzipped file per hour of public github activity, meaning 168 files. Extract them:
	- ls -1 | xargs gunzip
- Step into the kafka-feeder project directory and start sbt
- Run the KafkaFeeder, which will start publishing (producing) events to the 'github' topic:
	- run "localhost:9092,localhost:9093,localhost:9094" "github" "/path/to/githubdata" "10000"
- You should be seeing output along the lines of :

      Processing file 2015-01-01-0.json
      Pushing event to topic 'github'
      Pushing event to topic 'github'
      ...      

# Consume event stream
- Step into the kafka directory
- Consume the stream of events being pushed to the 'github' topic:
	- bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic github --from-beginning
	
# Kafka management
- Make sure that Kafka only binds to localhost
	- Uncomment '#host.name=localhost' in config/server-*.properties
- Enable the deletion of topics by adding the following entry to config/server-*.properties
	- delete.topic.enable=true
- Deleting a topic from kafka
	- bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic topic-name 
	
