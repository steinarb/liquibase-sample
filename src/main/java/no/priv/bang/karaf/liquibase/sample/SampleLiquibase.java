package no.priv.bang.karaf.liquibase.sample;

import static liquibase.Scope.Attr.resourceAccessor;
import static liquibase.command.core.UpdateCommandStep.CHANGELOG_FILE_ARG;
import static liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep.DATABASE_ARG;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@Component(immediate = true, property = "name=liquibasesampledb")
public class SampleLiquibase implements PreHook {

    @Activate
    public void activate() {
        // Called when component is activated
    }

    @Override
    public void prepare(DataSource datasource) throws SQLException {
        try(var connection = datasource.getConnection()) {
            createSchema(connection);
        } catch (Exception e) {
            throw new SQLException("Failed to create liquibase schema for liquibase-sample", e);
        }
    }

    public void createSchema(Connection connection) throws LiquibaseException {
        applyLiquibaseChangelist(connection, "liquibasesample/changelog01.xml");
    }


    public void applyLiquibaseChangelist(Connection connection, String changelistClasspathResource, ClassLoader classLoader) throws LiquibaseException {
        try (var database = findCorrectDatabaseImplementation(connection)) {
            Scope.child(scopeObjects(classLoader), () -> new CommandScope("update")
                .addArgumentValue(DATABASE_ARG, database)
                .addArgumentValue(CHANGELOG_FILE_ARG, changelistClasspathResource)
                .execute());
        } catch (LiquibaseException e) {
            throw e;
        } catch (Exception e) {
            // AutoClosable.close() may throw Exception
            throw new LiquibaseException(e);
        }
    }

    private Map<String, Object> scopeObjects(ClassLoader classLoader) {
        return Map.of(resourceAccessor.name(), new ClassLoaderResourceAccessor(classLoader));
    }

    private Database findCorrectDatabaseImplementation(Connection connection) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    private void applyLiquibaseChangelist(Connection connection, String changelistClasspathResource) throws LiquibaseException {
        applyLiquibaseChangelist(connection, changelistClasspathResource, getClass().getClassLoader());
    }

}
