package com.contrastsecurity.core;

import com.contrastsecurity.core.dto.DependencyDto;
import com.contrastsecurity.core.dto.ScaVulnerabilityDto;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public final class ScaUtil {
    private static final String AUDIT_RESULTS_FILE = "audit-results.json";

    private ScaUtil() {}

    public static void initiateAndManageScaAudit(final OrganizationConfig selectedOrganizationConfig, final String filePath, final JLabel scaOutputLabel) {
        final String teamServerUrl = selectedOrganizationConfig.getTeamServerUrl();
        final String host = teamServerUrl.replace("/Contrast/api", "");
        final String apiKey = selectedOrganizationConfig.getApiKey();
        final String orgId = selectedOrganizationConfig.getUuid();
        final String username = selectedOrganizationConfig.getUsername();
        final String serviceKey = selectedOrganizationConfig.getServiceKey();
        final String base64AuthHeader = Base64.getEncoder().encodeToString((username + ":" + serviceKey).getBytes());

        final String auditArgs = String.format(" audit -f %s --api-key %s --authorization %s --organization-id %s --host %s --save-results", filePath, apiKey, base64AuthHeader, orgId, host);

        final InputStream binaryFileStream = ScaUtil.class.getResourceAsStream("/cliMac");

        try {
            // Create temporary directory for contrast binary and JSON output
            final File tempDirForAudit = createTempDirectory();

            // Create temporary file path for contrast binary and copy the binary to it
            final File binaryFile = new File(tempDirForAudit, "cliMac");
            binaryFile.deleteOnExit();
            copyInputStreamToFile(binaryFileStream, binaryFile);

            // Make the binary file executable (if necessary)
            binaryFile.setExecutable(true);

            // Execute the binary file from the temporary directory
            Process contrastAuditProcess = Runtime.getRuntime().exec(binaryFile.getAbsolutePath() + auditArgs, null, tempDirForAudit);

            contrastAuditProcess.onExit().thenRun(handleAuditResults(scaOutputLabel, tempDirForAudit, contrastAuditProcess));
        } catch (IOException e) {
            // Handle any exceptions that occur during process execution
        }
    }

    private static Runnable handleAuditResults(final JLabel scaOutputLabel, final File tempDir, final Process contrastAuditProcess) {
        return () -> {
            final int exitCode = contrastAuditProcess.exitValue();

            if (exitCode == 0) {
                final File resultsOutputFile = new File(tempDir, AUDIT_RESULTS_FILE);

                try {
                    final List<DependencyDto> dependencies = new ObjectMapper().readValue(resultsOutputFile, new TypeReference<List<DependencyDto>>() {
                    });

                    StringBuilder output = new StringBuilder();

                    dependencies.forEach(dependencyDto -> {
                        final Map<Boolean, List<ScaVulnerabilityDto>> partitions = dependencyDto.getVulnerabilities().stream()
                                .filter(vulnerability -> vulnerability.getSeverity().equals("HIGH") || vulnerability.getSeverity().equals("CRITICAL"))
                                .collect(Collectors.partitioningBy(vulnerability -> vulnerability.getSeverity().equals("HIGH")));

                        final int numberOfHigh = partitions.get(true).size();
                        final int numberOfCritical = partitions.get(false).size();

                        output.append("<b>" + dependencyDto.getArtifactName() + "</b>");
                        output.append(": has " + numberOfHigh + " high and " + numberOfCritical + " critical vulnerabilities. The closest stable version is " + dependencyDto.getRemediationAdvice().getClosestStableVersion());
                        output.append("<br>");
                    });

                    scaOutputLabel.setText("<html>" + output.toString() + "</html>");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                scaOutputLabel.setText("SCA Audit Failed with non-zero exit code. Please check your configuration or contact support.");
            }
        };
    }

    private static File createTempDirectory() throws IOException {
        Path tempDirPath = Files.createTempDirectory("temp");
        File tempDir = tempDirPath.toFile();
        tempDir.deleteOnExit();
        return tempDir;
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }
}