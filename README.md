# ETL
ETL implementation on Java<br/>
This repository contains ETL implementation on Java to migrate data from TSF (tab-separated file) to database

## Features
- uses MongoDB
- validates source file headers and lines of data
- uses predicate to filter items to save to the database
- on validation error skips invalid lines and continue processing others

## Environment variables

   name|default|mandatory|description
   ----|-------|----|---
   MONGODB_URI|mongodb://localhost:27017|N|MongoDB connection string
   SOURCE_FILE_PATH|src/main/resources/etl_0.tsv|N|Path to the source file

## Building info
Create mongo database container using docker
<pre>
sudo docker run -d --name mongo -p 27017:27017 mongo:4.0
</pre>
or using docker-compose
<pre>
sudo docker-compose up -d
</pre>
Check the database is successfully running
<pre>
sudo lsof -i :27017
</pre>
Give executable permission to gradlew script
<pre>
sudo chmod +x gradlew
</pre>
Build an executable jar
<pre>
./gradlew clean build
</pre>
Run jar
<pre>
java -jar build/libs/*.jar
</pre>

## Testing info
Implementation is covered by unit tests for source file data validation and database repository interface operations<br/>
Used groovy SPOCK framework for this purpose<br/>
To ensure everything works correctly run `gradlew clean test` form the project root
