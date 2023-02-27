package com.contrastsecurity.core;

import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.intellij.openapi.util.SystemInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public final class ScaUtil {
    private ScaUtil() {}

    public static Process downloadAndRunContrastCli(final OrganizationConfig selectedOrganizationConfig, final String filePath) {
        final String CLI_URL;
        String FILE_NAME = "contrastBinary";

        if (SystemInfo.isMac) {
            CLI_URL = "https://pkg.contrastsecurity.com/artifactory/cli/1.0.23/mac/contrast";
        } else if (SystemInfo.isLinux) {
            CLI_URL = "https://pkg.contrastsecurity.com/artifactory/cli/1.0.23/linux/contrast";
        } else {
            FILE_NAME = "contrastBinary.exe";
            CLI_URL = "https://pkg.contrastsecurity.com/artifactory/cli/1.0.23/windows/contrast";
        }

        try {
            final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(CLI_URL).openStream());

            final FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);

            final FileChannel fileChannel = fileOutputStream.getChannel();

            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final File contrastBinary = new File(FILE_NAME);
        final boolean isExecutable = contrastBinary.setExecutable(true);

        final String apiKey = selectedOrganizationConfig.getApiKey();
        final String orgId = selectedOrganizationConfig.getUuid();
        final String authHeader = selectedOrganizationConfig.getAuthHeader();

        final Runtime runtime = Runtime.getRuntime();
        try {
            if (SystemInfo.isWindows) {
                return runtime.exec("contrastBinary.exe audit -f " + filePath + " --api-key " + apiKey + " --authorization " + authHeader + " --organization-id " + orgId);
            } else {
                return runtime.exec("./contrastBinary audit -f " + filePath + " --api-key " + apiKey + " --authorization " + authHeader + " --organization-id " + orgId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
