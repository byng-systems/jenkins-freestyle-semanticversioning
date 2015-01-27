/*
 * The MIT License
 *
 * Copyright 2015 M.D.Ward <matthew.ward@byng-systems.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.byng.versioningplugin.updater;

import com.github.zafarkhaja.semver.Version;
import hudson.EnvVars;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersionNumberUpdater {
    
    public Version incrementSingleVersionComponent(Version currentVersion, String component) {
        
        if (component.equals(VersionComponent.MAJOR)) {
            return currentVersion.incrementMajorVersion();
            
        } else if (component.equals(VersionComponent.MINOR)) {
            return currentVersion.incrementMinorVersion();
            
        } else if (component.equals(VersionComponent.PATCH)) {
            return currentVersion.incrementPatchVersion();
        }
        
        return currentVersion;
    }
    
    public Version updateMajorBasedOnEnvironmentVariable(
        Version currentVersion,
        EnvVars environment,
        String envVariableName
    ) throws Exception, NumberFormatException {
        int diff = this.getVersionDiffFromEnvVariable(
            environment,
            envVariableName,
            currentVersion.getMajorVersion()
        );
        
        for (int i = 0; i < diff; i++) {
            currentVersion = currentVersion.incrementMajorVersion();
        }
        
        return currentVersion;
    }
    
    public Version updateMinorBasedOnEnvironmentVariable(
        Version currentVersion,
        EnvVars environment,
        String envVariableName
    ) throws Exception, NumberFormatException {
        int diff = this.getVersionDiffFromEnvVariable(
            environment,
            envVariableName,
            currentVersion.getMinorVersion()
        );
        
        for (int i = 0; i < diff; i++) {
            currentVersion = currentVersion.incrementMinorVersion();
        }
        
        return currentVersion;
    }
    
    protected int getVersionDiffFromEnvVariable(
        EnvVars environment,
        String envVariableName,
        int currentComponentVersion
    ) throws Exception, NumberFormatException {
        if (envVariableName != null && !environment.containsKey(envVariableName)) {
            throw new Exception(
                "Environment variable '" + envVariableName + "' is not set in the current context"
            );
        }
        
        return Integer.valueOf(environment.get(envVariableName)) - currentComponentVersion;
    }
    
    public Version setPreReleaseVersion(Version currentVersion, String preRelease) {
        
        if (preRelease.equals(PreReleaseVersion.NONE)) {
            return Version.valueOf(currentVersion.getNormalVersion());
        }
        
        return currentVersion.setPreReleaseVersion(preRelease);
    }
    
    
    
    public static class VersionComponent {
        public static final String NONE = "";
        public static final String MAJOR = "major";
        public static final String MINOR = "minor";
        public static final String PATCH = "patch";
    }
    
    public static class PreReleaseVersion {
        public static final String NONE = "";
        public static final String ALPHA = "alpha";
        public static final String BETA = "beta";
        public static final String RELEASE_CANDIDATE = "rc";
        public static final String NIGHTLY = "nightly";
        public static final String BUILD = "build";
    }
    
}
