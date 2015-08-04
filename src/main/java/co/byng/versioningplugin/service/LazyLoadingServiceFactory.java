/*
 * The MIT License
 *
 * Copyright 2015 matt.
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
package co.byng.versioningplugin.service;

import co.byng.versioningplugin.AddEnvVarsAction;
import co.byng.versioningplugin.VariableExporter;
import co.byng.versioningplugin.handler.VersionCommittable;
import co.byng.versioningplugin.handler.VersionRetrievable;
import co.byng.versioningplugin.handler.file.AutoCreatingPropertyFileVersionHandler;
import co.byng.versioningplugin.handler.file.PropertyFileIoHandler;
import co.byng.versioningplugin.versioning.VersionNumberUpdater;
import co.byng.versioningplugin.versioning.StaticVersionFactory;
import co.byng.versioningplugin.versioning.VersionFactory;
import hudson.model.AbstractProject;
import java.io.IOException;

/**
 *
 * @author matt
 */
public class LazyLoadingServiceFactory implements ServiceFactory {
    
    protected PathProvider pathProvider;

    public LazyLoadingServiceFactory(PathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }
    
    
    
    public VersionCommittable createCommitter(
        AbstractProject project,
        String propertyFilePath,
        VersionCommittable currentCommitter
    ) throws IOException {
        if (currentCommitter == null) {
            return this.createDefaultFileHandler(project, propertyFilePath);
        }
        
        return currentCommitter;
    }

    public VersionRetrievable createRetriever(
        AbstractProject project,
        String propertyFilePath,
        VersionRetrievable currentRetriever
    ) throws IOException {
        if (currentRetriever == null) {
            return this.createDefaultFileHandler(project, propertyFilePath);
        }
        
        return currentRetriever;
    }

    protected AutoCreatingPropertyFileVersionHandler createDefaultFileHandler(
        AbstractProject project,
        String propertyFilePath
    ) {
        return new AutoCreatingPropertyFileVersionHandler(
            new PropertyFileIoHandler(),
            new StaticVersionFactory(),
            this.pathProvider.getPropertyFilePath(project, propertyFilePath)
        );
    }

    public VersionNumberUpdater createUpdater(VersionNumberUpdater currentUpdater) {
        if (currentUpdater == null) {
            return new VersionNumberUpdater();
        }
        
        return currentUpdater;
    }

    public VariableExporter createVarExporter(VariableExporter currentVarExporter) {
        if (currentVarExporter == null) {
            return new VariableExporter(new AddEnvVarsAction());
        }
        
        return currentVarExporter;
    }

    public VersionFactory createVersionFactory(VersionFactory versionFactory) {
        if (versionFactory == null) {
            return new StaticVersionFactory();
        }
        
        return versionFactory;
    }

    @Override
    public TokenExpansionProvider createTokenExpansionProvider(TokenExpansionProvider tokenExpansionProvider) {
        if  (tokenExpansionProvider == null) {
            return new SingletonCallTokenExpansionProvider();
        }
        
        return tokenExpansionProvider;
    }

}
