IMPORTANT: native libraries must be added to LD_LIBRARY_PATH in local/development mode.
They are uncompressed and downloaded by maven to target/maven-shared... (see the pom.xml). You need to add the maven folder to your java.library.path.
Here's how in Eclipse:

	Run Configurations -> ... -> JRE -> Installed JREs... -> Click -> Edit ... -> Default VM Arguments: -Djava.library.path=target/maven-shared-archive-resources/