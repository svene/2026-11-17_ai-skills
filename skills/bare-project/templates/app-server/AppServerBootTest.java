package @PACKAGE@;

import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Checks that the application boots. */
@QuarkusTest
class AppServerBootTest {

    @Test
    void boots() {
        assertThat(Arc.container().isRunning()).isTrue();
    }
}
