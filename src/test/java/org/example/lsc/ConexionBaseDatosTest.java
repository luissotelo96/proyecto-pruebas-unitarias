package org.example.lsc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ConexionBaseDatosTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void conexion_estaActivaYRespondeConsulta() throws Exception {
        try (Connection conexion = dataSource.getConnection()) {
            assertFalse(conexion.isClosed());
            assertTrue(conexion.isValid(2));

            try (ResultSet resultado = conexion.createStatement().executeQuery("SELECT 1")) {
                assertTrue(resultado.next());
                assertTrue(resultado.getInt(1) >= 1);
            }
        }
    }
}
