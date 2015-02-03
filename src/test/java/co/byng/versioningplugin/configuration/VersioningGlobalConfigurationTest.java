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
import static org.junit.Assert.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class VersioningGlobalConfigurationTest {
    
    protected VersioningGlobalConfiguration globalConfiguration;
    
    @Before
    public void setUp() {
        this.globalConfiguration = new VersioningGlobalConfiguration();
    }

    /**
     * 
     */
    @Test
    public void testGetPreviousVersionEnvVariable() {
        assertEquals("PREVIOUS_VERSION_NUMBER", this.globalConfiguration.getPreviousVersionEnvVariable());
        
        final String previousVersionNumber = "PREVIOUS VERSION NUMBER";

        assertSame(this.globalConfiguration, this.globalConfiguration.setPreviousVersionEnvVariable(previousVersionNumber));
        assertSame(previousVersionNumber, this.globalConfiguration.getPreviousVersionEnvVariable());
    }

    /**
     * 
     */
    @Test
    public void testGetAndSetCurrentVersionEnvVariable() {
        assertEquals("CURRENT_VERSION_NUMBER", this.globalConfiguration.getCurrentVersionEnvVariable());
        
        final String currentVersionNumber = "CURRENT VERSION NUMBER";

        assertSame(this.globalConfiguration, this.globalConfiguration.setCurrentVersionEnvVariable(currentVersionNumber));
        assertSame(currentVersionNumber, this.globalConfiguration.getCurrentVersionEnvVariable());
    }

}
