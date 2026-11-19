# outbound-postgres

PostgreSQL through plain JDBC (Agroal), with Flyway migrations.

**Extensions:** `io.quarkus:quarkus-agroal`, `io.quarkus:quarkus-narayana-jta`, `io.quarkus:quarkus-jdbc-postgresql`, `io.quarkus:quarkus-flyway`, `io.quarkus:quarkus-flyway-postgresql`.

```java
// core: <root>.feature.<x>.<X>RepositorySPI
public interface <X>RepositorySPI {
	void save(<X> item);
	List<<X>> findAll();
}

// outbound-postgres: <root>.feature.<x>.<X>Service
@ApplicationScoped
public class <X>Service implements <X>RepositorySPI {
	@Inject
	AgroalDataSource dataSource;

	@Override
	@Transactional
	public void save(<X> item) {
		try (var c = dataSource.getConnection(); var s = c.prepareStatement("insert into <x> (name) values (?)")) {
			s.setString(1, item.name());
			s.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<<X>> findAll() { /* select, map each row to <X> */ }
}
```

Once a second table appears, pull the connection handling into a small `cross.jdbc.Db` helper in this module (`query(sql, rowMapper, params…)`, `update(sql, params…)`) and give each table its own `<X>Table` class that the Service calls.

**Migrations:** `outbound-postgres/src/main/resources/db/migration/V1__create_<x>.sql`.

**Config:**

```properties
quarkus.datasource.db-kind=postgresql
quarkus.flyway.migrate-at-start=true
%test.quarkus.flyway.clean-at-start=true
%test.quarkus.flyway.clean-disabled=false
```

Leave the datasource URL unset: Dev Services starts PostgreSQL in dev and test (needs Docker).

**Test:** `@QuarkusTest` that injects the SPI, saves and reads back.
