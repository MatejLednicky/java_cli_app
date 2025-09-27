package org.example.java_cli_app.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "update", description = "Updates an existing issue")
public class UpdateCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Update command");
    }
}
