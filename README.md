# draw-out-spring-boot

Drawout for Spring Boot is a tool to get metrics from your project based on Spring Boot framework.

This tool makes use of AspectJ to inject code for metrics accountability.

### How to use

You just need to refer to this project as your parent artifact inside your main project file (pom.xml)

```xml
<parent>
	<groupId>br.com.ideotech</groupId>
	<artifactId>draw-out-spring-boot-lombok-lib</artifactId>
	<version>2.0.4.RELEASE</version>
	<relativePath />
</parent>
```

The version are respectively to the project `org.springframework.boot:spring-boot-starter-parent`, in the example above we are using the version *2.0.4.RELEASE* of *spring-boot-starter-parent*

You have two artifacts to choose here, they are:
- **draw-out-spring-boot-lib:** It's a simple version that doesn't provide support Lombok.
- **draw-out-spring-boot-lombok-lib:** It's provide support for lombok, but if you makes use of *maven-shade-plugin* into your project notice that you may need to add an additional config `combine.children="append"` if you wish to excludes some dependencies from your distribution.

Bellow are an example to follow if you need to excludes some dependencies:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-shade-plugin</artifactId>
	<configuration>
		<createDependencyReducedPom>false</createDependencyReducedPom>
	</configuration>
	<executions>
		<execution>
			<phase>package</phase>
			<goals>
				<goal>shade</goal>
			</goals>
			<configuration>
				<artifactSet>
					<excludes combine.children="append">
						<exclude>org.apache.tomcat.embed:*</exclude>
					</excludes>
				</artifactSet>
			</configuration>
		</execution>
	</executions>
</plugin>
```

#### Properties

There are some properties to be configured:

- **drawout.kinesis.stream:** The name of kinesis stream
- **drawout.kinesis.partition.name:** *(optional)* The partition name for Kinesis records, it will be prefixed by `drawout` string. So if it wasn't defined the partition name will contains only `drawout` string
- **drawout.dump.request.sensitive-data:** *(defaults=false)* If true will dump information about query parameters, headers, cookies and request attributes
- **drawout.dump.request.payload:** *(defaults=false)* If true will dump the request body payload
- **drawout.dump.response.payload:** *(defaults=false)* If true will dump the response body payload
