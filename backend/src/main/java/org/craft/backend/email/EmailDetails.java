package org.craft.backend.email;

import lombok.Data;

@Data
public class EmailDetails {
    private String recipient;
    private String subject;
    private String body;
}
