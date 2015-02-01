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

import hudson.EnvVars;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class AddEnvVarsActionTest {
    
    private Map<String, String> envVars;
    private AddEnvVarsAction addEnvVarsAction;
    
    @Before
    public void setUp() {
        this.envVars = mock(Map.class);
        
        this.addEnvVarsAction = new AddEnvVarsAction(
            this.envVars
        );
    }
    
    @Test
    public void testEmptyConstructorCreatesHashMap() {
        AddEnvVarsAction addEnvVarsAction = new AddEnvVarsAction();
        
        try {
            Field envVarsField = addEnvVarsAction.getClass().getDeclaredField("envVars");
            envVarsField.setAccessible(true);

            Object value = envVarsField.get(addEnvVarsAction);
            
            assertTrue(value instanceof HashMap);
            assertNotSame(this.envVars, value);
        } catch (Throwable t) {
            fail(
                "Exception occurred attempt to access protected field 'envVars' value: "
                    + t.getClass().getSimpleName()
                    + " - "
                    + t.getMessage()
            );
        }
    }
    
    @Test
    public void testConstructorAssignsInjectedValue() {
        try {
            Field envVarsField = this.addEnvVarsAction.getClass().getDeclaredField("envVars");
            envVarsField.setAccessible(true);

            Object value = envVarsField.get(this.addEnvVarsAction);
            
            assertSame(this.envVars, value);
        } catch (Throwable t) {
            fail(
                "Exception occurred attempt to access protected field 'envVars' value: "
                    + t.getClass().getSimpleName()
                    + " - "
                    + t.getMessage()
            );
        }
    }

    @Test
    public void testBuildEnvVars() {
        EnvVars env = mock(EnvVars.class);
        
        doNothing().when(env).putAll(eq(this.envVars));
        
        this.addEnvVarsAction.buildEnvVars(null, env);
        
        verify(env, times(1)).putAll(eq(this.envVars));
    }

    @Test
    public void testStaticGetters() {
        assertNull(this.addEnvVarsAction.getIconFileName());

        assertEquals(
            "Export variables for current and previous version",
            this.addEnvVarsAction.getDisplayName()
        );
        
        assertEquals(
            "versioning",
            this.addEnvVarsAction.getUrlName()
        );
    }
}
