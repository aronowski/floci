package io.github.hectorvent.floci.testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs a short local process to its end, for a test of a script Floci writes, and reaps it however the
 * test goes: one that outlives its bound fails the test and is killed.
 */
public final class ShortProcesses {

    private ShortProcesses() {
    }

    /** How a process ended: its exit code, and what it wrote, its standard error merged into its output. */
    public record Ended(int exit, String output) {
    }

    /** Runs {@code builder}'s process, which must end within five seconds, its error merged into its output. */
    public static Ended run(ProcessBuilder builder) throws IOException, InterruptedException {
        Process process = builder.redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(5, TimeUnit.SECONDS), "the process must end");
            return new Ended(process.exitValue(),
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "test process must be reaped");
            }
        }
    }
}
