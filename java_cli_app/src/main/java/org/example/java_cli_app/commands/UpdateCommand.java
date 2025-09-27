package org.example.java_cli_app.commands;

import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "update", description = "Updates the status of an existing issue")
public class UpdateCommand implements Runnable {

    private final IssueServiceFacade issueServiceFacade;

    public UpdateCommand(IssueServiceFacade issueServiceFacade) {
        this.issueServiceFacade = issueServiceFacade;
    }

    @CommandLine.Parameters(index = "0", description = "Issue ID")
    private String issueId;

    @CommandLine.Parameters(index = "1", description = "Status (OPEN, IN_PROGRESS, CLOSED)")
    private Status status;

    @Override
    public void run() {
        issueServiceFacade.updateIssue(issueId, status);
    }
}
