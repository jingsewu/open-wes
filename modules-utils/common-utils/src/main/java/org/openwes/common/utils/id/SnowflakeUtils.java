package org.openwes.common.utils.id;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SnowflakeUtils {

    private static Snowflake snowflake;

    @Autowired
    public SnowflakeUtils(Snowflake snowflake) {
        SnowflakeUtils.snowflake = snowflake;
    }

    public static long generateId() {
        return snowflake.nextId();
    }

}
