package org.openwes.common.utils.validate;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, ValidationSequence.Extended.class})
public interface ValidationSequence {

    interface Extended {
    }

}
