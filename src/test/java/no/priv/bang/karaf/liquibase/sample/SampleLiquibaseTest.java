package no.priv.bang.karaf.liquibase.sample;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.ops4j.pax.jdbc.derby.impl.DerbyDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

class SampleLiquibaseTest {
    DataSourceFactory derbyDataSourceFactory = new DerbyDataSourceFactory();

    @Test
    void testCreateSchema() throws Exception {
        var datasource = createDataSource("liquibasesample");
        var liquibase = new SampleLiquibase();
        liquibase.activate();

        liquibase.prepare(datasource);
        try(var connection = datasource.getConnection()) {
            addAlbumEntries(connection);
            assertAlbumEntries(connection);
        }
    }

    private void assertAlbumEntries(Connection connection) throws Exception {
        assertAlbumEntry(connection, 1, 0, "/album/", true, "Album", "This is an album", null, null);
        assertAlbumEntry(connection, 2, 1, "/album/bilde01", false, "VFR at Arctic Circle", "This is the VFR up north", "https://www.bang.priv.no/sb/pics/moto/vfr96/acirc1.jpg", "https://www.bang.priv.no/sb/pics/moto/vfr96/icons/acirc1.gif");
    }

    private void assertAlbumEntry(Connection connection, int id, int parent, String path, boolean album, String title, String description, String imageUrl, String thumbnailUrl) throws Exception {
        var sql = "select * from albumentries where albumentry_id = ?";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try(var result = statement.executeQuery()) {
                if (result.next()) {
                    assertEquals(parent, result.getInt(2));
                    assertEquals(path, result.getString(3));
                    assertEquals(album, result.getBoolean(4));
                    assertEquals(title, result.getString(5));
                    assertEquals(description, result.getString(6));
                    assertEquals(imageUrl, result.getString(7));
                    assertEquals(thumbnailUrl, result.getString(8));
                } else {
                    fail(String.format("Didn't find albumentry with id=%d", id));
                }
            }
        }
    }

    private void addAlbumEntries(Connection connection) throws Exception {
        addAlbumEntry(connection, 0, "/album/", true, "Album", "This is an album", null, null);
        addAlbumEntry(connection, 1, "/album/bilde01", false, "VFR at Arctic Circle", "This is the VFR up north", "https://www.bang.priv.no/sb/pics/moto/vfr96/acirc1.jpg", "https://www.bang.priv.no/sb/pics/moto/vfr96/icons/acirc1.gif");
    }

    private void addAlbumEntry(Connection connection, int parent, String path, boolean album, String title, String description, String imageUrl, String thumbnailUrl) throws Exception {
        var sql = "insert into albumentries (parent, localpath, album, title, description, imageurl, thumbnailurl) values (?, ?, ?, ?, ?, ?, ?)";
        try(var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parent);
            statement.setString(2, path);
            statement.setBoolean(3, album);
            statement.setString(4, title);
            statement.setString(5, description);
            statement.setString(6, imageUrl);
            statement.setString(7, thumbnailUrl);
            statement.executeUpdate();
        }
    }

    private DataSource createDataSource(String dbname) throws Exception {
        var properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:" + dbname + ";create=true");
        return derbyDataSourceFactory.createDataSource(properties);
    }

}
