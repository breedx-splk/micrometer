/*
 * Copyright 2021 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.tck;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeterRegistryAssertTests {

    SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry();
    
    MeterRegistryAssert meterRegistryAssert = new MeterRegistryAssert(simpleMeterRegistry);

    @Test
    void assertionErrorThrownWhenMetricsArePresent() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").register(this.simpleMeterRegistry));

        assertThatThrownBy(() -> meterRegistryAssert.hasNoMetrics())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected no metrics, but got metrics with following names <matching-metric-name>");
    }

    @Test
    void assertionErrorThrownWhenNoTimerUsingAssertThat() {
        assertThatThrownBy(() -> MeterRegistryAssert.assertThat(simpleMeterRegistry).hasTimerWithName("foo"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Expected a timer with name <foo> but found none");
    }

    @Test
    void assertionErrorThrownWhenNoTimerUsingThen() {
        assertThatThrownBy(() -> MeterRegistryAssert.then(simpleMeterRegistry).hasTimerWithName("foo"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Expected a timer with name <foo> but found none");
    }

    @Test
    void assertionErrorThrownWhenNoTimer() {
        assertThatThrownBy(() -> meterRegistryAssert.hasTimerWithName("foo"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Expected a timer with name <foo> but found none");
    }
    
    @Test
    void assertionErrorThrownWhenTimerPresentButWrongTagKeys() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("notmatching-tag", "baz").register(this.simpleMeterRegistry));
        
        assertThatThrownBy(() -> meterRegistryAssert.hasTimerWithNameAndTagKeys("matching-metric-name", "non-existent-tag"))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Expected a timer with name <matching-metric-name> and tag keys <non-existent-tag> but found none");
    }
    
    @Test
    void assertionErrorThrownWhenTimerPresentButWrongTagValue() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "not-matching-value").register(this.simpleMeterRegistry));
        
        assertThatThrownBy(() -> meterRegistryAssert.hasTimerWithNameAndTags("matching-metric-name", Tags.of("matching-tag", "some-value")))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Expected a timer with name <matching-metric-name> and tags <[tag(matching-tag=some-value)]> but found none");
    }

    @Test
    void assertionErrorThrownWhenTimerFound() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").register(this.simpleMeterRegistry));

        assertThatThrownBy(() -> meterRegistryAssert.doesNotHaveTimerWithName("matching-metric-name"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected no timer with name <matching-metric-name> but found one with tags <[]>");
    }

    @Test
    void assertionErrorThrownWhenTimerPresentWithTagKeys() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "baz").register(this.simpleMeterRegistry));

        assertThatThrownBy(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTagKeys("matching-metric-name", "matching-tag"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected a timer with name <matching-metric-name> and tag keys <matching-tag> but found one");
    }

    @Test
    void assertionErrorThrownWhenTimerPresentWithTagValue() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "matching-value").register(this.simpleMeterRegistry));

        assertThatThrownBy(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTags("matching-metric-name", Tags.of("matching-tag", "matching-value")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected no timer with name <matching-metric-name> and tags <[tag(matching-tag=matching-value)]> but found one");
    }

    @Test
    void assertionErrorThrownWhenTimerPresentWithCommonTagValue() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "matching-value").register(this.simpleMeterRegistry));

        assertThatThrownBy(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTags("matching-metric-name", io.micrometer.common.Tags.of("matching-tag", "matching-value")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected no timer with name <matching-metric-name> and tags <[tag(matching-tag=matching-value)]> but found one");
    }
    
    @Test
    void noAssertionErrorThrownWhenNoMetricsRegistered() {
        assertThatCode(() -> meterRegistryAssert.hasNoMetrics())
            .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerPresent() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("foo").register(this.simpleMeterRegistry));

        assertThatCode(() -> meterRegistryAssert.hasTimerWithName("foo"))
            .doesNotThrowAnyException();
    }
    
    @Test
    void noAssertionErrorThrownWhenTimerWithTagKeysPresent() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "baz").register(this.simpleMeterRegistry));
        
        assertThatCode(() -> meterRegistryAssert.hasTimerWithNameAndTagKeys("matching-metric-name", "matching-tag"))
            .doesNotThrowAnyException();
    }
    
    @Test
    void noAssertionErrorThrownWhenTimerWithTagPresent() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "matching-value").register(this.simpleMeterRegistry));
        
        assertThatCode(() -> meterRegistryAssert.hasTimerWithNameAndTags("matching-metric-name", Tags.of("matching-tag", "matching-value")))
            .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerWithCommonTagPresent() {
        Timer.start(this.simpleMeterRegistry).stop(Timer.builder("matching-metric-name").tag("matching-tag", "matching-value").register(this.simpleMeterRegistry));

        assertThatCode(() -> meterRegistryAssert.hasTimerWithNameAndTags("matching-metric-name", io.micrometer.common.Tags.of("matching-tag", "matching-value")))
            .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerMissing() {
        assertThatCode(() -> meterRegistryAssert.doesNotHaveTimerWithName("foo"))
                .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerWithTagsMissing() {
        assertThatCode(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTags("foo", Tags.of(Tag.of("bar", "baz"))))
                .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerWithCommonTagsMissing() {
        assertThatCode(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTags("foo", io.micrometer.common.Tags.of(io.micrometer.common.Tag.of("bar", "baz"))))
                .doesNotThrowAnyException();
    }

    @Test
    void noAssertionErrorThrownWhenTimerWithTagKeysMissing() {
        assertThatCode(() -> meterRegistryAssert.doesNotHaveTimerWithNameAndTagKeys("foo", "bar"))
                .doesNotThrowAnyException();
    }

}
