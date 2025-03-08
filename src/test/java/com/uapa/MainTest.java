package com.uapa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class MainTest {

    @Test
    void testMainSchedulesRunnable() {
        // Use Mockito's inline static mocking for SwingUtilities.
        try (MockedStatic<SwingUtilities> mockedSwing = mockStatic(SwingUtilities.class)) {
            // Call the main method.
            Main.main(new String[0]);
            // Verify that SwingUtilities.invokeLater was called exactly once with any
            // Runnable.
            mockedSwing.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)), times(1));
        }
    }

    @Test
    void testMainRunnableExecution() {
        // Capture the Runnable passed to SwingUtilities.invokeLater.
        AtomicReference<Runnable> capturedRunnable = new AtomicReference<>();
        try (MockedStatic<SwingUtilities> mockedSwing = mockStatic(SwingUtilities.class)) {
            // When invokeLater is called, capture the Runnable.
            mockedSwing.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        Runnable r = invocation.getArgument(0);
                        capturedRunnable.set(r);
                        return null;
                    });
            // Call main.
            Main.main(new String[0]);
        }
        // Ensure that a Runnable was captured.
        Runnable runnable = capturedRunnable.get();
        assertNotNull(runnable, "The main method should schedule a Runnable via SwingUtilities.invokeLater().");
        // Execute the captured Runnable.
        // (In a headless test environment, the view might not be truly displayed.)
        assertDoesNotThrow(runnable::run, "Executing the scheduled Runnable should not throw any exceptions.");
    }
}
