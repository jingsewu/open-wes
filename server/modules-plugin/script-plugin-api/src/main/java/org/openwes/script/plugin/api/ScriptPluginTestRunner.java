package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;

/** Executes one declared script method for real on the hosting node. */
public interface ScriptPluginTestRunner {
    TestRunResultDTO run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request);
}
