package com.cpf.foundation.time;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** 외부 시간 동기화 상태를 포함하는 비민감 Base 시간 Snapshot입니다. */
public record CpfTimeSnapshot(
        Instant utcInstant,
        ZoneId zoneId,
        ZonedDateTime zonedDateTime,
        long monotonicNanos,
        Duration estimatedSkew,
        boolean skewWithinPolicy) { }
