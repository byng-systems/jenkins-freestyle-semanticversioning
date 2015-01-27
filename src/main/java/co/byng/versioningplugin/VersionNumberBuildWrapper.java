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
package co.byng.versioningplugin;

import co.byng.versioningplugin.configuration.VersioningConfiguration;
import co.byng.versioningplugin.configuration.VersioningConfigurationProvider;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersionNumberBuildWrapper extends BuildWrapper implements VersioningConfigurationProvider {

    protected VersionNumberBuilder builder;

    public VersionNumberBuildWrapper(VersionNumberBuilder builder) {
        this.builder = builder;
    }
    
    @DataBoundConstructor
    public VersionNumberBuildWrapper(
        boolean doOverrideVersion,
        String overrideVersion,
        String propertyFilePath,
        boolean baseMajorOnEnvVariable,
        String majorEnvVariable,
        boolean baseMinorOnEnvVariable,
        String minorEnvVariable,
        String preReleaseVersion,
        String fieldToIncrement
    ) throws IllegalArgumentException {
        this(
            new VersionNumberBuilder(
                new VersioningConfiguration()
                    .setDoOverrideVersion(doOverrideVersion)
                    .setOverrideVersion(overrideVersion)
                    .setPropertyFilePath(propertyFilePath)
                    .setBaseMajorOnEnvVariable(baseMajorOnEnvVariable)
                    .setBaseMinorOnEnvVariable(baseMinorOnEnvVariable)
                    .setMinorEnvVariable(minorEnvVariable)
                    .setPreReleaseVersion(preReleaseVersion)
                    .setFieldToIncrement(fieldToIncrement)
            )
        );
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                return getBuilder().perform(build, null, listener);
            }
        };
    }
    
    public VersionNumberBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(VersionNumberBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    public boolean getDoOverrideVersion() {
        return this.builder.getDoOverrideVersion();
    }

    @Override
    public String getOverrideVersion() {
        return this.builder.getOverrideVersion();
    }

    @Override
    public String getPropertyFilePath() {
        return this.builder.getPropertyFilePath();
    }

    @Override
    public boolean getBaseMajorOnEnvVariable() {
        return this.builder.getBaseMajorOnEnvVariable();
    }

    @Override
    public String getMajorEnvVariable() {
        return this.builder.getMajorEnvVariable();
    }

    @Override
    public boolean getBaseMinorOnEnvVariable() {
        return this.builder.getBaseMinorOnEnvVariable();
    }

    @Override
    public String getMinorEnvVariable() {
        return this.builder.getMinorEnvVariable();
    }

    @Override
    public String getPreReleaseVersion() {
        return this.builder.getPreReleaseVersion();
    }

    @Override
    public String getFieldToIncrement() {
        return this.builder.getFieldToIncrement();
    }

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        return super.getDescriptor(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Update versioning before build";
        }

        @Override
        public String getConfigPage() {
            return getViewPage(VersionNumberBuilder.class, "config");
        }
        
        @Override
        public String getGlobalConfigPage() {
            return getViewPage(VersionNumberBuilder.class, "global");
        }
        
    }
    
}
