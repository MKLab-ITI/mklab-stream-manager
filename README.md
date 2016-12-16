mklab-stream-manager
===========================

<p>Stream Manager monitors a set of seven social streams : Twitter, Facebook, Instagram, Google+, Flickr, Tumblr and Youtube to collect incoming content relevant to a keyword, a user or a location, using the corresponding API that is supported from each service. Twitter API works as a real-time service, whereas the other six act as polling consumers perfoming requests to the network periodically. The framework also provides wrappers to a set of different storages.</p>

<h2><u>Getting started</u></h2>
The input data to the Stream Manager that will determine the nature of the retrieved content are viewed as input feeds that can represent either a keyword, a user or a location. There are several sources where the input feeds can be read from (i.e. a configuration file or a database). In general the feeds that will be used as the input to the system are created by the `eu.socialsensor.sfc.streams.input.FeedCreator` interface, which is implemented accordingly to the source that the input data are read from. In the case of a configuration file used as input, input feeds are created with `eu.socialsensor.sfc.streams.input.FeedsCreatorImpl.ConfigFeedsCreator` class, whereas when the data are read from <a href="http://www.mongodb.org/">MongoDB</a>, input feeds are created with `eu.socialsensor.sfc.streams.input.FeedsCreatorImpl.MongoFeedsCreator` class. It is important to note that, in contrast to the other six APIs, Twitter is able to collect content of no specific origin when no input is given.

To start collecting content from all or a subset of the above social networks, run the `gr.iti.mklab.sfc.StreamsManagerApp` class. Firstly, this class reads a configuration file, that contains the information regarding the creadentials needed to establish a connection with each social network, as well as the mandatory fields for storing/reading data to/from the selected databases. After the configuration file is read, the retrieving process occurs as shown below : 

1. An instance of the  `gr.iti.mklab.sfc.StreamsManager` class is created. This class is responsible for managing all the streams (open,search and close a stream) : 

          StreamsManager manager = new StreamsManager(config);
          

2. Manager opens all the streams, reads the input feeds with FeedCreator and establishes the connection with the given creadentials


           manager.open();  
           
3. Manager starts the retrieval process for all the streams. For the non-real time APIs, polling requests are performed periodically. 
    
           manager.search();
    

Inside Stream Manager, each stream is handled as a different thread. Thus, each social network wrapper can be given different input feeds to track. Additionally, each feed is being tracked by a different thread in order to minimize time cost, except Twitter which is a subscriber.

The collected data from the retrieval process are stored as JSON items (representing status update, post etc.), media items (images, videos, albums), users and webpages (embedded in posts/statuses). The above can be stored in a <a href="http://www.mongodb.org/">MongoDB</a>, <a href="http://lucene.apache.org/">Lucene</a> or <a href="http://lucene.apache.org/solr/">Solr</a> database, which are currently supported. In addition to this, the collected data are used to create graphs, which currently model `user -- retweets -- user` and `user -- mentions -- user` relationship. The graphs are created with <a href="http://thinkaurelius.github.io/titan/">Titan</a> and <a href="http://www.neo4j.org/">Neo4j</a> graph databases. The storage process is handled by the `gr.iti.mklab.sfc.management.Storagehandler` class.

<h2><u>Learning more</u></h2>

Stream Manager project is dependent to other three MKLab projects : 

1. <a href="https://github.com/MKLab-ITI/mklab-socialmedia-abstractions">mklab-socialmedia-abstractions</a> : The abstraction layer for mapping a set of different social networks' wrappers to a single representation. 
2. <a href="https://github.com/MKLab-ITI/mklab-framework-common"> mklab-framework-common</a> : The project contain main classes and interfaces to be used by other SocialSensor projects
3. <a href="https://github.com/MKLab-ITI/mklab-framework-client"> mklab-framework-client</a> : The wrappers for handling information in/from the supported databases (MongoDB,Solr,Lucene).


<h3><u>Contact for further details about the project</u></h3>


Manos Schinas (manosetro@iti.gr), Symeon Papadopoulos (papadop@iti.gr)
