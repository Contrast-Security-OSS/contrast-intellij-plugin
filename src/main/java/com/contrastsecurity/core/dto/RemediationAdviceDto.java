package com.contrastsecurity.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RemediationAdviceDto {
    private String closestStableVersion;
    private String latestStableVersion;
}
