
package com.contrastsecurity.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyDto {

    private String uuid;
    private String groupName;
    private String artifactName;
    private String version;
    private String fileName;
    private String libraryLanguage;
    private String severity;
    private String latestVersion;
    private List<ScaVulnerabilityDto> vulnerabilities;

    private RemediationAdviceDto remediationAdvice;
}