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
import co.byng.versioningplugin.handler.file.AutoCreatingPropertyFileVersionHandler;
import co.byng.versioningplugin.handler.file.PropertyFileIoHandler;
import co.byng.versioningplugin.service.FileAbsolutePathProvider;
import co.byng.versioningplugin.service.LazyLoadingServiceFactory;
import co.byng.versioningplugin.service.ServiceFactory;
import co.byng.versioningplugin.updater.VersionNumberUpdater;
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
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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
    protected transient VariableExporter varExporter;
    
    
    
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
        String fieldToIncrement
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
                .setFieldToIncrement(fieldToIncrement)
        );
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            this.lazyLoadServices(build.getProject());
            
            Version currentVersion;
            
            if (this.getDoOverrideVersion()) {
                currentVersion = Version.valueOf(this.getOverrideVersion());
                this.committer.saveVersion(currentVersion);

                this.configuration
                    .setDoOverrideVersion(false)
                    .setOverrideVersion(null)
                ;
                
            } else {
                currentVersion = this.retriever.loadVersion();
            }

            EnvVars environment = build.getEnvironment(listener);
            
            this.varExporter.addVariableToExport(
                this.getDescriptor().getPreviousVersionEnvVariable(),
                currentVersion.toString()
            );

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
            
            String preReleaseVersion;
            if ((preReleaseVersion = this.getPreReleaseVersion()) != null && !preReleaseVersion.isEmpty()) {
                currentVersion = this.updater.setPreReleaseVersion(currentVersion, preReleaseVersion);
            }
            
            listener.getLogger().append("Updating to " + currentVersion + "\n");
            this.committer.saveVersion(currentVersion);
            
            this.varExporter.addVariableToExport(
                this.getDescriptor().getCurrentVersionEnvVariable(),
                currentVersion.toString()
            );
            
            if (this.getDoEnvExport()) {
                this.varExporter.export(build);
            } else {
                this.varExporter.preventExport(build);
            }
            
            return true;

        } catch (Throwable t) {
            t.printStackTrace(listener.getLogger());
        }

        return false;
    }
    
    protected void lazyLoadServices(AbstractProject project) throws IOException {
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
          
        this.updater = this.serviceFactory.createUpdater(updater);
        
        this.varExporter = this.serviceFactory.createVarExporter(this.varExporter);
    }

    public VersioningConfigurationWriteableProvider getConfiguration() {
        return configuration;
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }
    
    public VersionNumberUpdater getUpdater() {
        return updater;
    }

    public VersionCommittable getCommitter() {
        return committer;
    }

    public VariableExporter getVarExporter() {
        return varExporter;
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

    public void setVarExporter(VariableExporter varExporter) {
        this.varExporter = varExporter;
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
    public String getFieldToIncrement() {
        return this.configuration.getFieldToIncrement();
    }

    @Override
    public boolean getDoEnvExport() {
        return this.configuration.getDoEnvExport();
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
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> implements VersioningGlobalConfigurationProvider {

        private VersioningGlobalConfigurationWriteableProvider globalConfiguration;
        private OptionsProvider optionsProvider;
    
        public DescriptorImpl(
            VersioningGlobalConfigurationWriteableProvider globalConfiguration,
            OptionsProvider optionsProvider
        ) {
            if (globalConfiguration == null) {
                throw new IllegalArgumentException("Global configuration cannot be given as a null object");
            }
            
            if (optionsProvider == null) {
                throw new IllegalArgumentException("Global configuration cannot be given as a null object");
            }
            
            this.globalConfiguration = globalConfiguration;
            this.optionsProvider = optionsProvider;
            
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
        
        public void setGlobalConfiguration(VersioningGlobalConfigurationWriteableProvider globalConfiguration) {
            this.globalConfiguration = globalConfiguration;
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
