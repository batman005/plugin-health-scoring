/*
 * MIT License
 *
 * Copyright (c) 2023 Jenkins Infra
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jenkins.pluginhealth.scoring.probes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Map;

import io.jenkins.pluginhealth.scoring.model.Plugin;
import io.jenkins.pluginhealth.scoring.model.ProbeResult;
import io.jenkins.pluginhealth.scoring.model.ResultStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContinuousDeliveryProbeTest {
    @Test
    void shouldNotRequireRelease() {
        final ContinuousDeliveryProbe probe = spy(ContinuousDeliveryProbe.class);
        assertThat(probe.requiresRelease()).isFalse();
    }

    @Test
    void shouldKeepUsingJEP229Key() {
        final ContinuousDeliveryProbe probe = spy(ContinuousDeliveryProbe.class);
        assertThat(probe.key()).isEqualTo("jep-229");
    }

    @Test
    void shouldBeAbleToDetectRepositoryWithNoGHA() throws Exception {
        final Plugin plugin = mock(Plugin.class);
        final ProbeContext ctx = mock(ProbeContext.class);
        final ContinuousDeliveryProbe probe = new ContinuousDeliveryProbe();

        when(plugin.getDetails()).thenReturn(Map.of(
            SCMLinkValidationProbe.KEY, new ProbeResult(SCMLinkValidationProbe.KEY, "", ResultStatus.SUCCESS, ZonedDateTime.now().minusMinutes(5))
        ));
        when(ctx.getScmRepository()).thenReturn(Files.createTempDirectory("foo"));

        final ProbeResult result = probe.apply(plugin, ctx);
        assertThat(result.status()).isEqualTo(ResultStatus.FAILURE);
        assertThat(result.message()).isEqualTo("Plugin has no GitHub Action configured");
    }

    @Test
    void shouldBeAbleToDetectNotConfiguredRepository() throws Exception {
        final Plugin plugin = mock(Plugin.class);
        final ProbeContext ctx = mock(ProbeContext.class);
        final ContinuousDeliveryProbe probe = new ContinuousDeliveryProbe();

        when(plugin.getDetails()).thenReturn(Map.of(
            SCMLinkValidationProbe.KEY, new ProbeResult(SCMLinkValidationProbe.KEY, "", ResultStatus.SUCCESS, ZonedDateTime.now().minusMinutes(5))
        ));
        final Path repo = Files.createTempDirectory("foo");
        Files.createDirectories(repo.resolve(".github/workflows"));
        when(ctx.getScmRepository()).thenReturn(repo);

        final ProbeResult result = probe.apply(plugin, ctx);
        assertThat(result.status()).isEqualTo(ResultStatus.FAILURE);
        assertThat(result.message()).isEqualTo("Could not find JEP-229 workflow definition");
    }

    @Test
    void shouldBeAbleToDetectConfiguredRepository() throws Exception {
        final Plugin plugin = mock(Plugin.class);
        final ProbeContext ctx = mock(ProbeContext.class);
        final ContinuousDeliveryProbe probe = new ContinuousDeliveryProbe();

        when(plugin.getDetails()).thenReturn(Map.of(
            SCMLinkValidationProbe.KEY, new ProbeResult(SCMLinkValidationProbe.KEY, "", ResultStatus.SUCCESS, ZonedDateTime.now().minusMinutes(5))
        ));
        final Path repo = Files.createTempDirectory("foo");
        final Path workflows = Files.createDirectories(repo.resolve(".github/workflows"));
        Files.createFile(workflows.resolve("cd.yml"));
        when(ctx.getScmRepository()).thenReturn(repo);

        final ProbeResult result = probe.apply(plugin, ctx);
        assertThat(result.status()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(result.message()).isEqualTo("JEP-229 workflow definition found");
    }

    @Test
    void shouldBeAbleToDetectConfiguredRepositoryWithLongExtension() throws Exception {
        final Plugin plugin = mock(Plugin.class);
        final ProbeContext ctx = mock(ProbeContext.class);
        final ContinuousDeliveryProbe probe = new ContinuousDeliveryProbe();

        when(plugin.getDetails()).thenReturn(Map.of(
            SCMLinkValidationProbe.KEY, new ProbeResult(SCMLinkValidationProbe.KEY, "", ResultStatus.SUCCESS, ZonedDateTime.now().minusMinutes(5))
        ));
        final Path repo = Files.createTempDirectory("foo");
        final Path workflows = Files.createDirectories(repo.resolve(".github/workflows"));
        Files.createFile(workflows.resolve("cd.yaml"));
        when(ctx.getScmRepository()).thenReturn(repo);

        final ProbeResult result = probe.apply(plugin, ctx);
        assertThat(result.status()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(result.message()).isEqualTo("JEP-229 workflow definition found");
    }
}
