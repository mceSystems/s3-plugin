package hudson.plugins.s3;

import hudson.model.Fingerprint;
import hudson.model.FingerprintMap;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import hudson.Functions;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.Serializable;

@ExportedBean
public class FingerprintRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean produced;
    private final String md5sum;
    private final S3Artifact artifact;
    private boolean keepForever;
    private boolean showDirectlyInBrowser;
    private final S3Profile profile;
    private final RunDetails runDetails;

    public FingerprintRecord(boolean produced, String bucket, String name, String region, String md5sum, RunDetails runDetails, S3Profile profile) {
        this.produced = produced;
        this.artifact = new S3Artifact(region, bucket, name);
        this.md5sum = md5sum;
        this.showDirectlyInBrowser = false;
        this.keepForever = false;
        this.runDetails = runDetails;
        this.profile = profile;
    }

    Fingerprint addRecord(Run<?, ?> run) throws IOException {
        final FingerprintMap map = Jenkins.getInstance().getFingerprintMap();
        return map.getOrCreate(produced ? run : null, artifact.getName(), md5sum);
    }

    public boolean isKeepForever() {
        return keepForever;
    }

    public void setKeepForever(boolean keepForever) {
        this.keepForever = keepForever;
    }

    public boolean isShowDirectlyInBrowser() {
        return showDirectlyInBrowser;
    }

    public void setShowDirectlyInBrowser(boolean showDirectlyInBrowser) {
        this.showDirectlyInBrowser = showDirectlyInBrowser;
    }

    @Exported
    public String getName() {
        return artifact.getName();
    }

    @Exported
    public String getLink() {
        final S3Profile s3 = this.profile;
        if (s3 == null) {
            //Chrome and IE convert backslash in the URL into forward slashes, need escape with %5c
            return artifact.getName().replace("\\","%5C");
        }

        final AmazonS3Client client = s3.getClient(this.getArtifact().getRegion());
        final String url = getDownloadURL(client, s3.getSignedUrlExpirySeconds(), runDetails, this);

        return url;
    }

    @Exported
    public String getFingerprint() {
        return md5sum;
    }

    @Exported
    public S3Artifact getArtifact() {
        return artifact;
    }

    private String getDownloadURL(AmazonS3Client client, int signedUrlExpirySeconds, RunDetails runDetails, FingerprintRecord record) {
        final Destination dest = Destination.newFromRunDetails(runDetails, record.getArtifact());
        final GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(dest.bucketName, dest.objectName);
        request.setExpiration(new Date(System.currentTimeMillis() + signedUrlExpirySeconds*1000));

        if (!record.isShowDirectlyInBrowser()) {
            // let the browser use the last part of the name, not the full path
            // when saving.
            final ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
            final String fileName = (new File(dest.objectName)).getName().trim();
            headers.setContentDisposition("attachment; filename=\"" + fileName + '"');
            request.setResponseHeaders(headers);
        }

        return client.generatePresignedUrl(request).toExternalForm();
    }
}
