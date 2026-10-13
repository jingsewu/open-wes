package org.openwes.script.plugin.api;

import org.openwes.script.engine.ScriptHashes;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Canonical content hash of a revision: declarations (sorted) + script sources + config schema.
 * Two revisions with the same hash are behaviorally identical.
 */
public final class ScriptPluginContentHasher {

    private ScriptPluginContentHasher() {
    }

    public static String hash(List<ExtensionDeclarationDTO> declarations, Map<String, String> scripts) {
        StringBuilder canonical = new StringBuilder();
        declarations.stream()
                .sorted(Comparator.comparing(d -> d.getType().name() + "|" + nullSafe(d.getScript())
                        + "|" + nullSafe(d.getMethod())))
                .forEach(d -> canonical.append(d.getType()).append('|').append(nullSafe(d.getPriority()))
                        .append('|').append(nullSafe(d.getScript())).append('|').append(nullSafe(d.getMethod()))
                        .append('|').append(nullSafe(d.getApiType())).append('|').append(nullSafe(d.getEntityName()))
                        .append('|').append(nullSafe(d.getCustomApiCode())).append('|')
                        .append(nullSafe(d.getHttpMethod())).append('|').append(nullSafe(d.getPath()))
                        .append('|').append(nullSafe(d.getCron())).append('\n'));
        scripts.keySet().stream().sorted().forEach(name ->
                canonical.append(name).append(':').append(scripts.get(name)).append('\n'));
        return ScriptHashes.sha256(canonical.toString());
    }

    private static String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}
