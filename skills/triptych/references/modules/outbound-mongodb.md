# outbound-mongodb

MongoDB through Panache.

**Extensions:** `io.quarkus:quarkus-mongodb-panache`.

```java
// core: <root>.feature.<x>.<X>StoreSPI
public interface <X>StoreSPI {
	void append(String event, String details);
	List<<X>Entry> latest(int limit);
}

// outbound-mongodb: <root>.feature.<x>.<X>EntryEntity
@MongoEntity(collection = "<x>")
public class <X>EntryEntity extends PanacheMongoEntity {
	public String event;
	public String details;
	public Instant timestamp;
}

// outbound-mongodb: <root>.feature.<x>.<X>Service
@ApplicationScoped
public class <X>Service implements <X>StoreSPI {
	@Override
	public void append(String event, String details) {
		var entry = new <X>EntryEntity();
		entry.event = event;
		entry.details = details;
		entry.timestamp = Instant.now();
		entry.persist();
	}

	@Override
	public List<<X>Entry> latest(int limit) {
		return <X>EntryEntity.<<X>EntryEntity>findAll(Sort.descending("timestamp")).page(0, limit).list()
			.stream().map(e -> new <X>Entry(e.event, e.details, e.timestamp)).toList();
	}
}
```

The entity stays in this module; the SPI speaks domain types (`<X>Entry` in `core`).

**Config:** `quarkus.mongodb.database=<artifactId>`. Leave the connection string unset: Dev Services starts MongoDB in dev and test (needs Docker).

**Test:** `@QuarkusTest` that injects the SPI, appends and reads back.
