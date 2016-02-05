
mvn install:install-file -Dfile=Harmony.jar -DartifactId=harmony -DgroupId=org.mitre -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=SchemaStoreClient.jar    -DartifactId=SchemaStoreClient    -DgroupId=org.mitre -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=SchemaStorePorters.jar   -DartifactId=SchemaStorePorters   -DgroupId=org.mitre -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=SchemaStoreUtilities.jar -DartifactId=SchemaStoreUtilities -DgroupId=org.mitre -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=gchart.jar -DartifactId=gchart -DgroupId=com.googlecode -Dversion=2.7 -Dpackaging=jar

mvn install:install-file -Dfile=html5canvas-1.0.jar -DartifactId=html5canvas -DgroupId=com.blogspot.qbeukes.gwt -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=YAMSimLib.jar -DartifactId=simlib -DgroupId=yam -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=YAMDataTypes.jar -DartifactId=datatypes -DgroupId=yam -Dversion=1.0 -Dpackaging=jar
