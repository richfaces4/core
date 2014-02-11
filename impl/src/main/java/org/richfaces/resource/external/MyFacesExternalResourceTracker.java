/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.resource.external;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.richfaces.application.ServiceTracker;
import org.richfaces.log.Logger;
import org.richfaces.log.RichfacesLogger;
import org.richfaces.resource.ResourceKey;

/**
 * Tracks what external resources are renderered to the page (specific for MyFaces)
 *
 * @author Lukas Fryc
 */
public class MyFacesExternalResourceTracker implements ExternalResourceTracker {

    private Class<?> resourceUtilsClass;

    private static final Logger LOG = RichfacesLogger.RESOURCE.getLogger();

    public MyFacesExternalResourceTracker(Class<?> resourceUtilsClass) {
        this.resourceUtilsClass = resourceUtilsClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.richfaces.resource.external.ExternalResourceTracker#isResourceRenderered(javax.faces.context.FacesContext,
     * org.richfaces.resource.ResourceKey)
     */
    @Override
    public boolean isResourceRenderered(FacesContext facesContext, ResourceKey resourceKey) {
        final String mimeType = facesContext.getExternalContext().getMimeType(resourceKey.getResourceName());

        try {
            if (MimeType.STYLESHEET.contains(mimeType)) {
                return (Boolean) resourceUtilsClass.getMethod("isRenderedStylesheet", FacesContext.class, String.class,
                    String.class).invoke(null, facesContext, resourceKey.getLibraryName(), resourceKey.getResourceName());
            } else if (MimeType.SCRIPT.contains(mimeType)) {
                return (Boolean) resourceUtilsClass.getMethod("isRenderedScript", FacesContext.class, String.class,
                    String.class).invoke(null, facesContext, resourceKey.getLibraryName(), resourceKey.getResourceName());
            }
        } catch (IllegalAccessException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (IllegalArgumentException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (InvocationTargetException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (NoSuchMethodException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (SecurityException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.richfaces.resource.external.ExternalResourceTracker#markResourceRendered(javax.faces.context.FacesContext,
     * org.richfaces.resource.ResourceKey)
     */
    @Override
    public void markResourceRendered(FacesContext facesContext, ResourceKey resourceKey) {
        final String mimeType = facesContext.getExternalContext().getMimeType(resourceKey.getResourceName());

        try {
            if (MimeType.STYLESHEET.contains(mimeType)) {
                resourceUtilsClass.getMethod("markStylesheetAsRendered", FacesContext.class, String.class, String.class)
                    .invoke(null, facesContext, resourceKey.getLibraryName(), resourceKey.getResourceName());
            } else if (MimeType.SCRIPT.contains(mimeType)) {
                resourceUtilsClass.getMethod("markScriptAsRendered", FacesContext.class, String.class, String.class).invoke(
                    null, facesContext, resourceKey.getLibraryName(), resourceKey.getResourceName());
            }
        } catch (IllegalAccessException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (IllegalArgumentException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (InvocationTargetException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (NoSuchMethodException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        } catch (SecurityException e) {
            LOG.error("error while delegating resource handling to myfaces impl", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.richfaces.resource.external.ExternalResourceTracker#markExternalResourceRendered(javax.faces.context.FacesContext,
     * org.richfaces.resource.external.ExternalResource)
     */
    @Override
    public void markExternalResourceRendered(FacesContext facesContext, ExternalResource resource) {
        ExternalStaticResourceFactory externalStaticResourceFactory = ServiceTracker
            .getService(ExternalStaticResourceFactory.class);

        ResourceKey originalResourceKey = ResourceKey.create(resource);
        Set<ResourceKey> resourcesKeys = externalStaticResourceFactory.getResourcesForLocation(resource.getExternalLocation());

        for (ResourceKey resourceKey : resourcesKeys) {
            if (!originalResourceKey.equals(resourceKey)) {
                markResourceRendered(facesContext, resourceKey);
            }
        }
    }

    private enum MimeType {
        SCRIPT("application/javascript", "text/javascript"),
        STYLESHEET("text/css");

        private String[] types;

        private MimeType(String... types) {
            this.types = types;
        }

        public boolean contains(String type) {
            return Arrays.asList(types).contains(type);
        }
    }
}
