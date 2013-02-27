/*
 * Copyright 2013 Jean-Francois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.cpr;

import org.atmosphere.container.BlockingIOCometSupport;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
<<<<<<< HEAD
=======
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
<<<<<<< HEAD
>>>>>>> b8ae6da... Fix for #938
=======
>>>>>>> b8ae6da... Fix for #938
import java.util.concurrent.atomic.AtomicReference;

import static org.atmosphere.cpr.ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class AtmosphereResourceTest {
    private AtmosphereFramework framework;

    @BeforeMethod
    public void create() throws Throwable {
        framework = new AtmosphereFramework();
        framework.setAsyncSupport(new BlockingIOCometSupport(framework.getAtmosphereConfig()));
        framework.init(new ServletConfig() {
            @Override
            public String getServletName() {
                return "void";
            }

            @Override
            public ServletContext getServletContext() {
                return mock(ServletContext.class);
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        });
    }


    @Test
    public void testUUID() throws IOException, ServletException {
        framework.addAtmosphereHandler("/a", new AbstractReflectorAtmosphereHandler() {
            @Override
            public void onRequest(AtmosphereResource resource) throws IOException {
            }

            @Override
            public void destroy() {
            }
        });

        AtmosphereRequest request = new AtmosphereRequest.Builder().pathInfo("/a").build();

        final AtomicReference<String> e = new AtomicReference<String>();
        final AtomicReference<String> e2 = new AtomicReference<String>();

        framework.interceptor(new AtmosphereInterceptor() {
            @Override
            public void configure(AtmosphereConfig config) {
            }

            @Override
            public Action inspect(AtmosphereResource r) {
                e.set(r.uuid());
                e2.set(r.getResponse().getHeader(HeaderConfig.X_ATMOSPHERE_TRACKING_ID));
                return Action.CANCELLED;
            }

            @Override
            public void postInspect(AtmosphereResource r) {
            }
        });
        framework.doCometSupport(request, AtmosphereResponse.newInstance());

        assertEquals(e.get(), e2.get());
    }

    @Test
    public void testCancelParentUUID() throws IOException, ServletException, InterruptedException {
        framework.addAtmosphereHandler("/a", new AbstractReflectorAtmosphereHandler() {
            @Override
            public void onRequest(AtmosphereResource resource) throws IOException {
            }

            @Override
            public void destroy() {
            }
        });

        final AtmosphereRequest parentRequest = new AtmosphereRequest.Builder().pathInfo("/a").build();

        final CountDownLatch suspended = new CountDownLatch(1);

        framework.interceptor(new AtmosphereInterceptor() {
            @Override
            public void configure(AtmosphereConfig config) {
            }

            @Override
            public Action inspect(AtmosphereResource r) {
                try {
                    r.getBroadcaster().addAtmosphereResource(r);
                    return suspended.getCount() == 1 ? Action.SUSPEND : Action.CONTINUE;
                } finally {
                    suspended.countDown();
                }
            }

            @Override
            public void postInspect(AtmosphereResource r) {
            }
        });

        new Thread() {
            public void run() {
                try {
                    framework.doCometSupport(parentRequest, AtmosphereResponse.create());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServletException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        suspended.await();
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(SUSPENDED_ATMOSPHERE_RESOURCE_UUID, parentRequest.resource().uuid());

        AtmosphereRequest request = new AtmosphereRequest.Builder().attributes(m).pathInfo("/a").build();
        framework.doCometSupport(request, AtmosphereResponse.create());

        AtmosphereResource r = parentRequest.resource();
        Broadcaster b = r.getBroadcaster();

        assertEquals(b.getAtmosphereResources().size(), 1);

        AtmosphereResourceImpl.class.cast(r).cancel();

        assertEquals(b.getAtmosphereResources().size(), 0);

    }

}
