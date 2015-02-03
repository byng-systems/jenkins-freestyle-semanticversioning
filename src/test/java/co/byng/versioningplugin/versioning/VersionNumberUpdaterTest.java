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
package co.byng.versioningplugin.versioning;

import com.github.zafarkhaja.semver.Version;
import hudson.EnvVars;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersionNumberUpdaterTest {
    
    private Version currentVersion;
    private EnvVars environment;
    private VersionNumberUpdater updater;
    
    @Before
    public void setUp() {
        this.currentVersion = mock(Version.class);
        when(this.currentVersion.incrementMajorVersion()).thenReturn(this.currentVersion);
        when(this.currentVersion.incrementMinorVersion()).thenReturn(this.currentVersion);
        when(this.currentVersion.incrementPatchVersion()).thenReturn(this.currentVersion);
        
        this.environment = mock(EnvVars.class);
        this.updater = new VersionNumberUpdater();
    }

    /**
     * Test of incrementSingleVersionComponent method, of class VersionNumberUpdater.
     */
    @Test
    public void testIncrementSingleVersionComponentMajor() {
        this.updater.incrementSingleVersionComponent(
            currentVersion,
            VersionNumberUpdater.VersionComponent.MAJOR
        );
        
        verify(this.currentVersion, times(1)).incrementMajorVersion();
    }

    /**
     * Test of incrementSingleVersionComponent method, of class VersionNumberUpdater.
     */
    @Test
    public void testIncrementSingleVersionComponentMinor() {
        this.updater.incrementSingleVersionComponent(
            currentVersion,
            VersionNumberUpdater.VersionComponent.MINOR
        );
        
        verify(this.currentVersion, times(1)).incrementMinorVersion();
    }

    /**
     * Test of incrementSingleVersionComponent method, of class VersionNumberUpdater.
     */
    @Test
    public void testIncrementSingleVersionComponentPatch() {
        this.updater.incrementSingleVersionComponent(
            currentVersion,
            VersionNumberUpdater.VersionComponent.PATCH
        );
        
        verify(this.currentVersion, times(1)).incrementPatchVersion();
    }

    /**
     * Test of incrementSingleVersionComponent method, of class VersionNumberUpdater.
     */
    @Test
    public void testIncrementSingleVersionComponentOther() {
        this.updater.incrementSingleVersionComponent(this.currentVersion, null);
        this.updater.incrementSingleVersionComponent(this.currentVersion, "RANDOM");
        
        verify(this.currentVersion, times(0)).incrementMajorVersion();
        verify(this.currentVersion, times(0)).incrementMinorVersion();
        verify(this.currentVersion, times(0)).incrementPatchVersion();
    }

    /**
     * Test of updateMajorBasedOnEnvironmentVariable method, of class VersionNumberUpdater.
     */
    @Test
    public void testUpdateMajorBasedOnEnvironmentVariable() throws Exception {
        final int targetVersionNumber = 7;
        final int currentVersionNumber = 3;
        
        String envVarName = "ARBITRARY MAJOR ENVIRONMENT VARIABLE";
        when(this.environment.containsKey(same(envVarName))).thenReturn(true);
        when(this.environment.get(same(envVarName))).thenReturn(String.valueOf(targetVersionNumber));
        when(this.currentVersion.getMajorVersion()).thenReturn(currentVersionNumber);
        
        assertSame(
            this.currentVersion,
            this.updater.updateMajorBasedOnEnvironmentVariable(
                this.currentVersion,
                this.environment,
                envVarName
            )
        );

        verify(this.currentVersion, times(targetVersionNumber - currentVersionNumber)).incrementMajorVersion();

        verify(this.environment, times(1)).containsKey(same(envVarName));
        verify(this.environment, times(1)).get(same(envVarName));
        verify(this.currentVersion, times(1)).getMajorVersion();
    }

    /**
     * Test of updateMinorBasedOnEnvironmentVariable method, of class VersionNumberUpdater.
     */
    @Test
    public void testUpdateMinorBasedOnEnvironmentVariable() throws Exception {
        final int targetVersionNumber = 7;
        final int currentVersionNumber = 3;
        
        String envVarName = "ARBITRARY MINOR ENVIRONMENT VARIABLE";
        when(this.environment.containsKey(same(envVarName))).thenReturn(true);
        when(this.environment.get(same(envVarName))).thenReturn(String.valueOf(targetVersionNumber));
        when(this.currentVersion.getMinorVersion()).thenReturn(currentVersionNumber);
        
        assertSame(
            this.currentVersion,
            this.updater.updateMinorBasedOnEnvironmentVariable(
                this.currentVersion,
                this.environment,
                envVarName
            )
        );
        
        verify(this.currentVersion, times(targetVersionNumber - currentVersionNumber)).incrementMinorVersion();
        
        verify(this.environment, times(1)).containsKey(same(envVarName));
        verify(this.environment, times(1)).get(same(envVarName));
        verify(this.currentVersion, times(1)).getMinorVersion();
    }

    /**
     * Test of getVersionDiffFromEnvVariable method, of class VersionNumberUpdater.
     */
    @Test
    public void testGetVersionDiffFromEnvVariableThrowsExceptionForMissingVar() throws Exception {
        try {
            this.updater.getVersionDiffFromEnvVariable(
                this.environment,
                null,
                1
            );
            
            fail("Exception should have been thrown");
        } catch (Exception ex) {

            assertTrue(ex.getClass() == Exception.class);
            assertEquals(
                "Environment variable '" + null + "' is not set in the current context",
                ex.getMessage()
            );
        }
        
        String envVariableName = "ARBITRARY ENV VARIABLE 1";
        when(this.environment.containsKey(eq(envVariableName))).thenReturn(false);
        
        try {
            this.updater.getVersionDiffFromEnvVariable(
                this.environment,
                envVariableName,
                1
            );
            
            fail("Exception should have been thrown");
        } catch (Exception ex) {

            assertTrue(ex.getClass() == Exception.class);
            assertEquals(
                "Environment variable '" + envVariableName + "' is not set in the current context",
                ex.getMessage()
            );
            
            verify(this.environment, times(1)).containsKey(eq(envVariableName));
        }
    }

    /**
     * Test of setPreReleaseVersion method, of class VersionNumberUpdater.
     */
    @Test
    public void testSetPreReleaseVersion() {
        String preRelease = "any";
        
        when(this.currentVersion.setPreReleaseVersion(same(preRelease))).thenReturn(this.currentVersion);
        
        this.updater.setPreReleaseVersion(this.currentVersion, preRelease);
        
        verify(this.currentVersion, times(1)).setPreReleaseVersion(same(preRelease));
    }
    
    /**
     * Test of setPreReleaseVersion method, of class VersionNumberUpdater.
     */
    @Test
    public void testSetPreReleaseVersionClearsIfNoneGiven() {
        String preRelease = VersionNumberUpdater.PreReleaseVersion.NONE;
        
        when(this.currentVersion.getNormalVersion()).thenReturn("1.0.0");
        
        assertNotSame(
            this.currentVersion,
            this.updater.setPreReleaseVersion(this.currentVersion, preRelease)
        );
        
        verify(this.currentVersion, times(1)).getNormalVersion();
    }
    
}
