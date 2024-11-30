package org.openwes.wes.api.basic.dto;

import static org.openwes.wes.api.basic.constants.PtlColorEnum.BLUE;
import static org.openwes.wes.api.basic.constants.PtlColorEnum.GRAY;
import static org.openwes.wes.api.basic.constants.PtlColorEnum.GREEN;
import static org.openwes.wes.api.basic.constants.PtlModeEnum.FLASH;
import static org.openwes.wes.api.basic.constants.PtlModeEnum.ON;
import static org.openwes.wes.api.basic.constants.PtlUpdownEnum.TAPABLE;
import static org.openwes.wes.api.basic.constants.PtlUpdownEnum.UNTAPABLE;

import org.openwes.wes.api.basic.constants.PtlColorEnum;
import org.openwes.wes.api.basic.constants.PtlModeEnum;
import org.openwes.wes.api.basic.constants.PtlUpdownEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
public class PutWallTagConfigDTO implements Serializable {

    private LightConfig waitingBinding;
    private LightConfig dispatch;
    private LightConfig waitingSeal;
    private LightConfig disabled;
    private LightConfig selected;
    private LightConfig optional;

    public void initialize() {
        waitingBinding = new LightConfig(BLUE, ON, UNTAPABLE);
        dispatch = new LightConfig(GREEN, ON, TAPABLE);
        waitingSeal = new LightConfig(BLUE, FLASH, TAPABLE);
        disabled = new LightConfig(GRAY);
        selected = new LightConfig(GREEN);
        optional = new LightConfig(BLUE);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LightConfig implements Serializable {

        private PtlColorEnum color;

        private PtlModeEnum mode;

        private PtlUpdownEnum updown;

        public LightConfig(PtlColorEnum color) {
            this.color = color;
        }
    }
}
