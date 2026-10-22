package org.openwes.ai.plugin.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.constants.AppCodeEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;

@Getter
@AllArgsConstructor
public enum AiPluginErrorDescEnum implements IBaseError {

    AI_QUOTA_EXCEEDED("PL001001", "daily AI draft limit of {0} reached for user {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_PLUGIN_NOT_GENERATABLE("PL001002",
            "plugin {0} is {1}; only DRAFT or DISABLED plugins can receive an AI draft",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_BASE_REVISION_NOT_FOUND("PL001003", "base revision {0} does not belong to plugin {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_UNSUPPORTED_EXTENSION_TYPE("PL001004", "extension type {0} is not supported for app {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_CORPUS_UNAVAILABLE("PL001005",
            "the plugin contract corpus needs a VectorStore; configure Pinecone or leave grounding disabled",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_GENERATION_FAILED("PL001006", "AI draft generation failed after {0} attempts: {1}",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AI_INTERNAL_ERROR("PL001999", "AI draft generation failed: {0}",
            AppCodeEnum.PLUGIN_PLATFORM.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
