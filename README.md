# draw-out-spring-boot

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
