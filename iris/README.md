You can find the IRIS Reasoner here:

http://sourceforge.net/p/iris-reasoner/code/HEAD/tree/iris/tags/iris-0.8.1/

Or just use the copies in this directory:

    mvn install:install-file -Dpackaging=pom -DgroupId=at.sti2.iris -DartifactId=iris -Dversion=0.8.1 -Dfile=pom-iris-0.8.1.xml
    mvn install:install-file -DgroupId=at.sti2.iris -Dversion=0.8.1 -DartifactId=iris-api \
                             -DpomFile=pom-iris-api-0.8.1.xml -Dfile=iris-api-0.8.1.jar
    mvn install:install-file -DgroupId=at.sti2.iris -Dversion=0.8.1 -DartifactId=iris-impl \
                             -DpomFile=pom-iris-impl-0.8.1.xml -Dfile=iris-impl-0.8.1.jar
    mvn install:install-file -DgroupId=at.sti2.iris -Dversion=0.8.1 -DartifactId=iris-parser \
                             -DpomFile=pom-iris-parser-0.8.1.xml -Dfile=iris-parser-0.8.1.jar

