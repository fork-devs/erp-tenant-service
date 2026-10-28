package com.erp.tenant.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Automated architectural test for transaction demarcation boundaries.
 *
 * <p>Verifies two core architectural invariants:
 * <ol>
 *   <li>Persistence adapters must not declare their own write transactions.</li>
 *   <li>Use cases writing to repositories must execute within a UnitOfWork boundary.</li>
 * </ol>
 */
class TransactionDemarcationArchitectureTest {

    private static final Path ADAPTERS = Paths.get(
            "src/main/java/com/erp/tenant/infrastructure/persistence/adapter");
    private static final Path APLICACION = Paths.get(
            "src/main/java/com/erp/tenant/application");

    /** Non-read-only transactional demarcation declaration. */
    private static final Pattern TRANSACCIONAL = Pattern.compile("@Transactional\\b");

    /** Read-only transactional demarcation declaration. */
    private static final Pattern SOLO_LECTURA = Pattern.compile(
            "@Transactional\\s*\\(\\s*readOnly\\s*=\\s*true\\s*\\)");

    /** A write operation to a repository port. */
    private static final Pattern ESCRITURA = Pattern.compile(
            "\\b(\\w+RepositoryPort)\\s*\\.\\s*(save|deleteById|revoke\\w*|invalidate\\w*|update\\w*|mark\\w*)\\s*\\(");

    @Test
    @DisplayName("Ningun adaptador de persistencia declara demarcacion propia")
    void ningunAdaptadorDeclaraSuPropiaDemarcacion() throws IOException {
        List<String> infracciones = new ArrayList<>();

        for (Path fichero : ficherosJava(ADAPTERS)) {
            List<String> lineas = Files.readAllLines(fichero, StandardCharsets.UTF_8);
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i);
                if (!TRANSACCIONAL.matcher(linea).find()) {
                    continue;
                }
                if (SOLO_LECTURA.matcher(linea).find()) {
                    continue;
                }
                infracciones.add(nombre(fichero) + ":" + (i + 1) + "  " + linea.trim());
            }
        }

        if (!infracciones.isEmpty()) {
            fail("Los adaptadores de persistencia no deben declarar demarcacion propia: "
                    + "se unen a la transaccion que abre la capa de aplicacion.\n"
                    + "Un @Transactional en un adaptador hace que cada escritura se confirme por "
                    + "separado, y una operacion que necesita ser atomica deja de serlo.\n"
                    + String.join("\n", infracciones));
        }
    }

    @Test
    @DisplayName("Ningun caso de uso escribe sin pasar por el limite transaccional")
    void ningunCasoDeUsoEscribeFueraDelLimite() throws IOException {
        List<String> infracciones = new ArrayList<>();

        for (Path fichero : ficherosJava(APLICACION)) {
            String contenido = Files.readString(fichero, StandardCharsets.UTF_8);

            Set<String> puertosEscritos = new LinkedHashSet<>();
            Matcher matcher = ESCRITURA.matcher(contenido);
            while (matcher.find()) {
                puertosEscritos.add(matcher.group(1));
            }

            if (puertosEscritos.isEmpty()) {
                continue;
            }
            if (contenido.contains("unitOfWork")) {
                continue;
            }

            infracciones.add(nombre(fichero) + "  escribe en " + String.join(", ", puertosEscritos)
                    + " sin usar el limite transaccional");
        }

        if (!infracciones.isEmpty()) {
            fail("Todo caso de uso que escriba debe usar el puerto UnitOfWork: "
                    + "los adaptadores se unen a la transaccion en curso y no abren una propia.\n"
                    + String.join("\n", infracciones));
        }
    }

    private List<Path> ficherosJava(Path directorio) throws IOException {
        if (!Files.isDirectory(directorio)) {
            fail("No se encuentra el directorio " + directorio.toAbsolutePath()
                    + ". El test debe ejecutarse desde la raiz del modulo.");
        }
        try (Stream<Path> ficheros = Files.walk(directorio)) {
            return ficheros
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private String nombre(Path fichero) {
        return directorioRaiz().relativize(fichero.toAbsolutePath()).toString();
    }

    private Path directorioRaiz() {
        return Paths.get("").toAbsolutePath();
    }
}
