package com.conductor.shared.workflow;

/**
 * Supported workflow trigger mechanisms.
 */
public enum TriggerType {
    EVENT,
    API,
    WEBHOOK,
    SCHEDULE,
    TIMER,
    MANUAL
}
