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
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.util.ListBoxModel;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
@RunWith(Enclosed.class)
public class VersionNumberBuildWrapperTest {
    
    @RunWith(MockitoJUnitRunner.class)
    public static class WrapperClassTest {
        
        private VersioningConfigurationWriteableProvider configuration;
        private VersionNumberBuilder builder;
        private VersionNumberBuildWrapper buildWrapper;

        @Before
        public void setUp() {
            this.configuration = mock(VersioningConfigurationWriteableProvider.class);
            this.builder = mock(VersionNumberBuilder.class);

            when(this.builder.getConfiguration()).thenReturn(this.configuration);

            this.buildWrapper = new VersionNumberBuildWrapper(this.builder);
        }

        @Test
        public void testPropertyConstructorCreatesDefaultConfiguredBuilder() {
            final boolean doOverrideVersion = true;
            final String overrideVersion = "OVERRIDE VERSION";
            final boolean temporaryOverride = false;
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

            final VersionNumberBuildWrapper buildWrapper = new VersionNumberBuildWrapper(
                doOverrideVersion,
                overrideVersion,
                temporaryOverride,
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

            final VersionNumberBuilder builder = buildWrapper.getBuilder();
            assertNotNull(builder);
            assertNotSame(this.builder, builder);

            final VersioningConfigurationWriteableProvider configuration = builder.getConfiguration();
            assertTrue(configuration instanceof VersioningConfiguration);

            assertSame(doOverrideVersion, configuration.getDoOverrideVersion());
            assertSame(overrideVersion, configuration.getOverrideVersion());
            assertSame(temporaryOverride, configuration.getTemporaryOverride());
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
         * Test of setUp method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testSetUp() throws Exception {
            final AbstractBuild build = mock(AbstractBuild.class);
            final Launcher launcher = mock(Launcher.class);
            final BuildListener listener = mock(BuildListener.class);

            when(this.builder.perform(same(build), same(launcher), same(listener))).thenReturn(true);
            
            final Map<String, String> myEnvVars = mock(Map.class);
            EnvVars buildEnvVars = mock(EnvVars.class);
            
            when(build.getEnvironment(same(listener))).thenReturn(buildEnvVars);
            doNothing().when(buildEnvVars).putAll(same(myEnvVars));
            
            Environment environment = this.buildWrapper.setUp(build, launcher, listener);
            assertNotNull(environment);
            
            environment.buildEnvVars(myEnvVars);
            
            verify(this.builder, times(1)).perform(eq(build), eq(launcher), eq(listener));
            verify(build, times(1)).getEnvironment(same(listener));
            verify(buildEnvVars, times(1)).putAll(same(myEnvVars));
        }

        /**
         * Test of getBuilder method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetBuilder() {
            assertSame(this.builder, this.buildWrapper.getBuilder());
        }

        /**
         * Test of setBuilder method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testSetBuilder() {
            this.buildWrapper.setBuilder(null);

            assertNull(this.buildWrapper.getBuilder());

            this.buildWrapper.setBuilder(this.builder);

            assertSame(this.builder, this.buildWrapper.getBuilder());
        }

        @Test
        public void testGetDoOverrideVersion() {
            boolean result = true;
            
            when(this.builder.getDoOverrideVersion()).thenReturn(result);
            
            assertSame(result, this.buildWrapper.getDoOverrideVersion());
            
            verify(this.builder, times(1)).getDoOverrideVersion();
        }
        
        /**
         * Test of getOverrideVersion method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetOverrideVersion() {
            String result = "OVERRIDE STRING RESULT";

            when(this.builder.getOverrideVersion()).thenReturn(result);

            assertSame(result, this.buildWrapper.getOverrideVersion());

            verify(this.builder, times(1)).getOverrideVersion();
        }

        /**
         * Test of getPropertyFilePath method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetPropertyFilePath() {
            String result = "PROPERTY STRING RESULT";

            when(this.builder.getPropertyFilePath()).thenReturn(result);

            assertSame(result, this.buildWrapper.getPropertyFilePath());

            verify(this.builder, times(1)).getPropertyFilePath();
        }

        /**
         * Test of getBaseMajorOnEnvVariable method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetBaseMajorOnEnvVariable() {
            boolean result = true;

            when(this.builder.getBaseMajorOnEnvVariable()).thenReturn(result);

            assertSame(result, this.buildWrapper.getBaseMajorOnEnvVariable());

            verify(this.builder, times(1)).getBaseMajorOnEnvVariable();
        }

        /**
         * Test of getMajorEnvVariable method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetMajorEnvVariable() {
            String result = "MajorEnv STRING RESULT";

            when(this.builder.getMajorEnvVariable()).thenReturn(result);

            assertSame(result, this.buildWrapper.getMajorEnvVariable());

            verify(this.builder, times(1)).getMajorEnvVariable();
        }

        /**
         * Test of getBaseMinorOnEnvVariable method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetBaseMinorOnEnvVariable() {
            boolean result = false;

            when(this.builder.getBaseMinorOnEnvVariable()).thenReturn(result);

            assertSame(result, this.buildWrapper.getBaseMinorOnEnvVariable());

            verify(this.builder, times(1)).getBaseMinorOnEnvVariable();
        }

        /**
         * Test of getMinorEnvVariable method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetMinorEnvVariable() {
            String result = "MinorEnv STRING RESULT";

            when(this.builder.getMinorEnvVariable()).thenReturn(result);

            assertSame(result, this.buildWrapper.getMinorEnvVariable());

            verify(this.builder, times(1)).getMinorEnvVariable();
        }

        /**
         * Test of getPreReleaseVersion method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetPreReleaseVersion() {
            String result = "PRV STRING RESULT";

            when(this.builder.getPreReleaseVersion()).thenReturn(result);

            assertSame(result, this.buildWrapper.getPreReleaseVersion());

            verify(this.builder, times(1)).getPreReleaseVersion();
        }

        /**
         * Test of getFieldToIncrement method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetFieldToIncrement() {
            String result = "INCREMENT STRING RESULT";

            when(this.builder.getFieldToIncrement()).thenReturn(result);

            assertSame(result, this.buildWrapper.getFieldToIncrement());

            verify(this.builder, times(1)).getFieldToIncrement();
        }

        /**
         * Test of getDoEnvExport method, of class VersionNumberBuildWrapper.
         */
        @Test
        public void testGetDoEnvExport() {
            boolean result = true;

            when(this.builder.getDoEnvExport()).thenReturn(result);

            assertSame(result, this.buildWrapper.getDoEnvExport());

            verify(this.builder, times(1)).getDoEnvExport();
        }
        
        @Test
        public void testGetDoSetNameOrDescription() {
            boolean result = false;
            
            when(this.builder.getDoSetNameOrDescription()).thenReturn(result);
            
            assertSame(result, this.buildWrapper.getDoSetNameOrDescription());
            
            verify(this.builder, times(1)).getDoSetNameOrDescription();
        }
        
        @Test
        public void testGetNewBuildName()
        {
            String result = "buildName";
            
            when(this.builder.getNewBuildName()).thenReturn(result);
            
            assertSame(result, this.buildWrapper.getNewBuildName());
            
            verify(this.builder, times(1)).getNewBuildName();
        }
        
        @Test
        public void testGetNewBuildDescription()
        {
            String result = "buildDescription";
            
            when(this.builder.getNewBuildDescription()).thenReturn(result);
            
            assertSame(result, this.buildWrapper.getNewBuildDescription());
            
            verify(this.builder, times(1)).getNewBuildDescription();
        }
    }
    
    @RunWith(MockitoJUnitRunner.class)
    public static class DescriptorImplTest {
        
        private OptionsProvider optionsProvider;
        private VersionNumberBuildWrapper.DescriptorImpl descriptor;
        
        @Before
        public void setUp() {
            this.optionsProvider = mock(OptionsProvider.class);
            this.descriptor = new VersionNumberBuildWrapper.DescriptorImpl(this.optionsProvider);
        }
        
        @Test
        public void testEmptyConstructorCreatesDefaultOptionsProvider() {
            VersionNumberBuildWrapper.DescriptorImpl descriptor = new VersionNumberBuildWrapper.DescriptorImpl();
            
            OptionsProvider optionsProvider = descriptor.getOptionsProvider();
            assertNotNull(optionsProvider);
            assertNotSame(this.optionsProvider, optionsProvider);
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void testSetOptionsProviderRejectsEmptyOptionsProvider() {
            this.descriptor.setOptionsProvider(null);
        }
        
        @Test
        public void testGetOptionsProvider() {
            assertSame(this.optionsProvider, this.descriptor.getOptionsProvider());
        }
        
        @Test
        public void testIsApplicable() {
            assertTrue(this.descriptor.isApplicable(null));
        }
        
        @Test
        public void testGetDisplayName() {
            assertEquals(
                "Update versioning before build",
                this.descriptor.getDisplayName()
            );
        }

        @Test
        public void testGetConfigPage() {
            assertEquals(
                "/" + VersionNumberBuilder.class.getName().replace(".", "/") + "/config.jelly",
                this.descriptor.getConfigPage()
            );
        }
        
        @Test
        public void testGetGlobalConfigPage() {
            assertNull(this.descriptor.getGlobalConfigPage());
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
        
    }
    
}
