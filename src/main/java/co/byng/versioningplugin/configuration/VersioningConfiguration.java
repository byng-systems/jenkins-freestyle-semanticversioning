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

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
 public class VersioningConfiguration implements VersioningConfigurationWriteableProvider {
    
    protected boolean doOverrideVersion = true;
    protected String overrideVersion;
    protected String propertyFilePath;
    protected boolean baseMajorOnEnvVariable;
    protected String majorEnvVariable;
    protected boolean baseMinorOnEnvVariable;
    protected String minorEnvVariable;
    protected String preReleaseVersion;
    protected String fieldToIncrement;
    protected boolean doEnvExport = true;
    
    @Override
    public boolean getDoOverrideVersion() {
        return doOverrideVersion;
    }
    
    @Override
    public String getOverrideVersion() {
        return overrideVersion;
    }

    @Override
    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    @Override
    public boolean getBaseMajorOnEnvVariable() {
        return baseMajorOnEnvVariable;
    }

    @Override
    public String getMajorEnvVariable() {
        return majorEnvVariable;
    }

    @Override
    public boolean getBaseMinorOnEnvVariable() {
        return baseMinorOnEnvVariable;
    }

    @Override
    public String getMinorEnvVariable() {
        return minorEnvVariable;
    }

    @Override
    public String getPreReleaseVersion() {
        return preReleaseVersion;
    }

    @Override
    public String getFieldToIncrement() {
        return fieldToIncrement;
    }

    @Override
    public boolean getDoEnvExport() {
        return doEnvExport;
    }

    @Override
    public VersioningConfiguration setDoOverrideVersion(boolean doOverrideVersion) {
        this.doOverrideVersion = doOverrideVersion;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setOverrideVersion(String overrideVersion) {
        this.overrideVersion = overrideVersion;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setPropertyFilePath(String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setBaseMajorOnEnvVariable(boolean baseMajorOnEnvVariable) {
        this.baseMajorOnEnvVariable = baseMajorOnEnvVariable;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setMajorEnvVariable(String majorEnvVariable) {
        this.majorEnvVariable = majorEnvVariable;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setBaseMinorOnEnvVariable(boolean baseMinorOnEnvVariable) {
        this.baseMinorOnEnvVariable = baseMinorOnEnvVariable;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setMinorEnvVariable(String minorEnvVariable) {
        this.minorEnvVariable = minorEnvVariable;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setPreReleaseVersion(String preReleaseVersion) {
        this.preReleaseVersion = preReleaseVersion;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setFieldToIncrement(String fieldToIncrement) {
        this.fieldToIncrement = fieldToIncrement;
        
        return this;
    }

    @Override
    public VersioningConfigurationWriteableProvider setDoEnvExport(boolean doEnvExport) {
        this.doEnvExport = doEnvExport;
        
        return this;
    }
    
}
