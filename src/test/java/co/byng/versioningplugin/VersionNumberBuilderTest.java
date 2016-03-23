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
import co.byng.versioningplugin.configuration.VersioningConfigurationWriteableProvider;
import co.byng.versioningplugin.configuration.VersioningGlobalConfigurationWriteableProvider;
import co.byng.versioningplugin.handler.VersionCommittable;
import co.byng.versioningplugin.handler.VersionRetrievable;
import co.byng.versioningplugin.service.ServiceFactory;
import co.byng.versioningplugin.service.TokenExpansionProvider;
import co.byng.versioningplugin.versioning.VersionNumberUpdater;
import co.byng.versioningplugin.versioning.VersionFactory;
import com.github.zafarkhaja.semver.Version;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import jenkins.model.Jenkins;
import jenkins.model.Jenkins.JenkinsHolder;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
@RunWith(Enclosed.class)
public class VersionNumberBuilderTest {
    
    @RunWith(MockitoJUnitRunner.class)
    public static class MainWrapperClassTest {
        
        private VersioningConfigurationWriteableProvider configuration;
        private ServiceFactory serviceFactory;
        private VersionNumberUpdater updater;
        private VersionRetrievable retriever;
        private VersionCommittable committer;
        private VersionNumberBuilder builder;
        private VersionFactory versionFactory;
        private TokenExpansionProvider tokenExpansionProvider;

        @Before
        public void setUp() {
            this.configuration = mock(VersioningConfigurationWriteableProvider.class);
            this.serviceFactory = mock(ServiceFactory.class);
            this.updater = mock(VersionNumberUpdater.class);
            this.retriever = mock(VersionRetrievable.class);
            this.committer = mock(VersionCommittable.class);
            this.versionFactory = mock(VersionFactory.class);
            this.tokenExpansionProvider = mock(TokenExpansionProvider.class);

            this.builder = new VersionNumberBuilder(this.configuration);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testConstructorThrowsExceptionForNullConfiguration() {
            new VersionNumberBuilder(null);
        }

        @Test
        public void testConstructorStoresInjectedConfiguration() {
            assertSame(this.configuration, this.builder.getConfiguration());

            assertNotNull(this.builder.getServiceFactory());
            assertNotSame(this.serviceFactory, this.builder.getServiceFactory());
        }

        @Test
        public void testPropertyConstructorCreatesConfiguration() {
            final boolean doOverrideVersion = true;
            final String overrideVersion = "OVERRIDE VERSION";
            final String propertyFilePath = "PROPERTY FILE PATH";
            final boolean baseMajorOnEnvVariable = false;
            final String majorEnvVariable = "MAJOR ENVVAR";
            final boolean baseMinorOnEnvVariable = true;
            final String minorEnvVariable = "MINOR ENVVAR";
            final String preReleaseVersion = "PRE_RELEASE";
            final String preReleaseSuffix = "";
            final String fieldToIncrement = "INCREMENT";
            final boolean doEnvExport = false;
            final boolean doSetNameOrDescription = false;
            final String newBuildName = "NEW BUILD NAME";
            final String newBuildDescription = "NEW BUILD DESCRIPTION";

            final VersionNumberBuilder builder = new VersionNumberBuilder(
                doOverrideVersion,
                overrideVersion,
                propertyFilePath,
                baseMajorOnEnvVariable,
                majorEnvVariable,
                baseMinorOnEnvVariable,
                minorEnvVariable,
                preReleaseVersion,
                preReleaseSuffix,
                fieldToIncrement,
                doEnvExport,
                doSetNameOrDescription,
                newBuildName,
                newBuildDescription
            );

            final VersioningConfigurationWriteableProvider configuration = builder.getConfiguration();
            assertTrue(configuration instanceof VersioningConfiguration);

            assertSame(doOverrideVersion, configuration.getDoOverrideVersion());
            assertSame(overrideVersion, configuration.getOverrideVersion());
            assertSame(propertyFilePath, configuration.getPropertyFilePath());
            assertSame(baseMajorOnEnvVariable, configuration.getBaseMajorOnEnvVariable());
            assertSame(majorEnvVariable, configuration.getMajorEnvVariable());
            assertSame(baseMinorOnEnvVariable, configuration.getBaseMinorOnEnvVariable());
            assertSame(minorEnvVariable, configuration.getMinorEnvVariable());
            assertSame(preReleaseVersion, configuration.getPreReleaseVersion());
            assertSame(fieldToIncrement, configuration.getFieldToIncrement());
            assertSame(doEnvExport, configuration.getDoEnvExport());
            assertSame(doSetNameOrDescription, configuration.getDoSetNameOrDescription());
            assertSame(newBuildName, configuration.getNewBuildName());
            assertSame(newBuildDescription, configuration.getNewBuildDescription());
        }

        /**
         * Test of lazyLoadServices method, of class VersionNumberBuilder.
         */
        @Test
        public void testLazyLoadServicesCreatesNewFactory() {
            final String path = "/path/to/my/file";
            final AbstractProject project = mock(AbstractProject.class);

            when(this.configuration.getPropertyFilePath()).thenReturn(path);

            try {
                this.builder.lazyLoadServices(project);

                assertNotNull(this.builder.getServiceFactory());
                assertNotSame(this.serviceFactory, this.builder.getServiceFactory());
                
                verify(this.configuration, times(1)).getPropertyFilePath();
            } catch (IOException ex) {
                fail("Failed due to IOException: " + ex.getMessage());
            }
        }

        public void testLazyLoadServicesUsesExistingFactoryIfSet() {
            final String path = "/path/to/my/file";
            final AbstractProject project = mock(AbstractProject.class);

            when(configuration.getPropertyFilePath()).thenReturn(path);

            try {
                when(this.serviceFactory.createCommitter(same(project), same(path), same(this.committer))).thenReturn(this.committer);
                when(this.serviceFactory.createRetriever(same(project), same(path), same(this.retriever))).thenReturn(this.retriever);
                when(this.serviceFactory.createUpdater(same(this.updater))).thenReturn(this.updater);
                when(this.serviceFactory.createVersionFactory(same(this.versionFactory))).thenReturn(this.versionFactory);
                when(this.serviceFactory.createTokenExpansionProvider(same(this.tokenExpansionProvider))).thenReturn(this.tokenExpansionProvider);

                this.builder.lazyLoadServices(project);

                assertSame(this.serviceFactory, this.builder.getServiceFactory());
                assertSame(this.committer, this.builder.getCommitter());
                assertSame(this.retriever, this.builder.getRetriever());
                assertSame(this.updater, this.builder.getUpdater());
                assertSame(this.tokenExpansionProvider, this.builder.getTokenExpander());

                verify(this.configuration, times(1)).getPropertyFilePath();

                verify(this.serviceFactory, times(1)).createCommitter(same(project), same(path), same(this.committer));
                verify(this.serviceFactory, times(1)).createRetriever(same(project), same(path), same(this.retriever));
                verify(this.serviceFactory, times(1)).createUpdater(same(this.updater));
                verify(this.serviceFactory, times(1)).createVersionFactory(same(this.versionFactory));
                verify(this.serviceFactory, times(1)).createTokenExpansionProvider(this.tokenExpansionProvider);

            } catch (IOException ex) {
                fail("Failed due to IOException: " + ex.getMessage());
            }
        }

        public void testSettersAndGetters() {
            this.builder.setConfiguration(null);
            assertNull(this.builder.getConfiguration());

            this.builder.setServiceFactory(null);
            assertNull(this.builder.getServiceFactory());

            this.builder.setCommitter(null);
            assertNull(this.builder.getCommitter());

            this.builder.setRetriever(null);
            assertNull(this.builder.getRetriever());

            this.builder.setUpdater(null);
            assertNull(this.builder.getUpdater());
            
            this.builder.setVersionFactory(null);
            assertNull(this.builder.getVersionFactory());
            
            this.builder.setTokenExpander(null);
            assertNull(this.builder.getTokenExpander());
        }

        /**
         * Test of getOverrideVersion method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetOverrideVersion() {
            String result = "OVERRIDE STRING RESULT";

            when(this.configuration.getOverrideVersion()).thenReturn(result);

            assertSame(result, this.builder.getOverrideVersion());

            verify(this.configuration, times(1)).getOverrideVersion();
        }

        /**
         * Test of getPropertyFilePath method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetPropertyFilePath() {
            String result = "PROPERTY STRING RESULT";

            when(this.configuration.getPropertyFilePath()).thenReturn(result);

            assertSame(result, this.builder.getPropertyFilePath());

            verify(this.configuration, times(1)).getPropertyFilePath();
        }

        /**
         * Test of getBaseMajorOnEnvVariable method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetBaseMajorOnEnvVariable() {
            boolean result = true;

            when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(result);

            assertSame(result, this.builder.getBaseMajorOnEnvVariable());

            verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
        }

        /**
         * Test of getMajorEnvVariable method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetMajorEnvVariable() {
            String result = "MajorEnv STRING RESULT";

            when(this.configuration.getMajorEnvVariable()).thenReturn(result);

            assertSame(result, this.builder.getMajorEnvVariable());

            verify(this.configuration, times(1)).getMajorEnvVariable();
        }

        /**
         * Test of getBaseMinorOnEnvVariable method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetBaseMinorOnEnvVariable() {
            boolean result = false;

            when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(result);

            assertSame(result, this.builder.getBaseMinorOnEnvVariable());

            verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
        }

        /**
         * Test of getMinorEnvVariable method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetMinorEnvVariable() {
            String result = "MinorEnv STRING RESULT";

            when(this.configuration.getMinorEnvVariable()).thenReturn(result);

            assertSame(result, this.builder.getMinorEnvVariable());

            verify(this.configuration, times(1)).getMinorEnvVariable();
        }

        /**
         * Test of getPreReleaseVersion method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetPreReleaseVersion() {
            String result = "PRV STRING RESULT";

            when(this.configuration.getPreReleaseVersion()).thenReturn(result);

            assertSame(result, this.builder.getPreReleaseVersion());

            verify(this.configuration, times(1)).getPreReleaseVersion();
        }

        /**
         * Test of getFieldToIncrement method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetFieldToIncrement() {
            String result = "INCREMENT STRING RESULT";

            when(this.configuration.getFieldToIncrement()).thenReturn(result);

            assertSame(result, this.builder.getFieldToIncrement());

            verify(this.configuration, times(1)).getFieldToIncrement();
        }

        /**
         * Test of getDoEnvExport method, of class VersionNumberBuilder.
         */
        @Test
        public void testGetDoEnvExport() {
            boolean result = true;

            when(this.configuration.getDoEnvExport()).thenReturn(result);

            assertSame(result, this.builder.getDoEnvExport());

            verify(this.configuration, times(1)).getDoEnvExport();
        }
        
        
        @Test
        public void testGetDoSetNameOrDescription() {
            boolean result = false;
            
            when(this.configuration.getDoSetNameOrDescription()).thenReturn(result);
            
            assertSame(result, this.builder.getDoSetNameOrDescription());
            
            verify(this.configuration, times(1)).getDoSetNameOrDescription();
        }
        
        @Test
        public void testGetNewBuildName()
        {
            String result = "buildName";
            
            when(this.configuration.getNewBuildName()).thenReturn(result);
            
            assertSame(result, this.builder.getNewBuildName());
            
            verify(this.configuration, times(1)).getNewBuildName();
        }
        
        @Test
        public void testGetNewBuildDescription()
        {
            String result = "buildDescription";
            
            when(this.configuration.getNewBuildDescription()).thenReturn(result);
            
            assertSame(result, this.builder.getNewBuildDescription());
            
            verify(this.configuration, times(1)).getNewBuildDescription();
        }
    
        @Test
        public void testGetDescriptor() {
            Jenkins jenkins = mock(Jenkins.class);
            
            try {
                JenkinsHolder jenkinsHolder = mock(JenkinsHolder.class);
                Field holderField = Jenkins.class.getDeclaredField("HOLDER");
                holderField.setAccessible(true);
                
                holderField.set(null, jenkinsHolder);
                
                when(jenkinsHolder.getInstance()).thenReturn(jenkins);
            } catch (ReflectiveOperationException ex) {
                
            }
            
            VersionNumberBuilder.DescriptorImpl descriptor = mock(VersionNumberBuilder.DescriptorImpl.class);
            when(jenkins.getDescriptorOrDie(eq((Class) VersionNumberBuilder.class))).thenReturn(descriptor);
            
            assertSame(
                descriptor,
                this.builder.getDescriptor()
            );
            
            verify(jenkins, times(1)).getDescriptorOrDie(eq((Class) VersionNumberBuilder.class));
        }

    }
    
    
    
    @RunWith(MockitoJUnitRunner.class)
    public static class WrapperClassPerformTest {
        
        private Jenkins jenkins;
        private VersionNumberBuilder.DescriptorImpl descriptor;
        
        private VersioningConfigurationWriteableProvider configuration;
        private ServiceFactory serviceFactory;
        private VersionNumberUpdater updater;
        private VersionRetrievable retriever;
        private VersionCommittable committer;
        private VersionNumberBuilder builder;
        private VersionFactory versionFactory;
        private TokenExpansionProvider tokenExpander;

        private final String path = "/path/to/my/file";
        private AbstractProject project;
        private AbstractBuild build;
        private Launcher launcher;
        private BuildListener listener;
        private PrintStream logger;
        private VariableExporter exporter;
        private EnvVars environment;
        
        @Before
        public void setUp() {
            
            this.jenkins = mock(Jenkins.class);
            
            try {
                JenkinsHolder jenkinsHolder = mock(JenkinsHolder.class);
                Field holderField = Jenkins.class.getDeclaredField("HOLDER");
                holderField.setAccessible(true);
                
                holderField.set(null, jenkinsHolder);
                
                when(jenkinsHolder.getInstance()).thenReturn(this.jenkins);
                
                this.descriptor = mock(VersionNumberBuilder.DescriptorImpl.class);
                when(jenkins.getDescriptorOrDie(eq((Class) VersionNumberBuilder.class))).thenReturn(this.descriptor);

            } catch (ReflectiveOperationException ex) {
            }
            
            this.configuration = mock(VersioningConfigurationWriteableProvider.class);
            this.serviceFactory = mock(ServiceFactory.class);
            this.updater = mock(VersionNumberUpdater.class);
            this.retriever = mock(VersionRetrievable.class);
            this.committer = mock(VersionCommittable.class);
            this.versionFactory = mock(VersionFactory.class);
            this.tokenExpander = mock(TokenExpansionProvider.class);

            this.builder = new VersionNumberBuilder(this.configuration);
            this.builder.setServiceFactory(this.serviceFactory);
            this.builder.setCommitter(this.committer);
            this.builder.setRetriever(this.retriever);
            this.builder.setVersionFactory(this.versionFactory);
            this.builder.setUpdater(this.updater);
            this.builder.setTokenExpander(this.tokenExpander);
            
            this.exporter = mock(VariableExporter.class);
            this.project = mock(AbstractProject.class);
            this.build = mock(AbstractBuild.class);
            this.launcher = mock(Launcher.class);
            this.listener = mock(BuildListener.class);
            this.logger = mock(PrintStream.class);
            this.exporter = mock(VariableExporter.class);
            this.environment = mock(EnvVars.class);
            
            try {
                when(this.build.getProject()).thenReturn(this.project);
                
                when(this.configuration.getPropertyFilePath()).thenReturn(this.path);

                when(this.serviceFactory.createCommitter(same(this.project), same(this.path), same(this.committer))).thenReturn(this.committer);
                when(this.serviceFactory.createRetriever(same(this.project), same(this.path), same(this.retriever))).thenReturn(this.retriever);
                when(this.serviceFactory.createUpdater(same(this.updater))).thenReturn(this.updater);
                when(this.serviceFactory.createVersionFactory(same(this.versionFactory))).thenReturn(this.versionFactory);
                when(this.serviceFactory.createTokenExpansionProvider(same(this.tokenExpander))).thenReturn(this.tokenExpander);
                
                when(this.serviceFactory.createVarExporter((VariableExporter) isNull())).thenReturn(this.exporter);
                
                when(this.listener.getLogger()).thenReturn(this.logger);
            } catch (IOException ex) {
            }
        }
        
        @After
        public void tearDown() {
            try {
                verify(this.build, times(1)).getProject();
                
                verify(this.configuration, times(1)).getPropertyFilePath();
                
                verify(this.serviceFactory, times(1)).createCommitter(same(this.project), same(this.path), same(this.committer));
                verify(this.serviceFactory, times(1)).createRetriever(same(this.project), same(this.path), same(this.retriever));
                verify(this.serviceFactory, times(1)).createUpdater(same(this.updater));
                verify(this.serviceFactory, times(1)).createVersionFactory(same(this.versionFactory));
                
                verify(this.serviceFactory, times(1)).createVarExporter((VariableExporter) isNull());
                
                verify(this.listener, times(1)).getLogger();
            } catch (IOException ex) {
            }
        }

        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithOverrideVersion() {
            try {
                final String overrideVersion = "1.0.0";
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(true);
                when(this.configuration.getTemporaryOverride()).thenReturn(true);
                when(this.configuration.getOverrideVersion()).thenReturn(overrideVersion);
                when(this.tokenExpander.expand(same(overrideVersion), same(this.build), same(this.listener))).thenReturn(overrideVersion);
                when(this.versionFactory.buildVersionFromString(same(overrideVersion))).thenReturn(version1);
                when(this.committer.saveVersion(same(version1))).thenReturn(true);
                
                when(this.configuration.setDoOverrideVersion(eq(false))).thenReturn(this.configuration);
                when(this.configuration.setOverrideVersion((String) isNull())).thenReturn(this.configuration);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                when(this.configuration.getPreReleaseSuffix()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version2))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.configuration, times(1)).getTemporaryOverride();
                verify(this.configuration, times(1)).getOverrideVersion();
                verify(this.tokenExpander, times(1)).expand(same(overrideVersion), same(this.build), same(this.listener));
                verify(this.versionFactory, times(1)).buildVersionFromString(same(overrideVersion));
                verify(this.committer, times(1)).saveVersion(same(version1));
                
                verify(this.configuration, times(1)).setDoOverrideVersion(eq(false));
                verify(this.configuration, times(1)).setOverrideVersion((String) isNull());
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version2));
                
                verify(this.configuration, times(1)).getDoEnvExport();
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }

        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithMajorEnvVarVersion() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                final String majorEnvVariable = "MAJOR ENV VARIABLE";
                final Version version3 = mock(Version.class);
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(true);
                when(this.configuration.getMajorEnvVariable()).thenReturn(majorEnvVariable);
                when(this.updater.updateMajorBasedOnEnvironmentVariable(same(version2), same(this.environment), same(majorEnvVariable))).thenReturn(version3);
                
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version2))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getMajorEnvVariable();
                verify(this.updater, times(1)).updateMajorBasedOnEnvironmentVariable(same(version2), same(this.environment), same(majorEnvVariable));
                
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version3));
                
                verify(this.configuration, times(1)).getDoEnvExport();
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithMinorEnvVarVersion() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                
                final String minorEnvVariable = "MINOR ENV VARIABLE";
                final Version version3 = mock(Version.class);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(true);
                when(this.configuration.getMinorEnvVariable()).thenReturn(minorEnvVariable);
                when(this.updater.updateMinorBasedOnEnvironmentVariable(same(version2), same(this.environment), same(minorEnvVariable))).thenReturn(version3);
                
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version3))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                verify(this.configuration, times(1)).getMinorEnvVariable();
                verify(this.updater, times(1)).updateMinorBasedOnEnvironmentVariable(same(version2), same(this.environment), same(minorEnvVariable));
                
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version3));
                
                verify(this.configuration, times(1)).getDoEnvExport();
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithPreReleaseVersion() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                
                final String preReleaseVersion = "rc";
                final Version version3 = mock(Version.class);
                when(this.configuration.getPreReleaseVersion()).thenReturn(preReleaseVersion);
                when(this.configuration.getPreReleaseSuffix()).thenReturn(null);
                when(this.updater.setPreReleaseVersion(same(version2), eq(preReleaseVersion))).thenReturn(version3);
                
                when(this.committer.saveVersion(same(version3))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                
                verify(this.configuration, times(1)).getPreReleaseVersion();
                verify(this.updater, times(1)).setPreReleaseVersion(same(version2), eq(preReleaseVersion));
                
                verify(this.committer, times(1)).saveVersion(same(version3));
                
                verify(this.configuration, times(1)).getDoEnvExport();
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithVarExport() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version2))).thenReturn(true);
                
                final Version[] bothVersions = new Version[]{version1, version2};
                
                final String previousVersionVariable = "PREVIOUS_VERSION";
                final String currentVersionVariable = "CURRENT VERSION";
                final String previousVersion = "Previous version string";
                final String currentVersion = "Current version string";
                when(version1.toString()).thenReturn(previousVersion);
                when(version2.toString()).thenReturn(currentVersion);
                
                for (Version version : bothVersions) {
                    when(version.getMajorVersion()).thenReturn(1);
                    when(version.getMinorVersion()).thenReturn(2);
                    when(version.getPatchVersion()).thenReturn(3);
                    when(version.getPreReleaseVersion()).thenReturn("");
                }
                
                when(this.configuration.getDoEnvExport()).thenReturn(true);
                when(this.descriptor.getPreviousVersionEnvVariable()).thenReturn(previousVersionVariable);
                when(this.descriptor.getCurrentVersionEnvVariable()).thenReturn(currentVersionVariable);
                
                doNothing().when(this.exporter).addVariableToExport(same(previousVersionVariable), same(previousVersion));
                doNothing().when(this.exporter).addVariableToExport(eq(previousVersionVariable + "_MAJOR"), eq("1"));
                doNothing().when(this.exporter).addVariableToExport(eq(previousVersionVariable + "_MINOR"), eq("2"));
                doNothing().when(this.exporter).addVariableToExport(eq(previousVersionVariable + "_PATCH"), eq("3"));
                doNothing().when(this.exporter).addVariableToExport(eq(previousVersionVariable + "_PRE_RELEASE"), eq(""));
                
                doNothing().when(this.exporter).addVariableToExport(same(currentVersionVariable), same(currentVersion));
                doNothing().when(this.exporter).addVariableToExport(eq(currentVersionVariable + "_MAJOR"), eq("1"));
                doNothing().when(this.exporter).addVariableToExport(eq(currentVersionVariable + "_MINOR"), eq("2"));
                doNothing().when(this.exporter).addVariableToExport(eq(currentVersionVariable + "_PATCH"), eq("3"));
                doNothing().when(this.exporter).addVariableToExport(eq(currentVersionVariable + "_PRE_RELEASE"), eq(""));
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version2));
                
                for (Version version : bothVersions) {
                    verify(version, times(1)).getMajorVersion();
                    verify(version, times(1)).getMinorVersion();
                    verify(version, times(1)).getPatchVersion();
                    verify(version, times(1)).getPreReleaseVersion();
                }
                
                verify(this.configuration, times(1)).getDoEnvExport();
                verify(this.descriptor, times(1)).getPreviousVersionEnvVariable();
                verify(this.descriptor, times(1)).getCurrentVersionEnvVariable();
                
                verify(this.exporter, times(1)).addVariableToExport(same(previousVersionVariable), same(previousVersion));
                verify(this.exporter, times(1)).addVariableToExport(eq(previousVersionVariable + "_MAJOR"), eq("1"));
                verify(this.exporter, times(1)).addVariableToExport(eq(previousVersionVariable + "_MINOR"), eq("2"));
                verify(this.exporter, times(1)).addVariableToExport(eq(previousVersionVariable + "_PATCH"), eq("3"));
                verify(this.exporter, times(1)).addVariableToExport(eq(previousVersionVariable + "_PRE_RELEASE"), eq(""));
                
                verify(this.exporter, times(1)).addVariableToExport(same(currentVersionVariable), same(currentVersion));
                verify(this.exporter, times(1)).addVariableToExport(eq(currentVersionVariable + "_MAJOR"), eq("1"));
                verify(this.exporter, times(1)).addVariableToExport(eq(currentVersionVariable + "_MINOR"), eq("2"));
                verify(this.exporter, times(1)).addVariableToExport(eq(currentVersionVariable + "_PATCH"), eq("3"));
                verify(this.exporter, times(1)).addVariableToExport(eq(currentVersionVariable + "_PRE_RELEASE"), eq(""));
                
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithSetBuildNameAndDescription() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version2))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                when(this.configuration.getDoSetNameOrDescription()).thenReturn(true);
                
                String newBuildName = "New build name";
                String newBuildDescription = " New build description ";
                final String tokenisedBuildName = "Tokenised build name";
                final String tokenisedBuildDescription = "Tokenised build name";
                when(this.configuration.getNewBuildName()).thenReturn(newBuildName);
                when(this.configuration.getNewBuildDescription()).thenReturn(newBuildDescription);
                
                when(this.tokenExpander.expand(eq(newBuildName), same(this.build), same(this.listener))).thenReturn(tokenisedBuildName);
                when(this.tokenExpander.expand(eq(newBuildDescription.trim()), same(this.build), same(this.listener))).thenReturn(tokenisedBuildDescription);
                
                doNothing().when(this.build).setDisplayName(eq(tokenisedBuildName));
                doNothing().when(this.build).setDescription(eq(tokenisedBuildDescription));
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version2));
                
                verify(this.configuration, times(1)).getDoEnvExport();
                verify(this.configuration, times(1)).getDoSetNameOrDescription();
                
                verify(this.configuration, times(1)).getNewBuildName();
                verify(this.configuration, times(1)).getNewBuildDescription();

                verify(this.tokenExpander, times(1)).expand(eq(newBuildName), same(this.build), same(this.listener));
                verify(this.tokenExpander, times(1)).expand(eq(newBuildDescription.trim()), same(this.build), same(this.listener));
                
                verify(this.build, times(1)).setDisplayName(eq(tokenisedBuildName));
                verify(this.build, times(1)).setDescription(eq(tokenisedBuildDescription));

            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        /**
         * Test of perform method, of class VersionNumberBuilder.
         */
        @Test
        public void testPerformWithoutSetBuildNameAndDescription() {
            try {
                final Version version1 = mock(Version.class);
                when(this.configuration.getDoOverrideVersion()).thenReturn(false);
                when(this.retriever.loadVersion()).thenReturn(version1);
                
                when(this.build.getEnvironment(same(this.listener))).thenReturn(this.environment);
                
                final String fieldToIncrement = "FIELD TO INCREMENT";
                final Version version2 = mock(Version.class);
                when(this.configuration.getFieldToIncrement()).thenReturn(fieldToIncrement);
                when(this.updater.incrementSingleVersionComponent(same(version1), same(fieldToIncrement))).thenReturn(version2);
                
                when(this.configuration.getBaseMajorOnEnvVariable()).thenReturn(false);
                when(this.configuration.getBaseMinorOnEnvVariable()).thenReturn(false);
                
                when(this.configuration.getPreReleaseVersion()).thenReturn(null);
                
                when(this.committer.saveVersion(same(version2))).thenReturn(true);
                
                when(this.configuration.getDoEnvExport()).thenReturn(false);
                
                when(this.configuration.getDoSetNameOrDescription()).thenReturn(false);
                
                assertTrue(this.builder.perform(this.build, this.launcher, this.listener));
                
                verify(this.configuration, times(1)).getDoOverrideVersion();
                verify(this.retriever, times(1)).loadVersion();
                verify(this.committer, times(1)).saveVersion(same(version1));
                
                verify(this.configuration, times(1)).getFieldToIncrement();
                verify(this.updater, times(1)).incrementSingleVersionComponent(same(version1), same(fieldToIncrement));
                
                verify(this.configuration, times(1)).getBaseMajorOnEnvVariable();
                verify(this.configuration, times(1)).getBaseMinorOnEnvVariable();
                
                verify(this.configuration, times(1)).getPreReleaseVersion();
                
                verify(this.committer, times(1)).saveVersion(same(version2));
                
                verify(this.configuration, times(1)).getDoEnvExport();
                verify(this.configuration, times(1)).getDoSetNameOrDescription();

            } catch (AssertionError ex) {
            } catch (Throwable t) {
                fail(
                    "Uncaught exception (should not have been thrown): "
                        + t.getClass().getSimpleName()
                        + " - "
                        + t.getMessage()
                );
            }
        }
        
        private static class ArbitraryExceptionStub extends Exception {}
        
        @Test
        public void testPerformReturnsFalseForAnyException() {
            
            when(this.serviceFactory.createVarExporter((VariableExporter) isNull())).thenThrow(ArbitraryExceptionStub.class);
            
            assertFalse(this.builder.perform(this.build, this.launcher, this.listener));
        }
        
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class DescriptorImplTest {
        
        private Jenkins jenkins;
        
        private VersioningGlobalConfigurationWriteableProvider globalConfiguration;
        private OptionsProvider optionsProvider;
        private VersionNumberBuilder.DescriptorImpl descriptor;
        
        @Before
        public void setUp() {
            this.jenkins = mock(Jenkins.class);
            
            try {
                JenkinsHolder jenkinsHolder = mock(JenkinsHolder.class);
                Field holderField = Jenkins.class.getDeclaredField("HOLDER");
                holderField.setAccessible(true);
                
                holderField.set(null, jenkinsHolder);
                
                when(jenkinsHolder.getInstance()).thenReturn(this.jenkins);
                
                this.descriptor = mock(VersionNumberBuilder.DescriptorImpl.class);
                when(this.jenkins.getRootDir()).thenReturn(null);

            } catch (ReflectiveOperationException ex) {
            }
            
            this.globalConfiguration = mock(VersioningGlobalConfigurationWriteableProvider.class);
            this.optionsProvider = mock(OptionsProvider.class);
            this.descriptor = new VersionNumberBuilder.DescriptorImpl(this.globalConfiguration, this.optionsProvider);
        }
        
        @After
        public void tearDown() {
            verify(this.jenkins, atLeast(1)).getRootDir();
        }
        
        @Test
        public void testEmptyConstructorCreatesDefaults() {
            VersionNumberBuilder.DescriptorImpl descriptor = new VersionNumberBuilder.DescriptorImpl();
            
            assertNotNull(descriptor.getGlobalConfiguration());
            assertNotSame(this.globalConfiguration, this);
            
            assertNotNull(descriptor.getOptionsProvider());
            assertNotSame(this.optionsProvider, descriptor.getOptionsProvider());
        }
        
        @Test
        public void testIsApplicable() {
            assertTrue(this.descriptor.isApplicable(null));
        }
        
        @Test
        public void testDoCheckOverrideVersion() {
            assertEquals(FormValidation.ok(), this.descriptor.doCheckOverrideVersion("1.0.0"));
        }
        
        @Test
        public void testDoCheckOverrideVersionHandlesParseException() {
            FormValidation warning = this.descriptor.doCheckOverrideVersion("jibberish");
            
            assertEquals(warning.kind, Kind.WARNING);
            assertEquals(
                "That does not appear to be a valid semantic versioning string; "
                        + "if using a token macro, then please be aware that this must resolve "
                        + "at build time",
                warning.getMessage()
            );
        }
        
        @Test
        public void testGetDisplayName() {
            assertEquals("Update versioning", this.descriptor.getDisplayName());
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void testSetGlobalConfigurationThrowsExceptionForNullValue() {
            this.descriptor.setGlobalConfiguration(null);
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void testSetOptionsProviderThrowsExceptionForNullValue() {
            this.descriptor.setOptionsProvider(null);
        }
        
        @Test
        public void testGetPreviousVersionEnvVariable() {
            final String result = "PREVIOUS ENV VARIABLE";
            
            when(this.globalConfiguration.getPreviousVersionEnvVariable()).thenReturn(result);
            
            assertSame(
                result,
                this.descriptor.getPreviousVersionEnvVariable()
            );
            
            verify(this.globalConfiguration, times(1)).getPreviousVersionEnvVariable();
        }

        @Test
        public void testGetCurrentVersionEnvVariable() {
            final String result = "CURRENT ENV VARIABLE";
            
            when(this.globalConfiguration.getCurrentVersionEnvVariable()).thenReturn(result);
            
            assertSame(
                result,
                this.descriptor.getCurrentVersionEnvVariable()
            );
            
            verify(this.globalConfiguration, times(1)).getCurrentVersionEnvVariable();
        }
        
        @Test
        public void testDoFillEnvVariableSubjectFieldItems() {
            ListBoxModel list = mock(ListBoxModel.class);
            
            when(this.optionsProvider.getEnvVariableSubjectFieldItems()).thenReturn(list);
            
            assertSame(list, this.descriptor.doFillEnvVariableSubjectFieldItems());
            
            verify(this.optionsProvider, times(1)).getEnvVariableSubjectFieldItems();
        }
        
        @Test
        public void testDoFillFieldToIncrementItems() {
            ListBoxModel list = mock(ListBoxModel.class);
            
            when(this.optionsProvider.getFieldToIncrementItems()).thenReturn(list);
            
            assertSame(list, this.descriptor.doFillFieldToIncrementItems());
            
            verify(this.optionsProvider, times(1)).getFieldToIncrementItems();
        }
        
        @Test
        public void testDoFillPreReleaseVersionItems() {
            ListBoxModel list = mock(ListBoxModel.class);
            
            when(this.optionsProvider.getPreReleaseVersionItems()).thenReturn(list);
            
            assertSame(list, this.descriptor.doFillPreReleaseVersionItems());
            
            verify(this.optionsProvider, times(1)).getPreReleaseVersionItems();
        }
        
        @Test
        public void testConfigure() {
            StaplerRequest request = mock(StaplerRequest.class);
            
            final String previousVersionEnvVariable = "previousVersionEnvVariable";
            final String currentVersionEnvVariable = "currentVersionEnvVariable";
            JSONObject formData = new JSONObject(false);
            formData.put(previousVersionEnvVariable, previousVersionEnvVariable);
            formData.put(currentVersionEnvVariable, currentVersionEnvVariable);
            
            when(this.globalConfiguration.setPreviousVersionEnvVariable(same(previousVersionEnvVariable))).thenReturn(this.globalConfiguration);
            when(this.globalConfiguration.setCurrentVersionEnvVariable(same(currentVersionEnvVariable))).thenReturn(this.globalConfiguration);
            
            VersionNumberBuilder.DescriptorImpl descriptor = mock(VersionNumberBuilder.DescriptorImpl.class);
            doCallRealMethod().when(descriptor).setGlobalConfiguration(same(this.globalConfiguration));
            descriptor.setGlobalConfiguration(this.globalConfiguration);
            
            try {
                doNothing().when(descriptor).save();
                doCallRealMethod().when(descriptor).configure(same(request), same(formData));

                descriptor.configure(request, formData);

                verify(descriptor, times(1)).save();
            } catch (FormException ex) {
                fail("Unexpected FormException was thrown");
            }
        }
        
    }
    
}
