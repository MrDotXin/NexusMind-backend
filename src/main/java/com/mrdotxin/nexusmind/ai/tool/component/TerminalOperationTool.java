package com.mrdotxin.nexusmind.ai.tool.component;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = null;
            if (isWindows()) {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                process = builder.start();
            } else {
                process = Runtime.getRuntime().exec(ArrayUtils.toArray(command));
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }


    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}

