# Versions
Java openjdk 17.0.3 2022-04-19

Apache Maven 3.6.3

Micronaut 3.8.2

JobRunr 6.0.0 (I also tried 5.3.3)

# Config
This application will startup 
1. A micronaut REST server on port 8000
2. The JobRunr dashboard on port 8001

You will also need to startup a postgres database. One is provided in this repo via docker can be started by using 
`docker-compose up -d db`


The rest endpoints can be found in test.runr.JobController. I used postman to exercise them. Most of them have issues with parameters getting cached. 
Note, the caching is not happening in the method, it is happening when jobrunr stores the lambda. Additionally one endpoint throws an error.

# Issue
JobRunr seems to have a problem with parameters that are 
1. primitives, 
2. cast inside the lambda, 
3. POJO's.

# Testing
To test I would hit each endpoint twice with 2 different values (1, 2).
Then I would go to the JobRunr dashboard and look at Recurring Jobs where it lists the "JobName". There you can see 
the generated job name has the cached values. You can also wait for the job to run but that takes longer.

When the endpoint is run I immediately run the lamda to show that the caching hasn't happened yet. Then when JobRunr 
runs the lambda you will see the values are cached based on the output.

### Example
```
mvn clean install
docker-compose up -d db
java -jar target/test-runr.jar

PUT http://localhost:8000/v1/schedule/1/int
PUT http://localhost:8000/v1/schedule/2/int
```
Go to http://localhost:8001/dashboard/recurring-jobs to see the job name.

## BUG: intValue cached
The intValue parameter comes in fine but when then lambda is stored somehow JobRunr is caching the intValue such that subsequent 
calls use the first calls id.

`PUT http://localhost:8000/v1/schedule/{intValue}/int`

## WORKS: The integerValue not cached 
The integerValue is now an object Integer, and seems to get written to JobRunr just fine (not cached).

`PUT http://localhost:8000/v1/schedule/{integerValue}/integer`

## ERROR: Throws exception
In the lambda I pass an int into a method that takes a long. While this is valid Java, JobRunr throws an error.

`PUT http://localhost:8000/v1/schedule/{intValue}/int_uselong`

## BUG: The intValue cached, UUID not cached.
The intValue parameter comes in fine but when then lambda is stored somehow JobRunr is caching the intValue such that subsequent
calls use the first calls id. Interestingly enough the UUID that is generated does not seem to get cached.

`PUT http://localhost:8000/v1/schedule/{intValue}/intuuid`

## BUG: MyObj cached
The entire POJO MyObj(intValue, UUID) seems to get cached by JobRunr such that subsequent calls use the first calls object.

`PUT http://localhost:8000/v1/schedule/{intValue}/intobj`

## BUG: longValue cached
The longValue parameter comes in fine but when then lambda is stored somehow JobRunr is caching the longValue such that subsequent
calls use the first calls id.

`PUT http://localhost:8000/v1/schedule/{longValue}/long`

## WORKS: longValue Long is not cached
The longValue is now an object Long, and seems to get written to JobRunr just fine (not cached).

`PUT http://localhost:8000/v1/schedule/{longValue}/longobj`
