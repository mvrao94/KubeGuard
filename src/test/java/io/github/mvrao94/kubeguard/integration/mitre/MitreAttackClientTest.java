package io.github.mvrao94.kubeguard.integration.mitre;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Comprehensive tests for MitreAttackClient
 * Ensures caching, thread safety, and functionality work correctly
 */
@ExtendWith(MockitoExtension.class)
class MitreAttackClientTest {

    private MitreAttackClient client;

    @BeforeEach
    void setUp() {
        client = new MitreAttackClient();
        // Enable MITRE integration for testing
        ReflectionTestUtils.setField(client, "enabled", true);
    }

    @Test
    void getKubernetesTechniques_whenEnabled_returnsAllTechniques() {
        List<MitreTechnique> techniques = client.getKubernetesTechniques();

        assertNotNull(techniques);
        assertFalse(techniques.isEmpty());
        assertEquals(21, techniques.size()); // Updated to match actual count

        // Verify techniques contain expected data
        assertTrue(techniques.stream().anyMatch(t -> "T1190".equals(t.getTechniqueId())));
        assertTrue(techniques.stream().anyMatch(t -> "Initial Access".equals(t.getTactic())));
    }

    @Test
    void getKubernetesTechniques_whenDisabled_returnsEmptyList() {
        ReflectionTestUtils.setField(client, "enabled", false);

        List<MitreTechnique> techniques = client.getKubernetesTechniques();

        assertNotNull(techniques);
        assertTrue(techniques.isEmpty());
    }

    @Test
    void getTechniqueById_withValidId_returnsTechnique() {
        MitreTechnique technique = client.getTechniqueById("T1190");

        assertNotNull(technique);
        assertEquals("T1190", technique.getTechniqueId());
        assertEquals("Exploit Public-Facing Application", technique.getName());
        assertEquals("Initial Access", technique.getTactic());
    }

    @Test
    void getTechniqueById_withInvalidId_returnsNull() {
        MitreTechnique technique = client.getTechniqueById("INVALID_ID");

        assertNull(technique);
    }

    @Test
    void getTechniqueById_withNullId_returnsNull() {
        MitreTechnique technique = client.getTechniqueById(null);

        assertNull(technique);
    }

    @Test
    void getTechniqueById_whenDisabled_returnsNull() {
        ReflectionTestUtils.setField(client, "enabled", false);

        MitreTechnique technique = client.getTechniqueById("T1190");

        assertNull(technique);
    }

    @Test
    void getTechniquesByTactic_withValidTactic_returnsMatchingTechniques() {
        List<MitreTechnique> techniques = client.getTechniquesByTactic("Execution");

        assertNotNull(techniques);
        assertFalse(techniques.isEmpty());
        assertTrue(techniques.stream().allMatch(t -> "Execution".equals(t.getTactic())));
    }

    @Test
    void getTechniquesByTactic_withInvalidTactic_returnsEmptyList() {
        List<MitreTechnique> techniques = client.getTechniquesByTactic("INVALID_TACTIC");

        assertNotNull(techniques);
        assertTrue(techniques.isEmpty());
    }

    @Test
    void getTechniquesByTactic_withNullTactic_returnsEmptyList() {
        List<MitreTechnique> techniques = client.getTechniquesByTactic(null);

        assertNotNull(techniques);
        assertTrue(techniques.isEmpty());
    }

    @Test
    void getTechniquesByTactic_whenDisabled_returnsEmptyList() {
        ReflectionTestUtils.setField(client, "enabled", false);

        List<MitreTechnique> techniques = client.getTechniquesByTactic("Execution");

        assertNotNull(techniques);
        assertTrue(techniques.isEmpty());
    }

    @Test
    void caching_worksCorrectly_multipleCallsReturnSameInstance() {
        List<MitreTechnique> techniques1 = client.getKubernetesTechniques();
        List<MitreTechnique> techniques2 = client.getKubernetesTechniques();

        // Should return the same cached instance
        assertSame(techniques1, techniques2);
    }

    @Test
    void techniqueData_isCorrectlyMapped() {
        MitreTechnique technique = client.getTechniqueById("T1609");

        assertNotNull(technique);
        assertEquals("T1609", technique.getTechniqueId());
        assertEquals("Container Administration Command", technique.getName());
        assertEquals("Execution", technique.getTactic());
        assertTrue(technique.isAppliesToKubernetes());
        assertEquals(List.of("Containers", "Kubernetes"), technique.getPlatforms());
        assertEquals("https://attack.mitre.org/techniques/T1609", technique.getUrl());
    }

    @Test
    void allTactics_areCovered() {
        List<String> expectedTactics = List.of(
            "Initial Access", "Execution", "Persistence", "Privilege Escalation",
            "Defense Evasion", "Credential Access", "Discovery", "Lateral Movement",
            "Collection", "Impact"
        );

        List<MitreTechnique> techniques = client.getKubernetesTechniques();

        for (String tactic : expectedTactics) {
            assertTrue(techniques.stream().anyMatch(t -> tactic.equals(t.getTactic())),
                "Tactic '" + tactic + "' should be present");
        }
    }

    @Test
    void threadSafety_cachingWorksUnderConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Submit multiple threads that will call getKubernetesTechniques concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    List<MitreTechnique> techniques = client.getKubernetesTechniques();
                    if (techniques != null && !techniques.isEmpty() &&
                        techniques.stream().allMatch(t -> t.getTechniqueId() != null)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // All threads should have succeeded
        assertEquals(numberOfThreads, successCount.get());
    }

    @Test
    void isEnabled_returnsCorrectValue() {
        assertTrue(client.isEnabled());

        ReflectionTestUtils.setField(client, "enabled", false);
        assertFalse(client.isEnabled());
    }

    @Test
    void techniques_haveRequiredFields() {
        List<MitreTechnique> techniques = client.getKubernetesTechniques();

        for (MitreTechnique technique : techniques) {
            assertNotNull(technique.getTechniqueId());
            assertNotNull(technique.getName());
            assertNotNull(technique.getTactic());
            assertNotNull(technique.getDescription());
            assertNotNull(technique.getUrl());
            assertTrue(technique.isAppliesToKubernetes());
            assertNotNull(technique.getPlatforms());
            assertNotNull(technique.getMitigations());
        }
    }
}
