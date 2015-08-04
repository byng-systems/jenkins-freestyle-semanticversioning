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
package co.byng.versioningplugin.service;

import co.byng.versioningplugin.AddEnvVarsAction;
import co.byng.versioningplugin.VariableExporter;
import co.byng.versioningplugin.handler.VersionCommittable;
import co.byng.versioningplugin.handler.VersionRetrievable;
import co.byng.versioningplugin.handler.file.AutoCreatingPropertyFileVersionHandler;
import co.byng.versioningplugin.handler.file.PropertyFileIoHandler;
import co.byng.versioningplugin.versioning.StaticVersionFactory;
import co.byng.versioningplugin.versioning.VersionFactory;
import co.byng.versioningplugin.versioning.VersionNumberUpdater;
import hudson.model.AbstractProject;
import java.io.File;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class LazyLoadingServiceFactoryTest {
    
    private String filePath = "/path/to/my/file";
    private File propertyFile;
    private AbstractProject project;
    private PathProvider pathProvider;
    private LazyLoadingServiceFactory factory;
    
    @Before
    public void setUp() {
        this.project = mock(AbstractProject.class);
        this.pathProvider = mock(PathProvider.class);
        this.factory = new LazyLoadingServiceFactory(this.pathProvider);
        
        when(this.pathProvider.getPropertyFilePath(same(this.project), same(this.filePath))).thenReturn(propertyFile);
    }

    /**
     * Test of createCommitter method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateCommitterReturnsPassedValueIfNotNull() throws Exception {
        VersionCommittable currentCommitter = mock(VersionCommittable.class);
        
        assertSame(
            currentCommitter,
            this.factory.createCommitter(
                this.project,
                this.filePath,
                currentCommitter
            )
        );
    }

    /**
     * Test of createCommitter method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateCommitterCreatesDefaultIfNull() throws Exception {
        VersionCommittable committer = this.factory.createCommitter(
            this.project,
            this.filePath,
            null
        );
        
        assertTrue(committer instanceof AutoCreatingPropertyFileVersionHandler);
        
        AutoCreatingPropertyFileVersionHandler castCommitter = (AutoCreatingPropertyFileVersionHandler) committer;
        
        assertSame(this.propertyFile, castCommitter.getPropertyFilePath());
        assertTrue(castCommitter.getFileHandler() instanceof PropertyFileIoHandler);
        assertTrue(castCommitter.getVersionFactory() instanceof StaticVersionFactory);
        
        verify(this.pathProvider, times(1)).getPropertyFilePath(same(this.project), same(this.filePath));
    }
    
    /**
     * Test of createRetriever method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateRetrieverReturnsPassedValueIfNotNull() throws Exception {
        VersionRetrievable currentRetriever = mock(VersionRetrievable.class);
        
        assertSame(
            currentRetriever,
            this.factory.createRetriever(
                this.project,
                this.filePath,
                currentRetriever
            )
        );
    }
    
    /**
     * Test of createRetriever method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateRetrieverCreatesDefaultIfNull() throws Exception {
        VersionRetrievable retriever = this.factory.createRetriever(
            this.project,
            this.filePath,
            null
        );
        
        assertTrue(retriever instanceof AutoCreatingPropertyFileVersionHandler);
        
        AutoCreatingPropertyFileVersionHandler castRetriever = (AutoCreatingPropertyFileVersionHandler) retriever;
        
        assertSame(this.propertyFile, castRetriever.getPropertyFilePath());
        assertTrue(castRetriever.getFileHandler() instanceof PropertyFileIoHandler);
        assertTrue(castRetriever.getVersionFactory() instanceof StaticVersionFactory);
        
        verify(this.pathProvider, times(1)).getPropertyFilePath(same(this.project), same(this.filePath));
    }

    /**
     * Test of createUpdater method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateUpdaterReturnsPassedValueIfNotNull() {
        VersionNumberUpdater updater = mock(VersionNumberUpdater.class);
        
        assertSame(updater, this.factory.createUpdater(updater));
    }

    /**
     * Test of createUpdater method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateUpdaterCreatesDefaultIfNull() {
        assertTrue(this.factory.createUpdater(null) instanceof VersionNumberUpdater);
    }

    /**
     * Test of createVarExporter method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateVarExporterReturnsPassedValueIfNotNull() {
        VariableExporter varExporter = mock(VariableExporter.class);
        
        assertSame(varExporter, this.factory.createVarExporter(varExporter));
    }

    /**
     * Test of createVarExporter method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateVarExporterCreatesDefaultIfNull() {
        VariableExporter exporter = this.factory.createVarExporter(null);
        
        assertNotNull(exporter);
        
        try {
            Field actionField = exporter.getClass().getDeclaredField("action");
            actionField.setAccessible(true);
            
            assertTrue(actionField.get(exporter) instanceof AddEnvVarsAction);
        } catch (ReflectiveOperationException ex) {
            fail("Error accessing 'action' property");
        }
    }

    /**
     * Test of createVersionFactory method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateVersionFactoryReturnsPassedValueIfNotNull() {
        VersionFactory versionFactory = mock(VersionFactory.class);
        
        assertSame(versionFactory, this.factory.createVersionFactory(versionFactory));
    }
    
    /**
     * Test of createVersionFactory method, of class LazyLoadingServiceFactory.
     */
    @Test
    public void testCreateVersionFactoryCreatesDefaultIfNull() {
        assertTrue(this.factory.createVersionFactory(null) instanceof VersionFactory);
    }
    
    /**
     * Test of createTokenExpansionProvider method, of class LazyLoadingServiceFactory
     */
    @Test
    public void testCreateTokenExpansionProviderReturnsPassedValueIfNotNull() {
        TokenExpansionProvider tokenProvider = mock(TokenExpansionProvider.class);
        
        assertSame(tokenProvider, this.factory.createTokenExpansionProvider(tokenProvider));
    }
    
    /**
     * Test of createTokenExpansionProvider method, of class LazyLoadingServiceFactory
     */
    @Test
    public void testCreateTokenExpansionProviderCreatesDefaultIfNull() {
        assertTrue(this.factory.createTokenExpansionProvider(null) instanceof SingletonCallTokenExpansionProvider);
    }
    
}
