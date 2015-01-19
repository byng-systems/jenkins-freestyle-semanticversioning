package co.byng.versioningplugin;
import co.byng.versioningplugin.handler.AutoCreatingPropertyFileHandler;
import co.byng.versioningplugin.handler.PropertyHandler;
import com.github.zafarkhaja.semver.Version;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.File;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

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
public class VersionNumberBuilder extends Builder {

    private final String propertyFilePath;
    private final String majorEnvVariable;
    private final String minorEnvVariable;
    private final String fieldToIncrement;
    
    
    
    /**
     * 
     * @param propertyFilePath
     * @param majorEnvVariable
     * @param minorEnvVariable
     * @param fieldToIncrement 
     */
    @DataBoundConstructor
    public VersionNumberBuilder(
        String propertyFilePath,
        String majorEnvVariable,
        String minorEnvVariable,
        String fieldToIncrement
    ) {
        this.propertyFilePath = propertyFilePath;
        this.majorEnvVariable = majorEnvVariable;
        this.minorEnvVariable = minorEnvVariable;
        this.fieldToIncrement = fieldToIncrement;
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        
        try {
            PropertyHandler handler = new AutoCreatingPropertyFileHandler(
                this.propertyFilePath
            );

            EnvVars environment = build.getEnvironment(listener);

            Version.Builder builder = new Version.Builder();
            builder.setNormalVersion(
                environment.get(this.majorEnvVariable, "1")
                + "."
                + environment.get(this.minorEnvVariable, "0")
                + ".0"
            );
            
            builder.setPreReleaseVersion("rc");

            environment.put("VERSION_NUMBER", builder.build().toString());
            build.setDisplayName(environment.get("VERSION_NUMBER"));
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
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
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

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

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Update versioning";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            
            /*// To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)*/
            
            save();
            
            return super.configure(req,formData);
        }

    }
}

