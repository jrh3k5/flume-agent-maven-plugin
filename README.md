# Flume Agent Maven Plugin

<img src="https://travis-ci.org/jrh3k5/flume-agent-maven-plugin.svg?branch=master" />
<img src="https://maven-badges.herokuapp.com/maven-central/com.github.jrh3k5/flume-agent-maven-plugin/badge.svg" />

A Maven plugin used to start, stop, and run a Flume agent. By default, this runs Flume 1.7.0.

## Goals

This plugin defines three goals:

* **run**: This, given the configuration of the plugin, runs a standalone instance of Flume agents.
  * NOTE: there is a known issue of a process being left behind on Windows platforms. Refer to [#19](https://github.com/jrh3k5/flume-agent-maven-plugin/issues/19) for more information.
* **start**: This starts the configured Flume agents. By default, it binds to the `pre-integration-test` phase.
* **stop**: This is the counterpart to the `start` goal, stopping the agents it started. By default, it binds to the `post-integration-test` phase.

## Configuration

The following describes configuration elements for the goals.

### Running and Starting an Agent

The goals to run and start agents share the same configuration parameters and are, thus, grouped into the same description here.

#### Required Parameters

These goals are geared toward multi-agent startups; as a result, the individual agent configurations are nested within an `<agents />` block. The following are required parameters in the configuration block of each agent. These, at a minimum, must be provided for the Flume agent to be started:

* **agentName**: The name of the Flume agent (matching one in the given configuration file) that is to be loaded and run.
* **configFile**: The configuration file that informs Flume how the agent named is to be configured.

An example configuration might look like:

```
<configuration>
    <agents>
        <agent>
            <agentName>a1</agentName>
            <configFile>${project.build.outputDirectory}/flume.properties</configFile>
        </agent>
    </agents>
</configuration>
```

#### Optional Configuration Parameters

The following are configuration parameters that can be configured optionally; either reasonable defaults are provided or they are not needed to run the agent.

#### Removing Flume Libraries

This feature was introduced in version 1.1 of the plugin.

Flume does not provide an out-of-the-box way to override the libraries provided by Flume. This can result in dependency collisions with plugins. To facilitate deference of classloading to the libraries provided by Flume plugins, this Maven plugin allows the specification of libraries to be removed from the Flume agent prior to it being started.

For example, to remove the `libthrift-0.7.0.jar` from the Flume agent's `lib/` directory, you can provide a configuration like the following:

```
<configuration>
    <agents>
        <agent>
            <!-- required fields omitted for brevity -->
            <libs>
                <removals>
                    <removal>libthrift-0.7.0.jar</removal>
                </removals>
            </libs>
        </agent>
    </agents>
</configuration>
```

#### Setting the JAVA_OPTS Parameter

This can be used to set the JAVA_OPTS parameter passed to the Flume agent's Java environment. This can be particularly useful because Flume, by default, only runs with a max heap of 20 MB (which can easily be too low, especially when using custom sinks, channels, and sources) or if you wish to enable JMX in the Flume agent to access its MBeans. An example configuration may look like:

```
<configuration>
    <agents>
        <agent>
            <!-- required fields omitted for brevity -->
            <javaOpts>-Xms128m -Xmx512m</javaOpts>
        </agent>
    </agents>
</configuration>
```

#### Flume Plugins

The Maven plugin can also add Flume plugins to the Flume agent, making it easier to test your custom sinks, channels, and sources. You may want to consult the [flume-plugin-maven-plugin](https://github.com/jrh3k5/flume-plugin-maven-plugin) for a tool used to assemble plugins in a format that this plugin expects.

The Flume plugin installation uses the dependency resolution of your POM to locate and download the plugin to be installed. An example configuration might look like:

```
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.jrh3k5</groupId>
                <artifactId>flume-agent-maven-plugin</artifactId>
                <configuration>
                    <agents>
                        <agent>
                            <!-- required fields omitted for brevity -->
                            <flumePlugins>
                                <flumePlugin>
                                    <groupId>com.me</groupId> <!-- required -->
                                    <artifactId>my-project</artifactId> <!-- required -->
                                    <classifier>flume-plugin</classifier> <!-- default value - this is optional -->
                                    <type>tar.gz</type> <!-- default value - this is optional -->
                                </flumePlugin>
                            </flumePlugins>
                        </agent>
                    </agents>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <dependency>
            <groupId>com.me</groupId>
            <artifactId>my-project</artifactId>
            <version>1.2.3</version>
            <classifier>flume-plugin</classifier>
            <type>tar.gz</type>
        </dependency>
    </dependencies>
</project>
```

This tells the plugin to look for a dependency in your project matching the given identifying information and to unpack and install it into the <tt>plugins.d/</tt> directory beneath the Flume agent installation.

#### Change Source of Flume Archive

By default, the plugin downloads (and then caches) the archive of Flume from the [Apache archives](http://archive.apache.org/dist/flume/). If for some reason that URL is unavailable to you or the default version that the plugin uses is not suitable to your needs, you can change the location from which plugin downloads Flume by setting the `flumeArchiveUrl` and `flumeArchiveMd5` parameters, like so:

```
<configuration>
    <flumeArchiveUrl>http://archive.apache.org/dist/flume/1.3.1/apache-flume-1.3.1-bin.tar.gz</flumeArchiveUrl>
    <flumeArchiveMd5>09362a5a8ed92c6fb0bfbdb2802301db</flumeArchiveMd5>
</configuration>
```

#### Logging Configuration

Starting with version 2.1.1 of the plugin, you can specify a logging configuration to be used by an agent like so:

```
<configuration>
    <agents>
        <agent>
            <loggingProperties>/some/location/of/logging.properties</loggingProperties>
        </agent>
    </agents>
</configuration>
```

This is expected to conform to the logging implementation used by your selected version of Flume.
