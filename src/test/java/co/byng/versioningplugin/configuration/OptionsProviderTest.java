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

import co.byng.versioningplugin.versioning.VersionNumberUpdater;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class OptionsProviderTest {
    
    private OptionsProvider optionsProvider;
    
    @Before
    public void setUp() {
        this.optionsProvider = new OptionsProvider();
    }
    
    protected void compareActualWithExpected(ListBoxModel expectedModel, ListBoxModel actualModel) {
        assertEquals(
            expectedModel.size(),
            actualModel.size()
        );
        
        for (int i = 0; i < expectedModel.size(); i++) {
            Option expectedOption = expectedModel.get(i);
            Option actualOption = actualModel.get(i);
            
            assertEquals(expectedOption.name, actualOption.name);
            assertEquals(expectedOption.value, actualOption.value);
        }
    }

    /**
     * Test of getEnvVariableSubjectFieldItems method, of class OptionsProvider.
     */
    @Test
    public void testGetEnvVariableSubjectFieldItems() {
        ListBoxModel expectedModel = new ListBoxModel();

        expectedModel.add("Major", VersionNumberUpdater.VersionComponent.MAJOR);
        expectedModel.add("Minor", VersionNumberUpdater.VersionComponent.MINOR);
        
        this.compareActualWithExpected(
            expectedModel,
            this.optionsProvider.getEnvVariableSubjectFieldItems()
        );
    }

    /**
     * Test of getFieldToIncrementItems method, of class OptionsProvider.
     */
    @Test
    public void testGetFieldToIncrementItems() {
        ListBoxModel expectedModel = new ListBoxModel();
        
        expectedModel.add("None (skip)", VersionNumberUpdater.VersionComponent.NONE);
        expectedModel.add("Major", VersionNumberUpdater.VersionComponent.MAJOR);
        expectedModel.add("Minor", VersionNumberUpdater.VersionComponent.MINOR);
        expectedModel.add("Patch", VersionNumberUpdater.VersionComponent.PATCH);

        this.compareActualWithExpected(
            expectedModel,
            this.optionsProvider.getFieldToIncrementItems()
        );
    }

    /**
     * Test of getPreReleaseVersionItems method, of class OptionsProvider.
     */
    @Test
    public void testGetPreReleaseVersionItems() {
        ListBoxModel expectedModel = new ListBoxModel();

        expectedModel.add(
            "None (clear)",
            VersionNumberUpdater.PreReleaseVersion.NONE
        );

        expectedModel.add(
            "Alpha",
            VersionNumberUpdater.PreReleaseVersion.ALPHA
        );

        expectedModel.add(
            "Beta",
            VersionNumberUpdater.PreReleaseVersion.BETA
        );

        expectedModel.add(
            "Release candidate (rc)",
            VersionNumberUpdater.PreReleaseVersion.RELEASE_CANDIDATE
        );

        expectedModel.add(
            "Nightly (nightly)",
            VersionNumberUpdater.PreReleaseVersion.NIGHTLY
        );

        expectedModel.add(
            "Build (build)",
            VersionNumberUpdater.PreReleaseVersion.BUILD
        );

        expectedModel.add(
            "Hotfix (hotfix)",
            VersionNumberUpdater.PreReleaseVersion.HOTFIX
        );

        this.compareActualWithExpected(
            expectedModel,
            this.optionsProvider.getPreReleaseVersionItems()
        );
    }
    
}
