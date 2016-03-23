package co.byng.versioningplugin;
import co.byng.versioningplugin.configuration.OptionsProvider;
import co.byng.versioningplugin.configuration.VersioningConfiguration;
import co.byng.versioningplugin.configuration.VersioningConfigurationProvider;
import co.byng.versioningplugin.configuration.VersioningConfigurationWriteableProvider;
import co.byng.versioningplugin.configuration.VersioningGlobalConfiguration;
import co.byng.versioningplugin.configuration.VersioningGlobalConfigurationProvider;
import co.byng.versioningplugin.configuration.VersioningGlobalConfigurationWriteableProvider;
import co.byng.versioningplugin.handler.VersionCommittable;
import co.byng.versioningplugin.handler.VersionRetrievable;
import co.byng.versioningplugin.service.FileAbsolutePathProvider;
import co.byng.versioningplugin.service.LazyLoadingServiceFactory;
import co.byng.versioningplugin.service.ServiceFactory;
import co.byng.versioningplugin.service.TokenExpansionProvider;
import co.byng.versioningplugin.versioning.VersionNumberUpdater;
import co.byng.versioningplugin.versioning.VersionFactory;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link VersionNumberBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class VersionNumberBuilder extends Builder implements VersioningConfigurationProvider {

    protected VersioningConfigurationWriteableProvider configuration;
    protected ServiceFactory serviceFactory;
    protected transient VersionNumberUpdater updater;
    protected transient VersionRetrievable retriever;
    protected transient VersionCommittable committer;
    protected transient VersionFactory versionFactory;
    protected transient TokenExpansionProvider tokenExpander;
    
    
    
    public VersionNumberBuilder(VersioningConfigurationWriteableProvider configuration) throws IllegalArgumentException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be given as null");
        }

        this.configuration = configuration;
        this.serviceFactory = new LazyLoadingServiceFactory(new FileAbsolutePathProvider());
    }
    
    @DataBoundConstructor
    public VersionNumberBuilder(
        boolean doOverrideVersion,
        String overrideVersion,
        String propertyFilePath,
        boolean baseMajorOnEnvVariable,
        String majorEnvVariable,
        boolean baseMinorOnEnvVariable,
        String minorEnvVariable,
        String preReleaseVersion,
        String preReleaseSuffix,
        String fieldToIncrement,
        boolean doEnvExport,
        boolean doSetNameOrDescription,
        String newBuildName,
        String newBuildDescription
    ) throws IllegalArgumentException {
        this(
            new VersioningConfiguration()
                .setDoOverrideVersion(doOverrideVersion)
                .setOverrideVersion(overrideVersion)
                .setPropertyFilePath(propertyFilePath)
                .setBaseMajorOnEnvVariable(baseMajorOnEnvVariable)
                .setMajorEnvVariable(majorEnvVariable)
                .setBaseMinorOnEnvVariable(baseMinorOnEnvVariable)
                .setMinorEnvVariable(minorEnvVariable)
                .setPreReleaseVersion(preReleaseVersion)
                .setPreReleaseSuffix(preReleaseSuffix)
                .setFieldToIncrement(fieldToIncrement)
                .setDoEnvExport(doEnvExport)
                .setDoSetNameOrDescription(doSetNameOrDescription)
                .setNewBuildName(newBuildName)
                .setNewBuildDescription(newBuildDescription)
        );
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PrintStream logger = listener.getLogger();
        
        try {
            this.lazyLoadServices(build.getProject());
            VariableExporter varExporter = this.serviceFactory.createVarExporter(null);
            
            Version currentVersion;
            if (this.getDoOverrideVersion()) {
                currentVersion = this.versionFactory.buildVersionFromString(this.getOverrideVersion());
                this.committer.saveVersion(currentVersion);

                if (this.getTemporaryOverride()) {
                    this.configuration
                        .setDoOverrideVersion(false)
                        .setOverrideVersion(null)
                    ;
                }

            } else {
                currentVersion = this.retriever.loadVersion();
            }

            EnvVars environment = build.getEnvironment(listener);
            Version previousVersion = currentVersion;

            currentVersion = this.updater.incrementSingleVersionComponent(
                currentVersion,
                this.getFieldToIncrement()
            );

            if (this.getBaseMajorOnEnvVariable()) {
                currentVersion = this.updater.updateMajorBasedOnEnvironmentVariable(
                    currentVersion,
                    environment,
                    this.getMajorEnvVariable()
                );
            }

            if (this.getBaseMinorOnEnvVariable()) {
                currentVersion = this.updater.updateMinorBasedOnEnvironmentVariable(
                    currentVersion,
                    environment,
                    this.getMinorEnvVariable()
                );
            }
            
            currentVersion = this.performSetPreReleaseVersion(
                build,
                listener,
                currentVersion
            );
            
            logger.append("Updating to " + currentVersion + "\n");
            this.committer.saveVersion(currentVersion);
            
            if (this.getDoEnvExport()) {
                varExporter.addVariableToExport(
                    this.getDescriptor().getPreviousVersionEnvVariable(),
                    previousVersion.toString()
                );
                
                varExporter.addVariableToExport(
                    this.getDescriptor().getCurrentVersionEnvVariable(),
                    currentVersion.toString()
                );
                
                varExporter.export(build);
            }
            
            this.performSetBuildNameOrDescription(build, listener);
            
            return true;

        } catch (Throwable t) {
            t.printStackTrace(logger);
        }

        return false;
    }
    
    protected Version performSetPreReleaseVersion(
        final AbstractBuild build,
        final BuildListener listener,
        final Version currentVersion
    ) throws IOException {
        final String preReleaseVersion = this.getPreReleaseVersion();
        
        if (preReleaseVersion != null) {
            final String preReleaseSuffix = this.getPreReleaseSuffix();

            return this.updater.setPreReleaseVersion(
                currentVersion,
                new StringBuilder()
                    .append(preReleaseVersion)
                    .append(
                        preReleaseSuffix != null && !preReleaseSuffix.isEmpty()
                        ? this.tokenExpander.expand(preReleaseSuffix, build, listener)
                        : ""
                    )
                    .toString()
            );
        }

        return currentVersion;
    }
    
    protected void performSetBuildNameOrDescription(
        final AbstractBuild build,
        final BuildListener listener
    ) throws IOException {
        if (this.getDoSetNameOrDescription() == false) {
            return;
        }
        
        String newBuildName = this.getNewBuildName();
        if (
            newBuildName != null
            && !(newBuildName = newBuildName.trim()).isEmpty()
        ) {
            String tokenisedBuildName = this.tokenExpander.expand(
                newBuildName,
                build,
                listener
            );
            
            if (tokenisedBuildName != null) {
                build.setDisplayName(tokenisedBuildName);
            }
        }
        
        String newBuildDescription = this.getNewBuildDescription();
        if (
            newBuildDescription != null
            && !(newBuildDescription = newBuildDescription.trim()).isEmpty()
        ) {
            String tokenisedBuildDescription = this.tokenExpander.expand(
                newBuildDescription,
                build,
                listener
            );
            
            if (tokenisedBuildDescription != null) {
                build.setDescription(tokenisedBuildDescription);
            }
        }
    }
    
    protected void lazyLoadServices(AbstractProject project) throws IOException {
        if (this.serviceFactory == null) {
            this.serviceFactory = new LazyLoadingServiceFactory(new FileAbsolutePathProvider());
        }
        
        String propertyFilePath = this.getPropertyFilePath();
        
        this.committer = this.serviceFactory.createCommitter(
            project,
            propertyFilePath,
            this.committer
        );
        
        this.retriever = this.serviceFactory.createRetriever(
            project,
            propertyFilePath,
            this.retriever
        );
        
        this.updater = this.serviceFactory.createUpdater(this.updater);
        
        this.versionFactory = this.serviceFactory.createVersionFactory(this.versionFactory);
        
        this.tokenExpander = this.serviceFactory.createTokenExpansionProvider(this.tokenExpander);
    }

    public VersioningConfigurationWriteableProvider getConfiguration() {
        return this.configuration;
    }

    public ServiceFactory getServiceFactory() {
        return this.serviceFactory;
    }
    
    public VersionNumberUpdater getUpdater() {
        return this.updater;
    }

    public VersionCommittable getCommitter() {
        return this.committer;
    }
    
    public VersionRetrievable getRetriever() {
        return this.retriever;
    }
    
    public VersionFactory getVersionFactory() {
        return this.versionFactory;
    }

    public TokenExpansionProvider getTokenExpander() {
        return tokenExpander;
    }

    public void setConfiguration(VersioningConfigurationWriteableProvider configuration) {
        this.configuration = configuration;
    }
    
    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public void setUpdater(VersionNumberUpdater updater) {
        this.updater = updater;
    }
    
    public void setCommitter(VersionCommittable committer) {
        this.committer = committer;
    }

    public void setRetriever(VersionRetrievable retriever) {
        this.retriever = retriever;
    }

    public void setVersionFactory(VersionFactory versionFactory) {
        this.versionFactory = versionFactory;
    }

    public void setTokenExpander(TokenExpansionProvider tokenExpander) {
        this.tokenExpander = tokenExpander;
    }
    
    @Override
    public boolean getDoOverrideVersion() {
        return this.configuration.getDoOverrideVersion();
    }

    @Override
    public String getOverrideVersion() {
        return this.configuration.getOverrideVersion();
    }

    @Override
    public boolean getTemporaryOverride() {
        return this.configuration.getTemporaryOverride();
    }

    @Override
    public String getPropertyFilePath() {
        return this.configuration.getPropertyFilePath();
    }

    @Override
    public boolean getBaseMajorOnEnvVariable() {
        return this.configuration.getBaseMajorOnEnvVariable();
    }

    @Override
    public String getMajorEnvVariable() {
        return this.configuration.getMajorEnvVariable();
    }

    @Override
    public boolean getBaseMinorOnEnvVariable() {
        return this.configuration.getBaseMinorOnEnvVariable();
    }

    @Override
    public String getMinorEnvVariable() {
        return this.configuration.getMinorEnvVariable();
    }

    @Override
    public String getPreReleaseVersion() {
        return this.configuration.getPreReleaseVersion();
    }

    @Override
    public String getPreReleaseSuffix() {
        return this.configuration.getPreReleaseSuffix();
    }

    @Override
    public String getFieldToIncrement() {
        return this.configuration.getFieldToIncrement();
    }

    @Override
    public boolean getDoEnvExport() {
        return this.configuration.getDoEnvExport();
    }

    @Override
    public boolean getDoSetNameOrDescription() {
        return this.configuration.getDoSetNameOrDescription();
    }

    @Override
    public String getNewBuildName() {
        return this.configuration.getNewBuildName();
    }

    @Override
    public String getNewBuildDescription() {
        return this.configuration.getNewBuildDescription();
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link VersionNumberBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> implements VersioningGlobalConfigurationProvider {

        private VersioningGlobalConfigurationWriteableProvider globalConfiguration;
        private OptionsProvider optionsProvider;
    
        public DescriptorImpl(
            VersioningGlobalConfigurationWriteableProvider globalConfiguration,
            OptionsProvider optionsProvider
        ) {
            this.setGlobalConfiguration(globalConfiguration);
            this.setOptionsProvider(optionsProvider);
            
            load();
        }
        
        public DescriptorImpl() {
            this(
                new VersioningGlobalConfiguration(),
                new OptionsProvider()
            );
        }
        
        /**
         * 
         * @param aClass
         * @return 
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public FormValidation doCheckOverrideVersion(@QueryParameter String overrideVersion) {
            try {
                Version.valueOf(overrideVersion);
            } catch (ParseException ex) {
                return FormValidation.error("Please enter a valid semantic version string");
            }
            
            return FormValidation.ok();
        }
        
        /**
         * Returns the human readable name is used in the configuration screen
         * @return human readable name is used in the configuration screen
         */
        @Override
        public String getDisplayName() {
            return "Update versioning";
        }

        public VersioningGlobalConfigurationWriteableProvider getGlobalConfiguration() {
            return globalConfiguration;
        }
        
        public void setGlobalConfiguration(VersioningGlobalConfigurationWriteableProvider globalConfiguration) throws IllegalArgumentException {
            if (globalConfiguration == null) {
                throw new IllegalArgumentException("Global configuration cannot be given as a null object");
            }
            
            this.globalConfiguration = globalConfiguration;
        }

        public OptionsProvider getOptionsProvider() {
            return optionsProvider;
        }

        public void setOptionsProvider(OptionsProvider optionsProvider) throws IllegalArgumentException {
            if (optionsProvider == null) {
                throw new IllegalArgumentException("Global configuration cannot be given as a null object");
            }
            
            this.optionsProvider = optionsProvider;
        }
    
        @Override
        public String getPreviousVersionEnvVariable() {
            return this.globalConfiguration.getPreviousVersionEnvVariable();
        }

        @Override
        public String getCurrentVersionEnvVariable() {
            return this.globalConfiguration.getCurrentVersionEnvVariable();
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

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            
            this.globalConfiguration
                    .setPreviousVersionEnvVariable(formData.getString("previousVersionEnvVariable"))
                    .setCurrentVersionEnvVariable(formData.getString("currentVersionEnvVariable"))
            ;
            
            save();
            
            return super.configure(req,formData);
        }

    }
}
