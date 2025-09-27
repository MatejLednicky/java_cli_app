package org.example.java_cli_app.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "list", description = "Lists issues by status")
public class ListCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("List command");
    }
}
