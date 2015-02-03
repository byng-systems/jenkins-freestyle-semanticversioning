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
package co.byng.versioningplugin.handler.file;

import co.byng.versioningplugin.versioning.VersionFactory;
import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoCreatingPropertyFileVersionHandlerTest {
    
    private PropertyFileIoHandler ioHandler;
    private VersionFactory versionFactory;
    private AutoCreatingPropertyFileVersionHandler propHandler;
    
    @Before
    public void setUp() {
        this.ioHandler = mock(PropertyFileIoHandler.class);
        this.versionFactory = mock(VersionFactory.class);
        this.propHandler = new AutoCreatingPropertyFileVersionHandler(
            this.ioHandler,
            this.versionFactory
        );
    }
    
    @Test
    public void testConstructorsCallSuperclassConstructors() {
        assertSame(this.ioHandler, this.propHandler.getFileHandler());
        assertSame(this.versionFactory, this.propHandler.getVersionFactory());
        
        File propertyPath = mock(File.class);
        this.propHandler = new AutoCreatingPropertyFileVersionHandler(
            this.ioHandler,
            this.versionFactory,
            propertyPath
        );
        assertSame(this.ioHandler, this.propHandler.getFileHandler());
        assertSame(this.versionFactory, this.propHandler.getVersionFactory());
        assertSame(propertyPath, this.propHandler.getPropertyFilePath());
        
        String propertyKey = "VERSION KEY";
        this.propHandler = new AutoCreatingPropertyFileVersionHandler(
            this.ioHandler,
            this.versionFactory,
            propertyPath,
            propertyKey
        );
        assertSame(this.ioHandler, this.propHandler.getFileHandler());
        assertSame(this.versionFactory, this.propHandler.getVersionFactory());
        assertSame(propertyPath, this.propHandler.getPropertyFilePath());
        assertSame(propertyKey, this.propHandler.getPropertyKey());
    }

    /**
     * Test of loadVersion method, of class AutoCreatingPropertyFileVersionHandler.
     */
    @Test
    public void testLoadVersionSavesDefaultVersionForNonExistentFile() throws IOException {
        File propertyFile = mock(File.class);
        when(propertyFile.exists()).thenReturn(false, true);
        
        final String propertyKey = "PROPERTY KEY";
        final String versionString = "VERSION STRING";
        final Version version = mock(Version.class);
        when(this.versionFactory.buildVersionFromString(same(AutoCreatingPropertyFileVersionHandler.DEFAULT_VERSION_STRING))).thenReturn(version);
        when(this.versionFactory.buildVersionFromString(same(versionString))).thenReturn(version);
        when(version.toString()).thenReturn(versionString);
        
        Properties properties = mock(Properties.class);
        when(properties.setProperty(same(propertyKey), same(versionString))).thenReturn(null);
        when(this.ioHandler.loadPropertiesFromFile(same(propertyFile))).thenReturn(properties);
        
        this.propHandler
                .setPropertyFilePath(propertyFile)
                .setPropertyKey(propertyKey)
                .loadVersion()
        ;
        
        verify(propertyFile, times(2)).exists();
        
        verify(this.versionFactory, times(1)).buildVersionFromString(same(AutoCreatingPropertyFileVersionHandler.DEFAULT_VERSION_STRING));
        verify(version, times(1)).toString();
        
        verify(properties, times(1)).setProperty(same(propertyKey), same(versionString));
        verify(this.ioHandler, times(1)).loadPropertiesFromFile(same(propertyFile));
    }
    
}
