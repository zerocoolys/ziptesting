### Runing

* how to execute. Step into the root folder, and run the command below. 
```java

mvn clean compile exec:java

```

* to change the arguments , open the **pom.xml**, and change the properties below:
```xml
<!-- the source data path -->
<args.input>/Users/yousheng/workspace/go</args.input>
<!-- the compressed data output path -->
<args.output>/Users/yousheng/workspace/go_output</args.output>
<!-- decompressed data output path -->
<args.unzip>/Users/yousheng/workspace/go_unzip</args.unzip>
<!-- the max file size -->
<args.size>20</args.size>

```

### Tesing

normal testing passed.

```text
env: macbook pro 2015

source data: $GOPATH , with 23435 regular files, largest file size is more than 32m, total size more than 1.4G

vm option: -Xms32m -Xmx32m -XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps

compress time: 76988 millis

decompress time: 11758 millis

```

My GOPATH contains 23000+ files ,and total file count is 1.4+ G, running application with jvm option "-Xms32m -Xmx32m -XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps",


###Known issue

1. when source data contains *zip*,*mp4* and other file type like be zipped already ,will throw a data header error exception when doing decompressed. The cause seems like when 
input zipped source data into Java zip Inflater, the source length is less than the output result len.
2. file type such as some c++ compile ouput file ".a" will get a error size.


### Thank You
