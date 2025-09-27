package org.example.java_cli_app.commands;

import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "list", description = "Lists issues by status")
public class ListCommand implements Runnable {

    private final IssueServiceFacade issueServiceFacade;

    public ListCommand(IssueServiceFacade issueServiceFacade) {
        this.issueServiceFacade = issueServiceFacade;
    }

    @CommandLine.Parameters(index = "0", description = "Status (OPEN, IN_PROGRESS, CLOSED)")
    private Status status;

    @Override
    public void run() {
        issueServiceFacade.listIssues(status);
    }
}
