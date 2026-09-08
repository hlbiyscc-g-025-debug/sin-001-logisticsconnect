package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AlertBotTest {

    @Test
    void shouldAlertWhenStageReachesThreshold() {
        assertTrue(AlertBotApp.shouldAlert(5));
    }

    @Test
    void shouldAlertWhenStageIsAboveThreshold() {
        assertTrue(AlertBotApp.shouldAlert(6));
    }

    @Test
    void shouldNotAlertWhenStageIsBelowThreshold() {
        assertFalse(AlertBotApp.shouldAlert(4));
    }

    @Test
    void shouldNotAlertAtStageZero() {
        assertFalse(AlertBotApp.shouldAlert(0));
    }

    @Test
    void shouldProcessStageMessage() {
        AlertBotApp.processStageMessage("{\"stage\":6}");
    }
}
