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
package co.byng.versioningplugin.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersioningConfigurationTest {
    
    protected VersioningConfiguration configuration;
    
    
    
    @Before
    public void setUp() {
        this.configuration = new VersioningConfiguration();
    }

    /**
     * Test of setDoOverrideVersion method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetDoOverrideVersion() {
        final boolean getOverrideVersion = false;
        
        assertSame(this.configuration, this.configuration.setDoOverrideVersion(getOverrideVersion));
        assertEquals(getOverrideVersion, this.configuration.getDoOverrideVersion());
    }

    /**
     * Test of setOverrideVersion method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetOverrideVersion() {
        final String overrideVersion = "OVERRIDE VERSION";
        
        assertSame(this.configuration, this.configuration.setOverrideVersion(overrideVersion));
        assertSame(overrideVersion, this.configuration.getOverrideVersion());
    }

    /**
     * Test of setPropertyFilePath method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetPropertyFilePath() {
        final String overrideVersion = "PROPERTY FILE PATH";
        
        assertSame(this.configuration, this.configuration.setOverrideVersion(overrideVersion));
        assertSame(overrideVersion, this.configuration.getOverrideVersion());
    }

    /**
     * Test of setBaseMajorOnEnvVariable method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetBaseMajorOnEnvVariable() {
        final boolean baseOnMajorEnvVariable = true;
        
        assertSame(this.configuration, this.configuration.setBaseMajorOnEnvVariable(baseOnMajorEnvVariable));
        assertEquals(baseOnMajorEnvVariable, this.configuration.getBaseMajorOnEnvVariable());
    }

    /**
     * Test of setMajorEnvVariable method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetMajorEnvVariable() {
        final String majorEnvVariable = "MAJOR ENV VARIABLE";

        assertSame(this.configuration, this.configuration.setOverrideVersion(majorEnvVariable));
        assertSame(majorEnvVariable, this.configuration.getOverrideVersion());
    }

    /**
     * Test of setBaseMinorOnEnvVariable method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetBaseMinorOnEnvVariable() {
        final boolean baseOnMinorEnvVariable = true;
        
        assertSame(this.configuration, this.configuration.setBaseMinorOnEnvVariable(baseOnMinorEnvVariable));
        assertEquals(baseOnMinorEnvVariable, this.configuration.getBaseMinorOnEnvVariable());
    }

    /**
     * Test of setMinorEnvVariable method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetMinorEnvVariable() {
        final String minorEnvVariable = "MINOR ENV VARIABLE";

        assertSame(this.configuration, this.configuration.setMinorEnvVariable(minorEnvVariable));
        assertSame(minorEnvVariable, this.configuration.getMinorEnvVariable());
    }

    /**
     * Test of setPreReleaseVersion method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetPreReleaseVersion() {
        final String preReleaseVersion = "PRE RELEASE VERSION";

        assertSame(this.configuration, this.configuration.setPreReleaseVersion(preReleaseVersion));
        assertSame(preReleaseVersion, this.configuration.getPreReleaseVersion());
    }

    /**
     * Test of setFieldToIncrement method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetFieldToIncrement() {
        final String fieldToIncrement = "FIELD TO INCREMENT";

        assertSame(this.configuration, this.configuration.setFieldToIncrement(fieldToIncrement));
        assertSame(fieldToIncrement, this.configuration.getFieldToIncrement());
    }

    /**
     * Test of setDoEnvExport method, of class VersioningConfiguration.
     */
    @Test
    public void testGetAndSetDoEnvExport() {
        final boolean doExport = false;
        
        assertSame(this.configuration, this.configuration.setDoEnvExport(doExport));
        assertEquals(doExport, this.configuration.getDoEnvExport());
    }
    
}
