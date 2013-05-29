package org.sonar.ide.intellij.model;

import org.apache.commons.lang.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/7/13
 * Time: 9:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Version implements Comparable<Version> {
    private int major;
    private int minor;
    private int incrementalVersion;

    public Version(int major, int minor, int incrementalVersion) {
        this.major = major;
        this.minor = minor;
        this.incrementalVersion = incrementalVersion;
    }

    public Version(int major, int minor) {
        this(major, minor, 0);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getIncrementalVersion() {
        return incrementalVersion;
    }

    public static Version parse(String version) {

        String[] split = version.trim().split("\\.");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid version number expecting xx.xx.xx, but got [" + version + "]");
        }
        int incremental = (split.length == 3) ? parseNumber(split[2]) : 0;
        return new Version(parseNumber(split[0]), parseNumber(split[1]), incremental);
    }

    private static int parseNumber(String number) {
        if (StringUtils.isEmpty(number) || !StringUtils.isNumeric(number)) {
            throw new IllegalArgumentException("Version number elements must be a positive numeric value, but was [" + number + "]");
        }

        int versionItem = Integer.parseInt(number);

        if (versionItem < 0) {
            throw new IllegalArgumentException("Version number elements must be a positive numeric value, but was [" + number + "]");
        }

        return versionItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (incrementalVersion != version.incrementalVersion) return false;
        if (major != version.major) return false;
        if (minor != version.minor) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + incrementalVersion;
        return result;
    }

    @Override
    public int compareTo(Version version) {

        if (major != version.major) {
            return major - version.major;
        }

        if (minor != version.minor) {
            return minor - version.minor;
        }


        if (incrementalVersion != version.incrementalVersion) {
            return incrementalVersion - version.incrementalVersion;
        }

        return 0;
    }

    public String getVersionAsString() {
        return String.format("%s.%s.%s", major, minor, incrementalVersion);
    }
}
