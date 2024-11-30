Here's the updated file with the new section on Configuration:

# Coding Standards and Best Practices

These are a set of coding standards and best practices to be followed during development. They cover various aspects, including code design, database design, validation, and testing. The main goals are to promote consistency, maintainability, and best practices across the codebase.

## Code Rules

### Transaction

1. **Avoid Big Transactions, Use Asynchronous and Eventually Consistent Approach**: Big transactions can lead to performance issues and potential deadlocks. Instead, use an asynchronous and eventually consistent approach to handle complex operations.
2. **Use @Transaction Annotation Appropriately**: We only use the `@Transaction` annotation on Aggregates and repositories. Any other places should not use this annotation.

### Error Definition

1. **Module-Specific Error Codes**: Each module should define its own error codes instead of relying on a common error code class. This promotes better encapsulation and maintainability within each module. For example, we have defined `InboundErrorDescEnum`, `OutboundErrorDescEnum`, and so on.

### Utils Usage

1. **Module Independence**: The `modules-utils` module should not depend on other modules, meaning it should not import or contain any domain classes. This module should only define common utility functions and classes.

2. **Redis Operations**: Use the `RedisUtils` class instead of `RedisTemplate` to perform Redis operations. For example:
   ```java
   redisUtils.get(key);
   ```

3. **Domain Event for Asynchronous Communication**: Use Domain Events to implement asynchronous communication between different domains. For example:
   ```java
   DomainEventPublisher.sendAsyncDomainEvent(stockTransferEvent);
   DomainEventPublisher.sendAsyncDomainEvent(orderEvent);
   ```

4. **Message Queue for Asynchronous Communication**: Use the `MqClient` class to implement asynchronous communication between servers via a message queue. For example:
   ```java
   mqClient.sendAsyncMessage(stockTransferEvent);
   mqClient.sendAsyncMessage(orderEvent);
   ```

5. **Distributed Locking**: Use the `DistributedLock` class to implement distributed locking mechanisms. For example:
   ```java
   distributedLock.acquireLock("lockKey", 1000);
   distributedLock.releaseLock("lockKey");
   ```

### Domain Entity

1. **Domain Entity Immutability**: Domain entities should not have setter methods. Instead, use methods that represent the entity's state transitions. For example:
   ```java
   // Instead of outboundPlanOrder.setStatus(xxx);
   outboundPlanOrder.complete();
   
   // In the OutboundPlanOrder class
   void complete() {
      this.status = COMPLETE;
   }
   ```

2. **Domain Entity Lifecycle**: Domain entities should have methods representing their full lifecycle. For example, the `OutboundPlanOrder` class should have methods like `initialize()` to set the initial status, `preAllocate()` to change the status to `ASSIGNED`, and so on. This approach makes the entity more "alive" and encapsulates the business logic within the entity.

   This practice promotes a better understanding of the entity's behavior and state transitions, making it easier to maintain and extend the codebase.

   Example:
   ```java
   public class OutboundPlanOrder implements Serializable {
   
       public void initialize() {
           this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.NEW;
       }
   
       public boolean preAllocate(List<OutboundPreAllocatedRecord> planPreAllocatedRecords) {
           this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.ASSIGNED;
           // Additional logic...
           return true;
       }
   
       public void cancel() {
           log.info("outbound plan order id: {}, orderNo: {} cancel", this.id, this.orderNo);
           if (OutboundPlanOrderStatusEnum.cancellability(this.outboundPlanOrderStatus)) {
               this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.CANCELED;
               this.details.forEach(OutboundPlanOrderDetail::cancel);
           }
       }
   
       // Other lifecycle methods...
   }
   ```

3. **String Field Length**: String type field lengths must be less than 512 characters. If you need to store more than 512 characters, use a text type and split the data into another table.

### Domain Service
1. **Domain Service Responsibilities**: Domain services should only contain calculation logic, and they must be stateless. For example, the `OutboundWaveService` contains a function `wavePickings` that takes a list of `OutboundPlanOrder` objects as input and returns the waved `OutboundPlanOrder` objects.
   ```java
   public interface OutboundWaveService {
       Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders);
   }
   ```

### Domain Aggregate
1. **Domain Aggregate Usage**: Domain aggregates will aggregate multiple domain entities. If we need to update multiple entities, we should build an aggregator and use the `@Transaction` annotation to ensure data consistency. Since we use the `@Transaction` annotation, we should pull queries out to improve speed.

### Domain Repository
1. **Domain Repository Responsibilities**:  Domain repositories should only contain find or save functions. They should not contain any update functions. For example:
```java
// Not compliant
void updateContainerStatus(Long containerId, ContainerStatusEnum status);

// Compliant
void saveContainer(Container container);
// Use like
Container container = containerRepository.findById(containerId);
container.putAway();
containerRepository.save(container);
```
2. **Function Naming**: All query functions should be named as find or findAll, and save functions should be named as save or saveAll.

### Server
1. **Server Module Responsibilities**: The server module cannot include business codes. It should only contain common configurations and the Spring Boot starter class.

## Database Rules

1. **Column Length Standards**: Follow these guidelines for column lengths:
   - Code: 64 characters
   - Name: 128 characters
   - Description: 255 characters
   - Remark: 500 characters
   - Enum: 20 characters

2. **POJO Class Design**: POJO classes must be designed as standard POJO classes, meaning they should not contain any special types or special database-specific configurations. For example:

   Instead of:
   ```java
   @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
   private String warehouseCode;
   ```

   Use:
   ```java
   @Column(nullable = false, length = 64)
   @Comment("仓库")
   private String warehouseCode;
   ```

   This approach separates the database-specific configurations from the POJO class, promoting better maintainability and portability of the code.

3. **Version Control for Optimistic Locking**: Add the `@Version` annotation on the POJO classes to enable optimistic locking and concurrency control.

## Validation Rules

1. **API Interface Method Validation**: Add `@Valid` or other validation annotations like `@NotEmpty` in the API interface method parameters.

   Example:
   ```java
   public interface IBatchAttributeConfigApi {
   
       void save(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);
   
       void update(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);
   
       BatchAttributeConfigDTO getByOwnerAndSkuFirstCategory(@NotNull String ownerCode, @NotNull String skuFirstCategory);
   }
   ```

2. **Service Implementation Validation**: Add `@Validated` in the service implementation classes.

   Example:
   ```java
   @Validated
   @Service
   public class BatchAttributeConfigApplicationImpl implements IBatchAttributeConfigApi {
       // ...
   }
   ```

   If your parameters are objects that need validation, add `@Validated(value = ValidationSequence.class)` instead of `@Validated`, and the object needs to implement the `IValidate` interface.

## Unit Test Rules

1. **Test Location**: All unit tests must be written within the respective module, not in the `xxx-server` module.

2. **Test Coverage**: Create unit tests for the domain layer, including entities and services. You can refer to the outbound tests as an example.

3. **Avoid Starting Spring Boot**: Unit tests should avoid running the Spring Boot application.

## Log Rules

1. **Log Language**: All log messages must be written in English.

## Configuration

1. **Configuration Classification**: Configurations are divided into two main categories: Environment Configuration and Business Configuration.
2. **Environment Configuration**: Environment configurations must be configured in Nacos.
3. **Business Configuration**: Business configurations must be configured in the database (e.g., MySQL).
4. **Nacos Restrictions**: Nacos cannot be used to configure any business configurations.


## Plugin Rules

1. **Plugin Definition and Scope**: Plugins must be defined once per domain. This means that each domain should have a dedicated plugin interface that encapsulates all the functionality related to that domain's lifecycle. For example, the ContainerTask domain should have an IContainerTaskPlugin interface that covers all the operations and behaviors related to container tasks.
```java
public interface IContainerTaskPlugin extends IContainerTaskGeneratorAction, IPlugin {
   // Default methods for various stages of the container task lifecycle
   default void beforeCreate(List<CreateContainerTaskDTO> createContainerTaskDTOs) { ... }
   default void afterCreate(List<CreateContainerTaskDTO> createContainerTaskDTOs) { ... }
   default void beforeFinish(List<ContainerTaskDTO> updateContainerTaskDTOs) { ... }
   default void afterFinish(List<ContainerTaskDTO> updateContainerTaskDTOs) { ... }
   default void beforeCancel(List<ContainerTaskDTO> containerTaskDTOs) { ... }
   default void afterCancel(List<ContainerTaskDTO> containerTaskDTOs) { ... }

   // Method for retrieving the generator plugin (if applicable)
   default IContainerTaskGeneratorAction getGeneratorPlugin() {
      return null;
   }
}

```
2. **Plugin Extensions and Actions**: If there is a need for additional functionality or extensions within a domain, you can create separate interfaces representing specific actions or behaviors. These interfaces can then be implemented by the main domain plugin interface.
   For example, if the ContainerTask domain requires generating different tasks based on the warehouse structure, you can create an IContainerTaskGeneratorAction interface and add a method in the IContainerTaskPlugin interface to retrieve an instance of the generator plugin.
```java
public interface IContainerTaskGeneratorAction {
    List<ContainerTaskDTO> generateContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOs);
}
```
```java
public interface IContainerTaskPlugin extends IContainerTaskGeneratorAction, IPlugin {
   // ...
   default IContainerTaskGeneratorAction getGeneratorPlugin() {
      return null;
   }
}
```
3. **Plugin Integration**: The integration of plugins into the application's business logic can be achieved by using a utility class like PluginUtils. This class can provide methods to retrieve instances of the required plugins.
```java
public List<ContainerTask> groupContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOs) {
   List<IContainerTaskPlugin> containerTaskPlugins = pluginUtils.getExtractObject(IContainerTaskPlugin.class);

   if (!containerTaskPlugins.isEmpty()) {
      IContainerTaskPlugin containerTaskPlugin = containerTaskPlugins.get(0);
      IContainerTaskGeneratorAction generatorPlugin = containerTaskPlugin.getGeneratorPlugin();
      if (generatorPlugin != null) {
         List<ContainerTaskDTO> containerTaskDTOs = generatorPlugin.generateContainerTasks(createContainerTaskDTOs);
         return containerTaskTransfer.dto2Dos(containerTaskDTOs);
      }
   }

   // Default implementation if no generator plugin is available
   List<ContainerTask> containerTasks = new ArrayList<>();
   createContainerTaskDTOs.stream()
           .collect(Collectors.groupingBy(dto -> getGroupKey(dto)))
           .forEach((key, values) -> containerTasks.addAll(generateTasks(values)));
   return containerTasks;
}


```
