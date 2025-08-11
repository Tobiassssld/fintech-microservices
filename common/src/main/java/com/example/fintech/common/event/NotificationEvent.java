package com.example.fintech.common.event;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String userId;
    private String title;
    private String message;
    private String type;
    private LocalDateTime timestamp;
}
