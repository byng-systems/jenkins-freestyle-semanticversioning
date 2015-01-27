package co.byng.versioningplugin;
import co.byng.versioningplugin.configuration.VersioningConfiguration;
import co.byng.versioningplugin.configuration.VersioningConfigurationProvider;
import co.byng.versioningplugin.handler.VersionCommittingInterface;
import co.byng.versioningplugin.handler.VersionRetrievalInterface;
import co.byng.versioningplugin.handler.file.AutoCreatingPropertyFileVersionHandler;
import co.byng.versioningplugin.handler.file.PropertyFileIoHandler;
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
import hudson.tasks.BuildStepMonitor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.IOException;
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

    protected transient VersionNumberUpdater updater = new VersionNumberUpdater();
    protected transient VersionRetrievalInterface retriever;
    protected transient VersionCommittingInterface committer;
    protected VersioningConfiguration configuration;
    
    
    
    public VersionNumberBuilder(VersioningConfiguration configuration) throws IllegalArgumentException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be given as null");
        }
        
        this.configuration = configuration;
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
                .setBaseMinorOnEnvVariable(baseMinorOnEnvVariable)
                .setMinorEnvVariable(minorEnvVariable)
                .setPreReleaseVersion(preReleaseVersion)
                .setFieldToIncrement(fieldToIncrement)
        );
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            AbstractProject project = build.getProject();
            VersionCommittingInterface committer = this.lazyLoadCommitter(project);
            VersionRetrievalInterface retriever = this.lazyLoadRetriever(project);

            Version currentVersion;

            if (this.getDoOverrideVersion()) {
                currentVersion = Version.valueOf(this.getOverrideVersion());
                committer.saveVersion(currentVersion);

                this.configuration
                    .setDoOverrideVersion(false)
                    .setOverrideVersion(null)
                ;
                
            } else {
                currentVersion = retriever.loadVersion();
            }

            EnvVars environment = build.getEnvironment(listener);
            
            environment.put(
                this.getDescriptor().getPreviousVersionEnvVariable(),
                currentVersion.toString()
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
            
            currentVersion = this.updater.incrementSingleVersionComponent(
                currentVersion,
                this.getFieldToIncrement()
            );
            
            String preReleaseVersion;
            if ((preReleaseVersion = this.getPreReleaseVersion()) != null && !preReleaseVersion.isEmpty()) {
                currentVersion = this.updater.setPreReleaseVersion(currentVersion, preReleaseVersion);
            }
            
            listener.getLogger().append("Updating to " + currentVersion + "\n");
            committer.saveVersion(currentVersion);
            
            environment.put(
                this.getDescriptor().getCurrentVersionEnvVariable(),
                currentVersion.toString()
            );
            
            return true;

        } catch (Throwable t) {
            listener.error(t.getMessage());
        }

        return false;
    }

    public VersioningConfiguration getConfiguration() {
        return configuration;
    }

    public VersionNumberUpdater getUpdater() {
        return updater;
    }

    public VersionCommittingInterface getCommitter() {
        return committer;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return super.getRequiredMonitorService();
    }
    
    public void setConfiguration(VersioningConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public void setUpdater(VersionNumberUpdater updater) {
        this.updater = updater;
    }
    
    public void setCommitter(VersionCommittingInterface committer) {
        this.committer = committer;
    }

    public void setRetriever(VersionRetrievalInterface retriever) {
        this.retriever = retriever;
    }
    
    protected VersionCommittingInterface lazyLoadCommitter(AbstractProject project) throws IOException {
        if (this.committer == null) {
            this.committer = new AutoCreatingPropertyFileVersionHandler(
                new PropertyFileIoHandler(),
                this.getAbsolutePropertyPath(project)
            );
        }
        
        return this.committer;
    }

    protected VersionRetrievalInterface lazyLoadRetriever(AbstractProject project) throws IOException {
        if (this.retriever == null) {
            this.retriever = new AutoCreatingPropertyFileVersionHandler(
                new PropertyFileIoHandler(),
                this.getAbsolutePropertyPath(project)
            );
        }
        
        return this.retriever;
    }
    
    protected File getAbsolutePropertyPath(AbstractProject project)
    {
        File propertyFile;
        
        if ((propertyFile = new File(this.getPropertyFilePath())).isAbsolute()) {
            return propertyFile;
        }
        
        return new File(project.getRootDir(), this.getPropertyFilePath());
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
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String previousVersionEnvVariable = "PREVIOUS_VERSION_NUMBER";
        private String currentVersionEnvVariable = "CURRENT_VERSION_NUMBER";
    
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
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

        public String getPreviousVersionEnvVariable() {
            return previousVersionEnvVariable;
        }

        public String getCurrentVersionEnvVariable() {
            return currentVersionEnvVariable;
        }
    
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            
            this.previousVersionEnvVariable = formData.getString("previousVersionEnvVariable");
            this.currentVersionEnvVariable = formData.getString("currentVersionEnvVariable");
            
            save();
            
            return super.configure(req,formData);
        }
        
    }
}
