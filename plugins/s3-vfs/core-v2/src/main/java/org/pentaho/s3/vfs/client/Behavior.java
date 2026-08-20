package org.pentaho.s3.vfs.client;

import software.amazon.awssdk.regions.Region;

public class Behavior {
  private static final boolean OVERRIDE_REGION = true;

  private static final boolean PREFER_ENV_CREDENTIALS = true;

  /** Override region from environment even  */
  public boolean overrideRegionFromEnv() {
    return OVERRIDE_REGION;
  }

  /**
   * Whether a change in the {@code aws.accessKeyId} / {@code aws.secretKey} system properties
   * replaces a client that was already built from configuration.
   * <p>
   * Hardcoded to the pre-existing behaviour: the cached values start out unset, so on the first
   * call any system property that <em>is</em> set counts as a change and the configured client is
   * discarded in favour of one built from the environment. Whether that is intended has not been
   * decided; this flag isolates it so the answer can change in one place.
   */
  public boolean preferEnvCredentials() {
    return PREFER_ENV_CREDENTIALS;
  }

  public Region defaultRegion() {
    return Region.US_EAST_1;
  }

  // TODO: check old URLs on error?

  // TODO: error on bad tls?
}
