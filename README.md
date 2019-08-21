# azeron-client
Works with [Azeron Server](https://github.com/pinect-io/azeron-server), The scalable and reliable messaging library, Wraps nats.io and uses Java Spring framework

---

Azeron client is library to work with Azeron Server and nats.

## Features

- Ability to choose to use nats directly or along azeron for message publishing
- Ability to choose between different types of event listening strategies
- Provides fallback repository to resend messages after failure (when azeron is down)
- Recovers un-ack messages from server
- Provides abstract locking layer to synchronize message processing between multiple instances of a service
- Lets you choose your own discovery strategy in clustered environment