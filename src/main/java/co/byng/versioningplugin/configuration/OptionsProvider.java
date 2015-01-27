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

import co.byng.versioningplugin.updater.VersionNumberUpdater;
import hudson.util.ListBoxModel;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public final class OptionsProvider {

    public static ListBoxModel doFillEnvVariableSubjectFieldItems() {
        ListBoxModel model = new ListBoxModel();

        model.add("Major", VersionNumberUpdater.VersionComponent.MAJOR);
        model.add("Minor", VersionNumberUpdater.VersionComponent.MINOR);

        return model;
    }

    public static ListBoxModel doFillFieldToIncrementItems() {
        ListBoxModel model = OptionsProvider.doFillEnvVariableSubjectFieldItems();

        model.add("Patch", VersionNumberUpdater.VersionComponent.PATCH);

        return model;
    }

    public static ListBoxModel doFillPreReleaseVersionItems() {
        ListBoxModel model = new ListBoxModel();

        model.add(
                "",
                VersionNumberUpdater.PreReleaseVersion.NONE
        );

        model.add(
                "Alpha",
                VersionNumberUpdater.PreReleaseVersion.ALPHA
        );

        model.add(
                "Beta",
                VersionNumberUpdater.PreReleaseVersion.BETA
        );

        model.add(
                "Release candidate (rc)",
                VersionNumberUpdater.PreReleaseVersion.RELEASE_CANDIDATE
        );

        return model;
    }

}
