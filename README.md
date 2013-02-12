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
	
IMPORT: For executing the examples you should have at least a QNode and DNode running in your system.

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