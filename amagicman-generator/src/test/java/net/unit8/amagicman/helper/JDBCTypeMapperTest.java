package net.unit8.amagicman.helper;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;

import static org.assertj.core.api.Assertions.assertThat;

class JDBCTypeMapperTest {
    @Test
    void customType() {
        JDBCTypeMapper mapper = new JDBCTypeMapper();
        mapper.setVendorType("ARRAY", JDBCType.ARRAY);
        mapper.setJDBCType(JDBCType.ARRAY, Object[].class);
        assertThat(mapper.getJavaType("ARRAY"))
                .isEqualTo(Object[].class);
    }
}