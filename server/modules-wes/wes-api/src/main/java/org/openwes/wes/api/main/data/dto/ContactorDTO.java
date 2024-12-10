package org.openwes.wes.api.main.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactorDTO implements Serializable {
    private String name;
    private String tel;
    private String mail;
    private String fax;
}
