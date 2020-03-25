[![](https://jitpack.io/v/sepehr-gh/azeron-client.svg)](https://jitpack.io/#sepehr-gh/azeron-client)

# azeron-client
Works with [Azeron Server](https://github.com/sepehr-gh/azeron-server), The scalable and reliable messaging library, Wraps nats.io and uses Java Spring framework

---

Azeron client is library to work with Azeron Server and nats.

## Features

- Ability to choose between using nats directly or Azeron for message publishing
- Ability to choose between different types of event listening strategies
- Provides fallback repository to resend messages after failure (when azeron or nats is down)
- Recovers messages from server
- Lets you choose your own discovery strategy in clustered environment


## Installation

### Maven

Add jitpack repository to your repositories:

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

Add azeron dependency:

	<dependency>
	    <groupId>com.github.sepehr-gh</groupId>
	    <artifactId>azeron-client</artifactId>
	    <version>1.2.0-SNAPSHOT</version>
	</dependency>

### Gradle, sbt, leiningen

Check [Jitpack](https://jitpack.io/#sepehr-gh/azeron-client)

### Clone from source

	git clone https://github.com/sepehr-gh/azeron-client.git

change directory to `azeron-client` and build project with maven

	mvn clean install

use Azeron in your maven POM dependencies

	<dependency>
		<groupId>io.pinect</groupId>
		<artifactId>azeron-client</artifactId>
		<version>1.1.6-SNAPSHOT</version>
	</dependency>

## Usage

Annotate your Spring Boot Application with `@EnableAzeronClient`

	@EnableAzeronServer
	@Configuration
	public class AzeronConfiguration {
	}

### Implement new clustering methods

It does not really matter how do you cluster your application. You can see our [example with eureka](https://github.com/sepehr-gh/azeron-examples/tree/master/azeron-client-eureka)

Its just important to provide a way to help clients fetch nats information from azeron server. Therefore, you have to implement your own `NatsConfigProvider` form package `io.pinect.azeron.client.service.api`.

Check [Azeron Server API's](https://github.com/sepehr-gh/azeron-server#api) for more information on how to get nats configurations.

### Implement ping service

In clustered environment, if any azeron instance is up, then things are working good. By default client sends request to azeron instance defined in its configuration but you might want to change this behaviour by implementing your own `Pinger` from `io.pinect.azeron.client.service.api`.

### Add new listener

The best way to create a listener is to implement `SimpleEventListener` for generic type of your DTO.
Then you need to mark this class with `@AzeronListener` and define eventName, classType of DTO, and policy.


    @Log4j2
    @AzeronListener(eventName = "event_channel_name", ofClass = YourDto.class, policy = HandlerPolicy.AZERON)
    public class SeenAsyncStrategyListener implements SimpleEventListener<SimpleAzeronMessage> {
        private final String serviceName;
        @Autowired
        public SeenAsyncStrategyListener(@Value("${spring.application.name}") String serviceName) {
            this.serviceName = serviceName;
        }
    
        @Override
        public AzeronMessageProcessor<SimpleAzeronMessage> azeronMessageProcessor() {
            return new AzeronMessageProcessor<SimpleAzeronMessage>() {
                @Override
                public void process(SimpleAzeronMessage simpleAzeronMessage) {
                    // Your processor logic
                }
            };
        }
    
        @Override
        public AzeronErrorHandler azeronErrorHandler() {
            return new AzeronErrorHandler() {
                @Override
                public void onError(Exception e, MessageDto messageDto) {
                    error handler logic
                }
            };
        }
    
        @Override
        public String serviceName() {
            return serviceName;
        }
    
    }

Azeron will register these beans when application is starting up.

##### Details

The generic `<E>` is type of the message dto class. Azeron will convert incoming message to this generic type automatically.

`AzeronMessageProcessor<E> azeronMessageProcessor()`: This is where you process the message.

`AzeronErrorHandler azeronErrorHandler()` this is where you handle errors.

`String eventName()`: event name or channel name you are subscribing/listening to.

`ClientConfig clientConfig()`: Simple client config to let azeron server know about this service. Use queue groups to balance messages between multiple instances of service.

`HandlerPolicy policy()`: Policy for message handling.

### Listener policies

- **FULL**: Receives message, process it, sends seen after process is complete
- **SEEN_FIRST**: Sends back seen (in new thread, it might fail), no matter if process is completed without errors
- **SEEN_ASYNC**: Receives message, process it, sends seen in new thread after process is complete
- **NO_AZERON**: Does not send back any seen or acknowledgement.


### Publish Message

All you need is to create an interface and mark it with `@Publisher`. Azeron automcatially wraps this interface with proxy
and handles message publishing when publish method(s) are invoked. First parameter of publish method should be the DTO to publish.
If you are using nats replies, you can add message handler as second argument.

Also, you can use `@Async` to move publish call to new thread. TaskExecutor configurations will be discussed later.

    @Publisher(
            publishStrategy = EventMessagePublisher.PublishStrategy.AZERON,
            eventName = "event_name",
            forClass = YourDto.class
    )
    public interface MyMessagePublisher {
        public void publish(YourDto yourDto){}
        
        public void publish(YourDto yourDto, @Nullable MessageHandler messageHandler) {
    
        }
    }

Then import and autowire your publisher to other services and publish your dto by invoking `publish(muDto, null)`.

### Publish Strategies

- **AZERON**: Sends message using Azeron, contains fallback repository to resend if failed
- **BLOCKED**: Sends message using Azeron, retries till message is successfully sent.
- **NATS**: Sends message using Nats (Does not check if Azeron Server is up! Message is sent as long as nats is connected)
- **AZERON_NO_FALLBACK**: Sends message using Azeron, does not provide fallback repository. If message sending is failed, its failed!

## Configuration


	azeron.client.azeron-server-host=localhost ##DEFAULT server address (and possibly port)
	azeron.client.ping-interval-seconds=10 ##Ping interval
	azeron.client.retrieve-unseen=true #Either this client must query for unseen messages or not
	azeron.client.fallback-publish-interval-seconds=20 #Fallback repository republish interval
	azeron.client.un-subscribe-when-shutting-down=false
	azeron.client.unseen-query-interval-seconds=20
	
	# These async configurations are for publisher task executors.
	
    azeron.client.asyncThreadPoolCorePoolSize=10
    azeron.client.asyncThreadPoolMaxPoolSize=10
    azeron.client.asyncThreadPoolQueueCapacity=10