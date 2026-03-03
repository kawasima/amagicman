package net.unit8.amagicman.helper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps vendor-specific SQL type names to JDBC types, and JDBC types to Java classes.
 *
 * <p>The mapping is performed in two stages:</p>
 * <ol>
 *   <li>Vendor type string (e.g. {@code "VARCHAR2"}, {@code "DATETIME"}) →
 *       {@link JDBCType} — covers common aliases used by MySQL, PostgreSQL,
 *       H2, Oracle, and SQL Server.</li>
 *   <li>{@link JDBCType} → Java class (e.g. {@link JDBCType#VARCHAR} →
 *       {@link String}, {@link JDBCType#BIGINT} → {@link Long}).</li>
 * </ol>
 *
 * <p>Custom mappings can be added at runtime via {@link #setVendorType} and
 * {@link #setJDBCType}. Unknown vendor types fall back to {@link JDBCType#OTHER},
 * which maps to {@link Object}.</p>
 *
 * @author kawasima
 */
public class JDBCTypeMapper {
    private final Map<String, JDBCType> vendor2jdbc = new HashMap<>();
    private final Map<JDBCType, Class<?>> jdbc2java = new HashMap<>();

    /**
     * Constructs a {@code JDBCTypeMapper} pre-populated with a comprehensive set of
     * vendor type aliases and standard JDBC-to-Java mappings.
     */
    public JDBCTypeMapper() {
        vendor2jdbc.put("INT", JDBCType.INTEGER);
        vendor2jdbc.put("INTEGER", JDBCType.INTEGER);
        vendor2jdbc.put("MEDIUMINT", JDBCType.INTEGER);
        vendor2jdbc.put("INT4", JDBCType.INTEGER);
        vendor2jdbc.put("SIGNED", JDBCType.INTEGER);
        vendor2jdbc.put("BOOLEAN", JDBCType.BOOLEAN);
        vendor2jdbc.put("BIT", JDBCType.BOOLEAN);
        vendor2jdbc.put("BOOL", JDBCType.BOOLEAN);
        vendor2jdbc.put("TINYINT", JDBCType.TINYINT);
        vendor2jdbc.put("SMALLINT", JDBCType.SMALLINT);
        vendor2jdbc.put("INT2", JDBCType.SMALLINT);
        vendor2jdbc.put("YEAR", JDBCType.SMALLINT);
        vendor2jdbc.put("BIGINT", JDBCType.BIGINT);
        vendor2jdbc.put("INT8", JDBCType.BIGINT);
        vendor2jdbc.put("IDENTITY", JDBCType.BIGINT);
        vendor2jdbc.put("DECIMAL", JDBCType.DECIMAL);
        vendor2jdbc.put("NUMBER", JDBCType.DECIMAL);
        vendor2jdbc.put("DEC", JDBCType.DECIMAL);
        vendor2jdbc.put("NUMERIC", JDBCType.DECIMAL);
        vendor2jdbc.put("DOUBLE", JDBCType.DOUBLE);
        vendor2jdbc.put("FLOAT8", JDBCType.DOUBLE);
        vendor2jdbc.put("FLOAT", JDBCType.FLOAT);
        vendor2jdbc.put("REAL", JDBCType.FLOAT);
        vendor2jdbc.put("TIME", JDBCType.TIME);
        vendor2jdbc.put("DATE", JDBCType.DATE);
        vendor2jdbc.put("TIMESTAMP", JDBCType.TIMESTAMP);
        vendor2jdbc.put("DATETIME", JDBCType.TIMESTAMP);
        vendor2jdbc.put("SMALLDATETIME", JDBCType.TIMESTAMP);
        vendor2jdbc.put("BINARY", JDBCType.BINARY);
        vendor2jdbc.put("VARBINARY", JDBCType.BINARY);
        vendor2jdbc.put("BINARY VARYING", JDBCType.BINARY);
        vendor2jdbc.put("LONGVARBINARY", JDBCType.BINARY);
        vendor2jdbc.put("RAW", JDBCType.BINARY);
        vendor2jdbc.put("BYTEA", JDBCType.BINARY);
        vendor2jdbc.put("OTHER", JDBCType.OTHER);
        vendor2jdbc.put("VARCHAR", JDBCType.VARCHAR);
        vendor2jdbc.put("CHARACTER VARYING", JDBCType.VARCHAR);
        vendor2jdbc.put("LONGVARCHAR", JDBCType.VARCHAR);
        vendor2jdbc.put("VARCHAR2", JDBCType.VARCHAR);
        vendor2jdbc.put("NVARCHAR", JDBCType.VARCHAR);
        vendor2jdbc.put("NVARCHAR2", JDBCType.VARCHAR);
        vendor2jdbc.put("VARCHAR_CASESENSITIVE", JDBCType.VARCHAR);
        vendor2jdbc.put("CHAR", JDBCType.CHAR);
        vendor2jdbc.put("CHARACTER", JDBCType.CHAR);
        vendor2jdbc.put("NCHAR", JDBCType.CHAR);
        vendor2jdbc.put("BLOB", JDBCType.BLOB);
        vendor2jdbc.put("BINARY LARGE OBJECT", JDBCType.BLOB);
        vendor2jdbc.put("TINYBLOB", JDBCType.BLOB);
        vendor2jdbc.put("MEDIUMBLOB", JDBCType.BLOB);
        vendor2jdbc.put("LONGBLOB", JDBCType.BLOB);
        vendor2jdbc.put("IMAGE", JDBCType.BLOB);
        vendor2jdbc.put("OID", JDBCType.BLOB);
        vendor2jdbc.put("CLOB", JDBCType.CLOB);
        vendor2jdbc.put("CHARACTER LARGE OBJECT", JDBCType.CLOB);
        vendor2jdbc.put("TINYTEXT", JDBCType.CLOB);
        vendor2jdbc.put("TEXT", JDBCType.CLOB);
        vendor2jdbc.put("MEDIUMTEXT", JDBCType.CLOB);
        vendor2jdbc.put("LONGTEXT", JDBCType.CLOB);
        vendor2jdbc.put("NTEXT", JDBCType.CLOB);
        vendor2jdbc.put("NCLOB", JDBCType.CLOB);

        jdbc2java.put(JDBCType.INTEGER, Integer.class);
        jdbc2java.put(JDBCType.BOOLEAN, Boolean.class);
        jdbc2java.put(JDBCType.TINYINT, Byte.class);
        jdbc2java.put(JDBCType.SMALLINT, Short.class);
        jdbc2java.put(JDBCType.BIGINT, Long.class);
        jdbc2java.put(JDBCType.DECIMAL, BigDecimal.class);
        jdbc2java.put(JDBCType.DOUBLE, Double.class);
        jdbc2java.put(JDBCType.REAL, Float.class);
        jdbc2java.put(JDBCType.TIME, Time.class);
        jdbc2java.put(JDBCType.DATE, Date.class);
        jdbc2java.put(JDBCType.TIMESTAMP, Timestamp.class);
        jdbc2java.put(JDBCType.BINARY, byte[].class);
        jdbc2java.put(JDBCType.OTHER, Object.class);
        jdbc2java.put(JDBCType.VARCHAR, String.class);
        jdbc2java.put(JDBCType.CHAR, String.class);
        jdbc2java.put(JDBCType.BLOB, Blob.class);
        jdbc2java.put(JDBCType.CLOB, Clob.class);
    }

    /**
     * Returns the Java class corresponding to the given vendor-specific SQL type name.
     *
     * <p>The lookup proceeds in two steps: vendor type → {@link JDBCType}, then
     * {@link JDBCType} → Java class. If the vendor type is unknown it falls back to
     * {@link JDBCType#OTHER}, which maps to {@link Object}.</p>
     *
     * @param vendorType the vendor-specific SQL type name (case-sensitive, e.g. {@code "VARCHAR"})
     * @return the corresponding Java class; never {@code null}
     */
    public Class<?> getJavaType(String vendorType) {
        JDBCType jdbcType = vendor2jdbc.getOrDefault(vendorType, JDBCType.OTHER);
        return jdbc2java.getOrDefault(jdbcType, Object.class);
    }

    /**
     * Registers or overrides a mapping from a vendor type name to a {@link JDBCType}.
     *
     * @param vendorType the vendor-specific SQL type name to register (case-sensitive)
     * @param jdbcType   the {@link JDBCType} to associate with {@code vendorType}
     */
    public void setVendorType(String vendorType, JDBCType jdbcType) {
        vendor2jdbc.put(vendorType, jdbcType);
    }

    /**
     * Registers or overrides a mapping from a {@link JDBCType} to a Java class.
     *
     * @param jdbcType the JDBC type whose mapping is to be set
     * @param javaType the Java class to associate with {@code jdbcType}
     */
    public void setJDBCType(JDBCType jdbcType, Class<?> javaType) {
        jdbc2java.put(jdbcType, javaType);
    }
}
