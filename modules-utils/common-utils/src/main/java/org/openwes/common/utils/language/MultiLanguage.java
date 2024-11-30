package org.openwes.common.utils.language;

import com.google.common.collect.Maps;
import org.openwes.common.utils.language.core.LanguageContext;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class MultiLanguage {

    private Map<String, String> languages;

    public MultiLanguage(Map<String, String> languages) {
        this.languages = languages;
    }

    public MultiLanguage(String language, String description) {
        if (this.languages == null) {
            languages = Maps.newHashMap();
        }
        this.languages.put(language, description);
    }

    public void put(String lang, String value) {
        languages.put(lang, value);
    }

    public String get(String lang) {
        return languages.get(lang);
    }

    public String get() {
        String lang = LanguageContext.getLanguage();
        return languages.get(lang);
    }
}
