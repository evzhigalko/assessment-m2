**Assessment task**


Consider using Event Sourcing and CQRS approach for that technical task.
Create one Spring boot service which allow CRUD operations for the customer object and should produce an event of any change of application state to the Kafka cluster Customer { Long id; String name; String address; } Read operation should respond with the current state of the Customer object. 
Create a second Spring boot service which should consume events and store it to any database of your choice.
Use Avro schemas to send and receive Events.
Add exception handling and logging, cover code with test.

**Technology stack**
- Java 17
- Spring Boot 3
- Apache Kafka
- Schema Registry (Avro schemas)
- MongoDB
- Redis
- Maven
- JUnit 5
- Mockito
- Testcontainers
- Docker/Docker Compose
