package hudson.plugins.s3;

import java.io.Serializable;
import hudson.model.Run;

public class RunDetails implements Serializable {
    private String fullName;
    private String name;
    private int buildId;

    public RunDetails(Run<?, ?> run) {
        this.fullName = run.getParent().getFullName();
        this.name = run.getParent().getName();

        this.buildId = run.getNumber();
    }

    public int getBuildId() {
        return this.buildId;
    }

    public String getProjectName(boolean enableFullpath) {
        return enableFullpath ? this.fullName : this.name;
    }
}
