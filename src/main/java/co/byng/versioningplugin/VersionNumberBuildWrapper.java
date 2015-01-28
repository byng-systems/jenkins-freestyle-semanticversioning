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

import co.byng.versioningplugin.configuration.OptionsProvider;
import co.byng.versioningplugin.configuration.VersioningConfiguration;
import co.byng.versioningplugin.configuration.VersioningConfigurationProvider;
import co.byng.versioningplugin.configuration.VersioningGlobalConfiguration;
import co.byng.versioningplugin.configuration.VersioningGlobalConfigurationProvider;
import co.byng.versioningplugin.configuration.VersioningGlobalConfigurationWriteableProvider;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.EnvironmentContributingAction;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersionNumberBuildWrapper extends BuildWrapper implements VersioningConfigurationProvider {

    protected VersionNumberBuilder builder;

    public VersionNumberBuildWrapper(VersionNumberBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {
        super.makeBuildVariables(build, variables); //To change body of generated methods, choose Tools | Templates.
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
                .setMajorEnvVariable(majorEnvVariable)
                .setBaseMinorOnEnvVariable(baseMinorOnEnvVariable)
                .setMinorEnvVariable(minorEnvVariable)
                .setPreReleaseVersion(preReleaseVersion)
                .setFieldToIncrement(fieldToIncrement)
            )
        );
    }

    @Override
    public Environment setUp(
        final AbstractBuild build,
        final Launcher launcher,
        final BuildListener listener
    ) throws IOException, InterruptedException {
        this.builder.perform(build, launcher, listener);

        return new Environment() {};
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
    public boolean getDoEnvExport() {
        return this.builder.getDoEnvExport();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {

        private OptionsProvider optionsProvider;

        public DescriptorImpl(
            OptionsProvider optionsProvider
        ) {
            if (optionsProvider == null) {
                throw new IllegalArgumentException("Global configuration cannot be given as a null object");
            }

            this.optionsProvider = optionsProvider;
        }

        public DescriptorImpl() {
            this(new OptionsProvider());
        }

        public OptionsProvider getOptionsProvider() {
            return optionsProvider;
        }

        public void setOptionsProvider(OptionsProvider optionsProvider) {
            this.optionsProvider = optionsProvider;
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Update versioning before build";
        }

        @Override
        public String getId() {
            return VersionNumberBuilder.class.getName();
        }
        
        @Override
        public String getConfigPage() {
            return getViewPage(VersionNumberBuilder.class, "config.jelly");
        }

        @Override
        public String getGlobalConfigPage() {
            return null;
        }

        public ListBoxModel doFillEnvVariableSubjectFieldItems() {
            return this.optionsProvider.getEnvVariableSubjectFieldItems();
        }

        public ListBoxModel doFillFieldToIncrementItems() {
            return this.optionsProvider.getFieldToIncrementItems();
        }

        public ListBoxModel doFillPreReleaseVersionItems() {
            return this.optionsProvider.getPreReleaseVersionItems();
        }

    }

}
