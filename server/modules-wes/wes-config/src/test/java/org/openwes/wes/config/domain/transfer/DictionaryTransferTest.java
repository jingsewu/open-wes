package org.openwes.wes.config.domain.transfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.config.domain.entity.Dictionary;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryTransferTest {

    @BeforeEach
    void setUp() {
        LanguageContext.setLanguage("en-US");
    }

    @AfterEach
    void tearDown() {
        LanguageContext.remove();
    }

    @Test
    void resolveLabel_customLabelTakesPriority() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("en-US", "System EN", "zh-CN", "系统中文")));
        item.setCustomLabel(new MultiLanguage(Map.of("en-US", "Custom EN")));

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("Custom EN");
    }

    @Test
    void resolveLabel_fallsBackToSystemLabel_whenNoCustom() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("en-US", "System EN", "zh-CN", "系统中文")));
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("System EN");
    }

    @Test
    void resolveLabel_fallsBackToZhCN_whenCurrentLangMissing() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("zh-CN", "系统中文")));
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("系统中文");
    }

    @Test
    void resolveLabel_returnsEmpty_whenBothNull() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(null);
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEmpty();
    }
}
