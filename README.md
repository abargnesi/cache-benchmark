cache-benchmark
===============

Build cache benchmark jar with:

```
mvn clean package -DskipTests
```

Run cache benchmark with:

```
java -jar target/benchmarks.jar -t {num threads for benchmark}
```
