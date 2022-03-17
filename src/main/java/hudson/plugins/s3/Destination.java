package hudson.plugins.s3;

import hudson.model.Run;

import java.io.Serializable;


/**
 * Provides a way to construct a destination bucket name and object name based
 * on the bucket name provided by the user.
 *
 * The convention implemented here is that a / in a bucket name is used to
 * construct a structure in the object name.  That is, a put of file.txt to bucket name
 * of "mybucket/v1" will cause the object "v1/file.txt" to be created in the mybucket.
 *
 */
public class Destination implements Serializable {
  private static final long serialVersionUID = 1L;
  public final String bucketName;
  public final String objectName;
  public final String s3ObjectLambda;

  public Destination(final String userBucketName, final String fileName, String s3ObjectLambda) {

    if (userBucketName == null || fileName == null)
      throw new IllegalArgumentException("Not defined for null parameters: "+userBucketName+","+fileName);

    final String[] bucketNameArray = userBucketName.split("/", 2);
    final String s3CompatibleFileName = replaceWindowsBackslashes(fileName);

    bucketName = bucketNameArray[0];

    if (bucketNameArray.length > 1) {
        objectName = bucketNameArray[1] + "/" + s3CompatibleFileName;
    } else {
        objectName = s3CompatibleFileName;
    }

    this.s3ObjectLambda = s3ObjectLambda;
  }

  private String replaceWindowsBackslashes(String fileName) {
      return fileName.replace("\\", "/");
  }

  @Override
  public String toString() {
    return "Destination [bucketName="+bucketName+", objectName="+objectName+"]";
  }


  public static Destination newFromRun(Run run, String bucketName, String fileName, boolean enableFullpath, String s3ObjectLambda)
  {
    final String projectName;

    if (enableFullpath) {
      projectName = run.getParent().getFullName();
    }
    else {
      projectName = run.getParent().getName();
    }

    int buildID = run.getNumber();
    return new Destination(bucketName, "jobs/" + projectName + "/" + buildID + "/" + fileName, s3ObjectLambda);
  }

  public static Destination newFromRun(Run run, S3Artifact artifact)
  {
    return newFromRun(run, artifact.getBucket(), artifact.getName(), artifact.useFullProjectName(), artifact.getObjectLambda());
  }

  public static Destination newFromRunDetails(RunDetails runDetails, String bucketName, String fileName, boolean enableFullpath, String s3ObjectLambda) {
    final String projectName = runDetails.getProjectName(enableFullpath);

    int buildID = runDetails.getBuildId();
    return new Destination(bucketName, "jobs/" + projectName + "/" + buildID + "/" + fileName, s3ObjectLambda);
  }

  public static Destination newFromRunDetails(RunDetails runDetails, S3Artifact artifact)
  {
    return newFromRunDetails(runDetails, artifact.getBucket(), artifact.getName(), artifact.useFullProjectName(), artifact.getObjectLambda());
  }
}
