package org.example.java_cli_app.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;


@Component
@CommandLine.Command(name = "create", description = "Creates a new issue at Google Spreadsheet")
public class CreateCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Create command");
    }
}
