Privacy Plugin for PDI/Kettle
====

This project implements a plugin for the Pentaho Data Integraion platform which provides methods for assessing and managing re-identification risks based on the methodology proposed in "El Emam, Khaled, Guide to the De-Identification of Personal Health Information, CRC Press, 2013".

Compilation
------
As a prerequisite, libarx-3.7.0-min and jhpl-0.0.1 have to be deployed to the local maven repository. To this end, execute the following commands:

```bash
cd ${project.dir}/lib
```

```bash	
mvn install:install-file -Dfile=arx/libarx-3.7.0-min.jar -DgroupId=org.deidentifier.arx -DartifactId=libarx-min -Dversion=3.7.0 -Dpackaging=jar
mvn install:install-file -Dfile=jhpl/jhpl-0.0.1.jar -DgroupId=jhpl -DartifactId=jhpl -Dversion=0.0.1 -Dpackaging=jar
```
    
Configure the property <kettle.install.dir/> in pom.xml so that 
the build result is automatically deployed to the local PDI installation
  
For the actual compilation, build and deploy, execute, the following commands:
```bash
cd ${project.dir}
```

```bash	
mvn clean install
```

License
------

GPLv3