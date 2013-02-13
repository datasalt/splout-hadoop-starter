splout-hadoop-starter
=====================

A maven project with everything properly set up for using the splout-hadoop API of Splout SQL (http://sploutsql.com/, https://github.com/datasalt/splout-db/).
It contains a toy example of indexing and deploying a tablespace with two tables.

IMPORTANT: native libraries must be added to LD_LIBRARY_PATH in local/development mode.
They are uncompressed and downloaded by maven to target/maven-shared... (see the pom.xml). You need to add the maven folder to your java.library.path.
Here's how in Eclipse:

	mvn install
	mvn eclipse:eclipse
	Run Configurations -> ... -> JRE -> Installed JREs... -> Click -> Edit ... -> Default VM Arguments: -Djava.library.path=target/maven-shared-archive-resources/
	
IMPORTANT: For executing the examples you should have at least a QNode and DNode running in your system (see Splout SQL's Getting started: http://sploutsql.com/gettingstarted.html).

For running the example locally, just execute "GenerateTablespace" with no args first and "DeployTablespace" with no args afterwards.

For running the example in pseudo-distributed mode first copy the toy resources to the HDFS:

	hadoop fs -put src/main/resources/ src/main/resources
	
Then install, uncompress the .tar.gz and proceed as follows:

	mvn install
	cd target
	tar xvfz splout-hadoop-starter-0.0.1-SNAPSHOT-distro.tar.gz
	cd splout-hadoop-starter-0.0.1-SNAPSHOT
	hadoop jar splout-hadoop-starter-0.0.1-SNAPSHOT-hadoop.jar generate
	hadoop jar splout-hadoop-starter-0.0.1-SNAPSHOT-hadoop.jar deploy

You can check that everything went fine by issuing the following queries providing any key:

	SELECT * FROM geonames;
	SELECT * FROM hashtags;
	
The example
===========

The example is very simple and it just indexes a small file with Twitter hashtag counts per day, partitioning it by hashtag.
This means you can do queries like: 

	(key = 'california')
	SELECT * FROM hashtags WHERE hashtag = 'california';
	 
It also indexes a toy database of "geonames". So you can check whether some hashtag is an alternate name
for a location:

	(key = 'california')
	SELECT * FROM geonames WHERE altname = 'california';
	
Indeed, you can query in each partition which hashtags correspond to a geo location:

	(for any partition key)
	SELECT * FROM hashtags, geonames WHERE altname = hashtag;  
